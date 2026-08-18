public AnyGetterWriter(BeanProperty property,
            AnnotatedMember accessor, MapSerializer serializer)
    {
        _accessor = accessor;
        _property = property;
        _mapSerializer = (serializer instanceof MapSerializer) ? serializer : null;
        _serializer = (serializer instanceof MapSerializer) ? null : (JsonSerializer<Object>) serializer;
    }