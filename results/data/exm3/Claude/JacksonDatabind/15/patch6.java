public boolean isEmpty(Object value)
{
    Object delegateValue = convertValue(value);
    if (_delegateSerializer != null) {
        return _delegateSerializer.isEmpty(delegateValue);
    }
    return delegateValue == null;
}