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

package org.objectpocket.example;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class Person {

    private String name;
    private String surname = UUID.randomUUID().toString();
    private Date birthdate = new Date();
    private Address address;
    private List<Person> friends;

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

    public List<Person> getFriends() {
	return friends;
    }

    public void setFriends(List<Person> friends) {
	this.friends = friends;
    }

    public String getSurname() {
	return surname;
    }

    public Date getBirthdate() {
	return birthdate;
    }

}
