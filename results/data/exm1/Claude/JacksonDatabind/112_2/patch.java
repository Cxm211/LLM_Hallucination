public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException
    {
        
        JsonDeserializer<Object> delegate = null;
        if (_valueInstantiator != null) {
            
            AnnotatedWithParams delegateCreator = _valueInstantiator.getDelegateCreator();
            if (delegateCreator != null) {
                JavaType delegateType = _valueInstantiator.getDelegateType(ctxt.getConfig());
                delegate = findDeserializer(ctxt, delegateType, property);
            }
        }
        JsonDeserializer<?> valueDeser = _valueDeserializer;
        final JavaType valueType = _containerType.getContentType();
        if (valueDeser == null) {
            
            valueDeser = findConvertingContentDeserializer(ctxt, property, valueDeser);
            if (valueDeser == null) {
            
                valueDeser = ctxt.findContextualValueDeserializer(valueType, property);
            }
        } else { 
            
            if (valueDeser instanceof ContextualDeserializer) {
                valueDeser = ((ContextualDeserializer) valueDeser).createContextual(ctxt, property);
            }
        }
        
        
        Boolean unwrapSingle = findFormatFeature(ctxt, property, Collection.class,
                JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        NullValueProvider nuller = findContentNullProvider(ctxt, property, valueDeser);
        if (isDefaultDeserializer(valueDeser)) {
            valueDeser = null;
        }
        return withResolved(delegate, valueDeser, nuller, unwrapSingle);
    }