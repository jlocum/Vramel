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
package com.nxttxn.vramel.model.language;

import com.nxttxn.vramel.Expression;
import com.nxttxn.vramel.Predicate;
import com.nxttxn.vramel.VramelContext;
import com.nxttxn.vramel.builder.SimpleBuilder;
import com.nxttxn.vramel.util.ObjectHelper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * For expressions and predicates using the
 * <a href="http://camel.apache.org/simple.html">simple language</a>
 *
 * @version
 */
@XmlRootElement(name = "simple")
@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleExpression extends ExpressionDefinition {
    @XmlAttribute(name = "resultType")
    private String resultTypeName;
    @XmlTransient
    private Class<?> resultType;

    public SimpleExpression() {
    }

    public SimpleExpression(String expression) {
        super(expression);
    }

    public SimpleExpression(Expression expression) {
        super(expression);
    }

    public String getLanguage() {
        return "simple";
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

    public String getResultTypeName() {
        return resultTypeName;
    }

    public void setResultTypeName(String resultTypeName) {
        this.resultTypeName = resultTypeName;
    }

    @Override
    public Expression createExpression(VramelContext vramelContext) {
        if (resultType == null && resultTypeName != null) {
            try {
                resultType = vramelContext.getClassResolver().resolveMandatoryClass(resultTypeName);
            } catch (ClassNotFoundException e) {
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        }

        String exp = getExpression();
        if (isTrim() && exp != null) {
            exp = exp.trim();
        }

        SimpleBuilder answer = new SimpleBuilder(exp);
        answer.setResultType(resultType);
        return answer;
    }

    @Override
    public Predicate createPredicate(VramelContext vramelContext) {
        // SimpleBuilder is also a Predicate
        return (Predicate) createExpression(vramelContext);
    }
}
