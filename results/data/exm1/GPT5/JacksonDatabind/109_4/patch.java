public JsonSerializer<?> createContextual(SerializerProvider prov,
            BeanProperty property) throws JsonMappingException
    {
        JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
        if (format != null) {
            JsonFormat.Shape shape = format.getShape();
            if (shape == JsonFormat.Shape.STRING) {
                // [databind#2264]: Need special handling for `BigDecimal`
                return ToStringSerializer.instance;
            }
        }
        return this;
    }