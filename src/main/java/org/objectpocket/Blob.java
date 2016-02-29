/*
 * Copyright (C) 2015 Edmund Klaus
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

package org.objectpocket;

import java.io.IOException;
import java.util.UUID;

import org.objectpocket.storage.BlobStore;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class Blob {

	private String id = UUID.randomUUID().toString();
	private String path;
	private transient byte[] bytes;
	private transient boolean persist = false;
	private transient BlobStore blobStore;

	public String getId() {
		return id;
	}
	public final String getPath() {
		return path;
	}
	public final void setPath(String path) {
		this.path = path;
	}
	public final byte[] getBytes() throws IOException {
		if (bytes == null) {
			bytes = blobStore.loadBlobData(this);
		}
		return bytes;
	}
	public final void setBytes(byte[] bytes) {
		this.bytes = bytes;
		persist = true;
	}
	public final boolean isPersist() {
		if (bytes != null && bytes.length > 0) {
			return persist;
		}
		return false;
	}
	public final BlobStore getBlobStore() {
		return blobStore;
	}
	public final void setBlobStore(BlobStore blobStore) {
		this.blobStore = blobStore;
	}

}
