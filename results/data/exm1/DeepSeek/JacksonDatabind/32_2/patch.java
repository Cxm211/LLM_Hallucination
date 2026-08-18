public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
{
    switch (p.getCurrentTokenId()) {
    case JsonTokenId.ID_START_OBJECT:
    case JsonTokenId.ID_FIELD_NAME:
        if (_mapDeserializer != null) {
            return _mapDeserializer.deserialize(p, ctxt);
        }
        if (p.nextToken() == JsonToken.END_OBJECT) {
            return new LinkedHashMap<String,Object>(2);
        }
        return mapObject(p, ctxt);
    case JsonTokenId.ID_START_ARRAY:
        if (ctxt.isEnabled(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)) {
            return mapArrayToArray(p, ctxt);
        }
        if (_listDeserializer != null) {
            return _listDeserializer.deserialize(p, ctxt);
        }
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return new ArrayList<Object>(2);
        }
        return mapArray(p, ctxt);
    case JsonTokenId.ID_EMBEDDED_OBJECT:
        return p.getEmbeddedObject();
    case JsonTokenId.ID_STRING:
        if (_stringDeserializer != null) {
            return _stringDeserializer.deserialize(p, ctxt);
        }
        return p.getText();
    case JsonTokenId.ID_NUMBER_INT:
        if (_numberDeserializer != null) {
            return _numberDeserializer.deserialize(p, ctxt);
        }
        if (ctxt.hasSomeOfFeatures(F_MASK_INT_COERCIONS)) {
            return _coerceIntegral(p, ctxt);
        }
        return p.getNumberValue();
    case JsonTokenId.ID_NUMBER_FLOAT:
        if (_numberDeserializer != null) {
            return _numberDeserializer.deserialize(p, ctxt);
        }
        if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
            return p.getDecimalValue();
        }
        return p.getDoubleValue();
    case JsonTokenId.ID_TRUE:
        return Boolean.TRUE;
    case JsonTokenId.ID_FALSE:
        return Boolean.FALSE;
    case JsonTokenId.ID_NULL:
        return null;
    default:
    }
    throw ctxt.mappingException(Object.class);
}