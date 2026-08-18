public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
    throws JsonMappingException
{
    if (_delegateSerializer != null) {
        _delegateSerializer.acceptJsonFormatVisitor(visitor, typeHint);
    }
}