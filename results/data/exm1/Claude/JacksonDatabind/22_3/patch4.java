protected JsonSerializer<?> _createSerializer2(SerializerProvider prov,
            JavaType type, BeanDescription beanDesc, boolean staticTyping)
        throws JsonMappingException
    {
        JsonSerializer<?> ser = findSerializerByAnnotations(prov, type, beanDesc);
        if (ser != null) {
            return ser;
        }
        final SerializationConfig config = prov.getConfig();
        
        if (type.isContainerType()) {
            if (!staticTyping) {
                staticTyping = usesStaticTyping(config, beanDesc, null);
            }
            ser =  buildContainerSerializer(prov, type, beanDesc, staticTyping);
            if (ser != null) {
                return ser;
            }
        } else {
            for (Serializers serializers : customSerializers()) {
                ser = serializers.findSerializer(config, type, beanDesc);
                if (ser != null) {
                    break;
                }
            }
        }
        
        if (ser == null) {
            ser = findSerializerByLookup(type, config, beanDesc, staticTyping);
            if (ser == null) {
                ser = findSerializerByPrimaryType(prov, type, beanDesc, staticTyping);
                if (ser == null) {
                    ser = findBeanSerializer(prov, type, beanDesc);
                    if (ser == null) {
                        ser = findSerializerByAddonType(config, type, beanDesc, staticTyping);
                        if (ser == null) {
                            ser = prov.getUnknownTypeSerializer(beanDesc.getBeanClass());
                        }
                    }
                }
            }
        }
        if (ser != null) {
            if (_factoryConfig.hasSerializerModifiers()) {
                for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                    ser = mod.modifySerializer(config, beanDesc, ser);
                }
            }
        }
        return ser;
    }