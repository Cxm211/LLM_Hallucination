public JavaType refineDeserializationType(final MapperConfig<?> config,
            final Annotated a, final JavaType baseType) throws JsonMappingException
    {
        JavaType type = baseType;
        final TypeFactory tf = config.getTypeFactory();

        final JsonDeserialize jsonDeser = _findAnnotation(a, JsonDeserialize.class);
        
        // Ok: start by refining the main type itself; common to all types
        final Class<?> valueClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.as());
        if (valueClass != null) {
            if (type.hasRawClass(valueClass)) {
                type = type.withStaticTyping();
            } else {
                Class<?> currRaw = type.getRawClass();
                try {
                    if (valueClass.isAssignableFrom(currRaw)) { // generalization
                        type = tf.constructGeneralizedType(type, valueClass);
                    } else if (currRaw.isAssignableFrom(valueClass)) { // specialization
                        type = tf.constructSpecializedType(type, valueClass);
                    } else {
                        throw new JsonMappingException(null,
                                String.format("Can not refine deserialization type %s into %s; types not related",
                                        type, valueClass.getName()));
                    }
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to narrow type %s with annotation (value %s), from '%s': %s",
                                    type, valueClass.getName(), a.getName(), iae.getMessage()),
                                    iae);
                }
            }
        }
        // Then further processing for container types

        // First, key type (for Maps, Map-like types):
        if (type.isMapLikeType()) {
            JavaType keyType = type.getKeyType();
            final Class<?> keyClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.keyAs());
            if (keyClass != null) {
                if (keyType.hasRawClass(keyClass)) {
                    keyType = keyType.withStaticTyping();
                } else {
                    Class<?> currRaw = keyType.getRawClass();
                    try {
                        if (keyClass.isAssignableFrom(currRaw)) { // generalization
                            keyType = tf.constructGeneralizedType(keyType, keyClass);
                        } else if (currRaw.isAssignableFrom(keyClass)) { // specialization
                            keyType = tf.constructSpecializedType(keyType, keyClass);
                        } else {
                            throw new JsonMappingException(null,
                                    String.format("Can not refine deserialization key type %s into %s; types not related",
                                            keyType, keyClass.getName()));
                        }
                    } catch (IllegalArgumentException iae) {
                        throw new JsonMappingException(null,
                                String.format("Failed to narrow key type of %s with concrete-type annotation (value %s), from '%s': %s",
                                        type, keyClass.getName(), a.getName(), iae.getMessage()),
                                        iae);
                    }
                }
                type = ((MapLikeType) type).withKeyType(keyType);
            }
        }

        JavaType contentType = type.getContentType();
        if (contentType != null) { // collection[like], map[like], array, reference
            // And then value types for all containers:
            final Class<?> contentClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.contentAs());
            if (contentClass != null) {
                if (contentType.hasRawClass(contentClass)) {
                    contentType = contentType.withStaticTyping();
                } else {
                    Class<?> currRaw = contentType.getRawClass();
                    try {
                        if (contentClass.isAssignableFrom(currRaw)) { // generalization
                            contentType = tf.constructGeneralizedType(contentType, contentClass);
                        } else if (currRaw.isAssignableFrom(contentClass)) { // specialization
                            contentType = tf.constructSpecializedType(contentType, contentClass);
                        } else {
                            throw new JsonMappingException(null,
                                    String.format("Can not refine deserialization value type %s into %s; types not related",
                                            contentType, contentClass.getName()));
                        }
                    } catch (IllegalArgumentException iae) {
                        throw new JsonMappingException(null,
                                String.format("Failed to narrow value type of %s with concrete-type annotation (value %s), from '%s': %s",
                                        type, contentClass.getName(), a.getName(), iae.getMessage()),
                                iae);
                    }
                }
                type = type.withContentType(contentType);
            }
        }
        return type;
    }