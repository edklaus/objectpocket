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

package org.objectpocket.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.objectpocket.annotations.Id;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class IdSupport {

	public static final String OP_REF_STRING = "op_ref:";
	private static Map<String, Field> idFieldForType_ObjectsInMemory = new HashMap<String, Field>();
	private static Map<String, Field> idFieldForType_ObjectsFromStore = new HashMap<String, Field>();

	public static String getId(Object obj, boolean referenceForAnnotation) {
		return getId(obj, referenceForAnnotation, null);
	}
	
	/**
	 * returns id from annotation @Id or existing id or generate random UUID.
	 * 
	 * @param obj
	 * @param referenceForAnnotation when set to true the method will return a referenced
	 * 	representation of the id instead of the real id. This only happens for classes with
	 *  custom id defined by @Id annotation. For all others the generated Id will be returned.
	 *  @param put an existing id here if one exists, otherwise a new might be generated
	 * @return
	 */
	public static String getId(Object obj, boolean referenceForAnnotation, String existingId) {
		String typeName = obj.getClass().getName();
		if (!idFieldForType_ObjectsInMemory.containsKey(typeName)) {
			Field[] fields = FieldUtils.getAllFields(obj.getClass());
			for (Field field : fields) {
				if (field.isAnnotationPresent(Id.class)) {
					if (field.getType().equals(String.class)) {
						idFieldForType_ObjectsInMemory.put(typeName, field);
					} else {
						Logger.getAnonymousLogger().warning("@Id annotated field in class " + 
								typeName + " is not of type java.lang.String. Will generate random ids for this class.");
					}
					break;
				}
			}
			if (!idFieldForType_ObjectsInMemory.containsKey(typeName)) {
				idFieldForType_ObjectsInMemory.put(typeName, null);
			}
		}
		Field field = idFieldForType_ObjectsInMemory.get(typeName);
		if (field != null) {
			try {
				field.setAccessible(true);
				String id = (String)field.get(obj);
				if (id != null) {
					if (referenceForAnnotation) {
						return OP_REF_STRING + field.getName();
					} else {
						return id;
					}
				} else {
					Logger.getAnonymousLogger().warning("Id for object " + obj + " has not been set. "
							+ "Will generate random id for this class.");
				}
			} catch (IllegalAccessException e) {
				Logger.getAnonymousLogger().log(Level.WARNING, "Could not read id from class " + typeName, e);
			}
		}
		if (existingId != null) {
			return existingId;
		}
		return UUID.randomUUID().toString();
	}

	/**
	 * returns id for object that has been read from json string
	 * @param obj
	 * @param idFromProxyIn
	 * @return
	 */
	public static String getId(Object obj, String idFromProxyIn) {
		if (idFromProxyIn.startsWith(OP_REF_STRING)) {
			String typeName = obj.getClass().getName();
			if (!idFieldForType_ObjectsFromStore.containsKey(typeName)) {
				String fieldName = idFromProxyIn.substring(OP_REF_STRING.length(), idFromProxyIn.length());
				Field field = FieldUtils.getField(obj.getClass(), fieldName, true);
				idFieldForType_ObjectsFromStore.put(typeName, field);
			}
			Field field = idFieldForType_ObjectsFromStore.get(typeName);
			try {
				idFromProxyIn = (String)field.get(obj);
			} catch (IllegalAccessException e) {
				Logger.getAnonymousLogger().log(Level.WARNING, "Could not read id from field " + 
						field.getName() + " for class " + obj.getClass(), e);
			}
		}
		return idFromProxyIn;
	}

}
