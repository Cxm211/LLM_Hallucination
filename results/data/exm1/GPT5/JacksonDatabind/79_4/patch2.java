public JsonSerializer<?> createContextual(SerializerProvider provider,
            BeanProperty property)
        throws JsonMappingException
    {
        final AnnotationIntrospector intr = provider.getAnnotationIntrospector();
        final AnnotatedMember accessor = (property == null || intr == null)
                ? null : property.getMember();
        final SerializationConfig config = provider.getConfig();
        
        JsonFormat.Shape shape = null;
        if (accessor != null) {
            JsonFormat.Value format = intr.findFormat((Annotated) accessor);

            if (format != null) {
                shape = format.getShape();
                if (shape != _serializationShape) {
                    if (_handledType.isEnum()) {
                        switch (shape) {
                        case STRING:
                        case NUMBER:
                        case NUMBER_INT:
                            BeanDescription desc = config.introspectClassAnnotations(_handledType);
                            JsonSerializer<?> ser = EnumSerializer.construct(_handledType,
                                    provider.getConfig(), desc, format);
                            return provider.handlePrimaryContextualization(ser, property);
                        }
                    }
                }
            }
        }

        ObjectIdWriter oiw = _objectIdWriter;
        String[] ignorals = null;
        Object newFilterId = null;
        
        if (accessor != null) {
            ignorals = intr.findPropertiesToIgnore(accessor, true);
            ObjectIdInfo objectIdInfo = intr.findObjectIdInfo(accessor);
            if (objectIdInfo == null) {
                if (oiw != null) {
                    objectIdInfo = intr.findObjectReferenceInfo(accessor,
                            new ObjectIdInfo(NAME_FOR_OBJECT_REF, null, null, null));
                        oiw = _objectIdWriter.withAlwaysAsId(objectIdInfo.getAlwaysAsId());
                }
            } else {
                objectIdInfo = intr.findObjectReferenceInfo(accessor, objectIdInfo);
                ObjectIdGenerator<?> gen;
                Class<?> implClass = objectIdInfo.getGeneratorType();
                JavaType type = provider.constructType(implClass);
                JavaType idType = provider.getTypeFactory().findTypeParameters(type, ObjectIdGenerator.class)[0];
                if (implClass == ObjectIdGenerators.PropertyGenerator.class) {
                    String propName = objectIdInfo.getPropertyName().getSimpleName();
                    BeanPropertyWriter idProp = null;

                    for (int i = 0, len = _props.length ;; ++i) {
                        if (i == len) {
                            throw new IllegalArgumentException("Invalid Object Id definition for "+_handledType.getName()
                                    +": can not find property with name '"+propName+"'");
                        }
                        BeanPropertyWriter prop = _props[i];
                        if (propName.equals(prop.getName())) {
                            idProp = prop;
                            if (i > 0) {
                                System.arraycopy(_props, 0, _props, 1, i);
                                _props[0] = idProp;
                                if (_filteredProps != null) {
                                    BeanPropertyWriter fp = _filteredProps[i];
                                    System.arraycopy(_filteredProps, 0, _filteredProps, 1, i);
                                    _filteredProps[0] = fp;
                                }
                            }
                            break;
                        }
                    }
                    idType = idProp.getType();
                    gen = new PropertyBasedObjectIdGenerator(objectIdInfo, idProp);
                    oiw = ObjectIdWriter.construct(idType, (PropertyName) null, gen, objectIdInfo.getAlwaysAsId());
                } else {
                    gen = provider.objectIdGeneratorInstance(accessor, objectIdInfo);
                    oiw = ObjectIdWriter.construct(idType, objectIdInfo.getPropertyName(), gen,
                            objectIdInfo.getAlwaysAsId());
                }
            }
            
            Object filterId = intr.findFilterId(accessor);
            if (filterId != null) {
                if (_propertyFilterId == null || !filterId.equals(_propertyFilterId)) {
                    newFilterId = filterId;
                }
            }
        }
        BeanSerializerBase contextual = this;
        if (oiw != null) {
            JsonSerializer<?> ser = provider.findValueSerializer(oiw.idType, property);
            oiw = oiw.withSerializer(ser);
            if (oiw != _objectIdWriter) {
                contextual = contextual.withObjectIdWriter(oiw);
            }
        }
        if (ignorals != null && ignorals.length != 0) {
            contextual = contextual.withIgnorals(ignorals);
        }
        if (newFilterId != null) {
            contextual = contextual.withFilterId(newFilterId);
        }
        if (shape == null) {
            shape = _serializationShape;
        }
        if (shape == JsonFormat.Shape.ARRAY) {
            return contextual.asArraySerializer();
        }
        return contextual;
    }