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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class FileReadTest {

	public static void main(String[] args) throws Exception {
		
		long time = System.currentTimeMillis();
		File f = new File("C:/Users/klause/objectpocket_example/org.objectpocket.example.Person");
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		StringBuffer buf = new StringBuffer();
		while((line = br.readLine()) != null) {
			buf.append(line);
		}
		br.close();
		String string = buf.toString();
		Pattern pattern = Pattern.compile("\"op_type\":.+\\,");
		Matcher matcher = pattern.matcher(string);
		String group = "";
		Set<String> classes = new HashSet<String>();
		while(matcher.find()) {
			group = matcher.group();
			group = group.substring(group.indexOf(":")+1, group.indexOf(",")).trim().replace("\"", "");
			classes.add(group);
		}
		System.out.println(System.currentTimeMillis() - time);
		for (String clazz : classes) {
			System.out.println(clazz);
		}
		
	}
	
}
