    protected Object _weirdKey(DeserializationContext ctxt, String key, Exception e) throws IOException {
        if (e instanceof JsonMappingException) {
            throw (JsonMappingException) e;
        }
        return ctxt.handleWeirdKey(_keyClass, key, "problem: %s",
                e.getMessage());
    }