// buggy code
    protected BeanPropertyWriter buildWriter(SerializerProvider prov,
            BeanPropertyDefinition propDef, JavaType declaredType, JsonSerializer<?> ser,
            TypeSerializer typeSer, TypeSerializer contentTypeSer,
            AnnotatedMember am, boolean defaultUseStaticTyping)
        throws JsonMappingException
    {
        // do we have annotation that forces type to use (to declared type or its super type)?
        JavaType serializationType;
        try {
            serializationType = findSerializationType(am, defaultUseStaticTyping, declaredType);
        } catch (JsonMappingException e) {
            return prov.reportBadPropertyDefinition(_beanDesc, propDef, e.getMessage());
        }

        // Container types can have separate type serializers for content (value / element) type
        if (contentTypeSer != null) {
            /* 04-Feb-2010, tatu: Let's force static typing for collection, if there is
             *    type information for contents. Should work well (for JAXB case); can be
             *    revisited if this causes problems.
             */
            if (serializationType == null) {
//                serializationType = TypeFactory.type(am.getGenericType(), _beanDesc.getType());
                serializationType = declaredType;
            }
            JavaType ct = serializationType.getContentType();
            // Not exactly sure why, but this used to occur; better check explicitly:
            if (ct == null) {
                prov.reportBadPropertyDefinition(_beanDesc, propDef,
                        "serialization type "+serializationType+" has no content");
            }
            serializationType = serializationType.withContentTypeHandler(contentTypeSer);
            ct = serializationType.getContentType();
        }

        Object valueToSuppress = null;
        boolean suppressNulls = false;

        // 12-Jul-2016, tatu: [databind#1256] Need to make sure we consider type refinement
        JavaType actualType = (serializationType == null) ? declaredType : serializationType;
        
        // 17-Aug-2016, tatu: Default inclusion covers global default (for all types), as well
        //   as type-default for enclosing POJO. What we need, then, is per-type default (if any)
        //   for declared property type... and finally property annotation overrides
        JsonInclude.Value inclV = _config.getDefaultPropertyInclusion(actualType.getRawClass(),
                _defaultInclusion);

        // property annotation override
        
        inclV = inclV.withOverrides(propDef.findInclusion());
        JsonInclude.Include inclusion = inclV.getValueInclusion();

        if (inclusion == JsonInclude.Include.USE_DEFAULTS) { // should not occur but...
            inclusion = JsonInclude.Include.ALWAYS;
        }
        
        switch (inclusion) {
        case NON_DEFAULT:
            // 11-Nov-2015, tatu: This is tricky because semantics differ between cases,
            //    so that if enclosing class has this, we may need to access values of property,
            //    whereas for global defaults OR per-property overrides, we have more
            //    static definition. Sigh.
            // First: case of class/type specifying it; try to find POJO property defaults

            // 16-Oct-2016, tatu: Note: if we can not for some reason create "default instance",
            //    revert logic to the case of general/per-property handling, so both
            //    type-default AND null are to be excluded.
            //    (as per [databind#1417]
            if (_useRealPropertyDefaults) {
                // 07-Sep-2016, tatu: may also need to front-load access forcing now
                if (prov.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                    am.fixAccess(_config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
                }
                valueToSuppress = getPropertyDefaultValue(propDef.getName(), am, actualType);
            } else {
                valueToSuppress = getDefaultValue(actualType);
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
        case NON_ABSENT: // new with 2.6, to support Guava/JDK8 Optionals
            // always suppress nulls
            suppressNulls = true;
            // and for referential types, also "empty", which in their case means "absent"
            if (actualType.isReferenceType()) {
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            }
            break;
        case NON_EMPTY:
            // always suppress nulls
            suppressNulls = true;
            // but possibly also 'empty' values:
            valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            break;
        case NON_NULL:
            suppressNulls = true;
            // fall through
        case ALWAYS: // default
        default:
            // we may still want to suppress empty collections, as per [JACKSON-254]:
            if (actualType.isContainerType()
                    && !_config.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)) {
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            }
            break;
        }
        BeanPropertyWriter bpw = new BeanPropertyWriter(propDef,
                am, _beanDesc.getClassAnnotations(), declaredType,
                ser, typeSer, serializationType, suppressNulls, valueToSuppress);

        // How about custom null serializer?
        Object serDef = _annotationIntrospector.findNullSerializer(am);
        if (serDef != null) {
            bpw.assignNullSerializer(prov.serializerInstance(am, serDef));
        }
        // And then, handling of unwrapping
        NameTransformer unwrapper = _annotationIntrospector.findUnwrappingNameTransformer(am);
        if (unwrapper != null) {
            bpw = bpw.unwrappingWriter(unwrapper);
        }
        return bpw;
    }

// relevant test
// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testIssue92
    public void testIssue92() throws Exception
    {
        assertEquals("{\"_id\":\"abc\"}", MAPPER.writeValueAsString(new Bean92()));
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testSerializeDeserializeWithJaxbAnnotations
    public void testSerializeDeserializeWithJaxbAnnotations() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JacksonExample ex = new JacksonExample();
        QName qname = new QName("urn:hi", "hello");
        ex.setQname(qname);
        ex.setAttributeProperty("attributeValue");
        ex.setElementProperty("elementValue");
        ex.setWrappedElementProperty(Arrays.asList("wrappedElementValue"));
        ex.setEnumProperty(EnumExample.VALUE1);
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, ex);
        writer.flush();
        writer.close();

        String json = writer.toString();
        JacksonExample readEx = mapper.readValue(json, JacksonExample.class);

        assertEquals(ex.qname, readEx.qname);
        assertEquals(ex.attributeProperty, readEx.attributeProperty);
        assertEquals(ex.elementProperty, readEx.elementProperty);
        assertEquals(ex.wrappedElementProperty, readEx.wrappedElementProperty);
        assertEquals(ex.enumProperty, readEx.enumProperty);
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testJsonTypeResolver
    public void testJsonTypeResolver() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(TypeResolverBean.class, mapper.getSerializationConfig());
        JavaType baseType = TypeFactory.defaultInstance().constructType(TypeResolverBean.class);
        TypeResolverBuilder<?> rb = ai.findTypeResolver(mapper.getDeserializationConfig(), ac, baseType);
        assertNotNull(rb);
        assertSame(DummyBuilder.class, rb.getClass());
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testEnumHandling
    public void testEnumHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new LcEnumIntrospector());
        assertEquals("\"value1\"", mapper.writeValueAsString(EnumExample.VALUE1));
        EnumExample result = mapper.readValue(quote("value1"), EnumExample.class);
        assertEquals(EnumExample.VALUE1, result);
    }

// com.fasterxml.jackson.databind.introspect.TestMixinMerging::testDisappearingMixins515
    public void testDisappearingMixins515() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
            .disable(MapperFeature.AUTO_DETECT_FIELDS)
            .disable(MapperFeature.AUTO_DETECT_GETTERS)
            .disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
            .disable(MapperFeature.INFER_PROPERTY_MUTATORS);
        SimpleModule module = new SimpleModule("Test");
        module.setMixInAnnotation(Person.class, PersonMixin.class);        
        mapper.registerModule(module);

        assertEquals("{\"city\":\"Seattle\"}", mapper.writeValueAsString(new PersonImpl()));
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testIssue193
    public void testIssue193() throws Exception
    {
        String json = objectWriter().writeValueAsString(new Bean193(1, 2));
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testNonConflict
    public void testNonConflict() throws Exception
    {
        String json = MAPPER.writeValueAsString(new BogusConflictBean());
        assertEquals(aposToQuotes("{'prop1':2,'prop2':1}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testHypotheticalGetters
    public void testHypotheticalGetters() throws Exception
    {
        String json = objectWriter().writeValueAsString(new MultipleTheoreticalGetters());
        assertEquals(aposToQuotes("{'a':3}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testOverrideName
    public void testOverrideName() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        String json = mapper.writeValueAsString(new CoreBean158());
        assertEquals(aposToQuotes("{'bar':'x'}"), json);

        
        CoreBean158 result = null;
        try {
            result = mapper.readValue(aposToQuotes("{'bar':'y'}"), CoreBean158.class);
        } catch (Exception e) {
            fail("Unexpected failure when reading CoreBean158: "+e);
        }
        assertNotNull(result);
        assertEquals("y", result.bar);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testSimpleGetters
    public void testSimpleGetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        assertEquals("{\"Get-key\":123}", mapper.writeValueAsString(new GetterBean()));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testSimpleSetters
    public void testSimpleSetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        SetterBean bean = mapper.readValue("{\"Set-key\":13}", SetterBean.class);
        assertEquals(13, bean.value);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testSimpleFields
    public void testSimpleFields() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        String json = mapper.writeValueAsString(new FieldBean(999));
        assertEquals("{\"Field-key\":999}", json);

        
        FieldBean result = mapper.readValue(json, FieldBean.class);
        assertEquals(999, result.key);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testCStyleNaming
    public void testCStyleNaming() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new CStyleStrategy());
        String json = mapper.writeValueAsString(new PersonBean("Joe", "Sixpack", 42));
        assertEquals("{\"first_name\":\"Joe\",\"last_name\":\"Sixpack\",\"age\":42}", json);
        
        
        PersonBean result = mapper.readValue(json, PersonBean.class);
        assertEquals("Joe", result.firstName);
        assertEquals("Sixpack", result.lastName);
        assertEquals(42, result.age);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testWithGetterAsSetter
    public void testWithGetterAsSetter() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new CStyleStrategy());
        SetterlessWithValue input = new SetterlessWithValue().add(3);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value_list\":[{\"int_value\":3}]}", json);

        SetterlessWithValue result = mapper.readValue(json, SetterlessWithValue.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(3, result.values.get(0).intValue);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testLowerCase
    public void testLowerCase() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new LcStrategy());

        RenamedCollectionBean result = mapper.readValue("{\"thevalues\":[\"a\"]}",
                RenamedCollectionBean.class);
        assertNotNull(result.getTheValues());
        assertEquals(1, result.getTheValues().size());
        assertEquals("a", result.getTheValues().get(0));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testPerClassAnnotation
    public void testPerClassAnnotation() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new LcStrategy());
        BeanWithPrefixNames input = new BeanWithPrefixNames();
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"Get-a\":3}", json);

        BeanWithPrefixNames output = mapper.readValue("{\"Set-a\":7}",
                BeanWithPrefixNames.class);
        assertEquals(7, output.a);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseStrategyStandAlone
    public void testLowerCaseStrategyStandAlone()
    {
        for (Object[] pair : SNAKE_CASE_NAME_TRANSLATIONS) {
            String translatedJavaName = PropertyNamingStrategy.SNAKE_CASE.nameForField(null, null,
                    (String) pair[0]);
            assertEquals((String) pair[1], translatedJavaName);
        }
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseTranslations
    public void testLowerCaseTranslations() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new PersonBean("Joe", "Sixpack", 42));
        assertEquals("{\"first_name\":\"Joe\",\"last_name\":\"Sixpack\",\"age\":42}", json);
        
        
        PersonBean result = _lcWithUndescoreMapper.readValue(json, PersonBean.class);
        assertEquals("Joe", result.firstName);
        assertEquals("Sixpack", result.lastName);
        assertEquals(42, result.age);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseAcronymsTranslations
    public void testLowerCaseAcronymsTranslations() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new Acronyms("world wide web", "http://jackson.codehaus.org", "/path1/,/path2/"));
        assertEquals("{\"www\":\"world wide web\",\"some_url\":\"http://jackson.codehaus.org\",\"some_uris\":\"/path1/,/path2/\"}", json);
        
        
        Acronyms result = _lcWithUndescoreMapper.readValue(json, Acronyms.class);
        assertEquals("world wide web", result.WWW);
        assertEquals("http://jackson.codehaus.org", result.someURL);
        assertEquals("/path1/,/path2/", result.someURIs);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseOtherNonStandardNamesTranslations
    public void testLowerCaseOtherNonStandardNamesTranslations() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new OtherNonStandardNames("Results", "_User", "___", "$User"));
        assertEquals("{\"results\":\"Results\",\"user\":\"_User\",\"__\":\"___\",\"$_user\":\"$User\"}", json);
        
        
        OtherNonStandardNames result = _lcWithUndescoreMapper.readValue(json, OtherNonStandardNames.class);
        assertEquals("Results", result.Results);
        assertEquals("_User", result._User);
        assertEquals("___", result.___);
        assertEquals("$User", result.$User);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseUnchangedNames
    public void testLowerCaseUnchangedNames() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new UnchangedNames("from_user", "_user", "from$user", "from7user", "_x"));
        assertEquals("{\"from_user\":\"from_user\",\"user\":\"_user\",\"from$user\":\"from$user\",\"from7user\":\"from7user\",\"x\":\"_x\"}", json);
        
        
        UnchangedNames result = _lcWithUndescoreMapper.readValue(json, UnchangedNames.class);
        assertEquals("from_user", result.from_user);
        assertEquals("_user", result._user);
        assertEquals("from$user", result.from$user);
        assertEquals("from7user", result.from7user);
        assertEquals("_x", result._x);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testPascalCaseStandAlone
    public void testPascalCaseStandAlone()
    {
        String translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
    	        (null, null, "userName");
        assertEquals("UserName", translatedJavaName);

        translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
                (null, null, "User");
        assertEquals("User", translatedJavaName);

        translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
                (null, null, "user");
        assertEquals("User", translatedJavaName);
        translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
                (null, null, "x");
        assertEquals("X", translatedJavaName);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testIssue428PascalWithOverrides
    public void testIssue428PascalWithOverrides() throws Exception {

        String json = new ObjectMapper()
                            .setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                            .writeValueAsString(new Bean428());
        
        if (!json.contains(quote("fooBar"))) {
            fail("Should use name 'fooBar', does not: "+json);
        }
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testSimpleLowerCase
    public void testSimpleLowerCase() throws Exception
    {
        final BoringBean input = new BoringBean();
        ObjectMapper m = objectMapper();

        assertEquals(aposToQuotes("{'firstname':'Bob','lastname':'Burger'}"),
                m.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testKebabCaseStrategyStandAlone
    public void testKebabCaseStrategyStandAlone()
    {
        assertEquals("some-value",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "someValue"));
        assertEquals("some-value",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "SomeValue"));
        assertEquals("url",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "URL"));
        assertEquals("url-stuff",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "URLStuff"));
        assertEquals("some-url-stuff",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "SomeURLStuff"));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testSimpleKebabCase
    public void testSimpleKebabCase() throws Exception
    {
        final FirstNameBean input = new FirstNameBean("Bob");
        ObjectMapper m = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

        assertEquals(aposToQuotes("{'first-name':'Bob'}"), m.writeValueAsString(input));

        FirstNameBean result = m.readValue(aposToQuotes("{'first-name':'Billy'}"),
                FirstNameBean.class);
        assertEquals("Billy", result.firstName);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testNamingWithObjectNode
    public void testNamingWithObjectNode() throws Exception
    {
        ObjectMapper m = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
        ClassWithObjectNodeField result =
            m.readValue(
                "{ \"id\": \"1\", \"json\": { \"foo\": \"bar\", \"baz\": \"bing\" } }",
                ClassWithObjectNodeField.class);
        assertNotNull(result);
        assertEquals("1", result.id);
        assertNotNull(result.json);
        assertEquals(2, result.json.size());
        assertEquals("bing", result.json.path("baz").asText());
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testExplicitRename
    public void testExplicitRename() throws Exception
    {
      ObjectMapper m = new ObjectMapper();
      m.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      m.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
      
      assertEquals(aposToQuotes("{'firstName':'Peter','lastName':'Venkman','user_age':'35'}"),
          m.writeValueAsString(new ExplicitBean()));

      m = new ObjectMapper();
      m.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      m.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
      m.enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING);
      
      assertEquals(aposToQuotes("{'first_name':'Peter','last_name':'Venkman','user_age':'35'}"),
          m.writeValueAsString(new ExplicitBean()));

      
      ExplicitBean bean =
          m.readValue(aposToQuotes("{'first_name':'Egon','last_name':'Spengler','user_age':'32'}"),
              ExplicitBean.class);

      assertNotNull(bean);
      assertEquals("Egon", bean.userFirstName);
      assertEquals("Spengler", bean.userLastName);
      assertEquals("32", bean.userAge);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testExplicitNoNaming
    public void testExplicitNoNaming() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        String json = mapper.writeValueAsString(new DefaultNaming());
        assertEquals(aposToQuotes("{'someValue':3}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimple
    public void testSimple()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
        		Simple.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("value");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleFieldVisibility
    public void testSimpleFieldVisibility()
    {
        
        POJOPropertiesCollector coll = collector(MAPPER,
        		SimpleFieldDeser.class, false);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("values");
        assertNotNull(prop);
        assertFalse(prop.hasSetter());
        assertFalse(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleGetterVisibility
    public void testSimpleGetterVisibility()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
        		SimpleGetterVisibility.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("a");
        assertNotNull(prop);
        assertFalse(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertFalse(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testEmpty
    public void testEmpty()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
        		Empty.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(0, props.size());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPartialIgnore
    public void testPartialIgnore()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
        		IgnoredSetter.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("value");
        assertNotNull(prop);
        assertFalse(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleRenamed
    public void testSimpleRenamed()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
        		RenamedProperties.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("x");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleRenamed2
    public void testSimpleRenamed2()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
        		RenamedProperties2.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("renamed");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertFalse(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testMergeWithRename
    public void testMergeWithRename()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
        		MergedProperties.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("x");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertFalse(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleIgnoreAndRename
    public void testSimpleIgnoreAndRename()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
        		IgnoredRenamedSetter.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("y");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertFalse(prop.hasGetter());
        assertFalse(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testGlobalVisibilityForGetters
    public void testGlobalVisibilityForGetters()
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        POJOPropertiesCollector coll = collector(m, SimpleGetterVisibility.class, true);
        
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(0, props.size());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testCollectionOfIgnored
    public void testCollectionOfIgnored()
    {
        POJOPropertiesCollector coll = collector(MAPPER, ImplicitIgnores.class, false);
        
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        
        Collection<String> ign = coll.getIgnoredPropertyNames();
        assertEquals(2, ign.size());
        assertTrue(ign.contains("a"));
        assertTrue(ign.contains("b"));
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleOrderingForDeserialization
    public void testSimpleOrderingForDeserialization()
    {
        POJOPropertiesCollector coll = collector(MAPPER, SortedProperties.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(4, props.size());
        assertEquals("a", props.get(0).getName());
        assertEquals("b", props.get(1).getName());
        assertEquals("c", props.get(2).getName());
        assertEquals("d", props.get(3).getName());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleWithType
    public void testSimpleWithType()
    {
        
        POJOPropertiesCollector coll = collector(MAPPER, TypeTestBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        assertEquals("value", props.get(0).getName());
        AnnotatedMember m = props.get(0).getAccessor();
        assertTrue(m instanceof AnnotatedMethod);
        assertEquals(Integer.class, m.getRawType());

        
        coll = collector(MAPPER, TypeTestBean.class, false);
        props = coll.getProperties();
        assertEquals(1, props.size());
        assertEquals("value", props.get(0).getName());
        m = props.get(0).getMutator();
        assertEquals(AnnotatedParameter.class, m.getClass());
        assertEquals(String.class, m.getRawType());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testInnerClassWithAnnotationsInCreator
    public void testInnerClassWithAnnotationsInCreator() throws Exception
    {
        BasicBeanDescription beanDesc;
        
        beanDesc = MAPPER.getSerializationConfig().introspect(MAPPER.constructType(Issue701Bean.class));
        assertNotNull(beanDesc);
        
        beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(Issue701Bean.class));
        assertNotNull(beanDesc);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testUseAnnotationsFalse
    public void testUseAnnotationsFalse() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, false);
        BasicBeanDescription beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(Jackson703.class));
        assertNotNull(beanDesc);

        Jackson703 bean = new Jackson703();
        String json = mapper.writeValueAsString(bean);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testJackson744
    public void testJackson744() throws Exception
    {
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(Issue744Bean.class));
        assertNotNull(beanDesc);
        AnnotatedMethod setter = beanDesc.findAnySetter();
        assertNotNull(setter);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPropertyDesc
    public void testPropertyDesc() throws Exception
    {
        
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, true, false, "13");
        
        beanDesc = MAPPER.getSerializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, true, false, "13");
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPropertyIndex
    public void testPropertyIndex() throws Exception
    {
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, false, true, "13");
        beanDesc = MAPPER.getSerializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, false, true, "13");
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testDuplicateGetters
    public void testDuplicateGetters() throws Exception
    {
        POJOPropertiesCollector coll = collector(MAPPER, DuplicateGetterBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        BeanPropertyDefinition prop = props.get(0);
        assertEquals("bloop", prop.getName());
        assertTrue(prop.getGetter().hasAnnotation(A.class));
        assertTrue(prop.getGetter().hasAnnotation(B.class));
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testDuplicateGettersCreator
    public void testDuplicateGettersCreator() throws Exception
    {
        POJOPropertiesCollector coll = collector(MAPPER, DuplicateGetterCreatorBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = (POJOPropertyBuilder) props.get(0);
        assertEquals("bloop", prop.getName());
        
        assertTrue(prop._getters.value.hasAnnotation(A.class));
        assertNotNull(prop._getters.next);
        assertTrue(prop._getters.next.value.hasAnnotation(A.class));
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testFailWithDupProps
    public void testFailWithDupProps() throws Exception
    {
        BeanWithConflict bean = new BeanWithConflict();
        try {
            String json = objectWriter().writeValueAsString(bean);
            fail("Should have failed due to conflicting accessor definitions; got JSON = "+json);
        } catch (JsonProcessingException e) {
            verifyException(e, "Conflicting getter definitions");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testRegularAndIsGetter
    public void testRegularAndIsGetter() throws Exception
    {
        final ObjectWriter writer = objectWriter();
        
        
        assertEquals("{\"value\":4}", writer.writeValueAsString(new Getters1A()));
        assertEquals("{\"value\":4}", writer.writeValueAsString(new Getters1B()));

        
        ObjectMapper mapper = objectMapper();
        assertEquals(1, mapper.readValue("{\"value\":1}", Getters1A.class).value);
        assertEquals(2, mapper.readValue("{\"value\":2}", Getters1B.class).value);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testInferredNameConflictsWithGetters
    public void testInferredNameConflictsWithGetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InferingIntrospector());
        String json = mapper.writeValueAsString(new Infernal());
        assertEquals(aposToQuotes("{'name':'Bob'}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testInferredNameConflictsWithSetters
    public void testInferredNameConflictsWithSetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InferingIntrospector());
        Infernal inf = mapper.readValue(aposToQuotes("{'stuff':'Bob'}"), Infernal.class);
        assertNotNull(inf);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testIssue541
    public void testIssue541() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS,
                MapperFeature.USE_GETTERS_AS_SETTERS
        );
        Bean541 data = mapper.readValue("{\"str\":\"the string\"}", Bean541.class);
        if (data == null) {
            throw new IllegalStateException("data is null");
        }
        if (!"the string".equals(data.getStr())) {
            throw new IllegalStateException("bad value for data.str");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyRename::testCreatorPropRenameWithIgnore
    public void testCreatorPropRenameWithIgnore() throws Exception
    {
        Bean323WithIgnore input = new Bean323WithIgnore(7);
        assertEquals("{\"b\":7}", objectWriter().writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyRename::testCreatorPropRenameWithCleave
    public void testCreatorPropRenameWithCleave() throws Exception
    {
        assertEquals("{\"a\":7,\"b\":7}",
        		objectWriter().writeValueAsString(new Bean323WithExplicitCleave1(7)));
        
        assertEquals("{\"b\":7}", objectWriter().writeValueAsString(new Bean323WithExplicitCleave2(7)));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testValProperty
    public void testValProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"val\"}", m.writeValueAsString(new ValProperty("val")));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testValWithBeanProperty
    public void testValWithBeanProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"val\"}", m.writeValueAsString(new ValWithBeanProperty("val")));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testVarProperty
    public void testVarProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"var\"}", m.writeValueAsString(new VarProperty("var")));
        VarProperty result = m.readValue("{\"prop\":\"read\"}", VarProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testVarWithBeanProperty
    public void testVarWithBeanProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"var\"}", m.writeValueAsString(new VarWithBeanProperty("var")));
        VarWithBeanProperty result = m.readValue("{\"prop\":\"read\"}", VarWithBeanProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testGetterSetterProperty
    public void testGetterSetterProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"get/set\"}", m.writeValueAsString(new GetterSetterProperty()));
        GetterSetterProperty result = m.readValue("{\"prop\":\"read\"}", GetterSetterProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TransientTest::testTransientFieldHandling
    public void testTransientFieldHandling() throws Exception
    {
        
        assertEquals(aposToQuotes("{'x':42,'value':3}"),
                MAPPER.writeValueAsString(new ClassyTransient()));
        assertEquals(aposToQuotes("{'a':1}"),
                MAPPER.writeValueAsString(new SimplePrunableTransient()));

        
        ObjectMapper m = new ObjectMapper()
            .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
        assertEquals(aposToQuotes("{'x':42}"),
                m.writeValueAsString(new ClassyTransient()));
    }

// com.fasterxml.jackson.databind.introspect.TransientTest::testBeanTransient
    public void testBeanTransient() throws Exception
    {
        assertEquals(aposToQuotes("{'y':4}"),
                MAPPER.writeValueAsString(new BeanTransient()));
    }

// com.fasterxml.jackson.databind.introspect.TransientTest::testOverridingTransient
    public void testOverridingTransient() throws Exception
    {
        assertEquals(aposToQuotes("{'tValue':38}"),
                MAPPER.writeValueAsString(new OverridableTransient(38)));
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testBasicTraversal
    public void testBasicTraversal() throws Exception
    {
        MAPPER.acceptJsonFormatVisitor(POJO.class, new JsonFormatVisitorWrapper.Base());
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testSimpleEnum
    public void testSimpleEnum() throws Exception
    {
        final Set<String> values = new TreeSet<String>();
        ObjectWriter w = MAPPER.writer(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        w.acceptJsonFormatVisitor(TestEnum.class, new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        values.addAll(enums);
                    }

                    @Override
                    public void format(JsonValueFormat format) { }
                };
            }
        });

        assertEquals(3, values.size());
        TreeSet<String> exp = new TreeSet<String>(Arrays.asList(
                        "ToString:A",
                        "ToString:B",
                        "ToString:C"
                        ));
        assertEquals(exp, values);
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testEnumWithJsonValue
    public void testEnumWithJsonValue() throws Exception
    {
        final Set<String> values = new TreeSet<String>();
        MAPPER.acceptJsonFormatVisitor(TestEnumWithJsonValue.class,
                new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        values.addAll(enums);
                    }

                    @Override
                    public void format(JsonValueFormat format) { }
                };
            }
        });

        assertEquals(3, values.size());
        TreeSet<String> exp = new TreeSet<String>(Arrays.asList(
                        "value-A",
                        "value-B",
                        "value-C"
                        ));
        assertEquals(exp, values);
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testJsonValueFormatHandling
    public void testJsonValueFormatHandling() throws Exception
    {
        
        final String EXP = quote("host-name");
        assertEquals(EXP, MAPPER.writeValueAsString(JsonValueFormat.HOST_NAME));

        
        assertSame(JsonValueFormat.HOST_NAME, MAPPER.readValue(EXP, JsonValueFormat.class));
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testSimpleNumbers
    public void testSimpleNumbers() throws Exception
    {
        final StringBuilder sb = new StringBuilder();
        
        MAPPER.acceptJsonFormatVisitor(Numbers.class,
                new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(final JavaType type) {
                return new JsonObjectFormatVisitor.Base(getProvider()) {
                    @Override
                    public void optionalProperty(BeanProperty prop) throws JsonMappingException {
                        sb.append("[optProp ").append(prop.getName()).append("(");
                        JsonSerializer<Object> ser = null;
                        if (prop instanceof BeanPropertyWriter) {
                            BeanPropertyWriter bpw = (BeanPropertyWriter) prop;
                            ser = bpw.getSerializer();
                        }
                        final SerializerProvider prov = getProvider();
                        if (ser == null) {
                            ser = prov.findValueSerializer(prop.getType(), prop);
                        }
                        ser.acceptJsonFormatVisitor(new JsonFormatVisitorWrapper.Base() {
                            @Override
                            public JsonNumberFormatVisitor expectNumberFormat(
                                    JavaType t) throws JsonMappingException {
                                return new JsonNumberFormatVisitor() {
                                    @Override
                                    public void format(JsonValueFormat format) {
                                        sb.append("[numberFormat=").append(format).append("]");
                                    }

                                    @Override
                                    public void enumTypes(Set<String> enums) { }

                                    @Override
                                    public void numberType(NumberType numberType) {
                                        sb.append("[numberType=").append(numberType).append("]");
                                    }
                                };
                            }

                            @Override
                            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType t) throws JsonMappingException {
                                return new JsonIntegerFormatVisitor() {
                                    @Override
                                    public void format(JsonValueFormat format) {
                                        sb.append("[integerFormat=").append(format).append("]");
                                    }

                                    @Override
                                    public void enumTypes(Set<String> enums) { }

                                    @Override
                                    public void numberType(NumberType numberType) {
                                        sb.append("[numberType=").append(numberType).append("]");
                                    }
                                };
                            }
                        }, prop.getType());

                        sb.append(")]");
                    }
                };
            }
        });
        assertEquals("[optProp dec([numberType=BIG_DECIMAL])][optProp bigInt([numberType=BIG_INTEGER])]",
                sb.toString());
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testOldSchemaGeneration
    public void testOldSchemaGeneration() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(SimpleBean.class);
        
        assertNotNull(jsonSchema);

        
        assertTrue(jsonSchema.equals(jsonSchema));
        assertFalse(jsonSchema.equals(null));
        assertFalse(jsonSchema.equals("foo"));

        
        assertNotNull(jsonSchema.toString());
        assertNotNull(JsonSchema.getDefaultSchemaNode());

        ObjectNode root = jsonSchema.getSchemaNode();
        assertEquals("object", root.get("type").asText());
        assertEquals(false, root.path("required").booleanValue());
        JsonNode propertiesSchema = root.get("properties");
        assertNotNull(propertiesSchema);
        JsonNode property1Schema = propertiesSchema.get("property1");
        assertNotNull(property1Schema);
        assertEquals("integer", property1Schema.get("type").asText());
        assertEquals(false, property1Schema.path("required").booleanValue());
        JsonNode property2Schema = propertiesSchema.get("property2");
        assertNotNull(property2Schema);
        assertEquals("string", property2Schema.get("type").asText());
        assertEquals(false, property2Schema.path("required").booleanValue());
        JsonNode property3Schema = propertiesSchema.get("property3");
        assertNotNull(property3Schema);
        assertEquals("array", property3Schema.get("type").asText());
        assertEquals(false, property3Schema.path("required").booleanValue());
        assertEquals("string", property3Schema.get("items").get("type").asText());
        JsonNode property4Schema = propertiesSchema.get("property4");
        assertNotNull(property4Schema);
        assertEquals("array", property4Schema.get("type").asText());
        assertEquals(false, property4Schema.path("required").booleanValue());
        assertEquals("number", property4Schema.get("items").get("type").asText());
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testGeneratingJsonSchemaWithFilters
    public void testGeneratingJsonSchemaWithFilters() throws Exception {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.setFilters(secretFilterProvider);
    	JsonSchema schema = mapper.generateJsonSchema(FilteredBean.class);
    	JsonNode node = schema.getSchemaNode().get("properties");
    	assertTrue(node.has("obvious"));
    	assertFalse(node.has("secret"));
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testSchemaSerialization
    public void testSchemaSerialization() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(SimpleBean.class);
        Map<String,Object> result = writeAndMap(MAPPER, jsonSchema);
        assertNotNull(result);
        
        assertEquals("object", result.get("type"));
        
        assertNull(result.get("required"));
        assertNotNull(result.get("properties"));
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testThatObjectsHaveNoItems
    public void testThatObjectsHaveNoItems() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(TrivialBean.class);
        String json = jsonSchema.toString().replaceAll("\"", "'");
        
        
        assertEquals("{'type':'object','properties':{'name':{'type':'string'}}}",
                json);
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testSchemaId
    public void testSchemaId() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(BeanWithId.class);
        String json = jsonSchema.toString().replaceAll("\"", "'");
        assertEquals("{'type':'object','id':'myType','properties':{'value':{'type':'string'}}}",
                json);
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testUnwrapping
    public void testUnwrapping()  throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(UnwrappingRoot.class);
        String json = jsonSchema.toString().replaceAll("\"", "'");
        String EXP = "{'type':'object',"
                +"'properties':{'age':{'type':'integer'},"
                +"'name.first':{'type':'string'},'name.last':{'type':'string'}}}";
        assertEquals(EXP, json);
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testNumberTypes
    public void testNumberTypes()  throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(Numbers.class);
        String json = quotesToApos(jsonSchema.toString());
        String EXP = "{'type':'object',"
                +"'properties':{'dec':{'type':'number'},"
                +"'bigInt':{'type':'integer'}}}";
        assertEquals(EXP, json);
    }

// com.fasterxml.jackson.databind.jsonschema.TestReadJsonSchema::testDeserializeSimple
    public void testDeserializeSimple() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchema schema = mapper.generateJsonSchema(Schemable.class);
        assertNotNull(schema);

        String schemaStr = mapper.writeValueAsString(schema);
        assertNotNull(schemaStr);
        JsonSchema result = mapper.readValue(schemaStr, JsonSchema.class);
        assertEquals("Trying to read from '"+schemaStr+"'", schema, result);
    }

// com.fasterxml.jackson.databind.jsontype.DefaultTypingWithPrimitivesTest::testDefaultTypingWithLong
    public void testDefaultTypingWithLong() throws Exception
    {
        Data data = new Data();
        data.key = 1L;
        Map<String, Object> mapData = new HashMap<String, Object>();
        mapData.put("longInMap", 2L);
        mapData.put("longAsField", data);

        
        ObjectMapper mapper = new ObjectMapper();
        StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
        resolver.init(JsonTypeInfo.Id.CLASS, null);
        resolver.inclusion(JsonTypeInfo.As.PROPERTY);
        resolver.typeProperty("__t");
        mapper.setDefaultTyping(resolver);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        
        String json = mapper.writeValueAsString(mapData);

        
        Map<?,?> result = mapper.readValue(json, Map.class);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.jsontype.Generic1128Test::testIssue1128
    public void testIssue1128() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        final DevMContainer devMContainer1 = new DevMContainer();
        final DevM entity = new DevM();
        final Dev parent = new Dev();
        parent.id = 2L;
        entity.parent = parent;
        devMContainer1.entity = entity;
    
        String json = mapper.writeValueAsString(devMContainer1);

        final DevMContainer devMContainer = mapper.readValue(json, DevMContainer.class);
        long id = devMContainer.entity.parent.id;

        assertEquals(2, id);
    }

// com.fasterxml.jackson.databind.jsontype.PolymorphicViaRefTypeTest::testPolymorphicAtomicRefProperty
    public void testPolymorphicAtomicRefProperty() throws Exception
    {
        TypeInfoAtomic data = new TypeInfoAtomic();
        data.value = new AtomicReference<BaseForAtomic>(new ImplForAtomic(42));
        String json = MAPPER.writeValueAsString(data);
        TypeInfoAtomic result = MAPPER.readValue(json, TypeInfoAtomic.class);
        assertNotNull(result);
        BaseForAtomic value = result.value.get();
        assertNotNull(value);
        assertEquals(ImplForAtomic.class, value.getClass());
        assertEquals(42, ((ImplForAtomic) value).x);
    }

// com.fasterxml.jackson.databind.jsontype.PolymorphicViaRefTypeTest::testAtomicRefViaDefaultTyping
    public void testAtomicRefViaDefaultTyping() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        AtomicStringWrapper data = new AtomicStringWrapper("foo");
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        AtomicStringWrapper result = mapper.readValue(json, AtomicStringWrapper.class);
        assertNotNull(result);
        assertNotNull(result.wrapper);
        assertEquals(AtomicReference.class, result.wrapper.getClass());
        StringWrapper w = result.wrapper.get();
        assertEquals("foo", w.str);
    }

// com.fasterxml.jackson.databind.jsontype.TestAbstractContainers::testAbstractLists
    public void testAbstractLists() throws Exception
    {
        ListWrapper w = new ListWrapper();
        w.list.add("x");

        String json = MAPPER.writeValueAsString(w);
        Object o = MAPPER.readValue(json, ListWrapper.class);
        assertEquals(ListWrapper.class, o.getClass());
        ListWrapper out = (ListWrapper) o;
        assertNotNull(out.list);
        assertEquals(1, out.list.size());
        assertEquals("x", out.list.get(0));
   }

// com.fasterxml.jackson.databind.jsontype.TestAbstractContainers::testAbstractMaps
    public void testAbstractMaps() throws Exception
    {
        MapWrapper w = new MapWrapper();
        w.map.put("key1", "name1");

        String json = MAPPER.writeValueAsString(w);
        Object o = MAPPER.readValue(json, MapWrapper.class);
        assertEquals(MapWrapper.class, o.getClass());
        MapWrapper out = (MapWrapper) o;
        assertEquals(1, out.map.size());
   }

// com.fasterxml.jackson.databind.jsontype.TestAbstractTypeNames::testEmptyCollection
    public void testEmptyCollection() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        List<User>friends = new ArrayList<User>();
        friends.add(new DefaultUser("Joe Hildebrandt", null));
        friends.add(new DefaultEmployee("Richard Nasr",null,"MDA"));

        User user = new DefaultEmployee("John Vanspronssen", friends, "MDA");
        String json = mapper.writeValueAsString(user);

        
        mapper = new ObjectMapper();
        mapper.registerSubtypes(DefaultEmployee.class);
        mapper.registerSubtypes(DefaultUser.class);
        
        User result = mapper.readValue(json, User.class);
        assertNotNull(result);
        assertEquals(DefaultEmployee.class, result.getClass());

        friends = result.getFriends();
        assertEquals(2, friends.size());
        assertEquals(DefaultUser.class, friends.get(0).getClass());
        assertEquals(DefaultEmployee.class, friends.get(1).getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestAbstractTypeNames::testInnerClassWithType
    public void testInnerClassWithType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString(new BeanWithAnon());
        BeanWithAnon result = mapper.readValue(json, BeanWithAnon.class);
        assertEquals(BeanWithAnon.class, result.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testCustomTypeIdResolver
    public void testCustomTypeIdResolver() throws Exception
    {
        List<JavaType> types = new ArrayList<JavaType>();
        CustomResolver.initTypes = types;
        String json = MAPPER.writeValueAsString(new CustomBean[] { new CustomBeanImpl(28) });
        assertEquals("[{\"*\":{\"x\":28}}]", json);
        assertEquals(1, types.size());
        assertEquals(CustomBean.class, types.get(0).getRawClass());

        types = new ArrayList<JavaType>();
        CustomResolver.initTypes = types;
        CustomBean[] result = MAPPER.readValue(json, CustomBean[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(28, ((CustomBeanImpl) result[0]).x);
        assertEquals(1, types.size());
        assertEquals(CustomBean.class, types.get(0).getRawClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testCustomWithExternal
    public void testCustomWithExternal() throws Exception
    {
        ExtBeanWrapper w = new ExtBeanWrapper();
        w.value = new ExtBeanImpl(12);

        String json = MAPPER.writeValueAsString(w);

        ExtBeanWrapper out = MAPPER.readValue(json, ExtBeanWrapper.class);
        assertNotNull(out);
        
        assertEquals(12, ((ExtBeanImpl) out.value).y);
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testPolymorphicTypeViaCustom
    public void testPolymorphicTypeViaCustom() throws Exception {
        Base1270<Poly1> req = new Base1270<Poly1>();
        Poly1 o = new Poly1();
        o.val = "optionValue";
        req.options = o;
        req.val = "some value";
        Top1270 top = new Top1270();
        top.b = req;
        String json = MAPPER.writeValueAsString(top);
        JsonNode tree = MAPPER.readTree(json);
        assertNotNull(tree.get("b"));
        assertNotNull(tree.get("b").get("options"));
        assertNotNull(tree.get("b").get("options").get("val"));

        
        Top1270 itemRead = MAPPER.readValue(json, Top1270.class);
        assertNotNull(itemRead);
        assertNotNull(itemRead.b);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testArrayTypingSimple
    public void testArrayTypingSimple() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        ArrayBean bean = new ArrayBean(new String[0]);
        String json = m.writeValueAsString(bean);
        ArrayBean result = m.readValue(json, ArrayBean.class);
        assertNotNull(result.values);
        assertEquals(String[].class, result.values.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testArrayTypingNested
    public void testArrayTypingNested() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        ArrayBean bean = new ArrayBean(new String[0][0]);
        String json = m.writeValueAsString(bean);
        ArrayBean result = m.readValue(json, ArrayBean.class);
        assertNotNull(result.values);
        assertEquals(String[][].class, result.values.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testNodeInArray
    public void testNodeInArray() throws Exception
    {
        JsonNode node = new ObjectMapper().readTree("{\"a\":3}");

        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT);
        Object[] obs = new Object[] { node };
        String json = m.writeValueAsString(obs);
        Object[] result = m.readValue(json, Object[].class);
        assertEquals(1, result.length);
        Object ob = result[0];
        assertTrue(ob instanceof JsonNode);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testNodeInEmptyArray
    public void testNodeInEmptyArray() throws Exception {
        Map<String, List<String>> outerMap = new HashMap<String, List<String>>();
        outerMap.put("inner", new ArrayList<String>());
        ObjectMapper m = new ObjectMapper().disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        JsonNode tree = m.convertValue(outerMap, JsonNode.class);
        
        String json = m.writeValueAsString(tree);
        assertEquals("{}", json);
        
        JsonNode node = new ObjectMapper().readTree("{\"a\":[]}");
        
        m.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT);
        Object[] obs = new Object[] { node };
        json = m.writeValueAsString(obs);
        Object[] result = m.readValue(json, Object[].class);
        assertEquals("{}", result[0].toString());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testArraysOfArrays
    public void testArraysOfArrays() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Object value = new Object[][] { new Object[] {} };
        String json = mapper.writeValueAsString(value);

        
        _testArraysAs(mapper, json, Object[][].class);
        _testArraysAs(mapper, json, Object[].class);
        _testArraysAs(mapper, json, Object.class);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums::testSimpleEnumBean
    public void testSimpleEnumBean() throws Exception
    {
        TimeUnitBean bean = new TimeUnitBean();
        bean.timeUnit = TimeUnit.SECONDS;
        
        
        ObjectMapper m = new ObjectMapper();
        String json = m.writeValueAsString(bean);
        TimeUnitBean result = m.readValue(json, TimeUnitBean.class);
        assertEquals(TimeUnit.SECONDS, result.timeUnit);
        
        
        m = new ObjectMapper();
        m.enableDefaultTyping();
        json = m.writeValueAsString(bean);
        result = m.readValue(json, TimeUnitBean.class);

        assertEquals(TimeUnit.SECONDS, result.timeUnit);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums::testSimpleEnumsInObjectArray
    public void testSimpleEnumsInObjectArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        
        
        String json = m.writeValueAsString(new Object[] { TestEnum.A });
        assertEquals("[[\"com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums$TestEnum\",\"A\"]]", json);

        
        Object[] value = m.readValue(json, Object[].class);
        assertEquals(1, value.length);
        assertSame(TestEnum.A, value[0]);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums::testSimpleEnumsAsField
    public void testSimpleEnumsAsField() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(new EnumHolder(TestEnum.B));
        assertEquals("{\"value\":[\"com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums$TestEnum\",\"B\"]}", json);
        EnumHolder holder = m.readValue(json, EnumHolder.class);
        assertSame(TestEnum.B, holder.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testListOfLongs
    public void testListOfLongs() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        ListOfLongs input = new ListOfLongs(1L, 2L, 3L);
        String json = m.writeValueAsString(input);
        assertEquals("{\"longs\":[\"java.util.ArrayList\",[1,2,3]]}", json);
        ListOfLongs output = m.readValue(json, ListOfLongs.class);

        assertNotNull(output.longs);
        assertEquals(3, output.longs.size());
        assertEquals(Long.valueOf(1L), output.longs.get(0));
        assertEquals(Long.valueOf(2L), output.longs.get(1));
        assertEquals(Long.valueOf(3L), output.longs.get(2));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testListOfNumbers
    public void testListOfNumbers() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        ListOfNumbers input = new ListOfNumbers(Long.valueOf(1L), Integer.valueOf(2), Double.valueOf(3.0));
        String json = m.writeValueAsString(input);
        assertEquals("{\"nums\":[\"java.util.ArrayList\",[[\"java.lang.Long\",1],2,3.0]]}", json);
        ListOfNumbers output = m.readValue(json, ListOfNumbers.class);

        assertNotNull(output.nums);
        assertEquals(3, output.nums.size());
        assertEquals(Long.valueOf(1L), output.nums.get(0));
        assertEquals(Integer.valueOf(2), output.nums.get(1));
        assertEquals(Double.valueOf(3.0), output.nums.get(2));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testDateTypes
    public void testDateTypes() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        ObjectListBean input = new ObjectListBean();
        List<Object> inputList = new ArrayList<Object>();
        inputList.add(TimeZone.getTimeZone("EST"));
        inputList.add(Locale.CHINESE);
        input.values = inputList;
        String json = m.writeValueAsString(input);
        
        ObjectListBean output = m.readValue(json, ObjectListBean.class);
        List<Object> outputList = output.values;
        assertEquals(2, outputList.size());
        assertTrue(outputList.get(0) instanceof TimeZone);
        assertTrue(outputList.get(1) instanceof Locale);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testJackson628
    public void testJackson628() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        ArrayList<Foo> data = new ArrayList<Foo>();
        String json = mapper.writeValueAsString(data);
        List<?> output = mapper.readValue(json, List.class);
        assertTrue(output.isEmpty());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testJackson667
    public void testJackson667() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        String json = mapper.writeValueAsString(new SetBean("abc"));
        SetBean bean = mapper.readValue(json, SetBean.class);
        assertNotNull(bean);
        assertTrue(bean.names instanceof HashSet);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForMaps::testJackson428
    public void testJackson428() throws Exception
    {
        ObjectMapper serMapper = new ObjectMapper();

        TypeResolverBuilder<?> serializerTyper = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        serializerTyper = serializerTyper.init(JsonTypeInfo.Id.NAME, createTypeNameIdResolver(true));
        serializerTyper = serializerTyper.inclusion(JsonTypeInfo.As.PROPERTY);
        serMapper.setDefaultTyping(serializerTyper);

        
        MapHolder holder = new MapHolder();
        holder.map = new HashMap<MapKey,List<Object>>();
        List<Object> ints = new ArrayList<Object>();
        ints.add(Integer.valueOf(3));
        holder.map.put(new MapKey("key"), ints);
        String json = serMapper.writeValueAsString(holder);

        
        ObjectMapper deserMapper = new ObjectMapper();
        TypeResolverBuilder<?> deserializerTyper = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        deserializerTyper = deserializerTyper.init(JsonTypeInfo.Id.NAME, createTypeNameIdResolver(false));
        deserializerTyper = deserializerTyper.inclusion(JsonTypeInfo.As.PROPERTY);
        deserMapper.setDefaultTyping(deserializerTyper);

        MapHolder result = deserMapper.readValue(json, MapHolder.class);
        assertNotNull(result);
        Map<?,?> map = result.map;
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        Object key = entry.getKey();
        assertEquals(MapKey.class, key.getClass());
        Object value = entry.getValue();
        assertTrue(value instanceof List<?>);
        List<?> list = (List<?>) value;
        assertEquals(1, list.size());
        assertEquals(Integer.class, list.get(0).getClass());
        assertEquals(Integer.valueOf(3), list.get(0));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForMaps::testList
    public void testList() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        ItemList child = new ItemList();
        child.value = "I am child";

        ItemList parent = new ItemList();
        parent.value = "I am parent";
        parent.addChildItem(child);
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parent);

        Object o = mapper.readValue(json, ItemList.class);
        assertNotNull(o);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForMaps::testMap
    public void testMap() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        ItemMap child = new ItemMap();
        child.value = "I am child";

        ItemMap parent = new ItemMap();
        parent.value = "I am parent";
        parent.addChildItem("child", child);

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parent);
        Object o = mapper.readValue(json, ItemMap.class);
        assertNotNull(o);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testBeanAsObject
    public void testBeanAsObject() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        
        String str = m.writeValueAsString(new Object[] { new StringBean("abc") });

        _verifySerializationAsMap(str);
        
        
        Object ob = m.readValue(str, Object[].class);
        assertNotNull(ob);
        Object[] result = (Object[]) ob;
        assertNotNull(result[0]);
        assertEquals(StringBean.class, result[0].getClass());
        assertEquals("abc", ((StringBean) result[0]).name);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testBeanAsObjectUsingAsProperty
    public void testBeanAsObjectUsingAsProperty() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL,
                ".hype");
        
        String json = m.writeValueAsString(new StringBean("abc"));
        
        
        Object result = m.readValue(json, Object.class);
        assertNotNull(result);
        assertEquals(StringBean.class, result.getClass());
        assertEquals("abc", ((StringBean) result).name);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testAbstractBean
    public void testAbstractBean() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        AbstractBean[] input = new AbstractBean[] { new StringBean("xyz") };
        String serial = m.writeValueAsString(input);
        try {
            m.readValue(serial, AbstractBean[].class);
            fail("Should have failed");
        } catch (JsonMappingException e) {
            
            verifyException(e, "can not construct");
        }
        
        
        m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        serial = m.writeValueAsString(input);
        AbstractBean[] beans = m.readValue(serial, AbstractBean[].class);
        assertEquals(1, beans.length);
        assertEquals(StringBean.class, beans[0].getClass());
        assertEquals("xyz", ((StringBean) beans[0]).name);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testNonFinalBean
    public void testNonFinalBean() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        StringBean bean = new StringBean("x");
        assertEquals("{\"name\":\"x\"}", m.writeValueAsString(bean));
        
        m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        assertEquals("[\""+StringBean.class.getName()+"\",{\"name\":\"x\"}]",
            m.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testNullValue
    public void testNullValue() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        BeanHolder h = new BeanHolder();
        String json = m.writeValueAsString(h);
        assertNotNull(json);
        BeanHolder result = m.readValue(json, BeanHolder.class);
        assertNotNull(result);
        assertNull(result.bean);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testEnumAsObject
    public void testEnumAsObject() {}

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testEnumSet
    public void testEnumSet() throws Exception
    {
        EnumSet<Choice> set = EnumSet.of(Choice.NO);
        Object[] input = new Object[] { set };
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(input);
        Object[] output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        Object ob = output[0];
        assertTrue(ob instanceof EnumSet<?>);
        EnumSet<Choice> set2 = (EnumSet<Choice>) ob;
        assertEquals(1, set2.size());
        assertTrue(set2.contains(Choice.NO));
        assertFalse(set2.contains(Choice.YES));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testEnumMap
    public void testEnumMap() throws Exception
    {
        EnumMap<Choice,String> map = new EnumMap<Choice,String>(Choice.class);
        map.put(Choice.NO, "maybe");
        Object[] input = new Object[] { map };
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(input);
        Object[] output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        Object ob = output[0];
        assertTrue(ob instanceof EnumMap<?,?>);
        EnumMap<Choice,String> map2 = (EnumMap<Choice,String>) ob;
        assertEquals(1, map2.size());
        assertEquals("maybe", map2.get(Choice.NO));
        assertNull(map2.get(Choice.YES));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testJackson311
    public void testJackson311() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString(new PolymorphicType("hello", 2));
        PolymorphicType value = mapper.readValue(json, PolymorphicType.class);
        assertEquals("hello", value.foo);
        assertEquals(Integer.valueOf(2), value.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testTokenBuffer
    public void testTokenBuffer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        
        TokenBuffer buf = new TokenBuffer(mapper, false);
        buf.writeStartObject();
        buf.writeNumberField("num", 42);
        buf.writeEndObject();
        String json = mapper.writeValueAsString(new ObjectHolder(buf));
        ObjectHolder holder = mapper.readValue(json, ObjectHolder.class);
        assertNotNull(holder.value);
        assertSame(TokenBuffer.class, holder.value.getClass());
        JsonParser jp = ((TokenBuffer) holder.value).asParser();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();

        
        buf = new TokenBuffer(mapper, false);
        buf.writeStartArray();
        buf.writeBoolean(true);
        buf.writeEndArray();
        json = mapper.writeValueAsString(new ObjectHolder(buf));
        holder = mapper.readValue(json, ObjectHolder.class);
        assertNotNull(holder.value);
        assertSame(TokenBuffer.class, holder.value.getClass());
        jp = ((TokenBuffer) holder.value).asParser();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();

        
        buf = new TokenBuffer(mapper, false);
        buf.writeNumber(321);
        json = mapper.writeValueAsString(new ObjectHolder(buf));
        holder = mapper.readValue(json, ObjectHolder.class);
        assertNotNull(holder.value);
        assertSame(TokenBuffer.class, holder.value.getClass());
        jp = ((TokenBuffer) holder.value).asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(321, jp.getIntValue());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testIssue352
    public void testIssue352() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping (ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        DiscussBean d1 = new DiscussBean();
        d1.subject = "mouse";
        d1.weight=88;
        DomainBeanWrapper wrapper = new DomainBeanWrapper();
        wrapper.name = "mickey";
        wrapper.myBean = d1;
        String json = mapper.writeValueAsString(wrapper);
        DomainBeanWrapper result = mapper.readValue(json, DomainBeanWrapper.class);
        assertNotNull(result);
        assertNotNull(wrapper.myBean);
        assertSame(DiscussBean.class, wrapper.myBean.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testFeature432
    public void testFeature432() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "*CLASS*");
        String json = mapper.writeValueAsString(new BeanHolder(new StringBean("punny")));
        assertEquals("{\"bean\":{\"*CLASS*\":\"com.fasterxml.jackson.databind.jsontype.TestDefaultForObject$StringBean\",\"name\":\"punny\"}}", json);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testNoGoWithExternalProperty
    public void testNoGoWithExternalProperty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT,
                    JsonTypeInfo.As.EXTERNAL_PROPERTY);
            fail("Should not have passed");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Can not use includeAs of EXTERNAL_PROPERTY");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testNumericScalars
    public void testNumericScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[123]", m.writeValueAsString(new Object[] { Integer.valueOf(123) }));
        assertEquals("[[\"java.lang.Long\",37]]", m.writeValueAsString(new Object[] { Long.valueOf(37) }));
        assertEquals("[0.25]", m.writeValueAsString(new Object[] { Double.valueOf(0.25) }));
        assertEquals("[[\"java.lang.Float\",0.5]]", m.writeValueAsString(new Object[] { Float.valueOf(0.5f) }));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testDateScalars
    public void testDateScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        long ts = 12345678L;
        assertEquals("[[\"java.util.Date\","+ts+"]]",
                m.writeValueAsString(new Object[] { new Date(ts) }));

        
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        String json = m.writeValueAsString(new Object[] { c });
        assertEquals("[[\""+c.getClass().getName()+"\","+ts+"]]", json);
        
        Object[] result = m.readValue(json, Object[].class);
        assertEquals(1, result.length);
        assertTrue(result[0] instanceof Calendar);
        assertEquals(ts, ((Calendar) result[0]).getTimeInMillis());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testMiscScalars
    public void testMiscScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[\"abc\"]", m.writeValueAsString(new Object[] { "abc" }));
        assertEquals("[true,null,false]", m.writeValueAsString(new Boolean[] { true, null, false }));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testScalarArrays
    public void testScalarArrays() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        Object[] input = new Object[] {
                "abc", new Date(1234567), null, Integer.valueOf(456)
        };
        String json = m.writeValueAsString(input);
        assertEquals("[\"abc\",[\"java.util.Date\",1234567],null,456]", json);

        
        Object[] output = m.readValue(json, Object[].class);
        assertArrayEquals(input, output);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::test417
    public void test417() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        Jackson417Bean input = new Jackson417Bean();
        String json = m.writeValueAsString(input);
        Jackson417Bean result = m.readValue(json, Jackson417Bean.class);
        assertEquals(input.foo, result.foo);
        assertEquals(input.bar, result.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForTreeNodes::testValueAsStringWithDefaultTyping
    public void testValueAsStringWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        String json = DEFAULT_MAPPER.writeValueAsString(foo);

        JsonNode jsonNode = DEFAULT_MAPPER.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForTreeNodes::testValueToTreeWithDefaultTyping
    public void testValueToTreeWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        JsonNode jsonNode = DEFAULT_MAPPER.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultWithCreators::testWithCreators
    public void testWithCreators() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        UrlJob input = new UrlJob(123L, "http://foo", 3);
        String json = mapper.writeValueAsString(input);
        assertNotNull(json);
        Job output = mapper.readValue(json, Job.class);
        assertNotNull(output);
        assertSame(UrlJob.class, output.getClass());
        UrlJob o2 = (UrlJob) output;
        assertEquals(123L, o2.id);
        assertEquals("http://foo", o2.getUrl());
        assertEquals(3, o2.getCount());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultWithCreators::testWithCreatorAndJsonValue
    public void testWithCreatorAndJsonValue() throws Exception
    {
        final byte[] BYTES = new byte[] { 1, 2, 3, 4, 5 };
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        String json = mapper.writeValueAsString(new Bean1385Wrapper(
                new Bean1385(BYTES)
        ));
        Bean1385Wrapper result = mapper.readValue(json, Bean1385Wrapper.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(Bean1385.class, result.value.getClass());
        Bean1385 b = (Bean1385) result.value;
        Assert.assertArrayEquals(BYTES, b.raw);
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testTagList
    public void testTagList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        TagList list = new TagList();
        list.add(Tag.A);
        list.add(Tag.B);
        String json = m.writeValueAsString(list);

        TagList result = m.readValue(json, TagList.class);
        assertEquals(2, result.size());
        assertSame(Tag.A, result.get(0));
        assertSame(Tag.B, result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testEnumInterface
    public void testEnumInterface() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String json = m.writeValueAsString(Tag.B);
        EnumInterface result = m.readValue(json, EnumInterface.class);
        assertSame(Tag.B, result);
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testEnumInterfaceList
    public void testEnumInterfaceList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        EnumInterfaceList list = new EnumInterfaceList();
        list.add(Tag.A);
        list.add(Tag.B);
        String json = m.writeValueAsString(list);
        
        EnumInterfaceList result = m.readValue(json, EnumInterfaceList.class);
        assertEquals(2, result.size());
        assertSame(Tag.A, result.get(0));
        assertSame(Tag.B, result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testUntypedEnum
    public void testUntypedEnum() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String str = mapper.writeValueAsString(new UntypedEnumBean(TestEnum.B));
        UntypedEnumBean result = mapper.readValue(str, UntypedEnumBean.class);
        assertNotNull(result);
        assertNotNull(result.value);
        Object ob = result.value;
        assertSame(TestEnum.class, ob.getClass());
        assertEquals(TestEnum.B, result.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestGenericListSerialization::testSubTypesFor356
    public void testSubTypesFor356() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        JSONResponse<List<Parent>> input = new JSONResponse<List<Parent>>();

        List<Parent> embedded = new ArrayList<Parent>();
        embedded.add(new Child1());
        embedded.add(new Child2());
        input.setResult(embedded);
        mapper.configure(MapperFeature.USE_STATIC_TYPING, true);

        JavaType rootType = TypeFactory.defaultInstance().constructType(new TypeReference<JSONResponse<List<Parent>>>() { });
        byte[] json = mapper.writerFor(rootType).writeValueAsBytes(input);
        
        JSONResponse<List<Parent>> out = mapper.readValue(json, 0, json.length, rootType);

        List<Parent> deserializedContent = out.getResult();

        assertEquals(2, deserializedContent.size());
        assertTrue(deserializedContent.get(0) instanceof Parent);
        assertTrue(deserializedContent.get(0) instanceof Child1);
        assertFalse(deserializedContent.get(0) instanceof Child2);
        assertTrue(deserializedContent.get(1) instanceof Child2);
        assertFalse(deserializedContent.get(1) instanceof Child1);

        assertEquals("PARENT", ((Child1) deserializedContent.get(0)).parentContent);
        assertEquals("PARENT", ((Child2) deserializedContent.get(1)).parentContent);
        assertEquals("CHILD1", ((Child1) deserializedContent.get(0)).childContent1);
        assertEquals("CHILD2", ((Child2) deserializedContent.get(1)).childContent2);
    }

// com.fasterxml.jackson.databind.jsontype.TestNoTypeInfo::testWithIdNone
    public void testWithIdNone() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        
        String json = mapper.writeValueAsString(new NoType());
        assertEquals("{\"a\":3}", json);

        
        NoTypeInterface bean = mapper.readValue("{\"a\":6}", NoTypeInterface.class);
        assertNotNull(bean);
        NoType impl = (NoType) bean;
        assertEquals(6, impl.a);
    }

// com.fasterxml.jackson.databind.jsontype.TestOverlappingTypeIdNames::testOverlappingNameDeser
    public void testOverlappingNameDeser() throws Exception
    {
        Base312 value;

        

        value = MAPPER.readValue(aposToQuotes("{'type':'a','x':7}"), Base312.class);
        assertNotNull(value);
        assertEquals(Impl312.class, value.getClass());
        assertEquals(7, ((Impl312) value).x);
        
        value = MAPPER.readValue(aposToQuotes("{'type':'b','x':3}"), Base312.class);
        assertNotNull(value);
        assertEquals(Impl312.class, value.getClass());
        assertEquals(3, ((Impl312) value).x);
    }

// com.fasterxml.jackson.databind.jsontype.TestOverlappingTypeIdNames::testOverlappingNameSer
    public void testOverlappingNameSer() throws Exception
    {
        assertEquals(aposToQuotes("{'type':'a','value':1}"),
                MAPPER.writeValueAsString(new Impl312B1()));
        assertEquals(aposToQuotes("{'type':'a','value':1}"),
                MAPPER.writeValueAsString(new Impl312B2()));
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleField
    public void testSimpleField() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new FieldWrapperBean(new StringWrapper("foo")));

        FieldWrapperBean bean = mapper.readValue(json, FieldWrapperBean.class);
        assertNotNull(bean.value);
        assertEquals(StringWrapper.class, bean.value.getClass());
        assertEquals(((StringWrapper) bean.value).str, "foo");
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleMethod
    public void testSimpleMethod() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new FieldWrapperBean(new IntWrapper(37)));

        FieldWrapperBean bean = mapper.readValue(json, FieldWrapperBean.class);
        assertNotNull(bean.value);
        assertEquals(IntWrapper.class, bean.value.getClass());
        assertEquals(((IntWrapper) bean.value).i, 37);
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleListField
    public void testSimpleListField() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        FieldWrapperBeanList list = new FieldWrapperBeanList();
        list.add(new FieldWrapperBean(new OtherBean()));
        String json = mapper.writeValueAsString(list);

        FieldWrapperBeanList result = mapper.readValue(json, FieldWrapperBeanList.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        FieldWrapperBean bean = list.get(0);
        assertEquals(OtherBean.class, bean.value.getClass());
        assertEquals(((OtherBean) bean.value).x, 1);
        assertEquals(((OtherBean) bean.value).y, 1);
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleListMethod
    public void testSimpleListMethod() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        MethodWrapperBeanList list = new MethodWrapperBeanList();
        list.add(new MethodWrapperBean(new BooleanWrapper(true)));
        list.add(new MethodWrapperBean(new StringWrapper("x")));
        list.add(new MethodWrapperBean(new OtherBean()));
        String json = mapper.writeValueAsString(list);
        MethodWrapperBeanList result = mapper.readValue(json, MethodWrapperBeanList.class);
        assertNotNull(result);
        assertEquals(3, result.size());
        MethodWrapperBean bean = result.get(0);
        assertEquals(BooleanWrapper.class, bean.value.getClass());
        assertEquals(((BooleanWrapper) bean.value).b, Boolean.TRUE);
        bean = result.get(1);
        assertEquals(StringWrapper.class, bean.value.getClass());
        assertEquals(((StringWrapper) bean.value).str, "x");
        bean = result.get(2);
        assertEquals(OtherBean.class, bean.value.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleArrayField
    public void testSimpleArrayField() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        FieldWrapperBeanArray array = new FieldWrapperBeanArray(new
                FieldWrapperBean[] { new FieldWrapperBean(new BooleanWrapper(true)) });
        String json = mapper.writeValueAsString(array);
        FieldWrapperBeanArray result = mapper.readValue(json, FieldWrapperBeanArray.class);
        assertNotNull(result);
        FieldWrapperBean[] beans = result.beans;
        assertEquals(1, beans.length);
        FieldWrapperBean bean = beans[0];
        assertEquals(BooleanWrapper.class, bean.value.getClass());
        assertEquals(((BooleanWrapper) bean.value).b, Boolean.TRUE);
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleArrayMethod
    public void testSimpleArrayMethod() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        MethodWrapperBeanArray array = new MethodWrapperBeanArray(new
                MethodWrapperBean[] { new MethodWrapperBean(new StringWrapper("A")) });
        String json = mapper.writeValueAsString(array);
        MethodWrapperBeanArray result = mapper.readValue(json, MethodWrapperBeanArray.class);
        assertNotNull(result);
        MethodWrapperBean[] beans = result.beans;
        assertEquals(1, beans.length);
        MethodWrapperBean bean = beans[0];
        assertEquals(StringWrapper.class, bean.value.getClass());
        assertEquals(((StringWrapper) bean.value).str, "A");
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleMapField
    public void testSimpleMapField() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        FieldWrapperBeanMap map = new FieldWrapperBeanMap();
        map.put("foop", new FieldWrapperBean(new IntWrapper(13)));
        String json = mapper.writeValueAsString(map);
        FieldWrapperBeanMap result = mapper.readValue(json, FieldWrapperBeanMap.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        FieldWrapperBean bean = result.get("foop");
        assertNotNull(bean);
        Object ob = bean.value;
        assertEquals(IntWrapper.class, ob.getClass());
        assertEquals(((IntWrapper) ob).i, 13);
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleMapMethod
    public void testSimpleMapMethod() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        MethodWrapperBeanMap map = new MethodWrapperBeanMap();
        map.put("xyz", new MethodWrapperBean(new BooleanWrapper(true)));
        String json = mapper.writeValueAsString(map);
        MethodWrapperBeanMap result = mapper.readValue(json, MethodWrapperBeanMap.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        MethodWrapperBean bean = result.get("xyz");
        assertNotNull(bean);
        Object ob = bean.value;
        assertEquals(BooleanWrapper.class, ob.getClass());
        assertEquals(((BooleanWrapper) ob).b, Boolean.TRUE);
    }

// com.fasterxml.jackson.databind.jsontype.TestScalars::testScalarsWithTyping
    public void testScalarsWithTyping() throws Exception
    {
        String json;
        DynamicWrapper result;
        ObjectMapper m = MAPPER;

        
        json = m.writeValueAsString(new DynamicWrapper(Integer.valueOf(3)));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals(Integer.valueOf(3), result.value);

        json = m.writeValueAsString(new DynamicWrapper("abc"));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new DynamicWrapper("abc"));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new DynamicWrapper(Boolean.TRUE));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals(Boolean.TRUE, result.value);
        
        
        json = m.writeValueAsString(new DynamicWrapper(Long.valueOf(7L)));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals(Long.valueOf(7), result.value);

        json = m.writeValueAsString(new DynamicWrapper(TestEnum.B));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals(TestEnum.B, result.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestScalars::testScalarsViaAbstractType
    public void testScalarsViaAbstractType() throws Exception
    {
        ObjectMapper m = MAPPER;
        String json;
        AbstractWrapper result;

        
        json = m.writeValueAsString(new AbstractWrapper(Integer.valueOf(3)));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(Integer.valueOf(3), result.value);

        json = m.writeValueAsString(new AbstractWrapper("abc"));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new AbstractWrapper("abc"));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new AbstractWrapper(Boolean.TRUE));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(Boolean.TRUE, result.value);
        
        
        json = m.writeValueAsString(new AbstractWrapper(Long.valueOf(7L)));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(Long.valueOf(7), result.value);

        json = m.writeValueAsString(new AbstractWrapper(TestEnum.B));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(TestEnum.B, result.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestScalars::testHeterogenousStringScalars
    public void testHeterogenousStringScalars() throws Exception
    {
        final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        ScalarList input = new ScalarList()
                .add("Test")
                .add(java.lang.Object.class)
                .add(NULL_UUID)
                ;
        String json = MAPPER.writeValueAsString(input);

        ScalarList result = MAPPER.readValue(json, ScalarList.class);
        assertNotNull(result.values);
        assertEquals(3, result.values.size());
        assertEquals("Test", result.values.get(0));
        assertEquals(Object.class, result.values.get(1));
        assertEquals(NULL_UUID, result.values.get(2));
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testPropertyWithSubtypes
    public void testPropertyWithSubtypes() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.registerSubtypes(SubB.class, SubC.class, SubD.class);
        String json = mapper.writeValueAsString(new PropertyBean(new SubC()));
        PropertyBean result = mapper.readValue(json, PropertyBean.class);
        assertSame(SubC.class, result.value.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testSubtypesViaModule
    public void testSubtypesViaModule() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.registerSubtypes(SubB.class, SubC.class, SubD.class);
        mapper.registerModule(module);
        String json = mapper.writeValueAsString(new PropertyBean(new SubC()));
        PropertyBean result = mapper.readValue(json, PropertyBean.class);
        assertSame(SubC.class, result.value.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testSerialization
    public void testSerialization() throws Exception
    {
        
        SubB bean = new SubB();
        assertEquals("{\"@type\":\"TypeB\",\"b\":1}", MAPPER.writeValueAsString(bean));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(new NamedType(SubB.class, "typeB"));
        assertEquals("{\"@type\":\"typeB\",\"b\":1}", mapper.writeValueAsString(bean));

        
        assertEquals("{\"@type\":\"TestSubtypes$SubD\",\"d\":0}", mapper.writeValueAsString(new SubD()));  
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testDeserializationNonNamed
    public void testDeserializationNonNamed() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubC.class);

        
        SuperType bean = mapper.readValue("{\"@type\":\"TestSubtypes$SubC\", \"c\":1}", SuperType.class);
        assertSame(SubC.class, bean.getClass());
        assertEquals(1, ((SubC) bean).c);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testDeserializatioNamed
    public void testDeserializatioNamed() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubB.class);
        mapper.registerSubtypes(new NamedType(SubD.class, "TypeD"));

        SuperType bean = mapper.readValue("{\"@type\":\"TypeB\", \"b\":13}", SuperType.class);
        assertSame(SubB.class, bean.getClass());
        assertEquals(13, ((SubB) bean).b);

        
        bean = mapper.readValue("{\"@type\":\"TypeD\", \"d\":-4}", SuperType.class);
        assertSame(SubD.class, bean.getClass());
        assertEquals(-4, ((SubD) bean).d);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testEmptyBean
    public void testEmptyBean() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        String json = mapper.writeValueAsString(new EmptyBean());
        assertEquals("{\"@type\":\"TestSubtypes$EmptyBean\"}", json);

        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        json = mapper.writeValueAsString(new EmptyBean());
        assertEquals("{\"@type\":\"TestSubtypes$EmptyBean\"}", json);

        
        mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        json = mapper.writeValueAsString(new EmptyNonFinal());
        assertEquals("[\"com.fasterxml.jackson.databind.jsontype.TestSubtypes$EmptyNonFinal\",{}]", json);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testDefaultImpl
    public void testDefaultImpl() throws Exception
    {
        
        SuperTypeWithDefault bean = MAPPER.readValue("{\"a\":13}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(13, ((DefaultImpl) bean).a);

        
        bean = MAPPER.readValue("{\"a\":14,\"#type\":\"foobar\"}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(14, ((DefaultImpl) bean).a);

        bean = MAPPER.readValue("{\"#type\":\"foobar\",\"a\":15}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(15, ((DefaultImpl) bean).a);

        bean = MAPPER.readValue("{\"#type\":\"foobar\"}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(0, ((DefaultImpl) bean).a);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testDefaultImplViaModule
    public void testDefaultImplViaModule() throws Exception
    {
        final String JSON = "{\"a\":123}";
        
        
        try {
            MAPPER.readValue(JSON, SuperTypeWithoutDefault.class);
            fail("Expected an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "missing property");
        }

        
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addAbstractTypeMapping(SuperTypeWithoutDefault.class, DefaultImpl505.class);
        mapper.registerModule(module);
        SuperTypeWithoutDefault bean = mapper.readValue(JSON, SuperTypeWithoutDefault.class);
        assertNotNull(bean);
        assertEquals(DefaultImpl505.class, bean.getClass());
        assertEquals(123, ((DefaultImpl505) bean).a);

        bean = mapper.readValue("{\"#type\":\"foobar\"}", SuperTypeWithoutDefault.class);
        assertEquals(DefaultImpl505.class, bean.getClass());
        assertEquals(0, ((DefaultImpl505) bean).a);
    
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testErrorMessage
    public void testErrorMessage() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{ \"type\": \"z\"}", BaseX.class);
            fail("Should have failed");
        } catch (JsonMappingException e) {
            verifyException(e, "known type ids =");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testViaAtomic
    public void testViaAtomic() throws Exception {
        AtomicWrapper input = new AtomicWrapper(3);
        String json = MAPPER.writeValueAsString(input);

        AtomicWrapper output = MAPPER.readValue(json, AtomicWrapper.class);
        assertNotNull(output);
        assertEquals(ImplX.class, output.value.getClass());
        assertEquals(3, ((ImplX) output.value).x);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testIssue1125NonDefault
    public void testIssue1125NonDefault() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Issue1125Wrapper(new Impl1125(1, 2, 3)));
        
        Issue1125Wrapper result = MAPPER.readValue(json, Issue1125Wrapper.class);
        assertNotNull(result.value);
        assertEquals(Impl1125.class, result.value.getClass());
        Impl1125 impl = (Impl1125) result.value;
        assertEquals(1, impl.a);
        assertEquals(2, impl.b);
        assertEquals(3, impl.c);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testIssue1125WithDefault
    public void testIssue1125WithDefault() throws Exception
    {
        Issue1125Wrapper result = MAPPER.readValue(aposToQuotes("{'value':{'a':3,'def':9,'b':5}}"),
        		Issue1125Wrapper.class);
        assertNotNull(result.value);
        assertEquals(Default1125.class, result.value.getClass());
        Default1125 impl = (Default1125) result.value;
        assertEquals(3, impl.a);
        assertEquals(5, impl.b);
        assertEquals(9, impl.def);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testExistingPropertySerializationFruits
    public void testExistingPropertySerializationFruits() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, pinguo);
        assertEquals(3, result.size());
        assertEquals(pinguo.name, result.get("name"));
        assertEquals(pinguo.seedCount, result.get("seedCount"));
        assertEquals(pinguo.type, result.get("type"));
        
        result = writeAndMap(MAPPER, mandarin);
        assertEquals(3, result.size());
        assertEquals(mandarin.name, result.get("name"));
        assertEquals(mandarin.color, result.get("color"));
        assertEquals(mandarin.type, result.get("type"));
        
        String pinguoSerialized = MAPPER.writeValueAsString(pinguo);
        assertEquals(pinguoSerialized, pinguoJson);

        String mandarinSerialized = MAPPER.writeValueAsString(mandarin);
        assertEquals(mandarinSerialized, mandarinJson);

        String fruitWrapperSerialized = MAPPER.writeValueAsString(pinguoWrapper);
        assertEquals(fruitWrapperSerialized, pinguoWrapperJson);

        String fruitListSerialized = MAPPER.writeValueAsString(fruitList);
        assertEquals(fruitListSerialized, fruitListJson);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testSimpleClassAsExistingPropertyDeserializationFruits
    public void testSimpleClassAsExistingPropertyDeserializationFruits() throws Exception
    {
        Fruit pinguoDeserialized = MAPPER.readValue(pinguoJson, Fruit.class);
        assertTrue(pinguoDeserialized instanceof Apple);
        assertSame(pinguoDeserialized.getClass(), Apple.class);
        assertEquals(pinguo.name, pinguoDeserialized.name);
        assertEquals(pinguo.seedCount, ((Apple) pinguoDeserialized).seedCount);
        assertEquals(pinguo.type, ((Apple) pinguoDeserialized).type);

        FruitWrapper pinguoWrapperDeserialized = MAPPER.readValue(pinguoWrapperJson, FruitWrapper.class);
        Fruit pinguoExtracted = pinguoWrapperDeserialized.fruit;
        assertTrue(pinguoExtracted instanceof Apple);
        assertSame(pinguoExtracted.getClass(), Apple.class);
        assertEquals(pinguo.name, pinguoExtracted.name);
        assertEquals(pinguo.seedCount, ((Apple) pinguoExtracted).seedCount);
        assertEquals(pinguo.type, ((Apple) pinguoExtracted).type);

        Fruit[] fruits = MAPPER.readValue(fruitListJson, Fruit[].class);
        assertEquals(2, fruits.length);
        assertEquals(Apple.class, fruits[0].getClass());
        assertEquals("apple", ((Apple) fruits[0]).type);
        assertEquals(Orange.class, fruits[1].getClass());
        assertEquals("orange", ((Orange) fruits[1]).type);
        
        List<Fruit> f2 = MAPPER.readValue(fruitListJson,
                new TypeReference<List<Fruit>>() { });
        assertNotNull(f2);
        assertTrue(f2.size() == 2);
        assertEquals(Apple.class, f2.get(0).getClass());
        assertEquals(Orange.class, f2.get(1).getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testExistingPropertySerializationAnimals
    public void testExistingPropertySerializationAnimals() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, beelzebub);
        assertEquals(3, result.size());
        assertEquals(beelzebub.name, result.get("name"));
        assertEquals(beelzebub.furColor, result.get("furColor"));
        assertEquals(beelzebub.getType(), result.get("type"));

        result = writeAndMap(MAPPER, rover);
        assertEquals(3, result.size());
        assertEquals(rover.name, result.get("name"));
        assertEquals(rover.boneCount, result.get("boneCount"));
        assertEquals(rover.getType(), result.get("type"));
        
        String beelzebubSerialized = MAPPER.writeValueAsString(beelzebub);
        assertEquals(beelzebubSerialized, beelzebubJson);
        
        String roverSerialized = MAPPER.writeValueAsString(rover);
        assertEquals(roverSerialized, roverJson);
        
        String animalWrapperSerialized = MAPPER.writeValueAsString(beelzebubWrapper);
        assertEquals(animalWrapperSerialized, beelzebubWrapperJson);

        String animalListSerialized = MAPPER.writeValueAsString(animalList);
        assertEquals(animalListSerialized, animalListJson);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testSimpleClassAsExistingPropertyDeserializationAnimals
    public void testSimpleClassAsExistingPropertyDeserializationAnimals() throws Exception
    {
        Animal beelzebubDeserialized = MAPPER.readValue(beelzebubJson, Animal.class);
        assertTrue(beelzebubDeserialized instanceof Cat);
        assertSame(beelzebubDeserialized.getClass(), Cat.class);
        assertEquals(beelzebub.name, beelzebubDeserialized.name);
        assertEquals(beelzebub.furColor, ((Cat) beelzebubDeserialized).furColor);
        assertEquals(beelzebub.getType(), beelzebubDeserialized.getType());

        AnimalWrapper beelzebubWrapperDeserialized = MAPPER.readValue(beelzebubWrapperJson, AnimalWrapper.class);
        Animal beelzebubExtracted = beelzebubWrapperDeserialized.animal;
        assertTrue(beelzebubExtracted instanceof Cat);
        assertSame(beelzebubExtracted.getClass(), Cat.class);
        assertEquals(beelzebub.name, beelzebubExtracted.name);
        assertEquals(beelzebub.furColor, ((Cat) beelzebubExtracted).furColor);
        assertEquals(beelzebub.getType(), beelzebubExtracted.getType());
    	
        @SuppressWarnings("unchecked")
        List<Animal> animalListDeserialized = MAPPER.readValue(animalListJson, List.class);
        assertNotNull(animalListDeserialized);
        assertTrue(animalListDeserialized.size() == 2);
        Animal cat = MAPPER.convertValue(animalListDeserialized.get(0), Animal.class);
        assertTrue(cat instanceof Cat);
        assertSame(cat.getClass(), Cat.class);
        Animal dog = MAPPER.convertValue(animalListDeserialized.get(1), Animal.class);
        assertTrue(dog instanceof Dog);
        assertSame(dog.getClass(), Dog.class);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testExistingPropertySerializationCars
    public void testExistingPropertySerializationCars() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, camry);
        assertEquals(3, result.size());
        assertEquals(camry.name, result.get("name"));
        assertEquals(camry.exteriorColor, result.get("exteriorColor"));
        assertEquals(camry.getType(), result.get("type"));

        result = writeAndMap(MAPPER, accord);
        assertEquals(3, result.size());
        assertEquals(accord.name, result.get("name"));
        assertEquals(accord.speakerCount, result.get("speakerCount"));
        assertEquals(accord.getType(), result.get("type"));
        
        String camrySerialized = MAPPER.writeValueAsString(camry);
        assertEquals(camrySerialized, camryJson);
        
        String accordSerialized = MAPPER.writeValueAsString(accord);
        assertEquals(accordSerialized, accordJson);
        
        String carWrapperSerialized = MAPPER.writeValueAsString(camryWrapper);
        assertEquals(carWrapperSerialized, camryWrapperJson);

        String carListSerialized = MAPPER.writeValueAsString(carList);
        assertEquals(carListSerialized, carListJson);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testSimpleClassAsExistingPropertyDeserializationCars
    public void testSimpleClassAsExistingPropertyDeserializationCars() throws Exception
    {
        Car camryDeserialized = MAPPER.readValue(camryJson, Camry.class);
        assertTrue(camryDeserialized instanceof Camry);
        assertSame(camryDeserialized.getClass(), Camry.class);
        assertEquals(camry.name, camryDeserialized.name);
        assertEquals(camry.exteriorColor, ((Camry) camryDeserialized).exteriorColor);
        assertEquals(camry.getType(), ((Camry) camryDeserialized).getType());

        CarWrapper camryWrapperDeserialized = MAPPER.readValue(camryWrapperJson, CarWrapper.class);
        Car camryExtracted = camryWrapperDeserialized.car;
        assertTrue(camryExtracted instanceof Camry);
        assertSame(camryExtracted.getClass(), Camry.class);
        assertEquals(camry.name, camryExtracted.name);
        assertEquals(camry.exteriorColor, ((Camry) camryExtracted).exteriorColor);
        assertEquals(camry.getType(), ((Camry) camryExtracted).getType());

        @SuppressWarnings("unchecked")
        List<Car> carListDeserialized = MAPPER.readValue(carListJson, List.class);
        assertNotNull(carListDeserialized);
        assertTrue(carListDeserialized.size() == 2);
        Car result = MAPPER.convertValue(carListDeserialized.get(0), Car.class);
        assertTrue(result instanceof Camry);
        assertSame(result.getClass(), Camry.class);

        result = MAPPER.convertValue(carListDeserialized.get(1), Car.class);
        assertTrue(result instanceof Accord);
        assertSame(result.getClass(), Accord.class);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testSerialization
    public void testSerialization() throws Exception
    {
        ObjectMapper m = new ObjectMapper();

        
        
        
        
        assertEquals("[{\"doggy\":{\"name\":\"Spot\",\"ageInYears\":3}}]",
                m.writeValueAsString(new Animal[] { new Dog("Spot", 3) }));
        assertEquals("[{\"MaineCoon\":{\"name\":\"Belzebub\",\"purrs\":true}}]",
                m.writeValueAsString(new Animal[] { new MaineCoon("Belzebub", true)}));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testRoundTrip
    public void testRoundTrip() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Animal[] input = new Animal[] {
                new Dog("Odie", 7),
                null,
                new MaineCoon("Piru", false),
                new Persian("Khomeini", true)
        };
        String json = m.writeValueAsString(input);
        List<Animal> output = m.readValue(json,
                TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Animal.class));
        assertEquals(input.length, output.size());
        for (int i = 0, len = input.length; i < len; ++i) {
            assertEquals("Entry #"+i+" differs, input = '"+json+"'",
                input[i], output.get(i));
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testRoundTripMap
    public void testRoundTripMap() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        AnimalMap input = new AnimalMap();
        input.put("venla", new MaineCoon("Venla", true));
        input.put("ama", new Dog("Amadeus", 13));
        String json = m.writeValueAsString(input);
        AnimalMap output = m.readValue(json, AnimalMap.class);
        assertNotNull(output);
        assertEquals(AnimalMap.class, output.getClass());
        assertEquals(input.size(), output.size());

        
        for (String name : input.keySet()) {
            Animal in = input.get(name);
            Animal out = output.get(name);
            if (!in.equals(out)) {
                fail("Animal in input was ["+in+"]; output not matching: ["+out+"]");
            }
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testListWithPolymorphic
    public void testListWithPolymorphic() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        BeanListWrapper beans = new BeanListWrapper();
        assertEquals("{\"beans\":[{\"@type\":\"bean\",\"x\":0}]}", mapper.writeValueAsString(beans));
        
        ObjectWriter w = mapper.writerWithView(Object.class);
        assertEquals("{\"beans\":[{\"@type\":\"bean\",\"x\":0}]}", w.writeValueAsString(beans));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testIntList
    public void testIntList() {}

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testStringListAsProp
    public void testStringListAsProp() {}

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testStringListAsObjectWrapper
    public void testStringListAsObjectWrapper() {}

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testIntArray
    public void testIntArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(int[].class, WrapperMixIn.class);
        int[] input = new int[] { 1, 2, 3 };
        String clsName = int[].class.getName();
        assertEquals("{\""+clsName+"\":[1,2,3]}", serializeAsString(m, input));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testGenericArray
    public void testGenericArray() throws Exception
    {
        ObjectMapper m;
        final A[] input = new A[] { new B() };
        final String EXP = "[{\"BB\":{\"value\":2}}]";

        
        m = new ObjectMapper();
        assertEquals(EXP, m.writeValueAsString(input));

        
        m = new ObjectMapper();
        m.configure(MapperFeature.USE_STATIC_TYPING, true);
        assertEquals(EXP, m.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedContainerSerialization::testPolymorphicWithContainer
    public void testPolymorphicWithContainer() throws Exception
    {
		Dog dog = new Dog("medor");
		dog.setBoneCount(3);
		Container1 c1 = new Container1();
		c1.setAnimal(dog);
		String s1 = mapper.writeValueAsString(c1);
		Assert.assertTrue("polymorphic type info is kept (1)", s1
				.indexOf("\"object-type\":\"doggy\"") >= 0);
		Container2<Animal> c2 = new Container2<Animal>();
		c2.setAnimal(dog);
		String s2 = mapper.writeValueAsString(c2);
		Assert.assertTrue("polymorphic type info is kept (2)", s2
				.indexOf("\"object-type\":\"doggy\"") >= 0);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedContainerSerialization::testIssue329
    public void testIssue329() throws Exception
    {
            ArrayList<Animal> animals = new ArrayList<Animal>();
            animals.add(new Dog("Spot"));
            JavaType rootType = TypeFactory.defaultInstance().constructParametrizedType(Iterator.class, Iterator.class, Animal.class);
            String json = mapper.writerFor(rootType).writeValueAsString(animals.iterator());
            if (json.indexOf("\"object-type\":\"doggy\"") < 0) {
                fail("No polymorphic type retained, should be; JSON = '"+json+"'");
            }
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedContainerSerialization::testIssue508
    public void testIssue508() throws Exception
    {
            List<List<Issue508A>> l = new ArrayList<List<Issue508A>>();
            List<Issue508A> l2 = new ArrayList<Issue508A>();
            l2.add(new Issue508A());
            l.add(l2);
            TypeReference<?> typeRef = new TypeReference<List<List<Issue508A>>>() {};
            String json = mapper.writerFor(typeRef).writeValueAsString(l);

            List<?> output = mapper.readValue(json, typeRef);
            assertEquals(1, output.size());
            Object ob = output.get(0);
            assertTrue(ob instanceof List<?>);
            List<?> list2 = (List<?>) ob;
            assertEquals(1, list2.size());
            ob = list2.get(0);
            assertSame(Issue508A.class, ob.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testSimpleClassAsProperty
    public void testSimpleClassAsProperty() {}

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testTypeAsWrapper
    public void testTypeAsWrapper() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(Animal.class, TypeWithWrapper.class);
        String JSON = "{\".TestTypedDeserialization$Dog\" : "
            +asJSONObjectValueString(m, "name", "Scooby", "boneCount", "6")+" }";
        Animal a = m.readValue(JSON, Animal.class);
        assertTrue(a instanceof Animal);
        assertEquals(Dog.class, a.getClass());
        Dog d = (Dog) a;
        assertEquals("Scooby", d.name);
        assertEquals(6, d.boneCount);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testTypeAsArray
    public void testTypeAsArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(Animal.class, TypeWithArray.class);
        
        String JSON = "[\""+Dog.class.getName()+"\", "
            +asJSONObjectValueString(m, "name", "Martti", "boneCount", "11")+" ]";
        Animal a = m.readValue(JSON, Animal.class);
        assertEquals(Dog.class, a.getClass());
        Dog d = (Dog) a;
        assertEquals("Martti", d.name);
        assertEquals(11, d.boneCount);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testListAsArray
    public void testListAsArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        String JSON = "[\n"
            +asJSONObjectValueString(m, "@classy", Cat.class.getName(), "name", "Hello", "furColor", "white")
            +",\n"
            
            +asJSONObjectValueString(m,
                                     "boneCount", Integer.valueOf(1),
                                     "@classy", Dog.class.getName(),
                                     "name", "Bob"
                                     )
            +",\n"
            +asJSONObjectValueString(m, "@classy", Fish.class.getName())
            +", null\n]";
        
        JavaType expType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Animal.class);
        List<Animal> animals = m.readValue(JSON, expType);
        assertNotNull(animals);
        assertEquals(4, animals.size());
        Cat c = (Cat) animals.get(0);
        assertEquals("Hello", c.name);
        assertEquals("white", c.furColor);
        Dog d = (Dog) animals.get(1);
        assertEquals("Bob", d.name);
        assertEquals(1, d.boneCount);
        Fish f = (Fish) animals.get(2);
        assertNotNull(f);
        assertNull(animals.get(3));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testCagedAnimal
    public void testCagedAnimal() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String jsonCat = asJSONObjectValueString(m, "@classy", Cat.class.getName(), "name", "Nilson", "furColor", "black");
        String JSON = "{\"animal\":"+jsonCat+"}";

        AnimalContainer cont = m.readValue(JSON, AnimalContainer.class);
        assertNotNull(cont);
        Animal a = cont.animal;
        assertNotNull(a);
        Cat c = (Cat) a;
        assertEquals("Nilson", c.name);
        assertEquals("black", c.furColor);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testAbstractEmptyBaseClass
    public void testAbstractEmptyBaseClass() throws Exception
    {
        DummyBase result = new ObjectMapper().readValue(
                "[\""+DummyImpl.class.getName()+"\",{\"x\":3}]", DummyBase.class);
        assertNotNull(result);
        assertEquals(DummyImpl.class, result.getClass());
        assertEquals(3, ((DummyImpl) result).x);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testIssue506WithDate
    public void testIssue506WithDate() throws Exception
    {
        Issue506DateBean input = new Issue506DateBean();
        input.date = new Date(1234L);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(input);

        Issue506DateBean output = mapper.readValue(json, Issue506DateBean.class);
        assertEquals(input.date, output.date);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testIssue506WithNumber
    public void testIssue506WithNumber() throws Exception
    {
        Issue506NumberBean input = new Issue506NumberBean();
        input.number = Long.valueOf(4567L);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(input);

        Issue506NumberBean output = mapper.readValue(json, Issue506NumberBean.class);
        assertEquals(input.number, output.number);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testSimpleClassAsProperty
    public void testSimpleClassAsProperty() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new Cat("Beelzebub", "tabby"));
        assertEquals(3, result.size());
        assertEquals("Beelzebub", result.get("name"));
        assertEquals("tabby", result.get("furColor"));
        
        String classProp = Id.CLASS.getDefaultPropertyName();
        assertEquals(Cat.class.getName(), result.get(classProp));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testTypeAsWrapper
    public void testTypeAsWrapper() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(Animal.class, TypeWithWrapper.class);
        Map<String,Object> result = writeAndMap(m, new Cat("Venla", "black"));
        
        assertEquals(1, result.size());
        
        Map<?,?> cat = (Map<?,?>) result.get(".TestTypedSerialization$Cat");
        assertNotNull(cat);
        assertEquals(2, cat.size());
        assertEquals("Venla", cat.get("name"));
        assertEquals("black", cat.get("furColor"));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testTypeAsArray
    public void testTypeAsArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(Animal.class, TypeWithArray.class);
        
        Map<String,Object> result = writeAndMap(m, new AnimalWrapper(new Dog("Amadeus", 7)));
        
        assertEquals(1, result.size());
        List<?> l = (List<?>) result.get("animal");
        assertNotNull(l);
        assertEquals(2, l.size());
        assertEquals(Dog.class.getName(), l.get(0));
        Map<?,?> doggie = (Map<?,?>) l.get(1);
        assertNotNull(doggie);
        assertEquals(2, doggie.size());
        assertEquals("Amadeus", doggie.get("name"));
        assertEquals(Integer.valueOf(7), doggie.get("boneCount"));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testInArray
    public void testInArray() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        
        m.disableDefaultTyping();
        
        Animal[] animals = new Animal[] { new Cat("Miuku", "white"), new Dog("Murre", 9) };
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("a", animals);
        String json = m.writeValueAsString(map);
        Map<String,Object> result = m.readValue(json, Map.class);
        assertEquals(1, result.size());
        Object ob = result.get("a");
        if (!(ob instanceof List<?>)) {
            
            fail("Did not map to entry with 'a' as List (but as "+ob.getClass().getName()+"): JSON == '"+json+"'");
        }
        List<?> l = (List<?>)ob;
        assertNotNull(l);
        assertEquals(2, l.size());
        Map<?,?> a1 = (Map<?,?>) l.get(0);
        assertEquals(3, a1.size());
        String classProp = Id.CLASS.getDefaultPropertyName();
        assertEquals(Cat.class.getName(), a1.get(classProp));
        Map<?,?> a2 = (Map<?,?>) l.get(1);
        assertEquals(3, a2.size());
        assertEquals(Dog.class.getName(), a2.get(classProp));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testEmptyBean
    public void testEmptyBean() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        assertEquals("{\"@type\":\"empty\"}", m.writeValueAsString(new Empty()));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testTypedMaps
    public void testTypedMaps() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        Map<Long, Collection<Super>> map = new HashMap<Long, Collection<Super>>();
        List<Super> list = new ArrayList<Super>();
        list.add(new A());
        map.put(1L, list);
        String json = mapper.writerFor(new TypeReference<Map<Long, Collection<Super>>>() {}).writeValueAsString(map);
        assertTrue("JSON does not contain '@class': "+json, json.contains("@class"));
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testVisibleWithProperty
    public void testVisibleWithProperty() throws Exception
    {
        String json = MAPPER.writeValueAsString(new PropertyBean());
        
        assertEquals("{\"type\":\"BaseType\",\"a\":3}", json);
        
        PropertyBean result = MAPPER.readValue(json, PropertyBean.class);
        assertEquals("BaseType", result.type);

        
        result = MAPPER.readValue("{\"a\":7, \"type\":\"BaseType\"}", PropertyBean.class);
        assertEquals(7, result.a);
        assertEquals("BaseType", result.type);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testVisibleWithWrapperArray
    public void testVisibleWithWrapperArray() throws Exception
    {
        String json = MAPPER.writeValueAsString(new WrapperArrayBean());
        
        assertEquals("[\"ArrayType\",{\"a\":1}]", json);
        
        WrapperArrayBean result = MAPPER.readValue(json, WrapperArrayBean.class);
        assertEquals("ArrayType", result.type);
        assertEquals(1, result.a);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testVisibleWithWrapperObject
    public void testVisibleWithWrapperObject() throws Exception
    {
        String json = MAPPER.writeValueAsString(new WrapperObjectBean());
        assertEquals("{\"ObjectType\":{\"a\":2}}", json);
        
        WrapperObjectBean result = MAPPER.readValue(json, WrapperObjectBean.class);
        assertEquals("ObjectType", result.type);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testTypeIdFromProperty
    public void testTypeIdFromProperty() throws Exception
    {
        assertEquals("{\"type\":\"SomeType\",\"a\":3}",
                MAPPER.writeValueAsString(new TypeIdFromFieldProperty()));
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testTypeIdFromArray
    public void testTypeIdFromArray() throws Exception
    {
        assertEquals("[\"SomeType\",{\"a\":3}]",
                MAPPER.writeValueAsString(new TypeIdFromFieldArray()));
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testTypeIdFromObject
    public void testTypeIdFromObject() throws Exception
    {
        assertEquals("{\"SomeType\":{\"a\":3}}",
                MAPPER.writeValueAsString(new TypeIdFromMethodObject()));
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testTypeIdFromExternal
    public void testTypeIdFromExternal() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ExternalIdWrapper2());
        
        assertEquals("{\"bean\":{\"a\":2},\"type\":\"SomeType\"}", json);
        
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testIssue263
    public void testIssue263() throws Exception
    {
        
        assertEquals("{\"name\":\"bob\",\"age\":41}", MAPPER.writeValueAsString(new I263Impl()));
        
        
        I263Base result = MAPPER.readValue("{\"age\":19,\"name\":\"bob\"}", I263Base.class);
        assertTrue(result instanceof I263Impl);
        assertEquals(19, ((I263Impl) result).age);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testVisibleTypeId408
    public void testVisibleTypeId408() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ExternalBeanWithId(3));
        ExternalBeanWithId result = MAPPER.readValue(json, ExternalBeanWithId.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        assertEquals(3, result.bean.value);
        assertEquals("vbean", result._type);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testInvalidMultipleTypeIds
    public void testInvalidMultipleTypeIds() throws Exception
    {
        try {
            MAPPER.writeValueAsString(new MultipleIds());
            fail("Should have failed");
        } catch (JsonMappingException e) {
            verifyException(e, "multiple type ids");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testWrapperWithGetter
    public void testWrapperWithGetter() throws Exception
    {
        Dog dog = new Dog("Fluffy", 3);
        String json = MAPPER.writeValueAsString(new ContainerWithGetter<Animal>(dog));
        if (json.indexOf("\"object-type\":\"doggy\"") < 0) {
            fail("polymorphic type not kept, result == "+json+"; should contain 'object-type':'...'");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testWrapperWithField
    public void testWrapperWithField() throws Exception
    {
        Dog dog = new Dog("Fluffy", 3);
        String json = MAPPER.writeValueAsString(new ContainerWithField<Animal>(dog));
        if (json.indexOf("\"object-type\":\"doggy\"") < 0) {
            fail("polymorphic type not kept, result == "+json+"; should contain 'object-type':'...'");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testWrapperWithExplicitType
    public void testWrapperWithExplicitType() throws Exception
    {
        Dog dog = new Dog("Fluffy", 3);
        ContainerWithGetter<Animal> c2 = new ContainerWithGetter<Animal>(dog);
        String json = MAPPER.writerFor(MAPPER.getTypeFactory().constructParametrizedType(ContainerWithGetter.class, ContainerWithGetter.class, Animal.class)).writeValueAsString(c2);
        if (json.indexOf("\"object-type\":\"doggy\"") < 0) {
            fail("polymorphic type not kept, result == "+json+"; should contain 'object-type':'...'");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testJackson387
    public void testJackson387() throws Exception
    {
        ObjectMapper om = new ObjectMapper();
        om.enableDefaultTyping( ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, JsonTypeInfo.As.PROPERTY );
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL );
        om.enable( SerializationFeature.INDENT_OUTPUT);

        MyClass mc = new MyClass();

        MyParam<Integer> moc1 = new MyParam<Integer>(1);
        MyParam<String> moc2 = new MyParam<String>("valueX");

        SomeObject so = new SomeObject();
        so.someValue = "xxxxxx"; 
        MyParam<SomeObject> moc3 = new MyParam<SomeObject>(so);

        List<SomeObject> colist = new ArrayList<SomeObject>();
        colist.add( new SomeObject() );
        colist.add( new SomeObject() );
        colist.add( new SomeObject() );
        MyParam<List<SomeObject>> moc4 = new MyParam<List<SomeObject>>(colist);

        mc.params.add( moc1 );
        mc.params.add( moc2 );
        mc.params.add( moc3 );
        mc.params.add( moc4 );

        String json = om.writeValueAsString( mc );
        
        MyClass mc2 = om.readValue(json, MyClass.class );
        assertNotNull(mc2);
        assertNotNull(mc2.params);
        assertEquals(4, mc2.params.size());
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testJackson430
    public void testJackson430() throws Exception
    {
        ObjectMapper om = new ObjectMapper();

        om.setSerializerFactory( new CustomJsonSerializerFactory() );
        MyClass mc = new MyClass();
        mc.params.add(new MyParam<Integer>(1));

        String str = om.writeValueAsString( mc );

        
        MyClass mc2 = om.readValue( str, MyClass.class );
        assertNotNull(mc2);
        assertNotNull(mc2.params);
        assertEquals(1, mc2.params.size());
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testValueWithMoreGenericParameters
    public void testValueWithMoreGenericParameters() throws Exception
    {
        WrappedContainerWithField wrappedContainerWithField = new WrappedContainerWithField();
        wrappedContainerWithField.animalContainer = new ContainerWithTwoAnimals<Dog,Dog>(new Dog("d1",1), new Dog("d2",2));
        String json = MAPPER.writeValueAsString(wrappedContainerWithField);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.jsontype.TypeRefinementForMapTest::testMapRefinement
    public void testMapRefinement() throws Exception
    {
        String ID1 = "3a6383d4-8123-4c43-8b8d-7cedf3e59404";
        String ID2 = "81c3d978-90c4-4b00-8da1-1c39ffcab02c";
        String json = aposToQuotes(
"{'id':'"+ID1+"','items':[{'id':'"+ID2+"','property':'value'}]}");

        ObjectMapper m = new ObjectMapper();
        Data data = m.readValue(json, Data.class);

        assertEquals(ID1, data.id);
        assertNotNull(data.items);
        assertEquals(1, data.items.size());
        Item value = data.items.get(ID2);
        assertNotNull(value);
        assertEquals("value", value.property);
    }

// com.fasterxml.jackson.databind.jsontype.TypeRefinementForMapTest::testMapKeyRefinement1384
    public void testMapKeyRefinement1384() throws Exception
    {
        final String TEST_INSTANCE_SERIALIZED =
                "{\"mapProperty\":[\"java.util.HashMap\",{\"Compound|Key\":\"Value\"}]}";
        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        TestClass testInstance = mapper.readValue(TEST_INSTANCE_SERIALIZED, TestClass.class);
        assertEquals(1, testInstance.mapProperty.size());
        Object key = testInstance.mapProperty.keySet().iterator().next();
        assertEquals(CompoundKey.class, key.getClass());
        String testInstanceSerialized = mapper.writeValueAsString(testInstance);
        assertEquals(TEST_INSTANCE_SERIALIZED, testInstanceSerialized);
    }

// com.fasterxml.jackson.databind.jsontype.WrapperObjectWithObjectIdTest::testSimple
    public void testSimple() throws Exception
    {
        Company comp = new Company();
        comp.addComputer(new DesktopComputer("computer-1", "Bangkok"));
        comp.addComputer(new DesktopComputer("computer-2", "Pattaya"));
        comp.addComputer(new LaptopComputer("computer-3", "Apple"));

        final ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(comp);

        Company result = mapper.readValue(json, Company.class);
        assertNotNull(result);
        assertNotNull(result.computers);
        assertEquals(3, result.computers.size());
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testSimpleSerialization
    public void testSimpleSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        
        
        
        
        
        
        assertEquals("{\"bean\":{\"value\":11},\"extType\":\"vbean\"}",
                mapper.writeValueAsString(new ExternalBean(11)));
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testImproperExternalIdSerialization
    public void testImproperExternalIdSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"extType\":\"funk\",\"i\":3}",
                mapper.writeValueAsString(new FunkyExternalBean()));
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testExternalTypeIdWithNull
    public void testExternalTypeIdWithNull() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        ExternalBean b;
        b = mapper.readValue(aposToQuotes("{'bean':null,'extType':'vbean'}"),
                ExternalBean.class);
        assertNotNull(b);
        b = mapper.readValue(aposToQuotes("{'extType':'vbean','bean':null}"),
                ExternalBean.class);
        assertNotNull(b);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testSimpleDeserialization
    public void testSimpleDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        ExternalBean result = mapper.readValue("{\"bean\":{\"value\":11},\"extType\":\"vbean\"}", ExternalBean.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        ValueBean vb = (ValueBean) result.bean;
        assertEquals(11, vb.value);

        
        result = mapper.readValue("{\"extType\":\"vbean\", \"bean\":{\"value\":13}}", ExternalBean.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        vb = (ValueBean) result.bean;
        assertEquals(13, vb.value);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testMultipleTypeIdsDeserialization
    public void testMultipleTypeIdsDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        String json = mapper.writeValueAsString(new ExternalBean3(3));
        ExternalBean3 result = mapper.readValue(json, ExternalBean3.class);
        assertNotNull(result);
        assertNotNull(result.value1);
        assertNotNull(result.value2);
        assertNotNull(result.value3);
        assertEquals(3, ((ValueBean)result.value1).value);
        assertEquals(4, ((ValueBean)result.value2).value);
        assertEquals(5, ((ValueBean)result.value3).value);
        assertEquals(3, result.foo);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testExternalTypeWithCreator
    public void testExternalTypeWithCreator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        String json = mapper.writeValueAsString(new ExternalBeanWithCreator(7));
        ExternalBeanWithCreator result = mapper.readValue(json, ExternalBeanWithCreator.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(7, ((ValueBean)result.value).value);
        assertEquals(7, result.foo);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testImproperExternalIdDeserialization
    public void testImproperExternalIdDeserialization() throws Exception
    {
        FunkyExternalBean result = MAPPER.readValue("{\"extType\":\"funk\",\"i\":3}",
                FunkyExternalBean.class);
        assertNotNull(result);
        assertEquals(3, result.i);

        result = MAPPER.readValue("{\"i\":4,\"extType\":\"funk\"}",
                FunkyExternalBean.class);
        assertNotNull(result);
        assertEquals(4, result.i);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testIssue798
    public void testIssue798() throws Exception
    {
        Base base = new Derived1("derived1 prop val", "base prop val");
        BaseContainer baseContainer = new BaseContainer("bc prop val", base);
        String generatedJson = MAPPER.writeValueAsString(baseContainer);
        BaseContainer baseContainer2 = MAPPER.readValue(generatedJson,BaseContainer.class);
        assertEquals("bc prop val", baseContainer.getBaseContainerProperty());

        Base b = baseContainer2.getBase();
        assertNotNull(b);
        if (b.getClass() != Derived1.class) {
            fail("Should have type Derived1, was "+b.getClass().getName());
        }

        Derived1 derived1 = (Derived1) b;
        assertEquals("base prop val", derived1.getBaseProperty());
        assertEquals("derived1 prop val", derived1.getDerived1Property());
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testIssue831
    public void testIssue831() throws Exception
    {
        final String JSON = "{ \"petType\": \"dog\",\n"
                +"\"pet\": { \"name\": \"Pluto\" }\n}";
        House831 result = MAPPER.readValue(JSON, House831.class);
        assertNotNull(result);
        assertNotNull(result.pet);
        assertSame(Dog.class, result.pet.getClass());
        assertEquals("dog", result.petType);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testWithScalar118
    public void testWithScalar118() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new java.util.Date(123L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);

        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof java.util.Date);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testWithNaturalScalar118
    public void testWithNaturalScalar118() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(Integer.valueOf(13));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof Integer);

        
        input = new ExternalTypeWithNonPOJO(Boolean.TRUE);
        json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof Boolean);

        input = new ExternalTypeWithNonPOJO("foobar");
        json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof String);
        assertEquals("foobar", result.value);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testWithAsValue
    public void testWithAsValue() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new AsValueThingy(12345L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        assertEquals("{\"value\":12345,\"type\":\"thingy\"}", json);

        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(AsValueThingy.class, result.value.getClass());
        assertEquals(12345L, ((AsValueThingy) result.value).rawDate);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testExternalTypeWithProp222
    public void testExternalTypeWithProp222() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        Issue222Bean input = new Issue222Bean(13);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value\":{\"x\":13},\"type\":\"foo\"}", json);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testInverseExternalId928
    public void testInverseExternalId928() throws Exception
    {
        final String CLASS = Payload928.class.getName();

        ObjectMapper mapper = new ObjectMapper();

        final String successCase = "{\"payload\":{\"something\":\"test\"},\"class\":\""+CLASS+"\"}";
        Envelope928 envelope1 = mapper.readValue(successCase, Envelope928.class);
        assertNotNull(envelope1);
        assertEquals(Payload928.class, envelope1._payload.getClass());

        
        final String failCase = "{\"class\":\""+CLASS+"\",\"payload\":{\"something\":\"test\"}}";
        Envelope928 envelope2 = mapper.readValue(failCase, Envelope928.class);
        assertNotNull(envelope2);
        assertEquals(Payload928.class, envelope2._payload.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testBigDecimal965
    public void testBigDecimal965() throws Exception
    {

        Wrapper965 w = new Wrapper965();
        w.typeEnum = Type965.BIG_DECIMAL;
        final String NUM_STR = "-10000000000.0000000001";
        w.value = new BigDecimal(NUM_STR);

        String json = MAPPER.writeValueAsString(w);

        
        if (!json.contains(NUM_STR)) {
            fail("JSON content should contain value '"+NUM_STR+"', does not appear to: "+json);
        }
        
        Wrapper965 w2 = MAPPER.readerFor(Wrapper965.class)
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .readValue(json);

        assertEquals(w.typeEnum, w2.typeEnum);
        assertTrue(String.format("Expected %s = %s; got back %s = %s",
            w.value.getClass().getSimpleName(), w.value.toString(), w2.value.getClass().getSimpleName(), w2.value.toString()),
            w.value.equals(w2.value));
    }

// com.fasterxml.jackson.databind.jsontype.ext.JsonValueExtTypeIdTest::testDoubleMetadata
    public void testDoubleMetadata() throws IOException {
        DoubleMetadata doub = new DoubleMetadata();
        String expected = "{\"metadata\":[{\"key\":\"num\",\"value\":1234.25,\"@type\":\"doubleValue\"}]}";
        String json = MAPPER.writeValueAsString(doub);
        assertEquals("Serialized json not equivalent", expected, json);
    }

// com.fasterxml.jackson.databind.jsontype.ext.JsonValueExtTypeIdTest::testDecimalMetadata
    public void testDecimalMetadata() throws IOException{
        DecimalMetadata dec = new DecimalMetadata();
        String expected = "{\"metadata\":[{\"key\":\"num\",\"value\":111.1,\"@type\":\"decimalValue\"}]}";
        String json = MAPPER.writeValueAsString(dec);
        assertEquals("Serialized json not equivalent", expected, json);
    }

// com.fasterxml.jackson.databind.misc.RaceCondition738Test::testRepeatedly
    public void testRepeatedly() throws Exception {
        final int COUNT = 2000;
        for (int i = 0; i < COUNT; i++) {
            runOnce(i, COUNT);
        }
    }

// com.fasterxml.jackson.databind.misc.TestJSONP::testSimpleScalars
    public void testSimpleScalars() throws Exception
    {
        assertEquals("callback(\"abc\")",
                MAPPER.writeValueAsString(new JSONPObject("callback", "abc")));
        assertEquals("calc(123)",
                MAPPER.writeValueAsString(new JSONPObject("calc", Integer.valueOf(123))));
        assertEquals("dummy(null)",
                MAPPER.writeValueAsString(new JSONPObject("dummy", null)));
    }

// com.fasterxml.jackson.databind.misc.TestJSONP::testSimpleBean
    public void testSimpleBean() throws Exception
    {
        assertEquals("xxx({\"a\":\"123\",\"b\":\"456\"})",
                MAPPER.writeValueAsString(new JSONPObject("xxx",
                        new Impl("123", "456"))));
    }

// com.fasterxml.jackson.databind.misc.TestJSONP::testWithType
    public void testWithType() throws Exception
    {
        Object ob = new Impl("abc", "def");
        JavaType type = MAPPER.constructType(Base.class);
        assertEquals("do({\"a\":\"abc\"})",
                MAPPER.writeValueAsString(new JSONPObject("do", ob, type)));
    }

// com.fasterxml.jackson.databind.misc.TestJSONP::testGeneralWrapping
    public void testGeneralWrapping() throws Exception
    {
        JSONWrappedObject input = new JSONWrappedObject("", "\n// the end",
                Arrays.asList());
        assertEquals("[]\n// the end", MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.mixins.MixinsWithBundlesTest::testMixinWithBundles
    public void testMixinWithBundles() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().addMixIn(Foo.class, FooMixin.class);
        String result = mapper.writeValueAsString(new Foo("result"));
        assertEquals("{\"bar\":\"result\"}", result);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinInheritance::testMixinFieldInheritance
    public void testMixinFieldInheritance() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Beano.class, BeanoMixinSub.class);
        Map<String,Object> result;
        result = writeAndMap(mapper, new Beano());
        assertEquals(2, result.size());
        if (!result.containsKey("id")
                || !result.containsKey("name")) {
            fail("Should have both 'id' and 'name', but content = "+result);
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinInheritance::testMixinMethodInheritance
    public void testMixinMethodInheritance() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Beano2.class, BeanoMixinSub2.class);
        Map<String,Object> result;
        result = writeAndMap(mapper, new Beano2());
        assertEquals(2, result.size());
        assertTrue(result.containsKey("id"));
        assertTrue(result.containsKey("name"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForClass::testClassMixInsTopLevel
    public void testClassMixInsTopLevel() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;

        
        result = writeAndMap(mapper, new LeafClass("abc"));
        assertEquals(1, result.size());
        assertEquals("abc", result.get("a"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(LeafClass.class, MixIn.class);
        result = writeAndMap(mapper, new LeafClass("abc"));
        assertEquals(2, result.size());
        assertEquals("abc", result.get("a"));
        assertEquals("c", result.get("c"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(BaseClass.class, MixIn.class);
        result = writeAndMap(mapper, new LeafClass("abc"));
        assertEquals(1, result.size());
        assertEquals("abc", result.get("a"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForClass::testClassMixInsMidLevel
    public void testClassMixInsMidLevel() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        LeafClass bean = new LeafClass("xyz");
        bean._c = "c2";

        
        result = writeAndMap(mapper, bean);
        assertEquals(2, result.size());
        assertEquals("xyz", result.get("a"));
        assertEquals("c2", result.get("c"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(BaseClass.class, MixInAutoDetect.class);
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("c2", result.get("c"));

        
        ObjectMapper mapper2 = new ObjectMapper();
        result = writeAndMap(mapper2, bean);
        assertEquals(2, result.size());
        ObjectMapper mapper3 = mapper2.copy();
        mapper3.addMixIn(BaseClass.class, MixInAutoDetect.class);
        result = writeAndMap(mapper3, bean);
        assertEquals(1, result.size());
        assertEquals("c2", result.get("c"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForFields::testFieldMixInsTopLevel
    public void testFieldMixInsTopLevel() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        BaseClass bean = new BaseClass("1", "2");

        
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("1", result.get("a"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(BaseClass.class, MixIn.class);
        result = writeAndMap(mapper, bean);
        assertEquals(2, result.size());
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("banana"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForFields::testMultipleFieldMixIns
    public void testMultipleFieldMixIns() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        
        HashMap<Class<?>,Class<?>> mixins = new HashMap<Class<?>,Class<?>>();
        mixins.put(SubClass.class, MixIn.class);
        mixins.put(BaseClass.class, MixIn2.class);
        mapper.setMixIns(mixins);

        Map<String,Object> result;
        result = writeAndMap(mapper, new SubClass("1", "2"));
        assertEquals(1, result.size());
        
        assertEquals("2", result.get("banana"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForMethods::testLeafMixin
    public void testLeafMixin() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        BaseClass bean = new BaseClass("a1", "b2");

        
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("b2", result.get("b"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(BaseClass.class, MixIn.class);
        result = writeAndMap(mapper, bean);
        assertEquals(2, result.size());
        assertEquals("b2", result.get("b2"));
        assertEquals("a1", result.get("a"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForMethods::testIntermediateMixin
    public void testIntermediateMixin() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        LeafClass bean = new LeafClass("XXX", "b2");

        mapper.addMixIn(BaseClass.class, MixIn.class);
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("XXX", result.get("a"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForMethods::testIntermediateMixin2
    public void testIntermediateMixin2() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(EmptyBean.class, MixInForSimple.class);
        Map<String,Object> result = writeAndMap(mapper, new SimpleBean());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(42), result.get("x"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForMethods::testObjectMixin
    public void testObjectMixin() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Object.class, ObjectMixIn.class);

        
        Map<String,Object> result = writeAndMap(mapper, new BaseClass("a", "b"));

        assertEquals(2, result.size());
        assertEquals("b", result.get("b"));
        Object ob = result.get("hashCode");
        assertNotNull(ob);
        assertEquals(Integer.class, ob.getClass());

        
        
        result = writeAndMap(mapper, new BaseClass("a", "b"));
        assertEquals(2, result.size());
        ob = result.get("hashCode");
        assertNotNull(ob);
        assertEquals(Integer.class, ob.getClass());
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForMethods::testCustomResolver
    public void testCustomResolver() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setMixInResolver(new ClassIntrospector.MixInResolver() {
            @Override
            public Class<?> findMixInClassFor(Class<?> target) {
                if (target == BaseClass.class) {
                    return ObjectMixIn.class;
                }
                return null;
            }

            @Override
            public MixInResolver copy() {
                return this;
            }
        });
        Map<String,Object> result = writeAndMap(mapper, new BaseClass("c", "d"));
        assertEquals(2, result.size());
        assertNotNull(result.get("hashCode"));
        assertTrue(result.containsKey("b"));
        assertFalse(result.containsKey("a"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerWithViews::testDataBindingUsage
    public void testDataBindingUsage( ) throws Exception
    {
      ObjectMapper objectMapper = createObjectMapper();
      ObjectWriter objectWriter = objectMapper.writerWithView(Views.View.class).withDefaultPrettyPrinter();
      Object object = new ComplexTestData();
      String json = objectWriter.writeValueAsString(object);
      assertTrue( json.indexOf( "nameHidden" ) == -1 );
      assertTrue( json.indexOf( "\"name\" : \"shown\"" ) > 0 );
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerWithViews::testIssue560
    public void testIssue560() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        A a = new A("myname", 29, "mysurname");

        
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, Boolean.FALSE);
        mapper.addMixIn(A.class, AMixInAnnotation.class);
        String json = mapper.writerWithView(AView.class).writeValueAsString(a);

        assertTrue(json.indexOf("\"name\"") > 0);
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testWithoutModule
    public void testWithoutModule()
    {
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            mapper.writeValueAsString(new CustomBean("foo", 3));
            fail("Should have caused an exception");
        } catch (IOException e) {
            verifyException(e, "No serializer found");
        }

        
        try {
            mapper.readValue("{\"str\":\"ab\",\"num\":2}", CustomBean.class);
            fail("Should have caused an exception");
        } catch (IOException e) {
            verifyException(e, "No suitable constructor found");
        }
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testSimpleBeanSerializer
    public void testSimpleBeanSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addSerializer(new CustomBeanSerializer());
        mapper.registerModule(mod);
        assertEquals(quote("abcde|5"), mapper.writeValueAsString(new CustomBean("abcde", 5)));
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testSimpleEnumSerializer
    public void testSimpleEnumSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addSerializer(new SimpleEnumSerializer());
        mapper.registerModule(mod);
        assertEquals(quote("b"), mapper.writeValueAsString(SimpleEnum.B));
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testSimpleInterfaceSerializer
    public void testSimpleInterfaceSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addSerializer(new BaseSerializer());
        mapper.registerModule(mod);
        assertEquals(quote("Base:1"), mapper.writeValueAsString(new Impl1()));
        assertEquals(quote("Base:2"), mapper.writeValueAsString(new Impl2()));
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testSimpleBeanDeserializer
    public void testSimpleBeanDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addDeserializer(CustomBean.class, new CustomBeanDeserializer());
        mapper.registerModule(mod);
        CustomBean bean = mapper.readValue(quote("xyz|3"), CustomBean.class);
        assertEquals("xyz", bean.str);
        assertEquals(3, bean.num);
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testSimpleEnumDeserializer
    public void testSimpleEnumDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addDeserializer(SimpleEnum.class, new SimpleEnumDeserializer());
        mapper.registerModule(mod);
        SimpleEnum result = mapper.readValue(quote("a"), SimpleEnum.class);
        assertSame(SimpleEnum.A, result);
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testMultipleModules
    public void testMultipleModules() throws Exception
    {
        MySimpleModule mod1 = new MySimpleModule("test1", Version.unknownVersion());
        SimpleModule mod2 = new SimpleModule("test2", Version.unknownVersion());
        mod1.addSerializer(SimpleEnum.class, new SimpleEnumSerializer());
        mod1.addDeserializer(CustomBean.class, new CustomBeanDeserializer());
        mod2.addDeserializer(SimpleEnum.class, new SimpleEnumDeserializer());
        mod2.addSerializer(CustomBean.class, new CustomBeanSerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(mod1);
        mapper.registerModule(mod2);
        assertEquals(quote("b"), mapper.writeValueAsString(SimpleEnum.B));
        SimpleEnum result = mapper.readValue(quote("a"), SimpleEnum.class);
        assertSame(SimpleEnum.A, result);

        
        mapper = new ObjectMapper();
        mapper.registerModule(mod2);
        mapper.registerModule(mod1);
        assertEquals(quote("b"), mapper.writeValueAsString(SimpleEnum.B));
        result = mapper.readValue(quote("a"), SimpleEnum.class);
        assertSame(SimpleEnum.A, result);
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testMixIns
    public void testMixIns() throws Exception
    {
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.setMixInAnnotation(MixableBean.class, MixInForOrder.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        Map<String,Object> props = this.writeAndMap(mapper, new MixableBean());
        assertEquals(3, props.size());
        assertEquals(Integer.valueOf(3), props.get("c"));
        assertEquals(Integer.valueOf(1), props.get("a"));
        assertEquals(Integer.valueOf(2), props.get("b"));
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testAccessToMapper
    public void testAccessToMapper() throws Exception
    {
        ContextVerifierModule module = new ContextVerifierModule();        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
    }

// com.fasterxml.jackson.databind.module.TestSimpleModule::testMixIns626
    public void testMixIns626() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.registerModule(new TestModule626(Object.class, String.class));
        Class<?> found = mapper.findMixInClassFor(Object.class);
        assertEquals(String.class, found);
    }

// com.fasterxml.jackson.databind.module.TestTypeModifierNameResolution::testTypeModiferNameResolution
	public void testTypeModiferNameResolution() throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new CustomTypeModifier()));
		mapper.addMixIn(MyType.class, Mixin.class);

		MyType obj = new MyTypeImpl();
		obj.setData("something");

		String s = mapper.writer().writeValueAsString(obj);
		assertTrue(s.startsWith("{\"TestTypeModifierNameResolution$MyType\":"));
	}

// com.fasterxml.jackson.databind.node.NotANumberConversionTest::testBigDecimalWithNaN
    public void testBigDecimalWithNaN() throws Exception
    {
        JsonNode tree = m.valueToTree(new DoubleWrapper(Double.NaN));
        assertNotNull(tree);
        String json = m.writeValueAsString(tree);
        assertNotNull(json);

        tree = m.valueToTree(new DoubleWrapper(Double.NEGATIVE_INFINITY));
        assertNotNull(tree);
        json = m.writeValueAsString(tree);
        assertNotNull(json);

        tree = m.valueToTree(new DoubleWrapper(Double.POSITIVE_INFINITY));
        assertNotNull(tree);
        json = m.writeValueAsString(tree);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.node.NotANumberConversionTest::testBigDecimalWithoutNaN
    public void testBigDecimalWithoutNaN() throws Exception
    {
        BigDecimal input = new BigDecimal(Double.MIN_VALUE).divide(new BigDecimal(10L));
        JsonNode tree = m.readTree(input.toString());
        assertTrue(tree.isBigDecimal());
        BigDecimal output = tree.decimalValue();
        assertEquals(input, output);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testAsInt
    public void testAsInt() throws Exception
    {
        assertEquals(9, IntNode.valueOf(9).asInt());
        assertEquals(7, LongNode.valueOf(7L).asInt());
        assertEquals(13, new TextNode("13").asInt());
        assertEquals(0, new TextNode("foobar").asInt());
        assertEquals(27, new TextNode("foobar").asInt(27));
        assertEquals(1, BooleanNode.TRUE.asInt());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testAsBoolean
    public void testAsBoolean() throws Exception
    {
        assertEquals(false, BooleanNode.FALSE.asBoolean());
        assertEquals(true, BooleanNode.TRUE.asBoolean());
        assertEquals(false, IntNode.valueOf(0).asBoolean());
        assertEquals(true, IntNode.valueOf(1).asBoolean());
        assertEquals(false, LongNode.valueOf(0).asBoolean());
        assertEquals(true, LongNode.valueOf(-34L).asBoolean());
        assertEquals(true, new TextNode("true").asBoolean());
        assertEquals(false, new TextNode("false").asBoolean());
        assertEquals(false, new TextNode("barf").asBoolean());
        assertEquals(true, new TextNode("barf").asBoolean(true));

        assertEquals(true, new POJONode(Boolean.TRUE).asBoolean());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testTreeToValue
    public void testTreeToValue() throws Exception
    {
        String JSON = "{\"leaf\":{\"value\":13}}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Leaf.class, LeafMixIn.class);
        JsonNode root = mapper.readTree(JSON);
        
        Root r1 = mapper.treeToValue(root, Root.class);
        assertNotNull(r1);
        assertEquals(13, r1.leaf.value);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testTreeToValueWithPOJO
    public void testTreeToValueWithPOJO() throws Exception
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date(0));
        ValueNode pojoNode = MAPPER.getNodeFactory().pojoNode(c);        
        Calendar result = MAPPER.treeToValue(pojoNode, Calendar.class);
        assertNotNull(result);
        assertEquals(result.getTimeInMillis(), c.getTimeInMillis());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBase64Text
    public void testBase64Text() throws Exception
    {
        
        
        final int[] LENS = { 1, 2, 3, 4, 7, 9, 32, 33, 34, 35 };
        final Base64Variant[] VARIANTS = {
                Base64Variants.MIME,
                Base64Variants.MIME_NO_LINEFEEDS,
                Base64Variants.MODIFIED_FOR_URL,
                Base64Variants.PEM
        };

        for (int len : LENS) {
            byte[] input = new byte[len];
            for (int i = 0; i < input.length; ++i) {
                input[i] = (byte) i;
            }
            for (Base64Variant variant : VARIANTS) {
                TextNode n = new TextNode(variant.encode(input));
                byte[] data = null;
                try {
                    data = n.getBinaryValue(variant);
                } catch (Exception e) {
                    throw new IOException("Failed (variant "+variant+", data length "+len+"): "+e.getMessage());
                }
                assertNotNull(data);
                assertArrayEquals(data, input);
            }
        }
    }

// com.fasterxml.jackson.databind.node.TestConversions::testIssue709
    public void testIssue709() throws Exception
    {
        byte[] inputData = new byte[] { 1, 2, 3 };
        ObjectNode node = MAPPER.createObjectNode();
        node.put("data", inputData);
        Issue709Bean result = MAPPER.treeToValue(node, Issue709Bean.class);
        String json = MAPPER.writeValueAsString(node);
        Issue709Bean resultFromString = MAPPER.readValue(json, Issue709Bean.class);
        Issue709Bean resultFromConvert = MAPPER.convertValue(node, Issue709Bean.class);
        
        
        Assert.assertArrayEquals(inputData, resultFromString.data);
        Assert.assertArrayEquals(inputData, resultFromConvert.data);
        Assert.assertArrayEquals(inputData, result.data);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testEmbeddedByteArray
    public void testEmbeddedByteArray() throws Exception
    {
        TokenBuffer buf = new TokenBuffer(MAPPER, false);
        buf.writeObject(new byte[3]);
        JsonNode node = MAPPER.readTree(buf.asParser());
        buf.close();
        assertTrue(node.isBinary());
        byte[] data = node.binaryValue();
        assertNotNull(data);
        assertEquals(3, data.length);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBigDecimalAsPlainStringTreeConversion
    public void testBigDecimalAsPlainStringTreeConversion() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.00000000";
        map.put("pi", new BigDecimal(PI_STR));
        JsonNode tree = mapper.valueToTree(map);
        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertTrue(tree.has("pi"));
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBeanToTree
    public void testBeanToTree() throws Exception
    {
        final CustomSerializedPojo pojo = new CustomSerializedPojo();
        pojo.setFoo("bar");
        final JsonNode node = MAPPER.valueToTree(pojo);
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testConversionOfPojos
    public void testConversionOfPojos() throws Exception
    {
        final Issue467Bean input = new Issue467Bean(13);
        final String EXP = "{\"x\":13}";
        
        
        String json = MAPPER.writeValueAsString(input);
        assertEquals(EXP, json);

        
        JsonNode tree = MAPPER.valueToTree(input);
        assertTrue("Expected Object, got "+tree.getNodeType(), tree.isObject());
        assertEquals(EXP, MAPPER.writeValueAsString(tree));
    }

// com.fasterxml.jackson.databind.node.TestConversions::testConversionOfTrees
    public void testConversionOfTrees() throws Exception
    {
        final Issue467Tree input = new Issue467Tree();
        final String EXP = "true";

        
        String json = MAPPER.writeValueAsString(input);
        assertEquals(EXP, json);

        
        JsonNode tree = MAPPER.valueToTree(input);
        assertTrue("Expected Object, got "+tree.getNodeType(), tree.isBoolean());
        assertEquals(EXP, MAPPER.writeValueAsString(tree));
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testSimpleObject
    public void testSimpleObject() throws Exception
    {
        String JSON = "{ \"key\" : 1, \"b\" : \"x\" }";
        JsonNode root = MAPPER.readTree(JSON);

        
        assertFalse(root.isValueNode());
        assertTrue(root.isContainerNode());
        assertFalse(root.isArray());
        assertTrue(root.isObject());
        assertEquals(2, root.size());

        
        Iterator<JsonNode> it = root.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        JsonNode n = it.next();
        assertNotNull(n);
        assertEquals(IntNode.valueOf(1), n);

        assertTrue(it.hasNext());
        n = it.next();
        assertNotNull(n);
        assertEquals(TextNode.valueOf("x"), n);

        assertFalse(it.hasNext());

        
        ObjectNode obNode = (ObjectNode) root;
        Iterator<Map.Entry<String,JsonNode>> fit = obNode.fields();
        
        assertTrue(fit.hasNext());
        Map.Entry<String,JsonNode> en = fit.next();
        assertEquals("key", en.getKey());
        assertEquals(IntNode.valueOf(1), en.getValue());

        assertTrue(fit.hasNext());
        en = fit.next();
        assertEquals("b", en.getKey());
        assertEquals(TextNode.valueOf("x"), en.getValue());

        
        fit.remove();
        assertEquals(1, obNode.size());
        assertEquals(IntNode.valueOf(1), root.get("key"));
        assertNull(root.get("b"));
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testEmptyNodeAsValue
    public void testEmptyNodeAsValue() throws Exception
    {
        Data w = MAPPER.readValue("{}", Data.class);
        assertNotNull(w);
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testBasics
    public void testBasics()
    {
        ObjectNode n = new ObjectNode(JsonNodeFactory.instance);
        assertStandardEquals(n);

        assertFalse(n.elements().hasNext());
        assertFalse(n.fields().hasNext());
        assertFalse(n.fieldNames().hasNext());
        assertNull(n.get("a"));
        assertTrue(n.path("a").isMissingNode());

        TextNode text = TextNode.valueOf("x");
        assertSame(n, n.set("a", text));
        
        assertEquals(1, n.size());
        assertTrue(n.elements().hasNext());
        assertTrue(n.fields().hasNext());
        assertTrue(n.fieldNames().hasNext());
        assertSame(text, n.get("a"));
        assertSame(text, n.path("a"));
        assertNull(n.get("b"));
        assertNull(n.get(0)); 

        assertFalse(n.has(0));
        assertFalse(n.hasNonNull(0));
        assertTrue(n.has("a"));
        assertTrue(n.hasNonNull("a"));
        assertFalse(n.has("b"));
        assertFalse(n.hasNonNull("b"));

        ObjectNode n2 = new ObjectNode(JsonNodeFactory.instance);
        n2.put("b", 13);
        assertFalse(n.equals(n2));
        n.setAll(n2);
        
        assertEquals(2, n.size());
        n.set("null", (JsonNode)null);
        assertEquals(3, n.size());
        
        assertTrue(n.has("null"));
        assertFalse(n.hasNonNull("null"));
        
        n.put("null", "notReallNull");
        assertEquals(3, n.size());
        assertNotNull(n.remove("null"));
        assertEquals(2, n.size());

        Map<String,JsonNode> nodes = new HashMap<String,JsonNode>();
        nodes.put("d", text);
        n.setAll(nodes);
        assertEquals(3, n.size());

        n.removeAll();
        assertEquals(0, n.size());
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testNullChecking
    public void testNullChecking()
    {
        ObjectNode o1 = JsonNodeFactory.instance.objectNode();
        ObjectNode o2 = JsonNodeFactory.instance.objectNode();
        
        o1.setAll(o2);
        assertEquals(0, o1.size());
        assertEquals(0, o2.size());

        
        o1.set("x", null);
        JsonNode n = o1.get("x");
        assertNotNull(n);
        assertSame(n, NullNode.instance);

        o1.put("str", (String) null);
        n = o1.get("str");
        assertNotNull(n);
        assertSame(n, NullNode.instance);

        o1.put("d", (BigDecimal) null);
        n = o1.get("d");
        assertNotNull(n);
        assertSame(n, NullNode.instance);
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testNullChecking2
    public void testNullChecking2()
    {
        ObjectNode src = MAPPER.createObjectNode();
        ObjectNode dest = MAPPER.createObjectNode();
        src.put("a", "b");
        dest.setAll(src);
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testRemove
    public void testRemove()
    {
        ObjectNode ob = MAPPER.createObjectNode();
        ob.put("a", "a");
        ob.put("b", "b");
        ob.put("c", "c");
        assertEquals(3, ob.size());
        assertSame(ob, ob.without(Arrays.asList("a", "c")));
        assertEquals(1, ob.size());
        assertEquals("b", ob.get("b").textValue());
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testRetain
    public void testRetain()
    {
        ObjectNode ob = MAPPER.createObjectNode();
        ob.put("a", "a");
        ob.put("b", "b");
        ob.put("c", "c");
        assertEquals(3, ob.size());
        assertSame(ob, ob.retain("a", "c"));
        assertEquals(2, ob.size());
        assertEquals("a", ob.get("a").textValue());
        assertNull(ob.get("b"));
        assertEquals("c", ob.get("c").textValue());
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testValidWith
    public void testValidWith() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        assertEquals("{}", MAPPER.writeValueAsString(root));
        JsonNode child = root.with("prop");
        assertTrue(child instanceof ObjectNode);
        assertEquals("{\"prop\":{}}", MAPPER.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testValidWithArray
    public void testValidWithArray() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        assertEquals("{}", MAPPER.writeValueAsString(root));
        JsonNode child = root.withArray("arr");
        assertTrue(child instanceof ArrayNode);
        assertEquals("{\"arr\":[]}", MAPPER.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testInvalidWith
    public void testInvalidWith() throws Exception
    {
        JsonNode root = MAPPER.createArrayNode();
        try { 
            root.with("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "not of type ObjectNode");
        }
        
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.put("prop", 13);
        try { 
            root2.with("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "has value that is not");
        }
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testInvalidWithArray
    public void testInvalidWithArray() throws Exception
    {
        JsonNode root = MAPPER.createArrayNode();
        try { 
            root.withArray("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "not of type ObjectNode");
        }
        
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.put("prop", 13);
        try { 
            root2.withArray("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "has value that is not");
        }
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testSetAll
    public void testSetAll() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        assertEquals(0, root.size());
        HashMap<String,JsonNode> map = new HashMap<String,JsonNode>();
        map.put("a", root.numberNode(1));
        root.setAll(map);
        assertEquals(1, root.size());
        assertTrue(root.has("a"));
        assertFalse(root.has("b"));

        map.put("b", root.numberNode(2));
        root.setAll(map);
        assertEquals(2, root.size());
        assertTrue(root.has("a"));
        assertTrue(root.has("b"));
        assertEquals(2, root.path("b").intValue());

        
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.setAll(root);
        assertEquals(2, root.size());
        assertEquals(2, root2.size());

        root2.setAll(root);
        assertEquals(2, root.size());
        assertEquals(2, root2.size());

        ObjectNode root3 = MAPPER.createObjectNode();
        root3.put("a", 2);
        root3.put("c", 3);
        assertEquals(2, root3.path("a").intValue());
        root3.setAll(root2);
        assertEquals(3, root3.size());
        assertEquals(1, root3.path("a").intValue());
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testFailOnDupKeys
    public void testFailOnDupKeys() throws Exception
    {
        final String DUP_JSON = "{ \"a\":1, \"a\":2 }";
        
        
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY));
        ObjectNode root = (ObjectNode) mapper.readTree(DUP_JSON);
        assertEquals(2, root.path("a").asInt());
        
        
        try {
            mapper.reader(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY).readTree(DUP_JSON);
            fail("Should have thrown exception!");
        } catch (JsonMappingException e) {
            verifyException(e, "duplicate field 'a'");
        }
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testEqualityWrtOrder
    public void testEqualityWrtOrder() throws Exception
    {
        ObjectNode ob1 = MAPPER.createObjectNode();
        ObjectNode ob2 = MAPPER.createObjectNode();

        
        
        ob1.put("a", 1);
        ob1.put("b", 2);
        ob1.put("c", 3);

        ob2.put("b", 2);
        ob2.put("c", 3);
        ob2.put("a", 1);

        assertTrue(ob1.equals(ob2));
        assertTrue(ob2.equals(ob1));
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testSimplePath
    public void testSimplePath() throws Exception
    {
        JsonNode root = MAPPER.readTree("{ \"results\" : { \"a\" : 3 } }");
        assertTrue(root.isObject());
        JsonNode rnode = root.path("results");
        assertNotNull(rnode);
        assertTrue(rnode.isObject());
        assertEquals(3, rnode.path("a").intValue());
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testNonEmptySerialization
    public void testNonEmptySerialization() throws Exception
    {
        ObNodeWrapper w = new ObNodeWrapper(MAPPER.createObjectNode()
                .put("a", 3));
        assertEquals("{\"node\":{\"a\":3}}", MAPPER.writeValueAsString(w));
        w = new ObNodeWrapper(MAPPER.createObjectNode());
        assertEquals("{}", MAPPER.writeValueAsString(w));
    }

// com.fasterxml.jackson.databind.node.TestObjectNode::testIssue941
    public void testIssue941() throws Exception
    {
        ObjectNode object = MAPPER.createObjectNode();

        String json = MAPPER.writeValueAsString(object);

        ObjectNode de1 = MAPPER.readValue(json, ObjectNode.class);  

        assertNotNull(de1);

        MyValue de2 = MAPPER.readValue(json, MyValue.class);  

        assertNotNull(de2);
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testValueAsStringWithoutDefaultTyping
    public void testValueAsStringWithoutDefaultTyping() throws Exception {

        Foo foo = new Foo("baz");
        String json = MAPPER.writeValueAsString(foo);

        JsonNode jsonNode = MAPPER.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testValueAsStringWithDefaultTyping
    public void testValueAsStringWithDefaultTyping() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Foo foo = new Foo("baz");
        String json = mapper.writeValueAsString(foo);

        JsonNode jsonNode = mapper.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testReadTreeWithDefaultTyping
    public void testReadTreeWithDefaultTyping() throws Exception
    {
        final String CLASS = Foo.class.getName();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        String json = "{\"@class\":\""+CLASS+"\",\"bar\":\"baz\"}";
        JsonNode jsonNode = mapper.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), "baz");
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testValueToTreeWithoutDefaultTyping
    public void testValueToTreeWithoutDefaultTyping() throws Exception {

        Foo foo = new Foo("baz");
        JsonNode jsonNode = MAPPER.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testValueToTreeWithDefaultTyping
    public void testValueToTreeWithDefaultTyping() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Foo foo = new Foo("baz");
        JsonNode jsonNode = mapper.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testIssue353
    public void testIssue353() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");

         SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null, "TEST", "TEST"));
         testModule.addDeserializer(SavedCookie.class, new SavedCookieDeserializer());
         mapper.registerModule(testModule);

         SavedCookie savedCookie = new SavedCookie("key", "v");
         String json = mapper.writeValueAsString(savedCookie);
         SavedCookie out = mapper.readerFor(SavedCookie.class).readValue(json);

         assertEquals("key", out.name);
         assertEquals("v", out.value);
    }

// com.fasterxml.jackson.databind.objectid.AlwaysAsReferenceFirstTest::testIssue1255
    public void testIssue1255() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Foo mo = new Foo();
        mo.bar1 = new Bar();
        mo.bar2 = mo.bar1;

        String json = mapper.writeValueAsString(mo);

        Foo result = mapper.readValue(json, Foo.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.objectid.JSOGDeserialize622Test::testStructJSOGRef
    public void testStructJSOGRef() throws Exception
    {
        IdentifiableExampleJSOG result = MAPPER.readValue(EXP_EXAMPLE_JSOG,
                IdentifiableExampleJSOG.class);
        assertEquals(66, result.foo);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.objectid.JSOGDeserialize622Test::testPolymorphicRoundTrip
    public void testPolymorphicRoundTrip() throws Exception
    {
        JSOGWrapper w = new JSOGWrapper(15);
        
        IdentifiableExampleJSOG ex = new IdentifiableExampleJSOG(123);
        ex.next = ex;
        w.jsog = ex;

        String json = MAPPER.writeValueAsString(w);

        JSOGWrapper out = MAPPER.readValue(json, JSOGWrapper.class);
        assertNotNull(out);
        assertEquals(15, out.value);
        assertTrue(out.jsog instanceof IdentifiableExampleJSOG);
        IdentifiableExampleJSOG jsog = (IdentifiableExampleJSOG) out.jsog;
        assertEquals(123, jsog.foo);
        assertSame(jsog, jsog.next);
    }

// com.fasterxml.jackson.databind.objectid.JSOGDeserialize622Test::testAlterativePolymorphicRoundTrip669
    public void testAlterativePolymorphicRoundTrip669() throws Exception
    {
        Outer outer = new Outer();
        outer.foo = "foo";
        outer.inner1 = outer.inner2 = new SubInner("bar", "extra");

        String jsog = MAPPER.writeValueAsString(outer);
        
        Outer back = MAPPER.readValue(jsog, Outer.class);

        assertSame(back.inner1, back.inner2);
    }

// com.fasterxml.jackson.databind.objectid.ObjectId825Test::testDeserialize
    public void testDeserialize() throws Exception {
        TestA a = new TestA();
        a.oidString = "oidA";

        TestC c = new TestC();
        c.oidString = "oidC";

        a.testAbst = c;

        TestD d = new TestD();
        d.oidString = "oidD";

        c.d = d;
        a.d = d;

        String json = DEF_TYPING_MAPPER.writeValueAsString(a);

        TestA testADeserialized = DEF_TYPING_MAPPER.readValue(json, TestA.class);

        assertNotNull(testADeserialized);
        assertNotNull(testADeserialized.d);
        assertEquals("oidD", testADeserialized.d.oidString);
    }

// com.fasterxml.jackson.databind.objectid.ObjectWithCreator1261Test::testObjectIds1261
    public void testObjectIds1261() throws Exception
    {
         ObjectMapper mapper = new ObjectMapper();
         mapper.enable(SerializationFeature.INDENT_OUTPUT);
         mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

         Answer initialAnswer = createInitialAnswer();
         String initialAnswerString = mapper.writeValueAsString(initialAnswer);

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

// com.fasterxml.jackson.databind.objectid.ReferentialWithObjectIdTest::testAtomicWithObjectId
    public void testAtomicWithObjectId() throws Exception
    {
        Employee first = new Employee();
        first.id = 1;
        first.name = "Alice";

        Employee second = new Employee();
        second.id = 2;
        second.name = "Bob";

        first.next(second);
        second.next(first);

        EmployeeList input = new EmployeeList();
        input.first = new AtomicReference<Employee>(first);

        String json = MAPPER.writeValueAsString(input);

        
 
        EmployeeList result = MAPPER.readValue(json, EmployeeList.class);
        Employee firstB = result.first.get();
        assertNotNull(firstB);
        assertEquals("Alice", firstB.name);
        Employee secondB = firstB.next.get();
        assertNotNull(secondB);
        assertEquals("Bob", secondB.name);
        assertNotNull(secondB.next.get());
        assertSame(firstB, secondB.next.get());
    }

// com.fasterxml.jackson.databind.objectid.TestAbstractWithObjectId::testIssue877
    public void testIssue877() throws Exception
    {
        
        BaseInterfaceImpl one = new BaseInterfaceImpl();
        BaseInterfaceImpl two = new BaseInterfaceImpl();

        
        one.addInstance(two);
        two.addInstance(one);

        
        ListWrapper<BaseInterfaceImpl> myList = new ListWrapper<BaseInterfaceImpl>();
        myList.add(one);
        myList.add(two);

        
        ObjectMapper om = new ObjectMapper();
        om.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");

        
        String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(myList);
        ListWrapper<BaseInterfaceImpl> result;
        
        result = om.readValue(json, new TypeReference<ListWrapper<BaseInterfaceImpl>>() { });

        assertNotNull(result);
        
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.objectid.TestObjectId::testColumnMetadata
    public void testColumnMetadata() throws Exception
    {
        ColumnMetadata col = new ColumnMetadata("Billy", "employee", "comment");
        Wrapper w = new Wrapper();
        w.a = col;
        w.b = col;
        String json = MAPPER.writeValueAsString(w);
        
        Wrapper deserialized = MAPPER.readValue(json, Wrapper.class);
        assertNotNull(deserialized);
        assertNotNull(deserialized.a);
        assertNotNull(deserialized.b);
        
        assertEquals("Billy", deserialized.a.getName());
        assertEquals("employee", deserialized.a.getType());
        assertEquals("comment", deserialized.a.getComment());

        assertSame(deserialized.a, deserialized.b);
    }
