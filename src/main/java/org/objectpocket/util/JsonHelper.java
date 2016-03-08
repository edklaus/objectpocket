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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * @author Edmund Klaus
 *
 */
public class JsonHelper {
	
	public static final String TYPE = "op_type";
	public static final String ID = "op_id";

	// FIXME: make this maximum fast!
	public static String addClassAndIdToJson(String jsonString, String typeName, String id, boolean prettyPrinting) {
		String classAndIdString = "";
		if (prettyPrinting) {
			classAndIdString = "{\n  \"" + TYPE + "\": \"" + typeName + "\","
							+ "\n  \"" + ID + "\": \"" + id + "\"";
		} else {
			classAndIdString = "{\"" + TYPE + "\":\"" + typeName + "\","
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
		String regex = "\"" + TYPE + "\":.*\"" + ID + "\":.*,";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(jsonString);
		if (matcher.find()) {
			jsonString = jsonString.substring(matcher.start(), matcher.end());
		}
		String[] classAndId = new String[2];
		String[] fields = jsonString.split(",");
		// class
		String[] classField = fields[0].split(":");
		if (classField[0].contains(TYPE)) {
			classAndId[0] = classField[1].trim().replaceAll("\"", "").replaceAll("\n", "");
		}
		// id
		String[] idField = fields[1].split(":");
		if (idField[0].contains(ID)) {
			classAndId[1] = idField[1].trim().replaceAll("\"", "").replaceAll("\n", "");
		}
		return classAndId;
	}
	
	public static List<String> splitToTopLevelJsonObjects(String s) {
		List<String> jsonObjects = new ArrayList<String>();
		int objectStartIndex = 0;
		int objectStart = 0;
		int objectEnd = 0;
		for (int i = 0; i < s.length(); i++) {
			switch (s.charAt(i)) {
			case '{':
				if (objectStart == 0) {
					objectStartIndex = i;
				}
				objectStart++;
				break;
			case '}':
				objectEnd++;
				if (objectStart == objectEnd) {
					jsonObjects.add(s.substring(objectStartIndex, i+1));
					objectStart = 0;
					objectEnd = 0;
				}
				break;
			default:
				break;
			}
		}
		return jsonObjects;
	}
	
}
