    protected JsonSerializer<Object> findConvertingSerializer(SerializerProvider provider,
            BeanPropertyWriter prop)
        throws JsonMappingException
    {
        final AnnotationIntrospector intr = provider.getAnnotationIntrospector();
        if (intr != null) {
            AnnotatedMember m = prop.getMember();
            if (m != null) {
                Object convDef = intr.findSerializationConverter(m);
                if (convDef != null) {
                    Converter<Object,Object> conv = provider.converterInstance(prop.getMember(), convDef);
                    JavaType delegateType = conv.getOutputType(provider.getTypeFactory());
                    // [databind#731]: Should skip if nominally java.lang.Object
                    if (delegateType.getRawClass() == Object.class) {
                        return null;
                    }
                    JsonSerializer<?> ser = provider.findValueSerializer(delegateType, prop);
                    return new StdDelegatingSerializer(conv, delegateType, ser);
                }
            }
        }
        return null;
    }