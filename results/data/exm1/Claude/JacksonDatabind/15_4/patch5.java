public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
            TypeSerializer typeSer) throws IOException
    {
        Object delegateValue = convertValue(value);
        _delegateSerializer.serializeWithType(delegateValue, gen, provider, typeSer);
    }