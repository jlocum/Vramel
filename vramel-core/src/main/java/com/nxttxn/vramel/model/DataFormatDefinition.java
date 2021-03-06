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
package com.nxttxn.vramel.model;

import com.nxttxn.vramel.spi.DataFormat;
import com.nxttxn.vramel.spi.FlowContext;
import com.nxttxn.vramel.util.IntrospectionSupport;
import com.nxttxn.vramel.util.ObjectHelper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * Represents the base XML type for DataFormat.
 *
 * @version
 */
@XmlType(name = "dataFormat")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataFormatDefinition extends IdentifiedType {
    @XmlTransient
    private DataFormat dataFormat;
    @XmlTransient
    private String dataFormatName;

    public DataFormatDefinition() {
    }

    public DataFormatDefinition(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    protected DataFormatDefinition(String dataFormatName) {
        this.dataFormatName = dataFormatName;
    }

    /**
     * Factory method to create the data format
     *
     * @param flowContext route context
     * @param type         the data format type
     * @param ref          reference to lookup for a data format
     * @return the data format or null if not possible to create
     */
    public static DataFormat getDataFormat(FlowContext flowContext, DataFormatDefinition type, String ref) {
        if (type == null) {
            ObjectHelper.notNull(ref, "ref or type");

            // try to let resolver see if it can resolve it, its not always possible
            type = ((ModelVramelContext)flowContext.getVramelContext()).resolveDataFormatDefinition(ref);

            if (type != null) {
                return type.getDataFormat(flowContext);
            }

            DataFormat dataFormat = flowContext.getVramelContext().resolveDataFormat(ref);
            if (dataFormat == null) {
                throw new IllegalArgumentException("Cannot find data format in registry with ref: " + ref);
            }

            return dataFormat;
        } else {
            return type.getDataFormat(flowContext);
        }
    }

    public DataFormat getDataFormat(FlowContext flowContext) {
        if (dataFormat == null) {
            dataFormat = createDataFormat(flowContext);
            if (dataFormat != null) {
                configureDataFormat(dataFormat);
            } else {
                throw new IllegalArgumentException(
                        "Data format '" + (dataFormatName != null ? dataFormatName : "<null>") + "' could not be created. "
                                + "Ensure that the data format is valid and the associated Camel component is present on the classpath");
            }
        }
        return dataFormat;
    }

    /**
     * Factory method to create the data format instance
     */
    protected DataFormat createDataFormat(FlowContext flowContext) {
        if (dataFormatName != null) {
            return flowContext.getVramelContext().resolveDataFormat(dataFormatName);
        }
        return null;
    }

    /**
     * Allows derived classes to customize the data format
     */
    protected void configureDataFormat(DataFormat dataFormat) {
    }

    /**
     * Sets a named property on the data format instance using introspection
     */
    protected void setProperty(Object bean, String name, Object value) {
        try {
            IntrospectionSupport.setProperty(bean, name, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to set property: " + name + " on: " + bean + ". Reason: " + e, e);
        }
    }

    public String getDataFormatName() {
        return dataFormatName;
    }

    public void setDataFormatName(String dataFormatName) {
        this.dataFormatName = dataFormatName;
    }

    public DataFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getShortName() {
        String name = getClass().getSimpleName();
        if (name.endsWith("DataFormat")) {
            name = name.substring(0, name.indexOf("DataFormat"));
        }
        return name;
    }

}

