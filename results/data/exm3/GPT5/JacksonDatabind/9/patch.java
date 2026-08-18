public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value instanceof Date) {
            provider.defaultSerializeDateKey((Date) value, jgen);
            return;
        }
        if (value instanceof java.util.Calendar) {
            provider.defaultSerializeDateKey(((java.util.Calendar) value).getTime(), jgen);
            return;
        }
        final String str;
        if (value instanceof Class<?>) {
            str = ((Class<?>) value).getName();
        } else {
            str = String.valueOf(value);
        }
        jgen.writeFieldName(str);
    }