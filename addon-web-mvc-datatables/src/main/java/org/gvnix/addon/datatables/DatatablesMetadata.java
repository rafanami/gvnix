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
package org.gvnix.addon.datatables;

import static org.gvnix.addon.datatables.DatatablesConstants.*;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.SpringJavaType.MODEL;
import static org.springframework.roo.model.SpringJavaType.MODEL_ATTRIBUTE;
import static org.springframework.roo.model.SpringJavaType.REQUEST_MAPPING;
import static org.springframework.roo.model.SpringJavaType.RESPONSE_BODY;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.jpa.query.JpaQueryMetadata;
import org.gvnix.addon.web.mvc.batch.WebJpaBatchMetadata;
import org.gvnix.support.WebItdBuilderHelper;
import org.springframework.roo.addon.finder.FieldToken;
import org.springframework.roo.addon.finder.ReservedToken;
import org.springframework.roo.addon.finder.Token;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.FinderMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * {@link GvNIXDatatables} metadata
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class DatatablesMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = HandlerUtils
            .getLogger(DatatablesMetadata.class);

    // Constants
    private static final String PROVIDES_TYPE_STRING = DatatablesMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    /**
     * Annotation values
     */
    private final DatatablesAnnotationValues annotationValues;

    /**
     * Entity property which is its identifier (as roo only use one property,
     * this includes the embedded pks)
     */
    private final FieldMetadata entityIdentifier;

    /**
     * Related entity
     */
    private final JavaType entity;

    /**
     * Entity name to use in var names
     */
    private final String entityName;

    /**
     * Related entity plural
     */
    private final String entityPlural;

    /**
     * If related entity has date properties
     */
    private final Map<JavaSymbolName, DateTimeFormatDetails> entityDatePatterns;

    /**
     * Method name to get entity manager from entity class
     */
    private final JavaSymbolName entityEntityManagerMethod;

    /**
     * Batch services metadata
     */
    private final WebJpaBatchMetadata webJpaBatchMetadata;

    /**
     * Jpa Query metadata
     */
    private final JpaQueryMetadata jpaQueryMetadata;

    /**
     * Field which holds conversionService
     */
    private FieldMetadata conversionService;

    /**
     * Itd builder herlper
     */
    private WebItdBuilderHelper helper;

    private final WebScaffoldAnnotationValues webScaffoldAnnotationValues;

    /**
     * Information about finders
     */
    private final Map<FinderMetadataDetails, QueryHolderTokens> findersRegistered;

    /**
     * Name to use for findAll method (name includes entity plurar)
     */
    private final JavaSymbolName findAllMethodName;

    /**
     * Name to use for renderItem method (name includes entity plural)
     */
    private final JavaSymbolName renderItemsMethodName;

    /**
     * Name to use for findByParamters method (name includes entity plural)
     */
    private final JavaSymbolName findByParametersMethodName;

    /**
     * JavaType for List entity elements
     */
    private final JavaType entityListType;

    public DatatablesMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            DatatablesAnnotationValues annotationValues, JavaType entity,
            List<FieldMetadata> identifierProperties, String entityPlural,
            JavaSymbolName entityManagerMethodName,
            Map<JavaSymbolName, DateTimeFormatDetails> datePatterns,
            JavaType webScaffoldAspectName,
            WebJpaBatchMetadata webJpaBatchMetadata,
            JpaQueryMetadata jpaQueryMetadata,
            WebScaffoldAnnotationValues webScaffoldAnnotationValues,
            Map<FinderMetadataDetails, QueryHolderTokens> findersRegistered) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.annotationValues = annotationValues;

        this.helper = new WebItdBuilderHelper(this,
                builder.getImportRegistrationResolver());

        // Roo only uses one property
        this.entityIdentifier = identifierProperties.get(0);
        this.entity = entity;
        this.entityName = JavaSymbolName.getReservedWordSafeName(entity)
                .getSymbolName();
        this.entityPlural = entityPlural;
        this.entityEntityManagerMethod = entityManagerMethodName;
        this.entityDatePatterns = datePatterns;
        this.webJpaBatchMetadata = webJpaBatchMetadata;
        this.jpaQueryMetadata = jpaQueryMetadata;
        this.webScaffoldAnnotationValues = webScaffoldAnnotationValues;

        // Prepare method names which includes entity plural
        this.findAllMethodName = new JavaSymbolName(
                "findAll".concat(StringUtils.capitalize(entityPlural)));

        this.renderItemsMethodName = new JavaSymbolName(
                "render".concat(StringUtils.capitalize(entityPlural)));

        this.findByParametersMethodName = new JavaSymbolName("find".concat(
                StringUtils.capitalize(entityPlural)).concat("ByParameters"));

        this.entityListType = new JavaType(LIST.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(entity));

        // store finders information
        if (findersRegistered != null) {
            this.findersRegistered = Collections
                    .unmodifiableMap(findersRegistered);
        }
        else {
            this.findersRegistered = null;
        }

        // Adding precedence declaration
        // This aspect before webScaffold
        builder.setDeclarePrecedence(aspectName, webScaffoldAspectName);

        // Adding field definition
        builder.addField(getConversionServiceField());

        // Adding methods
        builder.addMethod(getListDatatablesRequestMethod());
        builder.addMethod(getPopulateDatatablesConfig());
        builder.addMethod(getListRooRequestMethod());
        builder.addMethod(getPopulateParameterMapMethod());
        builder.addMethod(getGetPropertyMapMethod());

        // Add AJAX mode required methods
        if (isAjax()) {
            if (isStantardMode()) {
                builder.addMethod(getFindAllMethod());
            }
            else {
                // Render mode requires this methods
                builder.addMethod(getPopulateItemForRenderMethod());
                builder.addMethod(getRenderItemsMethod());
                builder.addMethod(getFindAllMethodRenderMode());

            }
            // add ajax request for finders
            if (this.findersRegistered != null) {
                for (Entry<FinderMetadataDetails, QueryHolderTokens> finder : this.findersRegistered
                        .entrySet()) {
                    builder.addMethod(getAjaxFinderMethod(finder.getKey(),
                            finder.getValue()));
                }
            }
        }
        else if (!isStantardMode()) {
            // Non-Standard view mode requires AJAX data mode
            throw new IllegalArgumentException(aspectName
                    .getFullyQualifiedTypeName()
                    .concat(".@GvNIXDatatables: 'mode = ")
                    .concat(annotationValues.getMode())
                    .concat("' requires 'ajax = true'"));
        }
        else {
            // DOM standard mode requires finder by parameters
            builder.addMethod(getFindByParametersMethod());
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>renderXXX</code> method. <br>
     * This method renders the required jspx for item an store it in a List of
     * Map(String,String)
     * 
     * @return
     */
    private MethodMetadata getRenderItemsMethod() {

        JavaType entitySearchResult = new JavaType(
                SEARCH_RESULTS.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(entity));

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(entitySearchResult, HTTP_SERVLET_REQUEST,
                        HTTP_SERVLET_RESPONSE);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(renderItemsMethodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(SERVLET_EXCEPTION);
        throwsTypes.add(IO_EXCEPTION);

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("searchResult"));
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(RESPONSE_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildRenderItemsMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, renderItemsMethodName,
                LIST_MAP_STRING_STRING, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance

    }

    /**
     * Builds the method body of <code>renderXXX</code> method. <br>
     * This method renders the required jspx for item an store it in a List of
     * Map(String,String)
     * 
     * @param bodyBuilder
     */
    private void buildRenderItemsMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        String entityTypeName = helper.getFinalTypeName(entity);
        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// Prepare result");

        String entityListVar = StringUtils.uncapitalize(entityPlural);
        /*
         * List<Pets> pets = searchResult.getResults();
         */
        bodyBuilder.appendFormalLine(String.format(
                "%s %s = searchResult.getResults();",
                helper.getFinalTypeName(entityListType), entityListVar));
        /*
         * List<Map<String, String>> result = new ArrayList<Map<String,
         * String>>();
         */
        bodyBuilder.appendFormalLine(String.format(
                "%s result = new %s(%s.size());",
                helper.getFinalTypeName(LIST_MAP_STRING_STRING),
                helper.getFinalTypeName(ARRAYLIST_MAP_STRING_STRING),
                entityListVar));
        bodyBuilder.appendFormalLine(String.format(
                "String controllerPath = \"%s\";",
                webScaffoldAnnotationValues.getPath()));
        bodyBuilder.appendFormalLine(String.format(
                "String pageToUse = \"%s\";", annotationValues.getMode()));
        bodyBuilder
                .appendFormalLine("String renderUrl = String.format(\"/WEB-INF/views/%s/%s.jspx\", controllerPath, pageToUse);");

        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// For every element");
        // for (Pet pet : owners) {
        bodyBuilder.appendFormalLine(String.format("for (%s %s: %s) {",
                entityTypeName, entityName, entityListVar));
        bodyBuilder.indent();

        // Map<String, String> row = new HashMap<String, String>();
        bodyBuilder.appendFormalLine(String.format("%s item = new %s();",
                helper.getFinalTypeName(MAP_STRING_STRING),
                helper.getFinalTypeName(HASHMAP_STRING_STRING)));
        // final StringWriter buffer = new StringWriter();
        bodyBuilder.appendFormalLine(String.format(
                "final %s buffer = new %s();",
                helper.getFinalTypeName(STRING_WRITER),
                helper.getFinalTypeName(STRING_WRITER)));

        // TODO Check it can get dispatcher outside of for
        bodyBuilder.appendFormalLine("// Call JSP to render current entity");
        // RequestDispatcher dispatcher =
        // request.getRequestDispatcher("/WEB-INF/views/owners/show.jspx");
        bodyBuilder.appendFormalLine(String.format(
                "%s dispatcher = %s.getRequestDispatcher(renderUrl);",
                helper.getFinalTypeName(REQUEST_DISPATCHER),
                REQUEST_PARAM_NAME.getSymbolName()));
        bodyBuilder.appendFormalLine("");

        // populateItemForRender(request, pet);
        bodyBuilder.appendFormalLine(String.format(
                "populateItemForRender(%s, %s);",
                REQUEST_PARAM_NAME.getSymbolName(), entityName));
        // dispatcher.include(request, new HttpServletResponseWrapper(response)
        // {
        bodyBuilder.appendFormalLine(String.format(
                "dispatcher.include(%s, new %s(%s) {",
                REQUEST_PARAM_NAME.getSymbolName(),
                helper.getFinalTypeName(HTTP_SERVLET_RESPONSE_WRAPPER),
                RESPONSE_PARAM_NAME.getSymbolName()));
        bodyBuilder.indent();
        // private PrintWriter writer = new PrintWriter(buffer);
        bodyBuilder.appendFormalLine(String.format(
                "private %s writer = new %s(buffer);",
                helper.getFinalTypeName(PRINT_WRITER),
                helper.getFinalTypeName(PRINT_WRITER)));
        // @Override
        bodyBuilder.appendFormalLine("@Override");
        // public PrintWriter getWriter() throws IOException {
        bodyBuilder.appendFormalLine(String.format(
                "public %s getWriter() throws %s {",
                helper.getFinalTypeName(PRINT_WRITER),
                helper.getFinalTypeName(IO_EXCEPTION)));
        bodyBuilder.indent();
        // return writer;
        bodyBuilder.appendFormalLine("return writer;");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("});");

        bodyBuilder.appendFormalLine("");

        // String render = buffer.toString();
        bodyBuilder.appendFormalLine("String render = buffer.toString();");

        bodyBuilder.appendFormalLine("// Load item id)");
        // row.put("DT_RowId",
        // conversionService_datatables.convert(owner.getId(), String.class));
        bodyBuilder
                .appendFormalLine(String
                        .format("item.put(\"DT_RowId\", %s.convert(%s.get%s(), String.class));",
                                getConversionServiceField().getFieldName()
                                        .getSymbolName(), entityName,
                                StringUtils.capitalize(entityIdentifier
                                        .getFieldName().getSymbolName())));
        bodyBuilder
                .appendFormalLine("// Put rendered content into first column (uses column index)");
        // row.put(Integer.toString(rowIdx), showOwner);
        bodyBuilder.appendFormalLine("item.put(\"0\", render);");

        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("result.add(item);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");

        bodyBuilder.appendFormalLine("return result;");

    }

    /**
     * Gets <code>getFindByParameters</code> method. <br>
     * This method generates a item List based on a parameters received in the
     * request. Used by DOM mode listDatatables.
     * 
     * @return
     */
    private MethodMetadata getFindByParametersMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(entity, ENUMERATION_STRING);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(findByParametersMethodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName(entityName));
        parameterNames.add(new JavaSymbolName("propertyNames"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildFindByParametersMapMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, findByParametersMethodName,
                entityListType, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>getFindByParameters</code> method. <br>
     * This method generates a item List based on a parameters received in the
     * request. Used by DOM mode listDatatables.
     * 
     * @param bodyBuilder
     */
    private void buildFindByParametersMapMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Gets propertyMap
        bodyBuilder.appendFormalLine("// Gets propertyMap");
        // Map<String, Object> propertyMap = getPropertyMap(visit,
        // propertyNames);
        bodyBuilder.appendFormalLine(String.format(
                "%s propertyMap = %s(%s, propertyNames);",
                helper.getFinalTypeName(MAP_STRING_OBJECT),
                GET_PROPERTY_MAP.getSymbolName(), entityName));
        //
        bodyBuilder.appendFormalLine("");
        //
        // // if there is a filter
        bodyBuilder.appendFormalLine("// if there is a filter");
        // if (!propertyMap.isEmpty()) {
        bodyBuilder.appendFormalLine("if (!propertyMap.isEmpty()) {");
        bodyBuilder.indent();

        // // Prepare a predicate
        bodyBuilder.appendFormalLine("// Prepare a predicate");
        // BooleanBuilder baseFilterPredicate = new BooleanBuilder();
        bodyBuilder.appendFormalLine(String.format(
                "%s baseFilterPredicate = new %s();",
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER),
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER)));
        bodyBuilder.appendFormalLine("");
        // // Base filter. Using BooleanBuilder, a cascading builder for
        bodyBuilder
                .appendFormalLine("// Base filter. Using BooleanBuilder, a cascading builder for");
        // // Predicate expressions
        bodyBuilder.appendFormalLine("// Predicate expressions");
        // PathBuilder<Visit> entity = new PathBuilder<Visit>(Visit.class,
        // "entity");
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> entity = new %s<%s>(%s.class, \"entity\");",
                helper.getFinalTypeName(QDSL_PATH_BUILDER),
                helper.getFinalTypeName(entity),
                helper.getFinalTypeName(QDSL_PATH_BUILDER),
                helper.getFinalTypeName(entity),
                helper.getFinalTypeName(entity)));

        bodyBuilder.appendFormalLine("");
        //
        // // Build base filter
        bodyBuilder.appendFormalLine("// Build base filter");
        // for (String key : propertyMap.keySet()) {
        bodyBuilder
                .appendFormalLine("for (String key : propertyMap.keySet()) {");
        bodyBuilder.indent();
        // baseFilterPredicate.and(entity.get(key).eq(propertyMap.get(key)));
        bodyBuilder
                .appendFormalLine("baseFilterPredicate.and(entity.get(key).eq(propertyMap.get(key)));");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");
        //
        // // Create a query with filter
        bodyBuilder.appendFormalLine("// Create a query with filter");
        // JPAQuery query = new JPAQuery(Visit.entityManager());
        bodyBuilder.appendFormalLine(String.format(
                "%s query = new %s(%s.entityManager());",
                helper.getFinalTypeName(QDSL_JPA_QUERY),
                helper.getFinalTypeName(QDSL_JPA_QUERY),
                helper.getFinalTypeName(entity)));
        // query = query.from(entity);
        bodyBuilder.appendFormalLine("query = query.from(entity);");
        //
        bodyBuilder.appendFormalLine("");
        // // execute query
        bodyBuilder.appendFormalLine("// execute query");
        // return query.where(baseFilterPredicate).list(entity);
        bodyBuilder
                .appendFormalLine("return query.where(baseFilterPredicate).list(entity);");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");
        //
        // // no filter: return all elements
        bodyBuilder.appendFormalLine("// no filter: return all elements");
        // return Visit.findAllVisits();
        bodyBuilder.appendFormalLine(String.format("return %s.findAll%s();",
                helper.getFinalTypeName(entity),
                StringUtils.capitalize(entityPlural)));
    }

    /**
     * Gets <code>getPropertyMap</code> method. <br>
     * This method returns a Map with bean properties which appears on a
     * Enumeration (usually from httpRequest.getParametersNames())
     * 
     * @return
     */
    private MethodMetadata getGetPropertyMapMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(entity, ENUMERATION_STRING);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GET_PROPERTY_MAP,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName(entityName));
        parameterNames.add(new JavaSymbolName("propertyNames"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetPropertyMapMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, GET_PROPERTY_MAP, MAP_STRING_OBJECT,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build body method <code>getPropertyMap</code> method. <br>
     * This method returns a Map with bean properties which appears on a
     * Enumeration (usually from httpRequest.getParametersNames())
     * 
     * @return
     */
    private void buildGetPropertyMapMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // Map<String, Object> propertyValuesMap = new HashMap<String,
        // Object>();
        bodyBuilder.appendFormalLine(String.format(
                "%s propertyValuesMap = new %s();",
                helper.getFinalTypeName(MAP_STRING_OBJECT),
                helper.getFinalTypeName(HASHMAP_STRING_OBJECT)));
        //
        bodyBuilder.appendFormalLine("");

        // // If no entity or properties given, return empty Map
        bodyBuilder
                .appendFormalLine("// If no entity or properties given, return empty Map");

        // if(entity == null || propertyNames == null) {
        bodyBuilder.appendFormalLine(String.format(
                "if(%s == null || propertyNames == null) {", entityName));
        bodyBuilder.indent();
        // return propertyValuesMap;
        bodyBuilder.appendFormalLine("return propertyValuesMap;");

        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");
        // List<String> properties = new ArrayList<String>();
        bodyBuilder.appendFormalLine(String.format("%s properties = new %s();",
                helper.getFinalTypeName(LIST_STRING),
                helper.getFinalTypeName(ARRAYLIST_STRING)));

        // CollectionUtils.addAll(properties, propertyNames);
        bodyBuilder.appendFormalLine(String.format(
                "%s.addAll(properties, propertyNames);",
                helper.getFinalTypeName(COLLECTION_UTILS)));

        //
        bodyBuilder.appendFormalLine("");

        // // There must be at least one property name, otherwise return empty
        // Map
        bodyBuilder
                .appendFormalLine("// There must be at least one property name, otherwise return empty Map");
        // if(properties.isEmpty()) {
        bodyBuilder.appendFormalLine("if (properties.isEmpty()) {");
        bodyBuilder.indent();
        // return propertyValuesMap;
        bodyBuilder.appendFormalLine("return propertyValuesMap;");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        //
        bodyBuilder.appendFormalLine("");
        // // Iterate over given properties to get each property value
        bodyBuilder
                .appendFormalLine("// Iterate over given properties to get each property value");
        // BeanWrapper entityBean = new BeanWrapperImpl(entity);
        bodyBuilder.appendFormalLine(String.format(
                "%s entityBean = new %s(%s);",
                helper.getFinalTypeName(BEAN_WRAPPER),
                helper.getFinalTypeName(BEAN_WRAPPER_IMP), entityName));

        // for (String propertyName : properties) {
        bodyBuilder
                .appendFormalLine("for (String propertyName : properties) {");
        bodyBuilder.indent();
        // if (entityBean.isReadableProperty(propertyName)) {
        bodyBuilder
                .appendFormalLine("if (entityBean.isReadableProperty(propertyName)) {");
        bodyBuilder.indent();
        // Object propertyValue = null;
        bodyBuilder.appendFormalLine("Object propertyValue = null;");
        // try {
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        // propertyValue = entityBean.getPropertyValue(propertyName);
        bodyBuilder
                .appendFormalLine("propertyValue = entityBean.getPropertyValue(propertyName);");
        // } catch (Exception e){
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} catch (Exception e){");
        bodyBuilder.indent();
        // // TODO log warning
        bodyBuilder.appendFormalLine("// TODO log warning");
        // continue;
        bodyBuilder.appendFormalLine("continue;");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // propertyValuesMap.put(propertyName, propertyValue);
        bodyBuilder
                .appendFormalLine("propertyValuesMap.put(propertyName, propertyValue);");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return propertyValuesMap;
        bodyBuilder.appendFormalLine("return propertyValuesMap;");
    }

    /**
     * Gets <code>populateParameterMap</code> method. This method transforms a
     * HttpServlerRequest Map<String,String[]> into a Map<String,String>
     * 
     * @return
     */
    private MethodMetadata getPopulateParameterMapMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(HTTP_SERVLET_REQUEST);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POPULATE_PARAMETERS_MAP,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("request"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildPopulateParameterMapMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POPULATE_PARAMETERS_MAP,
                MAP_STRING_STRING, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build body method of <code>populateParameterMap</code> method. This
     * method transforms a HttpServlerRequest Map<String,String[]> into a
     * Map<String,String>
     * 
     * @param bodyBuilder
     */
    private void buildPopulateParameterMapMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Map<String, Object> params;
        bodyBuilder.appendFormalLine(String.format("%s params;",
                helper.getFinalTypeName(MAP_STRING_OBJECT)));
        // if (request == null) {
        bodyBuilder.appendFormalLine("if (request == null) {");
        bodyBuilder.indent();
        // params = Collections.emptyMap();
        bodyBuilder.appendFormalLine(String.format("params = %s.emptyMap();",
                helper.getFinalTypeName(COLLECTIONS)));
        // } else {
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();
        // params = new HashMap<String, Object>(request.getParameterMap());
        bodyBuilder.appendFormalLine(String.format(
                "params = new %s(request.getParameterMap());",
                helper.getFinalTypeName(HASHMAP_STRING_OBJECT)));
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        //
        bodyBuilder.appendFormalLine("");

        // Map<String, String> allParams = new HashMap<String,
        // String>(params.size());
        bodyBuilder.appendFormalLine(String.format(
                "%s allParams = new %s(params.size());",
                helper.getFinalTypeName(MAP_STRING_STRING),
                helper.getFinalTypeName(HASHMAP_STRING_STRING)));
        //
        bodyBuilder.appendFormalLine("");

        // String value;
        bodyBuilder.appendFormalLine("String value;");
        // String objValue;
        bodyBuilder.appendFormalLine("Object objValue;");
        // for (String key : params.keySet()) {
        bodyBuilder.appendFormalLine("for (String key : params.keySet()) {");
        bodyBuilder.indent();

        bodyBuilder.appendFormalLine("objValue = params.get(key);");

        // if (objValue instanceof String[]) {
        bodyBuilder.appendFormalLine("if (objValue instanceof String[]) {");
        bodyBuilder.indent();
        // value = ((String[]) entry.getValue())[0];
        bodyBuilder.appendFormalLine("value = ((String[]) objValue)[0];");
        // } else {
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();
        // value = (String) entry.getValue();
        bodyBuilder.appendFormalLine("value = (String) objValue;");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        // allParams.put(entry.getKey(), value);
        bodyBuilder.appendFormalLine("allParams.put(key, value);");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        // return allParams;
        bodyBuilder.appendFormalLine("return allParams;");

    }

    /**
     * Gets a method to manage AJAX data request of a datatables which draw the
     * result of a Roo Dynamic finder.
     * 
     * @param finderMethod
     * @param queryHolder
     * @return
     */
    private MethodMetadata getAjaxFinderMethod(
            FinderMetadataDetails finderMethod, QueryHolderTokens queryHolder) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Add datatable request parameters
        parameterTypes.add(new AnnotatedJavaType(DATATABLES_CRITERIA_TYPE,
                new AnnotationMetadataBuilder(DATATABLES_PARAMS).build()));

        // Prepares @RequestParam method parameters based on
        // finder information
        List<AnnotatedJavaType> finderParamTypes = finderMethod
                .getFinderMethodMetadata().getParameterTypes();
        List<JavaSymbolName> finderParamNames = finderMethod
                .getFinderMethodMetadata().getParameterNames();
        JavaType paramType;
        for (int i = 0; i < finderParamTypes.size(); i++) {
            paramType = finderParamTypes.get(i).getJavaType();
            if (paramType.isBoolean()) {
                // Boolean's false value is omitted on request, so must be
                // optional (by default will get false)
                parameterTypes.add(helper.createRequestParam(paramType,
                        finderParamNames.get(i).getSymbolName(), false, null));
            }
            else {
                parameterTypes.add(helper.createRequestParam(paramType,
                        finderParamNames.get(i).getSymbolName(), null, null));
            }
        }
        if (!isStantardMode()) {
            // For render mode request and response are needed to
            // perform internal request for item render
            parameterTypes.add(AnnotatedJavaType
                    .convertFromJavaType(HTTP_SERVLET_REQUEST));
            parameterTypes.add(AnnotatedJavaType
                    .convertFromJavaType(HTTP_SERVLET_RESPONSE));
        }

        JavaSymbolName methodName = new JavaSymbolName(
                finderMethod.getFinderName());

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }
        // get Finder-name value
        String finderNameValue = finderMethod.getFinderName();
        finderNameValue = finderNameValue.substring(finderNameValue
                .indexOf("By"));
        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);
        methodAnnotation.addStringAttribute("headers",
                "Accept=application/json");
        methodAnnotation.addStringAttribute("value", "/datatables/ajax");
        methodAnnotation.addStringAttribute("params",
                "ajax_find=".concat(finderNameValue));
        methodAnnotation.addStringAttribute("produces", "application/json");
        annotations.add(methodAnnotation);
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        if (!isStantardMode()) {
            // On render mode internal render request
            // can throw ServletException or IOException
            throwsTypes.add(SERVLET_EXCEPTION);
            throwsTypes.add(IO_EXCEPTION);
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        for (JavaSymbolName paramName : finderParamNames) {
            parameterNames.add(paramName);
        }
        if (!isStantardMode()) {
            // For render mode request and response are needed to
            // perform internal request for item render
            parameterNames.add(REQUEST_PARAM_NAME);
            parameterNames.add(RESPONSE_PARAM_NAME);
        }

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildFinderAjaxMethodBody(bodyBuilder, finderMethod, queryHolder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, FIND_ALL_RETURN,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Build body method to manage AJAX data request of a datatables which draw
     * the result of a Roo Dynamic finder.
     * 
     * @param bodyBuilder
     * @param finderMethod
     * @param queryHolder
     */
    private void buildFinderAjaxMethodBody(
            InvocableMemberBodyBuilder bodyBuilder,
            FinderMetadataDetails finderMethod, QueryHolderTokens queryHolder) {

        // BooleanBuilder baseFilterPredicate = new BooleanBuilder();
        bodyBuilder.appendFormalLine(String.format("%s baseSearch = new %s();",
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER),
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER)));
        bodyBuilder.appendFormalLine("");

        bodyBuilder
                .appendFormalLine("// Base Search. Using BooleanBuilder, a cascading builder for");
        bodyBuilder.appendFormalLine("// Predicate expressions");

        String path_builder = helper.getFinalTypeName(QDSL_PATH_BUILDER);
        String entity_class = helper.getFinalTypeName(entity);

        // PathBuilder<Pet> entity = new PathBuilder<Pet>(Pet.class, "entity");
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> entity = new %s<%s>(%s.class, \"entity\");",
                path_builder, entity_class, path_builder, entity_class,
                entity_class));

        bodyBuilder.appendFormalLine("");

        // Build code to generate QueryDsl predicate to include base filter
        // on Datatables.findByCriteria
        buildFinderAjaxBaseSearch(bodyBuilder, finderMethod, queryHolder);

        bodyBuilder.appendFormalLine("");

        // Build Datatables.findByCriteria call code
        buildFindByCriteriaCall(bodyBuilder, true);

        // Build code for response based on SearchResult
        if (isStantardMode()) {
            // Build code to delegate on Datatables.populateDataSearch
            buildPopulateDataSearchCall(bodyBuilder);
        }
        else {
            // Build code to call RenderItem method
            buildPopulateDataSearchCallRenderMode(bodyBuilder);
        }
    }

    /**
     * Builds code to create a QueryDsl predicate which implements a Roo dynamic
     * filter condition
     * 
     * @param bodyBuilder
     * @param finderMethod
     * @param queryHolder
     */
    private void buildFinderAjaxBaseSearch(
            InvocableMemberBodyBuilder bodyBuilder,
            FinderMetadataDetails finderMethod, QueryHolderTokens queryHolder) {

        FieldToken lastFieldToken = null;

        // flag set when a FieldTocken is located
        boolean isNewField = true;

        // Flag set when a condition is generated
        // but not included yet
        boolean isConditionApplied = false;

        // Helper which can generated the expression
        // required for generate the predicate
        FinderToDslHelper fHelper = new FinderToDslHelper(finderMethod,
                queryHolder, helper);

        // Holds current expression
        StringBuilder expBuilder = null;
        for (final Token token : queryHolder.getTokens()) {
            if (token instanceof ReservedToken) {
                // Current token isn't a field
                final String reservedToken = token.getValue();
                if (lastFieldToken == null) {
                    // Any operator must be preceded by refereed field
                    // XXX Throw a "wrong format" exception?
                    continue;
                }

                // prepare field name
                final String fieldName = lastFieldToken.getField()
                        .getFieldName().getSymbolName();

                // Add PathBuilder to expression
                expBuilder = new StringBuilder("entity");

                // XXX Currently ManyToMany and OneToMany
                // properties are not supported by Roo finders
                if (!lastFieldToken.getField().getFieldType()
                        .isCommonCollectionType()) {

                    // If previous token was a field
                    // include Path getter
                    if (isNewField) {
                        JavaType fieldType = fHelper
                                .getFieldTypeOfFinder(fieldName);
                        Validate.notNull(
                                fieldType,
                                "Field type not found for '%s' field in '%s' finder",
                                fieldName, finderMethod.getFinderName());
                        expBuilder.append(fHelper.getDslGetterFor(fieldName,
                                fieldType));
                        if (reservedToken.equalsIgnoreCase("Like")) {
                            // Add function toLower (and cast to string if it's
                            // needed)
                            expBuilder.append(fHelper
                                    .getToLowerOperatorFor(fieldType));
                        }
                        // mark as field getter is already processed
                        isNewField = false;

                        // mark as condition is no included
                        isConditionApplied = false;
                    }
                    if (reservedToken.equalsIgnoreCase("And")) {
                        if (!isConditionApplied) {
                            // use .eq() operator if condition not applied
                            expBuilder.append(fHelper
                                    .getEqualExpression(fieldName));
                        }
                        // filterCondition.and( {exp});
                        bodyBuilder.appendFormalLine(fHelper
                                .getDslAnd(expBuilder.toString()));
                        expBuilder = null;
                        isConditionApplied = true;
                    }
                    else if (reservedToken.equalsIgnoreCase("Or")) {
                        if (!isConditionApplied) {
                            // use .eq() operator if condition not applied
                            expBuilder.append(fHelper
                                    .getEqualExpression(fieldName));
                        }
                        bodyBuilder.appendFormalLine(fHelper
                                .getDslOr(expBuilder.toString()));
                        expBuilder = null;
                        isConditionApplied = true;
                    }
                    else if (reservedToken.equalsIgnoreCase("Between")) {
                        // use .between(minField,maxField) expression
                        expBuilder.append(fHelper
                                .getBetweenExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("Like")) {
                        // use .like() expression
                        expBuilder.append(fHelper.getLikeExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("IsNotNull")) {
                        // builder.append(" IS NOT NULL ");
                        // use isNotNull() expression
                        expBuilder.append(".isNotNull()");
                    }
                    else if (reservedToken.equalsIgnoreCase("IsNull")) {
                        // builder.append(" IS NULL ");
                        // use isNull() expression
                        expBuilder.append(".isNull()");
                    }
                    else if (reservedToken.equalsIgnoreCase("Not")) {
                        // builder.append(" IS NOT ");
                        // use not() expression
                        expBuilder.append(".not()");
                    }
                    else if (reservedToken.equalsIgnoreCase("NotEquals")) {
                        // builder.append(" != ");
                        expBuilder.append(fHelper
                                .getNotEqualExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("LessThan")) {
                        // builder.append(" < ");
                        expBuilder.append(fHelper
                                .getLessThanExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("LessThanEquals")) {
                        // builder.append(" <= ");
                        expBuilder.append(fHelper
                                .getLessThanEqualsExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("GreaterThan")) {
                        // builder.append(" > ");
                        expBuilder.append(fHelper
                                .getGreaterThanExpression(fieldName));
                    }
                    else if (reservedToken
                            .equalsIgnoreCase("GreaterThanEquals")) {
                        // builder.append(" >= ");
                        expBuilder.append(fHelper
                                .getGreaterThanEqualseExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("Equals")) {
                        // builder.append(" = ");
                        expBuilder
                                .append(fHelper.getEqualExpression(fieldName));
                    }
                }
            }
            else {
                // current token is a field
                lastFieldToken = (FieldToken) token;
                isNewField = true;
            }
        }
        if (isNewField) {
            // New field is located but no operation
            // has found. generate an expression using
            // equals operator
            expBuilder = new StringBuilder("entity");
            if (lastFieldToken != null
                    && !lastFieldToken.getField().getFieldType()
                            .isCommonCollectionType()) {
                String fieldName = lastFieldToken.getField().getFieldName()
                        .getSymbolName();
                JavaType fieldType = fHelper.getFieldTypeOfFinder(fieldName);
                expBuilder
                        .append(fHelper.getDslGetterFor(fieldName, fieldType));
                expBuilder.append(fHelper.getEqualExpression(fieldName));

            }
            isConditionApplied = false;
        }
        if (!isConditionApplied) {
            // There is an expression not included in predicate yet.
            // Include it using "and" join
            if (expBuilder != null) {
                // filterCondition.and( {exp});
                bodyBuilder.appendFormalLine(fHelper.getDslAnd(expBuilder
                        .toString()));
            }
        }
    }

    /**
     * Gets <code>populateDatatablesConfig</code> method <br>
     * This method insert on Model all configuration properties which will need
     * ".tagx" to render final page. <br>
     * This properties are:
     * <ul>
     * <li><em>datatablesHasBatchSupport</em> informs if there is batch entity
     * operations support on controller (used for multi-row delete operation)</li>
     * <li><em>datatablesUseAjax</em> informs datatables data mode (
     * <em>true</em> : AJAX <em>false</em> DOM)</li>
     * <li><em>finderNameParam</em> sets the name of parameter that will contain
     * the {@code finderName} (only for AJAX mode)</li>
     * <li><em>datatablesStandardMode</em> informs render mode (<em>true</em>
     * for standard datatable view; <em>false</em> for single-item-page,
     * one-cell-per-item or render-jspx datatable modes)</li>
     * </ul>
     * 
     * @return
     */
    private MethodMetadata getPopulateDatatablesConfig() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(MODEL);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POPULATE_DATATABLES_CONFIG,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder annotation = new AnnotationMetadataBuilder(
                MODEL_ATTRIBUTE);
        // @ModelAttribute
        annotations.add(annotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UI_MODEL);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(String.format(
                "uiModel.addAttribute(\"datatablesHasBatchSupport\", %s);",
                hasJpaBatchSupport()));
        bodyBuilder.appendFormalLine(String.format(
                "uiModel.addAttribute(\"datatablesUseAjax\",%s);", isAjax()));
        bodyBuilder.appendFormalLine(String.format(
                "uiModel.addAttribute(\"datatablesStandardMode\",%s);",
                isStantardMode()));
        if (isAjax()) {
            bodyBuilder
                    .appendFormalLine("uiModel.addAttribute(\"finderNameParam\",\"ajax_find\");");
        }

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POPULATE_DATATABLES_CONFIG,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>populateItemForRender</code> method <br>
     * This methods prepares request attributes to render a entity item in
     * non-standard render mode. User can make push-in of this method to
     * customize the parameters received on target .jspx view.
     * 
     * @return
     */
    private MethodMetadata getPopulateItemForRenderMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(HTTP_SERVLET_REQUEST, entity);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POPULATE_ITEM_FOR_RENDER,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(new JavaSymbolName(entityName));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildPopulateItemForRenderMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POPULATE_ITEM_FOR_RENDER,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build standard body method for <code>populateItemForRender</code> method <br>
     * This methods prepares request attributes to render a entity item in
     * non-standard render mode. User can make push-in of this method to
     * customize the parameters received on target .jspx view.
     * 
     * @param bodyBuilder
     */
    private void buildPopulateItemForRenderMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Model uiModel = new ExtendedModelMap();
        bodyBuilder.appendFormalLine(String.format("%s uiModel = new %s();",
                MODEL, EXTENDED_MODEL_MAP));
        bodyBuilder.appendFormalLine("");

        // request.setAttribute("pet", pet);
        bodyBuilder.appendFormalLine(String.format(
                "%s.setAttribute(\"%s\", %s);",
                REQUEST_PARAM_NAME.getSymbolName(), entityName, entityName

        ));
        // request.setAttribute("itemId",
        // conversion_service.convert(pet.getId(), String.class);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s.setAttribute(\"itemId\", %s.convert(%s.get%s(),String.class));",
                                REQUEST_PARAM_NAME.getSymbolName(),
                                getConversionServiceField().getFieldName()
                                        .getSymbolName(), entityName,
                                StringUtils.capitalize(entityIdentifier
                                        .getFieldName().getSymbolName())));

        bodyBuilder.appendFormalLine("");
        // Add date patterns (if any)
        if (!entityDatePatterns.isEmpty()) {
            // Delegates on Roo standard populate date patterns method
            // addDateTimeFormatPatterns(uiModel);
            bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
            bodyBuilder.appendFormalLine("");
        }

        // Load uiModel attributes into request
        bodyBuilder.appendFormalLine("// Load uiModel attributes into request");
        // Map<String,Object> modelMap = uiModel.asMap();
        bodyBuilder.appendFormalLine(String.format(
                "%s modelMap = uiModel.asMap();",
                helper.getFinalTypeName(MAP_STRING_OBJECT)));
        // for (Entry<String,Object> entry : uiModel.asMap().entrySet()){
        bodyBuilder.appendFormalLine(String.format(
                "for (%s key : modelMap.keySet()){",
                helper.getFinalTypeName(JavaType.STRING)));
        bodyBuilder.indent();

        // request.setAttribute(entry.getKey(), entry.getValue());
        bodyBuilder
                .appendFormalLine("request.setAttribute(key, modelMap.get(key));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

    }

    /**
     * Returns <code>listDatatables</code> method <br>
     * This method is default list request handler for datatables controllers
     * 
     * @return
     */
    private MethodMetadata getListDatatablesRequestMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(MODEL, HTTP_SERVLET_REQUEST);

        if (!isAjax()) {
            // In DOM mode we include Item in parameters to use
            // spring's binder to get baseFilter values
            parameterTypes.add(new AnnotatedJavaType(entity,
                    new AnnotationMetadataBuilder(MODEL_ATTRIBUTE).build()));
        }

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(LIST_DATATABLES,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);

        // @RequestMapping(method = RequestMethod.GET...
        methodAnnotation.addEnumAttribute("method", REQUEST_METHOD, "GET");

        // @RequestMapping(... produces = "text/html")
        methodAnnotation.addStringAttribute("produces", "text/html");

        annotations.add(methodAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UI_MODEL);
        parameterNames.add(new JavaSymbolName("request"));

        if (!isAjax()) {
            // In DOM mode we include Item in parameters to use
            // spring's binder to get baseFilter values
            parameterNames.add(new JavaSymbolName(entityName));
        }

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        if (isAjax()) {
            buildListDatatablesRequesMethodAjaxBody(bodyBuilder);
        }
        else {
            buildListDatatablesRequesMethodDomBody(bodyBuilder);
        }

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, LIST_DATATABLES, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build method body for <code>listDatatables</code> method for DOM mode
     * 
     * @param bodyBuilder
     */
    private void buildListDatatablesRequesMethodDomBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        String listVarName = StringUtils.uncapitalize(entityPlural);

        bodyBuilder
                .appendFormalLine("// Get data (filtered by received parameters) and put it on pageContext");
        // List<Visit> visits =
        // findByParameters(visit,request.getParameterNames());
        bodyBuilder
                .appendFormalLine(String
                        .format("@SuppressWarnings(\"unchecked\") %s %s = %s(%s, request != null ? request.getParameterNames() : null);",
                                helper.getFinalTypeName(entityListType),
                                listVarName,
                                findByParametersMethodName.getSymbolName(),
                                entityName));

        // uiModel.addAttribute("pets",pets);
        bodyBuilder.appendFormalLine(String.format(
                "%s.addAttribute(\"%s\",%s);", UI_MODEL.getSymbolName(),
                entityPlural.toLowerCase(), listVarName));

        // return "pets/list";
        bodyBuilder.appendFormalLine(String.format("return \"%s/list\";",
                entityPlural.toLowerCase()));

    }

    /**
     * Build method body for <code>listDatatables</code> method for AJAX mode
     * 
     * @param bodyBuilder
     */
    private void buildListDatatablesRequesMethodAjaxBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // [Code generated] Map<String, String> params =
        // populateParameterMap(request);
        bodyBuilder.appendFormalLine(String.format("%s params = %s(request);",
                helper.getFinalTypeName(MAP_STRING_STRING),
                POPULATE_PARAMETERS_MAP.getSymbolName()));

        // [Code generated] if (!params.isEmpty()) {
        bodyBuilder.appendFormalLine("if (!params.isEmpty()) {");
        bodyBuilder.indent();

        // [Code generated] uiModel.addAttribute("baseFilter", params);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"baseFilter\", params);");

        // [Code generated] }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // [Code generated] return "pets/list";
        bodyBuilder.appendFormalLine(String.format("return \"%s/list\";",
                entityPlural.toLowerCase()));
    }

    /**
     * Redefines {@code list} Roo webScaffod method to delegate on
     * {@link #LIST_DATATABLES}
     * 
     * @return
     */
    private MethodMetadata getListRooRequestMethod() {

        // public String OwnerController.list(
        // @RequestParam(value = "page", required = false) Integer page,
        // @RequestParam(value = "size", required = false) Integer size,
        // Model uiModel) {

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = Arrays.asList(helper
                .createRequestParam(JavaType.INT_OBJECT, "page", false, null),
                helper.createRequestParam(JavaType.INT_OBJECT, "size", false,
                        null), new AnnotatedJavaType(MODEL));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(LIST_ROO, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping(produces = "text/html")
        annotations.add(helper.getRequestMappingAnnotation(null, null, null,
                "text/html", null, null));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName("page"), new JavaSymbolName("size"),
                UI_MODEL);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder
                .appendFormalLine("// overrides the standard Roo list method and");
        bodyBuilder.appendFormalLine("// delegates on datatables list method");
        // return listDatatables(uiModel);
        if (isAjax()) {
            bodyBuilder.appendFormalLine(String.format(
                    "return %s(uiModel, null);",
                    LIST_DATATABLES.getSymbolName()));
        }
        else {
            bodyBuilder.appendFormalLine(String.format(
                    "return %s(uiModel, null, null);",
                    LIST_DATATABLES.getSymbolName()));
        }

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, LIST_ROO, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Returns <code>findAllMethod</code> method <br>
     * This method handles datatables AJAX request for data which are no related
     * to a Roo Dynamic finder.
     * 
     * @return
     */
    private MethodMetadata getFindAllMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(new AnnotatedJavaType(DATATABLES_CRITERIA_TYPE,
                new AnnotationMetadataBuilder(DATATABLES_PARAMS).build()));

        parameterTypes.add(new AnnotatedJavaType(entity,
                new AnnotationMetadataBuilder(MODEL_ATTRIBUTE).build()));
        parameterTypes.addAll(AnnotatedJavaType
                .convertFromJavaTypes(HTTP_SERVLET_REQUEST));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(findAllMethodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);
        methodAnnotation.addStringAttribute("headers",
                "Accept=application/json");
        methodAnnotation.addStringAttribute("value", "/datatables/ajax");
        methodAnnotation.addStringAttribute("produces", "application/json");
        annotations.add(methodAnnotation);
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        parameterNames.add(new JavaSymbolName(StringUtils
                .uncapitalize(entityName)));
        parameterNames.add(REQUEST_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildFindAllDataMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, findAllMethodName, FIND_ALL_RETURN,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Returns <code>findAll</code> method for render-a-view visualization mode. <br>
     * This method handles datatables AJAX request for data which are no related
     * to a Roo Dynamic finder.
     * 
     * @return
     */
    private MethodMetadata getFindAllMethodRenderMode() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(new AnnotatedJavaType(DATATABLES_CRITERIA_TYPE,
                new AnnotationMetadataBuilder(DATATABLES_PARAMS).build()));
        parameterTypes.add(new AnnotatedJavaType(entity,
                new AnnotationMetadataBuilder(MODEL_ATTRIBUTE).build()));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_REQUEST));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_RESPONSE));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(findAllMethodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);
        methodAnnotation.addStringAttribute("headers",
                "Accept=application/json");
        methodAnnotation.addStringAttribute("value", "/datatables/ajax");
        methodAnnotation.addStringAttribute("produces", "application/json");
        annotations.add(methodAnnotation);
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(SERVLET_EXCEPTION);
        throwsTypes.add(IO_EXCEPTION);

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        parameterNames.add(new JavaSymbolName(entityName));
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(RESPONSE_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildFindAllMethodBodyRenderMode(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, findAllMethodName, FIND_ALL_RETURN,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Build method body for <code>findAll</code> method for render-a-view
     * visualization mode
     * 
     * @param bodyBuilder
     */
    private void buildFindAllMethodBodyRenderMode(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Build call to FindByCriteria
        buildFindByCriteriaCall(bodyBuilder, false);

        buildPopulateDataSearchCallRenderMode(bodyBuilder);
    }

    /**
     * Build method-code to call Datatables.populateDataSearch method.
     * 
     * @param bodyBuilder
     */
    private void buildPopulateDataSearchCallRenderMode(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                "%s rows = %s(searchResult, %s, %s);",
                helper.getFinalTypeName(LIST_MAP_STRING_STRING),
                renderItemsMethodName.getSymbolName(),
                REQUEST_PARAM_NAME.getSymbolName(),
                RESPONSE_PARAM_NAME.getSymbolName()));

        /*
         * DataSet<Map<String, String>> dataSet = new DataSet<Map<String,
         * String>>(rows, totalRecords, recordsFound);
         */
        bodyBuilder.appendFormalLine(String.format(
                "%s dataSet = new %s(rows, totalRecords, recordsFound); ",
                helper.getFinalTypeName(DATA_SET_MAP_STRING_STRING),
                helper.getFinalTypeName(DATA_SET_MAP_STRING_STRING)));

        // return DatatablesResponse.build(dataSet,criterias);
        bodyBuilder.appendFormalLine(String.format(
                "return %s.build(dataSet,%s);",
                helper.getFinalTypeName(DATATABLES_RESPONSE),
                CRITERIA_PARAM_NAME.getSymbolName()));
    }

    /**
     * Build method-code to call Datatables.findByCriteria method. <br>
     * Generated code will depend on entity has JPAQuery metadata and if there
     * is a base search.
     * 
     * @param bodyBuilder
     * @param baseSearch
     * @return
     */
    private String buildFindByCriteriaCall(
            InvocableMemberBodyBuilder bodyBuilder, boolean baseSearch) {
        final String entityTypeName = helper.getFinalTypeName(entity);
        JavaType serachResult = new JavaType(
                SEARCH_RESULTS.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(entity));

        if (jpaQueryMetadata != null) {
            String filterByInfo = String.format("%s.%s()", entityTypeName,
                    jpaQueryMetadata.getFilterByMethodName().getSymbolName());
            String orderByInfo = String.format("%s.%s()", entityTypeName,
                    jpaQueryMetadata.getOrderByMethodName().getSymbolName());

            // SearchResults<Pet> searchResult =
            // DatatablesUtils.findByCriteria(Pet.class,
            // Pet.getFilterByAssociations(), Pet.getOrderByAssociations(),
            // Pet.entityManager(), criterias);

            if (baseSearch) {
                bodyBuilder
                        .appendFormalLine(String
                                .format("%s searchResult = %s.findByCriteria(entity, %s.%s(), %s, baseSearch);",
                                        helper.getFinalTypeName(serachResult),
                                        helper.getFinalTypeName(DATATABLES_UTILS),
                                        entityTypeName,
                                        entityEntityManagerMethod
                                                .getSymbolName(),
                                        CRITERIA_PARAM_NAME.getSymbolName()));

            }
            else {

                bodyBuilder
                        .appendFormalLine("// URL parameters are used as base search filters");
                // Enumeration<String> parameterNames =
                // (Enumeration<String>)request.getParameterNames();
                bodyBuilder.appendFormalLine(String.format(
                        "%s parameterNames = (%s) %s.getParameterNames();",
                        helper.getFinalTypeName(ENUMERATION_STRING),
                        helper.getFinalTypeName(ENUMERATION_STRING),
                        REQUEST_PARAM_NAME.getSymbolName()));
                // Map<String, Object> baseSearchValuesMap = getPropertyMap(pet,
                // parameterNames);
                bodyBuilder
                        .appendFormalLine(String
                                .format("%s baseSearchValuesMap = getPropertyMap(%s, parameterNames);",
                                        helper.getFinalTypeName(MAP_STRING_OBJECT),
                                        entityName));

                bodyBuilder
                        .appendFormalLine(String
                                .format("%s searchResult = %s.findByCriteria(%s.class, %s, %s, %s.%s(), %s, baseSearchValuesMap);",
                                        helper.getFinalTypeName(serachResult),
                                        helper.getFinalTypeName(DATATABLES_UTILS),
                                        entityTypeName, filterByInfo,
                                        orderByInfo, entityTypeName,
                                        entityEntityManagerMethod
                                                .getSymbolName(),
                                        CRITERIA_PARAM_NAME.getSymbolName()));
            }
        }
        else {

            // SearchResults<Pet> searchResult =
            // DatatablesUtils.findByCriteria(Pet.class,
            // Pet.getFilterByAssociations(), Pet.getOrderByAssociations(),
            // Pet.entityManager(), criterias);
            if (baseSearch) {
                bodyBuilder
                        .appendFormalLine(String
                                .format("%s searchResult = %s.findByCriteria(entity, %s.%s(), %s, baseSearch);",
                                        helper.getFinalTypeName(serachResult),
                                        helper.getFinalTypeName(DATATABLES_UTILS),
                                        entityTypeName,
                                        entityEntityManagerMethod
                                                .getSymbolName(),
                                        CRITERIA_PARAM_NAME.getSymbolName()));
            }
            else {

                bodyBuilder
                        .appendFormalLine("// URL parameters are used as base search filters");
                // Enumeration<String> parameterNames =
                // (Enumeration<String>)request.getParameterNames();
                bodyBuilder.appendFormalLine(String.format(
                        "%s parameterNames = (%s) %s.getParameterNames();",
                        helper.getFinalTypeName(ENUMERATION_STRING),
                        helper.getFinalTypeName(ENUMERATION_STRING),
                        REQUEST_PARAM_NAME.getSymbolName()));
                // Map<String, Object> baseSearchValuesMap = getPropertyMap(pet,
                // parameterNames);
                bodyBuilder
                        .appendFormalLine(String
                                .format("%s baseSearchValuesMap = getPropertyMap(%s, parameterNames);",
                                        helper.getFinalTypeName(MAP_STRING_OBJECT),
                                        entityName));

                bodyBuilder
                        .appendFormalLine(String
                                .format("%s searchResult = %s.findByCriteria(%s.class, %s.%s(), %s, baseSearchValuesMap);",
                                        helper.getFinalTypeName(serachResult),
                                        helper.getFinalTypeName(DATATABLES_UTILS),
                                        entityTypeName, entityTypeName,
                                        entityEntityManagerMethod
                                                .getSymbolName(),
                                        CRITERIA_PARAM_NAME.getSymbolName()));
            }

        }

        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// Get datatables required counts");
        // long totalRecords = searchResult.getTotalCount();
        bodyBuilder
                .appendFormalLine("long totalRecords = searchResult.getTotalCount();");
        // long recordsFound = findResult.getResultsCount();
        bodyBuilder
                .appendFormalLine("long recordsFound = searchResult.getResultsCount();");
        return entityTypeName;
    }

    /**
     * Build method body for <code>findAll</code> method
     * 
     * @param bodyBuilder
     */
    private void buildFindAllDataMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // Build call to FindByCriteria
        buildFindByCriteriaCall(bodyBuilder, false);

        buildPopulateDataSearchCall(bodyBuilder);
    }

    /**
     * Build method-code to perform a call to Datatables.populateDateSet method
     * 
     * @param bodyBuilder
     */
    private void buildPopulateDataSearchCall(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// Entity pk field name");

        // String pkFieldName = "id";
        bodyBuilder.appendFormalLine(String.format(
                "String pkFieldName = \"%s\";", entityIdentifier.getFieldName()
                        .getSymbolName()));

        bodyBuilder.appendFormalLine("");
        /*
         * DataSet<Map<String, String>> dataSet =
         * DatatablesUtils.populateDataSet(searchResult.getResult(),
         * pkFieldName, totalRecords, recordsFound, criterias.getColumnDefs(),
         * null, conversionService_datatables);
         */

        bodyBuilder
                .appendFormalLine(String
                        .format("%s dataSet = %s.populateDataSet(searchResult.getResults(), pkFieldName, totalRecords, recordsFound, criterias.getColumnDefs(), null, %s); ",
                                helper.getFinalTypeName(DATA_SET_MAP_STRING_STRING),
                                helper.getFinalTypeName(DATATABLES_UTILS),
                                getConversionServiceField().getFieldName()
                                        .getSymbolName()));

        // return DatatablesResponse.build(dataSet,criterias);
        bodyBuilder.appendFormalLine(String.format(
                "return %s.build(dataSet,%s);",
                helper.getFinalTypeName(DATATABLES_RESPONSE),
                CRITERIA_PARAM_NAME.getSymbolName()));
    }

    /**
     * Create metadata for auto-wired convertionService field.
     * 
     * @return a FieldMetadata object
     */
    public FieldMetadata getConversionServiceField() {
        if (conversionService == null) {
            JavaSymbolName curName = new JavaSymbolName(
                    "conversionService_datatables");
            // Check if field exist
            FieldMetadata currentField = governorTypeDetails
                    .getDeclaredField(curName);
            if (currentField != null) {
                if (currentField.getAnnotation(AUTOWIRED) == null
                        || !currentField.getFieldType().equals(
                                CONVERSION_SERVICE)) {
                    // No compatible field: look for new name
                    currentField = null;
                    JavaSymbolName newName = curName;
                    int i = 1;
                    while (governorTypeDetails.getDeclaredField(newName) != null) {
                        newName = new JavaSymbolName(curName.getSymbolName()
                                .concat(StringUtils.repeat('_', i)));
                        i++;
                    }
                    curName = newName;
                }
            }
            if (currentField != null) {
                conversionService = currentField;
            }
            else {
                // create field
                List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                        1);
                annotations.add(new AnnotationMetadataBuilder(AUTOWIRED));
                // Using the FieldMetadataBuilder to create the field
                // definition.
                final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                        getId(), Modifier.PUBLIC, annotations, curName, // Field
                        CONVERSION_SERVICE); // Field type
                conversionService = fieldBuilder.build(); // Build and return a
                                                          // FieldMetadata
                // instance
            }
        }
        return conversionService;
    }

    /**
     * Informs if a method is already defined on class
     * 
     * @param methodName
     * @param paramTypes
     * @return
     */
    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {

        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }

    // Typically, no changes are required beyond this point

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("ajax", String.valueOf(annotationValues.isAjax()));
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * @return related JPA entity
     */
    public JavaType getEntity() {
        return entity;
    }

    /**
     * @return {@link GvNIXDatatables} annotation values
     */
    public DatatablesAnnotationValues getAnnotationValues() {
        return annotationValues;
    }

    /**
     * @return {@link RooWebScaffold} annotation values
     */
    public WebScaffoldAnnotationValues getWebScaffoldAnnotationValues() {
        return webScaffoldAnnotationValues;
    }

    /**
     * @return controllers use AJAX data mode or not (DOM)
     */
    public boolean isAjax() {
        return annotationValues.isAjax();
    }

    /**
     * @return informs if controllers has JPA-Batch-operations support available
     */
    public boolean hasJpaBatchSupport() {
        return webJpaBatchMetadata != null;
    }

    /**
     * @return datatables shows a standard list or use render-a-view mode
     */
    public boolean isStantardMode() {
        return annotationValues.isStandardMode();
    }

    /**
     * @return information about dynamic finder registered on the controller
     */
    public Map<FinderMetadataDetails, QueryHolderTokens> getFindersRegistered() {
        return findersRegistered;
    }
}
