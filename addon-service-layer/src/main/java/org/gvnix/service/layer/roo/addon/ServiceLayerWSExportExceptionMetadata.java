/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.service.layer.roo.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebFault;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class ServiceLayerWSExportExceptionMetadata extends
	AbstractItdTypeDetailsProvidingMetadataItem {

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWSExportExceptionMetadata.class.getName());

    private static final String EXCEPTION_WEB_FAULT_TYPE_STRING = ServiceLayerWSExportExceptionMetadata.class
	    .getName();
    private static final String EXCEPTION_WEB_FAULT_TYPE = MetadataIdentificationUtils
	    .create(EXCEPTION_WEB_FAULT_TYPE_STRING);

    public ServiceLayerWSExportExceptionMetadata(String identifier,
	    JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata) {
	super(identifier, aspectName, governorPhysicalTypeMetadata);
	// TODO Auto-generated constructor stub

	Assert.isTrue(isValid(identifier), "Metadata identification string '"
		+ identifier + "' does not appear to be a valid");

	if (!isValid()) {
	    return;
	}

	// Create the metadata.
	AnnotationMetadata annotationMetadata = MemberFindingUtils
		.getTypeAnnotation(governorTypeDetails, new JavaType(
			GvNIXWebFault.class.getName()));

	if (annotationMetadata != null) {

	    // Add @javax.jws.WebFault annotation to ITD.
	    AnnotationMetadata webFaultAnnotationMetadata = getTypeAnnotation(annotationMetadata);
	    if (webFaultAnnotationMetadata!= null) {
		builder.addTypeAnnotation(webFaultAnnotationMetadata);
	    }

	}

	// Create a representation of the desired output ITD
	itdTypeDetails = builder.build();

    }

    /**
     * Create @WebFault annotation with @GvNIXWebFault attribute values.
     * 
     * @param annotationMetadata
     *            to retrieve attributes.
     * @return WebFault annotation to define.
     */
    public AnnotationMetadata getTypeAnnotation(
	    AnnotationMetadata annotationMetadata) {

	JavaType javaType = new JavaType("javax.xml.ws.WebFault");

	if (isAnnotationIntroduced("javax.xml.ws.WebFault")) {

	    List<AnnotationAttributeValue<?>> annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

	    StringAttributeValue nameAttributeValue = (StringAttributeValue) annotationMetadata
		    .getAttribute(new JavaSymbolName("name"));

	    if (nameAttributeValue == null) {
		logger.log(Level.WARNING,
			"Attribute 'name' in annotation @GvNIXWebFault from class '"
				+ governorPhysicalTypeMetadata
					.getPhysicalTypeDetails().getName()
					.getFullyQualifiedTypeName()
				+ "' is not defined.");
		return null;
	    }

	    annotationAttributeValueList.add(nameAttributeValue);

	    StringAttributeValue targetNamespaceAttributeValue = (StringAttributeValue) annotationMetadata
		    .getAttribute(new JavaSymbolName("targetNamespace"));

	    if (targetNamespaceAttributeValue == null) {
		logger.log(Level.WARNING,
			"Attribute 'targetNamespace' in annotation @GvNIXWebFault from class '"
				+ governorPhysicalTypeMetadata
					.getPhysicalTypeDetails().getName()
					.getFullyQualifiedTypeName()
				+ "' is not defined.");

		return null;
	    } else if (!StringUtils.startsWithIgnoreCase(
		    targetNamespaceAttributeValue.getValue(), "http://")) {

		logger
			.log(
				Level.WARNING,
				"Attribute 'targetNamespace' for annotation @GvNIXWebFault in class '"
			+ governorPhysicalTypeMetadata.getPhysicalTypeDetails()
				.getName().getFullyQualifiedTypeName()
			+ "' not correctly defined. It must have URI format.");
		return null;
	    }

	    annotationAttributeValueList.add(targetNamespaceAttributeValue);

	    StringAttributeValue faultBeanAttributeValue = (StringAttributeValue) annotationMetadata
		    .getAttribute(new JavaSymbolName("faultBean"));

	    if (faultBeanAttributeValue == null) {
		logger.log(Level.WARNING,
			"Attribute 'faultBean' in annotation @GvNIXWebFault from class '"
				+ governorPhysicalTypeMetadata
					.getPhysicalTypeDetails().getName()
					.getFullyQualifiedTypeName()
				+ "' is not defined.");
		return null;
	    } else if (!governorPhysicalTypeMetadata.getPhysicalTypeDetails()
		    .getName().getFullyQualifiedTypeName().contentEquals(
			    faultBeanAttributeValue.getValue())) {

		logger
			.log(
				Level.WARNING,
				"Attribute 'faultBean' for annotation @GvNIXWebFault in class '"
			+ governorPhysicalTypeMetadata.getPhysicalTypeDetails()
				.getName().getFullyQualifiedTypeName()
			+ "' not correctly defined. It must be java absolute name (i.e. 'java.lang.String').");
		return null;
	    }

	    annotationAttributeValueList.add(faultBeanAttributeValue);

	    return new DefaultAnnotationMetadata(javaType,
		    annotationAttributeValueList);
	}

	return MemberFindingUtils.getDeclaredTypeAnnotation(
		governorTypeDetails, javaType);

    }

    /**
     * Indicates whether the annotation will be introduced via this ITD.
     * 
     * @param annotation
     *            to be check if exists.
     * 
     * @return true if it will be introduced, false otherwise
     */
    public boolean isAnnotationIntroduced(String annotation) {
	JavaType javaType = new JavaType(annotation);
	AnnotationMetadata result = MemberFindingUtils
		.getDeclaredTypeAnnotation(governorTypeDetails, javaType);

	return result == null;
    }

    public static String getMetadataIdentiferType() {
	return EXCEPTION_WEB_FAULT_TYPE;
    }

    public static boolean isValid(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.isValid(
		EXCEPTION_WEB_FAULT_TYPE_STRING, metadataIdentificationString);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getJavaType(
		EXCEPTION_WEB_FAULT_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getPath(
		EXCEPTION_WEB_FAULT_TYPE_STRING, metadataIdentificationString);
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
	return PhysicalTypeIdentifierNamingUtils.createIdentifier(
		EXCEPTION_WEB_FAULT_TYPE_STRING, javaType, path);
    }

}