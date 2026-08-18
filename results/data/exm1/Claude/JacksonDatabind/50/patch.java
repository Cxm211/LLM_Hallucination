protected Object _deserializeUsingPropertyBased(final JsonParser p, final DeserializationContext ctxt)
        throws IOException
    {
        final PropertyBasedCreator creator = _propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, _objectIdReader);

        TokenBuffer unknown = null;

        JsonToken t = p.getCurrentToken();
        for (; t == JsonToken.FIELD_NAME; t = p.nextToken()) {
            String propName = p.getCurrentName();
            p.nextToken(); 
            
            SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                
                if (buffer.assignParameter(creatorProp,
                        _deserializeWithErrorWrapping(p, ctxt, creatorProp))) {
                    p.nextToken(); 
                    Object bean;
                    try {
                        bean = creator.build(ctxt, buffer);
                    } catch (Exception e) {
                        bean = wrapInstantiationProblem(e, ctxt);
                    }
                    if (bean == null) {
                        return ctxt.handleInstantiationProblem(handledType(), null,
                                _creatorReturnedNullException());
                    }
                    
                    p.setCurrentValue(bean);

                    
                    if (bean.getClass() != _beanType.getRawClass()) {
                        return handlePolymorphic(p, ctxt, bean, unknown);
                    }
                    if (unknown != null) { 
                        bean = handleUnknownProperties(ctxt, bean, unknown);
                    }
                    
                    return deserialize(p, ctxt, bean);
                }
                continue;
            }
            
            if (buffer.readIdProperty(propName)) {
                continue;
            }
            
            SettableBeanProperty prop = _beanProperties.find(propName);
            if (prop != null) {
                    buffer.bufferProperty(prop, _deserializeWithErrorWrapping(p, ctxt, prop));
                    
                    
                    
                continue;
            }
            
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                handleIgnoredProperty(p, ctxt, handledType(), propName);
                continue;
            }
            
            if (_anySetter != null) {
                try {
                    buffer.bufferAnyProperty(_anySetter, propName, _anySetter.deserialize(p, ctxt));
                } catch (Exception e) {
                    wrapAndThrow(e, _beanType.getRawClass(), propName, ctxt);
                }
                continue;
            }
            
            if (unknown == null) {
                unknown = new TokenBuffer(p, ctxt);
            }
            unknown.writeFieldName(propName);
            unknown.copyCurrentStructure(p);
        }

        
        Object bean;
        try {
            bean =  creator.build(ctxt, buffer);
        } catch (Exception e) {
            bean = wrapInstantiationProblem(e, ctxt);
        }
        if (unknown != null) {
            
            if (bean.getClass() != _beanType.getRawClass()) {
                return handlePolymorphic(null, ctxt, bean, unknown);
            }
            
            return handleUnknownProperties(ctxt, bean, unknown);
        }
        return bean;
    }