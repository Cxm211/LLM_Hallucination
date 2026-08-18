public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return (_nullProvider == null) ? null : _nullProvider.getNullValue(ctxt);
    }