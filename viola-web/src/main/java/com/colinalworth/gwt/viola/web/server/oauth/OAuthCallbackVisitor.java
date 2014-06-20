package com.colinalworth.gwt.viola.web.server.oauth;

import com.colinalworth.gwt.viola.entity.User;
import com.colinalworth.gwt.viola.service.UserService;
import com.colinalworth.gwt.viola.web.server.Errors;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.auth.openidconnect.IdTokenVerifier;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import one.xio.AsioVisitor.Impl;
import one.xio.HttpHeaders;
import one.xio.HttpMethod;
import one.xio.HttpStatus;
import org.apache.commons.io.IOUtils;
import rxf.server.BlobAntiPatternObject;
import rxf.server.PreRead;
import rxf.server.Rfc822HeaderState;
import rxf.server.Rfc822HeaderState.HttpRequest;
import rxf.server.driver.CouchMetaDriver;
import rxf.server.driver.RxfBootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static one.xio.HttpHeaders.Host;

public class OAuthCallbackVisitor extends Impl implements PreRead {
	private static final ExecutorService execs = Executors.newFixedThreadPool(2);

	private String clientId = "888496828889-cjuie9aotun74v1p9tbrb568rchtjkc9.apps.googleusercontent.com";
	private String clientSecret = "SECRET";
	private String accessTokenUrl = "https://accounts.google.com/o/oauth2/token";
	private String issuer = "accounts.google.com";

	private String serverUrl = RxfBootstrap.getVar("url", "https://viola.colinalworth.com");

	@Inject
	UserService userService;

	private String sessionId;
	private String userid;
	private String displayName;
	private boolean newAccount = false;

	@Override
	public void onRead(SelectionKey key) throws Exception {
		ByteBuffer cursor = null;
		if (key.attachment() instanceof Object[]) {
			for (Object a : (Object[]) key.attachment()) {
				if (a instanceof ByteBuffer) {
					cursor = (ByteBuffer) a;
					break;
				}
			}
		}

		if (cursor == null) {
			Errors.$500(key);
			return;
		}

		final HttpRequest req =
				(HttpRequest) new Rfc822HeaderState().addHeaderInterest(Host).$req().apply(cursor);


		String[] pathParts = req.path().split("\\?");
		if (pathParts.length != 2) {
			Errors.$500(key);
		}
		String[] args = pathParts[1].split("&");

		String code = null;
		String state = null;
		String error = null;

		for (String arg : args) {
			if (arg.startsWith("code=")) {
				code = arg.substring("code=".length());
			} else if (arg.startsWith("state=")) {
				state = arg.substring("state=".length());
			} else if (arg.startsWith("error=")) {
				error = arg.substring("error=".length());
			}
		}

		if (error != null) {
			//TODO actually indicate failed auth
			Errors.$400(key);
			return;
		}

		//TODO verify state (though we need to get the original url to do that...)

		//next, POST to access_token
		execs.submit(new AccessTokenFetch(code, key));
		key.interestOps(SelectionKey.OP_READ).attach(null);
	}

	@Override
	public void onWrite(SelectionKey key) throws Exception {
		assert sessionId != null;
		//write back out a constant/template to say 'yep, loaded, here's what the app should use to get its credentials'
		//TODO escape this stuff...
		String str = "<html><body>Authentication successful, finishing login...<script>setTimeout(close, 500); opener.authSuccess('" + sessionId + "', '"+ userid + "', " + newAccount + ")</script></html></body>";

		ByteBuffer resp = new Rfc822HeaderState().$res()
				.status(HttpStatus.$200)
				.headerString(HttpHeaders.Content$2dType, "text/html")
				.headerString(HttpHeaders.Content$2dLength, String.valueOf(str.length()))
				.as(ByteBuffer.class);

		((SocketChannel) key.channel()).write(resp);
		((SocketChannel) key.channel()).write(HttpMethod.UTF8.encode(str));
		key.selector().wakeup();
		key.interestOps(SelectionKey.OP_READ).attach(null);
	}


	private class AccessTokenFetch implements Runnable {
		private final String code;
		private final SelectionKey key;

		public AccessTokenFetch(String code, SelectionKey key) {
			this.code = code;
			this.key = key;
		}

		@Override
		public void run() {
			HttpURLConnection c = null;
			try {
				String redirectUri = serverUrl + "/oauth2callback";
				String data = "redirect_uri=" + redirectUri + "&grant_type=authorization_code&client_id=" + clientId + "&client_secret=" + clientSecret + "&code=" + code;
				//github:
				//https://github.com/login/oauth/access_token
				//client_id
				//client_secret
				//code
				//redirect_uri (optional, probably ignore here)
				//grant_type=authorization_code

				//StackExchange:
				//https://stackexchange.com/oauth/access_token

				//Google:
				//https://accounts.google.com/o/oauth2/token

				//TODO gut this when we can instead make ssl calls via 1xio
				c = (HttpURLConnection) new URL(accessTokenUrl).openConnection();
				c.setDoOutput(true);
				c.setRequestMethod("POST");
				c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				c.setRequestProperty("Content-Length", String.valueOf(data.length()));
				c.setRequestProperty("Accept", "application/json");
				c.getOutputStream().write(data.getBytes());

				c.connect();

				InputStream result = c.getInputStream();
				String resultString = IOUtils.toString(result);
				JsonObject resultObj = new JsonParser().parse(resultString).getAsJsonObject();

				//get back these args:
				String access_token = resultObj.get("access_token").getAsString();
				String id_token = resultObj.get("id_token").getAsString(); //goog only, can't find a consistent way to get a cross-access_token id from se or gh
				int expires = resultObj.get("expires_in").getAsInt();

				//submit task to save, and create session
				execs.submit(new CreateSession(access_token, id_token, key));
			} catch (IOException e) {
				Errors.$500(key);
				try {
					if (c != null && c.getInputStream() != null) {
						System.err.println(IOUtils.toString(c.getInputStream()));
					}
				} catch (IOException ex) {
					//ignore, already handling an exception
				}
				e.printStackTrace();
			}
		}
	}
	public class CreateSession implements Runnable {
		private final String access_token;
		private final String id_token;
		private final SelectionKey key;

		public CreateSession(String access_token, String id_token, SelectionKey key) {

			this.access_token = access_token;
			this.id_token = id_token;
			this.key = key;
		}

		@Override
		public void run() {
			IdTokenVerifier verifier = new IdTokenVerifier.Builder()
					.setAcceptableTimeSkewSeconds(60)
					.setAudience(Arrays.asList(clientId))
					.setIssuer(issuer)
					.build();

			IdToken token;
			try {
				token = IdToken.parse(GsonFactory.getDefaultInstance(), id_token);
				if (!verifier.verify(token)) {
					//TODO invalid creds, can't proceed
					Errors.$400(key);
				}
			} catch (IOException e) {
				e.printStackTrace();
				Errors.$500(key);
				return;
			}
			//find user, if any
			User user = userService.findUserWithIdToken(token.getPayload().getSubject(), issuer);


			if (user == null) {
				//if not, try to create
				user = userService.createUserWithToken(token.getPayload().getSubject(), issuer, "user" + token.getPayload().getSubject());
				if (user == null) {
					//failed to create new user, something is wrong
					Errors.$500(key);
					return;
				}
				newAccount = true;
			}
			//once user has been created, create session
			String sessionId = userService.createSession(user);

			if (sessionId == null) {
				Errors.$500(key);
				return;
			}

			//one session created, respond back with sessionid
			OAuthCallbackVisitor.this.sessionId = sessionId;
			OAuthCallbackVisitor.this.userid = user.getId();
			OAuthCallbackVisitor.this.displayName = user.getDisplayName();
			key.interestOps(SelectionKey.OP_WRITE).attach(OAuthCallbackVisitor.this);
		}
	}
}
