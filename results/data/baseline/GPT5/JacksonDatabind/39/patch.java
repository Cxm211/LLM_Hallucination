public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
{
    // 29-Jan-2016, tatu: Simple skipping for all other tokens, but FIELD_NAME bit
    //    special unfortunately
    if (p.hasToken(JsonToken.FIELD_NAME)) {
        while (p.nextToken() != JsonToken.END_OBJECT) {
            p.skipChildren();
        }
    } else {
        p.skipChildren();
    }
    return null;
}
