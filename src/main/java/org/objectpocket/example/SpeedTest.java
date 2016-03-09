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

package org.objectpocket.example;

import java.util.Collection;

import org.objectpocket.ObjectPocket;
import org.objectpocket.ObjectPocketBuilder;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class SpeedTest {
	
	private static final String FILESTORE = System.getProperty("user.home") + "/objectpocket_example";
	
	public static void main(String[] args) throws Exception {
		
		long time = System.currentTimeMillis();
		ObjectPocket objectPocket = new ObjectPocketBuilder().createFileObjectPocket(FILESTORE);
		
		int numPersons = 100_000;
		
		for (int i = 0; i < numPersons; i++) {
			Address a = new Address();
			a.setCity("city"+i);
			Person p = new Person("name" + i, a);
			objectPocket.add(p);
		}
		time = System.currentTimeMillis();
		objectPocket.store();
		System.out.println("CREATE AND STORE: " + (System.currentTimeMillis()-time));
		
		time = System.currentTimeMillis();
		ObjectPocket objectPocket2 = new ObjectPocketBuilder().createFileObjectPocket(FILESTORE);
		objectPocket2.load();
		System.out.println("LOAD: " + (System.currentTimeMillis()-time));
		time = System.currentTimeMillis();
		Collection<Person> findAll = objectPocket2.findAll(Person.class);
		for (Person person : findAll) {
			if (person.getName().equals("name789")) {
				System.out.println("time consumed: " + (System.currentTimeMillis() - time));
			}
		}
		
		
	}

}
