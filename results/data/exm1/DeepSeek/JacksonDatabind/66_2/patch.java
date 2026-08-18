public final Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
    if (key == null) {
        return null;
    }
    try {
        if (_delegate instanceof KeyDeserializer) {
            return ((KeyDeserializer) _delegate).deserializeKey(key, ctxt);
        }
        Object result = _delegate.deserialize(ctxt.getParser(), ctxt);
        if (result != null) {
            return result;
        }
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
    } catch (Exception re) {
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
    }
}