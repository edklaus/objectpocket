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

package org.objectpocket;

import java.util.Collection;

import org.objectpocket.exception.ObjectPocketException;

/**
 * 
 * @author Edmund Klaus
 *
 */
public interface ObjectPocket {
	
	public void add(Object obj);
	
	public void store() throws ObjectPocketException;
	
	public void load() throws ObjectPocketException;
	
	public void loadAsynchronous(Class<?>... preload) throws ObjectPocketException;
	
	public boolean isLoading();
	
	public <T> T find(String id, Class<T> type);
	
	public <T> Collection<T> findAll(Class<T> type);
	
	public void remove(Object obj) throws ObjectPocketException;
	
	public void cleanup();
	
	public void link(ObjectPocket objectPocket);

}
