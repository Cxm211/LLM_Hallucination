public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(value.intValue());
    }