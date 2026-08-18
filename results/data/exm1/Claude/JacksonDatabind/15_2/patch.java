public boolean isMapLikeType() { return false; }

    public JsonSerializer<Object> createSerializer(SerializerProvider prov,
            JavaType origType)
        throws JsonMappingException
    {
        final SerializationConfig config = prov.getConfig();
        BeanDescription beanDesc = config.introspect(origType);
        JsonSerializer<?> ser = findSerializerFromAnnotation(prov, beanDesc.getClassInfo());
        if (ser != null) {
            return (JsonSerializer<Object>) ser;
        }
        boolean staticTyping;
        JavaType type = modifyTypeByAnnotation(config, beanDesc.getClassInfo(), origType);
        if (type == origType) {
            staticTyping = false;
        } else {
            staticTyping = true;
            if (!type.hasRawClass(origType.getRawClass())) {
                beanDesc = config.introspect(type);
            }
        }
        Converter<Object,Object> conv = beanDesc.findSerializationConverter();
        if (conv == null) {
            return (JsonSerializer<Object>) _createSerializer2(prov, type, beanDesc, staticTyping);
        }
        JavaType delegateType = conv.getOutputType(prov.getTypeFactory());
        
        if (!delegateType.hasRawClass(type.getRawClass())) {
            beanDesc = config.introspect(delegateType);
            ser = findSerializerFromAnnotation(prov, beanDesc.getClassInfo());
        }
        if (ser == null && !delegateType.isJavaLangObject()) {
            ser = _createSerializer2(prov, delegateType, beanDesc, true);
        }
        return new StdDelegatingSerializer(conv, delegateType, ser);
    }