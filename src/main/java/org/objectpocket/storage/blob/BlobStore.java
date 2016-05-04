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

package org.objectpocket.storage.blob;

import java.io.IOException;
import java.util.Set;

import org.objectpocket.Blob;

/**
 * 
 * @author Edmund Klaus
 *
 */
public interface BlobStore {

    /**
     * Write blobs to blob store.
     * 
     * @param blobs
     * @throws IOException
     *             If an I/O error occurs
     */
    public void writeBlobs(Set<Blob> blobs) throws IOException;

    /**
     * Load binary data for the given Blob object.
     * 
     * @param blob
     * @return
     * @throws IOException
     *             If an I/O error occurs
     */
    public byte[] loadBlobData(Blob blob) throws IOException;

    /**
     * Removes not referenced blobs from BlobStore.
     * 
     * @param Set
     *            containing referenced Blobs
     */
    public void cleanup(Set<Blob> referencedBlobs) throws IOException;

    /**
     * close blob store
     */
    public void close() throws IOException;

    /**
     * delete a blob store
     * 
     * @throws IOException
     */
    public void delete() throws IOException;
    
}
