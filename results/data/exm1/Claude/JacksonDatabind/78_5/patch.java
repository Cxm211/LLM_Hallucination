public JsonDeserializer<Object> createBeanDeserializer(DeserializationContext ctxt,
            JavaType type, BeanDescription beanDesc)
        throws JsonMappingException
    {
        final DeserializationConfig config = ctxt.getConfig();
        JsonDeserializer<Object> custom = _findCustomBeanDeserializer(type, config, beanDesc);
        if (custom != null) {
            return custom;
        }
        if (type.isThrowable()) {
            return buildThrowableDeserializer(ctxt, type, beanDesc);
        }
        if (type.isAbstract() && !type.isPrimitive() && !type.isEnumType()) {
            JavaType concreteType = materializeAbstractType(ctxt, type, beanDesc);
            if (concreteType != null) {
                beanDesc = config.introspect(concreteType);
                return buildBeanDeserializer(ctxt, concreteType, beanDesc);
            }
        }
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) findStdDeserializer(ctxt, type, beanDesc);
        if (deser != null) {
            return deser;
        }
        if (!isPotentialBeanType(type.getRawClass())) {
            return null;
        }
        return buildBeanDeserializer(ctxt, type, beanDesc);
    }