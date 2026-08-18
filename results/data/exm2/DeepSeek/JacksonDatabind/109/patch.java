    public JsonSerializer<?> createContextual(SerializerProvider prov,
            BeanProperty property) throws JsonMappingException
    {
        JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
        if (format != null) {
            switch (format.getShape()) {
            case STRING:
                // [databind#2264]: Need special handling for `BigDecimal`
                if (handledType() == BigDecimal.class && prov.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
                    // Return a serializer that writes BigDecimal as plain string
                    return new JsonSerializer<BigDecimal>() {
                        @Override
                        public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                            gen.writeString(value.toPlainString());
                        }
                    };
                }
                return ToStringSerializer.instance;
            default:
            }
        }
        return this;
    }