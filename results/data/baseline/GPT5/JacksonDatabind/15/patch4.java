public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException
    {
        Object delegateValue = convertValue(value);
        // should we accept nulls?
        if (delegateValue == null) {
            provider.defaultSerializeNull(gen);
            return;
        }
        // 02-Apr-2015, tatu: As per [databind#731] may need to do dynamic lookup
        JsonSerializer<Object> ser = (JsonSerializer<Object>) _delegateSerializer;
        if (ser == null) {
            JavaType t = _delegateType;
            if (t == null) {
                t = _converter.getOutputType(provider.getTypeFactory());
            }
            ser = (JsonSerializer<Object>) provider.findValueSerializer(t);
        }
        ser.serialize(delegateValue, gen, provider);
    }