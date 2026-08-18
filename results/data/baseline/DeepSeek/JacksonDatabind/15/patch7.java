public boolean isEmpty(SerializerProvider prov, Object value)
    {
        Object delegateValue = convertValue(value);
        JsonSerializer<Object> ser = _delegateSerializer;
        if (ser == null) {
            ser = prov.findValueSerializer(delegateValue.getClass());
        }
        return ser.isEmpty(prov, delegateValue);
    }