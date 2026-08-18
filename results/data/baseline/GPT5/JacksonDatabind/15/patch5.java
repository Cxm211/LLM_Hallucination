public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
            TypeSerializer typeSer) throws IOException
    {
        /* 03-Oct-2012, tatu: This is actually unlikely to work ok... but for now,
         *    let's give it a chance?
         */
        Object delegateValue = convertValue(value);
        JsonSerializer<Object> ser = (JsonSerializer<Object>) _delegateSerializer;
        if (ser == null) {
            JavaType t = _delegateType;
            if (t == null) {
                t = _converter.getOutputType(provider.getTypeFactory());
            }
            ser = (JsonSerializer<Object>) provider.findValueSerializer(t);
        }
        ser.serializeWithType(delegateValue, gen, provider, typeSer);
    }