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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectpocket.annotations.Id;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketIdTest {

	@Test
	public void testFindById() throws Exception {
		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
		objectPocket.add(new Address("Karlsruhe"));
		objectPocket.store();
		objectPocket.load();
		Address find = objectPocket.find("Karlsruhe", Address.class);
		assertTrue(find.getCity().equals("Karlsruhe"));
	}
	
	public class Address {
		@Id
		private String city;
		public Address() {
		}
		public Address(String city) {
			this.city = city;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
	}

}
