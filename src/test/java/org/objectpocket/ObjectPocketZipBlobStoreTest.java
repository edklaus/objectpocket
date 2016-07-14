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

package org.objectpocket;

import org.junit.Test;
import org.objectpocket.storage.FileStore;
import org.objectpocket.storage.blob.ZipBlobStore;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketZipBlobStoreTest extends ObjectPocketFileBlobStoreTest {

    @Override
    public ObjectPocket getObjectPocket() throws Exception {
	if (objectPocket != null) {
	    objectPocket.close();
	}
	ObjectPocketBuilder objectPocketBuilder = new ObjectPocketBuilder();
	FileStore objectStore = new FileStore(FILESTORE);
	blobStore = new ZipBlobStore(FILESTORE);
	objectStore.setBlobStore(blobStore);
	objectPocket = objectPocketBuilder.createObjectPocket(objectStore);
	return objectPocket;
    }
    
    @Test
    public void testAddToSamePath() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	Bean b1 = new Bean("bean1");
	Blob blob1 = new Blob();
	byte[] bytes = new byte[]{1,2};
	blob1.setBytes(bytes);
	blob1.setPath("testpath/test/blob1.data");
	b1.setBlob(blob1);
	Bean b2 = new Bean("bean2");
	Blob blob2 = new Blob();
	blob2.setBytes(bytes);
	blob2.setPath("testpath/test/blob2.data");
	b2.setBlob(blob2);
	objectPocket.add(b1);
	objectPocket.store();
	objectPocket.load();
	objectPocket.add(b2);
	objectPocket.store();
    }
    
    @Test
    public void testAddAfterRead() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	Bean b1 = new Bean("bean1");
	Blob blob1 = new Blob();
	byte[] bytes = new byte[]{1,2};
	blob1.setBytes(bytes);
	blob1.setPath("testpath/test/blob1.data");
	b1.setBlob(blob1);
	Bean b2 = new Bean("bean2");
	Blob blob2 = new Blob();
	blob2.setBytes(bytes);
	blob2.setPath("testpath/test/blob2.data");
	b2.setBlob(blob2);
	objectPocket.add(b1);
	objectPocket.store();
	objectPocket.load();
	Bean found = objectPocket.findAll(Bean.class).iterator().next();
	found.getBlob().getBytes();
	objectPocket.add(b2);
	objectPocket.store();
    }

}
