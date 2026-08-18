public JsonSerializer<?> createContextual(SerializerProvider prov,
                BeanProperty property) throws JsonMappingException
        {
            JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
            if (format != null) {
                switch (format.getShape()) {
                case STRING:
                    return ToStringSerializer.instance;
                default:
                }
            }
            // Check if WRITE_BIGDECIMAL_AS_PLAIN is enabled for BigDecimal
            if (prov.isEnabled(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN) && handledType() == BigDecimal.class) {
                return this;
            }
            return this;
        }