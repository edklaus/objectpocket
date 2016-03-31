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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.objectpocket.annotations.Entity;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketEntityTest {

	@Test
	public void testCyclicReferences() throws Exception {
		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
		Address address = new Address();
		address.setCity("Karlsruhe");
		Person person = new Person("person1", address);
		address.setInhabitant(person);
		objectPocket.add(person);
		objectPocket.store();
		objectPocket.load();
		Collection<Person> persons = objectPocket.findAll(Person.class);
		Collection<Address> addresses = objectPocket.findAll(Address.class);
		assertTrue(persons.size() == 1);
		assertTrue(addresses.size() == 1);
		Person foundPerson = persons.iterator().next();
		Address foundAddress = addresses.iterator().next();
		assertFalse(foundAddress.getInhabitant() == null);
		assertFalse(foundPerson.getAddress() == null);
		assertTrue(foundAddress.getInhabitant().equals(foundPerson));
		assertTrue(foundPerson.getAddress().equals(foundAddress));
	}
	
	@Entity
	public class Person {
		private String name;
		private Address address;
		public Person() {
		}
		public Person(String name, Address address) {
			this.name = name;
			this.address = address;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Address getAddress() {
			return address;
		}
		public void setAddress(Address address) {
			this.address = address;
		}
	}
	
	@Entity
	public class Address {
		private String city;
		private Person inhabitant;
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public Person getInhabitant() {
			return inhabitant;
		}
		public void setInhabitant(Person inhabitant) {
			this.inhabitant = inhabitant;
		}
	}

}
