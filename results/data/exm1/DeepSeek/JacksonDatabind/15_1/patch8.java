    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
        throws JsonMappingException
    {
        JsonSerializer<?> ser = _delegateSerializer;
        if (ser == null) {
            ser = visitor.getProvider().findValueSerializer(_delegateType);
        }
        ser.acceptJsonFormatVisitor(visitor, typeHint);
    }