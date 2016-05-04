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
public class ObjectPocketLoadAsynchronousTest extends FileStoreTest {

    @Test
    public void testLoadAsynchronous() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	int duePre = 10000;
	int dueAsync = 10000;
	for (int i = 0; i < duePre; i++) {
	    objectPocket.add(new BeanPreload("pre " + i));
	}
	for (int i = 0; i < dueAsync; i++) {
	    objectPocket.add(new BeanAsync("async " + i));
	}
	objectPocket.store();
	
	// without preloading
	objectPocket = getObjectPocket();
	objectPocket.loadAsynchronous();
	assertNull(objectPocket.findAll(BeanPreload.class));
	assertNull(objectPocket.findAll(BeanAsync.class));
	while(objectPocket.isLoading()){
	    Thread.sleep(1);
	}
	assertTrue(duePre == objectPocket.findAll(BeanPreload.class).size());
	assertTrue(dueAsync == objectPocket.findAll(BeanAsync.class).size());
	
	// with preloading
	objectPocket = getObjectPocket();
	objectPocket.loadAsynchronous(BeanPreload.class);
	assertTrue(duePre == objectPocket.findAll(BeanPreload.class).size());
	assertNull(objectPocket.findAll(BeanAsync.class));
	while(objectPocket.isLoading()){
	    Thread.sleep(1);
	}
	assertTrue(dueAsync == objectPocket.findAll(BeanAsync.class).size());
    }
    
    private class BeanPreload {
	@SuppressWarnings("unused")
	String name;
	public BeanPreload(String name) {
	    this.name = name;
	}
    }
    
    private class BeanAsync extends BeanPreload {
	public BeanAsync(String name) {
	    super(name);
	}
    }

}
