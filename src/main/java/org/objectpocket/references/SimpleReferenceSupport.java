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

package org.objectpocket.references;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectpocket.annotations.Entity;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class SimpleReferenceSupport extends ReferenceSupport {

	@Override
	public List<Field> filterReferencingFields(List<Field> fields) {
		List<Field> filteredFields = null;
		for (Field field : fields) {
			if (field.getType().getAnnotation(Entity.class) != null) {
				if (filteredFields == null) {
					filteredFields = new ArrayList<Field>();
				}
				filteredFields.add(field);
			}
		}
		return filteredFields;
	}

	@Override
	public Set<Object> getObjectsForPropertyDescriptor(Object obj, PropertyDescriptor propertyDescriptor) 
			throws InvocationTargetException, IllegalAccessException {
		if (propertyDescriptor != null) {
			Object object = propertyDescriptor.getReadMethod().invoke(obj);
			if (object != null) {
				Set<Object> identifiables = new HashSet<Object>();
				identifiables.add(object);
				return identifiables;
			}
		}
		return null;
	}
	
	@Override
	public Set<Object> getObjectsForField(Object obj, Field field) throws InvocationTargetException, IllegalAccessException {
		if (field != null) {
			field.setAccessible(true);
			Object object = field.get(obj);
			field.setAccessible(false);
			if (object != null) {
				Set<Object> objects = new HashSet<Object>();
				objects.add(object);
				return objects;
			}
		}
		return null;
	}

	@Override
	public void injectReferences(Object obj, Field field, Map<String, Map<String, Object>> objectMap,
			Map<Object, String> idsFromReadObjects) throws InvocationTargetException, IllegalAccessException {
		field.setAccessible(true);
		Object readObject = field.get(obj);
		field.setAccessible(false);
		if (readObject != null) {
			// marking proxy objects as proxy prevents from persisting proxy objects!
			//readObject.setProxy(true);
			Map<String, Object> typeMap = objectMap.get(field.getType().getName());
			if (typeMap != null) {
				Object reference = typeMap.get(idsFromReadObjects.get(readObject));
				if (reference != null) {
					field.setAccessible(true);
					field.set(obj, reference);
					field.setAccessible(false);
				}
			}
		}
	}

}
