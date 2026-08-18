public boolean isEmpty(SerializerProvider prov, Object value)
    {
        Object delegateValue = convertValue(value);
        return _delegateSerializer == null || _delegateSerializer.isEmpty(prov, delegateValue);
    }