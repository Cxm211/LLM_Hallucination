public void getAndFilter(Object bean, JsonGenerator gen, SerializerProvider provider,
            PropertyFilter filter)
                    throws Exception
    {
        Object value = _accessor.getValue(bean);
        if (value == null) {
            return;
        }
        if (_mapSerializer != null) {
            if (!(value instanceof Map<?,?>)) {
                throw new JsonMappingException("Value returned by 'any-getter' ("
                        +_accessor.getName()+"()) not java.util.Map but "+value.getClass().getName());
            }
            _mapSerializer.serializeFilteredFields((Map<?,?>) value, gen, provider, filter, null);
            return;
        }
        if (_serializer != null) {
            _serializer.serialize(value, gen, provider);
            return;
        }
        // ... not sure how custom handler would do it
    }