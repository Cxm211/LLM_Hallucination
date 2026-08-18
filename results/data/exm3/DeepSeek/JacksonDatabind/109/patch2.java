        public JsonSerializer<?> createContextual(SerializerProvider prov,
                BeanProperty property) throws JsonMappingException
        {
            JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
            if (format != null) {
                switch (format.getShape()) {
                case STRING:
                    if (handledType() == BigDecimal.class) {
                        return new JsonSerializer<BigDecimal>() {
                            @Override
                            public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                                gen.writeString(value == null ? "null" :
                                    (gen.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN) ?
                                        value.toPlainString() : value.toString()));
                            }
                        };
                    }
                    return ToStringSerializer.instance;
                default:
                }
            }
            return this;
        }