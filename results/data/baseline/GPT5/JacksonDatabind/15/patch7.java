public boolean isEmpty(SerializerProvider prov, Object value)
    {
        Object delegateValue = convertValue(value);
        JsonSerializer<Object> ser = (JsonSerializer<Object>) _delegateSerializer;
        if (ser == null) {
            JavaType t = _delegateType;
            if (t == null) {
                t = _converter.getOutputType(prov.getTypeFactory());
            }
            ser = (JsonSerializer<Object>) prov.findValueSerializer(t);
        }
        return ser.isEmpty(prov, delegateValue);
    }