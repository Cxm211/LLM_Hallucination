        public LongSerializer() { super(Long.class, JsonParser.NumberType.LONG, "number"); }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov,
                BeanProperty property) throws JsonMappingException
        {
            if (property != null) {
                AnnotatedMember m = property.getMember();
                if (m != null) {
                    JsonFormat.Value format = prov.getAnnotationIntrospector().findFormat(m);
                    if (format != null) {
                        switch (format.getShape()) {
                        case STRING:
                            return ToStringSerializer.instance;
                        default:
                        }
                    }
                }
            }
            return this;
        }