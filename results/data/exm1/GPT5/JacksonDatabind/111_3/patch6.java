public AtomicReference<Object> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return (AtomicReference<Object>) (_nullProvider == null ? null : _nullProvider.getNullValue(ctxt));
    }