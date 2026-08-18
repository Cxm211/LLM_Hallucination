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
        } else {
            // fallback: use default map serializer from provider
            JsonSerializer<Object> ser = provider.findValueSerializer(provider.constructType(Map.class), null);
            ser.serialize(value, gen, provider);
        }
    }