protected JsonSerializer<?> findConvertingContentSerializer(SerializerProvider provider,
        BeanProperty prop, JsonSerializer<?> existingSerializer)
        throws JsonMappingException
{
    final AnnotationIntrospector intr = provider.getAnnotationIntrospector();
    if (intr != null && prop != null) {
        AnnotatedMember m = prop.getMember();
        if (m != null) {
            Object convDef = intr.findSerializationContentConverter(m);
            if (convDef != null) {
                Converter<Object,Object> conv = provider.converterInstance(prop.getMember(), convDef);
                JavaType delegateType = conv.getOutputType(provider.getTypeFactory());
                if (existingSerializer == null && delegateType.getRawClass() != Object.class) {
                    existingSerializer = provider.findValueSerializer(delegateType);
                }
                return new StdDelegatingSerializer(conv, delegateType, existingSerializer);
            }
        }
    }
    return existingSerializer;
}