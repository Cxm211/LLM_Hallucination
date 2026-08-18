public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
        TypeSerializer typeSer) throws IOException
{
    Object delegateValue = convertValue(value);
    if (_delegateSerializer != null) {
        _delegateSerializer.serializeWithType(delegateValue, gen, provider, typeSer);
    } else {
        provider.defaultSerializeValue(delegateValue, gen);
    }
}