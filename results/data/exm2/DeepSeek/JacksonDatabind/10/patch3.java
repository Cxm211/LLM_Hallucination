    public void resolve(SerializerProvider provider) throws JsonMappingException
    {
        // 05-Sep-2013, tatu: I _think_ this can be considered a primary property...
        JsonSerializer<?> ser = provider.handlePrimaryContextualization(_mapSerializer, _property);
        if (ser instanceof MapSerializer) {
            _mapSerializer = (MapSerializer) ser;
        } else {
            _mapSerializer = null;
        }
    }