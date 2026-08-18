public final Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        if (key == null) {
            return null;
        }
        try {
            // Use a parser that reads the key string as a JSON value
            JsonParser p = ctxt.getFactory().createParser("\"" + key + "\"");
            p.nextToken();
            Object result = _delegate.deserialize(p, ctxt);
            if (result != null) {
                return result;
            }
            return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
        } catch (Exception re) {
            return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
        }
    }