public void getAndFilter(Object bean, JsonGenerator gen, SerializerProvider provider,
            PropertyFilter filter)
                    throws Exception
    {
        Object value = _accessor.getValue(bean);
        if (value == null) {
            return;
        }
        if (!(value instanceof Map<?,?>)) {
            throw new JsonMappingException("Value returned by 'any-getter' ("
                    +_accessor.getName()+"()) not java.util.Map but "+value.getClass().getName());
        }
        if (_mapSerializer != null) {
            _mapSerializer.serializeFilteredFields((Map<?,?>) value, gen, provider, filter, null);
            return;
        }
        JsonSerializer<Object> ser = provider.findValueSerializer(Map.class, _property);
        if (ser instanceof MapSerializer) {
            ((MapSerializer) ser).serializeFilteredFields((Map<?,?>) value, gen, provider, filter, null);
        } else {
            ser.serialize(value, gen, provider);
        }
    }