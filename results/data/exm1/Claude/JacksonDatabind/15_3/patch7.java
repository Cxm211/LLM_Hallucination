public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
    throws JsonMappingException
{
    _delegateSerializer.acceptJsonFormatVisitor(visitor, typeHint);
}