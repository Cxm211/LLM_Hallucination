protected JsonDeserializer<Object> buildBuilderBasedDeserializer(
    		DeserializationContext ctxt, JavaType valueType, BeanDescription builderDesc)
        throws JsonMappingException
    {
        ValueInstantiator valueInstantiator;
        try {
            valueInstantiator = findValueInstantiator(ctxt, builderDesc);
        } catch (NoClassDefFoundError error) {
            return new ErrorThrowingDeserializer(error);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            throw InvalidDefinitionException.from(ctxt.getParser(),
                    (msg == null) ? "N/A" : msg,
                    builderDesc, null);
        }
        final DeserializationConfig config = ctxt.getConfig();
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(ctxt, builderDesc);
        builder.setValueInstantiator(valueInstantiator);
        addBeanProps(ctxt, builderDesc, builder);
        addObjectIdReader(ctxt, builderDesc, builder);
        
        addBackReferenceProperties(ctxt, builderDesc, builder);
        addInjectables(ctxt, builderDesc, builder);

        JsonPOJOBuilder.Value builderConfig = builderDesc.findPOJOBuilderConfig();
        final String buildMethodName = (builderConfig == null) ?
                JsonPOJOBuilder.DEFAULT_BUILD_METHOD : builderConfig.buildMethodName;
        
        AnnotatedMethod buildMethod = builderDesc.findMethod(buildMethodName, null);
        if (buildMethod != null) {
            if (config.canOverrideAccessModifiers()) {
            	ClassUtil.checkAndFixAccess(buildMethod.getMember(), config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
        }
        builder.setPOJOBuilder(buildMethod, builderConfig);
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, builderDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer = builder.buildBuilderBased(
        		valueType, buildMethodName);

        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deserializer = mod.modifyDeserializer(config, builderDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>) deserializer;
    }