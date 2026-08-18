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
                        // ignore primitive <-> wrapper refinements
                        if ((currRaw == Integer.TYPE && serClass == Integer.class)
                                || (currRaw == Integer.class && serClass == Integer.TYPE)
                                || (currRaw == Long.TYPE && serClass == Long.class)
                                || (currRaw == Long.class && serClass == Long.TYPE)
                                || (currRaw == Boolean.TYPE && serClass == Boolean.class)
                                || (currRaw == Boolean.class && serClass == Boolean.TYPE)
                                || (currRaw == Double.TYPE && serClass == Double.class)
                                || (currRaw == Double.class && serClass == Double.TYPE)
                                || (currRaw == Float.TYPE && serClass == Float.class)
                                || (currRaw == Float.class && serClass == Float.TYPE)
                                || (currRaw == Short.TYPE && serClass == Short.class)
                                || (currRaw == Short.class && serClass == Short.TYPE)
                                || (currRaw == Byte.TYPE && serClass == Byte.class)
                                || (currRaw == Byte.class && serClass == Byte.TYPE)
                                || (currRaw == Character.TYPE && serClass == Character.class)
                                || (currRaw == Character.class && serClass == Character.TYPE)) {
                            // do nothing
                        } else {
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
                            // ignore primitive <-> wrapper refinements
                            if ((currRaw == Integer.TYPE && keyClass == Integer.class)
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
                                    || (currRaw == Character.class && keyClass == Character.TYPE)) {
                                // do nothing
                            } else {
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
                           // ignore primitive <-> wrapper refinements
                           if ((currRaw == Integer.TYPE && contentClass == Integer.class)
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
                                   || (currRaw == Character.class && contentClass == Character.TYPE)) {
                               // do nothing
                           } else {
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