public JsonDeserializer<Object> buildBeanDeserializer(DeserializationContext ctxt,
        JavaType type, BeanDescription beanDesc)
    throws JsonMappingException
{
    ValueInstantiator valueInstantiator;
    try {
        valueInstantiator = findValueInstantiator(ctxt, beanDesc);
    } catch (NoClassDefFoundError error) {
        return new ErrorThrowingDeserializer(error);
    } catch (IllegalArgumentException e) {
        throw InvalidDefinitionException.from(ctxt.getParser(),
                e.getMessage(),
                beanDesc, null);
    }
    BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(ctxt, beanDesc);
    builder.setValueInstantiator(valueInstantiator);
    addBeanProps(ctxt, beanDesc, builder);
    addObjectIdReader(ctxt, beanDesc, builder);

    addBackReferenceProperties(ctxt, beanDesc, builder);
    addInjectables(ctxt, beanDesc, builder);
    
    final DeserializationConfig config = ctxt.getConfig();
    if (_factoryConfig.hasDeserializerModifiers()) {
        for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
            builder = mod.updateBuilder(config, beanDesc, builder);
        }
    }
    JsonDeserializer<?> deserializer;

    if (type.isAbstract() && !valueInstantiator.canInstantiate()) {
        deserializer = builder.buildAbstract();
    } else {
        deserializer = builder.build();
    }
    if (_factoryConfig.hasDeserializerModifiers()) {
        for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
            deserializer = mod.modifyDeserializer(config, beanDesc, deserializer);
        }
    }
    return (JsonDeserializer<Object>) deserializer;
}