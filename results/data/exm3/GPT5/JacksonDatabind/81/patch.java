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
                    } else if ((currRaw.isPrimitive() &&
                                ((currRaw == Integer.TYPE && serClass == Integer.class)
                                 || (currRaw == Long.TYPE && serClass == Long.class)
                                 || (currRaw == Boolean.TYPE && serClass == Boolean.class)
                                 || (currRaw == Double.TYPE && serClass == Double.class)
                                 || (currRaw == Float.TYPE && serClass == Float.class)
                                 || (currRaw == Short.TYPE && serClass == Short.class)
                                 || (currRaw == Byte.TYPE && serClass == Byte.class)
                                 || (currRaw == Character.TYPE && serClass == Character.class)))
                               || (serClass.isPrimitive() &&
                                ((serClass == Integer.TYPE && currRaw == Integer.class)
                                 || (serClass == Long.TYPE && currRaw == Long.class)
                                 || (serClass == Boolean.TYPE && currRaw == Boolean.class)
                                 || (serClass == Double.TYPE && currRaw == Double.class)
                                 || (serClass == Float.TYPE && currRaw == Float.class)
                                 || (serClass == Short.TYPE && currRaw == Short.class)
                                 || (serClass == Byte.TYPE && currRaw == Byte.class)
                                 || (serClass == Character.TYPE && currRaw == Character.class))))) {
                        // Ignore primitive/wrapper refinement mismatch
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
                        } else if ((currRaw.isPrimitive() &&
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
                       } else if ((currRaw.isPrimitive() &&
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
               type = type.withContentType(contentType);
           }
        }
        return type;
    }