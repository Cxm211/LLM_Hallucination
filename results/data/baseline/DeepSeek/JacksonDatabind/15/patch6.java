public boolean isEmpty(Object value)
    {
        if (_delegateSerializer == null) {
            return false;
        }
        Object delegateValue = convertValue(value);
        return _delegateSerializer.isEmpty(delegateValue);
    }