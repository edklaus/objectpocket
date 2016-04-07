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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketBlobTest extends FileStoreTest {

    @Test
    public void testAdd() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	Bean b = new Bean("bean");
	Blob blob = new Blob();
	byte[] bytes = new byte[]{1,2};
	blob.setBytes(bytes);
	b.setBlob(blob);
	objectPocket.add(b);
	assertTrue(objectPocket.findAll(Blob.class).size() == 1);
	objectPocket.store();
	assertTrue(objectPocket.findAll(Blob.class).size() == 1);
	objectPocket.load();
	assertTrue(objectPocket.findAll(Blob.class).size() == 1);
	assertTrue(objectPocket.findAll(Blob.class).iterator().next().getBytes()[0] == bytes[0]);
	assertTrue(objectPocket.findAll(Bean.class).iterator().next().blob.getBytes()[0] == bytes[0]);
    }
    
    @Test
    public void testRemove() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	Bean b = new Bean("bean");
	Blob blob = new Blob();
	byte[] bytes = new byte[]{1,2};
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
    public void testRemoveMultiReferenced() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	Bean b1 = new Bean("bean1");
	Bean b2 = new Bean("bean2");
	Blob blob = new Blob();
	byte[] bytes = new byte[]{1,2};
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
    
    private class Bean {
	private String name;
	private Blob blob;
	public Bean(String name) {
	    this.name = name;
	}
	public void setBlob(Blob blob) {
	    this.blob = blob;
	}
    }

}
