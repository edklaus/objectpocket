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

import org.objectpocket.storage.blob.BlobStore;

/**
 * 
 * @author Edmund Klaus
 *
 */
public interface ObjectStore extends BlobStore {
    
    /**
     * Returns true if the given store exists.
     * 
     * @return
     */
    public boolean exists();

    /**
     * Loads all object type names from the underlying store.
     * 
     * @return object type names as strings<br> null if no objects type names
     *         are available
     * @throws IOException
     *             If an I/O error occurs
     */
    public Set<String> getAvailableObjectTypes() throws IOException;

    /**
     * Reads json objects of given typeName from the object store.
     * 
     * @param typeName
     *            fully qualified class name
     * @return Map<filename, Map<jsonObject, id>> (id can be
     *         reference when custom id has been set with @Id)
     * @throws IOException
     *             If an I/O error occurs
     */
    public Map<String, Map<String, String>> readJsonObjects(String typeName)
	    throws IOException;

    /**
     * Writes json objects to the object store.
     * 
     * @param jsonObjects
     *            Map<typeName, Map<filename, Set<jsonString>>>
     * @throws IOException
     *             If an I/O error occurs
     */
    public void writeJsonObjects(
	    Map<String, Map<String, Set<String>>> jsonObjects)
	    throws IOException;

    /**
     * Returns the source where data is loaded from.
     * 
     * @return
     */
    public String getSource();
    
}
