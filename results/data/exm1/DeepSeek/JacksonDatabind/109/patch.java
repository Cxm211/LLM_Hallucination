public JsonSerializer<?> createContextual(SerializerProvider prov,
            BeanProperty property) throws JsonMappingException
    {
        JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
        if (format != null) {
            switch (format.getShape()) {
            case STRING:
                if (BigDecimal.class.isAssignableFrom(handledType())) {
                    return ToStringSerializer.instance;
                }
                return super.createContextual(prov, property);
            default:
            }
        }
        return this;
    }