        public JsonSerializer<?> createContextualHelper_Duplicate(SerializerProvider prov,
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
            return this;
        }