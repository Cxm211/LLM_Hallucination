public JavaType refineDeserializationType(final MapperConfig<?> config,
        final Annotated a, final JavaType baseType) throws JsonMappingException
{
    JavaType type = baseType;
    final TypeFactory tf = config.getTypeFactory();

    final JsonDeserialize jsonDeser = _findAnnotation(a, JsonDeserialize.class);

    final Class<?> valueClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.as());
    if (valueClass != null && !type.hasRawClass(valueClass)) {
        try {
            type = tf.constructSpecializedType(type, valueClass);
        } catch (IllegalArgumentException iae) {
            throw new JsonMappingException(null,
                    String.format("Failed to narrow type %s with annotation (value %s), from '%s': %s",
                            type, valueClass.getName(), a.getName(), iae.getMessage()),
                            iae);
        }
    }

    if (type.isMapLikeType()) {
        JavaType keyType = type.getKeyType();
        final Class<?> keyClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.keyAs());
        if (keyClass != null) {
            if (keyType.hasRawClass(keyClass)) {
                // same type, no refinement needed
            } else {
                try {
                    keyType = tf.constructSpecializedType(keyType, keyClass);
                    type = ((MapLikeType) type).withKeyType(keyType);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to narrow key type of %s with concrete-type annotation (value %s), from '%s': %s",
                                    type, keyClass.getName(), a.getName(), iae.getMessage()),
                                    iae);
                }
            }
        }
    }
    JavaType contentType = type.getContentType();
    if (contentType != null) {
        final Class<?> contentClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.contentAs());
        if (contentClass != null) {
            if (contentType.hasRawClass(contentClass)) {
                // same type, no refinement needed
            } else {
                try {
                    contentType = tf.constructSpecializedType(contentType, contentClass);
                    type = type.withContentType(contentType);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to narrow value type of %s with concrete-type annotation (value %s), from '%s': %s",
                                    type, contentClass.getName(), a.getName(), iae.getMessage()),
                            iae);
                }
            }
        }
    }
    return type;
}