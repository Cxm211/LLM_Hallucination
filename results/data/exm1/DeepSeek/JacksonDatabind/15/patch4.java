public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException
    {
        Object delegateValue = convertValue(value);
        // should we accept nulls?
        if (delegateValue == null) {
            provider.defaultSerializeNull(gen);
            return;
        }
        if (_delegateSerializer != null) {
            _delegateSerializer.serialize(delegateValue, gen, provider);
        } else {
            provider.defaultSerializeValue(delegateValue, gen);
        }
    }