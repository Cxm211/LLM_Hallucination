public void resolve(SerializerProvider provider) throws JsonMappingException
    {
        if (_mapSerializer != null) {
            _mapSerializer = (MapSerializer) provider.handlePrimaryContextualization(_mapSerializer, _property);
        }
    }