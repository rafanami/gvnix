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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebService;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.beaninfo.BeanInfoMetadata;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

/**
 * <p>
 * Checks if @GvNIXWebServices annotated classes have been updated and this
 * affects to Service Contract WSDL.
 * </p>
 * 
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWSExportMetadataProvider extends AbstractItdMetadataProvider {

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWSExportMetadataProvider.class.getName());

    protected void activate(ComponentContext context) {
	// Ensure we're notified of all metadata related to physical Java types,
	// in particular their initial creation
	metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
		.getMetadataIdentiferType(), getProvidesType());
	addMetadataTrigger(new JavaType(GvNIXWebService.class.getName()));
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#createLocalIdentifier(org.springframework.roo.model.JavaType, org.springframework.roo.project.Path)
     */
    protected String createLocalIdentifier(JavaType javaType, Path path) {
	return ServiceLayerWSExportMetadata.createIdentifier(javaType, path);
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getGovernorPhysicalTypeIdentifier(java.lang.String)
     */
    protected String getGovernorPhysicalTypeIdentifier(
	    String metadataIdentificationString) {
	JavaType javaType = ServiceLayerWSExportMetadata
		.getJavaType(metadataIdentificationString);
	Path path = ServiceLayerWSExportMetadata
		.getPath(metadataIdentificationString);
	String physicalTypeIdentifier = PhysicalTypeIdentifier
		.createIdentifier(javaType, path);
	return physicalTypeIdentifier;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getMetadata(java.lang.String, org.springframework.roo.model.JavaType, org.springframework.roo.classpath.PhysicalTypeMetadata, java.lang.String)
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
	    String metadataIdentificationString, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata,
	    String itdFilename) {

	ServiceLayerWSExportMetadata serviceLayerMetadata = new ServiceLayerWSExportMetadata(
		metadataIdentificationString, aspectName,
		governorPhysicalTypeMetadata);

	return serviceLayerMetadata;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.ItdMetadataProvider#getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {
	return "GvNix_WebService";
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {
	return ServiceLayerWSExportMetadata.getMetadataIdentiferType();
    }

}