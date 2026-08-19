// ===== FIXED com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler :: _deserialize(JsonParser, DeserializationContext, int, String) [lines 222-242] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-29-fixed/src/main/java/com/fasterxml/jackson/databind/deser/impl/ExternalTypeHandler.java =====
    protected final Object _deserialize(JsonParser p, DeserializationContext ctxt,
            int index, String typeId) throws IOException
    {
        JsonParser p2 = _tokens[index].asParser(p);
        JsonToken t = p2.nextToken();
        // 29-Sep-2015, tatu: As per [databind#942], nulls need special support
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }

        TokenBuffer merged = new TokenBuffer(p);
        merged.writeStartArray();
        merged.writeString(typeId);
        merged.copyCurrentStructure(p2);
        merged.writeEndArray();

        // needs to point to START_OBJECT (or whatever first token is)
        JsonParser mp = merged.asParser(p);
        mp.nextToken();
        return _properties[index].getProperty().deserialize(mp, ctxt);
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler :: _deserializeAndSet(JsonParser, DeserializationContext, Object, int, String) [lines 245-268] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-29-fixed/src/main/java/com/fasterxml/jackson/databind/deser/impl/ExternalTypeHandler.java =====
    protected final void _deserializeAndSet(JsonParser p, DeserializationContext ctxt,
            Object bean, int index, String typeId) throws IOException
    {
        /* Ok: time to mix type id, value; and we will actually use "wrapper-array"
         * style to ensure we can handle all kinds of JSON constructs.
         */
        JsonParser p2 = _tokens[index].asParser(p);
        JsonToken t = p2.nextToken();
        // 29-Sep-2015, tatu: As per [databind#942], nulls need special support
        if (t == JsonToken.VALUE_NULL) {
            _properties[index].getProperty().set(bean, null);
            return;
        }
        TokenBuffer merged = new TokenBuffer(p);
        merged.writeStartArray();
        merged.writeString(typeId);
        
        merged.copyCurrentStructure(p2);
        merged.writeEndArray();
        // needs to point to START_OBJECT (or whatever first token is)
        JsonParser mp = merged.asParser(p);
        mp.nextToken();
        _properties[index].getProperty().deserializeAndSet(mp, ctxt, bean);
    }
