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
                try {
                    // 11-Oct-2015, tatu: For deser, we call `TypeFactory.constructSpecializedType()`,
                    //   may be needed here too in future?
                    if (serClass.isAssignableFrom(currRaw)) { // common case
                        type = tf.constructGeneralizedType(type, serClass);
                    } else if (currRaw.isAssignableFrom(serClass)) { // specialization, ok as well
                        type = tf.constructSpecializedType(type, serClass);
                        // 27-Apr-2017, tatu: [databind#1592] ignore primitive<->wrapper refinements
                    } else {
                        // [databind#1592]: ignore primitive<->wrapper refinements
                        boolean primWrapPair =
                                (currRaw.isPrimitive() && (
                                        (currRaw == int.class && serClass == Integer.class) ||
                                        (currRaw == long.class && serClass == Long.class) ||
                                        (currRaw == boolean.class && serClass == Boolean.class) ||
                                        (currRaw == double.class && serClass == Double.class) ||
                                        (currRaw == float.class && serClass == Float.class) ||
                                        (currRaw == short.class && serClass == Short.class) ||
                                        (currRaw == byte.class && serClass == Byte.class) ||
                                        (currRaw == char.class && serClass == Character.class)
                                )) ||
                                (serClass.isPrimitive() && (
                                        (serClass == int.class && currRaw == Integer.class) ||
                                        (serClass == long.class && currRaw == Long.class) ||
                                        (serClass == boolean.class && currRaw == Boolean.class) ||
                                        (serClass == double.class && currRaw == Double.class) ||
                                        (serClass == float.class && currRaw == Float.class) ||
                                        (serClass == short.class && currRaw == Short.class) ||
                                        (serClass == byte.class && currRaw == Byte.class) ||
                                        (serClass == char.class && currRaw == Character.class)
                                ));
                        if (!primWrapPair) {
                            throw new JsonMappingException(null,
                                    String.format("Can not refine serialization type %s into %s; types not related",
                                            type, serClass.getName()));
                        }
                    }
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to widen type %s with annotation (value %s), from '%s': %s",
                                    type, serClass.getName(), a.getName(), iae.getMessage()),
                                    iae);
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
                            // [databind#1592]: ignore primitive<->wrapper refinements for keys
                            boolean primWrapPair =
                                    (currRaw.isPrimitive() && (
                                            (currRaw == int.class && keyClass == Integer.class) ||
                                            (currRaw == long.class && keyClass == Long.class) ||
                                            (currRaw == boolean.class && keyClass == Boolean.class) ||
                                            (currRaw == double.class && keyClass == Double.class) ||
                                            (currRaw == float.class && keyClass == Float.class) ||
                                            (currRaw == short.class && keyClass == Short.class) ||
                                            (currRaw == byte.class && keyClass == Byte.class) ||
                                            (currRaw == char.class && keyClass == Character.class)
                                    )) ||
                                    (keyClass.isPrimitive() && (
                                            (keyClass == int.class && currRaw == Integer.class) ||
                                            (keyClass == long.class && currRaw == Long.class) ||
                                            (keyClass == boolean.class && currRaw == Boolean.class) ||
                                            (keyClass == double.class && currRaw == Double.class) ||
                                            (keyClass == float.class && currRaw == Float.class) ||
                                            (keyClass == short.class && currRaw == Short.class) ||
                                            (keyClass == byte.class && currRaw == Byte.class) ||
                                            (keyClass == char.class && currRaw == Character.class)
                                    ));
                            if (!primWrapPair) {
                                throw new JsonMappingException(null,
                                        String.format("Can not refine serialization key type %s into %s; types not related",
                                                keyType, keyClass.getName()));
                            }
                        }
                    } catch (IllegalArgumentException iae) {
                        throw new JsonMappingException(null,
                                String.format("Failed to widen key type of %s with concrete-type annotation (value %s), from '%s': %s",
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
           final Class<?> contentClass = (jsonSer == null) ? null : _classIfExplicit(jsonSer.contentAs());
           if (contentClass != null) {
               if (contentType.hasRawClass(contentClass)) {
                   contentType = contentType.withStaticTyping();
               } else {
                   // 03-Apr-2016, tatu: As per [databind#1178], may need to actually
                   //   specialize (narrow) type sometimes, even if more commonly opposite
                   //   is needed.
                   Class<?> currRaw = contentType.getRawClass();
                   try {
                       if (contentClass.isAssignableFrom(currRaw)) { // common case
                           contentType = tf.constructGeneralizedType(contentType, contentClass);
                       } else if (currRaw.isAssignableFrom(contentClass)) { // specialization, ok as well
                           contentType = tf.constructSpecializedType(contentType, contentClass);
                           // 27-Apr-2017, tatu: [databind#1592] ignore primitive<->wrapper refinements
                       } else {
                           // [databind#1592]: ignore primitive<->wrapper refinements for contents
                           boolean primWrapPair =
                                   (currRaw.isPrimitive() && (
                                           (currRaw == int.class && contentClass == Integer.class) ||
                                           (currRaw == long.class && contentClass == Long.class) ||
                                           (currRaw == boolean.class && contentClass == Boolean.class) ||
                                           (currRaw == double.class && contentClass == Double.class) ||
                                           (currRaw == float.class && contentClass == Float.class) ||
                                           (currRaw == short.class && contentClass == Short.class) ||
                                           (currRaw == byte.class && contentClass == Byte.class) ||
                                           (currRaw == char.class && contentClass == Character.class)
                                   )) ||
                                   (contentClass.isPrimitive() && (
                                           (contentClass == int.class && currRaw == Integer.class) ||
                                           (contentClass == long.class && currRaw == Long.class) ||
                                           (contentClass == boolean.class && currRaw == Boolean.class) ||
                                           (contentClass == double.class && currRaw == Double.class) ||
                                           (contentClass == float.class && currRaw == Float.class) ||
                                           (contentClass == short.class && currRaw == Short.class) ||
                                           (contentClass == byte.class && currRaw == Byte.class) ||
                                           (contentClass == char.class && currRaw == Character.class)
                                   ));
                           if (!primWrapPair) {
                               throw new JsonMappingException(null,
                                       String.format("Can not refine serialization content type %s into %s; types not related",
                                               contentType, contentClass.getName()));
                           }
                       }
                   } catch (IllegalArgumentException iae) { // shouldn't really happen
                       throw new JsonMappingException(null,
                               String.format("Internal error: failed to refine value type of %s with concrete-type annotation (value %s), from '%s': %s",
                                       type, contentClass.getName(), a.getName(), iae.getMessage()),
                                       iae);
                   }
               }
               type = type.withContentType(contentType);
           }
        }
        return type;
    }