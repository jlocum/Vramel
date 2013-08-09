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
package com.nxttxn.vramel.util;


import com.nxttxn.vramel.Exchange;
import com.nxttxn.vramel.Expression;
import com.nxttxn.vramel.Predicate;

public final class ExpressionToPredicateAdapter implements Predicate {
    private final Expression expression;

    public ExpressionToPredicateAdapter(Expression expression) {
        this.expression = expression;
    }

    public boolean matches(Exchange exchange) {
        if (expression instanceof Predicate) {
            return ((Predicate) expression).matches(exchange);
        } else {
            Object value = expression.evaluate(exchange, Object.class);
            return ObjectHelper.evaluateValuePredicate(value);
        }
    }

    @Override
    public String toString() {
        return expression.toString();
    }

    /**
     * Converts the given expression into an {@link Predicate}
     */
    public static Predicate toPredicate(final Expression expression) {
        return new ExpressionToPredicateAdapter(expression);
    }

}