public boolean isEmpty(Object value)
    {
        Object delegateValue = convertValue(value);
        JsonSerializer<Object> ser = (JsonSerializer<Object>) _delegateSerializer;
        if (ser == null) {
            return (delegateValue == null);
        }
        return ser.isEmpty(delegateValue);
    }