public CollectionDeserializer createContextual(DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException
    {
        // May need to resolve types for delegate-based creators:
        JsonDeserializer<Object> delegateDeser = null;
        if (_valueInstantiator != null) {
            if (_valueInstantiator.canCreateUsingDelegate()) {
                JavaType delegateType = null;
                // Try current/common signatures first
                try {
                    // Signature: getDelegateType(DeserializationConfig)
                    delegateType = _valueInstantiator.getDelegateType(ctxt.getConfig());
                } catch (Throwable t) {
                    // ignore, try alternative reflective lookups below
                }
                if (delegateType == null) {
                    // Some versions expose: getDelegateType(DeserializationConfig, BeanProperty)
                    try {
                        java.lang.reflect.Method m = _valueInstantiator.getClass().getMethod(
                                "getDelegateType",
                                com.fasterxml.jackson.databind.DeserializationConfig.class,
                                com.fasterxml.jackson.databind.BeanProperty.class);
                        Object dt = m.invoke(_valueInstantiator, ctxt.getConfig(), property);
                        if (dt instanceof JavaType) {
                            delegateType = (JavaType) dt;
                        }
                    } catch (Exception e) {
                        // ignore, try other variants
                    }
                }
                if (delegateType == null) {
                    // Fallback variant seen in some versions: getDelegateType(DeserializationConfig, JavaType)
                    try {
                        java.lang.reflect.Method m = _valueInstantiator.getClass().getMethod(
                                "getDelegateType",
                                com.fasterxml.jackson.databind.DeserializationConfig.class,
                                com.fasterxml.jackson.databind.JavaType.class);
                        Object dt = m.invoke(_valueInstantiator, ctxt.getConfig(), _collectionType);
                        if (dt instanceof JavaType) {
                            delegateType = (JavaType) dt;
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (delegateType == null) {
                    throw new IllegalArgumentException("Invalid delegate-creator definition for "+_collectionType
                            +": value instantiator ("+_valueInstantiator.getClass().getName()
                            +") returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'");
                }
                delegateDeser = findDeserializer(ctxt, delegateType, property);
            }
        }
        // [databind#1043]: allow per-property allow-wrapping of single overrides:
        // 11-Dec-2015, tatu: Should we pass basic `Collection.class`, or more refined? Mostly
        //   comes down to "List vs Collection" I suppose... for now, pass Collection
        Boolean unwrapSingle = findFormatFeature(ctxt, property, Collection.class,
                JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        // also, often value deserializer is resolved here:
        JsonDeserializer<?> valueDeser = _valueDeserializer;
        
        // May have a content converter
        valueDeser = findConvertingContentDeserializer(ctxt, property, valueDeser);
        final JavaType vt = _collectionType.getContentType();
        if (valueDeser == null) {
            valueDeser = ctxt.findContextualValueDeserializer(vt, property);
        } else { // if directly assigned, probably not yet contextual, so:
            valueDeser = ctxt.handleSecondaryContextualization(valueDeser, property, vt);
        }
        // and finally, type deserializer needs context as well
        TypeDeserializer valueTypeDeser = _valueTypeDeserializer;
        if (valueTypeDeser != null) {
            valueTypeDeser = valueTypeDeser.forProperty(property);
        }
        return withResolved(delegateDeser, valueDeser, valueTypeDeser, unwrapSingle);
    }