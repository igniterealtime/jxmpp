/**
 *
 * Copyright 2019 Florian Schmaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jxmpp.util;

import java.lang.reflect.Array;

public class ArraysUtil {

	/**
	 * Concatenate two arrays.
	 *
	 * @param arrayOne the first input array.
	 * @param arrayTwo the second input array.
	 * @param <T> the type of the arrays.
	 * @return a new array where the first and second array are concatenated.
	 */
	public static <T> T concatenate(T arrayOne, T arrayTwo) {
		if (!arrayOne.getClass().isArray() || !arrayTwo.getClass().isArray()) {
			throw new IllegalArgumentException();
		}

		int arrayOneLength = Array.getLength(arrayOne);
		int arrayTwoLength = Array.getLength(arrayTwo);

		@SuppressWarnings("unchecked")
		T res = (T) Array.newInstance(arrayOne.getClass().getComponentType(), arrayOneLength + arrayTwoLength);
		System.arraycopy(arrayOne, 0, res, 0, arrayOneLength);
		System.arraycopy(arrayTwo, 0, res, arrayOneLength, arrayTwoLength);

		return res;
	}

}
