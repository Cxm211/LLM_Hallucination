public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
            if (value instanceof Enum<?>) {
                Enum<?> en = (Enum<?>) value;
                String name = en.name();
                try {
                    java.lang.reflect.Field f = en.getDeclaringClass().getField(name);
                    com.fasterxml.jackson.annotation.JsonProperty prop = f.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
                    if (prop != null) {
                        String v = prop.value();
                        if (v != null && !v.isEmpty()) {
                            name = v;
                        }
                    }
                } catch (Exception e) {
                    // Fallback to default enum name if reflection fails
                }
                g.writeFieldName(name);
                return;
            }
            g.writeFieldName((String) value);
        }