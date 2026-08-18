public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException
    {
        Object delegateValue = convertValue(value);
        // should we accept nulls?
        if (delegateValue == null) {
            provider.defaultSerializeNull(gen);
            return;
        }
        // 02-Apr-2015, tatu: As per [databind#731] may need to do dynamic lookup
        JsonSerializer<Object> ser = _delegateSerializer;
        if (ser == null) {
            ser = provider.findValueSerializer(_delegateType);
        }
        ser.serialize(delegateValue, gen, provider);
    }