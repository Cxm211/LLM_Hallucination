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
            if (_mapSerializer instanceof MapSerializer) {
                ((MapSerializer)_mapSerializer).serializeFields((Map<?,?>) value, gen, provider);
            } else {
                _mapSerializer.serialize(value, gen, provider);
            }
            return;
        }
    }