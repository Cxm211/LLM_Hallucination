public void resolve(DeserializationContext ctxt)
        throws JsonMappingException
    {
        ExternalTypeHandler.Builder extTypes = null;
        SettableBeanProperty[] creatorProps;

        if (_valueInstantiator.canCreateFromObjectWith()) {
            creatorProps = _valueInstantiator.getFromObjectArguments(ctxt.getConfig());
        } else {
            creatorProps = null;
        }

        UnwrappedPropertyHandler unwrapped = null;

        for (SettableBeanProperty origProp : _beanProperties) {
            SettableBeanProperty prop = origProp;

            if (!prop.hasValueDeserializer()) {
                JsonDeserializer<?> deser = findConvertingDeserializer(ctxt, prop);
                if (deser == null) {
                    deser = findDeserializer(ctxt, prop.getType(), prop);
                }
                prop = prop.withValueDeserializer(deser);
            } else {
                JsonDeserializer<Object> deser = prop.getValueDeserializer();
                JsonDeserializer<?> cd = ctxt.handlePrimaryContextualization(deser, prop,
                        prop.getType());
                if (cd != deser) {
                    prop = prop.withValueDeserializer(cd);
                }
            }

            prop = _resolveManagedReferenceProperty(ctxt, prop);

            if (!(prop instanceof ManagedReferenceProperty)) {
                prop = _resolvedObjectIdProperty(ctxt, prop);
            }
            SettableBeanProperty u = _resolveUnwrappedProperty(ctxt, prop);
            if (u != null) {
                prop = u;
                if (unwrapped == null) {
                    unwrapped = new UnwrappedPropertyHandler();
                }
                unwrapped.addProperty(prop);
                _beanProperties.remove(prop);
                continue;
            }
            prop = _resolveInnerClassValuedProperty(ctxt, prop);
            if (prop != origProp) {
                _beanProperties.replace(prop);
            }
            if (prop.hasValueTypeDeserializer()) {
                TypeDeserializer typeDeser = prop.getValueTypeDeserializer();
                if (typeDeser.getTypeInclusion() == JsonTypeInfo.As.EXTERNAL_PROPERTY) {
                    if (extTypes == null) {
                        extTypes = new ExternalTypeHandler.Builder();
                    }
                    extTypes.addExternal(prop, typeDeser);
                    _beanProperties.remove(prop);
                    continue;
                }
            }
        }

        if (creatorProps != null) {
            for (int i = 0, len = creatorProps.length; i < len; ++i) {
                SettableBeanProperty origProp = creatorProps[i];
                SettableBeanProperty prop = _beanProperties.find(origProp.getName());
                if (prop != null) {
                    creatorProps[i] = prop;
                }
            }
        }

        if (_anySetter != null && !_anySetter.hasValueDeserializer()) {
            _anySetter = _anySetter.withValueDeserializer(findDeserializer(ctxt,
                    _anySetter.getType(), _anySetter.getProperty()));
        }
        if (_valueInstantiator.canCreateUsingDelegate()) {
            JavaType delegateType = _valueInstantiator.getDelegateType(ctxt.getConfig());
            if (delegateType == null) {
                throw new IllegalArgumentException("Invalid delegate-creator definition for "+_beanType
                        +": value instantiator ("+_valueInstantiator.getClass().getName()
                        +") returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'");
            }
            _delegateDeserializer = _findDelegateDeserializer(ctxt, delegateType,
                    _valueInstantiator.getDelegateCreator());
        }

        if (_valueInstantiator.canCreateUsingArrayDelegate()) {
            JavaType delegateType = _valueInstantiator.getArrayDelegateType(ctxt.getConfig());
            if (delegateType == null) {
                throw new IllegalArgumentException("Invalid array-delegate-creator definition for "+_beanType
                        +": value instantiator ("+_valueInstantiator.getClass().getName()
                        +") returned true for 'canCreateUsingArrayDelegate()', but null for 'getArrayDelegateType()'");
            }
            _arrayDelegateDeserializer = _findDelegateDeserializer(ctxt, delegateType,
                    _valueInstantiator.getArrayDelegateCreator());
        }

        if (creatorProps != null) {
            _propertyBasedCreator = PropertyBasedCreator.construct(ctxt, _valueInstantiator, creatorProps);
        }

        if (extTypes != null) {
            _externalTypeIdHandler = extTypes.build();
            _nonStandardCreation = true;
        }
        
        _unwrappedPropertyHandler = unwrapped;
        if (unwrapped != null) {
            _nonStandardCreation = true;
        }

        _vanillaProcessing = _vanillaProcessing && !_nonStandardCreation;
    }