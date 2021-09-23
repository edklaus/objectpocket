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

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.objectpocket.storage.FileStore;
import org.objectpocket.storage.blob.BlobStore;
import org.objectpocket.storage.blob.FileBlobStore;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class FileStoreTest {

    public static final String FILESTORE = System.getProperty("java.io.tmpdir") + "/objectpocket_test";
    protected static ObjectPocket objectPocket;
    protected static BlobStore blobStore;

    @Before
    public void prepare() throws Exception {
        if (objectPocket != null) {
            objectPocket.close();
        }
        File f = new File(FILESTORE);
        if (f.exists()) {
            FileUtils.deleteDirectory(f);
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        CountDownLatch waiter = new CountDownLatch(1);
        waiter.await(2, TimeUnit.SECONDS);
        if (objectPocket != null) {
            objectPocket.close();
        }
        File f = new File(FILESTORE);
        if (f.exists()) {
            FileUtils.deleteDirectory(f);
        }
    }

    /**
     * Always creates a new ObjectPocket instance!
     * 
     * @return
     * @throws Exception
     */
    public ObjectPocket getObjectPocket() throws Exception {
        if (objectPocket != null) {
            objectPocket.close();
        }
        ObjectPocketBuilder objectPocketBuilder = new ObjectPocketBuilder();
        objectPocketBuilder.doNotWriteBackups(); // keep test footrpint/time low
        FileStore objectStore = new FileStore(FILESTORE);
        blobStore = new FileBlobStore(FILESTORE);
        objectStore.setBlobStore(blobStore);
        objectPocket = objectPocketBuilder.createObjectPocket(objectStore);
        return objectPocket;
    }

    public static BlobStore getBlobStore() {
        return blobStore;
    }

}
