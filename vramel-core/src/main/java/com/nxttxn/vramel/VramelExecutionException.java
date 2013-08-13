/**
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
package com.nxttxn.vramel;

/**
 * Exception occurred during execution/processing of an {@link Exchange}.
 * <p/>
 * Is usually thrown to the caller when using the {@link org.apache.camel.ProducerTemplate}
 * to send messages to Camel.
 *
 * @version
 */
public class VramelExecutionException extends RuntimeExchangeException {

    public VramelExecutionException(String message, Exchange exchange) {
        super(message, exchange);
    }

    public VramelExecutionException(String message, Exchange exchange, Throwable cause) {
        super(message, exchange, cause);
    }
}