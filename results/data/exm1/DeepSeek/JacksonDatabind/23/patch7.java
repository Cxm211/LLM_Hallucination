public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(((Long) value).longValue());
    }