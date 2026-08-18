public DoubleSerializer() { super(Double.class, JsonParser.NumberType.DOUBLE, "number"); }

        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(((Double) value).doubleValue());
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, Object value) {
            return value == null || ((Double) value).doubleValue() == 0.0d;
        }