public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
{
    JsonToken t = jp.getCurrentToken();
    if (t == JsonToken.FIELD_NAME) {
        writeStartObject();
    }
    copyCurrentStructure(jp);
    return this;
}