public final Object deserializeKey(String key, DeserializationContext ctxt)
    throws IOException
{
    if (key == null) { // is this even legal call?
        return null;
    }
    try {
        JsonParser parser = ctxt.getParser();
        JsonParser keyParser = parser.getCodec().getFactory().createParser(key);
        keyParser.nextToken();
        Object result = _delegate.deserialize(keyParser, ctxt);
        keyParser.close();
        if (result != null) {
            return result;
        }
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
    } catch (Exception re) {
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
    }
}