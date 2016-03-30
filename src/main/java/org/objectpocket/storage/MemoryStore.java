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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectpocket.Blob;
import org.objectpocket.ProxyIn;

import com.google.gson.Gson;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class MemoryStore implements ObjectStore {

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
	public Map<String, String> readJsonObjects(String typeName) {
		Set<String> set = jsonObjectMap.get(typeName);
		Map<String, String> jsonObjects = new HashMap<String, String>(set.size());
		Gson gson = new Gson();
		for (String string : set) {
			ProxyIn proxy = gson.fromJson(string, ProxyIn.class);
			jsonObjects.put(string, proxy.getId());
		}
		return jsonObjects;
	}

	@Override
	public void writeJsonObjects(Map<String, Map<String, Set<String>>> jsonObjects) {
		for (String typeName : jsonObjects.keySet()) {
			Map<String, Set<String>> objectsForType = jsonObjects.get(typeName);
			Set<String> allObjects = new HashSet<String>();
			for (String key : objectsForType.keySet()) {
				allObjects.addAll(objectsForType.get(key));
			}
			jsonObjectMap.put(typeName, allObjects);
		}
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
	public void close() throws IOException {
		
	}
	
	@Override
	public String getSource() {
		return "MemStore." + hashCode();
	}

}
