public boolean isEmpty(SerializerProvider prov, Object value)
{
    Object delegateValue = convertValue(value);
    if (_delegateSerializer != null) {
        return _delegateSerializer.isEmpty(prov, delegateValue);
    }
    return delegateValue == null;
}