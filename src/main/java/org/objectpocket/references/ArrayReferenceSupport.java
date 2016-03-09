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
public class ArrayReferenceSupport extends ReferenceSupport {

	@Override
	public List<Field> filterReferencingFields(List<Field> fields) {
		List<Field> filteredFields = null;
		for (Field field : fields) {
			if (field.getType().isArray() && field.getType().getComponentType().getAnnotation(Entity.class) != null) {
				if (filteredFields == null) {
					filteredFields = new ArrayList<Field>();
				}
				filteredFields.add(field);
			}
		}
		return filteredFields;
	}

	@Override
	public Set<Object> getObjectsForField(Object obj, Field field) throws InvocationTargetException, IllegalAccessException {
		if (field != null) {
			field.setAccessible(true);
			Object[] objectArray = (Object[])field.get(obj);
			field.setAccessible(false);
			if (objectArray != null) {
				Set<Object> objects = new HashSet<Object>();
				for (Object object : objectArray) {
					objects.add(object);
				}
				return objects;
			}
		}
		return null;
	}

	@Override
	public void injectReferences(Object obj, Field field, Map<String, Map<String, Object>> objectMap,
			Map<Integer, String> idsFromReadObjects) throws InvocationTargetException, IllegalAccessException {
		field.setAccessible(true);
		Object[] readObjects = (Object[])field.get(obj);
		field.setAccessible(false);
		if (readObjects != null) {
			// TODO:
			// marking proxy objects as proxy prevents from persisting proxy objects!
//			for (Identifiable identifiable : ids) {
//				if (identifiable != null) {
//					identifiable.setProxy(true);
//				}
//			}
			String typeName = field.getType().getComponentType().getName();
			Map<String, Object> typeMap = objectMap.get(typeName);
			if (typeMap != null) {
				for (int i = 0; i < readObjects.length; i++) {
					if (readObjects[i] != null) {
						Object reference = typeMap.get(idsFromReadObjects.get(readObjects[i].hashCode()));
						if (reference != null) {
							field.setAccessible(true);
							readObjects[i] = reference;
							field.setAccessible(false);
						}
					}
				}
			}
		}
	}

}
