//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.10.22 at 09:54:58 AM CEST 
//


package org.knime.ext.tableau.hyper.sendtable.api.binding;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for importSourceType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="importSourceType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ActiveDirectory"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "importSourceType")
@XmlEnum
public enum ImportSourceType {

    @XmlEnumValue("ActiveDirectory")
    ACTIVE_DIRECTORY("ActiveDirectory");
    private final String value;

    ImportSourceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ImportSourceType fromValue(String v) {
        for (ImportSourceType c: ImportSourceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
