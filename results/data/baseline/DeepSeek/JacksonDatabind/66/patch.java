public final Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
    if (key == null) {
        return null;
    }
    try {
        // Ugh... should not have to give parser which may or may not be correct one...
        JsonParser p = ctxt.getFactory().createParser(key);
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