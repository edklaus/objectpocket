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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.objectpocket.annotations.Entity;

/**
 * 
 * @author Emdund Klaus
 *
 */
public abstract class ReferenceSupport {

    private Map<String, List<Field>> fieldsForType = new HashMap<String, List<Field>>();

    /**
     * This method needs to be implemented in order to support a specific kind
     * of reference detection.
     * 
     * @param fields
     * @return
     */
    public abstract List<Field> filterReferencingFields(List<Field> fields);

    public abstract void injectReferences(Object obj, Field field,
	    Map<String, Map<String, Object>> objectMap,
	    Map<Object, String> idsFromReadObjects)
	    throws InvocationTargetException, IllegalAccessException;

    public abstract Set<Object> getObjectsForField(Object obj, Field field)
	    throws InvocationTargetException, IllegalAccessException;

    /**
     * Detects references to other objects inside the given object that have
     * been annotated with {@link Entity}. Returns the references as a
     * {@link Set} of objects.
     * 
     * @param obj
     * @return
     */
    public Set<Object> getReferences(Object obj) {
	List<Field> objectFields = getFields(obj);
	if (objectFields != null && !objectFields.isEmpty()) {
	    Set<Object> references = new HashSet<Object>();
	    for (Field field : objectFields) {
		Set<Object> objectsForField = null;
		try {
		    objectsForField = getObjectsForField(obj, field);
		} catch (InvocationTargetException | IllegalAccessException e) {
		    // TODO exception handling
		    e.printStackTrace();
		}
		if (objectsForField != null) {
		    references.addAll(objectsForField);
		}
	    }
	    return references;
	}
	return null;
    }

    private List<Field> getFields(Object obj) {
	if (!fieldsForType.containsKey(obj.getClass().getName())) {
	    List<Field> allFields = FieldUtils.getAllFieldsList(obj.getClass());
	    List<Field> filteredFields = filterTransientFields(allFields);
	    filteredFields = filterReferencingFields(filteredFields);
	    fieldsForType.put(obj.getClass().getName(), filteredFields);
	}
	return fieldsForType.get(obj.getClass().getName());
    }
    
    private List<Field> filterTransientFields(List<Field> fields) {
        List<Field> returnFields = new ArrayList<Field>(fields.size());
        for (Field field : fields) {
            if (!Modifier.isTransient(field.getModifiers())) {
                returnFields.add(field);
            }
        }
        return returnFields;
    }

    public void injectReferences(Object obj,
	    Map<String, Map<String, Object>> objectMap,
	    Map<Object, String> idsFromReadObjects) {
	List<Field> objectFields = getFields(obj);
	if (objectFields != null && !objectFields.isEmpty()) {
	    for (Field field : objectFields) {
		try {
		    injectReferences(obj, field, objectMap, idsFromReadObjects);
		} catch (InvocationTargetException | IllegalAccessException e) {
		    // TODO exception handling
		    e.printStackTrace();
		}
	    }
	}
    }

}
