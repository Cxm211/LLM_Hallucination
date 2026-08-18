public JsonSerializer<?> createContextual(SerializerProvider prov,
            BeanProperty property) throws JsonMappingException
    {
        JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
        if (format != null) {
            switch (format.getShape()) {
            case STRING:
                // [databind#2264]: Need special handling for `BigDecimal`
                if (((Class<?>) handledType()) == BigDecimal.class && prov != null
                        && prov.isEnabled(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)) {
                    return new JsonSerializer<Object>() {
                        @Override
                        public void serialize(Object value, com.fasterxml.jackson.core.JsonGenerator gen,
                                SerializerProvider serializers) throws java.io.IOException {
                            if (value == null) {
                                serializers.defaultSerializeNull(gen);
                                return;
                            }
                            java.math.BigDecimal bd = (java.math.BigDecimal) value;
                            gen.writeString(bd.toPlainString());
                        }
                    };
                }
                return ToStringSerializer.instance;
            default:
            }
        }
        return this;
    }