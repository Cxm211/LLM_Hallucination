public Object deserializeFromDouble(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        NumberType t = p.getNumberType();
        if ((t == NumberType.DOUBLE) || (t == NumberType.FLOAT)) {
            if (_delegateDeserializer != null) {
                if (!_valueInstantiator.canCreateFromDouble()) {
                    Object bean = _valueInstantiator.createUsingDelegate(ctxt,
                            _delegateDeserializer.deserialize(p, ctxt));
                    if (_injectables != null) {
                        injectValues(ctxt, bean);
                    }
                    return bean;
                }
            }
            return _valueInstantiator.createFromDouble(ctxt, p.getDoubleValue());
        }
        if (_delegateDeserializer != null) {
            Object bean = _valueInstantiator.createUsingDelegate(ctxt,
                    _delegateDeserializer.deserialize(p, ctxt));
            if (_injectables != null) {
                injectValues(ctxt, bean);
            }
            return bean;
        }
        return ctxt.handleMissingInstantiator(handledType(), p,
                "no suitable creator method found to deserialize from Number value (%s)",
                p.getNumberValue());
    }