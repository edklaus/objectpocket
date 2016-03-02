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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.objectpocket.exception.ObjectPocketException;
import org.objectpocket.storage.BlobStore;
import org.objectpocket.storage.ObjectStore;
import org.objectpocket.util.JsonHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketImpl implements ObjectPocket{
	
	private ObjectStore objectStore;
	private BlobStore blobStore;
	private boolean serializeNulls = false;
	private boolean prettyPrinting = false;
	private Map<Type, Set<Object>> typeAdapterMap = new HashMap<Type, Set<Object>>();
	
	private boolean loading = false;
	
	// <typeName:<id,object>>
	private Map<String, Map<String, Object>> objectMap = new HashMap<String, Map<String, Object>>();
	// this extra map is necessary for faster lookup of already traced objects
	// objectMap.values.values is too slow for a proper lookup
	private Set<Object> tracedObjects = new HashSet<Object>();

	public ObjectPocketImpl(ObjectStore objectStore) {
		this.objectStore = objectStore;
	}
	
	@Override
	public void add(Object obj) {
		// TODO: check who owns the object (in case more than 1 ObjectPocket)
//		if (obj.getOwningInstance() == null || obj.getOwningInstance().equals(this)) {
//			// attach given object to this japer instance
//			obj.setOwningInstance(this);
//			// add object to objectMap
			String typeName = obj.getClass().getTypeName();
			if (objectMap.get(typeName) == null) {
				objectMap.put(typeName, new HashMap<String, Object>());
			}
			Map<String, Object> map = objectMap.get(typeName);
			if (!tracedObjects.contains(obj)) {
				tracedObjects.add(obj);
				// TODO: check if @Id has been set before generating ID
				// custom ID might change at runtime!! Is this a problem?
				map.put(UUID.randomUUID().toString(), obj);
			}
			// add references
			//addReferences(obj);
//		} else {
//			obj.getOwningInstance().add(obj);
//		}
	}

	@Override
	public void store() throws ObjectPocketException {
		long time = System.currentTimeMillis();
		// rescan for references
//		for (String typeName : objectMap.keySet()) {
//			Map<String, Identifiable> map = objectMap.get(typeName);
//			if (map.values() != null) {
//				for (Identifiable obj : map.values()) {
//					addReferences(obj);
//				}
//			}
//		}

		// go through all types that have been add to Japer
		Gson gson = configureGson();
		for (String typeName : objectMap.keySet()) {
			// store objects
			Map<String, Object> map = objectMap.get(typeName);
			if (map.values() == null) {
				return;
			}
			Set<String> jsonStrings = new HashSet<String>(map.values().size());
			String jsonString = null;
			for (String id : map.keySet()) {
				// TODO: Is this necessary any more?
//				if (!identifiable.isProxy()) {
//					identifiable.serializeAsRoot = true;
					jsonString = gson.toJson(map.get(id));
					jsonString = JsonHelper.addClassAndIdToJson(jsonString, typeName, id, prettyPrinting);
					jsonStrings.add(jsonString);
//				}
			}
			try {
				objectStore.writeJsonObjects(jsonStrings, typeName);
			} catch (IOException e) {
				throw new ObjectPocketException("Could not persist objects for typeName. " + typeName, e);
			}

			// persist blob data
//			try {
//				Class<?>  clazz = Class.forName(typeName);
//				if (Blob.class.isAssignableFrom(clazz)) {
//					Set<Blob> blobsToPersist = new HashSet<Blob>();
//					for (Identifiable identifiable : objectMap.get(typeName).values()) {
//						Blob blob = (Blob)identifiable;
//						if (blob.isPersist()) {
//							blobsToPersist.add(blob);
//						}
//					}
//					if (!blobsToPersist.isEmpty()) {
//						blobStore.writeBlobs(blobsToPersist);
//					}
//				}
//			} catch (ClassNotFoundException|IOException e) {
//				throw new JaperException("Could not collect blobs for typeName. " + typeName, e);
//			}

		}
		Logger.getAnonymousLogger().info("Stored all objects in " + objectStore.getSource() + 
				" in "+ (System.currentTimeMillis()-time) + " ms.");
	}

	@Override
	public void load() throws ObjectPocketException {
		loading = true;
		long timeAll = System.currentTimeMillis();

		/**
		 * get all available object types
		 */
		Set<String> availableObjectTypes = null;
		try {
			availableObjectTypes = objectStore.getAvailableObjectTypes();
		} catch (IOException e) {
			loading = false;
			throw new ObjectPocketException("Could not acquire available objects.", e);
		}

		/**
		 * load json objects strings into real objects
		 */
		if (availableObjectTypes != null) {
			for (String typeName : availableObjectTypes) {
				try {
					loadObjectsFromJsonStrings(typeName);
				} catch (ClassNotFoundException | IOException e) {
					loading = false;
					throw new ObjectPocketException("Could not load objects for type. " + typeName, e);
				}
			}
		}

		//injectReferences();

		Logger.getAnonymousLogger().info("Loaded all objects fomr " + objectStore.getSource() + 
				" in " + (System.currentTimeMillis()-timeAll) + " ms.");
		loading = false;
	}

	@Override
	public void loadAsynchronous(Class<?>... preload) throws ObjectPocketException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public boolean isLoading() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T find(String id, Class<T> type) {
		Map<String, Object> map = objectMap.get(type.getName());
		if (map != null) {
			return (T)map.get(id);
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Collection<T> findAll(Class<T> type) {
		Map<String, Object> map = objectMap.get(type.getName());
		if (map != null) {
			return (Collection<T>)map.values();
		}
		return null;
	}

	@Override
	public void remove(Object obj) throws ObjectPocketException {
		tracedObjects.remove(obj);
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void cleanup() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void link(ObjectPocket objectPocket) {
		throw new UnsupportedOperationException();
		
	}
	
	private void loadObjectsFromJsonStrings(String typeName) throws ClassNotFoundException, IOException {
		Class<?> clazz = Class.forName(typeName);
		boolean insertBlobStore = false;
		if (Blob.class.isAssignableFrom(clazz)) {
			insertBlobStore = true;
		}
		long time = System.currentTimeMillis();
		int counter = 0;
		Map<String, String> jsonObjects = objectStore.readJsonObjects(typeName);
		if (jsonObjects != null && !jsonObjects.isEmpty()) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			objectMap.put(clazz.getName(), map);
			Gson gson = configureGson();
			for (String id : jsonObjects.keySet()) {
				Object object = gson.fromJson(jsonObjects.get(id), clazz);

				// TODO: map to owning ObjectPocket
				//object.setOwningInstance(this);

				//					if (insertBlobStore) {
				//						((Blob)object).setBlobStore(blobStore);
				//					}

				objectMap.get(clazz.getName()).put(id, object);
				counter++;
			}
		}
		Logger.getAnonymousLogger().info("Loaded " + counter + " objects of type\n  "
				+ clazz.getName() + " in " + (System.currentTimeMillis()-time) + " ms");
	}
	
	private Gson configureGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		// null serialization
		if (serializeNulls) {
			gsonBuilder.serializeNulls();
		}
		
		// TODO: Find out if this is needed any more 
		//gsonBuilder.registerTypeAdapterFactory(new CustomTypeAdapterFactory());
		
		// add custom type adapters
		for (Type type : typeAdapterMap.keySet()) {
			for (Object typeAdapter : typeAdapterMap.get(type)) {
				gsonBuilder.registerTypeAdapter(type, typeAdapter);
			}
		}
		// pretty printing
		if (prettyPrinting) {
			gsonBuilder.setPrettyPrinting();
		}
		return gsonBuilder.create();
	}

	public void serializeNulls() {
		serializeNulls = true;
	}

	public void setPrettyPrinting() {
		prettyPrinting = true;
	}

	public void setTypeAdapterMap(Map<Type, Set<Object>> typeAdapterMap) {
		this.typeAdapterMap = typeAdapterMap;
	}

	public void setBlobStore(BlobStore blobStore) {
		this.blobStore = blobStore;
	}
	
}
