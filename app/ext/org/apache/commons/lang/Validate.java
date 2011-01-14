/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang;

/**
 * <p>This class assists in validating arguments.</p>
 * 
 * <p>The class is based along the lines of JUnit. If an argument value is 
 * deemed invalid, an IllegalArgumentException is thrown. For example:</p>
 * 
 * <pre>
 * Validate.isTrue( i > 0, "The value must be greater than zero: ", i);
 * Validate.notNull( surname, "The surname must not be null");
 * </pre>
 *
 * @author Apache Software Foundation
 * @author <a href="mailto:ola.berg@arkitema.se">Ola Berg</a>
 * @author Gary Gregory
 * @author Norm Deane
 * @since 2.0
 * @version $Id: Validate.java 905636 2010-02-02 14:03:32Z niallp $
 */
public class Validate {
    
    /**
     * Constructor. This class should not normally be instantiated.
     */
    public Validate() {
      super();
    }

    /**
     * <p>Validate that the argument condition is <code>true</code>; otherwise 
     * throwing an exception. This method is useful when validating according 
     * to an arbitrary boolean expression, such as validating a 
     * primitive number or using your own custom validation expression.</p>
     *
     * <pre>
     * Validate.isTrue(i > 0);
     * Validate.isTrue(myObject.isOk());</pre>
     *
     * <p>The message of the exception is &quot;The validated expression is 
     * false&quot;.</p>
     * 
     * @param expression the boolean expression to check 
     * @throws IllegalArgumentException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression) {
        if (expression == false) {
            throw new IllegalArgumentException("The validated expression is false");
        }
    }

    /**
     * <p>Validate that the specified argument is not <code>null</code>; 
     * otherwise throwing an exception.
     *
     * <pre>Validate.notNull(myObject);</pre>
     *
     * <p>The message of the exception is &quot;The validated object is 
     * null&quot;.</p>
     * 
     * @param object the object to check
     * @throws IllegalArgumentException if the object is <code>null</code>
     */
    public static void notNull(Object object) {
        if (object == null) {
		    throw new IllegalArgumentException("The validated object is null");
		}
    }

    /**
     * <p>Validate that the specified argument string is 
     * neither <code>null</code> nor a length of zero (no characters); 
     * otherwise throwing an exception with the specified message.
     *
     * <pre>Validate.notEmpty(myString);</pre>
     * 
     * <p>The message in the exception is &quot;The validated 
     * string is empty&quot;.</p>
     * 
     * @param string the string to check
     * @throws IllegalArgumentException if the string is empty
     */
    public static void notEmpty(String string) {
        if (string == null || string.length() == 0) {
		    throw new IllegalArgumentException("The validated string is empty");
		}
    }

}
