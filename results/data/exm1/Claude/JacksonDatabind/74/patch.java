protected Object _deserializeTypedUsingDefaultImpl(JsonParser p, DeserializationContext ctxt,
            TokenBuffer tb) throws IOException
    {
        JsonDeserializer<Object> deser = _findDefaultImplDeserializer(ctxt);
        if (deser != null) {
            if (tb != null) {
                tb.writeEndObject();
                p = tb.asParser(p);
                p.nextToken();
            }
            return deser.deserialize(p, ctxt);
        }
        Object result = TypeDeserializer.deserializeIfNatural(p, ctxt, _baseType);
        if (result != null) {
            return result;
        }
        if (p.getCurrentToken() == JsonToken.START_ARRAY) {
            return super.deserializeTypedFromAny(p, ctxt);
        }
        if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
            return super.deserializeTypedFromAny(p, ctxt);
        }
        ctxt.reportWrongTokenException(p, JsonToken.FIELD_NAME,
                "missing property '"+_typePropertyName+"' that is to contain type id  (for class "+baseTypeName()+")");
        return null;
    }