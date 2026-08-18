public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null) {
            jgen.writeFieldName("null");
            return;
        }
        if (value instanceof Date) {
            provider.defaultSerializeDateKey((Date) value, jgen);
            return;
        }
        if (value instanceof Calendar) {
            provider.defaultSerializeDateKey(((Calendar) value).getTime(), jgen);
            return;
        }
        String str = (value instanceof Class) ? ((Class<?>) value).getName() : value.toString();
        jgen.writeFieldName(str);
    }