public AnyGetterWriter(BeanProperty property,
            AnnotatedMember accessor, MapSerializer serializer)
    {
        _accessor = accessor;
        _property = property;
        _mapSerializer = serializer;
    }