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


/**
 * 
 * @author Edmund Klaus
 *
 */
public class JsonHelper {
	
	public static final String TYPE = "op_type";
	public static final String ID = "op_id";
	
	public static final String JSON_PREFIX = "{\"objects\":[";
	public static final String JSON_SUFFIX = "]}";
	
	public static String addTypeAndIdToJson(StringBuilder jsonString, String typeName, String id, boolean prettyPrinting) {
		StringBuilder sb = new StringBuilder();
		if (prettyPrinting) {
			sb.append("{\n  \"");
			sb.append(TYPE);
			sb.append("\": \"");
			sb.append(typeName);
			sb.append("\",\n  \"");
			sb.append(ID);
			sb.append("\": \"");
			sb.append(id);
			sb.append("\"");
		} else {
			sb.append("{\"");
			sb.append(TYPE);
			sb.append("\":\"");
			sb.append(typeName);
			sb.append("\",\"");
			sb.append(ID);
			sb.append("\":\"");
			sb.append(id);
			sb.append("\"");
		}
		if (jsonString.indexOf(":") > -1) {
			sb.append(",");
		}
		int index = jsonString.indexOf("{");
		if (index > -1) {
			jsonString = jsonString.replace(0, index+1, "");
		}
		sb.append(jsonString);
		return sb.toString();
	}
	
	public static List<String> splitToTopLevelJsonObjects(String s) {
		List<String> jsonObjects = new ArrayList<String>(1000000);
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
