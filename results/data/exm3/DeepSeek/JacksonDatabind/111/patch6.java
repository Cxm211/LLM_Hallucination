    public AtomicReference<Object> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        if (_valueDeserializer != null) {
            return new AtomicReference<Object>(_valueDeserializer.getNullValue(ctxt));
        }
        return new AtomicReference<Object>();
    }