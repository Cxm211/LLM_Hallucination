// buggy code
    public void resolve(DeserializationContext ctxt)
        throws JsonMappingException
    {
        ExternalTypeHandler.Builder extTypes = null;
        // if ValueInstantiator can use "creator" approach, need to resolve it here...
        SettableBeanProperty[] creatorProps;

        if (_valueInstantiator.canCreateFromObjectWith()) {
            creatorProps = _valueInstantiator.getFromObjectArguments(ctxt.getConfig());

            // 21-Jun-2015, tatu: This resolution was duplicated later on and seems like
            //    it really should be only done at a later point. So commented out in 2.8.
            //   However, just in case there was a reason for it, leaving commented out
            //   here instead of immediately removing.

            /*
            // also: need to try to resolve 'external' type ids...
            for (SettableBeanProperty prop : creatorProps) {
                if (prop.hasValueTypeDeserializer()) {
                    TypeDeserializer typeDeser = prop.getValueTypeDeserializer();
                    if (typeDeser.getTypeInclusion() == JsonTypeInfo.As.EXTERNAL_PROPERTY) {
                        if (extTypes == null) {
                            extTypes = new ExternalTypeHandler.Builder();
                        }
                        extTypes.addExternal(prop, typeDeser);
                    }
                }
            }
            */
        } else {
            creatorProps = null;
        }

        UnwrappedPropertyHandler unwrapped = null;

        for (SettableBeanProperty origProp : _beanProperties) {
            SettableBeanProperty prop = origProp;

            // May already have deserializer from annotations, if so, skip:
            if (!prop.hasValueDeserializer()) {
                // [databind#125]: allow use of converters
                JsonDeserializer<?> deser = findConvertingDeserializer(ctxt, prop);
                if (deser == null) {
                    deser = findDeserializer(ctxt, prop.getType(), prop);
                }
                prop = prop.withValueDeserializer(deser);
            } else { // may need contextual version
                JsonDeserializer<Object> deser = prop.getValueDeserializer();
                /* Important! This is the only place where actually handle "primary"
                 * property deserializers -- call is different from other places.
                 */
                JsonDeserializer<?> cd = ctxt.handlePrimaryContextualization(deser, prop,
                        prop.getType());
                if (cd != deser) {
                    prop = prop.withValueDeserializer(cd);
                }
            }

            // Need to link managed references with matching back references
            prop = _resolveManagedReferenceProperty(ctxt, prop);

            // [databind#351[: need to wrap properties that require object id resolution.
            if (!(prop instanceof ManagedReferenceProperty)) {
                prop = _resolvedObjectIdProperty(ctxt, prop);
            }
            // Support unwrapped values (via @JsonUnwrapped)
            SettableBeanProperty u = _resolveUnwrappedProperty(ctxt, prop);
            if (u != null) {
                prop = u;
                if (unwrapped == null) {
                    unwrapped = new UnwrappedPropertyHandler();
                }
                unwrapped.addProperty(prop);
                /* 12-Dec-2014, tatu: As per [databind#647], we will have problems if
                 *    the original property is left in place. So let's remove it now.
                 */
                _beanProperties.remove(prop);
                continue;
            }
            // non-static inner classes too:
            prop = _resolveInnerClassValuedProperty(ctxt, prop);
            if (prop != origProp) {
                _beanProperties.replace(prop);
                // [databind#795]: Make sure PropertyBasedCreator's properties stay in sync
                if (creatorProps != null) {
                    // 18-May-2015, tatu: _Should_ start with consistent set. But can we really
                    //   fully count on this? May need to revisit in future; seems to hold for now.
                    for (int i = 0, len = creatorProps.length; i < len; ++i) {
                        if (creatorProps[i] == origProp) {
                            creatorProps[i] = prop;
                            break;
                        }
                        // ... as per above, it is possible we'd need to add this as fallback
                        // if (but only if) identity check fails?
                        /*
                        if (creatorProps[i].getName().equals(prop.getName())) {
                            creatorProps[i] = prop;
                            break;
                        }
                        */
                    }
                }
            }
            // one more thing: if this property uses "external property" type inclusion,
            // it needs different handling altogether
            if (prop.hasValueTypeDeserializer()) {
                TypeDeserializer typeDeser = prop.getValueTypeDeserializer();
                if (typeDeser.getTypeInclusion() == JsonTypeInfo.As.EXTERNAL_PROPERTY) {
                    if (extTypes == null) {
                        extTypes = new ExternalTypeHandler.Builder();
                    }
                    extTypes.addExternal(prop, typeDeser);
                    // In fact, remove from list of known properties to simplify later handling
                    _beanProperties.remove(prop);
                    continue;
                }
            }
        }
        // "any setter" may also need to be resolved now
        if (_anySetter != null && !_anySetter.hasValueDeserializer()) {
            _anySetter = _anySetter.withValueDeserializer(findDeserializer(ctxt,
                    _anySetter.getType(), _anySetter.getProperty()));
        }
        // as well as delegate-based constructor:
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

        // and array-delegate-based constructor:
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

        // And now that we know CreatorProperty instances are also resolved can finally create the creator:
        if (creatorProps != null) {
            _propertyBasedCreator = PropertyBasedCreator.construct(ctxt, _valueInstantiator, creatorProps);
        }

        if (extTypes != null) {
            // 21-Jun-2016, tatu: related to [databind#999], may need to link type ids too,
            //    so need to pass collected properties
            _externalTypeIdHandler = extTypes.build();
            // we consider this non-standard, to offline handling
            _nonStandardCreation = true;
        }
        
        _unwrappedPropertyHandler = unwrapped;
        if (unwrapped != null) { // we consider this non-standard, to offline handling
            _nonStandardCreation = true;
        }

        // may need to disable vanilla processing, if unwrapped handling was enabled...
        _vanillaProcessing = _vanillaProcessing && !_nonStandardCreation;
    }

        public void addExternal(SettableBeanProperty property, TypeDeserializer typeDeser)
        {
            Integer index = _properties.size();
            _properties.add(new ExtTypedProperty(property, typeDeser));
            _nameToPropertyIndex.put(property.getName(), index);
            _nameToPropertyIndex.put(typeDeser.getPropertyName(), index);
        }

// relevant test
// com.fasterxml.jackson.databind.deser.TestAnyProperties::testJsonAnySetterOnNullMap
	public void testJsonAnySetterOnNullMap() throws Exception {
		JsonAnySetterOnNullMap result = MAPPER.readValue("{\"id\":2,\"name\":\"Joe\", \"city\":\"New Jersey\"}",
		        JsonAnySetterOnNullMap.class);
		assertEquals(2, result.id);
		assertNull(result.other);
	}

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testUntypedArray
    public void testUntypedArray() throws Exception
    {

        
        String JSON = "[ 1, null, \"x\", true, 2.0 ]";

        Object[] result = MAPPER.readValue(JSON, Object[].class);
        assertNotNull(result);

        assertEquals(5, result.length);

        assertEquals(Integer.valueOf(1), result[0]);
        assertNull(result[1]);
        assertEquals("x", result[2]);
        assertEquals(Boolean.TRUE, result[3]);
        assertEquals(Double.valueOf(2.0), result[4]);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testIntegerArray
    public void testIntegerArray() throws Exception
    {
        final int LEN = 90000;

        

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
        }
        sb.append(']');

        Integer[] result = MAPPER.readValue(sb.toString(), Integer[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            assertEquals(i, result[i].intValue());
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        assertNull(m.readValue(quote(""), Object[].class));
        assertNull( m.readValue(quote(""), String[].class));
        assertNull( m.readValue(quote(""), int[].class));
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testFromEmptyString2
    public void testFromEmptyString2() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        m.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Product p = m.readValue("{\"thelist\":\"\"}", Product.class);
        assertNotNull(p);
        assertNull(p.thelist);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testUntypedArrayOfArrays
    public void testUntypedArrayOfArrays() throws Exception
    {
        
        final String JSON = "[[[-0.027512,51.503221],[-0.008497,51.503221],[-0.008497,51.509744],[-0.027512,51.509744]]]";

        Object result = MAPPER.readValue(JSON, Object.class);
        assertEquals(ArrayList.class, result.getClass());
        assertNotNull(result);

        

        Object[] array = MAPPER.readValue(JSON, Object[].class);
        assertNotNull(array);
        assertEquals(Object[].class, array.getClass());

        
        ObjectWrapper w = MAPPER.readValue("{\"wrapped\":"+JSON+"}", ObjectWrapper.class);
        assertNotNull(w);
        assertNotNull(w.wrapped);
        assertEquals(ArrayList.class, w.wrapped.getClass());

        ObjectArrayWrapper aw = MAPPER.readValue("{\"wrapped\":"+JSON+"}", ObjectArrayWrapper.class);
        assertNotNull(aw);
        assertNotNull(aw.wrapped);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testStringArray
    public void testStringArray() throws Exception
    {
        final String[] STRS = new String[] {
            "a", "b", "abcd", "", "???", "\"quoted\"", "lf: \n",
        };
        StringWriter sw = new StringWriter();
        JsonGenerator jg = MAPPER.getFactory().createGenerator(sw);
        jg.writeStartArray();
        for (String str : STRS) {
            jg.writeString(str);
        }
        jg.writeEndArray();
        jg.close();

        String[] result = MAPPER.readValue(sw.toString(), String[].class);
        assertNotNull(result);

        assertEquals(STRS.length, result.length);
        for (int i = 0; i < STRS.length; ++i) {
            assertEquals(STRS[i], result[i]);
        }

        
        result = MAPPER.readValue(" [ null ]", String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertNull(result[0]);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testCharArray
    public void testCharArray() throws Exception
    {
        final String TEST_STR = "Let's just test it? Ok!";
        char[] result = MAPPER.readValue("\""+TEST_STR+"\"", char[].class);
        assertEquals(TEST_STR, new String(result));

        
        result = MAPPER.readValue("[\"a\",\"b\",\"c\"]", char[].class);
        assertEquals("abc", new String(result));
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testBooleanArray
    public void testBooleanArray() throws Exception
    {
        boolean[] result = MAPPER.readValue("[ true, false, false ]", boolean[].class);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertTrue(result[0]);
        assertFalse(result[1]);
        assertFalse(result[2]);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArrayAsNumbers
    public void testByteArrayAsNumbers() throws Exception
    {
        final int LEN = 37000;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            int value = i - 128;
            sb.append((value < 256) ? value : (value & 0x7F));
            sb.append(',');
        }
        sb.append("0]");
        byte[] result = MAPPER.readValue(sb.toString(), byte[].class);
        assertNotNull(result);
        assertEquals(LEN+1, result.length);
        for (int i = 0; i < LEN; ++i) {
            int value = i - 128;
            byte exp = (byte) ((value < 256) ? value : (value & 0x7F));
            if (exp != result[i]) {
                fail("At offset #"+i+" ("+result.length+"), expected "+exp+", got "+result[i]);
            }
            assertEquals(exp, result[i]);
        }
        assertEquals(0, result[LEN]);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArrayAsBase64
    public void testByteArrayAsBase64() throws Exception
    {
        
        JsonFactory jf = new JsonFactory();
        StringWriter sw = new StringWriter();

        int LEN = 9000;
        byte[] TEST = new byte[LEN];
        for (int i = 0; i < LEN; ++i) {
            TEST[i] = (byte) i;
        }

        JsonGenerator jg = jf.createGenerator(sw);
        jg.writeBinary(TEST);
        jg.close();
        String inputData = sw.toString();

        byte[] result = MAPPER.readValue(inputData, byte[].class);
        assertNotNull(result);
        assertArrayEquals(TEST, result);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArraysAsBase64
    public void testByteArraysAsBase64() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        StringWriter sw = new StringWriter(1000);

        final int entryCount = 15;

        JsonGenerator jg = jf.createGenerator(sw);
        jg.writeStartArray();

        byte[][] entries = new byte[entryCount][];
        for (int i = 0; i < entryCount; ++i) {
            byte[] b = new byte[1000 - i * 20];
            for (int x = 0; x < b.length; ++x) {
                b[x] = (byte) (i + x);
            }
            entries[i] = b;
            jg.writeBinary(b);
        }
        jg.writeEndArray();
        jg.close();

        String inputData = sw.toString();

        byte[][] result = MAPPER.readValue(inputData, byte[][].class);
        assertNotNull(result);

        assertEquals(entryCount, result.length);
        for (int i = 0; i < entryCount; ++i) {
            byte[] b = result[i];
            assertArrayEquals("Comparing entry #"+i+"/"+entryCount,entries[i], b);
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArraysWith763
    public void testByteArraysWith763() throws Exception
    {
        String[] input = new String[] { "YQ==", "Yg==", "Yw==" };
        byte[][] data = MAPPER.convertValue(input, byte[][].class);
        assertEquals("a", new String(data[0], "US-ASCII"));
        assertEquals("b", new String(data[1], "US-ASCII"));
        assertEquals("c", new String(data[2], "US-ASCII"));
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testShortArray
    public void testShortArray() throws Exception
    {
        final int LEN = 31001; 
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
        }
        sb.append(']');

        short[] result = MAPPER.readValue(sb.toString(), short[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            short exp = (short) i;
            assertEquals(exp, result[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testIntArray
    public void testIntArray() throws Exception
    {
        final int LEN = 70000;

        

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(-i);
        }
        sb.append(']');

        int[] result = MAPPER.readValue(sb.toString(), int[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            assertEquals(-i, result[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testLongArray
    public void testLongArray() throws Exception
    {
        final int LEN = 12300;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
        }
        sb.append(']');

        long[] result = MAPPER.readValue(sb.toString(), long[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            long exp = (long) i;
            assertEquals(exp, result[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testDoubleArray
    public void testDoubleArray() throws Exception
    {
        final int LEN = 7000;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i).append('.').append(i % 10);
        }
        sb.append(']');

        double[] result = MAPPER.readValue(sb.toString(), double[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            String expStr = String.valueOf(i) + "." + String.valueOf(i % 10);
            String actStr = String.valueOf(result[i]);
            if (!expStr.equals(actStr)) {
                fail("Entry #"+i+"/"+LEN+"; exp '"+expStr+"', got '"+actStr+"'");
            }
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testFloatArray
    public void testFloatArray() throws Exception
    {
        final int LEN = 7000;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            
            sb.append(i).append('.').append(i % 10);
        }
        sb.append(']');

        float[] result = MAPPER.readValue(sb.toString(), float[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            String expStr = String.valueOf(i) + "." + String.valueOf(i % 10);
            assertEquals(expStr, String.valueOf(result[i]));
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testBeanArray
    public void testBeanArray()
        throws Exception
    {
        List<Bean1> src = new ArrayList<Bean1>();

        List<Bean2> b2 = new ArrayList<Bean2>();
        b2.add(new Bean2("a"));
        b2.add(new Bean2("foobar"));
        src.add(new Bean1(1, 2, b2));

        b2 = new ArrayList<Bean2>();
        b2.add(null);
        src.add(new Bean1(4, 5, b2));

        
        StringWriter sw = new StringWriter();

        MAPPER.writeValue(sw, src);

        
        List<Bean1> result = MAPPER.readValue(sw.toString(), new TypeReference<List<Bean1>>() { });
        assertNotNull(result);
        assertEquals(src, result);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArrayTypeOverride890
    public void testByteArrayTypeOverride890() throws Exception
    {
        HiddenBinaryBean890 result = MAPPER.readValue(
                aposToQuotes("{'someBytes':'AQIDBA=='}"), HiddenBinaryBean890.class);
        assertNotNull(result);
        assertNotNull(result.someBytes);
        assertEquals(byte[].class, result.someBytes.getClass());
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testCustomDeserializers
    public void testCustomDeserializers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule testModule = new SimpleModule("test", Version.unknownVersion());
        testModule.addDeserializer(NonDeserializable[].class, new CustomNonDeserArrayDeserializer());
        mapper.registerModule(testModule);
        
        NonDeserializable[] result = mapper.readValue("[\"a\"]", NonDeserializable[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("a", result[0].value);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testSimpleSetter
    public void testSimpleSetter() throws Exception
    {
        SizeClassSetter result = MAPPER.readValue
            ("{ \"other\":3, \"size\" : 2, \"length\" : -999 }",
             SizeClassSetter.class);
                                             
        assertEquals(3, result._other);
        assertEquals(2, result._size);
        assertEquals(-999, result._length);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testSimpleSetter2
    public void testSimpleSetter2() throws Exception
    {
        SizeClassSetter2 result = MAPPER.readValue("{ \"x\": -3 }",
             SizeClassSetter2.class);
        assertEquals(-3, result._x);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testSimpleSetter3
    public void testSimpleSetter3() throws Exception
    {
        SizeClassSetter3 result = MAPPER.readValue
            ("{ \"x\": 128 }",
             SizeClassSetter3.class);
        assertEquals(128, result._x);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testSetterInheritance
    public void testSetterInheritance() throws Exception
    {
        BeanSubClass result = MAPPER.readValue
            ("{ \"x\":1, \"z\" : 3, \"y\" : 2 }",
             BeanSubClass.class);
        assertEquals(1, result._x);
        assertEquals(2, result._y);
        assertEquals(3, result._z);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testImpliedProperty
    public void testImpliedProperty() throws Exception
    {
        BeanWithDeserialize bean = MAPPER.readValue("{\"a\":3}", BeanWithDeserialize.class);
        assertNotNull(bean);
        assertEquals(3, bean.a);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testIssue442PrivateUnwrapped
    public void testIssue442PrivateUnwrapped() throws Exception
    {
        Issue442Bean bean = MAPPER.readValue("{\"i\":5}", Issue442Bean.class);
        assertEquals(5, bean.w.i);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testPropertyRemoval
    public void testPropertyRemoval() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ModuleImpl(new RemovingModifier("a")));
        Bean bean = mapper.readValue("{\"b\":\"2\"}", Bean.class);
        assertEquals("2", bean.b);
        
        assertEquals("a", bean.a);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testDeserializerReplacement
    public void testDeserializerReplacement() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ModuleImpl(new ReplacingModifier(new BogusBeanDeserializer("foo", "bar"))));
        Bean bean = mapper.readValue("{\"a\":\"xyz\"}", Bean.class);
        
        assertEquals("foo", bean.a);
        assertEquals("bar", bean.b);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testIssue476
    public void testIssue476() throws Exception
    {
        final String JSON = "{\"value1\" : {\"name\" : \"fruit\", \"value\" : \"apple\"}, \"value2\" : {\"name\" : \"color\", \"value\" : \"red\"}}";
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Issue476Module());
        mapper.readValue(JSON, Issue476Bean.class);

        
        assertEquals(2, Issue476Deserializer.propCount);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testPOJOFromEmptyString
    public void testPOJOFromEmptyString() throws Exception
    {
        
        assertFalse(MAPPER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        try {
            MAPPER.readValue(quote(""), Bean.class);
            fail("Should not accept Empty String for POJO");
        } catch (JsonProcessingException e) {
            verifyException(e, "from String value");
            assertValidLocation(e.getLocation());
        }
        
        ObjectReader r = MAPPER.readerFor(Bean.class)
                .with(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        Bean result = r.readValue(quote(""));
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testPOJOFromEmptyArray
    public void testPOJOFromEmptyArray() throws Exception
    {
        final String JSON = "  [\n]";
        assertFalse(MAPPER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(JSON, Bean.class);
            fail("Should not accept Empty Array for POJO by default");
        } catch (JsonProcessingException e) {
            verifyException(e, "START_ARRAY token");
            assertValidLocation(e.getLocation());
        }

        
        ObjectReader r = MAPPER.readerFor(Bean.class)
                .with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        Bean result = r.readValue(JSON);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyArrayDeserializer
    public void testModifyArrayDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new ArrayDeserializerModifier()));
        Object[] result = mapper.readValue("[1,2]", Object[].class);
        assertEquals(1, result.length);
        assertEquals("foo", result[0]);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyCollectionDeserializer
    public void testModifyCollectionDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new CollectionDeserializerModifier())
        );
        List<?> result = mapper.readValue("[1,2]", List.class);
        assertEquals(1, result.size());
        assertEquals("foo", result.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyMapDeserializer
    public void testModifyMapDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new MapDeserializerModifier())
        );
        Map<?,?> result = mapper.readValue("{\"a\":1,\"b\":2}", Map.class);
        assertEquals(1, result.size());
        assertEquals("foo", result.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyEnumDeserializer
    public void testModifyEnumDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new EnumDeserializerModifier())
        );
        Object result = mapper.readValue(quote("B"), EnumABC.class);
        assertEquals("foo", result);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyKeyDeserializer
    public void testModifyKeyDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new KeyDeserializerModifier())
        );
        Map<?,?> result = mapper.readValue("{\"a\":1}", Map.class);
        assertEquals(1, result.size());
        assertEquals("foo", result.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyStdScalarDeserializer
    public void testModifyStdScalarDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new BeanDeserializerModifier() {
                        @Override
                        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                BeanDescription beanDesc, JsonDeserializer<?> deser) {
                            if (beanDesc.getBeanClass() == String.class) {
                                return new UCStringDeserializer(deser);
                            }
                            return deser;
                        }
            }));
        Object result = mapper.readValue(quote("abcDEF"), String.class);
        assertEquals("ABCDEF", result);
    }

// com.fasterxml.jackson.databind.deser.TestCachingOfDeser::testCustomMapCaching1
    public void testCustomMapCaching1() throws Exception
    {

        ObjectMapper mapper = new ObjectMapper();
        TestMapWithCustom mapC = mapper.readValue(MAP_INPUT, TestMapWithCustom.class);
        TestMapNoCustom mapStd = mapper.readValue(MAP_INPUT, TestMapNoCustom.class);

        assertNotNull(mapC.map);
        assertNotNull(mapStd.map);
        assertEquals(Integer.valueOf(100), mapC.map.get("a"));
        assertEquals(Integer.valueOf(1), mapStd.map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestCachingOfDeser::testCustomMapCaching2
    public void testCustomMapCaching2() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        TestMapNoCustom mapStd = mapper.readValue(MAP_INPUT, TestMapNoCustom.class);
        TestMapWithCustom mapC = mapper.readValue(MAP_INPUT, TestMapWithCustom.class);

        assertNotNull(mapStd.map);
        assertNotNull(mapC.map);
        assertEquals(Integer.valueOf(1), mapStd.map.get("a"));
        assertEquals(Integer.valueOf(100), mapC.map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestCachingOfDeser::testCustomListCaching1
    public void testCustomListCaching1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TestListWithCustom listC = mapper.readValue(LIST_INPUT, TestListWithCustom.class);
        TestListNoCustom listStd = mapper.readValue(LIST_INPUT, TestListNoCustom.class);

        assertNotNull(listC.list);
        assertNotNull(listStd.list);
        assertEquals(Integer.valueOf(100), listC.list.get(0));
        assertEquals(Integer.valueOf(1), listStd.list.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestCachingOfDeser::testCustomListCaching2
    public void testCustomListCaching2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TestListNoCustom listStd = mapper.readValue(LIST_INPUT, TestListNoCustom.class);
        TestListWithCustom listC = mapper.readValue(LIST_INPUT, TestListWithCustom.class);

        assertNotNull(listC.list);
        assertNotNull(listStd.list);
        assertEquals(Integer.valueOf(100), listC.list.get(0));
        assertEquals(Integer.valueOf(1), listStd.list.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testUntypedList
    public void testUntypedList() throws Exception
    {
        
        String JSON = "[ \"text!\", true, null, 23 ]";

        
        
        Object value = MAPPER.readValue(JSON, Object.class);
        assertNotNull(value);
        assertTrue(value instanceof ArrayList<?>);
        List<?> result = (List<?>) value;

        assertEquals(4, result.size());

        assertEquals("text!", result.get(0));
        assertEquals(Boolean.TRUE, result.get(1));
        assertNull(result.get(2));
        assertEquals(Integer.valueOf(23), result.get(3));
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testExactStringCollection
    public void testExactStringCollection() throws Exception
    {
        
        String JSON = "[ \"a\", \"b\" ]";
        List<String> result = MAPPER.readValue(JSON, new TypeReference<ArrayList<String>>() { });

        assertNotNull(result);
        assertEquals(ArrayList.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testHashSet
    public void testHashSet() throws Exception
    {
        String JSON = "[ \"KEY1\", \"KEY2\" ]";

        EnumSet<Key> result = MAPPER.readValue(JSON, new TypeReference<EnumSet<Key>>() { });
        assertNotNull(result);
        assertTrue(EnumSet.class.isAssignableFrom(result.getClass()));
        assertEquals(2, result.size());

        assertTrue(result.contains(Key.KEY1));
        assertTrue(result.contains(Key.KEY2));
        assertFalse(result.contains(Key.WHATEVER));
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testCustomDeserializer
    public void testCustomDeserializer() throws IOException
    {
        CustomList result = MAPPER.readValue(quote("abc"), CustomList.class);
        assertEquals(1, result.size());
        assertEquals("abc", result.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testImplicitArrays
    public void testImplicitArrays() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        
        List<Integer> ints = mapper.readValue("4", List.class);
        assertEquals(1, ints.size());
        assertEquals(Integer.valueOf(4), ints.get(0));
        List<String> strings = mapper.readValue(quote("abc"), new TypeReference<ArrayList<String>>() { });
        assertEquals(1, strings.size());
        assertEquals("abc", strings.get(0));
        
        int[] intArray = mapper.readValue("-7", int[].class);
        assertEquals(1, intArray.length);
        assertEquals(-7, intArray[0]);
        String[] stringArray = mapper.readValue(quote("xyz"), String[].class);
        assertEquals(1, stringArray.length);
        assertEquals("xyz", stringArray[0]);

        
        List<XBean> xbeanList = mapper.readValue("{\"x\":4}", new TypeReference<List<XBean>>() { });
        assertEquals(1, xbeanList.size());
        assertEquals(XBean.class, xbeanList.get(0).getClass());

        Object ob = mapper.readValue("{\"x\":29}", XBean[].class);
        XBean[] xbeanArray = (XBean[]) ob;
        assertEquals(1, xbeanArray.length);
        assertEquals(XBean.class, xbeanArray[0].getClass());
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        List<?> result = r.forType(List.class).readValue(quote(""));
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testArrayBlockingQueue
    public void testArrayBlockingQueue() throws Exception
    {
        
        ArrayBlockingQueue<?> q = MAPPER.readValue("[1, 2, 3]", ArrayBlockingQueue.class);
        assertNotNull(q);
        assertEquals(3, q.size());
        assertEquals(Integer.valueOf(1), q.take());
        assertEquals(Integer.valueOf(2), q.take());
        assertEquals(Integer.valueOf(3), q.take());
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testIterableWithStrings
    public void testIterableWithStrings() throws Exception
    {
        String JSON = "{ \"values\":[\"a\",\"b\"]}";
        ListAsIterable w = MAPPER.readValue(JSON, ListAsIterable.class);
        assertNotNull(w);
        assertNotNull(w.values);
        Iterator<String> it = w.values.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testIterableWithBeans
    public void testIterableWithBeans() throws Exception
    {
        String JSON = "{ \"nums\":[{\"x\":1},{\"x\":2}]}";
        ListAsIterableX w = MAPPER.readValue(JSON, ListAsIterableX.class);
        assertNotNull(w);
        assertNotNull(w.nums);
        Iterator<XBean> it = w.nums.iterator();
        assertTrue(it.hasNext());
        XBean xb = it.next();
        assertNotNull(xb);
        assertEquals(1, xb.x);
        xb = it.next();
        assertEquals(2, xb.x);
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testArrayIndexForExceptions
    public void testArrayIndexForExceptions() throws Exception
    {
        final String OBJECTS_JSON = "[ \"KEY2\", false ]";
        try {
            MAPPER.readValue(OBJECTS_JSON, Key[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(1, refs.size());
            assertEquals(1, refs.get(0).getIndex());
        }

        try {
            MAPPER.readValue("[ \"xyz\", { } ]", String[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(1, refs.size());
            assertEquals(1, refs.get(0).getIndex());
        }

        try {
            MAPPER.readValue("{\"keys\":"+OBJECTS_JSON+"}", KeyListBean.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(2, refs.size());
            
            assertEquals(-1, refs.get(0).getIndex());
            assertEquals("keys", refs.get(0).getFieldName());

            
            assertEquals(1, refs.get(1).getIndex());
            assertNull(refs.get(1).getFieldName());
        }
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testWrapExceptions
    public void testWrapExceptions() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.WRAP_EXCEPTIONS);

        try {
            mapper.readValue("[{}]", new TypeReference<List<SomeObject>>() {});
        } catch (JsonMappingException exc) {
            assertEquals("I want to catch this exception", exc.getOriginalMessage());
        } catch (RuntimeException exc) {
            fail("The RuntimeException should have been wrapped with a JsonMappingException.");
        }

        ObjectMapper mapperNoWrap = new ObjectMapper();
        mapperNoWrap.disable(DeserializationFeature.WRAP_EXCEPTIONS);

        try {
            mapperNoWrap.readValue("[{}]", new TypeReference<List<SomeObject>>() {});
        } catch (JsonMappingException exc) {
            fail("It should not have wrapped the RuntimeException.");
        } catch (RuntimeException exc) {
            assertEquals("I want to catch this exception", exc.getMessage());
        }
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testSingletonCollections
    public void testSingletonCollections() throws Exception
    {
        final TypeReference<?> xbeanListType = new TypeReference<List<XBean>>() { };

        String json = MAPPER.writeValueAsString(Collections.singleton(new XBean(3)));
        Collection<XBean> result = MAPPER.readValue(json, xbeanListType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.iterator().next().x);

        json = MAPPER.writeValueAsString(Collections.singletonList(new XBean(28)));
        result = MAPPER.readValue(json, xbeanListType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(28, result.iterator().next().x);
    }

// com.fasterxml.jackson.databind.deser.TestConcurrency::testDeserializerResolution
    public void testDeserializerResolution() throws Exception
    {
        
        final String JSON = "{\"value\":42}";
        
        for (int i = 0; i < 5; ++i) {
            final ObjectMapper mapper = new ObjectMapper();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                         mapper.readValue(JSON, Bean.class);
                    } catch (Exception e) { }
                }
            };
            Thread t = new Thread(r);
            t.start();
            
            Thread.sleep(10L);
            
            Bean b = mapper.readValue(JSON, Bean.class);
            
            assertEquals(13, b.value);
            t.join();
        }   
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testEnumIndexes
    public void testEnumIndexes()
    {
        int max = 0;
        
        for (DeserializationFeature f : DeserializationFeature.values()) {
            max = Math.max(max, f.ordinal());
        }
        if (max >= 31) { 
            fail("Max number of DeserializationFeature enums reached: "+max);
        }
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testDefaults
    public void testDefaults()
    {
        ObjectMapper m = new ObjectMapper();
        DeserializationConfig cfg = m.getDeserializationConfig();

        
        assertTrue(cfg.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertTrue(cfg.isEnabled(MapperFeature.AUTO_DETECT_SETTERS));
        assertTrue(cfg.isEnabled(MapperFeature.AUTO_DETECT_CREATORS));
        assertTrue(cfg.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS));
        assertTrue(cfg.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS));

        assertFalse(cfg.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
        assertFalse(cfg.isEnabled(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS));

        assertTrue(cfg.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testOverrideIntrospectors
    public void testOverrideIntrospectors()
    {
        ObjectMapper m = new ObjectMapper();
        DeserializationConfig cfg = m.getDeserializationConfig();
        
        cfg = cfg.with((ClassIntrospector) null); 
        cfg = cfg.with((AnnotationIntrospector) null);
        assertNull(cfg.getAnnotationIntrospector());
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testAnnotationsDisabled
    public void testAnnotationsDisabled() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        assertTrue(m.getDeserializationConfig().isEnabled(MapperFeature.USE_ANNOTATIONS));
        
        AnnoBean bean = m.readValue("{ \"y\" : 0 }", AnnoBean.class);
        assertEquals(0, bean.value);

        m = new ObjectMapper();
        m.configure(MapperFeature.USE_ANNOTATIONS, false);
        
        bean = m.readValue("{ \"x\" : 0 }", AnnoBean.class);
        assertEquals(0, bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testEnumsWhenDisabled
    public void testEnumsWhenDisabled() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals(Alpha.B, m.readValue(quote("B"), Alpha.class));

        m = new ObjectMapper();
        m.configure(MapperFeature.USE_ANNOTATIONS, false);
        
        assertEquals(Alpha.B, m.readValue(quote("B"), Alpha.class));
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testNoAccessOverrides
    public void testNoAccessOverrides() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        SimpleBean bean = m.readValue("{\"x\":1,\"y\":2}", SimpleBean.class);
        assertEquals(1, bean.x);
        assertEquals(2, bean.y);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testCustomBeanDeserializer
    public void testCustomBeanDeserializer() throws Exception
    {
        String json = "{\"beans\":[{\"c\":{\"a\":10,\"b\":20},\"d\":\"hello, tatu\"}]}";
        TestBeans beans = MAPPER.readValue(json, TestBeans.class);

        assertNotNull(beans);
        List<TestBean> results = beans.beans;
        assertNotNull(results);
        assertEquals(1, results.size());
        TestBean bean = results.get(0);
        assertEquals("hello, tatu", bean.d);
        CustomBean c = bean.c;
        assertNotNull(c);
        assertEquals(10, c.a);
        assertEquals(20, c.b);

        json = "{\"beans\":[{\"c\":{\"b\":3,\"a\":-4},\"d\":\"\"},"
            +"{\"d\":\"abc\", \"c\":{\"b\":15}}]}";
        beans = MAPPER.readValue(json, TestBeans.class);

        assertNotNull(beans);
        results = beans.beans;
        assertNotNull(results);
        assertEquals(2, results.size());

        bean = results.get(0);
        assertEquals("", bean.d);
        c = bean.c;
        assertNotNull(c);
        assertEquals(-4, c.a);
        assertEquals(3, c.b);

        bean = results.get(1);
        assertEquals("abc", bean.d);
        c = bean.c;
        assertNotNull(c);
        assertEquals(0, c.a);
        assertEquals(15, c.b);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testDelegating
    public void testDelegating() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(Immutable.class,
            new StdDelegatingDeserializer<Immutable>(
                new StdConverter<JsonNode, Immutable>() {
                    @Override
                    public Immutable convert(JsonNode value)
                    {
                        int x = value.path("x").asInt();
                        int y = value.path("y").asInt();
                        return new Immutable(x, y);
                    }
                }
                ));

        mapper.registerModule(module);
        Immutable imm = mapper.readValue("{\"x\":3,\"y\":7}", Immutable.class);
        assertEquals(3, imm.x);
        assertEquals(7, imm.y);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testJsonNodeDelegating
    public void testJsonNodeDelegating() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(Immutable.class,
            new StdNodeBasedDeserializer<Immutable>(Immutable.class) {
                @Override
                public Immutable convert(JsonNode root, DeserializationContext ctxt) throws IOException {
                    int x = root.path("x").asInt();
                    int y = root.path("y").asInt();
                    return new Immutable(x, y);
                }
        });
        mapper.registerModule(module);
        Immutable imm = mapper.readValue("{\"x\":-10,\"y\":3}", Immutable.class);
        assertEquals(-10, imm.x);
        assertEquals(3, imm.y);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testIssue882
    public void testIssue882() throws Exception
    {
        Model original = new Model(Collections.singletonMap(new CustomKey(123), "test"));
        String json = MAPPER.writeValueAsString(original);
        Model deserialized = MAPPER.readValue(json, Model.class);
        assertNotNull(deserialized);
        assertNotNull(deserialized.map);
        assertEquals(1, deserialized.map.size());
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testContextReadValue
    public void testContextReadValue() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(Bean375Outer.class, new Bean375OuterDeserializer());
        module.addDeserializer(Bean375Inner.class, new Bean375InnerDeserializer());
        mapper.registerModule(module);

        
        Bean375Outer outer = mapper.readValue("13", Bean375Outer.class);
        assertEquals(26, outer.inner.x);

        
        Bean375Wrapper w = mapper.readValue("{\"value\":13}", Bean375Wrapper.class);
        assertNotNull(w.value);
        assertNotNull(w.value.inner);
        assertEquals(-13, w.value.inner.x);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testCurrentValueAccess
    public void testCurrentValueAccess() throws Exception
    {
        Issue631Bean bean = MAPPER.readValue(aposToQuotes("{'prop':'stuff'}"),
                Issue631Bean.class);
        assertNotNull(bean);
        assertEquals("prop/Issue631Bean", bean.prop);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testCustomStringDeser
    public void testCustomStringDeser() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().registerModule(
                new SimpleModule().addDeserializer(String.class, new UCStringDeserializer())
                );
        assertEquals("FOO", mapper.readValue(quote("foo"), String.class));
        StringWrapper sw = mapper.readValue("{\"str\":\"foo\"}", StringWrapper.class);
        assertNotNull(sw);
        assertEquals("FOO", sw.str);
    }

// com.fasterxml.jackson.databind.deser.TestCustomFactory::testCustomBeanDeserializer
    public void testCustomBeanDeserializer() throws Exception
    {

        final ObjectMapper map = new ObjectMapper();
        String json = "{\"beans\":[{\"c\":{\"a\":10,\"b\":20},\"d\":\"hello, tatu\"}]}";
        TestBeans beans = map.readValue(json, TestBeans.class);

        assertNotNull(beans);
        List<TestBean> results = beans.beans;
        assertNotNull(results);
        assertEquals(1, results.size());
        TestBean bean = results.get(0);
        assertEquals("hello, tatu", bean.d);
        CustomBean c = bean.c;
        assertNotNull(c);
        assertEquals(10, c.a);
        assertEquals(20, c.b);

        json = "{\"beans\":[{\"c\":{\"b\":3,\"a\":-4},\"d\":\"\"},"
            +"{\"d\":\"abc\", \"c\":{\"b\":15}}]}";
        beans = map.readValue(json, TestBeans.class);

        assertNotNull(beans);
        results = beans.beans;
        assertNotNull(results);
        assertEquals(2, results.size());

        bean = results.get(0);
        assertEquals("", bean.d);
        c = bean.c;
        assertNotNull(c);
        assertEquals(-4, c.a);
        assertEquals(3, c.b);

        bean = results.get(1);
        assertEquals("abc", bean.d);
        c = bean.c;
        assertNotNull(c);
        assertEquals(0, c.a);
        assertEquals(15, c.b);
    }

// com.fasterxml.jackson.databind.deser.TestCustomValueInstDefaults::testAllPresent
    public void testAllPresent() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket allPresent = mapper.readValue(
                "{\"a\":8,\"b\":9,\"c\":\"y\",\"d\":\"z\"}",
                Bucket.class);

        assertEquals(8, allPresent.a);
        assertEquals(9, allPresent.b);
        assertEquals("y", allPresent.c);
        assertEquals("z", allPresent.d);
    }

// com.fasterxml.jackson.databind.deser.TestCustomValueInstDefaults::testAllAbsent
    public void testAllAbsent() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket allAbsent = mapper.readValue(
                "{}",
                Bucket.class);

        assertEquals(Bucket.DEFAULT_A, allAbsent.a);
        assertEquals(Bucket.DEFAULT_B, allAbsent.b);
        assertEquals(Bucket.DEFAULT_C, allAbsent.c);
        assertEquals(Bucket.DEFAULT_D, allAbsent.d);
    }

// com.fasterxml.jackson.databind.deser.TestCustomValueInstDefaults::testMixedPresentAndAbsent
    public void testMixedPresentAndAbsent() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket aAbsent = mapper.readValue(
                "{\"b\":9,\"c\":\"y\",\"d\":\"z\"}",
                Bucket.class);

        assertEquals(Bucket.DEFAULT_A, aAbsent.a);
        assertEquals(9, aAbsent.b);
        assertEquals("y", aAbsent.c);
        assertEquals("z", aAbsent.d);

        Bucket bAbsent = mapper.readValue(
                "{\"a\":8,\"c\":\"y\",\"d\":\"z\"}",
                Bucket.class);

        assertEquals(8, bAbsent.a);
        assertEquals(Bucket.DEFAULT_B, bAbsent.b);
        assertEquals("y", bAbsent.c);
        assertEquals("z", bAbsent.d);

        Bucket cAbsent = mapper.readValue(
                "{\"a\":8,\"b\":9,\"d\":\"z\"}",
                Bucket.class);

        assertEquals(8, cAbsent.a);
        assertEquals(9, cAbsent.b);
        assertEquals(Bucket.DEFAULT_C, cAbsent.c);
        assertEquals("z", cAbsent.d);

        Bucket dAbsent = mapper.readValue(
                "{\"a\":8,\"b\":9,\"c\":\"y\"}",
                Bucket.class);

        assertEquals(8, dAbsent.a);
        assertEquals(9, dAbsent.b);
        assertEquals("y", dAbsent.c);
        assertEquals(Bucket.DEFAULT_D, dAbsent.d);
    }

// com.fasterxml.jackson.databind.deser.TestCustomValueInstDefaults::testPresentZeroPrimitive
    public void testPresentZeroPrimitive() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket aZeroRestAbsent = mapper.readValue(
                "{\"a\":0}",
                Bucket.class);

        assertEquals(0, aZeroRestAbsent.a);
        assertEquals(Bucket.DEFAULT_B, aZeroRestAbsent.b);
        assertEquals(Bucket.DEFAULT_C, aZeroRestAbsent.c);
        assertEquals(Bucket.DEFAULT_D, aZeroRestAbsent.d);
    }

// com.fasterxml.jackson.databind.deser.TestCustomValueInstDefaults::testPresentNullReference
    public void testPresentNullReference() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket cNullRestAbsent = mapper.readValue(
                "{\"c\":null}",
                Bucket.class);

        assertEquals(Bucket.DEFAULT_A, cNullRestAbsent.a);
        assertEquals(Bucket.DEFAULT_B, cNullRestAbsent.b);
        assertEquals(null, cNullRestAbsent.c);
        assertEquals(Bucket.DEFAULT_D, cNullRestAbsent.d);
    }

// com.fasterxml.jackson.databind.deser.TestCustomValueInstDefaults::testMoreThan32CreatorParams
    public void testMoreThan32CreatorParams() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        BigBucket big = mapper.readValue(
                "{\"i03\":0,\"i11\":1,\"s05\":null,\"s08\":\"x\"}",
                BigBucket.class);

        assertEquals(BigBucket.DEFAULT_I, big.i01);
        assertEquals(BigBucket.DEFAULT_I, big.i02);
        assertEquals(0, big.i03);
        assertEquals(BigBucket.DEFAULT_I, big.i04);
        assertEquals(BigBucket.DEFAULT_I, big.i05);
        assertEquals(BigBucket.DEFAULT_I, big.i06);
        assertEquals(BigBucket.DEFAULT_I, big.i07);
        assertEquals(BigBucket.DEFAULT_I, big.i08);
        assertEquals(BigBucket.DEFAULT_I, big.i09);
        assertEquals(BigBucket.DEFAULT_I, big.i10);
        assertEquals(1, big.i11);
        assertEquals(BigBucket.DEFAULT_I, big.i12);
        assertEquals(BigBucket.DEFAULT_I, big.i13);
        assertEquals(BigBucket.DEFAULT_I, big.i14);
        assertEquals(BigBucket.DEFAULT_I, big.i15);
        assertEquals(BigBucket.DEFAULT_I, big.i16);
        assertEquals(BigBucket.DEFAULT_S, big.s01);
        assertEquals(BigBucket.DEFAULT_S, big.s02);
        assertEquals(BigBucket.DEFAULT_S, big.s03);
        assertEquals(BigBucket.DEFAULT_S, big.s04);
        assertEquals(null, big.s05);
        assertEquals(BigBucket.DEFAULT_S, big.s06);
        assertEquals(BigBucket.DEFAULT_S, big.s07);
        assertEquals("x", big.s08);
        assertEquals(BigBucket.DEFAULT_S, big.s09);
        assertEquals(BigBucket.DEFAULT_S, big.s10);
        assertEquals(BigBucket.DEFAULT_S, big.s11);
        assertEquals(BigBucket.DEFAULT_S, big.s12);
        assertEquals(BigBucket.DEFAULT_S, big.s13);
        assertEquals(BigBucket.DEFAULT_S, big.s14);
        assertEquals(BigBucket.DEFAULT_S, big.s15);
        assertEquals(BigBucket.DEFAULT_S, big.s16);
    }

// com.fasterxml.jackson.databind.deser.TestCyclicTypes::testLinked
    public void testLinked() throws Exception
    {
        Bean first = MAPPER.readValue
            ("{\"name\":\"first\", \"next\": { "
             +" \"name\":\"last\", \"next\" : null }}",
             Bean.class);

        assertNotNull(first);
        assertEquals("first", first._name);
        Bean last = first._next;
        assertNotNull(last);
        assertEquals("last", last._name);
        assertNull(last._next);
    }

// com.fasterxml.jackson.databind.deser.TestCyclicTypes::testLinkedGeneric
    public void testLinkedGeneric() throws Exception
    {
        StringLink link = MAPPER.readValue("{\"next\":null}", StringLink.class);
        assertNotNull(link);
        assertNull(link.next);
    }

// com.fasterxml.jackson.databind.deser.TestCyclicTypes::testCycleWith2Classes
    public void testCycleWith2Classes() throws Exception
    {
        LinkA a = MAPPER.readValue("{\"next\":{\"a\":null}}", LinkA.class);
        assertNotNull(a.next);
        LinkB b = a.next;
        assertNull(b.a);
    }

// com.fasterxml.jackson.databind.deser.TestCyclicTypes::testIgnoredCycle
    public void testIgnoredCycle() throws Exception
    {
        Selfie405 self1 = new Selfie405(1);
        self1.parent = self1;

        
        assertTrue(MAPPER.isEnabled(SerializationFeature.FAIL_ON_SELF_REFERENCES));
        try {
            MAPPER.writeValueAsString(self1);
            fail("Should fail with direct self-ref");
        } catch (JsonMappingException e) {
            verifyException(e, "Direct self-reference");
        }
        
        ObjectWriter w = MAPPER.writer()
                .without(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        String json = w.writeValueAsString(self1);
        assertNotNull(json);
        assertEquals(aposToQuotes("{'id':1,'parent':{'id':1}}"), json);
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtil
    public void testDateUtil() throws Exception
    {
        long now = 123456789L;
        java.util.Date value = new java.util.Date(now);

        
        assertEquals(value, MAPPER.readValue(""+now, java.util.Date.class));

        
        String dateStr = dateToString(value);
        java.util.Date result = MAPPER.readValue("\""+dateStr+"\"", java.util.Date.class);

        assertEquals("Date: expect "+value+" ("+value.getTime()+"), got "+result+" ("+result.getTime()+")",
                value.getTime(), result.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilWithStringTimestamp
    public void testDateUtilWithStringTimestamp() throws Exception
    {
        long now = 1321992375446L;
        
        String json = quote(String.valueOf(now));
        java.util.Date value = MAPPER.readValue(json, java.util.Date.class);
        assertEquals(now, value.getTime());

        
        long before = - (24 * 3600 * 1000L);
        json = quote(String.valueOf(before));
        value = MAPPER.readValue(json, java.util.Date.class);
        assertEquals(before, value.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilRFC1123
    public void testDateUtilRFC1123() throws Exception
    {
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        
        String inputStr = "Sat, 17 Jan 2009 06:13:58 +0000";
        java.util.Date inputDate = fmt.parse(inputStr);
        assertEquals(inputDate, MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilRFC1123OnNonUSLocales
    public void testDateUtilRFC1123OnNonUSLocales() throws Exception
    {
        Locale old = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        
        String inputStr = "Sat, 17 Jan 2009 06:13:58 +0000";
        java.util.Date inputDate = fmt.parse(inputStr);
        assertEquals(inputDate, MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class));
        Locale.setDefault(old);
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601
    public void testDateUtilISO8601() throws Exception
    {
        
        String inputStr = "1972-12-28T00:00:00.000+0000";
        Date inputDate = MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        
        inputStr = "1972-12-28T00:00:00.000Z";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        
        inputStr = "1972-12-28T00:00:00.000+00:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        
        inputStr = "1972-12-28T00:00:00.000+00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        inputStr = "1984-11-30T00:00:00.000Z";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1984, c.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH));
        assertEquals(30, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testISO8601PartialMilliseconds
    public void testISO8601PartialMilliseconds() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        
        inputStr = "2014-10-03T18:00:00.6-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(600, c.get(Calendar.MILLISECOND));

        inputStr = "2014-10-03T18:00:00.61-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(18 + 5, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(610, c.get(Calendar.MILLISECOND));

        inputStr = "1997-07-16T19:20:30.45+01:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20:30.45+0100";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20:30.45+01";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testISO8601MissingSeconds
    public void testISO8601MissingSeconds() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        inputStr = "1997-07-16T19:20+01:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20+0200";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 2, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20+04";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 4, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601NoTimezone
    public void testDateUtilISO8601NoTimezone() throws Exception
    {
        
        String inputStr = "1984-11-13T00:00:09";
        Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1984, c.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(9, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601NoMilliseconds
    public void testDateUtilISO8601NoMilliseconds() throws Exception
    {
        final String INPUT_STR = "2013-10-31T17:27:00";
        Date inputDate;
        Calendar c;
        
        inputDate = MAPPER.readValue(quote(INPUT_STR), java.util.Date.class);
        c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(2013, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(31, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(17, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(27, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        
        
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601JustDate
    public void testDateUtilISO8601JustDate() throws Exception
    {
        
        String inputStr = "1972-12-28";
        Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateSql
    public void testDateSql() throws Exception
    {
        java.sql.Date value = new java.sql.Date(0L);
        value.setYear(99); 
        value.setDate(19);
        value.setMonth(Calendar.APRIL);
        long now = value.getTime();

        
        assertEquals(value, MAPPER.readValue(String.valueOf(now), java.sql.Date.class));

        
        
        java.sql.Date result = MAPPER.readValue(quote(value.toString()), java.sql.Date.class);
        Calendar c = gmtCalendar(result.getTime());
        assertEquals(1999, c.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, c.get(Calendar.MONTH));
        assertEquals(19, c.get(Calendar.DAY_OF_MONTH));

        
        String expStr = "1981-07-13";
        result = MAPPER.readValue(quote(expStr), java.sql.Date.class);
        c.setTimeInMillis(result.getTime());
        assertEquals(1981, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));

        

    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCalendar
    public void testCalendar() throws Exception
    {
        
        java.util.Calendar value = Calendar.getInstance();
        long l = 12345678L;
        value.setTimeInMillis(l);

        
        Calendar result = MAPPER.readValue(""+l, Calendar.class);
        assertEquals(l, result.getTimeInMillis());

        
        String dateStr = dateToString(new Date(l));
        result = MAPPER.readValue(quote(dateStr), Calendar.class);

        
        assertEquals(l, result.getTimeInMillis());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustom
    public void testCustom() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("PST"));
        mapper.setDateFormat(df);

        String dateStr = "1972-12-28X15:45:00";
        java.util.Date exp = df.parse(dateStr);
        java.util.Date result = mapper.readValue("\""+dateStr+"\"", java.util.Date.class);
        assertEquals(exp, result);
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDatesWithEmptyStrings
    public void testDatesWithEmptyStrings() throws Exception
    {
        assertNull(MAPPER.readValue(quote(""), java.util.Date.class));
        assertNull(MAPPER.readValue(quote(""), java.util.Calendar.class));
        assertNull(MAPPER.readValue(quote(""), java.sql.Date.class));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::test8601DateTimeNoMilliSecs
    public void test8601DateTimeNoMilliSecs() throws Exception
    {
        
        for (String inputStr : new String[] {
               "2010-06-28T23:34:22Z",
               "2010-06-28T23:34:22+0000",
               "2010-06-28T23:34:22+00:00",
               "2010-06-28T23:34:22+00",
        }) {
            Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.setTime(inputDate);
            assertEquals(2010, c.get(Calendar.YEAR));
            assertEquals(Calendar.JUNE, c.get(Calendar.MONTH));
            assertEquals(28, c.get(Calendar.DAY_OF_MONTH));
            assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
            assertEquals(34, c.get(Calendar.MINUTE));
            assertEquals(22, c.get(Calendar.SECOND));
            assertEquals(0, c.get(Calendar.MILLISECOND));
        }
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testTimeZone
    public void testTimeZone() throws Exception
    {
        TimeZone result = MAPPER.readValue(quote("PST"), TimeZone.class);
        assertEquals("PST", result.getID());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustomDateWithAnnotation
    public void testCustomDateWithAnnotation() throws Exception
    {
        final String INPUT = "{\"date\":\"/2005/05/25/\"}";
        DateAsStringBean result = MAPPER.readValue(INPUT, DateAsStringBean.class);
        assertNotNull(result);
        assertNotNull(result.date);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long l = result.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));

        
        
        result = MAPPER.readerFor(DateAsStringBean.class)
                .with(Locale.GERMANY)
                .readValue(INPUT);
        assertNotNull(result);
        assertNotNull(result.date);
        l = result.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));

        
        DateAsStringBeanGermany result2 = MAPPER.readerFor(DateAsStringBeanGermany.class).readValue(INPUT);
        assertNotNull(result2);
        assertNotNull(result2.date);
        l = result2.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustomCalendarWithAnnotation
    public void testCustomCalendarWithAnnotation() throws Exception
    {
        CalendarAsStringBean cbean = MAPPER.readValue("{\"cal\":\";2007/07/13;\"}",
                CalendarAsStringBean.class);
        assertNotNull(cbean);
        assertNotNull(cbean.cal);
        Calendar c = cbean.cal;
        assertEquals(2007, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustomCalendarWithTimeZone
    public void testCustomCalendarWithTimeZone() throws Exception
    {
        
        DateInCETBean cet = MAPPER.readValue("{\"date\":\"2001-01-01,10\"}",
                DateInCETBean.class);
        Calendar c = Calendar.getInstance(getUTCTimeZone());
        c.setTimeInMillis(cet.date.getTime());
        
        assertEquals(2001, c.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, c.get(Calendar.HOUR_OF_DAY));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testISO8601Directly
    public void testISO8601Directly() throws Exception
    {
        final String TIME_STR = "2015-01-21T08:56:13.533+0000";
        Date d = MAPPER.readValue(quote(TIME_STR), Date.class);
        assertNotNull(d);

        ISO8601DateFormat f = new ISO8601DateFormat();
        Date d2 = f.parse(TIME_STR);
        assertNotNull(d2);
        assertEquals(d.getTime(), d2.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testInvalidFormat
    public void testInvalidFormat() throws Exception
    {
        try {
            MAPPER.readValue(quote("foobar"), Date.class);
            fail("Should have failed with an exception");
        } catch (InvalidFormatException e) {
            verifyException(e, "Can not deserialize value of type java.util.Date from String");
            assertEquals("foobar", e.getValue());
            assertEquals(Date.class, e.getTargetType());
        } catch (Exception e) {
            fail("Wrong type of exception ("+e.getClass().getName()+"), should get "
                    +InvalidFormatException.class.getName());
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testIOException
    public void testIOException() throws IOException
    {
        IOException ioe = new IOException("TEST");
        String json = MAPPER.writeValueAsString(ioe);
        IOException result = MAPPER.readValue(json, IOException.class);
        assertEquals(ioe.getMessage(), result.getMessage());
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testWithCreator
    public void testWithCreator() throws IOException
    {
        final String MSG = "the message";
        String json = MAPPER.writeValueAsString(new MyException(MSG, 3));

        MyException result = MAPPER.readValue(json, MyException.class);
        assertEquals(MSG, result.getMessage());
        assertEquals(3, result.value);
        assertEquals(1, result.stuff.size());
        assertEquals(result.getFoo(), result.stuff.get("foo"));
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testWithNullMessage
    public void testWithNullMessage() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(new IOException((String) null));
        IOException result = mapper.readValue(json, IOException.class);
        assertNotNull(result);
        assertNull(result.getMessage());
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testNoArgsException
    public void testNoArgsException() throws IOException
    {
        MyNoArgException exc = MAPPER.readValue("{}", MyNoArgException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testJDK7SuppressionProperty
    public void testJDK7SuppressionProperty() throws IOException
    {
        Exception exc = MAPPER.readValue("{\"suppressed\":[]}", IOException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testSingleValueArrayDeserialization
    public void testSingleValueArrayDeserialization() {}

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testSingleValueArrayDeserializationException
    public void testSingleValueArrayDeserializationException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        final IOException exp;
        try {
            throw new IOException("testing");
        } catch (IOException internal) {
            exp = internal;
        }
        final String value = "[" + mapper.writeValueAsString(exp) + "]";
        
        try {
            mapper.readValue(value, IOException.class);
            fail("Exception not thrown when attempting to deserialize an IOException wrapped in a single value array with UNWRAP_SINGLE_VALUE_ARRAYS disabled");
        } catch (JsonMappingException exp2) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testHandlingOfUnrecognized
    public void testHandlingOfUnrecognized() throws Exception
    {
        UnrecognizedPropertyException exc = null;
        try {
            new ObjectMapper().readValue("{\"bar\":3}", Bean.class);
        } catch (UnrecognizedPropertyException e) {
            exc = e;
        }
        if (exc == null) {
            fail("Should have failed binding");
        }
        assertEquals("bar", exc.getPropertyName());
        assertEquals(Bean.class, exc.getReferringClass());
        
        verifyException(exc, "propX");
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testExceptionWithEmpty
    public void testExceptionWithEmpty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object result = mapper.readValue("    ", Object.class);
            fail("Expected an exception, but got result value: "+result);
        } catch (Exception e) {
            verifyException(e, JsonMappingException.class, "No content");
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testExceptionWithIncomplete
    public void testExceptionWithIncomplete()
        throws Exception
    {
        BrokenStringReader r = new BrokenStringReader("[ 1, ", "TEST");
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(r);
        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unused")
            Object ob = mapper.readValue(jp, Object.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testExceptionWithEOF
    public void testExceptionWithEOF()
        throws Exception
    {
        StringReader r = new StringReader("  3");
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(r);
        ObjectMapper mapper = new ObjectMapper();

        Integer I = mapper.readValue(jp, Integer.class);
        assertEquals(3, I.intValue());

        
        try {
            I = mapper.readValue(jp, Integer.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, JsonMappingException.class, "No content");
        }
        
        JsonToken t = jp.getCurrentToken();
        if (t != null) {
            fail("Expected current token to be null after end-of-stream, was: "+t);
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandlingWithDefaultDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals("com.fasterxml.jackson.databind.deser.Foo[\"bar\"]->com.fasterxml.jackson.databind.deser.Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandlingWithJsonCreatorDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals("com.fasterxml.jackson.databind.deser.Foo[\"bar\"]->com.fasterxml.jackson.databind.deser.Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testSimpleAutoDetect
    public void testSimpleAutoDetect() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        SimpleFieldBean result = m.readValue("{ \"x\" : -13 }",
                                           SimpleFieldBean.class);
        assertEquals(-13, result.x);
        assertEquals(0, result.y);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testSimpleAnnotation
    public void testSimpleAnnotation() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        SimpleFieldBean2 bean = m.readValue("{ \"values\" : [ \"x\", \"y\" ] }",
                SimpleFieldBean2.class);
        String[] values = bean.values;
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("x", values[0]);
        assertEquals("y", values[1]);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testNoAutoDetect
    public void testNoAutoDetect() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        NoAutoDetectBean bean = m.readValue("{ \"z\" : 7 }",
                                            NoAutoDetectBean.class);
        assertEquals(7, bean._z);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testTypeAnnotation
    public void testTypeAnnotation() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        AbstractWrapper w = m.readValue("{ \"value\" : \"abc\" }",
                                        AbstractWrapper.class);
        Abstract bean = w.value;
        assertNotNull(bean);
        assertEquals(Concrete.class, bean.getClass());
        assertEquals("abc", ((Concrete)bean).value);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testFailureDueToDups
    public void testFailureDueToDups() throws Exception
    {
        try {
            writeAndMap(new ObjectMapper(), new DupFieldBean());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing property");
        }
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testFailureDueToDups2
    public void testFailureDueToDups2() throws Exception
    {
        try {
            writeAndMap(new ObjectMapper(), new DupFieldBean2());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing property");
        }
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testOkFieldOverride
    public void testOkFieldOverride() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        OkDupFieldBean result = m.readValue("{ \"x\" : 1, \"y\" : 2 }",
                OkDupFieldBean.class);
        assertEquals(1, result.myX);
        assertEquals(2, result.y);
    }

// com.fasterxml.jackson.databind.deser.TestGenericCollectionDeser::testListSubClass
    public void testListSubClass() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ListSubClass result = mapper.readValue("[ \"123\" ]", ListSubClass.class);
        assertEquals(1, result.size());
        Object value = result.get(0);
        assertEquals(StringWrapper.class, value.getClass());
        StringWrapper bw = (StringWrapper) value;
        assertEquals("123", bw.str);
    }

// com.fasterxml.jackson.databind.deser.TestGenericCollectionDeser::testAnnotatedLStringist
    public void testAnnotatedLStringist() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedStringList result = mapper.readValue("[ \"...\" ]", AnnotatedStringList.class);
        assertEquals(1, result.size());
        Object ob = result.get(0);
        assertEquals(StringWrapper.class, ob.getClass());
        assertEquals("...", ((StringWrapper) ob).str);
    }

// com.fasterxml.jackson.databind.deser.TestGenericCollectionDeser::testAnnotatedBooleanList
    public void testAnnotatedBooleanList() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedBooleanList result = mapper.readValue("[ false ]", AnnotatedBooleanList.class);
        assertEquals(1, result.size());
        Object ob = result.get(0);
        assertEquals(BooleanWrapper.class, ob.getClass());
        assertFalse(((BooleanWrapper) ob).b);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testMapSubClass
    public void testMapSubClass() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        MapSubClass result = mapper.readValue
            ("{\"a\":true }", MapSubClass.class);
        assertEquals(1, result.size());
        Object value = result.get("a");
        assertEquals(BooleanWrapper.class, value.getClass());
        BooleanWrapper bw = (BooleanWrapper) value;
        assertEquals(Boolean.TRUE, bw.b);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testMapWrapper
    public void testMapWrapper() throws Exception
    {
        StringMap value = new ObjectMapper().readValue
            ("{\"entries\":{\"a\":9} }", StringMap.class);
        assertNotNull(value.getEntries());
        assertEquals(1, value.getEntries().size());
        assertEquals(Long.valueOf(9), value.getEntries().get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testIntermediateTypes
    public void testIntermediateTypes() throws Exception
    {
        StringStringWrapperMap result = new ObjectMapper().readValue
            ("{\"a\":\"b\"}", StringStringWrapperMap.class);
        assertEquals(1, result.size());
        Object value = result.get("a");
        assertNotNull(value);
        assertEquals(value.getClass(), StringWrapper.class);
        assertEquals("b", ((StringWrapper) value).str);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testAnnotatedMap
    public void testAnnotatedMap() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMap result = mapper.readValue
            ("{\"a\":true }", AnnotatedMap.class);
        assertEquals(1, result.size());
        Map.Entry<Object,Object> en = result.entrySet().iterator().next();
        assertEquals(StringWrapper.class, en.getKey().getClass());
        assertEquals(BooleanWrapper.class, en.getValue().getClass());
        assertEquals("a", ((StringWrapper) en.getKey()).str);
        assertEquals(Boolean.TRUE, ((BooleanWrapper) en.getValue()).b);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testKeyViaCtor
    public void testKeyViaCtor() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<KeyTypeCtor,Integer> map = mapper.readValue("{\"a\":123}",
                TypeFactory.defaultInstance().constructMapType(HashMap.class, KeyTypeCtor.class, Integer.class));
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123), entry.getValue());
        Object key = entry.getKey();
        assertEquals(KeyTypeCtor.class, key.getClass());
        assertEquals("a", ((KeyTypeCtor) key).value);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testKeyViaFactory
    public void testKeyViaFactory() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<KeyTypeCtor,Integer> map = mapper.readValue("{\"a\":123}",
                TypeFactory.defaultInstance().constructMapType(HashMap.class, KeyTypeFactory.class, Integer.class));
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123), entry.getValue());
        Object key = entry.getKey();
        assertEquals(KeyTypeFactory.class, key.getClass());
        assertEquals("a", ((KeyTypeFactory) key).value);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testSimpleNumberBean
    public void testSimpleNumberBean() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        NumberBean result = mapper.readValue("{\"number\":17}", NumberBean.class);
        assertEquals(17, result._number);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testGenericWrapper
    public void testGenericWrapper() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Wrapper<SimpleBean> result = mapper.readValue
            ("{\"value\": { \"x\" : 13 } }",
             new TypeReference<Wrapper<SimpleBean>>() { });
        assertNotNull(result);
        assertEquals(Wrapper.class, result.getClass());
        Object contents = result.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testGenericWrapperWithSingleElementArray
    public void testGenericWrapperWithSingleElementArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        Wrapper<SimpleBean> result = mapper.readValue
            ("[{\"value\": [{ \"x\" : 13 }] }]",
             new TypeReference<Wrapper<SimpleBean>>() { });
        assertNotNull(result);
        assertEquals(Wrapper.class, result.getClass());
        Object contents = result.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testMultipleWrappers
    public void testMultipleWrappers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        
        Wrapper<Boolean> result = mapper.readValue
            ("{\"value\": true}", new TypeReference<Wrapper<Boolean>>() { });
        assertEquals(new Wrapper<Boolean>(Boolean.TRUE), result);

        
        Wrapper<String> result2 = mapper.readValue
            ("{\"value\": \"abc\"}", new TypeReference<Wrapper<String>>() { });
        assertEquals(new Wrapper<String>("abc"), result2);

        
        Wrapper<Long> result3 = mapper.readValue
            ("{\"value\": 7}", new TypeReference<Wrapper<Long>>() { });
        assertEquals(new Wrapper<Long>(7L), result3);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testMultipleWrappersSingleValueArray
    public void testMultipleWrappersSingleValueArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        
        Wrapper<Boolean> result = mapper.readValue
            ("[{\"value\": [true]}]", new TypeReference<Wrapper<Boolean>>() { });
        assertEquals(new Wrapper<Boolean>(Boolean.TRUE), result);

        
        Wrapper<String> result2 = mapper.readValue
            ("[{\"value\": [\"abc\"]}]", new TypeReference<Wrapper<String>>() { });
        assertEquals(new Wrapper<String>("abc"), result2);

        
        Wrapper<Long> result3 = mapper.readValue
            ("[{\"value\": [7]}]", new TypeReference<Wrapper<Long>>() { });
        assertEquals(new Wrapper<Long>(7L), result3);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testArrayOfGenericWrappers
    public void testArrayOfGenericWrappers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Wrapper<SimpleBean>[] result = mapper.readValue
            ("[ {\"value\": { \"x\" : 9 } } ]",
             new TypeReference<Wrapper<SimpleBean>[]>() { });
        assertNotNull(result);
        assertEquals(Wrapper[].class, result.getClass());
        assertEquals(1, result.length);
        Wrapper<SimpleBean> elem = result[0];
        Object contents = elem.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(9, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testArrayOfGenericWrappersSingleValueArray
    public void testArrayOfGenericWrappersSingleValueArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        Wrapper<SimpleBean>[] result = mapper.readValue
            ("[ {\"value\": [ { \"x\" : [ 9 ] } ] } ]",
             new TypeReference<Wrapper<SimpleBean>[]>() { });
        assertNotNull(result);
        assertEquals(Wrapper[].class, result.getClass());
        assertEquals(1, result.length);
        Wrapper<SimpleBean> elem = result[0];
        Object contents = elem.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(9, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testIgnoredType
    public void testIgnoredType() throws Exception
    {
        final ObjectMapper mapper = objectMapper();

        
        NonIgnoredType bean = mapper.readValue("{\"value\":13}", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(13, bean.value);

        
        bean = mapper.readValue("{ \"ignored\":[1,2,{}], \"value\":9 }", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(9, bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testSingleWithMixins
    public void testSingleWithMixins() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Person.class, PersonMixin.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        PersonWrapper input = new PersonWrapper();
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value\":1}", json);
    }

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testListWithMixins
    public void testListWithMixins() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Person.class, PersonMixin.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("Bob"));
        String json = mapper.writeValueAsString(persons);
        assertEquals("[{\"name\":\"Bob\"}]", json);
    }

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testIgnoreUsingConfigOverride
    public void testIgnoreUsingConfigOverride() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.configOverride(Wrapped.class).setIsIgnoredType(true);

        
        String json = mapper.writeValueAsString(new Wrapper());
        assertEquals(aposToQuotes("{'value':3}"), json);

        
        Wrapper result = mapper.readValue(aposToQuotes("{'value':5,'wrapped':false}"),
                Wrapper.class);
        assertEquals(5, result.value);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testSimple
    public void testSimple() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std()
            .addValue(String.class, "stuffValue")
            .addValue("myId", "xyz")
            .addValue(Long.TYPE, Long.valueOf(37))
            );
        InjectedBean bean = mapper.readValue("{\"value\":3}", InjectedBean.class);
        assertEquals(3, bean.value);
        assertEquals("stuffValue", bean.stuff);
        assertEquals("xyz", bean.otherStuff);
        assertEquals(37L, bean.third);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testWithCtors
    public void testWithCtors() throws Exception
    {
        CtorBean bean = MAPPER.readerFor(CtorBean.class)
            .with(new InjectableValues.Std()
                .addValue(String.class, "Bubba"))
            .readValue("{\"age\":55}");
        assertEquals(55, bean.age);
        assertEquals("Bubba", bean.name);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testTwoInjectablesViaCreator
    public void testTwoInjectablesViaCreator() throws Exception
    {
        CtorBean2 bean = MAPPER.readerFor(CtorBean2.class)
                .with(new InjectableValues.Std()
                    .addValue(String.class, "Bob")
                    .addValue("number", Integer.valueOf(13))
                ).readValue("{ }");
        assertEquals(Integer.valueOf(13), bean.age);
        assertEquals("Bob", bean.name);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testInvalidDup
    public void testInvalidDup() throws Exception
    {
        try {
            MAPPER.readValue("{}", BadBean1.class);
        } catch (Exception e) {
            verifyException(e, "Duplicate injectable value");
        }
        try {
            MAPPER.readValue("{}", BadBean2.class);
        } catch (Exception e) {
            verifyException(e, "Duplicate injectable value");
        }
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testIssueGH471
    public void testIssueGH471() throws Exception
    {
        final Object constructorInjected = "constructorInjected";
        final Object methodInjected = "methodInjected";
        final Object fieldInjected = "fieldInjected";

        ObjectMapper mapper = new ObjectMapper()
                        .setInjectableValues(new InjectableValues.Std()
                                .addValue("constructor_injected", constructorInjected)
                                .addValue("method_injected", methodInjected)
                                .addValue("field_injected", fieldInjected));

        IssueGH471Bean bean = mapper.readValue("{\"x\":13,\"constructor_value\":\"constructor\",\"method_value\":\"method\",\"field_value\":\"field\"}",
                IssueGH471Bean.class);

        
        assertSame(constructorInjected, bean.constructorInjected);
        assertSame(methodInjected, bean.methodInjected);
        assertSame(fieldInjected, bean.fieldInjected);

        
        assertEquals("constructor", bean.constructorValue);
        assertEquals("method", bean.methodValue);
        assertEquals("field", bean.fieldValue);

        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testTransientField
    public void testTransientField() throws Exception
    {
        TransientBean bean = MAPPER.readerFor(TransientBean.class)
                .with(new InjectableValues.Std()
                        .addValue("transient", "Injected!"))
                .readValue("{\"value\":28}");
        assertEquals(28, bean.value);
        assertEquals("Injected!", bean.injected);
    }

// com.fasterxml.jackson.databind.deser.TestInnerClass::testSimpleNonStaticInner
    public void testSimpleNonStaticInner() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        Dog input = new Dog("Smurf", true);
        String json = mapper.writeValueAsString(input);
        Dog output = mapper.readValue(json, Dog.class);
        assertEquals("Smurf", output.name);
        assertNotNull(output.brain);
        assertTrue(output.brain.isThinking);
        
        assertEquals("Smurf", output.brain.parentName());
        output.name = "Foo";
        assertEquals("Foo", output.brain.parentName());
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAtomicBoolean
    public void testAtomicBoolean() throws Exception
    {
        AtomicBoolean b = MAPPER.readValue("true", AtomicBoolean.class);
        assertTrue(b.get());
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAtomicInt
    public void testAtomicInt() throws Exception
    {
        AtomicInteger value = MAPPER.readValue("13", AtomicInteger.class);
        assertEquals(13, value.get());
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAtomicLong
    public void testAtomicLong() throws Exception
    {
        AtomicLong value = MAPPER.readValue("12345678901", AtomicLong.class);
        assertEquals(12345678901L, value.get());
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAtomicReference
    public void testAtomicReference() throws Exception
    {
        AtomicReference<long[]> value = MAPPER.readValue("[1,2]",
                new com.fasterxml.jackson.core.type.TypeReference<AtomicReference<long[]>>() { });
        Object ob = value.get();
        assertNotNull(ob);
        assertEquals(long[].class, ob.getClass());
        long[] longs = (long[]) ob;
        assertNotNull(longs);
        assertEquals(2, longs.length);
        assertEquals(1, longs[0]);
        assertEquals(2, longs[1]);
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAbsentExclusion
    public void testAbsentExclusion() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new SimpleWrapper(null)));
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testSerPropInclusionAlways
    public void testSerPropInclusionAlways() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.ALWAYS);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testSerPropInclusionNonNull
    public void testSerPropInclusionNonNull() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_NULL);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testSerPropInclusionNonAbsent
    public void testSerPropInclusionNonAbsent() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_ABSENT);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testSerPropInclusionNonEmpty
    public void testSerPropInclusionNonEmpty() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_EMPTY);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testPolymorphicAtomicReference
    public void testPolymorphicAtomicReference() throws Exception
    {
        RefWrapper input = new RefWrapper(13);
        String json = MAPPER.writeValueAsString(input);
        
        RefWrapper result = MAPPER.readValue(json, RefWrapper.class);
        assertNotNull(result.w);
        Object ob = result.w.get();
        assertEquals(Impl.class, ob.getClass());
        assertEquals(13, ((Impl) ob).value);
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testFilteringOfAtomicReference
    public void testFilteringOfAtomicReference() throws Exception
    {
        SimpleWrapper input = new SimpleWrapper(null);
        ObjectMapper mapper = MAPPER;

        
        assertEquals("{\"value\":null}", mapper.writeValueAsString(input));

        
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude
                .Include.NON_NULL);
        assertEquals("{\"value\":null}", mapper.writeValueAsString(input));

        
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude
                .Include.NON_EMPTY);
        assertEquals("{}", mapper.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testTypeRefinement
    public void testTypeRefinement() throws Exception
    {
        RefiningWrapper input = new RefiningWrapper();
        BigDecimal bd = new BigDecimal("0.25");
        input.value = new AtomicReference<Serializable>(bd);
        String json = MAPPER.writeValueAsString(input);

        
        RefiningWrapper result = MAPPER.readValue(json, RefiningWrapper.class);
        assertNotNull(result.value);
        Object ob = result.value.get();
        assertEquals(BigDecimal.class, ob.getClass());
        assertEquals(bd, ob);
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testDeserializeWithContentAs
    public void testDeserializeWithContentAs() throws Exception
    {
        AtomicRefReadWrapper result = MAPPER.readValue(aposToQuotes("{'value':'abc'}"),
                AtomicRefReadWrapper.class);
         Object v = result.value.get();
         assertNotNull(v);
         assertEquals(WrappedString.class, v.getClass());
         assertEquals("abc", ((WrappedString)v).value);
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testWithUnwrapping
    public void testWithUnwrapping() throws Exception
    {
         String jsonExp = aposToQuotes("{'XX.name':'Bob'}");
         String jsonAct = MAPPER.writeValueAsString(new UnwrappingRefParent());
         assertEquals(jsonExp, jsonAct);
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testWithCustomDeserializer
    public void testWithCustomDeserializer() throws Exception
    {
        LCStringWrapper w = MAPPER.readValue(aposToQuotes("{'value':'FoobaR'}"),
                LCStringWrapper.class);
        assertEquals("foobar", w.value.get());
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testJsonLocation
    public void testJsonLocation() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        JsonLocation loc = new JsonLocation("whatever",  -1, -1, 100, 13);
        
        String ser = serializeAsString(m, loc);
        JsonLocation result = m.readValue(ser, JsonLocation.class);
        assertNotNull(result);
        assertEquals(loc.getSourceRef(), result.getSourceRef());
        assertEquals(loc.getByteOffset(), result.getByteOffset());
        assertEquals(loc.getCharOffset(), result.getCharOffset());
        assertEquals(loc.getColumnNr(), result.getColumnNr());
        assertEquals(loc.getLineNr(), result.getLineNr());
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testJsonLocationProps
    public void testJsonLocationProps()
    {
        JsonLocation loc = new JsonLocation(null,  -1, -1, 100, 13);
        assertTrue(loc.equals(loc));
        assertFalse(loc.equals(null));
        assertFalse(loc.equals("abx"));

        
        loc.hashCode();
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testTokenBufferWithSample
    public void testTokenBufferWithSample() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        TokenBuffer result = m.readValue(SAMPLE_DOC_JSON_SPEC, TokenBuffer.class);
        verifyJsonSpecSampleDoc(result.asParser(), true);
        result.close();
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testTokenBufferWithSequence
    public void testTokenBufferWithSequence() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        JsonParser jp = createParserUsingReader("[ 32, [ 1 ], \"abc\", { \"a\" : true } ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        TokenBuffer buf = m.readValue(jp, TokenBuffer.class);

        
        JsonParser bufParser = buf.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, bufParser.nextToken());
        assertEquals(32, bufParser.getIntValue());
        assertNull(bufParser.nextToken());

        
        buf = m.readValue(jp, TokenBuffer.class);
        bufParser = buf.asParser();
        assertToken(JsonToken.START_ARRAY, bufParser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, bufParser.nextToken());
        assertEquals(1, bufParser.getIntValue());
        assertToken(JsonToken.END_ARRAY, bufParser.nextToken());
        assertNull(bufParser.nextToken());

        
        buf = m.readValue(jp, TokenBuffer.class);
        String str = m.readValue(buf.asParser(), String.class);
        assertEquals("abc", str);

        
        buf = m.readValue(jp, TokenBuffer.class);
        Map<?,?> map = m.readValue(buf.asParser(), Map.class);
        assertEquals(1, map.size());
        assertEquals(Boolean.TRUE, map.get("a"));
        
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testJavaType
    public void testJavaType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = TypeFactory.defaultInstance();
        
        String json = mapper.writeValueAsString(tf.constructType(String.class));
        assertEquals(quote(java.lang.String.class.getName()), json);
        
        JavaType t = mapper.readValue(json, JavaType.class);
        assertNotNull(t);
        assertEquals(String.class, t.getRawClass());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testFile
    public void testFile() throws Exception
    {
        
        File src = new File("/test").getAbsoluteFile();
        String abs = src.getAbsolutePath();

        
        String json = MAPPER.writeValueAsString(abs);
        File result = MAPPER.readValue(json, File.class);
        assertEquals(abs, result.getAbsolutePath());

        
        final ObjectMapper mapper2 = new ObjectMapper();
        mapper2.setVisibility(PropertyAccessor.CREATOR, Visibility.NONE);

        result = mapper2.readValue(json, File.class);
        assertEquals(abs, result.getAbsolutePath());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testRegexps
    public void testRegexps() throws IOException
    {
        final String PATTERN_STR = "abc:\\s?(\\d+)";
        Pattern exp = Pattern.compile(PATTERN_STR);
        
        String json = MAPPER.writeValueAsString(exp);
        Pattern result = MAPPER.readValue(json, Pattern.class);
        assertEquals(exp.pattern(), result.pattern());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCurrency
    public void testCurrency() throws IOException
    {
        Currency usd = Currency.getInstance("USD");
        assertEquals(usd, new ObjectMapper().readValue(quote("USD"), Currency.class));
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testLocale
    public void testLocale() throws IOException
    {
        assertEquals(new Locale("en"), MAPPER.readValue(quote("en"), Locale.class));
        assertEquals(new Locale("es", "ES"), MAPPER.readValue(quote("es_ES"), Locale.class));
        assertEquals(new Locale("FI", "fi", "savo"),
                MAPPER.readValue(quote("fi_FI_savo"), Locale.class));
        
        Locale loc = MAPPER.readValue(quote(""), Locale.class);
        assertSame(Locale.ROOT, loc);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testNullForPrimitives
    public void testNullForPrimitives() throws IOException
    {
        
        PrimitivesBean bean = MAPPER.readValue("{\"intValue\":null, \"booleanValue\":null, \"doubleValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.intValue);
        assertEquals(false, bean.booleanValue);
        assertEquals(0.0, bean.doubleValue);

        bean = MAPPER.readValue("{\"byteValue\":null, \"longValue\":null, \"floatValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals((byte) 0, bean.byteValue);
        assertEquals(0L, bean.longValue);
        assertEquals(0.0f, bean.floatValue);
        
        
        final ObjectMapper mapper2 = new ObjectMapper();
        mapper2.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        
        try {
            mapper2.readValue("{\"booleanValue\":null}", PrimitivesBean.class);
            fail("Expected failure for boolean + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type boolean");
        }
        
        try {
            mapper2.readValue("{\"byteValue\":null}", PrimitivesBean.class);
            fail("Expected failure for byte + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type byte");
        }
        try {
            mapper2.readValue("{\"charValue\":null}", PrimitivesBean.class);
            fail("Expected failure for char + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type char");
        }
        try {
            mapper2.readValue("{\"shortValue\":null}", PrimitivesBean.class);
            fail("Expected failure for short + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type short");
        }
        try {
            mapper2.readValue("{\"intValue\":null}", PrimitivesBean.class);
            fail("Expected failure for int + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type int");
        }
        try {
            mapper2.readValue("{\"longValue\":null}", PrimitivesBean.class);
            fail("Expected failure for long + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type long");
        }

        
        try {
            mapper2.readValue("{\"floatValue\":null}", PrimitivesBean.class);
            fail("Expected failure for float + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type float");
        }
        try {
            mapper2.readValue("{\"doubleValue\":null}", PrimitivesBean.class);
            fail("Expected failure for double + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type double");
        }
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCharSequence
    public void testCharSequence() throws IOException
    {
        CharSequence cs = MAPPER.readValue("\"abc\"", CharSequence.class);
        assertEquals(String.class, cs.getClass());
        assertEquals("abc", cs.toString());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testInetAddress
    public void testInetAddress() throws IOException
    {
        InetAddress address = MAPPER.readValue(quote("127.0.0.1"), InetAddress.class);
        assertEquals("127.0.0.1", address.getHostAddress());

        
        final String HOST = "google.com";
        address = MAPPER.readValue(quote(HOST), InetAddress.class);
        assertEquals(HOST, address.getHostName());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testInetSocketAddress
    public void testInetSocketAddress() throws IOException
    {
        InetSocketAddress address = MAPPER.readValue(quote("127.0.0.1"), InetSocketAddress.class);
        assertEquals("127.0.0.1", address.getAddress().getHostAddress());

        InetSocketAddress ip6 = MAPPER.readValue(
                quote("2001:db8:85a3:8d3:1319:8a2e:370:7348"), InetSocketAddress.class);
        assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ip6.getAddress().getHostAddress());

        InetSocketAddress ip6port = MAPPER.readValue(
                quote("[2001:db8:85a3:8d3:1319:8a2e:370:7348]:443"), InetSocketAddress.class);
        assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ip6port.getAddress().getHostAddress());
        assertEquals(443, ip6port.getPort());

        
        final String HOST = "www.google.com";
        address = MAPPER.readValue(quote(HOST), InetSocketAddress.class);
        assertEquals(HOST, address.getHostName());

        final String HOST_AND_PORT = HOST+":80";
        address = MAPPER.readValue(quote(HOST_AND_PORT), InetSocketAddress.class);
        assertEquals(HOST, address.getHostName());
        assertEquals(80, address.getPort());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testClass
    public void testClass() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        assertSame(String.class, mapper.readValue(quote("java.lang.String"), Class.class));

        
        assertSame(Boolean.TYPE, mapper.readValue(quote("boolean"), Class.class));
        assertSame(Byte.TYPE, mapper.readValue(quote("byte"), Class.class));
        assertSame(Short.TYPE, mapper.readValue(quote("short"), Class.class));
        assertSame(Character.TYPE, mapper.readValue(quote("char"), Class.class));
        assertSame(Integer.TYPE, mapper.readValue(quote("int"), Class.class));
        assertSame(Long.TYPE, mapper.readValue(quote("long"), Class.class));
        assertSame(Float.TYPE, mapper.readValue(quote("float"), Class.class));
        assertSame(Double.TYPE, mapper.readValue(quote("double"), Class.class));
        assertSame(Void.TYPE, mapper.readValue(quote("void"), Class.class));
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testClassWithParams
    public void testClassWithParams() throws IOException
    {
        String json = MAPPER.writeValueAsString(new ParamClassBean("Foobar"));

        ParamClassBean result = MAPPER.readValue(json, ParamClassBean.class);
        assertEquals("Foobar", result.name);
        assertSame(String.class, result.clazz);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testEmptyStringForWrappers
    public void testEmptyStringForWrappers() throws IOException
    {
        WrappersBean bean;

        
        bean = MAPPER.readValue("{\"booleanValue\":\"\"}", WrappersBean.class);
        assertNull(bean.booleanValue);
        bean = MAPPER.readValue("{\"byteValue\":\"\"}", WrappersBean.class);
        assertNull(bean.byteValue);

        
        bean = MAPPER.readValue("{\"charValue\":\"\"}", WrappersBean.class);
        assertNull(bean.charValue);

        bean = MAPPER.readValue("{\"shortValue\":\"\"}", WrappersBean.class);
        assertNull(bean.shortValue);
        bean = MAPPER.readValue("{\"intValue\":\"\"}", WrappersBean.class);
        assertNull(bean.intValue);
        bean = MAPPER.readValue("{\"longValue\":\"\"}", WrappersBean.class);
        assertNull(bean.longValue);
        bean = MAPPER.readValue("{\"floatValue\":\"\"}", WrappersBean.class);
        assertNull(bean.floatValue);
        bean = MAPPER.readValue("{\"doubleValue\":\"\"}", WrappersBean.class);
        assertNull(bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testEmptyStringForPrimitives
    public void testEmptyStringForPrimitives() throws IOException
    {
        PrimitivesBean bean;
        bean = MAPPER.readValue("{\"booleanValue\":\"\"}", PrimitivesBean.class);
        assertFalse(bean.booleanValue);
        bean = MAPPER.readValue("{\"byteValue\":\"\"}", PrimitivesBean.class);
        assertEquals((byte) 0, bean.byteValue);
        bean = MAPPER.readValue("{\"charValue\":\"\"}", PrimitivesBean.class);
        assertEquals((char) 0, bean.charValue);
        bean = MAPPER.readValue("{\"shortValue\":\"\"}", PrimitivesBean.class);
        assertEquals((short) 0, bean.shortValue);
        bean = MAPPER.readValue("{\"intValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0, bean.intValue);
        bean = MAPPER.readValue("{\"longValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0L, bean.longValue);
        bean = MAPPER.readValue("{\"floatValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0f, bean.floatValue);
        bean = MAPPER.readValue("{\"doubleValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0, bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testUntypedWithJsonArrays
    public void testUntypedWithJsonArrays() throws Exception
    {
        
        Object ob = MAPPER.readValue("[1]", Object.class);
        assertTrue(ob instanceof List<?>);

        
        MAPPER.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        ob = MAPPER.readValue("[1]", Object.class);
        assertEquals(Object[].class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testLongToBoolean
    public void testLongToBoolean() throws Exception
    {
        long value = 1L + Integer.MAX_VALUE;
        BooleanBean b = MAPPER.readValue("{\"primitive\" : "+value+", \"wrapper\":"+value+", \"ctor\":"+value+"}",
                    BooleanBean.class);
        assertEquals(Boolean.TRUE, b.wrapper);
        assertTrue(b.primitive);
        assertEquals(Boolean.TRUE, b.ctor);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCharset
    public void testCharset() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        assertSame(UTF8, MAPPER.readValue(quote("UTF-8"), Charset.class));
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testStackTraceElement
    public void testStackTraceElement() throws Exception
    {
        StackTraceElement elem = null;
        try {
            throw new IllegalStateException();
        } catch (Exception e) {
            elem = e.getStackTrace()[0];
        }
        String json = MAPPER.writeValueAsString(elem);
        StackTraceElement back = MAPPER.readValue(json, StackTraceElement.class);
        
        assertEquals("testStackTraceElement", back.getMethodName());
        assertEquals(elem.getLineNumber(), back.getLineNumber());
        assertEquals(elem.getClassName(), back.getClassName());
        assertEquals(elem.isNativeMethod(), back.isNativeMethod());
        assertTrue(back.getClassName().endsWith("TestJdkTypes"));
        assertFalse(back.isNativeMethod());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testByteBuffer
    public void testByteBuffer() throws Exception
    {
        byte[] INPUT = new byte[] { 1, 3, 9, -1, 6 };
        String exp = MAPPER.writeValueAsString(INPUT);
        ByteBuffer result = MAPPER.readValue(exp,  ByteBuffer.class); 
        assertNotNull(result);
        assertEquals(INPUT.length, result.remaining());
        for (int i = 0; i < INPUT.length; ++i) {
            assertEquals(INPUT[i], result.get());
        }
        assertEquals(0, result.remaining());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testStringBuilder
    public void testStringBuilder() throws Exception
    {
        StringBuilder sb = MAPPER.readValue(quote("abc"), StringBuilder.class);
        assertEquals("abc", sb.toString());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testStackTraceElementWithCustom
    public void testStackTraceElementWithCustom() throws Exception
    {
        
        StackTraceBean bean = MAPPER.readValue(aposToQuotes("{'Location':'foobar'}"),
                StackTraceBean.class);
        assertNotNull(bean);
        assertNotNull(bean.location);
        assertEquals(StackTraceBean.NUM, bean.location.getLineNumber());

        
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StackTraceElement.class, new MyStackTraceElementDeserializer());
        mapper.registerModule(module);
        
        StackTraceElement elem = mapper.readValue("123", StackTraceElement.class);
        assertNotNull(elem);
        assertEquals(StackTraceBean.NUM, elem.getLineNumber());
 
        
        
        IOException ioe = mapper.readValue(aposToQuotes("{'stackTrace':[ 123, 456 ]}"),
                IOException.class);
        assertNotNull(ioe);
        StackTraceElement[] traces = ioe.getStackTrace();
        assertNotNull(traces);
        assertEquals(2, traces.length);
        assertEquals(StackTraceBean.NUM, traces[0].getLineNumber());
        assertEquals(StackTraceBean.NUM, traces[1].getLineNumber());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testSingleElementArray
    public void testSingleElementArray() throws Exception {
        final int intTest = 932832;
        final double doubleTest = 32.3234;
        final long longTest = 2374237428374293423L;
        final short shortTest = (short) intTest;
        final float floatTest = 84.3743f;
        final byte byteTest = (byte) 43;
        final char charTest = 'c';

        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        final int intValue = mapper.readValue(asArray(intTest), Integer.TYPE);
        assertEquals(intTest, intValue);
        final Integer integerWrapperValue = mapper.readValue(asArray(Integer.valueOf(intTest)), Integer.class);
        assertEquals(Integer.valueOf(intTest), integerWrapperValue);

        final double doubleValue = mapper.readValue(asArray(doubleTest), Double.class);
        assertEquals(doubleTest, doubleValue);
        final Double doubleWrapperValue = mapper.readValue(asArray(Double.valueOf(doubleTest)), Double.class);
        assertEquals(Double.valueOf(doubleTest), doubleWrapperValue);

        final long longValue = mapper.readValue(asArray(longTest), Long.TYPE);
        assertEquals(longTest, longValue);
        final Long longWrapperValue = mapper.readValue(asArray(Long.valueOf(longTest)), Long.class);
        assertEquals(Long.valueOf(longTest), longWrapperValue);

        final short shortValue = mapper.readValue(asArray(shortTest), Short.TYPE);
        assertEquals(shortTest, shortValue);
        final Short shortWrapperValue = mapper.readValue(asArray(Short.valueOf(shortTest)), Short.class);
        assertEquals(Short.valueOf(shortTest), shortWrapperValue);

        final float floatValue = mapper.readValue(asArray(floatTest), Float.TYPE);
        assertEquals(floatTest, floatValue);
        final Float floatWrapperValue = mapper.readValue(asArray(Float.valueOf(floatTest)), Float.class);
        assertEquals(Float.valueOf(floatTest), floatWrapperValue);

        final byte byteValue = mapper.readValue(asArray(byteTest), Byte.TYPE);
        assertEquals(byteTest, byteValue);
        final Byte byteWrapperValue = mapper.readValue(asArray(Byte.valueOf(byteTest)), Byte.class);
        assertEquals(Byte.valueOf(byteTest), byteWrapperValue);

        final char charValue = mapper.readValue(asArray(quote(String.valueOf(charTest))), Character.TYPE);
        assertEquals(charTest, charValue);
        final Character charWrapperValue = mapper.readValue(asArray(quote(String.valueOf(charTest))), Character.class);
        assertEquals(Character.valueOf(charTest), charWrapperValue);

        final boolean booleanTrueValue = mapper.readValue(asArray(true), Boolean.TYPE);
        assertTrue(booleanTrueValue);

        final boolean booleanFalseValue = mapper.readValue(asArray(false), Boolean.TYPE);
        assertFalse(booleanFalseValue);

        final Boolean booleanWrapperTrueValue = mapper.readValue(asArray(Boolean.valueOf(true)), Boolean.class);
        assertEquals(Boolean.TRUE, booleanWrapperTrueValue);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testSingleElementArrayException
    public void testSingleElementArrayException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("[42]", Integer.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42]", Integer.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[42.273]", Double.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42.2723]", Double.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[42342342342342]", Long.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42342342342342342]", Long.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[42]", Short.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42]", Short.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[327.2323]", Float.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[82.81902]", Float.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[22]", Byte.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[22]", Byte.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("['d']", Character.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("['d']", Character.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[true]", Boolean.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[true]", Boolean.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testMultiValueArrayException
    public void testMultiValueArrayException() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
            mapper.readValue("[42,42]", Integer.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42,42]", Integer.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[42.273,42.273]", Double.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42.2723,42.273]", Double.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[42342342342342,42342342342342]", Long.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42342342342342342,42342342342342]", Long.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[42,42]", Short.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42,42]", Short.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[327.2323,327.2323]", Float.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[82.81902,327.2323]", Float.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[22,23]", Byte.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[22,23]", Byte.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue(asArray(quote("c") + ","  + quote("d")), Character.class);
            
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue(asArray(quote("c") + ","  + quote("d")), Character.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[true,false]", Boolean.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[true,false]", Boolean.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUntypedMap
    public void testUntypedMap() throws Exception
    {
        
        String JSON = "{ \"foo\" : \"bar\", \"crazy\" : true, \"null\" : null }";

        
        @SuppressWarnings("unchecked")
        Map<String,Object> result = (Map<String,Object>)MAPPER.readValue(JSON, Object.class);
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);

        assertEquals(3, result.size());

        assertEquals("bar", result.get("foo"));
        assertEquals(Boolean.TRUE, result.get("crazy"));
        assertNull(result.get("null"));

        
        assertNull(result.get("bar"));
        assertNull(result.get(3));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testBigUntypedMap
    public void testBigUntypedMap() throws Exception
    {
        Map<String,Object> map = new LinkedHashMap<String,Object>();
        for (int i = 0; i < 1100; ++i) {
            if ((i & 1) == 0) {
                map.put(String.valueOf(i), Integer.valueOf(i));
            } else {
                Map<String,Object> map2 = new LinkedHashMap<String,Object>();
                map2.put("x", Integer.valueOf(i));
                map.put(String.valueOf(i), map2);
            }
        }
        String json = MAPPER.writeValueAsString(map);
        Object bound = MAPPER.readValue(json, Object.class);
        assertEquals(map, bound);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUntypedMap2
    public void testUntypedMap2() throws Exception
    {
        
        String JSON = "{ \"a\" : \"x\" }";

        @SuppressWarnings("unchecked")
        HashMap<String,Object> result =  MAPPER.readValue(JSON, HashMap.class);
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);

        assertEquals(1, result.size());

        assertEquals("x", result.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUntypedMap3
    public void testUntypedMap3() throws Exception
    {
        String JSON = "{\"a\":[{\"a\":\"b\"},\"value\"]}";
        Map<?,?> result = MAPPER.readValue(JSON, Map.class);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(1, result.size());
        Object ob = result.get("a");
        assertNotNull(ob);
        Collection<?> list = (Collection<?>)ob;
        assertEquals(2, list.size());

        JSON = "{ \"var1\":\"val1\", \"var2\":\"val2\", "
            +"\"subvars\": ["
            +" {  \"subvar1\" : \"subvar2\", \"x\" : \"y\" }, "
            +" { \"a\":1 } ]"
            +" }"
            ;
        result = MAPPER.readValue(JSON, Map.class);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(3, result.size());
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testSpecialMap
    public void testSpecialMap() throws IOException
    {
       final ObjectWrapperMap map = MAPPER.readValue(UNTYPED_MAP_JSON, ObjectWrapperMap.class);
       assertNotNull(map);
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testGenericMap
    public void testGenericMap() throws IOException
    {
        final Map<String, ObjectWrapper> map = MAPPER.readValue
            (UNTYPED_MAP_JSON,
             new TypeReference<Map<String, ObjectWrapper>>() { });
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        Map<?,?> result = m.readValue(quote(""), Map.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testExactStringIntMap
    public void testExactStringIntMap() throws Exception
    {
        
        String JSON = "{ \"foo\" : 13, \"bar\" : -39, \n \"\" : 0 }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<HashMap<String,Integer>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(3, result.size());

        assertEquals(Integer.valueOf(13), result.get("foo"));
        assertEquals(Integer.valueOf(-39), result.get("bar"));
        assertEquals(Integer.valueOf(0), result.get(""));
        assertNull(result.get("foobar"));
        assertNull(result.get(" "));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testIntBooleanMap
    public void testIntBooleanMap() throws Exception
    {
        
        String JSON = "{ \"1\" : true, \"-1\" : false }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<HashMap<Integer,Boolean>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals(Boolean.TRUE, result.get(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, result.get(Integer.valueOf(-1)));
        assertNull(result.get("foobar"));
        assertNull(result.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testExactStringStringMap
    public void testExactStringStringMap() throws Exception
    {
        
        String JSON = "{ \"a\" : \"b\" }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<TreeMap<String,String>>() { });

        assertNotNull(result);
        assertEquals(TreeMap.class, result.getClass());
        assertEquals(1, result.size());

        assertEquals("b", result.get("a"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testGenericStringIntMap
    public void testGenericStringIntMap() throws Exception
    {
        
        String JSON = "{ \"a\" : 1, \"b\" : 2, \"c\" : -99 }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<Map<String,Integer>>() { });
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(3, result.size());

        assertEquals(Integer.valueOf(-99), result.get("c"));
        assertEquals(Integer.valueOf(2), result.get("b"));
        assertEquals(Integer.valueOf(1), result.get("a"));

        assertNull(result.get(""));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapFromEmptyArray
    public void testMapFromEmptyArray() throws Exception
    {
        final String JSON = "  [\n]";
        assertFalse(MAPPER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(JSON, Map.class);
            fail("Should not accept Empty Array for Map by default");
        } catch (JsonProcessingException e) {
            verifyException(e, "START_ARRAY token");
        }
        
        ObjectReader r = MAPPER.readerFor(Map.class)
                .with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        Map<?,?> result = r.readValue(JSON);
        assertNull(result);

        EnumMap<?,?> result2 = r.forType(new TypeReference<EnumMap<Key,String>>() { })
                .readValue(JSON);
        assertNull(result2);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testEnumMap
    public void testEnumMap() throws Exception
    {
        String JSON = "{ \"KEY1\" : \"\", \"WHATEVER\" : null }";

        
        EnumMap<Key,String> result = MAPPER.readValue
            (JSON, new TypeReference<EnumMap<Key,String>>() { });

        assertNotNull(result);
        assertEquals(EnumMap.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals("", result.get(Key.KEY1));
        
        assertTrue(result.containsKey(Key.WHATEVER));
        assertNull(result.get(Key.WHATEVER));

        
        assertFalse(result.containsKey(Key.KEY2));
        assertNull(result.get(Key.KEY2));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapWithEnums
    public void testMapWithEnums() throws Exception
    {
        String JSON = "{ \"KEY2\" : \"WHATEVER\" }";

        
        Map<Enum<?>,Enum<?>> result = MAPPER.readValue
            (JSON, new TypeReference<Map<Key,Key>>() { });

        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(1, result.size());

        assertEquals(Key.WHATEVER, result.get(Key.KEY2));
        assertNull(result.get(Key.WHATEVER));
        assertNull(result.get(Key.KEY1));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testEnumPolymorphicSerializationTest
    public void testEnumPolymorphicSerializationTest() throws Exception 
    {
        ObjectMapper mapper = new ObjectMapper();
        List<ITestType> testTypesList = new ArrayList<ITestType>();
        testTypesList.add(ConcreteType.ONE);
        testTypesList.add(ConcreteType.TWO);
        ListContainer listContainer = new ListContainer();
        listContainer.testTypes = testTypesList;
        String json = mapper.writeValueAsString(listContainer);
        listContainer = mapper.readValue(json, ListContainer.class);
        EnumMapContainer enumMapContainer = new EnumMapContainer();
        EnumMap<KeyEnum,ITestType> testTypesMap = new EnumMap<KeyEnum,ITestType>(KeyEnum.class);
        testTypesMap.put(KeyEnum.A, ConcreteType.ONE);
        testTypesMap.put(KeyEnum.B, ConcreteType.TWO);
        enumMapContainer.testTypes = testTypesMap;
        
        json = mapper.writeValueAsString(enumMapContainer);
        enumMapContainer = mapper.readValue(json, EnumMapContainer.class);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testDateMap
    public void testDateMap() throws Exception
    {
    	 Date date1=new Date(123456000L);
    	 DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
         
    	 String JSON = "{ \""+  fmt.format(date1)+"\" : \"\", \""+new Date(0).getTime()+"\" : null }";
    	 HashMap<Date,String> result=  MAPPER.readValue
    	            (JSON, new TypeReference<HashMap<Date,String>>() { });
    	 
    	 assertNotNull(result);
    	 assertEquals(HashMap.class, result.getClass());
    	 assertEquals(2, result.size());
    	 
    	 assertTrue(result.containsKey(date1));
    	 assertEquals("", result.get(new Date(123456000L)));

    	 assertTrue(result.containsKey(new Date(0)));
    	 assertNull(result.get(new Date(0)));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testCalendarMap
    public void testCalendarMap() throws Exception
    {
        
        TimeZone tz = MAPPER.getSerializationConfig().getTimeZone();        
        Calendar c = Calendar.getInstance(tz);

        c.setTimeInMillis(123456000L);
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        String JSON = "{ \""+fmt.format(c.getTime())+"\" : \"\", \""+new Date(0).getTime()+"\" : null }";
        HashMap<Calendar,String> result = MAPPER.readValue
                (JSON, new TypeReference<HashMap<Calendar,String>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(2, result.size());

        assertTrue(result.containsKey(c));
        assertEquals("", result.get(c));
        c.setTimeInMillis(0);
        assertTrue(result.containsKey(c));
        assertNull(result.get(c));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUUIDKeyMap
    public void testUUIDKeyMap() throws Exception
    {
         UUID key = UUID.nameUUIDFromBytes("foobar".getBytes("UTF-8"));
         String JSON = "{ \""+key+"\":4}";
         Map<UUID,Object> result = MAPPER.readValue(JSON, new TypeReference<Map<UUID,Object>>() { });
         assertNotNull(result);
         assertEquals(1, result.size());
         Object ob = result.keySet().iterator().next();
         assertNotNull(ob);
         assertEquals(UUID.class, ob.getClass());
         assertEquals(key, ob);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testLocaleKeyMap
    public void testLocaleKeyMap() throws Exception {
        Locale key = Locale.CHINA;
        String JSON = "{ \"" + key + "\":4}";
        Map<Locale, Object> result = MAPPER.readValue(JSON, new TypeReference<Map<Locale, Object>>() {
        });
        assertNotNull(result);
        assertEquals(1, result.size());
        Object ob = result.keySet().iterator().next();
        assertNotNull(ob);
        assertEquals(Locale.class, ob.getClass());
        assertEquals(key, ob);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testCurrencyKeyMap
    public void testCurrencyKeyMap() throws Exception {
        Currency key = Currency.getInstance("USD");
        String JSON = "{ \"" + key + "\":4}";
        Map<Currency, Object> result = MAPPER.readValue(JSON, new TypeReference<Map<Currency, Object>>() {
        });
        assertNotNull(result);
        assertEquals(1, result.size());
        Object ob = result.keySet().iterator().next();
        assertNotNull(ob);
        assertEquals(Currency.class, ob.getClass());
        assertEquals(key, ob);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testKeyWithCreator
    public void testKeyWithCreator() throws Exception
    {
        
        KeyType key = MAPPER.readValue(quote("abc"), KeyType.class);
        assertEquals("abc", key.value);

        Map<KeyType,Integer> map = MAPPER.readValue("{\"foo\":3}", new TypeReference<Map<KeyType,Integer>>() {} );
        assertEquals(1, map.size());
        key = map.keySet().iterator().next();
        assertEquals("foo", key.value);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testClassKeyMap
    public void testClassKeyMap() throws Exception {
        ClassStringMap map = MAPPER.readValue(aposToQuotes("{'java.lang.String':'foo'}"),
                ClassStringMap.class);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("foo", map.get(String.class));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapWithDeserializer
    public void testMapWithDeserializer() throws Exception
    {
        CustomMap result = MAPPER.readValue(quote("xyz"), CustomMap.class);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("x"));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapEntrySimpleTypes
    public void testMapEntrySimpleTypes() throws Exception
    {
        List<Map.Entry<String,Long>> stuff = MAPPER.readValue(aposToQuotes("[{'a':15},{'b':42}]"),
                new TypeReference<List<Map.Entry<String,Long>>>() { });
        assertNotNull(stuff);
        assertEquals(2, stuff.size());
        assertNotNull(stuff.get(1));
        assertEquals("b", stuff.get(1).getKey());
        assertEquals(Long.valueOf(42), stuff.get(1).getValue());
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapEntryWithStringBean
    public void testMapEntryWithStringBean() throws Exception
    {
        List<Map.Entry<Integer,StringWrapper>> stuff = MAPPER.readValue(aposToQuotes("[{'28':'Foo'},{'13':'Bar'}]"),
                new TypeReference<List<Map.Entry<Integer,StringWrapper>>>() { });
        assertNotNull(stuff);
        assertEquals(2, stuff.size());
        assertNotNull(stuff.get(1));
        assertEquals(Integer.valueOf(13), stuff.get(1).getKey());
        
        StringWrapper sw = stuff.get(1).getValue();
        assertEquals("Bar", sw.str);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapEntryFail
    public void testMapEntryFail() throws Exception
    {
        try {
             MAPPER.readValue(aposToQuotes("[{'28':'Foo','13':'Bar'}]"),
                    new TypeReference<List<Map.Entry<Integer,StringWrapper>>>() { });
            fail("Should not have passed");
        } catch (Exception e) {
            verifyException(e, "more than one entry in JSON");
        }
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testReadProperties
    public void testReadProperties() throws Exception
    {
        Properties props = MAPPER.readValue(aposToQuotes("{'a':'foo', 'b':123, 'c':true}"),
                Properties.class);
        assertEquals(3, props.size());
        assertEquals("foo", props.getProperty("a"));
        assertEquals("123", props.getProperty("b"));
        assertEquals("true", props.getProperty("c"));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testSingletonMapRoundtrip
    public void testSingletonMapRoundtrip() throws Exception
    {
        final TypeReference<?> type = new TypeReference<Map<String,IntWrapper>>() { };

        String json = MAPPER.writeValueAsString(Collections.singletonMap("value", new IntWrapper(5)));
        Map<String,IntWrapper> result = MAPPER.readValue(json, type);
        assertNotNull(result);
        assertEquals(1, result.size());
        IntWrapper w = result.get("value");
        assertNotNull(w);
        assertEquals(5, w.i);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapError
    public void testMapError() throws Exception
    {
        try {
            Object result = MAPPER.readValue("[ 1, 2 ]", 
                                             new TypeReference<Map<String,String>>() { });
            fail("Expected an exception, but got result value: "+result);
        } catch (JsonMappingException jex) {
            verifyException(jex, "START_ARRAY");
        }
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testNoCtorMap
    public void testNoCtorMap() throws Exception
    {
        try {
            BrokenMap result = MAPPER.readValue("{ \"a\" : 3 }", BrokenMap.class);
            
            assertNull(result);
        } catch (JsonMappingException e) {
            
            verifyException(e, "no default constructor found");
        }
    }

// com.fasterxml.jackson.databind.deser.TestNullHandling::testAnySetterNulls
    public void testAnySetterNulls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(String.class, new FunnyNullDeserializer());
        mapper.registerModule(module);

        String fieldName = "fieldName";
        String nullValue = "{\""+fieldName+"\":null}";

        
        AnySetter result = mapper.readValue(nullValue, AnySetter.class);

        assertEquals(1, result.getAny().size());
        assertNotNull(result.getAny().get(fieldName));
        assertEquals("funny", result.getAny().get(fieldName));

        
        ObjectReader reader = mapper.readerFor(AnySetter.class);
        result = reader.readValue(nullValue);

        assertEquals(1, result.getAny().size());
        assertNotNull(result.getAny().get(fieldName));
        assertEquals("funny", result.getAny().get(fieldName));
    }

// com.fasterxml.jackson.databind.deser.TestNullHandling::testCustomRootNulls
    public void testCustomRootNulls() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(String.class, new FunnyNullDeserializer());
        mapper.registerModule(module);

        
        String str = mapper.readValue("null", String.class);
        assertNotNull(str);
        assertEquals("funny", str);
        
        
        ObjectReader reader = mapper.readerFor(String.class);
        str = reader.readValue("null");
        assertNotNull(str);
        assertEquals("funny", str);
    }

// com.fasterxml.jackson.databind.deser.TestNullHandling::testListOfNulls
    public void testListOfNulls() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(String.class, new FunnyNullDeserializer());
        mapper.registerModule(module);

        List<String> list = Arrays.asList("funny");
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);

        
        List<?> deser = mapper.readValue("[null]", type);
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals(list.get(0), deser.get(0));

        
        ObjectReader reader = mapper.readerFor(type);
        deser = reader.readValue("[null]");
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals(list.get(0), deser.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestNullHandling::testMapOfNulls
    public void testMapOfNulls() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(String.class, new FunnyNullDeserializer());
        mapper.registerModule(module);

        JavaType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);
        
        Map<?,?> deser = mapper.readValue("{\"key\":null}", type);
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals("funny", deser.get("key"));

        
        ObjectReader reader = mapper.readerFor(type);
        deser = reader.readValue("{\"key\":null}");
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals("funny", deser.get("key"));
    }

// com.fasterxml.jackson.databind.deser.TestObjectOrArrayDeserialization::testObjectCase
    public void testObjectCase() throws Exception {
        ArrayOrObject arrayOrObject = new ObjectMapper().readValue("{}", ArrayOrObject.class);
        assertNull("expected objects field to be null", arrayOrObject.objects);
        assertNotNull("expected object field not to be null", arrayOrObject.object);
    }

// com.fasterxml.jackson.databind.deser.TestObjectOrArrayDeserialization::testEmptyArrayCase
    public void testEmptyArrayCase() throws Exception {
        ArrayOrObject arrayOrObject = new ObjectMapper().readValue("[]", ArrayOrObject.class);
        assertNotNull("expected objects field not to be null", arrayOrObject.objects);
        assertTrue("expected objects field to be an empty list", arrayOrObject.objects.isEmpty());
        assertNull("expected object field to be null", arrayOrObject.object);
    }

// com.fasterxml.jackson.databind.deser.TestObjectOrArrayDeserialization::testNotEmptyArrayCase
    public void testNotEmptyArrayCase() throws Exception {
        ArrayOrObject arrayOrObject = new ObjectMapper().readValue("[{}, {}]", ArrayOrObject.class);
        assertNotNull("expected objects field not to be null", arrayOrObject.objects);
        assertEquals("expected objects field to have size 2", 2, arrayOrObject.objects.size());
        assertNull("expected object field to be null", arrayOrObject.object);
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testSpecialization
    public void testSpecialization() throws Exception
    {
        ArrayListBean bean = MAPPER.readValue
            ("{\"list\":[\"a\",\"b\",\"c\"]}", ArrayListBean.class);
        assertNotNull(bean.list);
        assertEquals(3, bean.list.size());
        assertEquals(ArrayList.class, bean.list.getClass());
        assertEquals("a", bean.list.get(0));
        assertEquals("b", bean.list.get(1));
        assertEquals("c", bean.list.get(2));
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testOverride
    public void testOverride() throws Exception
    {
        WasNumberBean bean = MAPPER.readValue
            ("{\"value\" : \"abc\"}", WasNumberBean.class);
        assertNotNull(bean);
        assertEquals("abc", bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testConflictResolution
    public void testConflictResolution() throws Exception
    {
        Overloaded739 bean = MAPPER.readValue
                ("{\"value\":\"abc\"}", Overloaded739.class);
        assertNotNull(bean);
        assertEquals("abc", bean._value);
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testSetterConflict
    public void testSetterConflict() throws Exception
    {
    	try {    		
    	MAPPER.readValue("{ }", ConflictBean.class);
    	} catch (Exception e) {
    	    verifyException(e, "Conflicting setter definitions");
    	}
    }

// com.fasterxml.jackson.databind.deser.TestPolymorphicDeserialization676::testDeSerFail
    public void testDeSerFail() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        MapContainer deserMapBad = createDeSerMapContainer(originMap, mapper);
        assertEquals(originMap, deserMapBad);
        assertEquals(originMap,
                mapper.readValue(mapper.writeValueAsString(originMap), MapContainer.class));
    }

// com.fasterxml.jackson.databind.deser.TestPolymorphicDeserialization676::testDeSerCorrect
    public void testDeSerCorrect() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", 1);
        
        assertEquals(new MapContainer(map),
                mapper.readValue(mapper.writeValueAsString(new MapContainer(map)),
                        MapContainer.class));

        MapContainer deserMapGood = createDeSerMapContainer(originMap, mapper);

        assertEquals(originMap, deserMapGood);
        assertEquals(new Date(TIMESTAMP), deserMapGood.map.get("DateValue"));

        assertEquals(originMap, mapper.readValue(mapper.writeValueAsString(originMap), MapContainer.class));
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessCollectionOk
    public void testSimpleSetterlessCollectionOk()
        throws Exception
    {
        CollectionBean result = new ObjectMapper().readValue
            ("{\"values\":[ \"abc\", \"def\" ]}", CollectionBean.class);
        List<String> l = result._values;
        assertEquals(2, l.size());
        assertEquals("abc", l.get(0));
        assertEquals("def", l.get(1));
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessCollectionFailure
    public void testSimpleSetterlessCollectionFailure()
        throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        assertTrue(m.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS));
        m.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        assertFalse(m.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS));

        
        try {
            m.readValue
                ("{\"values\":[ \"abc\", \"def\" ]}", CollectionBean.class);
            fail("Expected an exception");
        } catch (JsonMappingException e) {
            
            verifyException(e, "Unrecognized field");
        }
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessMapOk
    public void testSimpleSetterlessMapOk()
        throws Exception
    {
        MapBean result = new ObjectMapper().readValue
            ("{\"values\":{ \"a\": 15, \"b\" : -3 }}", MapBean.class);
        Map<String,Integer> m = result._values;
        assertEquals(2, m.size());
        assertEquals(Integer.valueOf(15), m.get("a"));
        assertEquals(Integer.valueOf(-3), m.get("b"));
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessMapFailure
    public void testSimpleSetterlessMapFailure()
        throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        
        try {
            m.readValue
                ("{\"values\":{ \"a\":3 }}", MapBean.class);
            fail("Expected an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field");
        }
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSetterlessPrecedence
    public void testSetterlessPrecedence() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_GETTERS_AS_SETTERS, true);
        Dual value = m.readValue("{\"list\":[1,2,3]}, valueType)", Dual.class);
        assertNotNull(value);
        assertEquals(3, value.values.size());
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBooleanPrimitive
    public void testBooleanPrimitive() throws Exception
    {
        
        BooleanBean result = MAPPER.readValue(new StringReader("{\"v\":true}"), BooleanBean.class);
        assertTrue(result._v);
        
        result = MAPPER.readValue(new StringReader("{\"v\":null}"), BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);

        
        boolean[] array = MAPPER.readValue(new StringReader("[ null ]"), boolean[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertFalse(array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue(new StringReader("{\"v\":[true]}"), BooleanBean.class);
        assertTrue(result._v);
        
        try {
            mapper.readValue(new StringReader("[{\"v\":[true,true]}]"), BooleanBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue(new StringReader("{\"v\":[null]}"), BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        
        result = mapper.readValue(new StringReader("[{\"v\":[null]}]"), BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        
        array = mapper.readValue(new StringReader("[ [ null ] ]"), boolean[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertFalse(array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testIntPrimitive
    public void testIntPrimitive() throws Exception
    {
        
        IntBean result = MAPPER.readValue(new StringReader("{\"v\":3}"), IntBean.class);
        assertEquals(3, result._v);
        
        result = MAPPER.readValue(new StringReader("{\"v\":null}"), IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        
        int[] array = MAPPER.readValue(new StringReader("[ null ]"), int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue(new StringReader("{\"v\":[3]}"), IntBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue(new StringReader("{\"v\":[3]}"), IntBean.class);
        assertEquals(3, result._v);
        
        result = mapper.readValue(new StringReader("[{\"v\":[3]}]"), IntBean.class);
        assertEquals(3, result._v);
        
        try {
            mapper.readValue(new StringReader("[{\"v\":[3,3]}]"), IntBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue(new StringReader("{\"v\":[null]}"), IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        array = mapper.readValue(new StringReader("[ [ null ] ]"), int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testDoublePrimitive
    public void testDoublePrimitive() throws Exception
    {
        
        
        double value = 0.016;
        DoubleBean result = MAPPER.readValue(new StringReader("{\"v\":"+value+"}"), DoubleBean.class);
        assertEquals(value, result._v);
        
        result = MAPPER.readValue(new StringReader("{\"v\":null}"), DoubleBean.class);
        assertNotNull(result);
        assertEquals(0.0, result._v);

        
        double[] array = MAPPER.readValue(new StringReader("[ null ]"), double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0.0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue(new StringReader("{\"v\":[" + value + "]}"), DoubleBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue(new StringReader("{\"v\":[" + value + "]}"), DoubleBean.class);
        assertEquals(value, result._v);
        
        result = mapper.readValue(new StringReader("[{\"v\":[" + value + "]}]"), DoubleBean.class);
        assertEquals(value, result._v);
        
        try {
            mapper.readValue(new StringReader("[{\"v\":[" + value + "," + value + "]}]"), DoubleBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue(new StringReader("{\"v\":[null]}"), DoubleBean.class);
        assertNotNull(result);
        assertEquals(0d, result._v);

        array = mapper.readValue(new StringReader("[ [ null ] ]"), double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0d, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testDoublePrimitiveNonNumeric
    public void testDoublePrimitiveNonNumeric() throws Exception
    {
        
        
        double value = Double.POSITIVE_INFINITY;
        DoubleBean result = MAPPER.readValue(new StringReader("{\"v\":\""+value+"\"}"), DoubleBean.class);
        assertEquals(value, result._v);
        
        
        double[] array = MAPPER.readValue(new StringReader("[ \"Infinity\" ]"), double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Double.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testFloatPrimitiveNonNumeric
    public void testFloatPrimitiveNonNumeric() throws Exception
    {
        
        float value = Float.POSITIVE_INFINITY;
        FloatBean result = MAPPER.readValue(new StringReader("{\"v\":\""+value+"\"}"), FloatBean.class);
        assertEquals(value, result._v);
        
        
        float[] array = MAPPER.readValue(new StringReader("[ \"Infinity\" ]"), float[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Float.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testIntWithOverride
    public void testIntWithOverride() throws Exception
    {
        IntBean2 result = MAPPER.readValue(new StringReader("{\"v\":8}"), IntBean2.class);
        assertEquals(9, result._v);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testEmptyToNullCoercionForPrimitives
    public void testEmptyToNullCoercionForPrimitives() throws Exception {
        _testEmptyToNullCoercion(int.class, Integer.valueOf(0));
        _testEmptyToNullCoercion(long.class, Long.valueOf(0));
        _testEmptyToNullCoercion(double.class, Double.valueOf(0.0));
        _testEmptyToNullCoercion(float.class, Float.valueOf(0.0f));
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBooleanWrapper
    public void testBooleanWrapper() throws Exception
    {
        Boolean result = MAPPER.readValue(new StringReader("true"), Boolean.class);
        assertEquals(Boolean.TRUE, result);
        result = MAPPER.readValue(new StringReader("false"), Boolean.class);
        assertEquals(Boolean.FALSE, result);

        
        result = MAPPER.readValue(new StringReader("0"), Boolean.class);
        assertEquals(Boolean.FALSE, result);
        result = MAPPER.readValue(new StringReader("1"), Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testByteWrapper
    public void testByteWrapper() throws Exception
    {
        Byte result = MAPPER.readValue(new StringReader("   -42\t"), Byte.class);
        assertEquals(Byte.valueOf((byte)-42), result);

        
        result = MAPPER.readValue(new StringReader(" \"-12\""), Byte.class);
        assertEquals(Byte.valueOf((byte)-12), result);

        result = MAPPER.readValue(new StringReader(" 39.07"), Byte.class);
        assertEquals(Byte.valueOf((byte)39), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testShortWrapper
    public void testShortWrapper() throws Exception
    {
        Short result = MAPPER.readValue(new StringReader("37"), Short.class);
        assertEquals(Short.valueOf((short)37), result);

        
        result = MAPPER.readValue(new StringReader(" \"-1009\""), Short.class);
        assertEquals(Short.valueOf((short)-1009), result);

        result = MAPPER.readValue(new StringReader("-12.9"), Short.class);
        assertEquals(Short.valueOf((short)-12), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testCharacterWrapper
    public void testCharacterWrapper() throws Exception
    {
        
        Character result = MAPPER.readValue(new StringReader("\"a\""), Character.class);
        assertEquals(Character.valueOf('a'), result);

        
        result = MAPPER.readValue(new StringReader(" "+((int) 'X')), Character.class);
        assertEquals(Character.valueOf('X'), result);
        
        final CharacterWrapperBean wrapper = MAPPER.readValue(new StringReader("{\"v\":null}"), CharacterWrapperBean.class);
        assertNotNull(wrapper);
        assertNull(wrapper.getV());
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        try {
            mapper.readValue("{\"v\":null}", CharacterBean.class);
            fail("Attempting to deserialize a 'null' JSON reference into a 'char' property did not throw an exception");
        } catch (JsonMappingException exp) {
            
        }

        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);  
        final CharacterBean charBean = MAPPER.readValue(new StringReader("{\"v\":null}"), CharacterBean.class);
        assertNotNull(wrapper);
        assertEquals('\u0000', charBean.getV());
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testIntWrapper
    public void testIntWrapper() throws Exception
    {
        Integer result = MAPPER.readValue(new StringReader("   -42\t"), Integer.class);
        assertEquals(Integer.valueOf(-42), result);

        
        result = MAPPER.readValue(new StringReader(" \"-1200\""), Integer.class);
        assertEquals(Integer.valueOf(-1200), result);

        result = MAPPER.readValue(new StringReader(" 39.07"), Integer.class);
        assertEquals(Integer.valueOf(39), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testLongWrapper
    public void testLongWrapper() throws Exception
    {
        Long result = MAPPER.readValue(new StringReader("12345678901"), Long.class);
        assertEquals(Long.valueOf(12345678901L), result);

        
        result = MAPPER.readValue(new StringReader(" \"-9876\""), Long.class);
        assertEquals(Long.valueOf(-9876), result);

        result = MAPPER.readValue(new StringReader("1918.3"), Long.class);
        assertEquals(Long.valueOf(1918), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testFloatWrapper
    public void testFloatWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Float exp = Float.valueOf(str);
            Float result;

            if (NAN_STRING != str) {
                
                result = MAPPER.readValue(new StringReader(str), Float.class);
                assertEquals(exp, result);
            }

            
            result = MAPPER.readValue(new StringReader(" \""+str+"\""), Float.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testDoubleWrapper
    public void testDoubleWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Double exp = Double.valueOf(str);
            Double result;

            
            if (NAN_STRING != str) {
                result = MAPPER.readValue(str, Double.class);
            	assertEquals(exp, result);
            }
            
            result = MAPPER.readValue(new StringReader(" \""+str+"\""), Double.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBase64Variants
    public void testBase64Variants() throws Exception
    {
        final byte[] INPUT = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890X".getBytes("UTF-8");
        
        
        Assert.assertArrayEquals(INPUT, MAPPER.readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="),
                byte[].class));
        ObjectReader reader = MAPPER.readerFor(byte[].class);
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MIME_NO_LINEFEEDS).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="
        )));

        
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MIME).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1\\ndnd4eXoxMjM0NTY3ODkwWA=="
        )));
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MODIFIED_FOR_URL).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA"
        )));
        
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.PEM).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamts\\nbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="
        )));
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testSingleString
    public void testSingleString() throws Exception
    {
        String value = "FOO!";
        String result = MAPPER.readValue(new StringReader("\""+value+"\""), String.class);
        assertEquals(value, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testSingleStringWrapped
    public void testSingleStringWrapped() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        String value = "FOO!";
        try {
            mapper.readValue(new StringReader("[\""+value+"\"]"), String.class);
            fail("Exception not thrown when attempting to unwrap a single value 'String' array into a simple String");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
            mapper.readValue(new StringReader("[\""+value+"\",\""+value+"\"]"), String.class);
            fail("Exception not thrown when attempting to unwrap a single value 'String' array that contained more than one value into a simple String");
        } catch (JsonMappingException exp) {
            
        }
        
        String result = mapper.readValue(new StringReader("[\""+value+"\"]"), String.class);
        assertEquals(value, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testNull
    public void testNull() throws Exception
    {
        
        Object result = MAPPER.readValue("   null", Object.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testClass
    public void testClass() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();        
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);        
        
        Class<?> result = mapper.readValue(quote(String.class.getName()), Class.class);
        assertEquals(String.class, result);
        
        
        try {
            mapper.readValue("[" + quote(String.class.getName()) + "]", Class.class);
            fail("Did not throw exception when UNWRAP_SINGLE_VALUE_ARRAYS feature was disabled and attempted to read a Class array containing one element");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
           mapper.readValue("[" + quote(Object.class.getName()) + "," + quote(Object.class.getName()) +"]", Class.class); 
           fail("Did not throw exception when UNWRAP_SINGLE_VALUE_ARRAYS feature was enabled and attempted to read a Class array containing two elements");
        } catch (JsonMappingException exp) {
            
        }               
        result = mapper.readValue("[" + quote(String.class.getName()) + "]", Class.class);
        assertEquals(String.class, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBigDecimal
    public void testBigDecimal() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        BigDecimal value = new BigDecimal("0.001");
        BigDecimal result = mapper.readValue(value.toString(), BigDecimal.class);
        assertEquals(value, result);
        
        
        try {
            mapper.readValue("[" + value.toString() + "]", BigDecimal.class);
            fail("Exception was not thrown when attempting to read a single value array of BigDecimal when UNWRAP_SINGLE_VALUE_ARRAYS feature is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue("[" + value.toString() + "]", BigDecimal.class);
        assertEquals(value, result);
        
        try {
            mapper.readValue("[" + value.toString() + "," + value.toString() + "]", BigDecimal.class);
            fail("Exception was not thrown when attempting to read a muti value array of BigDecimal when UNWRAP_SINGLE_VALUE_ARRAYS feature is enabled");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBigInteger
    public void testBigInteger() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        BigInteger value = new BigInteger("-1234567890123456789012345567809");
        BigInteger result = mapper.readValue(new StringReader(value.toString()), BigInteger.class);
        assertEquals(value, result);
        
        
        try {
            mapper.readValue("[" + value.toString() + "]", BigInteger.class);
            fail("Exception was not thrown when attempting to read a single value array of BigInteger when UNWRAP_SINGLE_VALUE_ARRAYS feature is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue("[" + value.toString() + "]", BigInteger.class);
        assertEquals(value, result);
        
        try {
            mapper.readValue("[" + value.toString() + "," + value.toString() + "]", BigInteger.class);
            fail("Exception was not thrown when attempting to read a muti value array of BigInteger when UNWRAP_SINGLE_VALUE_ARRAYS feature is enabled");
        } catch (JsonMappingException exp) {
            
        }        
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testUUID
    public void testUUID() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        
        final String NULL_UUID = "00000000-0000-0000-0000-000000000000";
        
        for (String value : new String[] {
                "76e6d183-5f68-4afa-b94a-922c1fdb83f8",
                "540a88d1-e2d8-4fb1-9396-9212280d0a7f",
                "2c9e441d-1cd0-472d-9bab-69838f877574",
                "591b2869-146e-41d7-8048-e8131f1fdec5",
                "82994ac2-7b23-49f2-8cc5-e24cf6ed77be",
                "00000007-0000-0000-0000-000000000000"
        }) {
            
            mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
            
            UUID uuid = UUID.fromString(value);
            assertEquals(uuid,
                    mapper.readValue(quote(value), UUID.class));
            
            try {
                mapper.readValue("[" + quote(value) + "]", UUID.class);
                fail("Exception was not thrown when UNWRAP_SINGLE_VALUE_ARRAYS is disabled and attempted to read a single value array as a single element");
            } catch (JsonMappingException exp) {
                
            }
            
            mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
            
            assertEquals(uuid,
                    mapper.readValue("[" + quote(value) + "]", UUID.class));
            
            try {
                mapper.readValue("[" + quote(value) + "," + quote(value) + "]", UUID.class);
                fail("Exception was not thrown when UNWRAP_SINGLE_VALUE_ARRAYS is enabled and attempted to read a multi value array as a single element");
            } catch (JsonMappingException exp) {
                
            }
        }
        
        
        final String TEMPL = NULL_UUID;
        final String chars = "123456789abcdefABCDEF";

        for (int i = 0; i < chars.length(); ++i) {
            String value = TEMPL.replace('0', chars.charAt(i));
            assertEquals(UUID.fromString(value).toString(),
                    mapper.readValue(quote(value), UUID.class).toString());
        }

        
        String base64 = Base64Variants.getDefaultVariant().encode(new byte[16]);
        assertEquals(UUID.fromString(NULL_UUID),
                mapper.readValue(quote(base64), UUID.class));
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testUUIDInvalid
    public void testUUIDInvalid() throws Exception
    {
        
        try {
            MAPPER.readValue(quote("abcde"), UUID.class);
            fail("Should fail on invalid UUID string");
        } catch (InvalidFormatException e) {
            verifyException(e, "UUID has to be represented by standard");
        }
        try {
            MAPPER.readValue(quote("76e6d183-5f68-4afa-b94a-922c1fdb83fx"), UUID.class);
            fail("Should fail on invalid UUID string");
        } catch (InvalidFormatException e) {
            verifyException(e, "non-hex character 'x'");
        }
        
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testUUIDAux
    public void testUUIDAux() throws Exception
    {
        
        final UUID value = UUID.fromString("76e6d183-5f68-4afa-b94a-922c1fdb83f8");

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), UUID.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(value);
        assertSame(value, MAPPER.readValue(buf.asParser(), UUID.class));

        
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeLong(value.getMostSignificantBits());
        out.writeLong(value.getLeastSignificantBits());
        byte[] data = bytes.toByteArray();
        assertEquals(16, data.length);
        
        buf.writeObject(data);

        UUID value2 = MAPPER.readValue(buf.asParser(), UUID.class);
        
        assertEquals(value, value2);
        buf.close();
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testURL
    public void testURL() throws Exception
    {
        URL value = new URL("http://foo.com");
        assertEquals(value, MAPPER.readValue("\""+value.toString()+"\"", URL.class));

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(value);
        assertSame(value, MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testURI
    public void testURI() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        
        URI value = new URI("http://foo.com");
        assertEquals(value, mapper.readValue("\""+value.toString()+"\"", URI.class));
        
        
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {            
            assertEquals(value, mapper.readValue("[\""+value.toString()+"\"]", URI.class));
            fail("Did not throw exception for single value array when UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            assertEquals(value, mapper.readValue("[\""+value.toString()+"\",\""+value.toString()+"\"]", URI.class));
            fail("Did not throw exception for single value array when there were multiple values");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        assertEquals(value, mapper.readValue("[\""+value.toString()+"\"]", URI.class));

        
        value = mapper.readValue(quote(""), URI.class);
        assertNotNull(value);
        assertEquals(URI.create(""), value);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testSequenceOfInts
    public void testSequenceOfInts() throws Exception
    {
        final int NR_OF_INTS = 100;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NR_OF_INTS; ++i) {
            sb.append(" ");
            sb.append(i);
        }
        JsonParser jp = MAPPER.getFactory().createParser(sb.toString());
        for (int i = 0; i < NR_OF_INTS; ++i) {
            Integer result = MAPPER.readValue(jp, Integer.class);
            assertEquals(Integer.valueOf(i), result);
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.TestStatics::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        Bean result = m.readValue("{ \"x\":3}", Bean.class);
        assertEquals(3, result._x);
    }

// com.fasterxml.jackson.databind.deser.TestTimestampDeserialization::testTimestampUtil
    public void testTimestampUtil() throws Exception
    {
        long now = 123456789L;
        java.sql.Timestamp value = new java.sql.Timestamp(now);

        
        assertEquals(value, new ObjectMapper().readValue(""+now, java.sql.Timestamp.class));

        String dateStr = serializeTimestampAsString(value);
        java.sql.Timestamp result = new ObjectMapper().readValue("\""+dateStr+"\"", java.sql.Timestamp.class);

        assertEquals("Date: expect "+value+" ("+value.getTime()+"), got "+result+" ("+result.getTime()+")", value.getTime(), result.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestTimestampDeserialization::testTimestampUtilSingleElementArray
    public void testTimestampUtilSingleElementArray() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        long now = System.currentTimeMillis();
        java.sql.Timestamp value = new java.sql.Timestamp(now);

        
        assertEquals(value, mapper.readValue("["+now+"]", java.sql.Timestamp.class));

        String dateStr = serializeTimestampAsString(value);
        java.sql.Timestamp result = mapper.readValue("[\""+dateStr+"\"]", java.sql.Timestamp.class);

        assertEquals("Date: expect "+value+" ("+value.getTime()+"), got "+result+" ("+result.getTime()+")", value.getTime(), result.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testSampleDoc
    public void testSampleDoc() throws Exception
    {
        final String JSON = SAMPLE_DOC_JSON_SPEC;

        
        Object root = MAPPER.readValue(JSON, Object.class);

        assertType(root, Map.class);
        Map<?,?> rootMap = (Map<?,?>) root;
        assertEquals(1, rootMap.size());
        Map.Entry<?,?> rootEntry =  rootMap.entrySet().iterator().next();
        assertEquals("Image", rootEntry.getKey());
        Object image = rootEntry.getValue();
        assertType(image, Map.class);
        Map<?,?> imageMap = (Map<?,?>) image;
        assertEquals(5, imageMap.size());

        Object value = imageMap.get("Width");
        assertType(value, Integer.class);
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_WIDTH), value);

        value = imageMap.get("Height");
        assertType(value, Integer.class);
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_HEIGHT), value);

        assertEquals(SAMPLE_SPEC_VALUE_TITLE, imageMap.get("Title"));

        
        value = imageMap.get("Thumbnail");
        assertType(value, Map.class);
        Map<?,?> tnMap = (Map<?,?>) value;
        assertEquals(3, tnMap.size());

        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_HEIGHT), tnMap.get("Height"));
        
        assertEquals(SAMPLE_SPEC_VALUE_TN_WIDTH, tnMap.get("Width"));
        assertEquals(SAMPLE_SPEC_VALUE_TN_URL, tnMap.get("Url"));

        
        value = imageMap.get("IDs");
        assertType(value, List.class);
        List<Object> ids = (List<Object>) value;
        assertEquals(4, ids.size());
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID1), ids.get(0));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID2), ids.get(1));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID3), ids.get(2));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID4), ids.get(3));

        
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testNestedUntypes
    public void testNestedUntypes() throws IOException
    {
        
        Object root = MAPPER.readValue(aposToQuotes("{'a':3,'b':[1,2]}"),
                Object.class);
        assertTrue(root instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) root;
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(3), map.get("a"));
        Object ob = map.get("b");
        assertTrue(ob instanceof List<?>);
        List<?> l = (List<?>) ob;
        assertEquals(2, l.size());
        assertEquals(Integer.valueOf(2), l.get(1));
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testObjectSerializeWithLong
    public void testObjectSerializeWithLong() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT, As.PROPERTY);
        final long VALUE = 1337800584532L;

        String serialized = "{\"timestamp\":"+VALUE+"}";
        
        JsonNode deserialized = mapper.readTree(serialized);
        assertEquals(VALUE, deserialized.get("timestamp").asLong());
        
        Map<?,?> deserMap = mapper.readValue(serialized, Map.class);
        Number n = (Number) deserMap.get("timestamp");
        assertNotNull(n);
        assertSame(Long.class, n.getClass());
        assertEquals(Long.valueOf(VALUE), n);
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithCustomScalarDesers
    public void testUntypedWithCustomScalarDesers() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(String.class, new UCStringDeserializer());
        m.addDeserializer(Number.class, new CustomNumberDeserializer(13));
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        Object ob = mapper.readValue("{\"a\":\"b\", \"nr\":1 }", Object.class);
        assertTrue(ob instanceof Map);
        Object value = ((Map<?,?>) ob).get("a");
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals("B", value);

        value = ((Map<?,?>) ob).get("nr");
        assertNotNull(value);
        assertTrue(value instanceof Number);
        assertEquals(Integer.valueOf(13), value);
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithListDeser
    public void testUntypedWithListDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(List.class, new ListDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("[1, 2, true]", Object.class);
        assertTrue(ob instanceof List<?>);
        List<?> l = (List<?>) ob;
        assertEquals(3, l.size());
        assertEquals("X1", l.get(0));
        assertEquals("X2", l.get(1));
        assertEquals("Xtrue", l.get(2));
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithMapDeser
    public void testUntypedWithMapDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(Map.class, new MapDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("{\"a\":true}", Object.class);
        assertTrue(ob instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) ob;
        assertEquals(1, map.size());
        assertEquals("Ytrue", map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testNestedUntyped989
    public void testNestedUntyped989() throws IOException
    {
        Untyped989 pojo;
        ObjectReader r = MAPPER.readerFor(Untyped989.class);

        pojo = r.readValue("[]");
        assertTrue(pojo.value instanceof List);
        pojo = r.readValue("[{}]");
        assertTrue(pojo.value instanceof List);
        
        pojo = r.readValue("{}");
        assertTrue(pojo.value instanceof Map);
        pojo = r.readValue("{\"a\":[]}");
        assertTrue(pojo.value instanceof Map);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideClassValid
    public void testOverrideClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        CollectionHolder result = m.readValue
            ("{ \"strings\" : [ \"test\" ] }", CollectionHolder.class);

        Collection<String> strs = result._strings;
        assertEquals(1, strs.size());
        assertEquals(TreeSet.class, strs.getClass());
        assertEquals("test", strs.iterator().next());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideMapValid
    public void testOverrideMapValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        MapHolder result = m.readValue
            ("{ \"strings\" :  { \"a\" : 3 } }", MapHolder.class);

        Map<String,String> strs = result._data;
        assertEquals(1, strs.size());
        assertEquals(TreeMap.class, strs.getClass());
        String value = strs.get("a");
        assertEquals("3", value);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideArrayClass
    public void testOverrideArrayClass() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ArrayHolder result = m.readValue
            ("{ \"strings\" : [ \"test\" ] }", ArrayHolder.class);

        String[] strs = result._strings;
        assertEquals(1, strs.length);
        assertEquals(String[].class, strs.getClass());
        assertEquals("test", strs[0]);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideClassInvalid
    public void testOverrideClassInvalid() throws Exception
    {
        
        try {
            BrokenCollectionHolder result = new ObjectMapper().readValue
                ("{ \"strings\" : [ ] }", BrokenCollectionHolder.class);
            fail("Expected a failure, but got results: "+result);
        } catch (JsonMappingException jme) {
            verifyException(jme, "not subtype of");
        }
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootInterfaceAs
    public void testRootInterfaceAs() throws Exception
    {
        RootInterface value = new ObjectMapper().readValue("{\"a\":\"abc\" }", RootInterface.class);
        assertTrue(value instanceof RootInterfaceImpl);
        assertEquals("abc", value.getA());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootInterfaceUsing
    public void testRootInterfaceUsing() throws Exception
    {
        RootString value = new ObjectMapper().readValue("\"xxx\"", RootString.class);
        assertTrue(value instanceof RootString);
        assertEquals("xxx", value.contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootListAs
    public void testRootListAs() throws Exception
    {
        RootMap value = new ObjectMapper().readValue("{\"a\":\"b\"}", RootMap.class);
        assertEquals(1, value.size());
        Object v2 = value.get("a");
        assertEquals(RootStringImpl.class, v2.getClass());
        assertEquals("b", ((RootString) v2).contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootMapAs
    public void testRootMapAs() throws Exception
    {
        RootList value = new ObjectMapper().readValue("[ \"c\" ]", RootList.class);
        assertEquals(1, value.size());
        Object v2 = value.get(0);
        assertEquals(RootStringImpl.class, v2.getClass());
        assertEquals("c", ((RootString) v2).contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideKeyClassValid
	public void testOverrideKeyClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapKeyHolder result = m.readValue("{ \"map\" : { \"xxx\" : \"yyy\" } }", MapKeyHolder.class);
        Map<StringWrapper, String> map = (Map<StringWrapper,String>)(Map<?,?>)result._map;
        assertEquals(1, map.size());
        Map.Entry<StringWrapper, String> en = map.entrySet().iterator().next();

        StringWrapper key = en.getKey();
        assertEquals(StringWrapper.class, key.getClass());
        assertEquals("xxx", key._string);
        assertEquals("yyy", en.getValue());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideKeyClassInvalid
    public void testOverrideKeyClassInvalid() throws Exception
    {
        
        try {
            BrokenMapKeyHolder result = new ObjectMapper().readValue
                ("{ \"123\" : \"xxx\" }", BrokenMapKeyHolder.class);
            fail("Expected a failure, but got results: "+result);
        } catch (JsonMappingException jme) {
            verifyException(jme, "not subtype of");
        }
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideContentClassValid
	public void testOverrideContentClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ListContentHolder result = m.readValue("{ \"list\" : [ \"abc\" ] }", ListContentHolder.class);
        List<StringWrapper> list = (List<StringWrapper>)result._list;
        assertEquals(1, list.size());
        Object value = list.get(0);
        assertEquals(StringWrapper.class, value.getClass());
        assertEquals("abc", ((StringWrapper) value)._string);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideArrayContents
    public void testOverrideArrayContents() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ArrayContentHolder result = m.readValue("{ \"data\" : [ 1, 2, 3 ] }",
                                                ArrayContentHolder.class);
        Object[] data = result._data;
        assertEquals(3, data.length);
        assertEquals(Long[].class, data.getClass());
        assertEquals(1L, data[0]);
        assertEquals(2L, data[1]);
        assertEquals(3L, data[2]);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideMapContents
    public void testOverrideMapContents() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapContentHolder result = m.readValue("{ \"map\" : { \"a\" : 9 } }",
                                                MapContentHolder.class);
        Map<Object,Object> map = result._map;
        assertEquals(1, map.size());
        Object ob = map.values().iterator().next();
        assertEquals(Integer.class, ob.getClass());
        assertEquals(Integer.valueOf(9), ob);
    }

// com.fasterxml.jackson.databind.deser.UntypedNumbersTest::testIntAsNumber
    public void testIntAsNumber() throws Exception
    {
        
        Number result = MAPPER.readValue(" 123 ", Number.class);
        assertEquals(Integer.valueOf(123), result);
    }

// com.fasterxml.jackson.databind.deser.UntypedNumbersTest::testLongAsNumber
    public void testLongAsNumber() throws Exception
    {
        
        long exp = 1234567890123L;
        Number result = MAPPER.readValue(String.valueOf(exp), Number.class);
        assertEquals(Long.valueOf(exp), result);
    }

// com.fasterxml.jackson.databind.deser.UntypedNumbersTest::testBigIntAsNumber
    public void testBigIntAsNumber() throws Exception
    {
        
        BigInteger biggie = new BigInteger("1234567890123456789012345678901234567890");
        Number result = MAPPER.readValue(biggie.toString(), Number.class);
        assertEquals(BigInteger.class, biggie.getClass());
        assertEquals(biggie, result);
    }

// com.fasterxml.jackson.databind.deser.UntypedNumbersTest::testIntTypeOverride
    public void testIntTypeOverride() throws Exception
    {
        
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

        BigInteger exp = BigInteger.valueOf(123L);

        
        Number result = r.forType(Number.class).readValue(" 123 ");
        assertEquals(BigInteger.class, result.getClass());
        assertEquals(exp, result);

        
         r.forType(Object.class).readValue("123");
        assertEquals(BigInteger.class, result.getClass());
        assertEquals(exp, result);

        
        JsonNode node = r.readTree("  123");
        assertTrue(node.isBigInteger());
        assertEquals(123, node.asInt());
    }

// com.fasterxml.jackson.databind.deser.UntypedNumbersTest::testDoubleAsNumber
    public void testDoubleAsNumber() throws Exception
    {
        Number result = MAPPER.readValue(new StringReader(" 1.0 "), Number.class);
        assertEquals(Double.valueOf(1.0), result);
    }

// com.fasterxml.jackson.databind.deser.UntypedNumbersTest::testFpTypeOverrideSimple
    public void testFpTypeOverrideSimple() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        BigDecimal dec = new BigDecimal("0.1");

        
        Number result = r.forType(Number.class).readValue(dec.toString());
        assertEquals(BigDecimal.class, result.getClass());
        assertEquals(dec, result);

        
        Object value = r.forType(Object.class).readValue(dec.toString());
        assertEquals(BigDecimal.class, result.getClass());
        assertEquals(dec, value);

        JsonNode node = r.readTree(dec.toString());
        assertTrue(node.isBigDecimal());
        assertEquals(dec.doubleValue(), node.asDouble());
    }

// com.fasterxml.jackson.databind.deser.UntypedNumbersTest::testFpTypeOverrideStructured
    public void testFpTypeOverrideStructured() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        BigDecimal dec = new BigDecimal("-19.37");
        
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) r.forType(List.class).readValue("[ "+dec.toString()+" ]");
        assertEquals(1, list.size());
        Object val = list.get(0);
        assertEquals(BigDecimal.class, val.getClass());
        assertEquals(dec, val);

        
        Map<?,?> map = r.forType(Map.class).readValue("{ \"a\" : "+dec.toString()+" }");
        assertEquals(1, map.size());
        val = map.get("a");
        assertEquals(BigDecimal.class, val.getClass());
        assertEquals(dec, val);
    }

// com.fasterxml.jackson.databind.deser.UntypedNumbersTest::testForceIntsToLongs
    public void testForceIntsToLongs() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_LONG_FOR_INTS);

        Object ob = r.forType(Object.class).readValue("42");
        assertEquals(Long.class, ob.getClass());
        assertEquals(Long.valueOf(42L), ob);

        Number n = r.forType(Number.class).readValue("42");
        assertEquals(Long.class, n.getClass());
        assertEquals(Long.valueOf(42L), n);

        
        JsonNode node = r.readTree("42");
        if (!node.isLong()) {
            fail("Expected LongNode, got: "+node.getClass().getName());
        }
        assertEquals(42, node.asInt());
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testQNameSer
    public void testQNameSer() {}

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDurationSer
    public void testDurationSer() {}

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testXMLGregorianCalendarSerAndDeser
    public void testXMLGregorianCalendarSerAndDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        XMLGregorianCalendar cal = dtf.newXMLGregorianCalendar
            (1974, 10, 10, 18, 15, 17, 123, 0);
        
        ObjectMapper mapper = new ObjectMapper();
        long timestamp = cal.toGregorianCalendar().getTimeInMillis();
        String numStr = String.valueOf(timestamp);
        assertEquals(numStr, mapper.writeValueAsString(cal));

        
        XMLGregorianCalendar calOut = mapper.readValue(numStr, XMLGregorianCalendar.class);
        assertNotNull(calOut);
        assertEquals(timestamp, calOut.toGregorianCalendar().getTimeInMillis());

        
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        String exp = cal.toXMLFormat();
        String act = mapper.writeValueAsString(cal);
        act = act.substring(1, act.length() - 1); 
        exp = removeZ(exp);
        act = removeZ(act);
        assertEquals(exp, act);
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDeserializerLoading
    public void testDeserializerLoading()
    {
        CoreXMLDeserializers sers = new CoreXMLDeserializers();
        TypeFactory f = TypeFactory.defaultInstance();
        sers.findBeanDeserializer(f.constructType(Duration.class), null, null);
        sers.findBeanDeserializer(f.constructType(XMLGregorianCalendar.class), null, null);
        sers.findBeanDeserializer(f.constructType(QName.class), null, null);
    }
