public ShortSerializer() { super(Short.class, JsonParser.NumberType.INT, "number"); }

        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(((Short) value).shortValue());
        }
