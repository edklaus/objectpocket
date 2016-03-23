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
import java.util.logging.Logger;

import org.objectpocket.references.ArrayReferenceSupport;
import org.objectpocket.references.CollectionReferenceSupport;
import org.objectpocket.references.SimpleReferenceSupport;
import org.objectpocket.storage.BlobStore;
import org.objectpocket.storage.CryptoFileStore;
import org.objectpocket.storage.FileStore;
import org.objectpocket.storage.MemoryStore;
import org.objectpocket.storage.ObjectStore;
import org.objectpocket.storage.ZipFileStore;

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
	
	/**
	 * Create an {@link ObjectPocket} instance that will store data into memory.
	 * @return {@link ObjectPocket}, or null if {@link ObjectPocket} could not be instantiated
	 */
	public ObjectPocket createMemoryObjectPocket() {
		MemoryStore memStore = new MemoryStore();
		return createObjectPocket(memStore);
	}
	
	/**
	 * Create an {@link ObjectPocket} instance that will store data into the given directory.
	 * @param directory
	 * @return {@link ObjectPocket}, or null if {@link ObjectPocket} could not be instantiated
	 */
	public ObjectPocket createFileObjectPocket(String directory) {
		FileStore fileStore = new FileStore(directory);
		return createObjectPocket(fileStore);
	}
	
	/**
	 * Create an {@link ObjectPocket} instance that will store data into the given directory.</br>
	 * Data will be stored encrypted. For encryption and decryption a password is needed.
	 * @param directory
	 * @param password
	 * @return {@link ObjectPocket}, or null if {@link ObjectPocket} could not be instantiated
	 */
	public ObjectPocket createCryptoFileObjectPocket(String directory, String password) {
		CryptoFileStore fileStore = new CryptoFileStore(directory, password);
		return createObjectPocket(fileStore);
	}
	
	public ObjectPocket createZipFileObjectPocket(String filename, int compressionLevel, String password) {
		ZipFileStore zipFileStore = new ZipFileStore(filename, compressionLevel, password);
		return createObjectPocket(zipFileStore);
	}
	
	/**
	 * Create an {@link ObjectPocket} instance with a given object store.
	 * @param objectStore 
	 * @return {@link ObjectPocket}, or null if {@link ObjectPocket} could not be instantiated
	 */
	public ObjectPocket createObjectPocket(ObjectStore objectStore) {
		if (objectStore == null) {
			Logger.getAnonymousLogger().severe("Argument objectStore is null.");
			return null;
		}
		ObjectPocketImpl objectPocketImpl = new ObjectPocketImpl(objectStore);
		addReferenceSupport(objectPocketImpl);
		if (blobStore != null) {
			objectPocketImpl.setBlobStore(blobStore);
		} else {
			objectPocketImpl.setBlobStore(objectStore);
		}
		if (serializeNulls) {
			objectPocketImpl.serializeNulls();
		}
		if (prettyPrinting) {
			objectPocketImpl.setPrettyPrinting();
		}
		objectPocketImpl.setTypeAdapterMap(typeAdapterMap);
		return objectPocketImpl;
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
	
	private void addReferenceSupport(ObjectPocketImpl objectPocketImpl) {
		objectPocketImpl.addReferenceSupport(new SimpleReferenceSupport());
		objectPocketImpl.addReferenceSupport(new ArrayReferenceSupport());
		objectPocketImpl.addReferenceSupport(new CollectionReferenceSupport());
	}

}
