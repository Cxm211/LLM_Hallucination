public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
{
    if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
        // As per #592, need to support a special case of starting from
        // FIELD_NAME, which is taken to mean that we are missing START_OBJECT,
        // but need to assume one did exist.
        writeStartObject();
        copyCurrentStructure(jp);
        writeEndObject();
    } else {
        copyCurrentStructure(jp);
    }
    return this;
}