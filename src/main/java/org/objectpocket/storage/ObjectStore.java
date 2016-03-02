/*
 * Copyright (C) 2015 Edmund Klaus
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

package org.objectpocket.storage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Edmund Klaus
 *
 */
public interface ObjectStore extends BlobStore {
	
	/**
	 * Loads all object type names from the underlying store.
	 * @return object type names as strings</br>
	 * 		null if no objects type names are available
	 * @throws IOException If an I/O error occurs
	 */
	public Set<String> getAvailableObjectTypes() throws IOException;
	
	/**
	 * Reads json objects of given typeName from the object store.
	 * @param typeName fully qualified class name
	 * @return map of object id, and json objects as strings
	 * @throws IOException If an I/O error occurs
	 */
	public Map<String, String> readJsonObjects(String typeName) throws IOException;
	
	/**
	 * Writes json objects of given typeName to the object store.
	 * @param jsonObjects json objects as strings.
	 *   If null or an empty Set is passed as argument the type will be removed
	 *   and will not be available anymore.
	 * @param typeName fully qualified class name
	 * @throws IOException If an I/O error occurs
	 */
	public void writeJsonObjects(Set<String> jsonObjects, String typeName) throws IOException;
	
	/**
	 * Returns the source where data is loaded from.
	 * @return
	 */
	public String getSource();

}
