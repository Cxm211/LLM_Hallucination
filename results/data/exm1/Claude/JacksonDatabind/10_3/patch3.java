public void resolve(SerializerProvider provider) throws JsonMappingException
{
    if (_mapSerializer != null) {
        JsonSerializer<?> ser = provider.handlePrimaryContextualization(_mapSerializer, _property);
        if (ser != null && ser != _mapSerializer) {
            _mapSerializer = (MapSerializer) ser;
        }
    }
}