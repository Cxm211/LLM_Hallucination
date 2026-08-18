public boolean isEmpty(SerializerProvider prov, Object value)
    {
        Object delegateValue = convertValue(value);
        JsonSerializer<Object> ser = _delegateSerializer;
        if (ser == null) {
            ser = prov.findValueSerializer(_delegateType);
        }
        return ser.isEmpty(prov, delegateValue);
    }