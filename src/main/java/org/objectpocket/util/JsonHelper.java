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

/**
 * 
 * @author Edmund Klaus
 *
 */
public class JsonHelper {
	
	public static final String CLASS = "op_class";
	public static final String ID = "op_id";

	// FIXME: make this maximum fast!
	public static String addClassAndIdToJson(String jsonString, String typeName, String id, boolean prettyPrinting) {
		System.out.println(jsonString);
		String classAndIdString = "";
		if (prettyPrinting) {
			classAndIdString = "{\n  \"" + CLASS + "\": \"" + typeName + "\","
							+ "\n  \"" + ID + "\": \"" + id + "\"";
		} else {
			classAndIdString = "{\"" + CLASS + "\":\"" + typeName + "\","
					+ "\"" + ID + "\":\"" + id + "\"";
		}
		if (jsonString.trim().contains(":")) {
			classAndIdString += ",";
		}
		jsonString = jsonString.replaceFirst("\\{", "");
		jsonString = classAndIdString + jsonString;
		return jsonString;
	}
	
	// FIXME: make this maximum fast!
	public static String[] getClassAndIdFromJson(String jsonString) {
		String[] classAndId = new String[2];
		String[] fields = jsonString.split(",");
		// class
		String[] classField = fields[0].split(":");
		if (classField[0].contains(CLASS)) {
			classAndId[0] = classField[1].trim().replaceAll("\"", "").replaceAll("\n", "");
		}
		// id
		String[] idField = fields[1].split(":");
		if (idField[0].contains(ID)) {
			classAndId[1] = idField[1].trim().replaceAll("\"", "").replaceAll("\n", "");
		}
		return classAndId;
	}
	
}
