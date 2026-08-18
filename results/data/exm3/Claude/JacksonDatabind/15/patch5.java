public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
        TypeSerializer typeSer) throws IOException
{
    /* 03-Oct-2012, tatu: This is actually unlikely to work ok... but for now,
     *    let's give it a chance?
     */
    Object delegateValue = convertValue(value);
    if (_delegateSerializer != null) {
        _delegateSerializer.serializeWithType(delegateValue, gen, provider, typeSer);
    } else {
        provider.findTypedValueSerializer(_delegateType, true, null).serializeWithType(delegateValue, gen, provider, typeSer);
    }
}