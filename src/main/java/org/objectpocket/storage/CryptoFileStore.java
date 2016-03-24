/*
 * Copyright (C) 2016 Edmund Klaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.objectpocket.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class CryptoFileStore extends FileStore {
	
	private byte shift;

	public CryptoFileStore(String directory, String password) {
		super(directory);
		if (password != null && !password.isEmpty()) {
			byte[] bytes = password.getBytes();
			for (byte b : bytes) {
				shift += b;
			}
		}
	}

	protected OutputStreamWriter getOutputStreamWriter(String filename) throws IOException {
		File file = initFile(filename, true, true);
		return new OutputStreamWriter(new CryptedOutputstream(new FileOutputStream(file)));
	}

	protected BufferedReader getBufferedReader(String filename) throws IOException {
		File file = initFile(filename, true, false);
		return new BufferedReader(new InputStreamReader(new CryptedInputStream(new FileInputStream(file))));
	}
	
	private class CryptedOutputstream extends OutputStream {
		private OutputStream delegate;
		public CryptedOutputstream(OutputStream delegate) {
			super();
			this.delegate = delegate;
		}
		@Override
		public void write(int b) throws IOException {
			delegate.write(b ^ shift);
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			for (int i = off; i < len; i++) {
				b[i] = (byte)(b[i] ^ shift);
			}
			delegate.write(b, off, len);
		}
	}
	
	private class CryptedInputStream extends InputStream {
		private InputStream delegate;
		public CryptedInputStream(InputStream delegate) {
			super();
			this.delegate = delegate;
		}
		@Override
		public int read() throws IOException {
			return delegate.read() ^ shift;
		}
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int result = delegate.read(b, off, len);
			for (int i = off; i < result; i++) {
				b[i] = (byte)(b[i] ^ shift);
			}
			return result;
		}
	}
	
}
