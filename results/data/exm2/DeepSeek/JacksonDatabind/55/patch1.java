        public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
            if (value instanceof Enum) {
                Enum<?> e = (Enum<?>) value;
                AnnotationIntrospector intr = provider.getAnnotationIntrospector();
                if (intr != null) {
                    String name = intr.findEnumValue(e);
                    if (name != null) {
                        g.writeFieldName(name);
                        return;
                    }
                }
                g.writeFieldName(e.name());
            } else {
                g.writeFieldName(value.toString());
            }
        }