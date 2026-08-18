public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        String str;
        
        if (value instanceof Date) {
            provider.defaultSerializeDateKey((Date) value, jgen);
            return;
        } else if (value instanceof Class<?>) {
            jgen.writeFieldName(((Class<?>) value).getName());
            return;
        } else if (value instanceof java.util.Calendar) {
            provider.defaultSerializeDateKey(((java.util.Calendar) value).getTime(), jgen);
            return;
        } else {
            str = value.toString();
        }
        jgen.writeFieldName(str);
    }