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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectpocket.Blob;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class MemStore implements ObjectStore {

	/**
	 * Keep json objects in memory with this list.</br>
	 * - key: typeName</br>
	 * - value:	json objects for this type
	 */
	private Map<String, Set<String>> jsonObjectMap = new HashMap<String, Set<String>>();
	private Map<String, byte[]> blobData = new HashMap<String, byte[]>();

	@Override
	public Set<String> getAvailableObjectTypes() {
		return jsonObjectMap.keySet();
	}

	@Override
	public Set<String> readJsonObjects(String typeName) {
		return jsonObjectMap.get(typeName);
	}

	@Override
	public void writeJsonObjects(Set<String> jsonObjects, String typeName) {
		if (jsonObjects == null || jsonObjects.isEmpty()) {
			jsonObjectMap.remove(typeName);
		}
		jsonObjectMap.put(typeName, jsonObjects);
	}

	@Override
	public void writeBlobs(Set<Blob> blobs) throws IOException {
		for (Blob blob : blobs) {
			blobData.put(blob.getPath(), blob.getBytes());
		}
	}

	@Override
	public byte[] loadBlobData(Blob blob) {
		return blobData.get(blob.getPath());
	}
	
	@Override
	public String getSource() {
		return "MemStore." + hashCode();
	}

}
