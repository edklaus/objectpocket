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

import java.util.ArrayList;
import java.util.List;

import org.objectpocket.annotations.Entity;

/**
 * 
 * @author Edmund Klaus
 *
 */
@Entity
public class Person {
	
	private String name;
	private Person mother;
	private Person child;
	private Address address;
	private Car[] cars;
	private List<Dog> dogs;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Person getMother() {
		return mother;
	}
	public void setMother(Person mother) {
		this.mother = mother;
	}
	public Person getChild() {
		return child;
	}
	public void setChild(Person child) {
		this.child = child;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public Car[] getCars() {
		return cars;
	}
	public void addCar(Car car) {
		if (cars == null) {
			cars = new Car[0];
		}
		Car[] newCars = new Car[cars.length+1];
		for (int i = 0; i < cars.length; i++) {
			newCars[i] = cars[i];
		}
		newCars[newCars.length-1] = car;
		cars = newCars;
	}
	public List<Dog> getDogs() {
		return dogs;
	}
	public void addDog(Dog dog) {
		if (dogs == null) {
			dogs = new ArrayList<Dog>();
		}
		dogs.add(dog);
	}
}
