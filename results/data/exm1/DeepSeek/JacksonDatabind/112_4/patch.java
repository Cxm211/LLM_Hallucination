public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<Object> delegate = null;
        if (_valueInstantiator != null) {
            AnnotatedWithParams delegateCreator = _valueInstantiator.getDelegateCreator();
            if (delegateCreator != null) {
                JavaType delegateType = _valueInstantiator.getDelegateType(ctxt.getConfig());
                delegate = findDeserializer(ctxt, delegateType, property);
            }
            AnnotatedWithParams arrayDelegateCreator = _valueInstantiator.getArrayDelegateCreator();
            if (arrayDelegateCreator != null) {
                JavaType arrayDelegateType = _valueInstantiator.getArrayDelegateType(ctxt.getConfig());
                delegate = findDeserializer(ctxt, arrayDelegateType, property);
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
            valueDeser = ctxt.handleSecondaryContextualization(valueDeser, property, valueType);
        }
        Boolean unwrapSingle = findFormatFeature(ctxt, property, Collection.class, JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        NullValueProvider nuller = findContentNullProvider(ctxt, property, valueDeser);
        if (isDefaultDeserializer(valueDeser)) {
            valueDeser = null;
        }
        return withResolved(delegate, valueDeser, nuller, unwrapSingle);
    }