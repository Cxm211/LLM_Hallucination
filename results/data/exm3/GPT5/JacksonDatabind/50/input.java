// buggy function
    protected Object _deserializeUsingPropertyBased(final JsonParser p, final DeserializationContext ctxt)
        throws IOException
    {
        final PropertyBasedCreator creator = _propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, _objectIdReader);

        TokenBuffer unknown = null;

        JsonToken t = p.getCurrentToken();
        for (; t == JsonToken.FIELD_NAME; t = p.nextToken()) {
            String propName = p.getCurrentName();
            p.nextToken(); // to point to value
            // creator property?
            SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                // Last creator property to set?
                if (buffer.assignParameter(creatorProp,
                        _deserializeWithErrorWrapping(p, ctxt, creatorProp))) {
                    p.nextToken(); // to move to following FIELD_NAME/END_OBJECT
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
                    // [databind#631]: Assign current value, to be accessible by custom serializers
                    p.setCurrentValue(bean);

                    //  polymorphic?
                    if (bean.getClass() != _beanType.getRawClass()) {
                        return handlePolymorphic(p, ctxt, bean, unknown);
                    }
                    if (unknown != null) { // nope, just extra unknown stuff...
                        bean = handleUnknownProperties(ctxt, bean, unknown);
                    }
                    // or just clean?
                    return deserialize(p, ctxt, bean);
                }
                continue;
            }
            // Object Id property?
            if (buffer.readIdProperty(propName)) {
                continue;
            }
            // regular property? needs buffering
            SettableBeanProperty prop = _beanProperties.find(propName);
            if (prop != null) {
                    buffer.bufferProperty(prop, _deserializeWithErrorWrapping(p, ctxt, prop));
                    // 14-Jun-2016, tatu: As per [databind#1261], looks like we need additional
                    //    handling of forward references here. Not exactly sure why existing
                    //    facilities did not cover, but this does appear to solve the problem
                continue;
            }
            // Things marked as ignorable should not be passed to any setter
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                handleIgnoredProperty(p, ctxt, handledType(), propName);
                continue;
            }
            // "any property"?
            if (_anySetter != null) {
                try {
                    buffer.bufferAnyProperty(_anySetter, propName, _anySetter.deserialize(p, ctxt));
                } catch (Exception e) {
                    wrapAndThrow(e, _beanType.getRawClass(), propName, ctxt);
                }
                continue;
            }
            // Ok then, let's collect the whole field; name and value
            if (unknown == null) {
                unknown = new TokenBuffer(p, ctxt);
            }
            unknown.writeFieldName(propName);
            unknown.copyCurrentStructure(p);
        }

        // We hit END_OBJECT, so:
        Object bean;
        try {
            bean =  creator.build(ctxt, buffer);
        } catch (Exception e) {
            wrapInstantiationProblem(e, ctxt);
            bean = null; // never gets here
        }
        if (unknown != null) {
            // polymorphic?
            if (bean.getClass() != _beanType.getRawClass()) {
                return handlePolymorphic(null, ctxt, bean, unknown);
            }
            // no, just some extra unknown properties
            return handleUnknownProperties(ctxt, bean, unknown);
        }
        return bean;
    }

    protected Exception _creatorReturnedNullException() {
        if (_nullFromCreator == null) {
            _nullFromCreator = new NullPointerException("JSON Creator returned null");
        }
        return _nullFromCreator;
    }

// trigger testcase
// com/fasterxml/jackson/databind/objectid/ObjectWithCreator1261Test.java::testObjectIds1261
public void testObjectIds1261() throws Exception
    {
         ObjectMapper mapper = new ObjectMapper();
         mapper.enable(SerializationFeature.INDENT_OUTPUT);
         mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

         Answer initialAnswer = createInitialAnswer();
         String initialAnswerString = mapper.writeValueAsString(initialAnswer);
// System.out.println("Initial answer:\n"+initialAnswerString);
         JsonNode tree = mapper.readTree(initialAnswerString);
         Answer deserializedAnswer = mapper.readValue(initialAnswerString,
               Answer.class);
         String reserializedAnswerString = mapper
               .writeValueAsString(deserializedAnswer);
         JsonNode newTree = mapper.readTree(reserializedAnswerString);
         if (!tree.equals(newTree)) {
                  fail("Original and recovered Json are different. Recovered = \n"
                        + reserializedAnswerString + "\n");
         }
   }
