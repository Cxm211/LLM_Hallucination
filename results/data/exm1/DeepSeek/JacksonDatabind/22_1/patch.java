protected JsonSerializer<?> buildContainerSerializer(SerializerProvider prov,
            JavaType type, BeanDescription beanDesc, boolean staticTyping)
        throws JsonMappingException
    {
        final SerializationConfig config = prov.getConfig();

        if (!staticTyping && type.useStaticType()) {
            if (!type.isContainerType() || type.getContentType().getRawClass() != Object.class) {
                staticTyping = true;
            }
        }
        
        JavaType elementType = type.getContentType();
        TypeSerializer elementTypeSerializer = createTypeSerializer(config, elementType);

        if (elementTypeSerializer != null) {
            staticTyping = false;
        }
        JsonSerializer<Object> elementValueSerializer = _findContentSerializer(prov, beanDesc.getClassInfo());
        if (type.isMapLikeType()) {
            MapLikeType mlt = (MapLikeType) type;
            JsonSerializer<Object> keySerializer = _findKeySerializer(prov, beanDesc.getClassInfo());
            if (mlt.isTrueMapType()) {
                return buildMapSerializer(config, (MapType) mlt, beanDesc, staticTyping,
                        keySerializer, elementTypeSerializer, elementValueSerializer);
            }
            JsonSerializer<?> ser = null;
            for (Serializers serializers : customSerializers()) {
                ser = serializers.findMapLikeSerializer(config, mlt, beanDesc, keySerializer, elementTypeSerializer, elementValueSerializer);
                if (ser != null) {
                    if (_factoryConfig.hasSerializerModifiers()) {
                        for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                            ser = mod.modifyMapLikeSerializer(config, mlt, beanDesc, ser);
                        }
                    }
                    return ser;
                }
            }
            return null;
        }
        if (type.isCollectionLikeType()) {
            CollectionLikeType clt = (CollectionLikeType) type;
            if (clt.isTrueCollectionType()) {
                return buildCollectionSerializer(config, (CollectionType) clt, beanDesc, staticTyping,
                        elementTypeSerializer, elementValueSerializer);
            }
            JsonSerializer<?> ser = null;
            for (Serializers serializers : customSerializers()) {
                ser = serializers.findCollectionLikeSerializer(config, clt, beanDesc, elementTypeSerializer, elementValueSerializer);
                if (ser != null) {
                    if (_factoryConfig.hasSerializerModifiers()) {
                        for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                            ser = mod.modifyCollectionLikeSerializer(config, clt, beanDesc, ser);
                        }
                    }
                    return ser;
                }
            }
            return null;
        }
        if (type.isArrayType()) {
            return buildArraySerializer(config, (ArrayType) type, beanDesc, staticTyping,
                    elementTypeSerializer, elementValueSerializer);
        }
        return null;
    }