    public JsonSerializer<?> createContextual(SerializerProvider prov,
            BeanProperty property) throws JsonMappingException
    {
        JsonSerializer<?> ser = super.createContextual(prov, property);
        if (ser != this) {
            return ser;
        }
        JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
        if (format != null) {
            switch (format.getShape()) {
            case STRING:
                return ToStringSerializer.instance;
            default:
            }
        }
        return this;
    }