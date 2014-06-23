package com.colinalworth.gwt.viola.web.server;

import one.xio.AsioVisitor.Impl;
import one.xio.HttpHeaders;
import one.xio.HttpStatus;
import rxf.core.Rfc822HeaderState;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public final class Errors {

	public static void $301(SelectionKey key, final String newUrl) {
		redir(key, newUrl);
	}
	public static void $303(SelectionKey key, final String newUrl) {
		redir(key, newUrl);
	}

	private static void redir(SelectionKey key, final String newUrl) {
		String message = "Resource moved to <a href='" + newUrl + "'>" + newUrl + "</a>";
		final String html = "<html><head><title>Resource Moved</title></head><body><div>" + message + "</div><div><a href='/'>Back to home</a></div></body></html>";
		key.attach(new Impl() {
			@Override
			public void onWrite(SelectionKey key) throws Exception {
				ByteBuffer headers = new Rfc822HeaderState().$res()
						.status(HttpStatus.$303)
						.headerString(HttpHeaders.Content$2dType, "text/html")
						.headerString(HttpHeaders.Location, newUrl)
						.headerString(HttpHeaders.Content$2dLength, String.valueOf(html.length()))
						.as(ByteBuffer.class);

				((SocketChannel) key.channel()).write(headers);
				((SocketChannel) key.channel()).write(StandardCharsets.UTF_8.encode(html));
				key.selector().wakeup();
				key.interestOps(SelectionKey.OP_READ).attach(null);
			}
		});
		key.interestOps(SelectionKey.OP_WRITE);
	}

	public static void $400(SelectionKey key) {
		error(key, HttpStatus.$400, "Bad Request");
	}

	public static void $404(SelectionKey key, String path) {
		error(key, HttpStatus.$404, "Not Found: " + path);
	}
	public static void $500(SelectionKey key) {
		error(key, HttpStatus.$500, "Internal Server Error");
	}

	private static void error(SelectionKey key, final HttpStatus code, final String message) {
		final String html = message;
		key.attach(new Impl() {
			@Override
			public void onWrite(SelectionKey key) throws Exception {
				ByteBuffer headers = new Rfc822HeaderState().$res()
						.status(code)
						.headerString(HttpHeaders.Content$2dType, "text/html")
						.headerString(HttpHeaders.Content$2dLength, String.valueOf(html.length()))
						.as(ByteBuffer.class);
				
				((SocketChannel) key.channel()).write(headers);
				((SocketChannel) key.channel()).write(StandardCharsets.UTF_8.encode(html));
				key.selector().wakeup();
				key.interestOps(SelectionKey.OP_READ).attach(null);
			}
		});
		key.interestOps(SelectionKey.OP_WRITE);
	}
}
