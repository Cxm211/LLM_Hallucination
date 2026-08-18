public final Object deserializeKey(String key, DeserializationContext ctxt)
    throws IOException
{
    if (key == null) { // is this even legal call?
        return null;
    }
    try {
        // Create a parser from the key string to deserialize it
        JsonParser p = ctxt.getParser().getCodec().getFactory().createParser(key);
        p.nextToken();
        Object result = _delegate.deserialize(p, ctxt);
        p.close();
        if (result != null) {
            return result;
        }
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
    } catch (Exception re) {
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
    }
}