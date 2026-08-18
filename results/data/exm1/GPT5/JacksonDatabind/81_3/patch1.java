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
                boolean primitiveWrapper = false;
                if (currRaw.isPrimitive()) {
                    try {
                        java.lang.reflect.Field f = valueClass.getField("TYPE");
                        Object v = f.get(null);
                        if (v == currRaw) {
                            primitiveWrapper = true;
                        }
                    } catch (Exception e) { }
                } else if (valueClass.isPrimitive()) {
                    try {
                        java.lang.reflect.Field f = currRaw.getField("TYPE");
                        Object v = f.get(null);
                        if (v == valueClass) {
                            primitiveWrapper = true;
                        }
                    } catch (Exception e) { }
                }
                if (!primitiveWrapper) {
                    type = tf.constructSpecializedType(type, valueClass);
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
                    boolean primitiveWrapper = false;
                    if (currRaw.isPrimitive()) {
                        try {
                            java.lang.reflect.Field f = keyClass.getField("TYPE");
                            Object v = f.get(null);
                            if (v == currRaw) {
                                primitiveWrapper = true;
                            }
                        } catch (Exception e) { }
                    } else if (keyClass.isPrimitive()) {
                        try {
                            java.lang.reflect.Field f = currRaw.getField("TYPE");
                            Object v = f.get(null);
                            if (v == keyClass) {
                                primitiveWrapper = true;
                            }
                        } catch (Exception e) { }
                    }
                    if (!primitiveWrapper) {
                        keyType = tf.constructSpecializedType(keyType, keyClass);
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
                    boolean primitiveWrapper = false;
                    if (currRaw.isPrimitive()) {
                        try {
                            java.lang.reflect.Field f = contentClass.getField("TYPE");
                            Object v = f.get(null);
                            if (v == currRaw) {
                                primitiveWrapper = true;
                            }
                        } catch (Exception e) { }
                    } else if (contentClass.isPrimitive()) {
                        try {
                            java.lang.reflect.Field f = currRaw.getField("TYPE");
                            Object v = f.get(null);
                            if (v == contentClass) {
                                primitiveWrapper = true;
                            }
                        } catch (Exception e) { }
                    }
                    if (!primitiveWrapper) {
                        contentType = tf.constructSpecializedType(contentType, contentClass);
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