    public JavaType refineSerializationType(final MapperConfig<?> config,
            final Annotated a, final JavaType baseType) throws JsonMappingException
    {
        JavaType type = baseType;
        final TypeFactory tf = config.getTypeFactory();

        final JsonSerialize jsonSer = _findAnnotation(a, JsonSerialize.class);
        
        // Ok: start by refining the main type itself; common to all types

        final Class<?> serClass = (jsonSer == null) ? null : _classIfExplicit(jsonSer.as());
        if (serClass != null) {
            if (type.hasRawClass(serClass)) {
                // 30-Nov-2015, tatu: As per [databind#1023], need to allow forcing of
                //    static typing this way
                type = type.withStaticTyping();
            } else {
                Class<?> currRaw = type.getRawClass();
                // Check for primitive/wrapper pair
                boolean primitiveWrapperPair = false;
                if (currRaw.isPrimitive() != serClass.isPrimitive()) {
                    Class<?> prim = currRaw.isPrimitive() ? currRaw : serClass;
                    Class<?> other = currRaw.isPrimitive() ? serClass : currRaw;
                    primitiveWrapperPair = (prim == Integer.TYPE && other == Integer.class) ||
                        (prim == Long.TYPE && other == Long.class) ||
                        (prim == Double.TYPE && other == Double.class) ||
                        (prim == Float.TYPE && other == Float.class) ||
                        (prim == Boolean.TYPE && other == Boolean.class) ||
                        (prim == Byte.TYPE && other == Byte.class) ||
                        (prim == Short.TYPE && other == Short.class) ||
                        (prim == Character.TYPE && other == Character.class) ||
                        (prim == Void.TYPE && other == Void.class);
                }
                if (primitiveWrapperPair) {
                    // ignore; do nothing
                } else {
                    try {
                        // 11-Oct-2015, tatu: For deser, we call `TypeFactory.constructSpecializedType()`,
                        //   may be needed here too in future?
                        if (serClass.isAssignableFrom(currRaw)) { // common case
                            type = tf.constructGeneralizedType(type, serClass);
                        } else if (currRaw.isAssignableFrom(serClass)) { // specialization, ok as well
                            type = tf.constructSpecializedType(type, serClass);
                            // 27-Apr-2017, tatu: [databind#1592] ignore primitive<->wrapper refinements
                        } else {
                            throw new JsonMappingException(null,
                                    String.format("Can not refine serialization type %s into %s; types not related",
                                            type, serClass.getName()));
                        }
                    } catch (IllegalArgumentException iae) {
                        throw new JsonMappingException(null,
                                String.format("Failed to widen type %s with annotation (value %s), from '%s': %s",
                                        type, serClass.getName(), a.getName(), iae.getMessage()),
                                        iae);
                    }
                }
            }
        }
        // Then further processing for container types

        // First, key type (for Maps, Map-like types):
        if (type.isMapLikeType()) {
            JavaType keyType = type.getKeyType();
            final Class<?> keyClass = (jsonSer == null) ? null : _classIfExplicit(jsonSer.keyAs());
            if (keyClass != null) {
                if (keyType.hasRawClass(keyClass)) {
                    keyType = keyType.withStaticTyping();
                } else {
                    Class<?> currRaw = keyType.getRawClass();
                    // Check for primitive/wrapper pair
                    boolean keyPrimitiveWrapperPair = false;
                    if (currRaw.isPrimitive() != keyClass.isPrimitive()) {
                        Class<?> prim = currRaw.isPrimitive() ? currRaw : keyClass;
                        Class<?> other = currRaw.isPrimitive() ? keyClass : currRaw;
                        keyPrimitiveWrapperPair = (prim == Integer.TYPE && other == Integer.class) ||
                            (prim == Long.TYPE && other == Long.class) ||
                            (prim == Double.TYPE && other == Double.class) ||
                            (prim == Float.TYPE && other == Float.class) ||
                            (prim == Boolean.TYPE && other == Boolean.class) ||
                            (prim == Byte.TYPE && other == Byte.class) ||
                            (prim == Short.TYPE && other == Short.class) ||
                            (prim == Character.TYPE && other == Character.class) ||
                            (prim == Void.TYPE && other == Void.class);
                    }
                    if (keyPrimitiveWrapperPair) {
                        // ignore; do nothing
                    } else {
                        try {
                            // 19-May-2016, tatu: As per [databind#1231], [databind#1178] may need to actually
                            //   specialize (narrow) type sometimes, even if more commonly opposite
                            //   is needed.
                            if (keyClass.isAssignableFrom(currRaw)) { // common case
                                keyType = tf.constructGeneralizedType(keyType, keyClass);
                            } else if (currRaw.isAssignableFrom(keyClass)) { // specialization, ok as well
                                keyType = tf.constructSpecializedType(keyType, keyClass);
                                // 27-Apr-2017, tatu: [databind#1592] ignore primitive<->wrapper refinements
                            } else {
                                throw new JsonMappingException(null,
                                        String.format("Can not refine serialization key type %s into %s; types not related",
                                                keyType, keyClass.getName()));
                            }
                        } catch (IllegalArgumentException iae) {
                            throw new JsonMappingException(null,
                                    String.format("Failed to widen key type of %s with concrete-type annotation (value %s), from '%s': %s",
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
           final Class<?> contentClass = (jsonSer == null) ? null : _classIfExplicit(jsonSer.contentAs());
           if (contentClass != null) {
               if (contentType.hasRawClass(contentClass)) {
                   contentType = contentType.withStaticTyping();
               } else {
                   // 03-Apr-2016, tatu: As per [databind#1178], may need to actually
                   //   specialize (narrow) type sometimes, even if more commonly opposite
                   //   is needed.
                   Class<?> currRaw = contentType.getRawClass();
                   // Check for primitive/wrapper pair
                   boolean contentPrimitiveWrapperPair = false;
                   if (currRaw.isPrimitive() != contentClass.isPrimitive()) {
                       Class<?> prim = currRaw.isPrimitive() ? currRaw : contentClass;
                       Class<?> other = currRaw.isPrimitive() ? contentClass : currRaw;
                       contentPrimitiveWrapperPair = (prim == Integer.TYPE && other == Integer.class) ||
                           (prim == Long.TYPE && other == Long.class) ||
                           (prim == Double.TYPE && other == Double.class) ||
                           (prim == Float.TYPE && other == Float.class) ||
                           (prim == Boolean.TYPE && other == Boolean.class) ||
                           (prim == Byte.TYPE && other == Byte.class) ||
                           (prim == Short.TYPE && other == Short.class) ||
                           (prim == Character.TYPE && other == Character.class) ||
                           (prim == Void.TYPE && other == Void.class);
                   }
                   if (contentPrimitiveWrapperPair) {
                       // ignore; do nothing
                   } else {
                       try {
                           if (contentClass.isAssignableFrom(currRaw)) { // common case
                               contentType = tf.constructGeneralizedType(contentType, contentClass);
                           } else if (currRaw.isAssignableFrom(contentClass)) { // specialization, ok as well
                               contentType = tf.constructSpecializedType(contentType, contentClass);
                               // 27-Apr-2017, tatu: [databind#1592] ignore primitive<->wrapper refinements
                           } else {
                               throw new JsonMappingException(null,
                                       String.format("Can not refine serialization content type %s into %s; types not related",
                                               contentType, contentClass.getName()));
                           }
                       } catch (IllegalArgumentException iae) { // shouldn't really happen
                           throw new JsonMappingException(null,
                                   String.format("Internal error: failed to refine value type of %s with concrete-type annotation (value %s), from '%s': %s",
                                           type, contentClass.getName(), a.getName(), iae.getMessage()),
                                           iae);
                       }
                   }
               }
               type = type.withContentType(contentType);
           }
        }
        return type;
    }