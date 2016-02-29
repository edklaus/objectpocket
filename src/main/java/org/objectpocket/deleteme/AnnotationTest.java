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

import java.lang.reflect.Field;

import org.objectpocket.annotations.Entity;
import org.objectpocket.annotations.Id;
import org.objectpocket.annotations.Pocket;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class AnnotationTest {
	
	@Pocket(name="myfilename")
	@Entity
	class Person {
		@Id
		String name;
	}
	
	public AnnotationTest() {
		
		if (Person.class.isAnnotationPresent(Pocket.class)) {
			System.out.println("Class " + Person.class.getName() + " has annotation " + 
					Pocket.class.getName() + " with value \"" + 
					Person.class.getAnnotation(Pocket.class).name() + "\"");
		}
		
		if (Person.class.isAnnotationPresent(Entity.class)) {
			System.out.println("Class " + Person.class.getName() + " has annotation " + 
					Entity.class.getName());
		}
		
		Field[] fields = Person.class.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Id.class)) {
				System.out.println("Class " + Person.class.getName() + " has id \"" + field.getName() + "\"");
			}
		}
	}
	
	public static void main(String[] args) {
		new AnnotationTest();
	}

}
