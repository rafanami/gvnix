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

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.ws.export.WSExportWsdlConfigService.GvNIXAnnotationType;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;

/**
 * Listen files generated by wsdl2java maven plugin and create related sources.
 * 
 * <p>
 * This listener is monitoring target folder
 * {@link WSExportWsdlListener#GENERATED_CXF_SOURCES_DIR} files and include this
 * files for generation into project sources folder. On listener activation, the
 * files for generation are reset.
 * </p>
 * 
 * <p>
 * Project target files are generated by wsdl2java maven plugin executed by
 * {@link WSExportWsdlOperations#exportWsdl(String)}.
 * </p>
 * 
 * @see WSExportWsdlListener#onFileEvent(FileEvent)
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Mario Martínez Sánchez at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class WSExportWsdlListener implements FileEventListener {

    static final String webService = "javax.jws.WebService";
    static final String webServiceInterface = "WebService";
    static final String soapBinding = "SOAPBinding";
    static final String xmlRootElement = "XmlRootElement";
    static final String xmlAccessorType = "XmlAccessorType";
    static final String xmlType = "XmlType";
    static final String xmlEnum = "XmlEnum";
    static final String webFault = "WebFault";
    static final String PACKAGE_INFO_FILE = "package-info.java";

    /** Sources directory generated by wsdl2java maven plugin into target folder */
    public static final String GENERATED_CXF_SOURCES_DIR = "target/generated-sources/cxf/server/";

    private String genSourcesDir;

    protected static Logger logger = Logger
            .getLogger(WSExportWsdlListener.class.getName());

    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private WSExportWsdlConfigService wSExportWsdlConfigService;

    /**
     * Reset files list to generate.
     * 
     * @param context
     *            Context
     */
    protected void activate(ComponentContext context) {

        // Reset files lists for generation
        wSExportWsdlConfigService.resetGeneratedFilesList();
    }

    /**
     * {@inheritDoc}
     * 
     * Include target file for src generation when is valid.
     * 
     * <p>
     * Consider event only when:
     * </p>
     * 
     * <ul>
     * <li>Project available</li>
     * <li>Event file is not a directory and included on generated sources dir</li>
     * <li>Monitor starts, file created or file updated</li>
     * </ul>
     * 
     * <p>
     * Include file for generation when is a package info file (special
     * generated java file). Or check file first java type first valid
     * annotation type and name if type is a class, interface or enumeration:
     * </p>
     * 
     * <p>
     * Only files with valid annotations will be included
     * {@link WSExportWsdlListener#includeFileValidAnnot(File, AnnotationExpr, TypeDeclaration)}
     * </p>
     * 
     * @param event
     *            Event
     **/
    public void onFileEvent(FileEvent event) {

        // Get event file
        File file = event.getFileDetails().getFile();

        // Nothing to do when project hasn't been created
        if (!projectOperations.isProjectAvailable()) {
            return;
        }

        // Is not a directory and is included on generated sources dir
        if (file.getAbsolutePath().contains(getGenSourcesDir())
                && !file.isDirectory()) {

            // Only on start monitoring or create file or update file
            FileOperation op = event.getOperation();
            if ((op.compareTo(FileOperation.MONITORING_START) == 0)
                    || (op.compareTo(FileOperation.CREATED) == 0)
                    || (op.compareTo(FileOperation.UPDATED) == 0)) {

                // When is a package info file (special generated java file)
                if (file.getName().contentEquals(PACKAGE_INFO_FILE)) {

                    wSExportWsdlConfigService.setSchemaPackageInfoFile(file);
                    return;
                }

                try {

                    // Get java types from java file
                    List<TypeDeclaration> types = JavaParser.parse(file)
                            .getTypes();
                    if (types != null) {

                        // Get fist java type
                        TypeDeclaration type = types.get(0);

                        // If java type is a class, interface or enumeration
                        if ((type instanceof ClassOrInterfaceDeclaration)
                                || (type instanceof EnumDeclaration)) {

                            logger.fine(event.getFileDetails().getFile()
                                    .getAbsolutePath());

                            // First valid annot type and name include generate
                            for (AnnotationExpr annot : type.getAnnotations()) {
                                if (includeFileValidAnnot(file, annot, type)) {
                                    break;
                                }
                            }
                        }
                    }
                } catch (ParseException e) {
                    throw new IllegalStateException(
                            "Generated web service java file has errors:\n"
                                    + e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IllegalStateException(
                            "Generated web service java file has errors:\n"
                                    + e.getMessage());
                }
            }
        }
    }

    /**
     * When annotation has a valid class and name, include file for generation.
     * 
     * <ul>
     * <li>Xml root element or xml accessor type: include file for xml element
     * generation</li>
     * <li>Web fault: include file for web fault generation</li>
     * <li>Web service: include file for web service generation
     * <li>Web service interface: include file for web service interface
     * generation</li>
     * <li>Xml enum: include file for xml element generation</li>
     * </ul>
     * 
     * <p>
     * Only files with valid annotations names will be included
     * {@link WSExportWsdlListener#includeFileValidAnnotName(File, TypeDeclaration, String)}
     * </p>
     * 
     * @param file
     *            Generated file
     * @param annot
     *            File type annotation
     * @param type
     *            File type
     * @return Is valid annotation class and name included for generation ?
     */
    protected boolean includeFileValidAnnot(File file, AnnotationExpr annot,
            TypeDeclaration type) {

        if (annot instanceof NormalAnnotationExpr) {

            return includeFileValidAnnotName(file, type,
                    ((NormalAnnotationExpr) annot).getName().getName());

        } else if (annot instanceof MarkerAnnotationExpr) {

            return includeFileValidAnnotName(file, type,
                    ((MarkerAnnotationExpr) annot).getName().getName());

        } else if (annot instanceof SingleMemberAnnotationExpr) {

            return includeFileValidAnnotName(file, type,
                    ((SingleMemberAnnotationExpr) annot).getName().getName());
        }

        return false;
    }

    /**
     * When annotation has a valid name, include file for generation.
     * 
     * <ul>
     * <li>Xml root element or xml accessor type: include file for xml element
     * generation</li>
     * <li>Web fault: include file for web fault generation</li>
     * <li>Web service: include file for web service generation</li>
     * <li>Web service interface: include file for web service interface
     * generation</li>
     * <li>Xml enum: include file for xml element generation</li>
     * </ul>
     * 
     * @param file
     *            Generated file
     * @param annot
     *            File type annotation
     * @param name
     *            Annotation name
     * @return Is valid annotation name included for generation ?
     */
    private boolean includeFileValidAnnotName(File file, TypeDeclaration type,
            String name) {

        if (name.equals(xmlRootElement) || name.equals(xmlAccessorType)) {

            wSExportWsdlConfigService.addFileToUpdateAnnotation(file,
                    GvNIXAnnotationType.XML_ELEMENT);
            return true;

        } else if (name.equals(webFault)) {

            wSExportWsdlConfigService.addFileToUpdateAnnotation(file,
                    GvNIXAnnotationType.WEB_FAULT);
            return true;

        } else if (name.equals(webService)) {

            wSExportWsdlConfigService.addFileToUpdateAnnotation(file,
                    GvNIXAnnotationType.WEB_SERVICE);
            return true;

        } else if (name.equals(webServiceInterface)) {

            wSExportWsdlConfigService.addFileToUpdateAnnotation(file,
                    GvNIXAnnotationType.WEB_SERVICE_INTERFACE);
            return true;

        } else if (name.equals(xmlEnum)) {

            wSExportWsdlConfigService.addFileToUpdateAnnotation(file,
                    GvNIXAnnotationType.XML_ELEMENT);
            return true;
        }

        return false;
    }

    /**
     * Utility to get {@link #genSourcesDir}.
     * 
     * <p>
     * Use this method to ensure it is initialized.
     * </p>
     * 
     * @return Generated sources directory.
     */
    private String getGenSourcesDir() {

        if (genSourcesDir != null) {
            return genSourcesDir;
        }

        genSourcesDir = projectOperations.getPathResolver().getIdentifier(
                Path.ROOT, GENERATED_CXF_SOURCES_DIR);

        return genSourcesDir;
    }

}
