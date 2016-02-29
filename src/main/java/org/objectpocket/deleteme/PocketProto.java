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

package org.objectpocket.deleteme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectpocket.storage.FileStore;
import org.objectpocket.storage.ObjectStore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class PocketProto {
	
	private ObjectStore store;
	private Gson gson;
	private boolean prettyPrint = true; 
	
	public PocketProto(String directory) {
		store = new FileStore(directory);
		GsonBuilder gsonBuilder = new GsonBuilder();
		if (prettyPrint) {
			gsonBuilder.setPrettyPrinting();
		}
		gson = gsonBuilder.create();
	}
	
	public void writeJsonObjects(List<Object> objects) {
		Map<String, Set<String>> objectMap = new HashMap<String, Set<String>>();
		for (Object object : objects) {
			String className = object.getClass().getCanonicalName();
			Set<String> objectSet = objectMap.get(className);
			if (objectSet == null) {
				objectSet = new HashSet<String>();
				objectMap.put(className, objectSet);
			}
			String json = gson.toJson(object);
			if (prettyPrint) {
				json = json.replaceFirst("\\{", "{\n  \"class\":\"" + Person.class.getCanonicalName() + "\",");
			} else {
				json = json.replaceFirst("\\{", "{\"class\":\"" + Person.class.getCanonicalName() + "\",");
			}
			objectSet.add(json);
			System.out.println(json);
		}
		for (String key : objectMap.keySet()) {
			try {
				store.writeJsonObjects(objectMap.get(key), key);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	class Person {
		protected String name;
		protected Address address;
		protected List<Person> friends;
	}

	class Address {
		protected String city;
		protected String street;
		protected String number;
	}

	public static void main(String[] args) {
		
		PocketProto pocketProto = new PocketProto("c:/testdir");

		// example objects
		Address karlsruhe = pocketProto.new Address();
		karlsruhe.city = "Karlsruhe";
		Person anton = pocketProto.new Person();
		anton.name = "Anton";
		anton.address = karlsruhe;
		
		List<Object> objects = new ArrayList<Object>();
		objects.add(anton);
		pocketProto.writeJsonObjects(objects);
	}

}
