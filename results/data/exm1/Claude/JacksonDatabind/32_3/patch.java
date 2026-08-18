public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        switch (p.getCurrentTokenId()) {
        case JsonTokenId.ID_START_OBJECT:
        case JsonTokenId.ID_FIELD_NAME:
            if (_mapDeserializer != null) {
                return _mapDeserializer.deserialize(p, ctxt);
            }
            return mapObject(p, ctxt);
        case JsonTokenId.ID_START_ARRAY:
            if (ctxt.isEnabled(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)) {
                return mapArrayToArray(p, ctxt);
            }
            if (_listDeserializer != null) {
                return _listDeserializer.deserialize(p, ctxt);
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