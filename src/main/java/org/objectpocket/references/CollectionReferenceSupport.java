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

package org.objectpocket.references;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
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
public class CollectionReferenceSupport extends ReferenceSupport {

	@Override
	public List<Field> filterReferencingFields(List<Field> fields) {
		List<Field> filteredFields = null;
		for (Field field : fields) {
			if (Collection.class.isAssignableFrom(field.getType())) {
				Type genericReturnType = field.getGenericType();
				if (genericReturnType instanceof ParameterizedType) {
					ParameterizedType type = (ParameterizedType)genericReturnType;
					Class<?> clazz;
					try {
						clazz = Class.forName(type.getActualTypeArguments()[0].getTypeName());
						if (clazz.getAnnotation(Entity.class) != null) {
							if (filteredFields == null) {
								filteredFields = new ArrayList<Field>();
							}
							filteredFields.add(field);
						}
					} catch (ClassNotFoundException e) {
						// TODO: log
						e.printStackTrace();
					}
				}
			}
		}
		return filteredFields;
	}

	@Override
	public Set<Object> getObjectsForField(Object obj, Field field) throws InvocationTargetException, IllegalAccessException {
		if (field != null) {
			field.setAccessible(true);
			Object o = field.get(obj);
			field.setAccessible(false);
			if (o != null) {
				Collection<Object> collection = (Collection<Object>)o;
				Set<Object> objects = new HashSet<Object>();
				for (Object object : collection) {
					if (object != null) {
						objects.add(object);
					}
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
		@SuppressWarnings("unchecked")
		Collection<Object> readObjects = (Collection<Object>)field.get(obj);
		field.setAccessible(false);
		if (readObjects != null) {
			// TODO:
			// marking proxy objects as proxy prevents from persisting proxy objects!
//			for (Identifiable identifiable : ids) {
//				if (identifiable != null) {
//					identifiable.setProxy(true);
//				}
//			}
			// TODO: This seems too complicated to get the generic type of collection
			Type genericType = field.getGenericType();
			ParameterizedType type = (ParameterizedType)genericType;
			String typeName = type.getActualTypeArguments()[0].getTypeName();
			Map<String, Object> typeMap = objectMap.get(typeName);
			
			if (typeMap != null) {
				Object[] tempArray = new Object[readObjects.size()];
				int i = 0;
				for (Object object : readObjects) {
					Object reference = typeMap.get(idsFromReadObjects.get(object.hashCode()));
					if (reference != null) {
						tempArray[i] = objectMap.get(typeName).get(idsFromReadObjects.get(object.hashCode()));
					} else {
						tempArray[i] = object;
					}
					i++;
				}
				readObjects.clear();
				for (Object o : tempArray) {
					readObjects.add(o);
				}
			}
		}

	}

}
