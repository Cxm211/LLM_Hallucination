public TypeDeserializer buildTypeDeserializer(DeserializationConfig config,
            JavaType baseType, Collection<NamedType> subtypes)
    {
        if (_idType == JsonTypeInfo.Id.NONE) { return null; }

        TypeIdResolver idRes = idResolver(config, baseType, subtypes, false, true);

        JavaType defaultImpl;

        if (_defaultImpl == null) {
            defaultImpl = null;
        } else {
            if ((_defaultImpl == Void.class)
                     || (_defaultImpl == NoClass.class)) {
                defaultImpl = config.getTypeFactory().constructType(_defaultImpl);
            } else {
                defaultImpl = config.getTypeFactory()
                    .constructSpecializedType(baseType, _defaultImpl);
            }
        }

        switch (_includeAs) {
        case WRAPPER_ARRAY:
            return new AsArrayTypeDeserializer(baseType, idRes,
                    _typeProperty, _typeIdVisible, defaultImpl);
        case PROPERTY:
        case EXISTING_PROPERTY:
            return new AsPropertyTypeDeserializer(baseType, idRes,
                    _typeProperty, _typeIdVisible, defaultImpl, _includeAs);
        case WRAPPER_OBJECT:
            return new AsWrapperTypeDeserializer(baseType, idRes,
                    _typeProperty, _typeIdVisible, defaultImpl);
        case EXTERNAL_PROPERTY:
            return new AsExternalTypeDeserializer(baseType, idRes,
                    _typeProperty, _typeIdVisible, defaultImpl);
        }
        throw new IllegalStateException("Do not know how to construct standard type serializer for inclusion type: "+_includeAs);
    }