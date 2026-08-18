public ObjectNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
{
    JsonToken t = p.getCurrentToken();
    if (t == JsonToken.START_OBJECT) {
        p.nextToken();
        return deserializeObject(p, ctxt, ctxt.getNodeFactory());
    }
    if (t == JsonToken.FIELD_NAME) {
        return deserializeObject(p, ctxt, ctxt.getNodeFactory());
    }
    if (t == JsonToken.END_OBJECT) {
        return ctxt.getNodeFactory().objectNode();
    }
    throw ctxt.mappingException(ObjectNode.class);
}
