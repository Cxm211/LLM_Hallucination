protected JsonSerializer<?> _createSerializer2(SerializerProvider prov,
            JavaType type, BeanDescription beanDesc, boolean staticTyping)
        throws JsonMappingException
    {
        final SerializationConfig config = prov.getConfig();
        
        // Container types differ from non-container types
        // (note: called method checks for module-provided serializers)
        if (type.isContainerType()) {
            if (!staticTyping) {
                staticTyping = usesStaticTyping(config, beanDesc, null);
            }
            // 03-Aug-2012, tatu: As per [Issue#40], may require POJO serializer...
            JsonSerializer<?> ser = buildContainerSerializer(prov, type, beanDesc, staticTyping);
            if (ser != null) {
                // [databind#120]: Allow post-processing
                if (_factoryConfig.hasSerializerModifiers()) {
                    for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                        ser = mod.modifySerializer(config, beanDesc, ser);
                    }
                }
                return ser;
            }
        } else {
            // Modules may provide serializers of POJO types first (must override annotations like @JsonValue)
            JsonSerializer<?> ser = null;
            for (Serializers serializers : customSerializers()) {
                ser = serializers.findSerializer(config, type, beanDesc);
                if (ser != null) {
                    // [databind#120]: Allow post-processing
                    if (_factoryConfig.hasSerializerModifiers()) {
                        for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                            ser = mod.modifySerializer(config, beanDesc, ser);
                        }
                    }
                    return ser;
                }
            }
        }

        // Then annotations like @JsonValue, @JsonSerialize(using=...)
        JsonSerializer<?> ser = findSerializerByAnnotations(prov, type, beanDesc);
        if (ser != null) {
            // [databind#120]: Allow post-processing
            if (_factoryConfig.hasSerializerModifiers()) {
                for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                    ser = mod.modifySerializer(config, beanDesc, ser);
                }
            }
            return ser;
        }
        
        if (ser == null) {
            // Otherwise, we will check "primary types"; both marker types that
            // indicate specific handling (JsonSerializable), or main types that have
            // precedence over container types
            ser = findSerializerByLookup(type, config, beanDesc, staticTyping);
            if (ser == null) {
                ser = findSerializerByPrimaryType(prov, type, beanDesc, staticTyping);
                if (ser == null) {
                    // And this is where this class comes in: if type is not a
                    // known "primary JDK type", perhaps it's a bean? We can still
                    // get a null, if we can't find a single suitable bean property.
                    ser = findBeanSerializer(prov, type, beanDesc);
                    // Finally: maybe we can still deal with it as an implementation of some basic JDK interface?
                    if (ser == null) {
                        ser = findSerializerByAddonType(config, type, beanDesc, staticTyping);
                        // 18-Sep-2014, tatu: Actually, as per [jackson-databind#539], need to get
                        //   'unknown' serializer assigned earlier, here, so that it gets properly
                        //   post-processed
                        if (ser == null) {
                            ser = prov.getUnknownTypeSerializer(beanDesc.getBeanClass());
                        }
                    }
                }
            }
        }
        if (ser != null) {
            // [databind#120]: Allow post-processing
            if (_factoryConfig.hasSerializerModifiers()) {
                for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                    ser = mod.modifySerializer(config, beanDesc, ser);
                }
            }
        }
        return ser;
    }