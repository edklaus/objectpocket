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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectpocket.ObjectPocket;
import org.objectpocket.ObjectPocketBuilder;
import org.objectpocket.exception.ObjectPocketException;

/**
 * A simple example for using Japer to persist and load data.
 * 
 * @author Edmund Klaus
 *
 */
public class SimpleExample {

    private static final String FILESTORE = System.getProperty("user.home")
	    + "/objectpocket_example";

    public void createAndPersist() {

	// define store
	ObjectPocket objectPocket = new ObjectPocketBuilder()
		.createFileObjectPocket(FILESTORE);

	// create data
	Address a1 = new Address();
	a1.setCity("Karlsruhe");
	Address a2 = new Address();
	a2.setCity("Mannheim");
	Person p1 = new Person("person1", a1);
	Person p2 = new Person("person2", a2);
	List<Person> friends = p1.getFriends();
	if (friends == null) {
	    friends = new ArrayList<Person>();
	}
	friends.add(p2);
	p1.setFriends(friends);

	// store
	// note: all references are added automatically
	// you don't have to add every single object
	objectPocket.add(p1);
	try {
	    objectPocket.store();
	    ;
	} catch (ObjectPocketException e) {
	    e.printStackTrace();
	}
    }

    public void load() {

	// define store
	ObjectPocket objectPocket = new ObjectPocketBuilder()
		.createFileObjectPocket(FILESTORE);
	try {
	    objectPocket.load();
	} catch (ObjectPocketException e) {
	    e.printStackTrace();
	}

	// access data
	Collection<Person> persons = objectPocket.findAll(Person.class);
	for (Person person : persons) {
	    System.out.println("found person: " + person.getName());
	    Address address = person.getAddress();
	    if (address != null) {
		System.out.println("  with address: "
			+ person.getAddress().getCity());
	    }
	    List<Person> friends = person.getFriends();
	    if (friends != null) {
		for (Person friend : friends) {
		    System.out.println("  and friend " + friend.getName());
		}
	    }
	}

    }

    public static void main(String[] args) throws Exception {
	SimpleExample simpleExample = new SimpleExample();
	simpleExample.createAndPersist();
	simpleExample.load();
    }

}
