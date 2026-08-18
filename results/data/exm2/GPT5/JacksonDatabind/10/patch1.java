public void getAndSerialize(Object bean, JsonGenerator gen, SerializerProvider provider)
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
        _mapSerializer.serializeFields((Map<?,?>) value, gen, provider);
        return;
    }
    JsonSerializer<Object> ser = provider.findValueSerializer(value.getClass(), _property);
    ser.serialize(value, gen, provider);
}