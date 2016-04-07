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
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.objectpocket.exception.ObjectPocketException;
import org.objectpocket.gson.CustomTypeAdapterFactory;
import org.objectpocket.references.ReferenceSupport;
import org.objectpocket.storage.ObjectStore;
import org.objectpocket.storage.blob.BlobStore;
import org.objectpocket.util.IdSupport;
import org.objectpocket.util.JsonHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketImpl implements ObjectPocket {

    private ObjectStore objectStore;
    private BlobStore blobStore;
    private boolean serializeNulls = false;
    private boolean prettyPrinting = false;
    private boolean objectStoreInitialized = false;

    private Map<Type, Set<Object>> typeAdapterMap = new HashMap<Type, Set<Object>>(
	    10);
    private Set<ReferenceSupport> referenceSupportSet = new HashSet<ReferenceSupport>(
	    10);

    private Gson gson = null;
    private boolean loading = false;

    // <typeName:<id,object>>
    private Map<String, Map<String, Object>> objectMap = new HashMap<String, Map<String, Object>>(
	    1000000);

    // this extra map<object,id> is necessary for faster lookup of already
    // traced objects
    // objectMap.values.values is too slow for a proper lookup
    private Map<Object, String> tracedObjects = new HashMap<Object, String>(
	    1000000);

    private Set<Object> serializeAsRoot;

    // holds specific filenames for objects, set by the user
    private Map<Object, String> objectFilenames = new HashMap<Object, String>(
	    10);

    // <object, id>
    private Map<Object, String> idsFromReadObjects = new HashMap<Object, String>(
	    1000000);

    @SuppressWarnings("unused")
    private ObjectPocketImpl() {
    }

    protected ObjectPocketImpl(ObjectStore objectStore) {
	this.objectStore = objectStore;
    }

    @Override
    public void add(Object obj) throws ObjectPocketException {
	if (obj == null) {
	    return;
	}
	if (!storeIsReady()) {
	    throw new ObjectPocketException("The desired location contains data. Please load the data first.");
	}
	// TODO: check who owns the object (in case more than 1 ObjectPocket)
	// if (obj.getOwningInstance() == null ||
	// obj.getOwningInstance().equals(this)) {
	// // attach given object to this japer instance
	// obj.setOwningInstance(this);
	// // add object to objectMap
	String typeName = obj.getClass().getTypeName();
	if (objectMap.get(typeName) == null) {
	    objectMap.put(typeName, new HashMap<String, Object>());
	}
	Map<String, Object> map = objectMap.get(typeName);
	if (!tracedObjects.containsKey(obj)) {
	    String objectId = IdSupport.getId(obj, false);
	    tracedObjects.put(obj, objectId);
	    map.put(objectId, obj);
	}
	// add references
	addReferences(obj);
	// } else {
	// obj.getOwningInstance().add(obj);
	// }
    }

    @Override
    public void add(Object obj, String filename) throws ObjectPocketException {
	// TODO: validate filename to not be something like /home... or C:/...
	// throw exception in that case?
	this.add(obj);
	if (tracedObjects.containsKey(obj) && filename != null
		&& !filename.isEmpty()) {
	    objectFilenames.put(obj, filename);
	}
    }

    private void addReferences(Object obj) throws ObjectPocketException {
	for (ReferenceSupport referenceSupport : referenceSupportSet) {
	    Set<Object> references = referenceSupport.getReferences(obj);
	    if (references != null) {
		for (Object reference : references) {
		    // this supports cyclic references between objects
		    if (reference != null) {
			if (!tracedObjects.containsKey(reference)) {
			    // System.out.println(reference +
			    // " is not traced, but referenced by " + obj);
			    add(reference);
			}
		    }
		}
	    }
	}
    }

    @Override
    public void store() throws ObjectPocketException {

	if (!storeIsReady()) {
	    throw new ObjectPocketException("The desired location contains data. Please load the data first.");
	}

	long time = System.currentTimeMillis();
	serializeAsRoot = new HashSet<Object>();

	// rescan for references
	for (Object obj : tracedObjects.keySet()) {
	    addReferences(obj);
	}

	// update ids for all objects,
	// they might have been changed by the user in the meantime
	objectMap.clear();
	String newId = null;
	for (Object obj : tracedObjects.keySet()) {
	    newId = IdSupport.getId(obj, false, tracedObjects.get(obj));
	    tracedObjects.put(obj, newId);
	    String typeName = obj.getClass().getName();
	    Map<String, Object> map = objectMap.get(typeName);
	    if (map == null) {
		map = new HashMap<String, Object>();
		objectMap.put(typeName, map);
	    }
	    map.put(newId, obj);
	}

	// go through all types that have been add to ObjectPocket and collect
	// data to persist
	Map<String, Map<String, Set<String>>> jsonObjects = new HashMap<String, Map<String, Set<String>>>();
	Set<Blob> blobsToPersist = new HashSet<Blob>();
	Gson gson = configureGson();
	for (String typeName : objectMap.keySet()) {
	    // collect objects
	    Map<String, Object> map = objectMap.get(typeName);
	    if (map.values() == null) {
		return;
	    }
	    Map<String, Set<String>> jsonStrings = new HashMap<String, Set<String>>(
		    map.values().size());
	    String jsonString = null;
	    String filename = typeName;
	    for (String id : map.keySet()) {
		// TODO: Is this necessary any more?
		// if (!identifiable.isProxy()) {
		// identifiable.serializeAsRoot = true;
		Object object = map.get(id);
		if (object instanceof ProxyOut) {
		    System.out.println("proxyOut");
		}
		serializeAsRoot.add(object);
		StringBuilder sb = new StringBuilder(gson.toJson(object));
		jsonString = JsonHelper.addTypeAndIdToJson(sb, typeName,
			IdSupport.getId(object, true, id), prettyPrinting);
		if (objectFilenames.get(object) != null) {
		    filename = objectFilenames.get(object);
		}
		if (jsonStrings.get(filename) == null) {
		    jsonStrings.put(filename, new HashSet<String>());
		}
		jsonStrings.get(filename).add(jsonString);
		// }
	    }
	    jsonObjects.put(typeName, jsonStrings);

	    // collect blob data
	    try {
		Class<?> clazz = Class.forName(typeName);
		if (Blob.class.isAssignableFrom(clazz)) {
		    for (Object o : objectMap.get(typeName).values()) {
			Blob blob = (Blob) o;
			if (blob.doPersist()) {
			    blobsToPersist.add(blob);
			}
		    }
		}
	    } catch (ClassNotFoundException e) {
		throw new ObjectPocketException(
			"Could not collect blobs for typeName. " + typeName, e);
	    }

	}

	// persist object data
	try {
	    objectStore.writeJsonObjects(jsonObjects);
	} catch (IOException e) {
	    throw new ObjectPocketException("Could not persist objects.", e);
	}

	// persist blob data
	try {
	    blobStore.writeBlobs(blobsToPersist);
	} catch (IOException e) {
	    throw new ObjectPocketException("Could not persist blobs.", e);
	}
	
	objectStoreInitialized = true;

	Logger.getAnonymousLogger().info(
		"Stored all objects in " + objectStore.getSource() + " in "
			+ (System.currentTimeMillis() - time) + " ms.");
    }

    @Override
    public void load() throws ObjectPocketException {
	loading = true;
	long timeAll = System.currentTimeMillis();

	idsFromReadObjects.clear();
	tracedObjects.clear();
	objectMap.clear();

	/**
	 * get all available object types
	 */
	Set<String> availableObjectTypes = null;
	try {
	    availableObjectTypes = objectStore.getAvailableObjectTypes();
	} catch (IOException e) {
	    loading = false;
	    throw new ObjectPocketException(
		    "Could not acquire available objects.", e);
	}

	// ExecutorService threadPool = Executors.newCachedThreadPool();

	/**
	 * load json objects strings into real objects
	 */
	if (availableObjectTypes != null) {
	    for (String typeName : availableObjectTypes) {

		// Runnable r = new Runnable() {
		// @Override
		// public void run() {
		// try {
		// loadObjectsFromJsonStrings(typeName);
		// } catch (ClassNotFoundException | IOException e) {
		// //throw new
		// ObjectPocketException("Could not load objects for type. " +
		// typeName, e);
		// Logger.getAnonymousLogger().log(Level.SEVERE,
		// "Could not load objects for type. " + typeName, e);
		// }
		// }
		// };
		//
		// threadPool.execute(r);

		try {
		    loadObjectsFromJsonStrings(typeName);
		} catch (ClassNotFoundException | IOException e) {
		    loading = false;
		    throw new ObjectPocketException(
			    "Could not load objects for type. " + typeName, e);
		}

	    }
	}

	// threadPool.shutdown();
	// try {
	// threadPool.awaitTermination(60, TimeUnit.SECONDS);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }

	injectReferences();

	Logger.getAnonymousLogger().info(
		"Loaded all objects from " + objectStore.getSource() + " in "
			+ (System.currentTimeMillis() - timeAll) + " ms.");
	loading = false;
	objectStoreInitialized = true;
    }

    @Override
    public void loadAsynchronous(Class<?>... preload)
	    throws ObjectPocketException {
	loading = true;
	long timeAll = System.currentTimeMillis();
	/**
	 * get all available object types
	 */
	Set<String> availableObjectTypes = null;
	try {
	    availableObjectTypes = new HashSet<String>(
		    objectStore.getAvailableObjectTypes());
	} catch (IOException e) {
	    loading = false;
	    throw new ObjectPocketException(
		    "Could not acquire available objects.", e);
	}

	if (preload != null) {
	    for (Class<?> type : preload) {
		if (type != null) {
		    // remove type from all types
		    availableObjectTypes.remove(type.getName());
		    // preload objects for this type
		    try {
			loadObjectsFromJsonStrings(type.getName());
		    } catch (ClassNotFoundException | IOException e) {
			loading = false;
			throw new ObjectPocketException(
				"Could not load objects for type. "
					+ type.getName(), e);
		    }
		}
	    }
	}

	injectReferences();
	objectStoreInitialized = true;
	
	final Set<String> otherTypes = availableObjectTypes;
	// do everything else asynchronous
	SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
	    @Override
	    protected Void doInBackground() throws Exception {
		// load the rest of object types
		for (String typeName : otherTypes) {
		    try {
			loadObjectsFromJsonStrings(typeName);
		    } catch (ClassNotFoundException | IOException e) {
			loading = false;
			throw new ObjectPocketException(
				"Could not load objects asynchronously for type. "
					+ typeName, e);
		    }
		}
		injectReferences();
		Logger.getAnonymousLogger().info(
			"Loaded all objects from " + objectStore.getSource()
			+ " in "
			+ (System.currentTimeMillis() - timeAll)
			+ " ms.");
		loading = false;
		return null;
	    }

	    @Override
	    protected void done() {
		try {
		    get();
		} catch (InterruptedException | ExecutionException e) {
		    loading = false;
		    Logger.getAnonymousLogger().severe(
			    "Could not load objects asynchronously.");
		    e.printStackTrace();
		}
	    }
	};
	sw.execute();
    }

    @Override
    public boolean isLoading() {
	return loading;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T find(String id, Class<T> type) throws ObjectPocketException {
	if (id == null || id.isEmpty()) {
	    return null;
	}
	if (type == null) {
	    return null;
	}
	if (!storeIsReady()) {
	    throw new ObjectPocketException("The desired location contains data. Please load the data first.");
	}
	Map<String, Object> map = objectMap.get(type.getName());
	if (map != null) {
	    return (T) map.get(id);
	}
	return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> findAll(Class<T> type) throws ObjectPocketException {
	if (type != null) {
	    if (!storeIsReady()) {
		throw new ObjectPocketException("The desired location contains data. Please load the data first.");
	    }
	    Map<String, Object> map = objectMap.get(type.getName());
	    if (map != null && !map.isEmpty()) {
		return (Collection<T>) map.values();
	    }
	}
	return null;
    }

    @Override
    public void remove(Object obj) throws ObjectPocketException {
	System.out.println("tracedobjects: " + tracedObjects.size());
	System.out.println("remove: " + obj);
	if (obj == null) {
	    return;
	}
	String id = tracedObjects.get(obj);
	if (id == null) {
	    return;
	}
	if (!storeIsReady()) {
	    throw new ObjectPocketException("The desired location contains data. Please load the data first.");
	}
	Map<String, Object> map = objectMap.get(obj.getClass().getName());
	if (map == null) {
	    return;
	}
	map.remove(id);
	tracedObjects.remove(obj);
	System.out.println("tracedobjects: " + tracedObjects.size());
	// remove referenced Blob objects
	if (!(obj instanceof Blob)) {
	    for (ReferenceSupport referenceSupport : referenceSupportSet) {
		Set<Object> references = referenceSupport.getReferences(obj);
		if (references != null) {
		    for (Object reference : references) {
			if (reference != null && reference instanceof Blob) {
			    remove(reference);
			}
		    }
		}
	    }
	}
    }

    @Override
    public void cleanup() throws ObjectPocketException {
	if (!storeIsReady()) {
	    throw new ObjectPocketException("The desired location contains data. Please load the data first.");
	}
	throw new UnsupportedOperationException();

    }

    @Override
    public void close() throws IOException {
	if (objectStore != null) {
	    objectStore.close();
	}
	if (blobStore != null) {
	    blobStore.close();
	}
    }
    
    @Override
    public boolean exists() {
        return objectStore.exists();
    }

    @Override
    public void link(ObjectPocket objectPocket) {
	throw new UnsupportedOperationException();

    }

    @Override
    public void setDefaultFilename(Class<?> type, String filename) throws ObjectPocketException {
	if (!storeIsReady()) {
	    throw new ObjectPocketException("The desired location contains data. Please load the data first.");
	}
	throw new UnsupportedOperationException();
    }

    private void loadObjectsFromJsonStrings(String typeName)
	    throws ClassNotFoundException, IOException {
	Class<?> clazz = Class.forName(typeName);
	boolean setBlobStore = false;
	if (Blob.class.isAssignableFrom(clazz)) {
	    setBlobStore = true;
	}
	long time = System.currentTimeMillis();
	int counter = 0;
	Map<String, String> jsonObjects = objectStore.readJsonObjects(typeName);
	if (jsonObjects != null && !jsonObjects.isEmpty()) {
	    HashMap<String, Object> map = new HashMap<String, Object>();
	    Gson gson = configureGson();
	    for (String jsonObject : jsonObjects.keySet()) {
		Object object = gson.fromJson(jsonObject, clazz);

		// TODO: map to owning ObjectPocket
		// object.setOwningInstance(this);

		if (setBlobStore) {
		    ((Blob) object).setBlobStore(blobStore);
		}

		String id = IdSupport
			.getId(object, jsonObjects.get(jsonObject));

		tracedObjects.put(object, id);
		map.put(id, object);
		counter++;
	    }
	    objectMap.put(typeName, map);
	}
	Logger.getAnonymousLogger().info(
		"Loaded " + counter + " objects of type\n  " + clazz.getName()
		+ " in " + (System.currentTimeMillis() - time) + " ms");
    }

    private void injectReferences() {
	long time = System.currentTimeMillis();
	for (ReferenceSupport referenceSupport : referenceSupportSet) {
	    Map<String, Map<String, Object>> globalMap = new HashMap<String, Map<String, Object>>(
		    objectMap);
	    // TODO: extends to more instances
	    // for (JaperImpl japer : otherJapers) {
	    // amendMap(globalMap, japer.objectMap);
	    // }
	    for (String typeName : globalMap.keySet()) {
		Collection<Object> values = globalMap.get(typeName).values();
		for (Object object : values) {
		    referenceSupport.injectReferences(object, globalMap,
			    idsFromReadObjects);
		}
	    }
	}
	Logger.getAnonymousLogger()
	.info("Injection took " + (System.currentTimeMillis() - time)
		+ " ms");
    }

    // private void amendMap(Map<String, Map<String, Object>> dest,
    // Map<String, Map<String, Object>> source) {
    // for (String key : source.keySet()) {
    // if (dest.get(key) != null) {
    // Map<String, Object> map = dest.get(key);
    // map.putAll(source.get(key));
    // } else {
    // dest.put(key, source.get(key));
    // }
    // }
    // }

    public void addIdFromReadObject(Object object, String id) {
	idsFromReadObjects.put(object, id);
    }

    private Gson configureGson() {
	if (gson == null) {
	    GsonBuilder gsonBuilder = new GsonBuilder();
	    // null serialization
	    if (serializeNulls) {
		gsonBuilder.serializeNulls();
	    }

	    // This is where the referencing entry magic happens
	    gsonBuilder
	    .registerTypeAdapterFactory(new CustomTypeAdapterFactory(
		    this));

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
	    gson = gsonBuilder.create();
	}
	return gson;
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

    public void addReferenceSupport(ReferenceSupport referenceSupport) {
	referenceSupportSet.add(referenceSupport);
    }

    public String getIdForObject(Object obj) {
	return tracedObjects.get(obj);
    }

    public boolean isSerializeAsRoot(Object obj) {
	return serializeAsRoot.contains(obj);
    }

    public void setSerializeAsRoot(Object obj, boolean val) {
	if (val) {
	    serializeAsRoot.add(obj);
	} else {
	    serializeAsRoot.remove(obj);
	}
    }

    private boolean storeIsReady() {
	return objectStoreInitialized || !objectStore.exists();
    }

}
