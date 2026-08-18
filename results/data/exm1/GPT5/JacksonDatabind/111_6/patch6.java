public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        if (_nullProvider == null) {
            return null;
        }
        return _nullProvider.getNullValue(ctxt);
    }