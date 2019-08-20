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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectpocket.annotations.Entity;
import org.objectpocket.annotations.Id;
import org.objectpocket.storage.blob.BlobStore;

/**
 * 
 * @author Edmund Klaus
 *
 */
@Entity
public class Blob {

    @Id
    private final String id = UUID.randomUUID().toString();
    private String path;
    private transient byte[] bytes;
    private transient boolean persist = false;
    private transient BlobStore blobStore;

    public Blob() {
    }

    public Blob(byte[] bytes) {
	this(null, bytes);
    }

    public Blob(String path) {
	this(path, null);
    }

    public Blob(String path, byte[] bytes) {
	this.setPath(path);
	this.setBytes(bytes);
    }

    public String getId() {
	return id;
    }

    public final String getPath() {
	return path;
    }

    public final void setPath(String path) {
	if (path != null) {
	    this.path = path.replaceAll("\\\\", "/");
	    while(this.path.startsWith("/")) {
		this.path = this.path.substring(1);
	    }
	}
    }

    public final byte[] getBytes() throws IOException {
	if (bytes == null && blobStore != null) {
	    bytes = blobStore.loadBlobData(this);
	}
	return bytes;
    }

    public final void setBytes(byte[] bytes) {
	this.bytes = bytes;
	persist = true;
    }

    public final boolean doPersist() {
	if (bytes != null && bytes.length > 0) {
	    return persist;
	}
	return false;
    }
    
    protected void prepareToPersist() {
        try {
            getBytes();
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Could not load byte data for " + getPath(), e);
        }
        persist = true;
    }
    
    public void setPersisted() {
        persist = false;
    }

    public final BlobStore getBlobStore() {
	return blobStore;
    }

    public final void setBlobStore(BlobStore blobStore) {
	this.blobStore = blobStore;
    }

}
