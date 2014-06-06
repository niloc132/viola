package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager.PlaceFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.AcceptsView;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceBasedPresenterFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.Presenter;
import com.colinalworth.gwt.viola.web.shared.mvp.View;
import com.google.inject.Inject;
import one.xio.AsioVisitor.Impl;
import one.xio.HttpHeaders;
import one.xio.HttpMethod;
import one.xio.HttpStatus;
import rxf.server.PreRead;
import rxf.server.Rfc822HeaderState;
import rxf.server.Rfc822HeaderState.HttpRequest;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the heart of the server side of the place-&gt;html bits of the app for RelaxFactory - it runs the presenter
 * with the server implementation of the view, and takes the html output from the view to build the output html file.
 *
 */
public class ViolaServerApp extends Impl implements PreRead {
	public static ExecutorService WEBAPP_THREADS = Executors.newCachedThreadPool();


	private static final byte[][] APP_RESPONSE_TEMPLATE = {
			("<!doctype html>\n" +
					"<html>\n" +
					"<head>\n" +
					//TODO meta content goes here
					"    <title>").getBytes(),
			//insert page title here, html encoded
			("</title>\n" +
					"    <link rel=\"stylesheet\" type=\"text/css\" href=\"/static/viola/reset.css\" />\n" +
					"</head>\n" +
					"<body>\n" +
					"<noscript><div><a href='/'>Viola: a fiddle for GWT</a></div>").getBytes(),
			//insert rendered html here
			("</noscript>\n" +
					"<script language='javascript' src='/static/viola/viola.nocache.js'></script>\n" +
					"<script language='javascript'>\n").getBytes(),
			//insert valid js here
			("\n</script>\n" +
					"</body>\n" +
					"</html>").getBytes()
	};

	private static final int APP_RESPONSE_WRAPPER_SIZE = APP_RESPONSE_TEMPLATE[0].length + APP_RESPONSE_TEMPLATE[1].length + APP_RESPONSE_TEMPLATE[2].length + APP_RESPONSE_TEMPLATE[3].length;
	@Inject
	PlaceFactory placeFactory;
	@Inject
	PlaceBasedPresenterFactory presenterFactory;
	@Override
	public void onRead(final SelectionKey key) throws Exception {
		HttpRequest req1 = null;
		if (key.attachment() instanceof Object[]) {
			Object[] ar = (Object[]) key.attachment();
			for (Object o : ar) {
				if (o instanceof Rfc822HeaderState) {
					req1 = ((Rfc822HeaderState) o).$req();
				}
			}
		}
		final HttpRequest request = req1;
		if (request == null) {
			Errors.$500(key);
			return;//fail, something miswired
		}
		final String path = request.path();

		//peel off the leading '/' since the client never has it
		final Place place = placeFactory.route(path.substring(1));
		if (place == null) {
			//either we're looking at a 404, or a static file
			//assume a static file, if we can't find it, a 404 will be issued
			//TODO actually look for a static file

			Errors.$404(key, path);
			return;
		}
		final Presenter presenter = presenterFactory.getPresenterInstance(place);
		if (presenter == null) {
			Errors.$404(key, path);
			return;
		}

		//push the rest of this off into a submitted task, signal ready for write when done, write contents
		WEBAPP_THREADS.submit(new Runnable() {
			public void run() {
				final View<?>[] viewWrapper = new View[1];
				presenter.go(new AcceptsView() {
					@Override
					public void setView(View view) {
						viewWrapper[0] = view;
					}
				}, place);

				final String response = viewWrapper[0] == null ? null :
						viewWrapper[0].asSafeHtml() == null ? null :
								viewWrapper[0].asSafeHtml().asString();
				if (response == null) {
					// assume that if it returns null that it already sent back a response
					return;
				}

				String title = "Viola: a fiddle for GWT";
				int length = APP_RESPONSE_WRAPPER_SIZE + response.length() + title.length();
				ByteBuffer resp = request.$res()
						.status(HttpStatus.$200)
						.headerString(HttpHeaders.Content$2dType, "text/html")
						.headerString(HttpHeaders.Content$2dLength, String.valueOf(length))
						.as(ByteBuffer.class);
				int needed = resp.rewind().limit() + length;
				final ByteBuffer payload = (ByteBuffer) ByteBuffer.allocate(needed);
				payload.put(resp);
				payload.put(APP_RESPONSE_TEMPLATE[0]);

				payload.put(title.getBytes());
				payload.put(APP_RESPONSE_TEMPLATE[1]);
				payload.put(HttpMethod.UTF8.encode(response));
				payload.put(APP_RESPONSE_TEMPLATE[2]);
//				payload.put("".getBytes());
				payload.put(APP_RESPONSE_TEMPLATE[3]).rewind();

				//ok, data in hand, lets get ready to write
				key.attach(new Impl(){
					@Override
					public void onWrite(SelectionKey key) throws Exception {
						((SocketChannel) key.channel()).write(payload);
						if (!payload.hasRemaining()) {
							key.selector().wakeup();
							key.interestOps(SelectionKey.OP_READ).attach(null);
						}
					}
				});
				key.interestOps(SelectionKey.OP_WRITE);
			}
		});
	}
}
