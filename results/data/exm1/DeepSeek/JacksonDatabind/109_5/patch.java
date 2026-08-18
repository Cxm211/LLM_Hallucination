public JsonSerializer<?> createContextual(SerializerProvider prov,
            BeanProperty property) throws JsonMappingException
    {
        JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
        if (format != null) {
            switch (format.getShape()) {
            case STRING:
                if (BigDecimal.class.isAssignableFrom(handledType())) {
                    return new JsonSerializer<BigDecimal>() {
                        @Override
                        public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                            if (provider.isEnabled(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)) {
                                gen.writeString(value.toPlainString());
                            } else {
                                gen.writeString(value.toString());
                            }
                        }
                    };
                }
                return ToStringSerializer.instance;
            default:
            }
        }
        return this;
    }