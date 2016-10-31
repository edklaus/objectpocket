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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;
import org.objectpocket.storage.FileStore;
import org.objectpocket.storage.blob.MultiZipBlobStore;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class MultiZipBlobStoreTest extends ObjectPocketFileBlobStoreTest{

    @Override
    public ObjectPocket getObjectPocket() throws Exception {
        if (objectPocket != null) {
            objectPocket.close();
        }
        ObjectPocketBuilder objectPocketBuilder = new ObjectPocketBuilder();
        FileStore objectStore = new FileStore(FILESTORE);
        blobStore = new MultiZipBlobStore(FILESTORE);
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

    @Test
    public void testMultipleBlobContainerCreation() throws Exception {
        ObjectPocket objectPocket = getObjectPocket();
        long size = 0;
        Random r = new Random();
        while(size < MultiZipBlobStore.MAX_BINARY_FILE_SIZE*2.5) {
            Bean bean = new Bean(UUID.randomUUID().toString());
            byte[] bytes = new byte[r.nextInt(1024000)]; // max 1MB
            r.nextBytes(bytes);
            Blob blob = new Blob(bytes);
            bean.setBlob(blob);
            objectPocket.add(bean);
            size += bytes.length;
        }
        objectPocket.store();
        // num of created files
        File f = new File(FILESTORE);
        File[] list = f.listFiles();
        int counter = 0;
        for (File file : list) {
            if (file.getName().startsWith(MultiZipBlobStore.BLOB_STORE_DEFAULT_FILENAME)) {
                // ensure file size
                assertTrue(file.length() < MultiZipBlobStore.MAX_BINARY_FILE_SIZE+1024000);
                counter++;
            }
        }
        assertTrue(counter == 3);
    }

    @Test
    public void testModifyBlobData() throws Exception {
        
        ObjectPocket objectPocket = getObjectPocket();
        Bean bean = new Bean("bean1");
        Blob blob = new Blob();
        byte[] bytes = "abcd".getBytes();
        blob.setBytes(bytes);
        bean.setBlob(blob);
        objectPocket.add(bean);
        objectPocket.store();
        
        MultiZipBlobStore multiZipBlobStore = (MultiZipBlobStore)getBlobStore();
        
        File f = new File(FILESTORE);
        File[] list = f.listFiles();
        long oldSize = -1;
        long oldEntryNum = multiZipBlobStore.numEntries();
        for (File file : list) {
            if (file.getName().startsWith(MultiZipBlobStore.BLOB_STORE_DEFAULT_FILENAME)) {
                oldSize = file.length();
            }
        }
        
        bytes = "efgh".getBytes();
        blob.setBytes(bytes);
        objectPocket.store();
        assertTrue(oldEntryNum == multiZipBlobStore.numEntries());
        for (File file : list) {
            if (file.getName().startsWith(MultiZipBlobStore.BLOB_STORE_DEFAULT_FILENAME)) {
                assertTrue(oldSize == file.length());
            }
        }
        
    }

}
