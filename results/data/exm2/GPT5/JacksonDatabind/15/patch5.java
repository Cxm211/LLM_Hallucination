public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
        TypeSerializer typeSer) throws IOException
{
    /* 03-Oct-2012, tatu: This is actually unlikely to work ok... but for now,
     *    let's give it a chance?
     */
    Object delegateValue = convertValue(value);
    JsonSerializer<Object> ser = (JsonSerializer<Object>) _delegateSerializer;
    if (ser == null && delegateValue != null) {
        ser = provider.findValueSerializer(delegateValue.getClass());
    }
    if (ser != null) {
        ser.serializeWithType(delegateValue, gen, provider, typeSer);
    } else {
        provider.defaultSerializeNull(gen);
    }
}