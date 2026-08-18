public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
{
    if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
        // Assume a START_OBJECT is missing, write one
        writeStartObject();
        copyCurrentStructure(jp);
        writeEndObject();
    } else {
        copyCurrentStructure(jp);
    }
    return this;
}