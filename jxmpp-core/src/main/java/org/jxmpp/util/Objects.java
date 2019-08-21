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

public class Objects {

	/**
	 * Checks that the specified object reference is not <code>null</code> and
	 * throws a customized {@link IllegalArgumentException} if it is.
	 * <p>
	 * Note that unlike <code>java.util.Objects</code>, this method throws an
	 * {@link IllegalArgumentException} instead of an {@link NullPointerException}.
	 * </p>
	 *
	 * @param <T>           the type of the reference.
	 * @param obj           the object reference to check for nullity.
	 * @param parameterName the name of the parameter which is null
	 * @return <code>obj</code> if not null.
	 * @throws IllegalArgumentException in case <code>obj</code> is
	 *                                  <code>null</code>.
	 */
	public static <T> T requireNonNull(T obj, String parameterName) throws IllegalArgumentException {
		if (obj == null) {
			throw new IllegalArgumentException("Argument '" + parameterName + "' must not be null");
		}
		return obj;
	}
}
