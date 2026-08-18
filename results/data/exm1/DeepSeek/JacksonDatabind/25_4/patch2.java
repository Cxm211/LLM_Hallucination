    protected final JsonDeserializer<Object> _findDeserializer(DeserializationContext ctxt,
            String typeId) throws IOException
    {
        JsonDeserializer<Object> deser = _deserializers.get(typeId);
        if (deser == null) {
            JavaType type = _idResolver.typeFromId(ctxt, typeId);
            if (type == null) {
                deser = _findDefaultImplDeserializer(ctxt);
                if (deser == null) {
                    deser = _handleUnknownTypeId(ctxt, typeId, _idResolver, _baseType);
                }
            } else {
                if ((_baseType != null)
                        && _baseType.getClass() == type.getClass()) {
                    type = _baseType.narrowBy(type.getRawClass());
                }
                deser = ctxt.findContextualValueDeserializer(type, _property);
            }
            _deserializers.put(typeId, deser);
        }
        return deser;
    }