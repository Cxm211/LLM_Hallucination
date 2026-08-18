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
    // 19-Oct-2014, tatu: Should we try to support @JsonInclude options here?
    if (_mapSerializer != null) {
        _mapSerializer.serializeFilteredFields((Map<?,?>) value, gen, provider, filter, null);
        return;
    }
    // Fallback: serialize without filter using default Map serializer
    provider.findValueSerializer(Map.class).serialize(value, gen, provider);
}
