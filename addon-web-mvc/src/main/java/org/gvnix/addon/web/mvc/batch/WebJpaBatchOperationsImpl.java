/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.web.mvc.batch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.JpaOperations;
import org.gvnix.addon.jpa.batch.GvNIXJpaBatch;
import org.gvnix.addon.jpa.batch.JpaBatchAnnotationValues;
import org.gvnix.addon.web.mvc.MvcOperations;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.ProjectOperations;

/**
 * Implementation of {@link WebJpaBatchOperations}
 * 
 * @since 1.1
 */
@Component
@Service
public class WebJpaBatchOperationsImpl implements WebJpaBatchOperations {

    private static final JavaType JPA_BATCH_ANNOTATION = new JavaType(
            GvNIXJpaBatch.class);

    private static final JavaType WEB_JPA_BATCH_ANNOTATION = new JavaType(
            GvNIXWebJpaBatch.class);

    private static final List<JavaType> JPA_BATCH_SERVICE_ANNOTATIONS = Arrays
            .asList(JPA_BATCH_ANNOTATION, SpringJavaType.SERVICE);

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private MvcOperations mvcOperations;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private TypeManagementService typeManagementService;

    /**
     * {@inheritDoc}
     * <p/>
     * Commands are available if Spring MVC, gvNIX JPA dependencies and gvNIX
     * MVC dependencies are installed
     */
    public boolean isCommandAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.MVC)
                && projectOperations
                        .isFeatureInstalledInFocusedModule(JpaOperations.FEATURE_NAME_GVNIX_JPA)
                && projectOperations
                        .isFeatureInstalledInFocusedModule(MvcOperations.FEATURE_NAME_GVNIX_MVC);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Setup is available if Spring MVC and gvNIX JPA dependencies are installed
     * and gvNIX MVC dependencies have not been installed yet.
     * <p/>
     * Note if gvNIX MVC dependencies are installed there is no need to run
     * setup and it will be not available in
     */
    public boolean isSetupAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.MVC)
                && projectOperations
                        .isFeatureInstalledInFocusedModule(JpaOperations.FEATURE_NAME_GVNIX_JPA)
                && !projectOperations
                        .isFeatureInstalledInFocusedModule(MvcOperations.FEATURE_NAME_GVNIX_MVC);
    }

    public void setup() {
        // If gvNIX MVC dependencies are not installed, install them
        if (!projectOperations
                .isFeatureInstalledInFocusedModule(MvcOperations.FEATURE_NAME_GVNIX_MVC)) {
            mvcOperations.setup();
        }
    }

    /** {@inheritDoc} */
    public void addAll() {
        Set<JavaType> jpaBatchServices = new HashSet<JavaType>(
                getJpaBatchServices());
        for (JavaType controller : typeLocationService
                .findTypesWithAnnotation(RooJavaType.ROO_WEB_SCAFFOLD)) {
            ClassOrInterfaceTypeDetails controllerDetails = typeLocationService
                    .getTypeDetails(controller);

            // check for if there is jpa batch service
            JavaType entity = getFormBackingObject(controllerDetails);

            if (entity == null) {
                continue;
            }

            // look for jpaBatchSevice for entity
            JavaType service = findJpaServiceForEntity(entity, jpaBatchServices);
            if (service == null) {
                continue;
            }
            // remove service from list (this service is not needed any more)
            jpaBatchServices.remove(service);

            // Add annotations
            add(controller, service);
        }
    }

    private Set<JavaType> getJpaBatchServices() {
        return typeLocationService
                .findTypesWithAnnotation(JPA_BATCH_SERVICE_ANNOTATIONS);
    }

    /**
     * Find the Spring service which entity match with required
     * 
     * @param entity
     * @param jpaBatchServices list of class annotated with
     *        {@link #JPA_BATCH_SERVICE_ANNOTATIONS}
     * @return
     */
    private JavaType findJpaServiceForEntity(JavaType entity,
            Set<JavaType> jpaBatchServices) {
        JavaType serviceEntity;
        for (JavaType service : jpaBatchServices) {
            serviceEntity = getJpaBatchEntity(service);
            if (ObjectUtils.equals(serviceEntity, entity)) {
                return service;
            }
        }
        return null;
    }

    /**
     * Gets the entity value of {@link GvNIXJpaBatch}
     * 
     * @param service
     * @return
     */
    private JavaType getJpaBatchEntity(JavaType service) {
        ClassOrInterfaceTypeDetails serviceDatils = typeLocationService
                .getTypeDetails(service);
        JpaBatchAnnotationValues jpaBatchValues = new JpaBatchAnnotationValues(
                serviceDatils);
        return jpaBatchValues.getEntity();
    }

    /**
     * Gets the formBackingObject value of a {@link RooWebScaffold} annotation.
     * 
     * @param controllerDetails
     * @return
     */
    private JavaType getFormBackingObject(
            ClassOrInterfaceTypeDetails controllerDetails) {
        WebScaffoldAnnotationValues annotationValues = new WebScaffoldAnnotationValues(
                controllerDetails);
        return annotationValues.getFormBackingObject();
    }

    /**
     * Gets the formBackingObject value of a {@link RooWebScaffold} annotation.
     * 
     * @param controllerDetails
     * @return
     */
    private JavaType getFormBackingObject(JavaType controller) {
        ClassOrInterfaceTypeDetails controllerDetails = typeLocationService
                .getTypeDetails(controller);
        return getFormBackingObject(controllerDetails);
    }

    /** {@inheritDoc} */
    @Override
    public void add(JavaType controller, JavaType service) {
        Validate.notNull(controller, "Controller required");
        if (service != null) {
            annotateController(controller, service);
            return;
        }

        // check for if there is jpa batch service suitable
        JavaType entity = getFormBackingObject(controller);
        service = findJpaServiceForEntity(entity, getJpaBatchServices());
        Validate.notNull(service,
                "No spring service suitable found for Controller: %s",
                controller);

        annotateController(controller, service);
    }

    private void annotateController(JavaType controller, JavaType service) {
        Validate.notNull(controller, "Controller required");
        Validate.notNull(service, "Service required");

        ClassOrInterfaceTypeDetails existing = typeLocationService
                .getTypeDetails(controller);

        // Get controller annotation
        JavaType entity = getFormBackingObject(existing);
        Validate.notNull(entity, "Operation only supported for controllers");

        final boolean isAlreadyAnnotated = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        WEB_JPA_BATCH_ANNOTATION) != null;

        // Test if the annotation already exists on the target type
        if (!isAlreadyAnnotated) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    existing);

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    WEB_JPA_BATCH_ANNOTATION);

            annotationBuilder.addClassAttribute("service", service);

            // Add annotation to target type
            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder
                    .build());

            // Save changes to disk
            typeManagementService
                    .createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder
                            .build());
        }
    }
}