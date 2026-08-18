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
                type = tf.constructSpecializedType(type, valueClass);
            } catch (IllegalArgumentException iae) {
                // Tolerate primitive <-> wrapper mismatches by ignoring refinement
                Class<?> currRaw = type.getRawClass();
                if (!((currRaw == Integer.TYPE && valueClass == Integer.class)
                        || (currRaw == Integer.class && valueClass == Integer.TYPE)
                        || (currRaw == Long.TYPE && valueClass == Long.class)
                        || (currRaw == Long.class && valueClass == Long.TYPE)
                        || (currRaw == Boolean.TYPE && valueClass == Boolean.class)
                        || (currRaw == Boolean.class && valueClass == Boolean.TYPE)
                        || (currRaw == Double.TYPE && valueClass == Double.class)
                        || (currRaw == Double.class && valueClass == Double.TYPE)
                        || (currRaw == Float.TYPE && valueClass == Float.class)
                        || (currRaw == Float.class && valueClass == Float.TYPE)
                        || (currRaw == Short.TYPE && valueClass == Short.class)
                        || (currRaw == Short.class && valueClass == Short.TYPE)
                        || (currRaw == Byte.TYPE && valueClass == Byte.class)
                        || (currRaw == Byte.class && valueClass == Byte.TYPE)
                        || (currRaw == Character.TYPE && valueClass == Character.class)
                        || (currRaw == Character.class && valueClass == Character.TYPE))) {
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
                if (!keyType.hasRawClass(keyClass)) {
                    try {
                        keyType = tf.constructSpecializedType(keyType, keyClass);
                    } catch (IllegalArgumentException iae) {
                        Class<?> currRaw = keyType.getRawClass();
                        if (!((currRaw == Integer.TYPE && keyClass == Integer.class)
                                || (currRaw == Integer.class && keyClass == Integer.TYPE)
                                || (currRaw == Long.TYPE && keyClass == Long.class)
                                || (currRaw == Long.class && keyClass == Long.TYPE)
                                || (currRaw == Boolean.TYPE && keyClass == Boolean.class)
                                || (currRaw == Boolean.class && keyClass == Boolean.TYPE)
                                || (currRaw == Double.TYPE && keyClass == Double.class)
                                || (currRaw == Double.class && keyClass == Double.TYPE)
                                || (currRaw == Float.TYPE && keyClass == Float.class)
                                || (currRaw == Float.class && keyClass == Float.TYPE)
                                || (currRaw == Short.TYPE && keyClass == Short.class)
                                || (currRaw == Short.class && keyClass == Short.TYPE)
                                || (currRaw == Byte.TYPE && keyClass == Byte.class)
                                || (currRaw == Byte.class && keyClass == Byte.TYPE)
                                || (currRaw == Character.TYPE && keyClass == Character.class)
                                || (currRaw == Character.class && keyClass == Character.TYPE))) {
                            throw new JsonMappingException(null,
                                    String.format("Failed to narrow key type of %s with concrete-type annotation (value %s), from '%s': %s",
                                            type, keyClass.getName(), a.getName(), iae.getMessage()),
                                            iae);
                        }
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
                if (!contentType.hasRawClass(contentClass)) {
                    try {
                        contentType = tf.constructSpecializedType(contentType, contentClass);
                        type = type.withContentType(contentType);
                    } catch (IllegalArgumentException iae) {
                        Class<?> currRaw = contentType.getRawClass();
                        if (!((currRaw == Integer.TYPE && contentClass == Integer.class)
                                || (currRaw == Integer.class && contentClass == Integer.TYPE)
                                || (currRaw == Long.TYPE && contentClass == Long.class)
                                || (currRaw == Long.class && contentClass == Long.TYPE)
                                || (currRaw == Boolean.TYPE && contentClass == Boolean.class)
                                || (currRaw == Boolean.class && contentClass == Boolean.TYPE)
                                || (currRaw == Double.TYPE && contentClass == Double.class)
                                || (currRaw == Double.class && contentClass == Double.TYPE)
                                || (currRaw == Float.TYPE && contentClass == Float.class)
                                || (currRaw == Float.class && contentClass == Float.TYPE)
                                || (currRaw == Short.TYPE && contentClass == Short.class)
                                || (currRaw == Short.class && contentClass == Short.TYPE)
                                || (currRaw == Byte.TYPE && contentClass == Byte.class)
                                || (currRaw == Byte.class && contentClass == Byte.TYPE)
                                || (currRaw == Character.TYPE && contentClass == Character.class)
                                || (currRaw == Character.class && contentClass == Character.TYPE))) {
                            throw new JsonMappingException(null,
                                    String.format("Failed to narrow value type of %s with concrete-type annotation (value %s), from '%s': %s",
                                            type, contentClass.getName(), a.getName(), iae.getMessage()),
                            iae);
                        }
                    }
                } else {
                    type = type.withContentType(contentType);
                }
            }
        }
        return type;
    }