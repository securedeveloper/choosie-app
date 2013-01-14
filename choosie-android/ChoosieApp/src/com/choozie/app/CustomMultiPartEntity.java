package com.choozie.app;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;


public class CustomMultiPartEntity extends MultipartEntity {

	Callback<Void, Integer, Void> progressCallback;

	// private final ProgressListener listener;

	public CustomMultiPartEntity(
			final Callback<Void, Integer, Void> progressCallback) {
		super();
		this.progressCallback = progressCallback;
	}

	public CustomMultiPartEntity(final HttpMultipartMode mode,
			final Callback<Void, Integer, Void> progressCallback) {
		super(mode);
		this.progressCallback = progressCallback;
	}

	public CustomMultiPartEntity(HttpMultipartMode mode, final String boundary,
			final Charset charset,
			final Callback<Void, Integer, Void> progressCallback) {
		super(mode, boundary, charset);
		this.progressCallback = progressCallback;
	}

	@Override
	public void writeTo(final OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.progressCallback));
	}

	// public static interface ProgressListener
	// {
	// void transferred(long param);
	// }

	public static class CountingOutputStream extends FilterOutputStream {

		private final Callback<Void, Integer, Void> progressCallback;
		private int transferred;

		public CountingOutputStream(final OutputStream out,
				final Callback<Void, Integer, Void> progressCallback) {
			super(out);
			this.progressCallback = progressCallback;
			this.transferred = 0;
		}

		public void write(byte[] b, int off, int len) throws IOException {
			final int kBufferSize = 1024;
			int bytesWritten = 0;
			int remaining = len;
			while (remaining > 0) {
				int toWrite = Math.min(remaining, kBufferSize);
				out.write(b, off + bytesWritten, toWrite);
				bytesWritten += toWrite;
				
				this.transferred += toWrite;
				this.progressCallback.onProgress(this.transferred);
				remaining = len - bytesWritten;
			}
		}

		public void write(int b) throws IOException {
			out.write(b);
			this.transferred++;
			this.progressCallback.onProgress(this.transferred);
		}
	}

}
