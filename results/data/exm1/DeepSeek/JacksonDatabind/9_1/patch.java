    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null) {
            jgen.writeFieldName("null");
            return;
        }
        if (value instanceof Date) {
            provider.defaultSerializeDateKey((Date) value, jgen);
            return;
        } else {
            jgen.writeFieldName(value.toString());
        }
    }