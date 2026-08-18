    public void resolve(SerializerProvider provider) throws JsonMappingException
    {
        _mapSerializer = provider.handlePrimaryContextualization(_mapSerializer, _property);
    }