/**
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package me.snowdrop.istio.annotator;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sun.codemodel.*;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.builder.annotations.Inline;
import io.sundr.transform.annotations.VelocityTransformation;
import io.sundr.transform.annotations.VelocityTransformations;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.snowdrop.istio.api.IstioSpec;
import me.snowdrop.istio.api.internal.ClassWithInterfaceFieldsDeserializer;
import me.snowdrop.istio.api.internal.IstioApiVersion;
import me.snowdrop.istio.api.internal.IstioKind;
import me.snowdrop.istio.api.internal.IstioSpecRegistry;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class IstioTypeAnnotator extends Jackson2Annotator {

    private static final String BUILDER_PACKAGE = "io.fabric8.kubernetes.api.builder";

    private static final String DONEABLE_CLASS_NAME = "io.fabric8.kubernetes.api.model.Doneable";

    private static final String OBJECT_META_CLASS_NAME = "io.fabric8.kubernetes.api.model.ObjectMeta";

    private static final String IS_INTERFACE_FIELD = "isInterface";

    private static final String EXISTING_JAVA_TYPE_FIELD = "existingJavaType";

    private final JDefinedClass doneableClass;

    private final JDefinedClass objectMetaClass;

    static {
        final String strict = System.getenv("ISTIO_STRICT");
        if ("true".equals(strict)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                final String unvisitedCRDs = IstioSpecRegistry.unvisitedCRDNames();
                if (!unvisitedCRDs.isEmpty()) {
                    throw new IllegalStateException("The following CRDs were not visited:\n" + unvisitedCRDs);
                }
            }));
        }
    }

    public IstioTypeAnnotator(GenerationConfig generationConfig) {
        super(generationConfig);
        String className = DONEABLE_CLASS_NAME;
        try {
            doneableClass = new JCodeModel()._class(className);
            className = OBJECT_META_CLASS_NAME;
            objectMetaClass = new JCodeModel()._class(className);
        } catch (JClassAlreadyExistsException e) {
            throw new IllegalStateException("Couldn't load " + className);
        }
    }

    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        JAnnotationArrayMember annotationValue = clazz.annotate(JsonPropertyOrder.class).paramArray("value");
        annotationValue.param("apiVersion");
        annotationValue.param("kind");
        annotationValue.param("metadata");

        final Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
        while (fields.hasNext()) {
            final Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            switch (key) {
                case "kind":
                case "metadata":
                case "apiVersion":
                    break;
                case "deprecatedAllowOrigin":
                    key = "allowOrigin";
                default:
                    annotationValue.param(key);
            }
        }

        final String pkgName = clazz.getPackage().name();
        final int i = pkgName.lastIndexOf('.');
        final String version = pkgName.substring(i + 1);
        if (version.startsWith("v")) {
            final Optional<IstioSpecRegistry.CRDInfo> kind = IstioSpecRegistry.getCRDInfo(clazz.name(), version);
            kind.ifPresent(k -> {
                clazz._implements(IstioSpec.class);
                clazz.annotate(IstioKind.class).param("name", k.getKind()).param("plural", k.getPlural());
                clazz.annotate(IstioApiVersion.class).param("value", k.getAPIVersion());
            });
        }

        clazz.annotate(ToString.class);
        clazz.annotate(EqualsAndHashCode.class);
        JAnnotationUse buildable = clazz.annotate(Buildable.class)
              .param("editableEnabled", false)
              .param("generateBuilderPackage", true)
              .param("builderPackage", BUILDER_PACKAGE);

        buildable.paramArray("inline").annotate(Inline.class)
              .param("type", doneableClass)
              .param("prefix", "Doneable")
              .param("value", "done");

        buildable.paramArray("refs").annotate(BuildableReference.class)
              .param("value", objectMetaClass);

        if (clazz.name().endsWith("Spec")) {
            JAnnotationArrayMember arrayMember = clazz.annotate(VelocityTransformations.class)
                  .paramArray("value");
            arrayMember.annotate(VelocityTransformation.class).param("value", "/istio-resource.vm");
            arrayMember.annotate(VelocityTransformation.class).param("value", "/istio-resource-list.vm");
            arrayMember.annotate(VelocityTransformation.class).param("value", "/istio-manifest.vm")
                  .param("outputPath", "crd.properties").param("gather", true);
            arrayMember.annotate(VelocityTransformation.class).param("value", "/istio-mappings-provider.vm")
                  .param("outputPath", Paths.get("me", "snowdrop", "istio", "api", "model",
                        "IstioResourceMappingsProvider.java").toString())
                  .param("gather", true);
        }
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        propertyName = propertyName.equals("deprecatedAllowOrigin") ? "allowOrigin" : propertyName;
        super.propertyField(field, clazz, propertyName, propertyNode);
        if (propertyNode.hasNonNull(IS_INTERFACE_FIELD)) {
            field.annotate(JsonUnwrapped.class);

            // todo: fix me, this won't work if a type has several fields using interfaces
            String interfaceFQN = propertyNode.get(EXISTING_JAVA_TYPE_FIELD).asText();

            // create interface if we haven't done it yet
            try {
                final JDefinedClass fieldInterface = clazz._interface(interfaceFQN.substring(interfaceFQN.lastIndexOf('.') + 1));
                fieldInterface._extends(Serializable.class);
            } catch (JClassAlreadyExistsException e) {
                throw new RuntimeException(e);
            }
            annotateIfNotDone(clazz, ClassWithInterfaceFieldsDeserializer.class);
        }
    }

    public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
        // overridden to avoid annotating the getter
    }

    public void propertySetter(JMethod setter, JDefinedClass clazz, String propertyName) {
        // overridden to avoid annotating the setter
    }

    private Set<JDefinedClass> annotated = new HashSet<>();

    private void annotateIfNotDone(JDefinedClass clazz, Class<? extends JsonDeserializer> deserializerClass) {
        if (!annotated.contains(clazz)) {
            clazz.annotate(JsonDeserialize.class).param("using", deserializerClass);
            annotated.add(clazz);
        }
    }
}
