public TypeDeserializer findTypeDeserializer(DeserializationConfig config,
            JavaType baseType)
        throws JsonMappingException
    {
        BeanDescription bean = config.introspectClassAnnotations(baseType.getRawClass());
        AnnotatedClass ac = bean.getClassInfo();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);

        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = config.getDefaultTyper(baseType);
            if (b == null) {
                return null;
            }
        } else {
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByTypeId(config, ac);
        }
        if ((b.getDefaultImpl() == null) && baseType.isAbstract()) {
            JavaType defaultType = mapAbstractType(config, baseType);
            if ((defaultType != null) && !defaultType.hasRawClass(baseType.getRawClass())) {
                b = b.defaultImpl(defaultType.getRawClass());
            }
        }
        try {
            return b.buildTypeDeserializer(config, baseType, subtypes);
        } catch (IllegalArgumentException e0) {
            String msg = e0.getMessage();
            InvalidDefinitionException e = InvalidDefinitionException.from((JsonParser) null,
                    (msg == null) ? "N/A" : msg, baseType);
            e.initCause(e0);
            throw e;
        }
    }