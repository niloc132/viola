package com.colinalworth.gwt.viola.web;

import static java.nio.channels.SelectionKey.OP_READ;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.xio.AsioVisitor.Impl;
import one.xio.HttpHeaders;
import rxf.server.BlobAntiPatternObject;
import rxf.server.PreRead;
import rxf.server.Rfc822HeaderState;
import rxf.server.Rfc822HeaderState.HttpRequest;
import rxf.server.Rfc822HeaderState.HttpResponse;
import rxf.server.gen.CouchDriver;
import rxf.server.gen.CouchDriver.DocFetch.DocFetchActionBuilder;

import com.google.gwt.safehtml.shared.UriUtils;

public class HttpProxyImpl extends Impl implements PreRead {
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
		HttpRequest req1 = null;
		if (key.attachment() instanceof Object[]) {
			Object[] ar = (Object[]) key.attachment();
			for (Object o : ar) {
				if (o instanceof Rfc822HeaderState) {
					req1 = ((Rfc822HeaderState) o).$req();
					break;
				}
			}
		}
		final HttpRequest req = req1;
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
		DocFetchActionBuilder to = CouchDriver.DocFetch.$().db("").docId(link).to();
		final Rfc822HeaderState state = to.state();
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
				BlobAntiPatternObject.EXECUTOR_SERVICE.submit(new Runnable() {
					@Override
					public void run() {
						try {
							ByteBuffer buffer = result.get();
							
							if (!state.pathResCode().startsWith("200")) {
								Errors.$404(key, path);
								return;
							}

							HttpResponse response = req.$res();

							ByteBuffer headers = response
									.status(state.protocolStatus())
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

