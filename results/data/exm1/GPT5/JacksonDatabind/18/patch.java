protected MappingIterator(JavaType type, JsonParser p, DeserializationContext ctxt,
            JsonDeserializer<?> deser,
            boolean managedParser, Object valueToUpdate)
    {
        _type = type;
        _parser = p;
        _context = ctxt;
        _deserializer = (JsonDeserializer<T>) deser;
        _closeParser = managedParser;
        if (valueToUpdate == null) {
            _updatedValue = null;
        } else {
            _updatedValue = (T) valueToUpdate;
        }

        if (managedParser && (p != null) && p.isExpectedStartArrayToken()) {
                p.clearCurrentToken();
        }
    }