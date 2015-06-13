/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ojm.jackson;

import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.ojm.Marshaller;
import org.springframework.ojm.Unmarshaller;

public class JacksonProcessor implements Marshaller, Unmarshaller {

	private ObjectMapper objectMapper;

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public <T> T unmarshal(InputStream inputStream, Class<T> _class) throws Exception {
		return objectMapper.readValue(inputStream, _class);
	}

	@Override
	public void marshal(OutputStream outputStream, Object value) throws Exception {
		objectMapper.writeValue(outputStream, value);
	}
}
