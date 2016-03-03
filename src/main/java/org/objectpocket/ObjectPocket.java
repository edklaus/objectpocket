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

import java.util.Collection;

import org.objectpocket.exception.ObjectPocketException;
import org.objectpocket.storage.BlobStore;

/**
 * ObjectPocket is a Java library for simple storing and loading of Java objects.
 * Objects are converted to JSON strings and can be directly written to files and read from files.</br>
 * It supports object references as well as Inheritance. Complex references between objects, like cross
 * referencing or cyclic referencing can be handled with annotations.</br>
 * ObjectPocket also supports storing different object types in separate files like one would do with 
 * database tables.
 * 
 * <p>Store object:</p>
 * 
 * <pre>
 * MyClass obj = new MyClass();
 * ObjectPocket objectPocket = new ObjectPocketBuilder().createFileObjectPocket("directory");
 * objectPocket.add(obj);
 * objectPocket.store();
 * </pre>
 * 
 * <p>Load object:</p>
 * 
 * <pre>
 * ObjectPocket objectPocket = new ObjectPocketBuilder().createFileObjectPocket("directory");
 * objectPocket.load();
 * MyClass obj = objectPocket.findAll(MyClass.class).iterator().next();
 * </pre>
 * 
 * You can find more examples/tutorials/documentation at:
 * <li>{@link org.objectpocket.example} package
 * <li><a href="http://objectpocket.org">http://objectpocket.org</a>
 * 
 * @author Edmund Klaus
 *
 */
public interface ObjectPocket {
	
	/**
	 * Add an object to ObjectPocket persistence context.</br>
	 * All objects that this object references, will also be add to the persistence context.</br></br> 
	 * All object inside the persistence context will be persisted to a given object store by calling {@link #store()}.
	 * 
	 * @param obj
	 */
	public void add(Object obj);
	
	/**
	 * Persist all objects inside the ObjectPocket persistence context to an object store.
	 * @throws ObjectPocketException If store operation fails
	 */
	public void store() throws ObjectPocketException;
	
	/**
	 * Loads all objects from an object store. This will override the currently available
	 * objects in the ObjectPocket.</br></br> 
	 * Therefore this method should be used to:
	 * <li>load objects initially
	 * <li>reload previous object state</br></br>
	 * After a reload, the old object references will not represent the
	 * reloaded object state. One has to replace the old objects by the
	 * new ones by getting them from ObjectPocket. 
	 * @throws ObjectPocketException
	 */
	public void load() throws ObjectPocketException;
	
	/**
	 * Loads all objects from an object store. This method behaves the same way {@link #load()} does,
	 * except that it loads the objects asynchronous.</br>
	 * To find out when loading is done, you can use {@link #isLoading()}.</br></br>
	 * You also have the option to pass 0...n Classes that should be loaded in advance.
	 * <pre>
	 * loadAsynchronous(Address.class, Person.class);
	 * </pre>
	 * The method will load all objects of these class types <b>synchronous</b> and then go on 
	 * with all other class types from the object store asynchronous. This is convenient if you 
	 * want to access parts of the information as soon as possible, and load everything else 
	 * asynchronous in background.</br></br>
	 * This feature comes in handy to improve user experience.</br></br>
	 * Be aware that references of "preload" objects may not represent the
	 * real data from the object store, until all object data has been loaded.
	 * @param preload
	 * @throws ObjectPocketException
	 */
	public void loadAsynchronous(Class<?>... preload) throws ObjectPocketException;
	
	/**
	 * Gives information about the loading status of ObjectPocket.</br>
	 * This is necessary when {@link #loadAsynchronous(Class...)} has been called and
	 * one wants to know when loading is finished.
	 * @return true if ObjectPocket is still loading, false otherwise
	 */
	public boolean isLoading();
	
	/**
	 * Find an object by the given id and object type.
	 * 
	 * @param id objectId
	 * @param type type of the object to find
	 * @return the object of given type and with the given id, if this exists in the persistence context, null otherwise
	 */
	public <T> T find(String id, Class<T> type);
	
	/**
	 * Find all object by the given object type.
	 * 
	 * @param type type of the object to find
	 * @return all objects of given type that exist in the persistence context, null otherwise
	 */
	public <T> Collection<T> findAll(Class<T> type);
	
	/**
	 * Remove object from persistence context.</br>
	 * You need to call {@link #store()} after {@link #remove(Object)} to remove the object from the object store.</br>
	 * 
	 * @param obj
	 * @throws ObjectPocketException if given object is not managed by this ObjectPocket instance.
	 */
	public void remove(Object obj) throws ObjectPocketException;
	
	/**
	 * Call this method to cleanup blob data from {@link BlobStore}.</br>
	 * To remove blob data just set the blob in an Identifiable object null. When calling {@link #store()} the reference
	 * to the blob data will just be removed. The data itself will stay in the {@link BlobStore}. 
	 * To really delete the data and free disk space call {@link #cleanup()}.
	 */
	public void cleanup();
	
	/**
	 * Links another ObjectPocket instance to this ObjectPocket.</br>
	 * This supports loading objects from different object stores. Objects from
	 * different object stores can reference each other.
	 * 
	 * @param objectPocket
	 */
	public void link(ObjectPocket objectPocket);

}
