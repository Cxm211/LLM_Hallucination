public Object deserializeFromString(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        if (_objectIdReader != null) {
            return deserializeFromObjectId(p, ctxt);
        }
        /* Bit complicated if we have delegating creator; may need to use it,
         * or might not...
         */
        if (_delegateDeserializer != null) {
            if (!_valueInstantiator.canCreateFromString()) {
                Object bean = _valueInstantiator.createUsingDelegate(ctxt,
                        _delegateDeserializer.deserialize(p, ctxt));
                if (_injectables != null) {
                    injectValues(ctxt, bean);
                }
                return bean;
            }
        }
        return _valueInstantiator.createFromString(ctxt, p.getText());
    }