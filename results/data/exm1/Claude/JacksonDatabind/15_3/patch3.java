public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException
{
    Object delegateValue = convertValue(value);
    if (delegateValue == null) {
        provider.defaultSerializeNull(gen);
        return;
    }
    _delegateSerializer.serialize(delegateValue, gen, provider);
}