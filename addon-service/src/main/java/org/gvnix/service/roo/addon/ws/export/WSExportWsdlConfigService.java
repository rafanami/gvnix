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
package org.gvnix.service.roo.addon.ws.export;

import japa.parser.ParseException;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.gvnix.service.roo.addon.annotations.GvNIXWebMethod;
import org.gvnix.service.roo.addon.annotations.GvNIXWebService;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface WSExportWsdlConfigService {

    /**
     * <ul>
     * <li>XML_ELEMENT: Associated with @GvNIXXmlElement annotation.</li>
     * <li>WEB_FAULT: Associated with @GvNIXWebFault annotation.</li>
     * <li>WEB_SERVICE: Associated with @GvNIXWebService annotation.</li>
     * </ul>
     */
    public enum GvNIXAnnotationType {

        /**
         * Web Service Xml Annotation type.
         */
        XML_ELEMENT,

        /**
         * Web Fault Web Service Annotation type.
         */
        WEB_FAULT,

        /**
         * Web Service Annotation type.
         */
        WEB_SERVICE,

        /**
         * Web Service Annotation type in interface file.
         */
        WEB_SERVICE_INTERFACE
    };

    /**
     * Generate java source code related to a WSDL with maven plugin.
     * 
     * <p>
     * Check correct WSDL format, configure plugin to generate sources and
     * generate java sources.
     * </p>
     * 
     * @param wsdlLocation
     *            contract wsdl url to export
     */
    public void generateJavaFromWsdl(String wsdlLocation);

    /**
     * <p>
     * Adds CXF Web Service generated sources directory to Roo FileSystem
     * monitor service.
     * </p>
     * <p>
     * This makes able {@link WSExportWsdlListener} to collect java file
     * creation events from wsdl2java maven plugin
     * </p>
     * 
     * @param directoryToMonitoring
     *            directory to look up for CXF Web Service generated java files.
     */
    public void monitorFolder(String directoryToMonitoring);

    /**
     * Create gvNIX web service from wsdl2java plugin generation monitoring.
     * 
     * <p>
     * Creates java files in 'src/main/java' from result of wsdl2java plugin
     * generation monitoring and creating required gvNIX annotation to identify
     * as a Web Service.
     * </p>
     * <p>
     * This files will be monitoring by {@link WSExportWsdlListener}.<br/>
     * Create files from each monitored {@link File} lists:
     * <ul>
     * <li>gvNIX xml element list</li>
     * <li>gvNIX web fault list</li>
     * <li>gvNIX web service list</li>
     * </ul>
     * </p>
     * 
     * @return implementation classes
     */
    public List<JavaType> createGvNixClasses();

    /**
     * <p>
     * Creates web service fail java files in 'src/main/java' from result of
     * wsdl2java plugin. In addition, adds @GvNIXWebService annotation.
     * </p>
     * <p>
     * This files will be registered by {@link WSExportWsdlListener}
     * </p>
     * 
     * TODO to be removed from interface. This method need a sequence of
     * operations to be useful.
     */
    public void createGvNixWebFaultClasses();

    /**
     * <p>
     * Creates web service java files in 'src/main/java' from result of
     * wsdl2java plugin. In addition, adds @GvNIXWebService annotation.
     * </p>
     * <p>
     * This files will be registered by {@link WSExportWsdlListener}
     * </p>
     * 
     * TODO to be removed from interface. This method need a sequence of
     * operations to be useful.
     * 
     * @return implementation class list
     */
    public List<JavaType> createGvNixWebServiceClasses();

    /**
     * Retrieve Web Service implemented interface from interface list.
     * 
     * TODO to be removed?. This method could be useless outside this service.
     * 
     * @param classOrInterfaceDeclaration
     *            to retrieve its implemented interface.
     * @return {@link ClassOrInterfaceDeclaration} interface that implements
     *         classOrInterfaceDeclaratio, null if not exists.
     * @throws IOException
     * @throws ParseException
     */
    public ClassOrInterfaceDeclaration getWebServiceInterface(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration)
            throws ParseException, IOException;

    /**
     * <p>
     * Convert field with @XmlElement annotation to field with @GvNIXXmlElementField
     * with its values.
     * </p>
     * <p>
     * Creates {@link FieldMetadata} with @GvNIXXmlElementField annotation even
     * if not exists @XmlElement.
     * </p>
     * 
     * TODO to be removed from interface?. This method could be useless outside
     * this service.
     * 
     * @param fieldMetadata
     * @return {@link FieldMetadata} with @GvNIXXmlElementField annotation.
     */
    public FieldMetadata getGvNIXXmlElementFieldAnnotation(
            FieldMetadata fieldMetadata);

    /**
     * Convert annotation @WebFault values from
     * {@link ClassOrInterfaceDeclaration} to {@link GvNIXWebFault}.
     * 
     * TODO to be removed from interface?. This method could be useless outside
     * this service.
     * 
     * @param classOrInterfaceDeclaration
     *            to retrieve values from @WebFault annotations and convert to
     *            {@link GvNIXWebFault} values.
     * @param exceptionType
     *            to retrieve faultBean attribute value {@link GvNIXWebFault}.
     * @return {@link GvNIXWebFault} to define in class.
     */
    public AnnotationMetadata getGvNIXWebFaultAnnotation(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
            JavaType exceptionType);

    /**
     * Convert annotation @WebService values from
     * {@link ClassOrInterfaceDeclaration} to {@link GvNIXWebService}.
     * 
     * TODO to be removed from interface?. This method could be useless outside
     * this service.
     * 
     * @param classOrInterfaceDeclaration
     *            to retrieve values from @WebService annotations and convert to
     *            {@link GvNIXWebService} values.
     * @param implementedInterface
     *            Web Service interface.
     * @param javaType
     *            to retrieve mandatory Annotation attributed with its values.
     * @return {@link GvNIXWebService} to define in class.
     */
    public AnnotationMetadata getGvNIXWebServiceAnnotation(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
            ClassOrInterfaceDeclaration implementedInterface, JavaType javaType);

    /**
     * Creates MethodMetadata with GvNIX Annotation values to export as Web
     * Service Operation.
     * 
     * TODO to be removed from interface?. This method could be useless outside
     * this service.
     * 
     * @param methodMetadata
     * @param defaultNamespace
     *            from Web Service to set where is not defined in annotations.
     * @return {@link MethodMetadata} with GvNIX Annotations.
     */
    public MethodMetadata getGvNIXWebMethodMetadata(
            MethodMetadata methodMetadata, String defaultNamespace);

    /**
     * Define @GvNIXWebMethod annotation using Web Services annotation attribute
     * values.
     * 
     * TODO to be removed from interface?. This method could be useless outside
     * this service.
     * 
     * @param methodMetadata
     *            to check Web Services annotations.
     * @param defaultNamespace
     *            from Web Service to set where is not defined in annotations.
     * @return {@link GvNIXWebMethod} with Attributes.
     */
    public AnnotationMetadata getGvNIXWebMethodAnnotation(
            MethodMetadata methodMetadata, String defaultNamespace);

    /**
     * Define @GVNIXWebParam for each input method parameter.
     * 
     * @param methodMetadata
     *            to check for Web Services annotations.
     * @param defaultNamespace
     *            from Web Service to set where is not defined in annotations.
     * @return {@link List} of {@link AnnotatedJavaType} for each input method
     *         parameter.
     */
    public List<AnnotatedJavaType> getGvNIXWebParamsAnnotations(
            MethodMetadata methodMetadata, String defaultNamespace);

    /**
     * Updates list of files generated to update with '@GvNIX' annotations.
     * 
     * <b>For {@link WSExportWsdlListener} uses only</b>
     * 
     * @param file
     *            scanned to add to list.
     * @param gvNIXAnnotationType
     *            to select the list where add the file.
     */
    public void addFileToUpdateAnnotation(File file,
            GvNIXAnnotationType gvNIXAnnotationType);

    /**
     * Reset Web Service generated file lists.
     * 
     * <b>For {@link WSExportWsdlListener} uses only</b>
     */
    public void resetGeneratedFilesList();

    /**
     * Updates package info file location.
     * 
     * <b>For {@link WSExportWsdlListener} uses only</b>
     * 
     * @param schemaPackageInfoFile
     *            File absolute path.
     */
    public void setSchemaPackageInfoFile(File schemaPackageInfoFile);

}
