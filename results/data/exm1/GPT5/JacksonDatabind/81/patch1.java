public JavaType refineDeserializationType(final MapperConfig<?> config,
            final Annotated a, final JavaType baseType) throws JsonMappingException
    {
        JavaType type = baseType;
        final TypeFactory tf = config.getTypeFactory();

        final JsonDeserialize jsonDeser = _findAnnotation(a, JsonDeserialize.class);
        
        // Ok: start by refining the main type itself; common to all types
        final Class<?> valueClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.as());
        if ((valueClass != null) && !type.hasRawClass(valueClass)) {
            try {
                Class<?> currRaw = type.getRawClass();
                if (valueClass.isAssignableFrom(currRaw)) {
                    type = tf.constructGeneralizedType(type, valueClass);
                } else if (currRaw.isAssignableFrom(valueClass)) {
                    type = tf.constructSpecializedType(type, valueClass);
                } else {
                    throw new IllegalArgumentException(String.format(
                            "Types not related: can not refine deserialization type %s into %s",
                            type, valueClass.getName()));
                }
            } catch (IllegalArgumentException iae) {
                throw new JsonMappingException(null,
                        String.format("Failed to narrow type %s with annotation (value %s), from '%s': %s",
                                type, valueClass.getName(), a.getName(), iae.getMessage()),
                                iae);
            }
        }
        // Then further processing for container types

        // First, key type (for Maps, Map-like types):
        if (type.isMapLikeType()) {
            JavaType keyType = type.getKeyType();
            final Class<?> keyClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.keyAs());
            if (keyClass != null) {
                try {
                    Class<?> currRaw = keyType.getRawClass();
                    if (keyClass.isAssignableFrom(currRaw)) {
                        keyType = tf.constructGeneralizedType(keyType, keyClass);
                    } else if (currRaw.isAssignableFrom(keyClass)) {
                        keyType = tf.constructSpecializedType(keyType, keyClass);
                    } else {
                        throw new IllegalArgumentException(String.format(
                                "Types not related: can not refine deserialization key type %s into %s",
                                keyType, keyClass.getName()));
                    }
                    type = ((MapLikeType) type).withKeyType(keyType);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to narrow key type of %s with concrete-type annotation (value %s), from '%s': %s",
                                    type, keyClass.getName(), a.getName(), iae.getMessage()),
                                    iae);
                }
            }
        }
        JavaType contentType = type.getContentType();
        if (contentType != null) { // collection[like], map[like], array, reference
            // And then value types for all containers:
            final Class<?> contentClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.contentAs());
            if (contentClass != null) {
                try {
                    Class<?> currRaw = contentType.getRawClass();
                    if (contentClass.isAssignableFrom(currRaw)) {
                        contentType = tf.constructGeneralizedType(contentType, contentClass);
                    } else if (currRaw.isAssignableFrom(contentClass)) {
                        contentType = tf.constructSpecializedType(contentType, contentClass);
                    } else {
                        throw new IllegalArgumentException(String.format(
                                "Types not related: can not refine deserialization content type %s into %s",
                                contentType, contentClass.getName()));
                    }
                    type = type.withContentType(contentType);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to narrow value type of %s with concrete-type annotation (value %s), from '%s': %s",
                                    type, contentClass.getName(), a.getName(), iae.getMessage()),
                            iae);
                }
            }
        }
        return type;
    }