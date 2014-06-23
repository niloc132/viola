package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.web.server.mvp.ViolaServerApp;
import com.google.gwt.safehtml.shared.UriUtils;
import one.xio.AsioVisitor.Impl;
import one.xio.HttpHeaders;
import one.xio.HttpStatus;
import rxf.core.Rfc822HeaderState;
import rxf.couch.gen.CouchDriver;
import rxf.shared.PreRead;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.channels.SelectionKey.OP_READ;

@PreRead
public class HttpProxyImpl extends Impl  {
	private final Pattern passthroughExpr;
	private final String prefix;
	private final String suffix;

	public HttpProxyImpl(Pattern passthroughExpr, String prefix, String suffix) {
		this.passthroughExpr = passthroughExpr;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	@Override
	public void onRead(SelectionKey key) throws Exception {
		Rfc822HeaderState.HttpRequest req1 = null;
		if (key.attachment() instanceof Object[]) {
			Object[] ar = (Object[]) key.attachment();
			for (Object o : ar) {
				if (o instanceof Rfc822HeaderState) {
					req1 = ((Rfc822HeaderState) o).$req();
					break;
				}
			}
		}
		final Rfc822HeaderState.HttpRequest req = req1;
		if (req == null) {
			Errors.$500(key);
			return;//fail, something miswired
		}
		final String path = req.path();

		Matcher matcher = passthroughExpr.matcher(path);
		if (!matcher.matches()) {
			Errors.$404(key, path);
			return;
		}
		String link = UriUtils.sanitizeUri(prefix + matcher.group(1) + suffix);
		if (link.endsWith("/")) {
			link += "index.html";
		}
		CouchDriver.DocFetch.DocFetchActionBuilder to = new CouchDriver.DocFetch().db("").docId(link).to();
		final Rfc822HeaderState.HttpResponse state = to.state().$res();
		state.addHeaderInterest(HttpHeaders.Content$2dType);
		final Future<ByteBuffer> result = to.fire().future();

		key.attach(new Impl() {
			ByteBuffer payload;
			@Override
			public void onWrite(final SelectionKey key) throws Exception {
				final SocketChannel channel = (SocketChannel) key.channel();
				//when outbound connection is ready, see if data is ready
				if (payload != null) {
					//if so, write it
					channel.write(payload);
					if (!payload.hasRemaining()) {
						//if we're done, close it up
						key.selector().wakeup();
						key.interestOps(OP_READ).attach(null);
					}
					return;
				}
				//if not, wait until it is ready, then try again
				key.interestOps(0);
				ViolaServerApp.WEBAPP_THREADS.submit(new Runnable() {
					@Override
					public void run() {
						try {
							ByteBuffer buffer = result.get();
							
							if (state.statusEnum() != HttpStatus.$200) {
								Errors.$404(key, path);
								return;
							}

							ByteBuffer headers = req.$res()
									.status(state.statusEnum())
									.headerString(HttpHeaders.Content$2dType, state.headerString(HttpHeaders.Content$2dType))
									.headerString(HttpHeaders.Content$2dLength, state.headerString(HttpHeaders.Content$2dLength))
									.as(ByteBuffer.class);

							int needed = headers.rewind().limit() + buffer.rewind().limit();
							payload = (ByteBuffer) ByteBuffer.allocateDirect(needed).put(headers).put(buffer).rewind();

							key.interestOps(SelectionKey.OP_WRITE);
						} catch (Exception e) {
							Errors.$500(key);
							e.printStackTrace();
						}
					}
				});
			}
		});
		key.interestOps(SelectionKey.OP_WRITE);
	}

}

