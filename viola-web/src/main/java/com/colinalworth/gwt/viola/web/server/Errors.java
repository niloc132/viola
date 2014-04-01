package com.colinalworth.gwt.viola.web.server;

import one.xio.AsioVisitor.Impl;
import one.xio.HttpHeaders;
import one.xio.HttpMethod;
import one.xio.HttpStatus;
import rxf.server.Rfc822HeaderState;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public final class Errors {
	public static void $303(SelectionKey key, String newUrl) {
		key.attach(new Impl() {
			@Override
			public void onWrite(SelectionKey key) throws Exception {

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

	private static void error(SelectionKey key, final HttpStatus code, final String html) {
		key.attach(new Impl() {
			@Override
			public void onWrite(SelectionKey key) throws Exception {
				ByteBuffer resp = new Rfc822HeaderState().$res()
						.resCode(code)
						.headerString(HttpHeaders.Content$2dType, "text/html")
						.headerString(HttpHeaders.Content$2dLength, String.valueOf(html.length()))
						.as(ByteBuffer.class);
				
				((SocketChannel) key.channel()).write(resp);
				((SocketChannel) key.channel()).write(HttpMethod.UTF8.encode(html));
				key.selector().wakeup();
				key.interestOps(SelectionKey.OP_READ).attach(null);
			}
		});
		key.interestOps(SelectionKey.OP_WRITE);
	}
}
