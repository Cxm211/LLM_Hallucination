public boolean isEmpty(Object value)
{
    Object delegateValue = convertValue(value);
    if (_delegateSerializer == null) {
        return (delegateValue == null);
    }
    return _delegateSerializer.isEmpty(delegateValue);
}