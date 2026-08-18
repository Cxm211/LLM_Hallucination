public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentTokenId()) {
            case JsonTokenId.ID_START_OBJECT:
                {
                    JsonToken t = p.nextToken();
                    if (t == JsonToken.END_OBJECT) {
                        return new LinkedHashMap<String,Object>(2);
                    }
                }
                break;
            case JsonTokenId.ID_FIELD_NAME:
                return mapObject(p, ctxt);
            case JsonTokenId.ID_START_ARRAY:
                {
                    JsonToken t = p.nextToken();
                    if (t == JsonToken.END_ARRAY) {
                        if (ctxt.isEnabled(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)) {
                            return NO_OBJECTS;
                        }
                        return new ArrayList<Object>(2);
                    }
                }
                if (ctxt.isEnabled(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)) {
                    return mapArrayToArray(p, ctxt);
                }
                return mapArray(p, ctxt);
            case JsonTokenId.ID_EMBEDDED_OBJECT:
                return p.getEmbeddedObject();
            case JsonTokenId.ID_STRING:
                return p.getText();

            case JsonTokenId.ID_NUMBER_INT:
                if (ctxt.hasSomeOfFeatures(F_MASK_INT_COERCIONS)) {
                    return _coerceIntegral(p, ctxt);
                }
                return p.getNumberValue();

            case JsonTokenId.ID_NUMBER_FLOAT:
                if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return p.getDecimalValue();
                }
                return Double.valueOf(p.getDoubleValue());

            case JsonTokenId.ID_TRUE:
                return Boolean.TRUE;
            case JsonTokenId.ID_FALSE:
                return Boolean.FALSE;

            case JsonTokenId.ID_NULL:
                return null;

            default:
                throw ctxt.mappingException(Object.class);
            }
            return mapObject(p, ctxt);
        }