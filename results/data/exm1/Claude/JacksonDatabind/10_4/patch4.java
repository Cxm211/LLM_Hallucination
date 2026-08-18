protected JsonSerializer<Object> constructBeanSerializer(SerializerProvider prov,
        BeanDescription beanDesc)
    throws JsonMappingException
{
    if (beanDesc.getBeanClass() == Object.class) {
        return prov.getUnknownTypeSerializer(Object.class);
    }
    final SerializationConfig config = prov.getConfig();
    BeanSerializerBuilder builder = constructBeanSerializerBuilder(beanDesc);
    builder.setConfig(config);

    List<BeanPropertyWriter> props = findBeanProperties(prov, beanDesc, builder);
    if (props == null) {
        props = new ArrayList<BeanPropertyWriter>();
    }
    prov.getAnnotationIntrospector().findAndAddVirtualProperties(config, beanDesc.getClassInfo(), props);

    if (_factoryConfig.hasSerializerModifiers()) {
        for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
            props = mod.changeProperties(config, beanDesc, props);
        }
    }

    props = filterBeanProperties(config, beanDesc, props);

    if (_factoryConfig.hasSerializerModifiers()) {
        for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
            props = mod.orderProperties(config, beanDesc, props);
        }
    }

    builder.setObjectIdWriter(constructObjectIdHandler(prov, beanDesc, props));
    
    builder.setProperties(props);
    builder.setFilterId(findFilterId(config, beanDesc));
    
    AnnotatedMember anyGetter = beanDesc.findAnyGetter();
    if (anyGetter != null) {
        if (config.canOverrideAccessModifiers()) {
            anyGetter.fixAccess();
        }
        JavaType type = anyGetter.getType(beanDesc.bindingsForBeanType());
        boolean staticTyping = config.isEnabled(MapperFeature.USE_STATIC_TYPING);
        JavaType valueType = type.getContentType();
        TypeSerializer typeSer = createTypeSerializer(config, valueType);
        MapSerializer anySer = MapSerializer.construct(null, type, staticTyping, typeSer, null, null, null);
        PropertyName name = new PropertyName(anyGetter.getName());
        BeanProperty.Std anyProp = new BeanProperty.Std(name, valueType, null,
                beanDesc.getClassAnnotations(), anyGetter, PropertyMetadata.STD_OPTIONAL);
        builder.setAnyGetter(new AnyGetterWriter(anyProp, anyGetter, anySer));
    }
    processViews(config, builder);

    if (_factoryConfig.hasSerializerModifiers()) {
        for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
            builder = mod.updateBuilder(config, beanDesc, builder);
        }
    }
    
    JsonSerializer<Object> ser = (JsonSerializer<Object>) builder.build();
    
    if (ser == null) {
        if (beanDesc.hasKnownClassAnnotations()) {
            return builder.createDummy();
        }
    }
    return ser;
}