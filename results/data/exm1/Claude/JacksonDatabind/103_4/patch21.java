protected BeanPropertyWriter buildWriter(SerializerProvider prov,
            BeanPropertyDefinition propDef, JavaType declaredType, JsonSerializer<?> ser,
            TypeSerializer typeSer, TypeSerializer contentTypeSer,
            AnnotatedMember am, boolean defaultUseStaticTyping)
        throws JsonMappingException
    {
        JavaType serializationType;
        try {
            serializationType = findSerializationType(am, defaultUseStaticTyping, declaredType);
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            if (propDef == null) {
                return prov.reportBadDefinition(declaredType, (msg == null) ? "N/A" : msg);
            }
            return prov.reportBadPropertyDefinition(_beanDesc, propDef, (msg == null) ? "N/A" : msg);
        }

        if (contentTypeSer != null) {
            if (serializationType == null) {
                serializationType = declaredType;
            }
            JavaType ct = serializationType.getContentType();
            if (ct == null) {
                prov.reportBadPropertyDefinition(_beanDesc, propDef,
                        "serialization type "+serializationType+" has no content");
            }
            serializationType = serializationType.withContentTypeHandler(contentTypeSer);
            ct = serializationType.getContentType();
        }

        Object valueToSuppress = null;
        boolean suppressNulls = false;

        JavaType actualType = (serializationType == null) ? declaredType : serializationType;
        
        AnnotatedMember accessor = propDef.getAccessor();
        if (accessor == null) {
            return prov.reportBadPropertyDefinition(_beanDesc, propDef,
                    "could not determine property type");
        }
        Class<?> rawPropertyType = accessor.getRawType();

        JsonInclude.Value inclV = _config.getDefaultInclusion(actualType.getRawClass(),
                rawPropertyType, _defaultInclusion);

        inclV = inclV.withOverrides(propDef.findInclusion());

        JsonInclude.Include inclusion = inclV.getValueInclusion();
        if (inclusion == JsonInclude.Include.USE_DEFAULTS) {
            inclusion = JsonInclude.Include.ALWAYS;
        }
        switch (inclusion) {
        case NON_DEFAULT:
            Object defaultBean;

            if (_useRealPropertyDefaults && (defaultBean = getDefaultBean()) != null) {
                if (prov.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                    am.fixAccess(_config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
                }
                try {
                    valueToSuppress = am.getValue(defaultBean);
                } catch (Exception e) {
                    _throwWrapped(e, propDef.getName(), defaultBean);
                }
            } else {
                valueToSuppress = BeanUtil.getDefaultValue(actualType);
                suppressNulls = true;
            }
            if (valueToSuppress == null) {
                suppressNulls = true;
            } else {
                if (valueToSuppress.getClass().isArray()) {
                    valueToSuppress = ArrayBuilders.getArrayComparator(valueToSuppress);
                }
            }
            break;
        case NON_ABSENT:
            suppressNulls = true;
            if (actualType.isReferenceType()) {
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            }
            break;
        case NON_EMPTY:
            suppressNulls = true;
            valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            break;
        case CUSTOM:
            valueToSuppress = prov.includeFilterInstance(propDef, inclV.getValueFilter());
            if (valueToSuppress == null) {
                suppressNulls = true;
            } else {
                suppressNulls = prov.includeFilterSuppressNulls(valueToSuppress);
            }
            break;
        case NON_NULL:
            suppressNulls = true;
        case ALWAYS:
        default:
            if (actualType.isContainerType()
                    && !_config.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)) {
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            }
            break;
        }
        Class<?>[] views = propDef.findViews();
        if (views == null) {
            views = _beanDesc.findDefaultViews();
        }
        BeanPropertyWriter bpw = new BeanPropertyWriter(propDef,
                am, _beanDesc.getClassAnnotations(), declaredType,
                ser, typeSer, serializationType, suppressNulls, valueToSuppress, views);

        Object serDef = _annotationIntrospector.findNullSerializer(am);
        if (serDef != null) {
            bpw.assignNullSerializer(prov.serializerInstance(am, serDef));
        }
        NameTransformer unwrapper = _annotationIntrospector.findUnwrappingNameTransformer(am);
        if (unwrapper != null) {
            bpw = bpw.unwrappingWriter(unwrapper);
        }
        return bpw;
    }