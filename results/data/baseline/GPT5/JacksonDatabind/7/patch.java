public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
{
    JsonToken t = jp.getCurrentToken();
    if (t == JsonToken.FIELD_NAME) {
        writeStartObject();
        writeFieldName(jp.getCurrentName());
        jp.nextToken();
        copyCurrentStructure(jp);
        writeEndObject();
    } else {
        copyCurrentStructure(jp);
    }
    return this;
}
