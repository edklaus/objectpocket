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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectpocket.storage.BlobStore;
import org.objectpocket.storage.ObjectStore;

import com.google.gson.GsonBuilder;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketBuilder {
	
	private Map<Type, Set<Object>> typeAdapterMap = new HashMap<Type, Set<Object>>();
	private BlobStore blobStore;
	private boolean prettyPrinting = true;
	private boolean serializeNulls = false;
	
	public ObjectPocket createMemoryObjectPocket() {
		return null;
	}
	
	public ObjectPocket createFileObjectPocket(String directory) {
		return null;
	}
	
	public ObjectPocket createObjectPocket(ObjectStore objectStore) {
		return null;
	}
	
	/**
	 * Configure {@link ObjectPocket} to serialize Nulls.</br>
	 * By default {@link ObjectPocket} will not serialize fields with Nulls to save space.
	 */
	public ObjectPocketBuilder serializeNulls() {
		this.serializeNulls = true;
		return this;
	}
	
	/**
	 * Configure {@link ObjectPocket} to not pretty print JSON output.
	 * By default {@link ObjectPocket} will prettyPrint the JSON output.
	 */
	public ObjectPocketBuilder noPrittyPrinting() {
		this.prettyPrinting = false;
		return this;
	}
	
	/**
	 * Register a specific type adapter for the serialization and deserialization of objects.</br>
	 * This method directly derives from {@link GsonBuilder#registerTypeAdapter(Type, Object)}.
	 * @param type
	 * @param typeAdapter
	 */
	public void registerTypeAdapter(Type type, Object typeAdapter) {
		if (typeAdapterMap.get(type) == null) {
			typeAdapterMap.put(type, new HashSet<Object>());
		}
		typeAdapterMap.get(type).add(typeAdapter);
	}

}
