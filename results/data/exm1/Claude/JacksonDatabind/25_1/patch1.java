private JavaType modifyTypeByAnnotation(DeserializationContext ctxt,
            Annotated a, JavaType type)
        throws JsonMappingException
    {
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        Class<?> subclass = intr.findDeserializationType(a, type);
        if (subclass != null) {
            try {
                type = type.narrowBy(subclass);
            } catch (IllegalArgumentException iae) {
                throw new JsonMappingException("Failed to narrow type "+type+" with concrete-type annotation (value "+subclass.getName()+"), method '"+a.getName()+"': "+iae.getMessage(), null, iae);
            }
        }

        if (type.isContainerType()) {
            Class<?> keyClass = intr.findDeserializationKeyType(a, type.getKeyType());
            if (keyClass != null) {
                if (!(type instanceof MapLikeType)) {
                    throw new JsonMappingException("Illegal key-type annotation: type "+type+" is not a Map(-like) type");
                }
                try {
                    type = ((MapLikeType) type).narrowKey(keyClass);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException("Failed to narrow key type "+type+" with key-type annotation ("+keyClass.getName()+"): "+iae.getMessage(), null, iae);
                }
            }
            JavaType keyType = type.getKeyType();
            if (keyType != null && keyType.getValueHandler() == null) {
                Object kdDef = intr.findKeyDeserializer(a);
                KeyDeserializer kd = ctxt.keyDeserializerInstance(a, kdDef);
                if (kd != null) {
                    type = ((MapLikeType) type).withKeyValueHandler(kd);
                    keyType = type.getKeyType();
                }
            }            
            
            Class<?> cc = intr.findDeserializationContentType(a, type.getContentType());
            if (cc != null) {
                try {
                    type = type.narrowContentsBy(cc);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException("Failed to narrow content type "+type+" with content-type annotation ("+cc.getName()+"): "+iae.getMessage(), null, iae);
                }
            }
            JavaType contentType = type.getContentType();
            if (contentType.getValueHandler() == null) {
                Object cdDef = intr.findContentDeserializer(a);
                JsonDeserializer<?> cd = ctxt.deserializerInstance(a, cdDef);
                if (cd != null) {
                    type = type.withContentValueHandler(cd);
                }
            }
        }
        return type;
    }