    public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
            TypeSerializer typeSer) throws IOException
    {
        Object delegateValue = convertValue(value);
        JsonSerializer<?> ser = _delegateSerializer;
        if (ser == null) {
            ser = provider.findValueSerializer(_delegateType);
        }
        ser.serializeWithType(delegateValue, gen, provider, typeSer);
    }