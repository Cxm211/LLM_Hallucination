protected JsonSerializer<?> buildMapSerializer(SerializationConfig config,
            MapType type, BeanDescription beanDesc,
            boolean staticTyping, JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
        throws JsonMappingException
    {
        JsonSerializer<?> ser = null;
        
        for (Serializers serializers : customSerializers()) { 
            ser = serializers.findMapSerializer(config, type, beanDesc,
                    keySerializer, elementTypeSerializer, elementValueSerializer);
            if (ser != null) { break; }
        }
        if (ser == null) {
                Object filterId = findFilterId(config, beanDesc);
                AnnotationIntrospector ai = config.getAnnotationIntrospector();
                MapSerializer mapSer = MapSerializer.construct(ai.findPropertiesToIgnore(beanDesc.getClassInfo(), true),
                        type, staticTyping, elementTypeSerializer,
                        keySerializer, elementValueSerializer, filterId);
                Object suppressableValue = findSuppressableContentValue(config,
                        type.getContentType(), beanDesc);
                if (suppressableValue != null) {
                    mapSer = mapSer.withContentInclusion(suppressableValue);
                }
                ser = mapSer;
            }
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                ser = mod.modifyMapSerializer(config, type, beanDesc, ser);
            }
        }
        return ser;
    }