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
                if ((currRaw.isPrimitive() &&
                        ((currRaw == Integer.TYPE && valueClass == Integer.class)
                         || (currRaw == Long.TYPE && valueClass == Long.class)
                         || (currRaw == Boolean.TYPE && valueClass == Boolean.class)
                         || (currRaw == Double.TYPE && valueClass == Double.class)
                         || (currRaw == Float.TYPE && valueClass == Float.class)
                         || (currRaw == Short.TYPE && valueClass == Short.class)
                         || (currRaw == Byte.TYPE && valueClass == Byte.class)
                         || (currRaw == Character.TYPE && valueClass == Character.class)))
                    || (valueClass.isPrimitive() &&
                        ((valueClass == Integer.TYPE && currRaw == Integer.class)
                         || (valueClass == Long.TYPE && currRaw == Long.class)
                         || (valueClass == Boolean.TYPE && currRaw == Boolean.class)
                         || (valueClass == Double.TYPE && currRaw == Double.class)
                         || (valueClass == Float.TYPE && currRaw == Float.class)
                         || (valueClass == Short.TYPE && currRaw == Short.class)
                         || (valueClass == Byte.TYPE && currRaw == Byte.class)
                         || (valueClass == Character.TYPE && currRaw == Character.class))))) {
                    // Ignore primitive/wrapper refinement mismatch
                } else {
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
                    if ((currRaw.isPrimitive() &&
                            ((currRaw == Integer.TYPE && keyClass == Integer.class)
                             || (currRaw == Long.TYPE && keyClass == Long.class)
                             || (currRaw == Boolean.TYPE && keyClass == Boolean.class)
                             || (currRaw == Double.TYPE && keyClass == Double.class)
                             || (currRaw == Float.TYPE && keyClass == Float.class)
                             || (currRaw == Short.TYPE && keyClass == Short.class)
                             || (currRaw == Byte.TYPE && keyClass == Byte.class)
                             || (currRaw == Character.TYPE && keyClass == Character.class)))
                        || (keyClass.isPrimitive() &&
                            ((keyClass == Integer.TYPE && currRaw == Integer.class)
                             || (keyClass == Long.TYPE && currRaw == Long.class)
                             || (keyClass == Boolean.TYPE && currRaw == Boolean.class)
                             || (keyClass == Double.TYPE && currRaw == Double.class)
                             || (keyClass == Float.TYPE && currRaw == Float.class)
                             || (keyClass == Short.TYPE && currRaw == Short.class)
                             || (keyClass == Byte.TYPE && currRaw == Byte.class)
                             || (keyClass == Character.TYPE && currRaw == Character.class))))) {
                        // Ignore primitive/wrapper refinement mismatch
                    } else {
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
                    if ((currRaw.isPrimitive() &&
                            ((currRaw == Integer.TYPE && contentClass == Integer.class)
                             || (currRaw == Long.TYPE && contentClass == Long.class)
                             || (currRaw == Boolean.TYPE && contentClass == Boolean.class)
                             || (currRaw == Double.TYPE && contentClass == Double.class)
                             || (currRaw == Float.TYPE && contentClass == Float.class)
                             || (currRaw == Short.TYPE && contentClass == Short.class)
                             || (currRaw == Byte.TYPE && contentClass == Byte.class)
                             || (currRaw == Character.TYPE && contentClass == Character.class)))
                        || (contentClass.isPrimitive() &&
                            ((contentClass == Integer.TYPE && currRaw == Integer.class)
                             || (contentClass == Long.TYPE && currRaw == Long.class)
                             || (contentClass == Boolean.TYPE && currRaw == Boolean.class)
                             || (contentClass == Double.TYPE && currRaw == Double.class)
                             || (contentClass == Float.TYPE && currRaw == Float.class)
                             || (contentClass == Short.TYPE && currRaw == Short.class)
                             || (contentClass == Byte.TYPE && currRaw == Byte.class)
                             || (contentClass == Character.TYPE && currRaw == Character.class))))) {
                        // Ignore primitive/wrapper refinement mismatch
                    } else {
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