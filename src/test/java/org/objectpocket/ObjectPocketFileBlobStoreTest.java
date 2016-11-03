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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.objectpocket.storage.blob.FileBlobStore;
import org.objectpocket.storage.blob.MultiZipBlobStore;
import org.objectpocket.storage.blob.ZipBlobStore;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketFileBlobStoreTest extends FileStoreTest {

    @Test
    public void testAdd() throws Exception {
        ObjectPocket objectPocket = getObjectPocket();
        Bean b = new Bean("bean");
        Blob blob = new Blob();
        byte[] bytes = new byte[] { 1, 2 };
        blob.setBytes(bytes);
        b.setBlob(blob);
        objectPocket.add(b);
        assertTrue(objectPocket.findAll(Blob.class).size() == 1);
        objectPocket.store();
        assertTrue(objectPocket.findAll(Blob.class).size() == 1);
        objectPocket.load();
        assertTrue(objectPocket.findAll(Blob.class).size() == 1);
        Blob foundBlob = objectPocket.findAll(Blob.class).iterator().next();
        assertTrue(objectPocket.findAll(Blob.class).iterator().next().getBytes()[0] == bytes[0]);
        assertTrue(objectPocket.findAll(Bean.class).iterator().next().blob.getBytes()[0] == bytes[0]);
    }

    @Test
    public void testRemoveDirectly() throws Exception {
        ObjectPocket objectPocket = getObjectPocket();
        Bean b = new Bean("bean");
        Blob blob = new Blob();
        byte[] bytes = new byte[] { 1, 2 };
        blob.setBytes(bytes);
        b.setBlob(blob);
        objectPocket.add(b);
        assertTrue(objectPocket.findAll(Blob.class).size() == 1);
        objectPocket.remove(b);
        assertNull(objectPocket.findAll(Blob.class));
        objectPocket.store();
        assertNull(objectPocket.findAll(Blob.class));
        objectPocket.load();
        assertNull(objectPocket.findAll(Blob.class));
    }

    @Test
    public void testRemoveAfterStore() throws Exception {
        ObjectPocket objectPocket = getObjectPocket();
        Bean b = new Bean("bean");
        Blob blob = new Blob();
        byte[] bytes = new byte[] { 1, 2 };
        blob.setBytes(bytes);
        b.setBlob(blob);
        objectPocket.add(b);
        assertTrue(objectPocket.findAll(Blob.class).size() == 1);
        objectPocket.store();
        assertTrue(objectPocket.findAll(Blob.class).size() == 1);
        objectPocket.remove(b);
        assertNull(objectPocket.findAll(Blob.class));
        objectPocket.store();
        assertNull(objectPocket.findAll(Blob.class));
        objectPocket.load();
        assertNull(objectPocket.findAll(Blob.class));
    }

    @Test
    public void testRemoveMultiReferenced() throws Exception {
        ObjectPocket objectPocket = getObjectPocket();
        Bean b1 = new Bean("bean1");
        Bean b2 = new Bean("bean2");
        Blob blob = new Blob();
        byte[] bytes = new byte[] { 1, 2 };
        blob.setBytes(bytes);
        b1.setBlob(blob);
        b2.setBlob(blob);
        objectPocket.add(b1);
        objectPocket.add(b2);
        assertTrue(objectPocket.findAll(Blob.class).size() == 1);
        objectPocket.remove(b1);
        assertNull(objectPocket.findAll(Blob.class));
        objectPocket.store();
        assertTrue(objectPocket.findAll(Blob.class).size() == 1);
        objectPocket.load();
        assertTrue(objectPocket.findAll(Blob.class).size() == 1);
        assertTrue(objectPocket.findAll(Blob.class).iterator().next().getBytes()[0] == bytes[0]);
        assertTrue(objectPocket.findAll(Bean.class).size() == 1);
        assertTrue(objectPocket.findAll(Bean.class).iterator().next().name.equals(b2.name));
        assertTrue(objectPocket.findAll(Bean.class).iterator().next().blob.getBytes()[0] == bytes[0]);
    }

    @Test
    public void testCleanupAll() throws Exception {
        ObjectPocket objectPocket = getObjectPocket();
        Bean b = new Bean("bean");
        Blob blob = new Blob();
        byte[] bytes = new byte[] { 1, 2 };
        blob.setBytes(bytes);
        b.setBlob(blob);
        objectPocket.add(b);
        objectPocket.store();
        File f = new File(FILESTORE + "/" + FileBlobStore.BLOB_STORE_DIRNAME);
        if (getBlobStore() instanceof MultiZipBlobStore) {
            f = new File(FILESTORE + "/" + MultiZipBlobStore.BLOB_STORE_DEFAULT_FILENAME + ".0"); 
        }
        assertTrue(f.exists());
        objectPocket.remove(b);
        objectPocket.store();
        assertTrue(f.exists());
        objectPocket.cleanup();
        assertFalse(f.exists());
        assertTrue(true);
    }

    @Test
    public void testCleanupPartially() throws Exception {
        ObjectPocket objectPocket = getObjectPocket();

        Bean b1 = new Bean("bean1");
        Blob blob1 = new Blob();
        byte[] bytes = new byte[] { 1, 2 };
        blob1.setBytes(bytes);
        b1.setBlob(blob1);
        objectPocket.add(b1);

        Bean b2 = new Bean("bean2");
        Blob blob2 = new Blob();
        blob2.setBytes(bytes);
        b2.setBlob(blob2);
        objectPocket.add(b2);

        Bean b3 = new Bean("bean3");
        Blob blob3 = new Blob();
        blob3.setBytes(bytes);
        blob3.setPath("/subdir/path.jpg");
        b3.setBlob(blob3);
        objectPocket.add(b3);

        objectPocket.store();
        File f = new File(FILESTORE + "/" + FileBlobStore.BLOB_STORE_DIRNAME);
        if (getBlobStore() instanceof MultiZipBlobStore) {
            f = new File(FILESTORE + "/" + MultiZipBlobStore.BLOB_STORE_DEFAULT_FILENAME + ".0"); 
        }
        assertTrue(f.exists());
        objectPocket.remove(b1);
        objectPocket.remove(b3);
        objectPocket.store();
        assertTrue(f.exists());
        objectPocket.cleanup();
        assertTrue(f.exists());
        if (getBlobStore() instanceof FileBlobStore) {
            assertTrue(((FileBlobStore) getBlobStore()).numEntries() == 1);
        } else if (getBlobStore() instanceof MultiZipBlobStore) {
            assertTrue(((MultiZipBlobStore) getBlobStore()).numEntries() == 1);
        }
    }

    @Test
    public void copyObjectFromOneObjectPocketToAnother() {

    }

    public class Bean {
        private String name;
        private Blob blob;

        public Bean(String name) {
            this.name = name;
        }

        public void setBlob(Blob blob) {
            this.blob = blob;
        }

        public Blob getBlob() {
            return blob;
        }
    }

}
