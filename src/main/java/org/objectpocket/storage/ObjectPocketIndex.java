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

package org.objectpocket.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketIndex {
	
	private Map<String, Set<String>> typeToFilenamesMapping = new HashMap<String, Set<String>>();

	protected Map<String, Set<String>> getTypeToFilenamesMapping() {
		return typeToFilenamesMapping;
	}
	
}