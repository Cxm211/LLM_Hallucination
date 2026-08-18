// ===== FIXED com.fasterxml.jackson.databind.deser.BeanDeserializerBase :: deserializeFromArray(JsonParser, DeserializationContext) [lines 1338-1371] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-68-fixed/src/main/java/com/fasterxml/jackson/databind/deser/BeanDeserializerBase.java =====
    public Object deserializeFromArray(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        // note: can not call `_delegateDeserializer()` since order reversed here:
        JsonDeserializer<Object> delegateDeser = _arrayDelegateDeserializer;
        // fallback to non-array delegate
        if ((delegateDeser != null) || ((delegateDeser = _delegateDeserializer) != null)) {
            Object bean = _valueInstantiator.createUsingArrayDelegate(ctxt,
                    delegateDeser.deserialize(p, ctxt));
            if (_injectables != null) {
                injectValues(ctxt, bean);
            }
            return bean;
        }
        if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            JsonToken t = p.nextToken();
            if (t == JsonToken.END_ARRAY && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
                return null;
            }
            final Object value = deserialize(p, ctxt);
            if (p.nextToken() != JsonToken.END_ARRAY) {
                handleMissingEndArrayForSingle(p, ctxt);
            }
            return value;
        }
        if (ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
            JsonToken t = p.nextToken();
            if (t == JsonToken.END_ARRAY) {
                return null;
            }
            return ctxt.handleUnexpectedToken(handledType(),
                    JsonToken.START_ARRAY, p, null);
        }
        return ctxt.handleUnexpectedToken(handledType(), p);
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.BeanDeserializerBase :: deserializeFromBoolean(JsonParser, DeserializationContext) [lines 1321-1336] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-68-fixed/src/main/java/com/fasterxml/jackson/databind/deser/BeanDeserializerBase.java =====
    public Object deserializeFromBoolean(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        JsonDeserializer<Object> delegateDeser = _delegateDeserializer();
        if (delegateDeser != null) {
            if (!_valueInstantiator.canCreateFromBoolean()) {
                Object bean = _valueInstantiator.createUsingDelegate(ctxt,
                        delegateDeser.deserialize(p, ctxt));
                if (_injectables != null) {
                    injectValues(ctxt, bean);
                }
                return bean;
            }
        }
        boolean value = (p.getCurrentToken() == JsonToken.VALUE_TRUE);
        return _valueInstantiator.createFromBoolean(ctxt, value);
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.BeanDeserializerBase :: deserializeFromDouble(JsonParser, DeserializationContext) [lines 1289-1316] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-68-fixed/src/main/java/com/fasterxml/jackson/databind/deser/BeanDeserializerBase.java =====
    public Object deserializeFromDouble(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        NumberType t = p.getNumberType();
        // no separate methods for taking float...
        if ((t == NumberType.DOUBLE) || (t == NumberType.FLOAT)) {
            JsonDeserializer<Object> delegateDeser = _delegateDeserializer();
            if (delegateDeser != null) {
                if (!_valueInstantiator.canCreateFromDouble()) {
                    Object bean = _valueInstantiator.createUsingDelegate(ctxt,
                            delegateDeser.deserialize(p, ctxt));
                    if (_injectables != null) {
                        injectValues(ctxt, bean);
                    }
                    return bean;
                }
            }
            return _valueInstantiator.createFromDouble(ctxt, p.getDoubleValue());
        }
        // actually, could also be BigDecimal, so:
        JsonDeserializer<Object> delegateDeser = _delegateDeserializer();
        if (delegateDeser != null) {
            return _valueInstantiator.createUsingDelegate(ctxt,
                    delegateDeser.deserialize(p, ctxt));
        }
        return ctxt.handleMissingInstantiator(handledType(), p,
                "no suitable creator method found to deserialize from Number value (%s)",
                p.getNumberValue());
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.BeanDeserializerBase :: deserializeFromNumber(JsonParser, DeserializationContext) [lines 1214-1260] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-68-fixed/src/main/java/com/fasterxml/jackson/databind/deser/BeanDeserializerBase.java =====
    public Object deserializeFromNumber(JsonParser p, DeserializationContext ctxt)
        throws IOException
    {
        // First things first: id Object Id is used, most likely that's it
        if (_objectIdReader != null) {
            return deserializeFromObjectId(p, ctxt);
        }
        final JsonDeserializer<Object> delegateDeser = _delegateDeserializer();
        switch (p.getNumberType()) {
        case INT:
            if (delegateDeser != null) {
                if (!_valueInstantiator.canCreateFromInt()) {
                    Object bean = _valueInstantiator.createUsingDelegate(ctxt,
                            delegateDeser.deserialize(p, ctxt));
                    if (_injectables != null) {
                        injectValues(ctxt, bean);
                    }
                    return bean;
                }
            }
            return _valueInstantiator.createFromInt(ctxt, p.getIntValue());
        case LONG:
            if (delegateDeser != null) {
                if (!_valueInstantiator.canCreateFromInt()) {
                    Object bean = _valueInstantiator.createUsingDelegate(ctxt,
                            delegateDeser.deserialize(p, ctxt));
                    if (_injectables != null) {
                        injectValues(ctxt, bean);
                    }
                    return bean;
                }
            }
            return _valueInstantiator.createFromLong(ctxt, p.getLongValue());
        }
        // actually, could also be BigInteger, so:
        if (delegateDeser != null) {
            Object bean = _valueInstantiator.createUsingDelegate(ctxt,
                    delegateDeser.deserialize(p, ctxt));
            if (_injectables != null) {
                injectValues(ctxt, bean);
            }
            return bean;
        }
        return ctxt.handleMissingInstantiator(handledType(), p,
                "no suitable creator method found to deserialize from Number value (%s)",
                p.getNumberValue());
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.BeanDeserializerBase :: deserializeFromObjectUsingNonDefault(JsonParser, DeserializationContext) [lines 1189-1207] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-68-fixed/src/main/java/com/fasterxml/jackson/databind/deser/BeanDeserializerBase.java =====
    protected Object deserializeFromObjectUsingNonDefault(JsonParser p,
            DeserializationContext ctxt) throws IOException
    {
        final JsonDeserializer<Object> delegateDeser = _delegateDeserializer();
        if (delegateDeser != null) {
            return _valueInstantiator.createUsingDelegate(ctxt,
                    delegateDeser.deserialize(p, ctxt));
        }
        if (_propertyBasedCreator != null) {
            return _deserializeUsingPropertyBased(p, ctxt);
        }
        // should only occur for abstract types...
        if (_beanType.isAbstract()) {
            return ctxt.handleMissingInstantiator(handledType(), p,
                    "abstract type (need to add/enable type information?)");
        }
        return ctxt.handleMissingInstantiator(_beanType.getRawClass(), p,
                "no suitable constructor found, can not deserialize from Object value (missing default constructor or creator, or perhaps need to add/enable type information?)");
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.BeanDeserializerBase :: deserializeFromString(JsonParser, DeserializationContext) [lines 1262-1283] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-68-fixed/src/main/java/com/fasterxml/jackson/databind/deser/BeanDeserializerBase.java =====
    public Object deserializeFromString(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        // First things first: id Object Id is used, most likely that's it
        if (_objectIdReader != null) {
            return deserializeFromObjectId(p, ctxt);
        }
        /* Bit complicated if we have delegating creator; may need to use it,
         * or might not...
         */
        JsonDeserializer<Object> delegateDeser = _delegateDeserializer();
        if (delegateDeser != null) {
            if (!_valueInstantiator.canCreateFromString()) {
                Object bean = _valueInstantiator.createUsingDelegate(ctxt,
                        delegateDeser.deserialize(p, ctxt));
                if (_injectables != null) {
                    injectValues(ctxt, bean);
                }
                return bean;
            }
        }
        return _valueInstantiator.createFromString(ctxt, p.getText());
    }
