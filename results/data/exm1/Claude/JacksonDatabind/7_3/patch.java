public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
{
    while (jp.nextToken() != null) {
        copyCurrentEvent(jp);
    }
    return this;
}