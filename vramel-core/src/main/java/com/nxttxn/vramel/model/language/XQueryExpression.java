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
import com.nxttxn.vramel.util.ObjectHelper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;



/**
 * For XQuery expressions and predicates
 *
 * @version
 */
@XmlRootElement(name = "xquery")
@XmlAccessorType(XmlAccessType.FIELD)
public class XQueryExpression extends NamespaceAwareExpression {
    @XmlAttribute
    private String type;
    @XmlTransient
    private Class<?> resultType;
    @XmlAttribute
    private String headerName;

    public XQueryExpression() {
    }

    public XQueryExpression(String expression) {
        super(expression);
    }

    public String getLanguage() {
        return "xquery";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public Expression createExpression(VramelContext vramelContext) {
        if (resultType == null && type != null) {
            try {
                resultType = vramelContext.getClassResolver().resolveMandatoryClass(type);
            } catch (ClassNotFoundException e) {
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        }

        return super.createExpression(vramelContext);
    }

    @Override
    protected void configureExpression(VramelContext vramelContext, Expression expression) {
        super.configureExpression(vramelContext, expression);
        if (resultType != null) {
            setProperty(expression, "resultType", resultType);
        }
        if (ObjectHelper.isNotEmpty(getHeaderName())) {
            setProperty(expression, "headerName", getHeaderName());
        }
    }

    @Override
    protected void configurePredicate(VramelContext vramelContext, Predicate predicate) {
        super.configurePredicate(vramelContext, predicate);
        if (resultType != null) {
            setProperty(predicate, "resultType", resultType);
        }
        if (ObjectHelper.isNotEmpty(getHeaderName())) {
            setProperty(predicate, "headerName", getHeaderName());
        }
    }

}