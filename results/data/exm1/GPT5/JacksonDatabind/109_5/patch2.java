public JsonSerializer<?> createContextual2(SerializerProvider prov,
                BeanProperty property) throws JsonMappingException
        {
            // Delegate to primary createContextual to avoid duplicate method signature
            return createContextual(prov, property);
        }