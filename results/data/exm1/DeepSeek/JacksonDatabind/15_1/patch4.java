    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException
    {
        Object delegateValue = convertValue(value);
        if (delegateValue == null) {
            provider.defaultSerializeNull(gen);
            return;
        }
        JsonSerializer<?> ser = _delegateSerializer;
        if (ser == null) {
            ser = provider.findValueSerializer(_delegateType);
        }
        ser.serialize(delegateValue, gen, provider);
    }