public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
        TypeSerializer typeSer) throws IOException
{
    Object delegateValue = convertValue(value);
    JsonSerializer<Object> ser = _delegateSerializer;
    if (ser == null) {
        ser = provider.findValueSerializer(delegateValue.getClass());
    }
    ser.serializeWithType(delegateValue, gen, provider, typeSer);
}