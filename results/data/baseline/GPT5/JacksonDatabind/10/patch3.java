public void resolve(SerializerProvider provider) throws JsonMappingException
    {
        if (_mapSerializer != null) {
            _mapSerializer = (MapSerializer) provider.handlePrimaryContextualization(_mapSerializer, _property);
        } else if (_serializer != null) {
            _serializer = provider.handlePrimaryContextualization(_serializer, _property);
        }
    }