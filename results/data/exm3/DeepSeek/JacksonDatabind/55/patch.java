    public static JsonSerializer<Object> getFallbackKeySerializer(SerializationConfig config,
            Class<?> rawKeyType)
    {
        // Return a serializer that handles enums with annotations, and falls back to toString for other types.
        return new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                if (value instanceof Enum) {
                    Enum<?> e = (Enum<?>) value;
                    String name = provider.getAnnotationIntrospector().findEnumValue(e);
                    if (name == null) {
                        name = e.name();
                    }
                    gen.writeFieldName(name);
                } else {
                    gen.writeFieldName(value.toString());
                }
            }
        };
    }