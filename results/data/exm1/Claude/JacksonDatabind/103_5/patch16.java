protected Object _weirdKey(DeserializationContext ctxt, String key, Exception e) throws IOException {
        String msg = e.getMessage();
        return ctxt.handleWeirdKey(_keyClass, key, "problem: %s",
                msg == null ? "N/A" : msg);
    }