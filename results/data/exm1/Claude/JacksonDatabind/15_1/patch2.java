public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
    throws JsonMappingException
{
    JsonSerializer<?> delSer = _delegateSerializer;
    JavaType delegateType = _delegateType;

    if (delSer == null) {
        if (delegateType == null) {
            delegateType = _converter.getOutputType(provider.getTypeFactory());
        }
        if (!delegateType.isJavaLangObject()) {
            delSer = provider.findValueSerializer(delegateType);
        }
    }
    if (delSer instanceof ContextualSerializer) {
        delSer = provider.handleSecondaryContextualization(delSer, property);
    }
    return (delSer == _delegateSerializer) ? this
            : withDelegate(_converter, delegateType, delSer);
}