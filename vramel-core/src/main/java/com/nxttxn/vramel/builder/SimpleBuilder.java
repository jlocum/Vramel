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
package com.nxttxn.vramel.builder;


import com.nxttxn.vramel.Exchange;
import com.nxttxn.vramel.Expression;
import com.nxttxn.vramel.Predicate;
import com.nxttxn.vramel.language.simple.SimpleLanguage;


/**
 * Creates an {@link org.apache.camel.language.Simple} language builder.
 * <p/>
 * This builder is available in the Java DSL from the {@link FlowBuilder} which means that using
 * simple language for {@link Expression}s or {@link Predicate}s is very easy with the help of this builder.
 *
 * @version
 */
public class SimpleBuilder implements Predicate, Expression {

    private final String text;
    private Class<?> resultType;
    // cache the expression/predicate
    private volatile Expression expression;
    private volatile Predicate predicate;

    public SimpleBuilder(String text) {
        this.text = text;
    }

    public static SimpleBuilder simple(String text) {
        return new SimpleBuilder(text);
    }

    public static SimpleBuilder simple(String text, Class<?> resultType) {
        SimpleBuilder answer = simple(text);
        answer.setResultType(resultType);
        return answer;
    }

    public String getText() {
        return text;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

    public SimpleBuilder resultType(Class<?> resultType) {
        setResultType(resultType);
        return this;
    }

    public boolean matches(Exchange exchange) {
        if (predicate == null) {
            predicate = exchange.getContext().resolveLanguage("simple").createPredicate(text);
        }
        return predicate.matches(exchange);
    }

    public <T> T evaluate(Exchange exchange, Class<T> type) {
        if (expression == null) {
            expression = createExpression(exchange);
        }
        return expression.evaluate(exchange, type);
    }

    private Expression createExpression(Exchange exchange) {
        SimpleLanguage simple = (SimpleLanguage) exchange.getContext().resolveLanguage("simple");
        if (resultType != null) {
            simple.setResultType(resultType);
        }
        return simple.createExpression(text);
    }

    public String toString() {
        return "Simple: " + text;
    }
}
