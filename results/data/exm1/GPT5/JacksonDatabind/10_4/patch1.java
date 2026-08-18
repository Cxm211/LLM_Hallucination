public void getAndSerialize(Object bean, JsonGenerator gen, SerializerProvider provider)
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
            _mapSerializer.serializeFields((Map<?,?>) value, gen, provider);
            return;
        }
        // Fallback: try to find a serializer capable of serializing just fields if possible
        JsonSerializer<Object> ser = provider.findValueSerializer(Map.class, _property);
        if (ser instanceof MapSerializer) {
            ((MapSerializer) ser).serializeFields((Map<?,?>) value, gen, provider);
        } else {
            ser.serialize(value, gen, provider);
        }
    }