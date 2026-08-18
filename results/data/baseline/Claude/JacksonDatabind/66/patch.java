public final Object deserializeKey(String key, DeserializationContext ctxt)
    throws IOException
{
    if (key == null) { // is this even legal call?
        return null;
    }
    try {
        // Ugh... should not have to give parser which may or may not be correct one...
        JsonParser p = ctxt.getParser();
        TokenBuffer tb = new TokenBuffer(p, ctxt);
        tb.writeString(key);
        JsonParser keyParser = tb.asParser();
        keyParser.nextToken();
        Object result = _delegate.deserialize(keyParser, ctxt);
        if (result != null) {
            return result;
        }
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
    } catch (Exception re) {
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
    }
}