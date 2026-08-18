    public AtomicReference<Object> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return (AtomicReference<Object>) _nullProvider.nullValue(ctxt);
    }