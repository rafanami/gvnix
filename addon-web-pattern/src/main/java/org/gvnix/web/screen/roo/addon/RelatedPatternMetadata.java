/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.screen.roo.addon;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.itd.ItdSourceFileComposer;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * This type produces metadata for a new ITD. It uses an
 * {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in
 * the ITD and a new method.
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public class RelatedPatternMetadata extends AbstractPatternMetadata {

    private static final String PROVIDES_TYPE_STRING = RelatedPatternMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);
    
    private MemberDetails entityDetails;
    private MemberDetails masterEntityDetails;
    private Set<String> relationsFields;

    public RelatedPatternMetadata(String mid, JavaType aspect, PhysicalTypeMetadata controllerMetadata, MemberDetails controllerDetails,
    		WebScaffoldMetadata webScaffoldMetadata, List<StringAttributeValue> patterns, PhysicalTypeMetadata entityMetadata, MemberDetails entityDetails,
    		JavaTypeMetadataDetails masterEntityJavaDetails, MemberDetails masterEntityDetails, Set<String> relationsFields,
    		SortedMap<JavaType, JavaTypeMetadataDetails> relatedEntities, SortedMap<JavaType, JavaTypeMetadataDetails> relatedFields,
    		Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relatedDates, Map<JavaSymbolName, DateTimeFormatDetails> entityDateTypes) {
    	
        super(mid, aspect, controllerMetadata, controllerDetails, webScaffoldMetadata, patterns, entityMetadata, 
        		masterEntityJavaDetails, relatedEntities, relatedFields, relatedDates, entityDateTypes);

        if (!isValid()) {
        
        	// This metadata instance not be already produced at the time of instantiation (will retry)
            return;
        }
        
        this.entityDetails = entityDetails;
        this.masterEntityDetails = masterEntityDetails;
        this.relationsFields = relationsFields;
        
        List<String> tabularEditPatterns = getPatternTypeDefined(WebPatternType.tabular_edit_register, this.patterns);
        if (!tabularEditPatterns.isEmpty()) {
        	
            for (String tabularEditPattern : tabularEditPatterns) {
            	
            	// Method only exists when this is a detail pattern (has master entity)
            	builder.addMethod(getCreateFormMethod(tabularEditPattern));
            }
        }
        
        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
        new ItdSourceFileComposer(itdTypeDetails);
    }

	/**
	 * @see org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.getCreateFormMethod
	 * 
	 * @param patternName
	 * @return
	 */
	protected MethodMetadata getCreateFormMethod(String patternName) {
		
        Assert.notNull(masterEntity, "Master entity required to generate createForm");
        Assert.notNull(masterEntityTypeDetails, "Master entity metadata required to generate createForm");
		
		JavaSymbolName methodName = new JavaSymbolName("createForm" + patternName);
		
		// Create method params: annotation, type and name

		List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
		List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();

		// @RequestParam(value = "gvnixpattern", required = true) String gvnixpattern
		paramNames.add(new JavaSymbolName("gvnixpattern"));
		AnnotationMetadataBuilder gvnixpatternParamBuilder = new AnnotationMetadataBuilder(
				new JavaType("org.springframework.web.bind.annotation.RequestParam"));
		gvnixpatternParamBuilder.addStringAttribute("value", "gvnixpattern");
		gvnixpatternParamBuilder.addBooleanAttribute("required", true);
		List<AnnotationMetadata> gvnixpatternParam = new ArrayList<AnnotationMetadata>();
		gvnixpatternParam.add(gvnixpatternParamBuilder.build());
		paramTypes.add(new AnnotatedJavaType(new JavaType("java.lang.String"), gvnixpatternParam));

		// @RequestParam(value = "gvnixreference", required = true) MasterEntityIdType gvnixreference
		paramNames.add(new JavaSymbolName("gvnixreference"));
		AnnotationMetadataBuilder gvnixreferenceParamBuilder = new AnnotationMetadataBuilder(
				new JavaType("org.springframework.web.bind.annotation.RequestParam"));
		gvnixreferenceParamBuilder.addStringAttribute("value", "gvnixreference");
		gvnixreferenceParamBuilder.addBooleanAttribute("required", true);
		List<AnnotationMetadata> gvnixreferenceParam = new ArrayList<AnnotationMetadata>();
		gvnixreferenceParam.add(gvnixreferenceParamBuilder.build());
		paramTypes.add(new AnnotatedJavaType(
				new JavaType(masterEntityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getFullyQualifiedTypeName()), 
				gvnixreferenceParam));

		// Model uiModel
		paramNames.add(new JavaSymbolName("uiModel"));
		paramTypes.add(new AnnotatedJavaType(new JavaType("org.springframework.ui.Model"), null));

		// If method exists (in java file, by example) no create it in AspectJ file
		if (methodExists(methodName, paramTypes) != null) {
			return null;
		}
		
		// Create method annotation
		
		List<AnnotationMetadataBuilder> methodAnnotations = new ArrayList<AnnotationMetadataBuilder>();

		// @RequestMapping(params = { "form", "gvnixpattern=AplicacionListados2", "gvnixreference" }, method = RequestMethod.GET)
		List<StringAttributeValue> requestMapingAnnotationParams = new ArrayList<StringAttributeValue>();
		requestMapingAnnotationParams.add(new StringAttributeValue(new JavaSymbolName("value"), "form"));
		requestMapingAnnotationParams.add(new StringAttributeValue(new JavaSymbolName("value"), "gvnixpattern=" + patternName));
		requestMapingAnnotationParams.add(new StringAttributeValue(new JavaSymbolName("value"), "gvnixreference"));
		List<AnnotationAttributeValue<?>> requestMappingAnnotation = new ArrayList<AnnotationAttributeValue<?>>();
		requestMappingAnnotation.add(new ArrayAttributeValue<StringAttributeValue>(
				new JavaSymbolName("params"), requestMapingAnnotationParams));
		requestMappingAnnotation.add(new EnumAttributeValue(
				new JavaSymbolName("method"), 
				new EnumDetails(new JavaType("org.springframework.web.bind.annotation.RequestMethod"), new JavaSymbolName("GET"))));
		AnnotationMetadataBuilder requestMappingAnnotationBuilder = new AnnotationMetadataBuilder(
				new JavaType("org.springframework.web.bind.annotation.RequestMapping"), requestMappingAnnotation);
		methodAnnotations.add(requestMappingAnnotationBuilder);

		// Create method body
		
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

		/*
		 * MasterEntityName masterentityname = MasterEntityName.findMasterEntityName(gvnixreference);
		 * EntityName entityname = new EntityName();
		 * entityname.setMasterEntityName(masterentityname);
		 * uiModel.addAttribute("entityName", entityname);
		 * addDateTimeFormatPatterns(uiModel);  // Only if date types exists
		 * return "entitynames/create";
		 */
		String masterEntityName = masterEntity.getSimpleTypeName();
		String entityName = entity.getSimpleTypeName();

		
		// Get field from entity related with some master entity defined into the fields names list
		FieldMetadata relationField = getFieldRelationMasterEntity();
		// TODO Unify next 3 cases code
        if (relationField == null) {
        	
        	// TODO This case is already required ? 
        	
    		bodyBuilder.appendFormalLine(
    				masterEntity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + 
    				" " + masterEntityName.toLowerCase() + " = " + masterEntityName + "." + 
    						masterEntityTypeDetails.getPersistenceDetails().getFindMethod().getMethodName() + "(gvnixreference);");
    		bodyBuilder.appendFormalLine(entityName + " " + entityName.toLowerCase() + " = new " + 
    						entity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "();");
    		bodyBuilder.appendFormalLine(
    				entityName.toLowerCase() + ".set" + masterEntityName + "(" + masterEntityName.toLowerCase() + ");");
        }
        else if (entityTypeDetails.getPersistenceDetails().getRooIdentifierFields().contains(relationField)) {
        	
            // TODO When field metadata in composite roo identifier: use PK constructor
        	bodyBuilder.append(entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getNameIncludingTypeParameters(
        			false, builder.getImportRegistrationResolver()) + 
        			" " + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getSimpleTypeName().toLowerCase() +
        			" = new " + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getNameIncludingTypeParameters(
        					false, builder.getImportRegistrationResolver()) + "(");
        	Iterator<FieldMetadata> fields = entityTypeDetails.getPersistenceDetails().getRooIdentifierFields().iterator();
        	while (fields.hasNext()) {
        		FieldMetadata field = fields.next();
        		if (field.getFieldName().equals(relationField.getFieldName())) {
        			bodyBuilder.append("gvnixreference");	
        		}
        		else {
        			bodyBuilder.append("null");
        		}
        		if (fields.hasNext()) {
        			bodyBuilder.append(", ");
        		}
			}
        	bodyBuilder.append(");");
        	bodyBuilder.newLine(true);
    		bodyBuilder.appendFormalLine(entityName + " " + entityName.toLowerCase() + " = new " + 
					entity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "();");
    		bodyBuilder.appendFormalLine(entityName.toLowerCase() + ".set" + 
					entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldName().getSymbolNameCapitalisedFirstLetter() +
					"(" + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getSimpleTypeName().toLowerCase() + ");");
        }
        else {
        	
    		bodyBuilder.appendFormalLine(
    				masterEntity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + 
    				" " + masterEntityName.toLowerCase() + " = " + masterEntityName + "." + 
    						masterEntityTypeDetails.getPersistenceDetails().getFindMethod().getMethodName() + "(gvnixreference);");
    		bodyBuilder.appendFormalLine(entityName + " " + entityName.toLowerCase() + " = new " + 
    						entity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "();");
			bodyBuilder.appendFormalLine(
					entityName.toLowerCase() + ".set" + relationField.getFieldName().getSymbolNameCapitalisedFirstLetter() + "(" + masterEntityName.toLowerCase() + ");");
        }
		
		// Add attribute with identical name as required by Roo create page
		bodyBuilder.appendFormalLine(
				"uiModel.addAttribute(\"" + uncapitalize(entityName) + "\", " + entityName.toLowerCase() + ");");
		if (!entityDateTypes.isEmpty()) {
			
			bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
		}
		
		bodyBuilder.appendFormalLine("return \"" + webScaffoldMetadata.getAnnotationValues().getPath() + "/create\";");

		// TODO Remove dependencies or add it from entity pattern ?

		MethodMetadataBuilder method = new MethodMetadataBuilder(
				getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT, paramTypes, paramNames, bodyBuilder);
		method.setAnnotations(methodAnnotations);
		
		return method.build();
	}

	/**
	 * Get field from entity related with some master entity defined into the fields names list.
	 * 
	 * @param relationsFields Master fields names
	 * @param masterEntityDetails Master entity details
	 * @param entityTypeDetails Entity details
	 * @return Entity field related with master entity field
	 */
	protected FieldMetadata getFieldRelationMasterEntity() {
		
        FieldMetadata field = null;

		// Get field from master entity with OneToMany annotation and "mappedBy" attribute some value
		AnnotationAttributeValue<?> masterField = null;
        List<FieldMetadata> masterFields = MemberFindingUtils.getFields(masterEntityDetails);
        for (FieldMetadata tmpMasterField : masterFields) {
			
        	List<AnnotationMetadata> masterFieldAnnotations = tmpMasterField.getAnnotations();
        	for (AnnotationMetadata masterFieldAnnotation : masterFieldAnnotations) {
        		
        		// TODO May be more fields on relationsField var
				if (masterFieldAnnotation.getAnnotationType().equals(new JavaType("javax.persistence.OneToMany")) &&
						tmpMasterField.getFieldName().equals(new JavaSymbolName(relationsFields.iterator().next()))) {
					
					masterField = masterFieldAnnotation.getAttribute(new JavaSymbolName("mappedBy"));
				}
			}
		}
        
        if (masterField != null) {

			// Get field from entity with Column annotation and "name" attribute same as previous "mappedBy"
	        List<FieldMetadata> fields = MemberFindingUtils.getFields(entityDetails);
	        fields.addAll(entityTypeDetails.getPersistenceDetails().getRooIdentifierFields());
	        for (FieldMetadata tmpField : fields) {

	        	List<AnnotationMetadata> fieldAnnotations = tmpField.getAnnotations();
	        	for (AnnotationMetadata annotationMetadata : fieldAnnotations) {
	        		
					if (annotationMetadata.getAnnotationType().equals(new JavaType("javax.persistence.Column")) &&
							masterField.getValue().toString().equals(annotationMetadata.getAttribute(
									new JavaSymbolName("name")).getValue().toString())) {
						
						field = tmpField;
					}
				}
			}
        }
        
		return field;
	}

    // Typically, no changes are required beyond this point

    public static final String getMetadataIdentiferType() {
    	
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType controller, Path path) {
    	
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, controller, path);
    }

    public static final JavaType getJavaType(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, mid);
    }

    public static final Path getPath(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, mid);
    }

    public static boolean isValid(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, mid);
    }
    
}
