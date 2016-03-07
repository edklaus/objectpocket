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

package org.objectpocket.deleteme.complex;

import java.util.Collection;

import org.objectpocket.ObjectPocket;
import org.objectpocket.ObjectPocketBuilder;
import org.objectpocket.exception.ObjectPocketException;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ComplexExample {
	
	private static final String FILESTORE = System.getProperty("user.home") + "/objectpocket_example_complex";
	
	private void store() {
		
		Person mother = new Person();
		mother.setName("mother");
		Person child = new Person();
		child.setName("child");
		
		Address mothersAddress = new Address();
		mothersAddress.setCity("Karlsruhe");
		Address childsAddress = new Address();
		childsAddress.setCity("Mannheim");
		
		Car mothersCar = new Car();
		mothersCar.setName("BWM");
		Car childsCar = new Car();
		childsCar.setName("Audio");
		
		child.setAddress(childsAddress);
		child.setCar(childsCar);
		
		mother.setAddress(mothersAddress);
		mother.setChild(child);
		child.setMother(mother);
		mother.setCar(mothersCar);
		
		ObjectPocket objectPocket = new ObjectPocketBuilder().createFileObjectPocket(FILESTORE);
		objectPocket.add(mother);
		try {
			objectPocket.store();
		} catch (ObjectPocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ObjectPocket objectPocket2 = new ObjectPocketBuilder().createFileObjectPocket(FILESTORE);
		try {
			objectPocket2.load();
		} catch (ObjectPocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Collection<Person> persons = objectPocket2.findAll(Person.class);
		for (Person person : persons) {
			System.out.println(person.getName());
			System.out.println(person.getAddress().getCity());
			System.out.println(person.getCar().getName());
		}
		
	}
	
	public static void main(String[] args) {
		ComplexExample complexExample = new ComplexExample();
		complexExample.store();
	}

}
