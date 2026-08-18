public boolean isEmpty(SerializerProvider prov, Object value)
{
    Object delegateValue = convertValue(value);
    if (_delegateSerializer == null) {
        if (delegateValue == null) {
            return true;
        }
        JsonSerializer<Object> ser = (JsonSerializer<Object>) prov.findValueSerializer(delegateValue.getClass());
        return ser.isEmpty(prov, delegateValue);
    }
    return _delegateSerializer.isEmpty(prov, delegateValue);
}