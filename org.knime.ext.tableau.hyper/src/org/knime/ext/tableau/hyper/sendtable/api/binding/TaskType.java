//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.11.07 at 05:07:04 PM CET 
//


package org.knime.ext.tableau.hyper.sendtable.api.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for taskType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="taskType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="extractRefresh" type="{http://tableau.com/api}taskExtractRefreshType"/>
 *       &lt;/choice>
 *       &lt;attribute name="runNow" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taskType", propOrder = {
    "extractRefresh"
})
public class TaskType {

    protected TaskExtractRefreshType extractRefresh;
    @XmlAttribute(name = "runNow")
    protected Boolean runNow;

    /**
     * Gets the value of the extractRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link TaskExtractRefreshType }
     *     
     */
    public TaskExtractRefreshType getExtractRefresh() {
        return extractRefresh;
    }

    /**
     * Sets the value of the extractRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskExtractRefreshType }
     *     
     */
    public void setExtractRefresh(TaskExtractRefreshType value) {
        this.extractRefresh = value;
    }

    /**
     * Gets the value of the runNow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRunNow() {
        return runNow;
    }

    /**
     * Sets the value of the runNow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRunNow(Boolean value) {
        this.runNow = value;
    }

}
