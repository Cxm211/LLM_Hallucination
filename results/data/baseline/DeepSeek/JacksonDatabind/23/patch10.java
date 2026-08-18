public DoubleSerializer() { super(Double.class, JsonParser.NumberType.DOUBLE, "number"); }

        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(((Double) value).doubleValue());
        }