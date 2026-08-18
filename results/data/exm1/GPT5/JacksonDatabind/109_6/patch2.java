public JsonSerializer<?> createContextual(SerializerProvider prov,
                BeanProperty property, Object... unused) throws JsonMappingException
        {
            // Delegate to primary implementation to retain behavior; overload avoids duplicate signature
            return createContextual(prov, property);
        }