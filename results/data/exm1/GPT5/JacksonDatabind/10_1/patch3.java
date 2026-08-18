public void resolve(SerializerProvider provider) throws JsonMappingException
    {
        _mapSerializer = (MapSerializer) provider.handlePrimaryContextualization(_mapSerializer, _property);
    }