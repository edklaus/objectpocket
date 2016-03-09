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

package org.objectpocket.gson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.objectpocket.ObjectPocketImpl;
import org.objectpocket.ProxyOut;
import org.objectpocket.annotations.Entity;
import org.objectpocket.util.ReflectionUtil;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * 
 * This is where references with the annotation {@link Entity} are handled to insert
 * as references into JSON strings and not as complete objects.
 * 
 * @author Edmund Klaus
 *
 */
public class CustomTypeAdapterFactory implements TypeAdapterFactory {

	private ObjectPocketImpl objectPocket;

	public CustomTypeAdapterFactory(ObjectPocketImpl objectPocket) {
		this.objectPocket = objectPocket;
	}

	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {

		TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

		// @Entity
		if (type.getRawType().getAnnotation(Entity.class) != null) {
			return new TypeAdapter<T>() {
				public void write(JsonWriter out, T obj) throws IOException {
					if (obj != null) {
						String id = objectPocket.getIdForObject(obj);
						// normalize
						if (!objectPocket.isSerializeAsRoot(obj)) {
							gson.toJson(new ProxyOut(obj.getClass().getTypeName(), id), ProxyOut.class, out);
							return;
						} 
						else {
							objectPocket.setSerializeAsRoot(obj, false);
						}
					}
					// default serialization
					delegate.write(out, obj);
				};
				@SuppressWarnings("unchecked")
				@Override
				public T read(JsonReader in) throws IOException {
					if (in.getPath().length() > 2) {
						in.beginObject();
						in.nextName();
						StringBuilder sb = new StringBuilder(in.nextString());
						String id = sb.substring(0, sb.indexOf("@"));
						in.endObject();
						T obj = null;
						try {
							obj = (T)ReflectionUtil.instantiateDefaultConstructor(type.getRawType());
						} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | 
								NoSuchMethodException | InvocationTargetException e) {
							throw new IOException("Could not instanciate class " + type.getRawType().getName() + "\n"
									+ "\tMight be that the class has no default constructor!", e);
						}
						objectPocket.addIdFromReadObject(obj.hashCode(), id);
						return obj;
					} else {
						T obj = delegate.read(in);
						return obj;
					}
				}
			};
		}
		// All other
		else {
			return delegate;
		}
	}

}
