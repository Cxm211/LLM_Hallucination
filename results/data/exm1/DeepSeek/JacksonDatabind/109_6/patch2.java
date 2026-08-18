public JsonSerializer<?> createContextual(SerializerProvider prov,
            BeanProperty property) throws JsonMappingException
        {
            JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
            if (format != null) {
                switch (format.getShape()) {
                case STRING:
                    if (handledType() == BigDecimal.class) {
                        return BigDecimalAsStringSerializer.instance;
                    }
                    return ToStringSerializer.instance;
                default:
                }
            }
            return this;
        }