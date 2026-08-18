protected JsonDeserializer<Object> _findRootDeserializer(DeserializationContext ctxt,
            JavaType valueType)
        throws JsonMappingException
    {
        if (_rootDeserializer != null) {
            if (_valueType != null && _valueType.equals(valueType)) {
                return _rootDeserializer;
            }
        }

        if (valueType == null) {
            throw new JsonMappingException("No value type configured for ObjectReader");
        }
        
        JsonDeserializer<Object> deser = _rootDeserializers.get(valueType);
        if (deser != null) {
            return deser;
        }
        deser = ctxt.findRootValueDeserializer(valueType);
        if (deser == null) {
            throw new JsonMappingException("Can not find a deserializer for type "+valueType);
        }
        _rootDeserializers.put(valueType, deser);
        return deser;
    }