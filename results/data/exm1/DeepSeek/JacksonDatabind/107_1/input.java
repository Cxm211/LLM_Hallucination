// buggy code
    protected final JsonDeserializer<Object> _findDeserializer(DeserializationContext ctxt,
            String typeId) throws IOException
    {
        JsonDeserializer<Object> deser = _deserializers.get(typeId);
        if (deser == null) {
            /* As per [databind#305], need to provide contextual info. But for
             * backwards compatibility, let's start by only supporting this
             * for base class, not via interface. Later on we can add this
             * to the interface, assuming deprecation at base class helps.
             */
            JavaType type = _idResolver.typeFromId(ctxt, typeId);
            if (type == null) {
                // use the default impl if no type id available:
                deser = _findDefaultImplDeserializer(ctxt);
                if (deser == null) {
                    // 10-May-2016, tatu: We may get some help...
                    JavaType actual = _handleUnknownTypeId(ctxt, typeId);
                    if (actual == null) { // what should this be taken to mean?
                        // 17-Jan-2019, tatu: As per [databind#2221], better NOT return `null` but...
                        return null;
                    }
                    // ... would this actually work?
                    deser = ctxt.findContextualValueDeserializer(actual, _property);
                }
            } else {
                /* 16-Dec-2010, tatu: Since nominal type we get here has no (generic) type parameters,
                 *   we actually now need to explicitly narrow from base type (which may have parameterization)
                 *   using raw type.
                 *
                 *   One complication, though; cannot change 'type class' (simple type to container); otherwise
                 *   we may try to narrow a SimpleType (Object.class) into MapType (Map.class), losing actual
                 *   type in process (getting SimpleType of Map.class which will not work as expected)
                 */
                if ((_baseType != null)
                        && _baseType.getClass() == type.getClass()) {
                    /* 09-Aug-2015, tatu: Not sure if the second part of the check makes sense;
                     *   but it appears to check that JavaType impl class is the same which is
                     *   important for some reason?
                     *   Disabling the check will break 2 Enum-related tests.
                     */
                    // 19-Jun-2016, tatu: As per [databind#1270] we may actually get full
                    //   generic type with custom type resolvers. If so, should try to retain them.
                    //  Whether this is sufficient to avoid problems remains to be seen, but for
                    //  now it should improve things.
                    if (!type.hasGenericTypes()) {
                        type = ctxt.getTypeFactory().constructSpecializedType(_baseType, type.getRawClass());
                    }
                }
                deser = ctxt.findContextualValueDeserializer(type, _property);
            }
            _deserializers.put(typeId, deser);
        }
        return deser;
    }

// relevant test
// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testNullValue
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testEnumAsObject
    public void testEnumAsObject() throws Exception
    {
        
        Object[] input = new Object[] { Choice.YES };
        Object[] input2 = new Object[] { ComplexChoice.MAYBE};
        
        assertEquals("[\"YES\"]", serializeAsString(input));
        assertEquals("[\"MAYBE\"]", serializeAsString(input2));

        
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        String json = m.writeValueAsString(input);
        assertEquals("[[\""+Choice.class.getName()+"\",\"YES\"]]", json);

        
        Object[] output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        assertEquals(Choice.YES, output[0]);

        
        json = m.writeValueAsString(input2);
        assertEquals("[[\""+ComplexChoice.class.getName()+"\",\"MAYBE\"]]", json);
        output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        assertEquals(ComplexChoice.MAYBE, output[0]);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testEnumSet
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testEnumMap
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testJackson311
    public void testJackson311() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString(new PolymorphicType("hello", 2));
        PolymorphicType value = mapper.readValue(json, PolymorphicType.class);
        assertEquals("hello", value.foo);
        assertEquals(Integer.valueOf(2), value.bar);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testTokenBuffer
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testIssue352
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testFeature432
    public void testFeature432() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "*CLASS*");
        String json = mapper.writeValueAsString(new BeanHolder(new StringBean("punny")));
        assertEquals("{\"bean\":{\"*CLASS*\":\"com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject$StringBean\",\"name\":\"punny\"}}", json);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testNoGoWithExternalProperty
    public void testNoGoWithExternalProperty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT,
                    JsonTypeInfo.As.EXTERNAL_PROPERTY);
            fail("Should not have passed");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot use includeAs of EXTERNAL_PROPERTY");
        }
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testNumericScalars
    public void testNumericScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[123]", m.writeValueAsString(new Object[] { Integer.valueOf(123) }));
        assertEquals("[[\"java.lang.Long\",37]]", m.writeValueAsString(new Object[] { Long.valueOf(37) }));
        assertEquals("[0.25]", m.writeValueAsString(new Object[] { Double.valueOf(0.25) }));
        assertEquals("[[\"java.lang.Float\",0.5]]", m.writeValueAsString(new Object[] { Float.valueOf(0.5f) }));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testDateScalars
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testMiscScalars
    public void testMiscScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[\"abc\"]", m.writeValueAsString(new Object[] { "abc" }));
        assertEquals("[true,null,false]", m.writeValueAsString(new Boolean[] { true, null, false }));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testScalarArrays
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::test417
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testDefaultTypingWithLong
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForTreeNodes::testValueAsStringWithDefaultTyping
    public void testValueAsStringWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        String json = DEFAULT_MAPPER.writeValueAsString(foo);

        JsonNode jsonNode = DEFAULT_MAPPER.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForTreeNodes::testValueToTreeWithDefaultTyping
    public void testValueToTreeWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        JsonNode jsonNode = DEFAULT_MAPPER.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultWithCreators::testWithCreators
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultWithCreators::testWithCreatorAndJsonValue
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeId198Test::testFails
    public void testFails() throws Exception {
        String json = "{ \"name\": \"foo\", \"attack\":\"right\" } }";

        Character character = MAPPER.readValue(json, Character.class);

        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeId198Test::testWorks
    public void testWorks() throws Exception {
        String json = "{ \"name\": \"foo\", \"preferredAttack\": \"KICK\", \"attack\":\"right\" } }";

        Character character = MAPPER.readValue(json, Character.class);

        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeId999Test::testExternalTypeId
    public void testExternalTypeId() throws Exception
    {
        TypeReference<?> type = new TypeReference<Message<FooPayload>>() { };

        Message<?> msg = MAPPER.readValue(aposToQuotes("{ 'type':'foo', 'payload': {} }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);

        
        msg = MAPPER.readValue(aposToQuotes("{'payload': {}, 'type':'foo' }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdWithEnum1328Test::testExample
    public void testExample() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(Arrays.asList(new AnimalAndType(AnimalType.Dog, new Dog())));
        List<AnimalAndType> list = mapper.readerFor(new TypeReference<List<AnimalAndType>>() { })
            .readValue(json);
        assertNotNull(list);
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

// com.fasterxml.jackson.databind.jsontype.ext.MultipleExternalIds291Test::testMultipleValuesSingleExtId
    public void testMultipleValuesSingleExtId() throws Exception
    {
        
        _testMultipleValuesSingleExtId(
"{'type' : '1',\n"
+"'field1' : { 'a' : 'AAA' },\n"
+"'field2' : { 'c' : 'CCC' }\n"
+"}"
);

        
        _testMultipleValuesSingleExtId(
"{\n"
+"'field1' : { 'a' : 'AAA' },\n"
+"'field2' : { 'c' : 'CCC' },\n"
+"'type' : '1'\n"
+"}"
);
        
        _testMultipleValuesSingleExtId(
"{\n"
+"'field1' : { 'a' : 'AAA' },\n"
+"'type' : '1',\n"
+"'field2' : { 'c' : 'CCC' }\n"
+"}"
);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationPresent
    public void testDeserializationPresent() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBox();
        checkAppleBox();

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBox();
        checkAppleBox();
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationNull
    public void testDeserializationNull() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxNull(orangeBoxNullJson);
        checkAppleBoxNull(appleBoxNullJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxNull(orangeBoxNullJson);
        checkAppleBoxNull(appleBoxNullJson);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationEmpty
    public void testDeserializationEmpty() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxEmpty(orangeBoxEmptyJson);
        checkAppleBoxEmpty(appleBoxEmptyJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxEmpty(orangeBoxEmptyJson);
        checkAppleBoxEmpty(appleBoxEmptyJson);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationMissing
    public void testDeserializationMissing() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxNull(orangeBoxMissingJson);
        checkAppleBoxNull(appleBoxMissingJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkBoxJsonMappingException(orangeBoxMissingJson);
        checkBoxJsonMappingException(appleBoxMissingJson);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationMissingRequired
    public void testDeserializationMissingRequired() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkReqBoxJsonMappingException(orangeBoxMissingJson);
        checkReqBoxJsonMappingException(appleBoxMissingJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkReqBoxJsonMappingException(orangeBoxMissingJson);
        checkReqBoxJsonMappingException(appleBoxMissingJson);
    }

// com.fasterxml.jackson.databind.misc.RaceCondition738Test::testRepeatedly
    public void testRepeatedly() throws Exception {
        final int COUNT = 3000;
        for (int i = 0; i < COUNT; i++) {
            runOnce(i, COUNT);
        }
    }

// com.fasterxml.jackson.databind.mixins.MapperMixinsCopy1998Test::testB_KO
    public void testB_KO() throws Exception
    {
        final ObjectMapper DEFAULT = defaultMapper();
        MyModelRoot myModelInstance = new MyModelRoot();
        myModelInstance.setChild(new MyChildB("testB"));

        ObjectMapper myObjectMapper = DEFAULT.copy();

        String postResult = getString(myModelInstance, myObjectMapper);
        assertEquals(FULLMODEL, postResult);

        myObjectMapper = DEFAULT.copy();

        myObjectMapper.addMixIn(MyModelRoot.class, MixinConfig.MyModelRoot.class)
                .addMixIn(MyModelChildBase.class, MixinConfig.MyModelChildBase.class)
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .setConfig(myObjectMapper.getSerializationConfig().withView(MyModelView.class));

        String result = getString(myModelInstance, myObjectMapper);
        assertEquals(EXPECTED, result);

    }

// com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testWithEnumKeys
    public void testWithEnumKeys() throws Exception {
        ObjectMapper plainObjectMapper = new ObjectMapper();
        JsonNode tree = plainObjectMapper.readTree(aposToQuotes("{'red' : [ 'a', 'b']}"));

        ObjectMapper fancyObjectMapper = new ObjectMapper().registerModule(new TestEnumModule());

        
        Map<TestEnum, Set<String>> map = fancyObjectMapper.convertValue(tree,
                new TypeReference<Map<TestEnum, Set<String>>>() { } );
        assertNotNull(map);
    }

// com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testCustomEnumKeySerializerWithPolymorphic
    public void testCustomEnumKeySerializerWithPolymorphic() throws IOException
    {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(SuperTypeEnum.class, new JsonDeserializer<SuperTypeEnum>() {
            @Override
            public SuperTypeEnum deserialize(JsonParser p, DeserializationContext deserializationContext)
                    throws IOException
            {
                return SuperTypeEnum.valueOf(p.getText());
            }
        });
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(simpleModule);

        SuperType superType = mapper.readValue("{\"someMap\": {\"FOO\": \"bar\"}}",
                SuperType.class);
        assertEquals("Deserialized someMap.FOO should equal bar", "bar",
                superType.someMap.get(SuperTypeEnum.FOO));
    }

// com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testCustomEnumValueAndKeyViaModifier
    public void testCustomEnumValueAndKeyViaModifier() throws IOException
    {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {        
            @Override
            public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config,
                    final JavaType type, BeanDescription beanDesc,
                    final JsonDeserializer<?> deserializer) {
                return new JsonDeserializer<Enum>() {
                    @Override
                    public Enum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        final String str = p.getValueAsString().toLowerCase();
                        return KeyEnum.valueOf(rawClass, str);
                    }
                };
            }

            @Override
            public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config,
                    final JavaType type, KeyDeserializer deserializer)
            {
                if (!type.isEnumType()) {
                    return deserializer;
                }
                return new KeyDeserializer() {
                    @Override
                    public Object deserializeKey(String key, DeserializationContext ctxt)
                            throws IOException
                    {
                        Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        return Enum.valueOf(rawClass, key.toLowerCase());
                    }
                };
            }
        });
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(module);

        
        KeyEnum key = mapper.readValue(quote(KeyEnum.replacements.name().toUpperCase()),
                KeyEnum.class);
        assertSame(KeyEnum.replacements, key);

        
        EnumMap<KeyEnum,String> map = mapper.readValue(
                aposToQuotes("{'REPlaceMENTS':'foobar'}"),
                new TypeReference<EnumMap<KeyEnum,String>>() { });
        assertEquals(1, map.size());
        assertSame(KeyEnum.replacements, map.keySet().iterator().next());
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
                    fail("Failed (variant "+variant+", data length "+len+"): "+e.getMessage());
                }
                assertNotNull(data);
                assertArrayEquals(data, input);

                
                JsonParser p = new TreeTraversingParser(n);
                assertEquals(JsonToken.VALUE_STRING, p.nextToken());
                try {
                    data = p.getBinaryValue(variant);
                } catch (Exception e) {
                    fail("Failed (variant "+variant+", data length "+len+"): "+e.getMessage());
                }
                assertNotNull(data);
                assertArrayEquals(data, input);
                p.close();
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

// com.fasterxml.jackson.databind.node.TestConversions::testBufferedLongViaCoercion
    public void testBufferedLongViaCoercion() throws Exception {
        long EXP = 1519348261000L;
        JsonNode tree = MAPPER.readTree("{\"longObj\": "+EXP+".0, \"_class\": \""+LongContainer1940.class.getName()+"\"}");
        LongContainer1940 obj = MAPPER.treeToValue(tree, LongContainer1940.class);
        assertEquals(Long.valueOf(EXP), obj.longObj);
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testBoolean
    public void testBoolean() throws Exception
    {
        BooleanNode f = BooleanNode.getFalse();
        assertNotNull(f);
        assertTrue(f.isBoolean());
        assertSame(f, BooleanNode.valueOf(false));
        assertStandardEquals(f);
        assertFalse(f.booleanValue());
        assertFalse(f.asBoolean());
        assertEquals("false", f.asText());
        assertEquals(JsonToken.VALUE_FALSE, f.asToken());

        
        BooleanNode t = BooleanNode.getTrue();
        assertNotNull(t);
        assertTrue(t.isBoolean());
        assertSame(t, BooleanNode.valueOf(true));
        assertStandardEquals(t);
        assertTrue(t.booleanValue());
        assertTrue(t.asBoolean());
        assertEquals("true", t.asText());
        assertEquals(JsonToken.VALUE_TRUE, t.asToken());

        assertNodeNumbers(f, 0, 0.0);
        assertNodeNumbers(t, 1, 1.0);
    
        JsonNode result = objectMapper().readTree("true\n");
        assertFalse(result.isNull());
        assertFalse(result.isNumber());
        assertFalse(result.isTextual());
        assertTrue(result.isBoolean());
        assertType(result, BooleanNode.class);
        assertTrue(result.booleanValue());
        assertEquals("true", result.asText());
        assertFalse(result.isMissingNode());

        
        assertEquals(result, BooleanNode.valueOf(true));
        assertEquals(result, BooleanNode.getTrue());
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testBinary
    public void testBinary() throws Exception
    {
        assertNull(BinaryNode.valueOf(null));
        assertNull(BinaryNode.valueOf(null, 0, 0));

        BinaryNode empty = BinaryNode.valueOf(new byte[1], 0, 0);
        assertSame(BinaryNode.EMPTY_BINARY_NODE, empty);
        assertStandardEquals(empty);

        byte[] data = new byte[3];
        data[1] = (byte) 3;
        BinaryNode n = BinaryNode.valueOf(data, 1, 1);
        data[2] = (byte) 3;
        BinaryNode n2 = BinaryNode.valueOf(data, 2, 1);
        assertTrue(n.equals(n2));
        assertEquals("\"Aw==\"", n.toString());

        assertEquals("AAMD", new BinaryNode(data).asText());
        assertNodeNumbersForNonNumeric(n);
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testPOJO
    public void testPOJO()
    {
        POJONode n = new POJONode("x"); 
        assertStandardEquals(n);
        assertEquals(n, new POJONode("x"));
        assertEquals("x", n.asText());
        
        assertEquals("x", n.toString());

        assertEquals(new POJONode(null), new POJONode(null));

        
        assertNodeNumbersForNonNumeric(n);
        
        assertNodeNumbers(new POJONode(Integer.valueOf(123)), 123, 123.0);
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testRawValue
    public void testRawValue() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        root.putRawValue("a", new RawValue(new SerializedString("[1, 2, 3]")));

        assertEquals("{\"a\":[1, 2, 3]}", MAPPER.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testCustomComparators
    public void testCustomComparators() throws Exception
    {
        ObjectNode nestedObject1 = MAPPER.createObjectNode();
        nestedObject1.put("value", 6);
        ArrayNode nestedArray1 = MAPPER.createArrayNode();
        nestedArray1.add(7);
        ObjectNode root1 = MAPPER.createObjectNode();
        root1.put("value", 5);
        root1.set("nested_object", nestedObject1);
        root1.set("nested_array", nestedArray1);

        ObjectNode nestedObject2 = MAPPER.createObjectNode();
        nestedObject2.put("value", 6.9);
        ArrayNode nestedArray2 = MAPPER.createArrayNode();
        nestedArray2.add(7.0);
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.put("value", 5.0);
        root2.set("nested_object", nestedObject2);
        root2.set("nested_array", nestedArray2);

        
        assertFalse(root1.equals(root2));
        assertFalse(root2.equals(root1));
        assertTrue(root1.equals(root1));
        assertTrue(root2.equals(root2));

        assertTrue(nestedArray1.equals(nestedArray1));
        assertFalse(nestedArray1.equals(nestedArray2));
        assertFalse(nestedArray2.equals(nestedArray1));

        
        Comparator<JsonNode> cmp = new Comparator<JsonNode>() {

            @Override
            public int compare(JsonNode o1, JsonNode o2) {
                if (o1 instanceof ContainerNode || o2 instanceof ContainerNode) {
                    fail("container nodes should be traversed, comparator should not be invoked");
                }
                if (o1.equals(o2)) {
                    return 0;
                }
                if ((o1 instanceof NumericNode) && (o2 instanceof NumericNode)) {
                    int d1 = ((NumericNode) o1).asInt();
                    int d2 = ((NumericNode) o2).asInt();
                    if (d1 == d2) { 
                        return 0;
                    }
                    if (d1 < d2) {
                        return -1;
                    }
                    return 1;
                }
                return 0;
            }
        };
        assertTrue(root1.equals(cmp, root2));
        assertTrue(root2.equals(cmp, root1));
        assertTrue(root1.equals(cmp, root1));
        assertTrue(root2.equals(cmp, root2));

        ArrayNode array3 = MAPPER.createArrayNode();
        array3.add(123);
        
        assertFalse(root2.equals(cmp, nestedArray1));
        assertTrue(nestedArray1.equals(cmp, nestedArray1));
        assertFalse(nestedArray1.equals(cmp, root2));
        assertFalse(nestedArray1.equals(cmp, array3));
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testArrayWithDefaultTyping
    public void testArrayWithDefaultTyping() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .enableDefaultTyping();

        JsonNode array = mapper.readTree("[ 1, 2 ]");
        assertTrue(array.isArray());
        assertEquals(2, array.size());

        JsonNode obj = mapper.readTree("{ \"a\" : 2 }");
        assertTrue(obj.isObject());
        assertEquals(1, obj.size());
        assertEquals(2, obj.path("a").asInt());
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

// com.fasterxml.jackson.databind.objectid.ObjectId825BTest::testFull825
    public void testFull825() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        String INPUT = aposToQuotes(
"{\n"+
"    '@class': '_PKG_CTC',\n"+
"     'var': [{\n"+
"      'ch': {\n"+
"        '@class': '_PKG_Ch',\n"+
"         'act': [{\n"+
"            '@class': '_PKG_CTD',\n"+
"            'oidString': 'oid1',\n"+
"            'dec': [{\n"+
"              '@class': '_PKG_Dec',\n"+
"                'oidString': 'oid2',\n"+
"                'outTr': [{\n"+
"                  '@class': '_PKG_Tr',\n"+
"                  'target': {\n"+
"                    '@class': '_PKG_Ti',\n"+
"                    'oidString': 'oid3',\n"+
"                    'timer': 'problemoid',\n"+
"                    'outTr': [{\n"+
"                      '@class': '_PKG_Tr',\n"+
"                      'target': {\n"+
"                        '@class': '_PKG_Ti',\n"+
"                        'oidString': 'oid4',\n"+
"                        'timer': {\n"+
"                          '@class': '_PKG_V',\n"+
"                          'oidString': 'problemoid'\n"+
"                        }\n"+
"                      }\n"+
"                    }]\n"+
"                  }\n"+
"                }]\n"+
"              }]\n"+
"         }],\n"+
"         'oidString': 'oid5'\n"+
"      },\n"+
"       '@class': '_PKG_CTV',\n"+
"       'oidString': 'oid6',\n"+
"       'locV': ['problemoid']\n"+
"    }],\n"+
"     'oidString': 'oid7'\n"+
"}\n"
                );

        
        final String newPkg = getClass().getName() + "\\$";
        INPUT = INPUT.replaceAll("_PKG_", newPkg);
        
        CTC result = mapper.readValue(INPUT, CTC.class);
        assertNotNull(result);
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

// com.fasterxml.jackson.databind.objectid.PolymorphicWithObjectId1551Test::testWithAbstractUsingProp
    public void testWithAbstractUsingProp() throws Exception
    {
        Car c = new Car();
        c.vehicleId = "123";
        c.numberOfDoors = 2;
        
        VehicleOwnerViaProp v1 = new VehicleOwnerViaProp();
        v1.ownedVehicle = c;
        VehicleOwnerViaProp v2 = new VehicleOwnerViaProp();
        v2.ownedVehicle = c;

        ObjectMapper objectMapper = new ObjectMapper();
        String serialized = objectMapper.writer()
                .writeValueAsString(new VehicleOwnerViaProp[] { v1, v2 });
        

        VehicleOwnerViaProp[] deserialized = objectMapper.readValue(serialized, VehicleOwnerViaProp[].class);
        assertEquals(2, deserialized.length);
        assertSame(deserialized[0].ownedVehicle, deserialized[1].ownedVehicle);
    }

// com.fasterxml.jackson.databind.objectid.PolymorphicWithObjectId1551Test::testFailingAbstractUsingProp
    public void testFailingAbstractUsingProp() throws Exception
    {
        Car c = new Car();
        c.vehicleId = "123";
        c.numberOfDoors = 2;
        
        VehicleOwnerBroken v1 = new VehicleOwnerBroken();
        v1.ownedVehicle = c;
        VehicleOwnerBroken v2 = new VehicleOwnerBroken();
        v2.ownedVehicle = c;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writer()
                .writeValueAsString(new VehicleOwnerBroken[] { v1, v2 });
        } catch (InvalidDefinitionException e) {
            
            assertEquals(Car.class, e.getType().getRawClass());
            verifyException(e, "Invalid Object Id definition");
            verifyException(e, "cannot find property with name 'bogus'");
        }

        
        final String JSON = aposToQuotes(
"[{'ownedVehicle':{'@class':'com.fasterxml.jackson.failing.PolymorphicWithObjectId1551Test$Car','vehicleId':'123',"
+"'numberOfDoors':2}},{'ownedVehicle':'123'}]"
                );
        try {
            objectMapper.readValue(JSON, VehicleOwnerBroken[].class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            assertEquals(Vehicle.class, e.getType().getRawClass());
            verifyException(e, "Invalid Object Id definition");
            verifyException(e, "cannot find property with name 'bogus'");
        }
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

// com.fasterxml.jackson.databind.objectid.TestObjectId::testMixedRefsIssue188
    public void testMixedRefsIssue188() throws Exception
    {
        Company comp = new Company();
        Employee e1 = new Employee(1, "First", null);
        Employee e2 = new Employee(2, "Second", e1);
        e1.addReport(e2);
        comp.add(e1);
        comp.add(e2);

        String json = MAPPER.writeValueAsString(comp);
        
        assertEquals("{\"employees\":["
                +"{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                +"{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                +"]}",
                json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectId::testObjectAndTypeId
    public void testObjectAndTypeId() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();

        Bar inputRoot = new Bar();
        Foo inputChild = new Foo();
        inputRoot.next = inputChild;
        inputChild.ref = inputRoot;

        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(inputRoot);
        
        BaseEntity resultRoot = mapper.readValue(json, BaseEntity.class);
        assertNotNull(resultRoot);
        assertTrue(resultRoot instanceof Bar);
        Bar first = (Bar) resultRoot;

        assertNotNull(first.next);
        assertTrue(first.next instanceof Foo);
        Foo second = (Foo) first.next;
        assertNotNull(second.ref);
        assertSame(first, second.ref);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectId::testWithFieldsInBaseClass1083
    public void testWithFieldsInBaseClass1083() throws Exception {
          final String json = aposToQuotes("{'schemas': [{\n"
              + "  'name': 'FoodMart'\n"
              + "}]}\n");
          MAPPER.readValue(json, JsonRoot.class);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithEquals::testSimpleEquals
    public void testSimpleEquals() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        assertFalse(mapper.isEnabled(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID));
        mapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID);

        Foo foo = new Foo(1);

        Bar bar1 = new Bar(1);
        Bar bar2 = new Bar(2);
        
        
        
        Bar anotherBar1 = new Bar(1);

        foo.bars.add(bar1);
        foo.bars.add(bar2);
        
        foo.otherBars.add(anotherBar1);
        foo.otherBars.add(bar2);

        String json = mapper.writeValueAsString(foo);
        assertEquals("{\"id\":1,\"bars\":[{\"id\":1},{\"id\":2}],\"otherBars\":[1,2]}", json);

        Foo foo2 = mapper.readValue(json, Foo.class);       
        assertNotNull(foo2);
        assertEquals(foo.id, foo2.id);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithEquals::testEqualObjectIdsExternal
    public void testEqualObjectIdsExternal() throws Exception
    {
        Element element = new Element();
        element.uri = URI.create("URI");
        element.name = "Element1";

        Element element2 = new Element();
        element2.uri = URI.create("URI");
        element2.name = "Element2";

        
        

        List<Element> input = Arrays.asList(element, element2);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID);

        String json = mapper.writerFor(new TypeReference<List<Element>>() { })
                .writeValueAsString(input);

        Element[] output = mapper.readValue(json, Element[].class);
        assertNotNull(output);
        assertEquals(2, output.length);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithPolymorphic::testPolymorphicRoundtrip
    public void testPolymorphicRoundtrip() throws Exception
    {
        
        Impl in1 = new Impl(123, 456);
        in1.next = new Impl(111, 222);
        in1.next.next = in1;
        
        String json = mapper.writeValueAsString(in1);
        
        
        Base result0 = mapper.readValue(json, Base.class);
        assertNotNull(result0);
        assertSame(Impl.class, result0.getClass());
        Impl result = (Impl) result0;
        assertEquals(123, result.value);
        assertEquals(456, result.extra);
        Impl result2 = (Impl) result.next;
        assertEquals(111, result2.value);
        assertEquals(222, result2.extra);
        assertSame(result, result2.next);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithPolymorphic::testIssue811
    public void testIssue811() throws Exception
    {
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "@class");
    
        Process p = new Process();
        Scope s = new Scope(p, null);
        FaultHandler fh = new FaultHandler(p);
        Catch c = new Catch(p, s);
        fh.catchBlocks.add(c);
        s.faultHandlers.add(fh);
        
        String json = om.writeValueAsString(p);
        Process restored = om.readValue(json, Process.class);
        assertNotNull(restored);

        assertEquals(0, p.id);
        assertEquals(3, p.children.size());
        assertSame(p, p.children.get(0).owner);
        assertSame(p, p.children.get(1).owner);
        assertSame(p, p.children.get(2).owner);
    }

// com.fasterxml.jackson.databind.seq.PolyMapWriter827Test::testPolyCustomKeySerializer
    public void testPolyCustomKeySerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        mapper.registerModule(new SimpleModule("keySerializerModule")
            .addKeySerializer(CustomKey.class, new CustomKeySerializer()));

        Map<CustomKey, String> map = new HashMap<CustomKey, String>();
        CustomKey key = new CustomKey();
        key.a = "foo";
        key.b = 1;
        map.put(key, "bar");

        final ObjectWriter writer = mapper.writerFor(new TypeReference<Map<CustomKey,String>>() { });
        String json = writer.writeValueAsString(map);
        Assert.assertEquals("[\"java.util.HashMap\",{\"foo,1\":\"bar\"}]", json);
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testSimpleNonArray
    public void testSimpleNonArray() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .forType(Bean.class)
                .writeValues(strw);
        w.write(new Bean(13))
            .write(new Bean(-6))
            .writeAll(new Bean[] { new Bean(3), new Bean(1) })
            .writeAll(Arrays.asList(new Bean(5), new Bean(7)))
        ;
        w.close();
        assertEquals(aposToQuotes("{'a':13}\n{'a':-6}\n{'a':3}\n{'a':1}\n{'a':5}\n{'a':7}"),
                strw.toString());

        strw = new StringWriter();
        JsonGenerator gen = WRITER.getFactory().createGenerator(strw);
        w = WRITER
                .withRootValueSeparator(new SerializedString("/"))
                .writeValues(gen);
        w.write(new Bean(1))
            .write(new Bean(2));
        w.close();
        gen.close();
        assertEquals(aposToQuotes("{'a':1}/{'a':2}"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testSimpleArray
    public void testSimpleArray() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER.writeValuesAsArray(strw);
        w.write(new Bean(1))
            .write(new Bean(2))
            .writeAll(new Bean[] { new Bean(-7), new Bean(2) });
        w.close();
        assertEquals(aposToQuotes("[{'a':1},{'a':2},{'a':-7},{'a':2}]"),
                strw.toString());

        strw = new StringWriter();
        JsonGenerator gen = WRITER.getFactory().createGenerator(strw);
        w = WRITER.writeValuesAsArray(gen);
        Collection<Bean> bean = Collections.singleton(new Bean(3));
        w.write(new Bean(1))
            .write(null)
            .writeAll((Iterable<Bean>) bean);
        w.close();
        gen.close();
        assertEquals(aposToQuotes("[{'a':1},null,{'a':3}]"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testPolymorphicNonArrayWithoutType
    public void testPolymorphicNonArrayWithoutType() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .writeValues(strw);
        w.write(new ImplA(3))
            .write(new ImplA(4))
            .close();
        assertEquals(aposToQuotes("{'type':'A','value':3}\n{'type':'A','value':4}"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testPolymorphicArrayWithoutType
    public void testPolymorphicArrayWithoutType() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .writeValuesAsArray(strw);
        w.write(new ImplA(-1))
            .write(new ImplA(6))
            .close();
        assertEquals(aposToQuotes("[{'type':'A','value':-1},{'type':'A','value':6}]"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testPolymorphicArrayWithType
    public void testPolymorphicArrayWithType() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .forType(PolyBase.class)
                .writeValuesAsArray(strw);
        w.write(new ImplA(-1))
            .write(new ImplB(3))
            .write(new ImplA(7));
        w.flush();
        w.close();
        assertEquals(aposToQuotes("[{'type':'A','value':-1},{'type':'B','b':3},{'type':'A','value':7}]"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testSimpleCloseable
    public void testSimpleCloseable() throws Exception
    {
        ObjectWriter w = MAPPER.writer()
                .with(SerializationFeature.CLOSE_CLOSEABLE);
        CloseableValue input = new CloseableValue();
        assertFalse(input.closed);
        StringWriter out = new StringWriter();
        SequenceWriter seq = w.writeValues(out);
        input = new CloseableValue();
        assertFalse(input.closed);
        seq.write(input);
        assertTrue(input.closed);
        seq.close();
        input.close();
        assertEquals(aposToQuotes("{'x':0,'closed':false}"), out.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testWithExplicitType
    public void testWithExplicitType() throws Exception
    {
        ObjectWriter w = MAPPER.writer()
                
                .without(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)
                .with(SerializationFeature.CLOSE_CLOSEABLE);
        StringWriter out = new StringWriter();
        SequenceWriter seq = w.writeValues(out);
        
        seq.write(new BareBaseExt());
        
        seq.write(new BareBaseExt(), MAPPER.constructType(BareBase.class));

        
        
        BareBaseCloseable cl = new BareBaseCloseable();
        seq.write(cl, MAPPER.constructType(BareBase.class));
        assertTrue(cl.closed);
        cl.close();

        seq.close();
        seq.flush();
        assertEquals(aposToQuotes("{'a':1,'b':2} {'a':1} {'a':1}"), out.toString());
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testSimpleMethodJsonValue
    public void testSimpleMethodJsonValue() throws Exception
    {
        assertEquals("\"abc\"", MAPPER.writeValueAsString(new ValueClass<String>("abc")));
        assertEquals("null", MAPPER.writeValueAsString(new ValueClass<String>(null)));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testSimpleFieldJsonValue
    public void testSimpleFieldJsonValue() throws Exception
    {
        assertEquals("\"abc\"", MAPPER.writeValueAsString(new FieldValueClass<String>("abc")));
        assertEquals("null", MAPPER.writeValueAsString(new FieldValueClass<String>(null)));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testJsonValueWithUseSerializer
    public void testJsonValueWithUseSerializer() throws Exception
    {
        String result = serializeAsString(MAPPER, new ToStringValueClass<Integer>(Integer.valueOf(123)));
        assertEquals("\"123\"", result);
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testMixedJsonValue
    public void testMixedJsonValue() throws Exception
    {
        String result = serializeAsString(MAPPER, new ToStringValueClass2("xyz"));
        assertEquals("\"xyz\"", result);
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testDisabling
    public void testDisabling() throws Exception
    {
        assertEquals(aposToQuotes("{'x':1,'y':2}"),
                MAPPER.writeValueAsString(new DisabledJsonValue()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testValueWithStaticType
    public void testValueWithStaticType() throws Exception
    {
        
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", MAPPER.writeValueAsString(new ValueWrapper()));

        
        ObjectMapper staticMapper = new ObjectMapper();
        staticMapper.configure(MapperFeature.USE_STATIC_TYPING, true);
        assertEquals("{\"a\":\"a\"}", staticMapper.writeValueAsString(new ValueWrapper()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testMapWithJsonValue
    public void testMapWithJsonValue() throws Exception {
        
        assertEquals("{\"a\":\"1\"}", MAPPER.writeValueAsString(new MapBean()));

        
        assertEquals("{\"b\":\"2\"}", MAPPER.writeValueAsString(new MapFieldBean()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testWithMap
    public void testWithMap() throws Exception {
        assertEquals("42", MAPPER.writeValueAsString(new MapAsNumber()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testWithList
    public void testWithList() throws Exception {
        assertEquals("13", MAPPER.writeValueAsString(new ListAsNumber()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testInList
    public void testInList() throws Exception {
        IntExtBean bean = new IntExtBean();
        bean.add(1);
        bean.add(2);
        String json = MAPPER.writeValueAsString(bean);
        assertEquals(json, "{\"values\":[{\"i\":1},{\"i\":2}]}");
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testPolymorphicSerdeWithDelegate
    public void testPolymorphicSerdeWithDelegate() throws Exception
    {
	    AdditionInterface adder = new AdditionInterfaceImpl(1);
	
	    assertEquals(2, adder.add(1));
	    String json = MAPPER.writeValueAsString(adder);
	    assertEquals("{\"boingo\":\"boopsy\",\"toAdd\":1}", json);
	    assertEquals(2, MAPPER.readValue(json, AdditionInterface.class).add(1));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testJsonValueWithCustomOverride
    public void testJsonValueWithCustomOverride() throws Exception
    {
        final Bean838 INPUT = new Bean838();

        
        assertEquals(quote("value"), MAPPER.writeValueAsString(INPUT));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(Bean838.class, new Bean838Serializer())
            );
        assertEquals("42", mapper.writeValueAsString(INPUT));
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testNotKarl
    public void testNotKarl() throws IOException {
        final String serialized = MAPPER.writeValueAsString(new NotKarlBean());
        assertEquals("{\"map\":{\"Not Karl\":1}}", serialized);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testKarl
    public void testKarl() throws IOException {
        final String serialized = MAPPER.writeValueAsString(new KarlBean());
        assertEquals("{\"map\":{\"Karl\":1}}", serialized);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testBoth
    public void testBoth() throws IOException
    {
        
        final ObjectMapper mapper = new ObjectMapper();
        final String value1 = mapper.writeValueAsString(new NotKarlBean());
        assertEquals("{\"map\":{\"Not Karl\":1}}", value1);
        final String value2 = mapper.writeValueAsString(new KarlBean());
        assertEquals("{\"map\":{\"Karl\":1}}", value2);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testCustomForEnum
    public void testCustomForEnum() throws IOException
    {
        
        final ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test");
        mod.addKeySerializer(ABC.class, new ABCKeySerializer());
        mapper.registerModule(mod);

        String json = mapper.writeValueAsString(new ABCMapWrapper());
        assertEquals("{\"stuff\":{\"xxxB\":\"bar\"}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testCustomNullSerializers
    public void testCustomNullSerializers() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer("NULL-KEY"));
        mapper.getSerializerProvider().setNullValueSerializer(new NullValueSerializer("NULL"));
        Map<String,Integer> input = new HashMap<>();
        input.put(null, 3);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"NULL-KEY\":3}", json);
        json = mapper.writeValueAsString(new Object[] { 1, null, true });
        assertEquals("[1,\"NULL\",true]", json);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testCustomEnumInnerMapKey
    public void testCustomEnumInnerMapKey() throws Exception {
        Map<Outer, Object> outerMap = new HashMap<Outer, Object>();
        Map<ABC, Map<String, String>> map = new EnumMap<ABC, Map<String, String>>(ABC.class);
        Map<String, String> innerMap = new HashMap<String, String>();
        innerMap.put("one", "1");
        map.put(ABC.A, innerMap);
        outerMap.put(Outer.inner, map);
        final ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test");
        mod.setMixInAnnotation(ABC.class, ABCMixin.class);
        mod.addKeySerializer(ABC.class, new ABCKeySerializer());
        mapper.registerModule(mod);

        JsonNode tree = mapper.convertValue(outerMap, JsonNode.class);

        JsonNode innerNode = tree.get("inner");
        String key = innerNode.fieldNames().next();
        assertEquals("xxxA", key);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testUnWrappedMapWithDefaultType
    public void testUnWrappedMapWithDefaultType() throws Exception{
        final ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test");
        mod.addKeySerializer(ABC.class, new ABCKeySerializer());
        mapper.registerModule(mod);

        TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        typer = typer.init(JsonTypeInfo.Id.NAME, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        
        typer = typer.typeIdVisibility(true);
        mapper.setDefaultTyping(typer);

        Map<ABC,String> stuff = new HashMap<ABC,String>();
        stuff.put(ABC.B, "bar");
        String json = mapper.writerFor(new TypeReference<Map<ABC, String>>() {})
                .writeValueAsString(stuff);
        assertEquals("{\"@type\":\"HashMap\",\"xxxB\":\"bar\"}", json);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testUnWrappedMapWithKeySerializer
    public void testUnWrappedMapWithKeySerializer() throws Exception{
        SimpleModule mod = new SimpleModule("test");
        mod.addKeySerializer(ABC.class, new ABCKeySerializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(mod)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_NULL_MAP_VALUES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            ;

        Map<ABC,BAR<?>> stuff = new HashMap<ABC,BAR<?>>();
        stuff.put(ABC.B, new BAR<String>("bar"));
        String json = mapper.writerFor(new TypeReference<Map<ABC,BAR<?>>>() {})
                .writeValueAsString(stuff);
        assertEquals("{\"xxxB\":\"bar\"}", json);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testDynamicMapKeys
    public void testDynamicMapKeys() throws Exception
    {
        Map<Object,Integer> stuff = new LinkedHashMap<Object,Integer>();
        stuff.put(AbcLC.B, Integer.valueOf(3));
        stuff.put(new UCString("foo"), Integer.valueOf(4));
        String json = MAPPER.writeValueAsString(stuff);
        assertEquals(aposToQuotes("{'b':3,'FOO':4}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testUsingObjectWriter
    public void testUsingObjectWriter() throws IOException
    {
        ObjectWriter w = MAPPER.writerFor(Object.class);
        Map<String,Object> map = new LinkedHashMap<String,Object>();
        map.put("a", 1);
        String json = w.writeValueAsString(map);
        assertEquals(aposToQuotes("{'a':1}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapSerializer
    public void testMapSerializer() throws IOException
    {
        assertEquals("\"{a=b, c=d}\"", MAPPER.writeValueAsString(new PseudoMap("a", "b", "c", "d")));
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapKeySetValuesSerialization
    public void testMapKeySetValuesSerialization() throws IOException
    {
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", "b");
        assertEquals("[\"a\"]", MAPPER.writeValueAsString(map.keySet()));
        assertEquals("[\"b\"]", MAPPER.writeValueAsString(map.values()));

        
        map = new TreeMap<String,String>();
        map.put("c", "d");
        assertEquals("[\"c\"]", MAPPER.writeValueAsString(map.keySet()));
        assertEquals("[\"d\"]", MAPPER.writeValueAsString(map.values()));

        
        map = new ConcurrentHashMap<String,String>();
        map.put("e", "f");
        assertEquals("[\"e\"]", MAPPER.writeValueAsString(map.keySet()));
        assertEquals("[\"f\"]", MAPPER.writeValueAsString(map.values()));
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testOrderByKey
    public void testOrderByKey() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        assertFalse(m.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS));
        LinkedHashMap<String,Integer> map = new LinkedHashMap<String,Integer>();
        map.put("b", 3);
        map.put("a", 6);
        
        assertEquals("{\"b\":3,\"a\":6}", m.writeValueAsString(map));
        
        ObjectWriter sortingW =  m.writer(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        assertEquals("{\"a\":6,\"b\":3}", sortingW.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testOrderByWithNulls
    public void testOrderByWithNulls() throws IOException
    {
        ObjectWriter sortingW = MAPPER.writer(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        
        Map<String,Integer> mapWithNullKey = new LinkedHashMap<String,Integer>();
        mapWithNullKey.put(null, 1);
        mapWithNullKey.put("b", 2);
        
        try {
             sortingW.writeValueAsString(mapWithNullKey);
            
        } catch (JsonMappingException e) {
            verifyException(e, "Null key for a Map not allowed");
        }
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testOrderByKeyViaProperty
    public void testOrderByKeyViaProperty() throws IOException
    {
        MapOrderingBean input = new MapOrderingBean("c", "b", "a");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'map':{'a':3,'b':2,'c':1}}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapEntry
    public void testMapEntry() throws IOException
    {
        StringIntMapEntry input = new StringIntMapEntry("answer", 42);
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'answer':42}"), json);

        StringIntMapEntry[] array = new StringIntMapEntry[] { input };
        json = MAPPER.writeValueAsString(array);
        assertEquals(aposToQuotes("[{'answer':42}]"), json);

        
        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(DefaultTyping.NON_FINAL);
        json = mapper.writeValueAsString(input);
        assertEquals(aposToQuotes("['"+StringIntMapEntry.class.getName()+"',{'answer':42}]"),
                json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapEntryWrapper
    public void testMapEntryWrapper() throws IOException
    {
        StringIntMapEntryWrapper input = new StringIntMapEntryWrapper("answer", 42);
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'value':{'answer':42}}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testNullJsonMapping691
    public void testNullJsonMapping691() throws Exception
    {
        MapWithTypedValues input = new MapWithTypedValues();
        input.put("id", "Test");
        input.put("NULL", null);

        String json = MAPPER.writeValueAsString(input);

        assertEquals(aposToQuotes("{'@type':'mymap','id':'Test','NULL':null}"),
                json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testNullJsonInTypedMap691
    public void testNullJsonInTypedMap691() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("NULL", null);
    
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Object.class, Mixin691.class);
        String json = mapper.writeValueAsString(map);
        assertEquals("{\"@class\":\"java.util.HashMap\",\"NULL\":null}", json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testConcurrentMaps
    public void testConcurrentMaps() throws Exception
    {
        final ObjectWriter w = MAPPER.writer(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

        Map<String,String> input = new ConcurrentSkipListMap<String,String>();
        input.put("x", "y");
        input.put("a", "b");
        String json = w.writeValueAsString(input);
        assertEquals(aposToQuotes("{'a':'b','x':'y'}"), json);

        input = new ConcurrentHashMap<String,String>();
        input.put("x", "y");
        input.put("a", "b");
        json = w.writeValueAsString(input);
        assertEquals(aposToQuotes("{'a':'b','x':'y'}"), json);

        
        input = new Hashtable<String,String>();
        input.put("x", "y");
        input.put("a", "b");
        json = w.writeValueAsString(input);
        assertEquals(aposToQuotes("{'a':'b','x':'y'}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testSuperClass
    public void testSuperClass() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        SubType bean = new SubType();

        
        Map<String,Object> result = writeAndMap(mapper, bean);
        assertEquals(4, result.size());
        assertEquals("a", result.get("a"));
        assertEquals(Integer.valueOf(3), result.get("b"));
        assertEquals("x", result.get("a2"));
        assertEquals(Boolean.TRUE, result.get("b2"));

        
        ObjectWriter w = mapper.writerFor(BaseType.class);
        String json = w.writeValueAsString(bean);
        result = (Map<String,Object>)mapper.readValue(json, Map.class);
        assertEquals(2, result.size());
        assertEquals("a", result.get("a"));
        assertEquals(Integer.valueOf(3), result.get("b"));
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testSuperInterface
    public void testSuperInterface() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        SubType bean = new SubType();

        
        ObjectWriter w = mapper.writerFor(BaseInterface.class);
        String json = w.writeValueAsString(bean);
        @SuppressWarnings("unchecked")
        Map<String,Object> result = mapper.readValue(json, Map.class);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get("b"));
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testInArray
    public void testInArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.configure(MapperFeature.USE_STATIC_TYPING, true);
        SubType[] ob = new SubType[] { new SubType() };
        String json = mapper.writerFor(BaseInterface[].class).writeValueAsString(ob);
        
        assertEquals("[{\"b\":3}]", json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testIncompatibleRootType
    public void testIncompatibleRootType() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        SubType bean = new SubType();

        
        ObjectWriter w = mapper.writerFor(HashMap.class);
        try {
            w.writeValueAsString(bean);
            fail("Should have failed due to incompatible type");
        } catch (JsonProcessingException e) {
            verifyException(e, "Incompatible types");
        }

        
        try {
            w.writeValueAsBytes(bean);
            fail("Should have failed due to incompatible type");
        } catch (JsonProcessingException e) {
            verifyException(e, "Incompatible types");
        }
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testJackson398
    public void testJackson398() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        JavaType collectionType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, BaseClass398.class);
        List<TestClass398> typedList = new ArrayList<TestClass398>();
        typedList.add(new TestClass398());

        final String EXP = "[{\"beanClass\":\"TestRootType$TestClass398\",\"property\":\"aa\"}]";
        
        
        String json = mapper.writerFor(collectionType).writeValueAsString(typedList);
        assertEquals(EXP, json);

        StringWriter out = new StringWriter();
        JsonFactory f = new JsonFactory();
        mapper.writerFor(collectionType).writeValue(f.createGenerator(out), typedList);

        assertEquals(EXP, out.toString());
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testRootWrapping
    public void testRootWrapping() throws Exception
    {
        String json = WRAP_ROOT_MAPPER.writeValueAsString(new StringWrapper("abc"));
        assertEquals("{\"StringWrapper\":{\"str\":\"abc\"}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testIssue456WrapperPart
    public void testIssue456WrapperPart() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        assertEquals("123", mapper.writerFor(Integer.TYPE).writeValueAsString(Integer.valueOf(123)));
        assertEquals("456", mapper.writerFor(Long.TYPE).writeValueAsString(Long.valueOf(456L)));
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testRootNameAnnotation
    public void testRootNameAnnotation() throws Exception
    {
        String json = WRAP_ROOT_MAPPER.writeValueAsString(new WithRootName());
        assertEquals("{\"root\":{\"a\":3}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testRootNameWithExplicitType
    public void testRootNameWithExplicitType() throws Exception
    {
        TestCommandChild cmd = new TestCommandChild();
        cmd.uuid = "1234";
        cmd.type = 1;

        ObjectWriter writer = WRAP_ROOT_MAPPER.writerFor(TestCommandParent.class);
        String json =  writer.writeValueAsString(cmd);

        assertEquals("{\"TestCommandParent\":{\"uuid\":\"1234\",\"type\":1}}", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testAtomicBoolean
    public void testAtomicBoolean() throws Exception
    {
        assertEquals("true", MAPPER.writeValueAsString(new AtomicBoolean(true)));
        assertEquals("false", MAPPER.writeValueAsString(new AtomicBoolean(false)));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testAtomicInteger
    public void testAtomicInteger() throws Exception
    {
        assertEquals("1", MAPPER.writeValueAsString(new AtomicInteger(1)));
        assertEquals("-9", MAPPER.writeValueAsString(new AtomicInteger(-9)));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testAtomicLong
    public void testAtomicLong() throws Exception
    {
        assertEquals("0", MAPPER.writeValueAsString(new AtomicLong(0)));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testAtomicReference
    public void testAtomicReference() throws Exception
    {
        String[] strs = new String[] { "abc" };
        assertEquals("[\"abc\"]", MAPPER.writeValueAsString(new AtomicReference<String[]>(strs)));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testCustomSerializer
    public void testCustomSerializer() throws Exception
    {
        final String VALUE = "fooBAR";
        String json = MAPPER.writeValueAsString(new UCStringWrapper(VALUE));
        assertEquals(json, aposToQuotes("{'value':'FOOBAR'}"));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testContextualAtomicReference
    public void testContextualAtomicReference() throws Exception
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        final ObjectMapper mapper = objectMapper();
        mapper.setDateFormat(df);
        ContextualOptionals input = new ContextualOptionals();
        input.date = new AtomicReference<>(new Date(0L));
        input.date1 = new AtomicReference<>(new Date(0L));
        input.date2 = new AtomicReference<>(new Date(0L));
        final String json = mapper.writeValueAsString(input);
        assertEquals(aposToQuotes(
                "{'date1':'1970+01+01','date2':'1970*01*01','date':'1970/01/01'}"),
                json);
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testPolymorphicReferenceSimple
    public void testPolymorphicReferenceSimple() throws Exception
    {
        final String EXPECTED = "{\"type\":\"Foo\",\"foo\":42}";
        String json = MAPPER.writeValueAsString(new ContainerA());
        assertEquals("{\"strategy\":" + EXPECTED + "}", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testPolymorphicReferenceListOf
    public void testPolymorphicReferenceListOf() throws Exception
    {
        final String EXPECTED = "{\"type\":\"Foo\",\"foo\":42}";
        
        
        String json = MAPPER.writeValueAsString(new ContainerB());
        assertEquals("{\"strategy\":[" + EXPECTED + "]}", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testCollections
    public void testCollections() throws IOException
    {
        
        final int entryLen = 98;

        for (int type = 0; type < 4; ++type) {
            Object value;

            if (type == 0) { 
                int[] ints = new int[entryLen];
                for (int i = 0; i < entryLen; ++i) {
                    ints[i] = Integer.valueOf(i);
                }
                value = ints;
            } else {
                Collection<Integer> c;

                switch (type) {
                case 1:
                    c = new LinkedList<Integer>();
                    break;
                case 2:
                    c = new TreeSet<Integer>(); 
                    break;
                default:
                    c = new ArrayList<Integer>();
                    break;
                }
                for (int i = 0; i < entryLen; ++i) {
                    c.add(Integer.valueOf(i));
                }
                value = c;
            }
            String json = MAPPER.writeValueAsString(value);
            
            
            JsonParser jp = new JsonFactory().createParser(json);
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = 0; i < entryLen; ++i) {
                assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                assertEquals(i, jp.getIntValue());
            }
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testBigCollection
    public void testBigCollection() throws IOException
    {
        final int COUNT = 9999;
        ArrayList<Integer> value = new ArrayList<Integer>();
        for (int i = 0; i <= COUNT; ++i) {
            value.add(i);
        }
        
        for (int mode = 0; mode < 3; ++mode) {
            JsonParser jp = null;
            switch (mode) {
            case 0:
                {
                    byte[] data = MAPPER.writeValueAsBytes(value);
                    jp = new JsonFactory().createParser(data);
                }
                break;
            case 1:
                {
                    StringWriter sw = new StringWriter(value.size());
                    MAPPER.writeValue(sw, value);
                    jp = createParserUsingReader(sw.toString());
                }
                break;
            case 2:
                {
                    String str = MAPPER.writeValueAsString(value);
                    jp = createParserUsingReader(str);
                }
                break;
            }

            
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = 0; i <= COUNT; ++i) {
                assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                assertEquals(i, jp.getIntValue());
            }
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testEnumMap
    public void testEnumMap() throws IOException
    {
        EnumMap<Key,String> map = new EnumMap<Key,String>(Key.class);
        map.put(Key.B, "xyz");
        map.put(Key.C, "abc");
        
        String json = MAPPER.writeValueAsString(map);
        assertEquals("{\"B\":\"xyz\",\"C\":\"abc\"}",json.trim());
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testEmptyBeanCollection
    public void testEmptyBeanCollection() throws IOException
    {
        Collection<Object> x = new ArrayList<Object>();
        x.add("foobar");
        CollectionBean cb = new CollectionBean(x);
        Map<String,Object> result = writeAndMap(MAPPER, cb);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("values"));
        Collection<Object> x2 = (Collection<Object>) result.get("values");
        assertNotNull(x2);
        assertEquals(x, x2);
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testNullBeanCollection
    public void testNullBeanCollection()
        throws IOException
    {
        CollectionBean cb = new CollectionBean(null);
        Map<String,Object> result = writeAndMap(MAPPER, cb);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("values"));
        assertNull(result.get("values"));
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testEmptyBeanEnumMap
    public void testEmptyBeanEnumMap() throws IOException
    {
        EnumMap<Key,String> map = new EnumMap<Key,String>(Key.class);
        EnumMapBean b = new EnumMapBean(map);
        Map<String,Object> result = writeAndMap(MAPPER, b);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("map"));
        
        Map<Object,Object> map2 = (Map<Object,Object>) result.get("map");
        assertNotNull(map2);
        assertEquals(0, map2.size());
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testNullBeanEnumMap
    public void testNullBeanEnumMap() throws IOException
    {
        EnumMapBean b = new EnumMapBean(null);
        Map<String,Object> result = writeAndMap(MAPPER, b);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("map"));
        assertNull(result.get("map"));
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testListSerializer
    public void testListSerializer() throws IOException
    {
        assertEquals(quote("[ab, cd, ef]"),
                MAPPER.writeValueAsString(new PseudoList("ab", "cd", "ef")));
        assertEquals(quote("[]"),
                MAPPER.writeValueAsString(new PseudoList()));
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testEmptyListOrArray
    public void testEmptyListOrArray() throws IOException
    {
        
        EmptyListBean list = new EmptyListBean();
        EmptyArrayBean array = new EmptyArrayBean();
        assertTrue(MAPPER.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS));
        assertEquals("{\"empty\":[]}", MAPPER.writeValueAsString(list));
        assertEquals("{\"empty\":[]}", MAPPER.writeValueAsString(array));

        
        ObjectMapper m = new ObjectMapper();
        m.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        assertEquals("{}", m.writeValueAsString(list));
        assertEquals("{}", m.writeValueAsString(array));
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testStaticList
    public void testStaticList() throws IOException
    {
        
        StaticListWrapper w = new StaticListWrapper("a", "b", "c");
        String json = MAPPER.writeValueAsString(w);
        assertEquals(aposToQuotes("{'list':['a','b','c']}"), json);

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        json = mapper.writeValueAsString(w);
        assertEquals(aposToQuotes(String.format("['%s',{'list':['%s',['a','b','c']]}]",
                w.getClass().getName(), w.list.getClass().getName())), json);
    }

// com.fasterxml.jackson.databind.struct.PojoAsArray646Test::testWithCustomTypeId
    public void testWithCustomTypeId() throws Exception {

        List<TheItem.NestedItem> nestedList = new ArrayList<TheItem.NestedItem>();
        nestedList.add(new TheItem.NestedItem("foo1"));
        nestedList.add(new TheItem.NestedItem("foo2"));
        TheItem item = new TheItem("first", false, nestedList);
        Outer outer = new Outer();
        outer.getAttributes().put("entry1", item);

        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(outer);

        Outer result = MAPPER.readValue(json, Outer.class);
        assertNotNull(result);
        assertNotNull(result.attributes);
        assertEquals(1, result.attributes.size());
    }

// com.fasterxml.jackson.databind.struct.TestBackRefsWithPolymorphic::testDeserialize
    public void testDeserialize() throws IOException
    {
        PropertySheet input = MAPPER.readValue(JSON, PropertySheet.class);
        assertEquals(JSON, MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.struct.TestBackRefsWithPolymorphic::testSerialize
    public void testSerialize() throws IOException
    {
        PropertySheet sheet = new PropertySheetImpl();

        sheet.addProperty(new StringPropertyImpl("p1name", "p1value"));
        sheet.addProperty(new StringPropertyImpl("p2name", "p2value"));
        String actual = MAPPER.writeValueAsString(sheet);
        assertEquals(JSON, actual);
    }

// com.fasterxml.jackson.databind.struct.TestForwardReference::testForwardRef
	public void testForwardRef() throws IOException {
		MAPPER.readValue("{" +
				"  \"@type\" : \"TestForwardReference$ForwardReferenceContainerClass\"," +
				"  \"frc\" : \"willBeForwardReferenced\"," +
				"  \"yac\" : {" +
				"    \"@type\" : \"TestForwardReference$YetAnotherClass\"," +
				"    \"frc\" : {" +
				"      \"@type\" : \"One\"," +
				"      \"id\" : \"willBeForwardReferenced\"" +
				"    }," +
				"    \"id\" : \"anId\"" +
				"  }," +
				"  \"id\" : \"ForwardReferenceContainerClass1\"" +
				"}", ForwardReferenceContainerClass.class);

	}

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testSimpleRefs
    public void testSimpleRefs() throws Exception
    {
        SimpleTreeNode root = new SimpleTreeNode("root");
        SimpleTreeNode child = new SimpleTreeNode("kid");
        root.child = child;
        child.parent = root;
        
        String json = MAPPER.writeValueAsString(root);
        
        SimpleTreeNode resultNode = MAPPER.readValue(json, SimpleTreeNode.class);
        assertEquals("root", resultNode.name);
        SimpleTreeNode resultChild = resultNode.child;
        assertNotNull(resultChild);
        assertEquals("kid", resultChild.name);
        assertSame(resultChild.parent, resultNode);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testSimpleRefsWithGetter
    public void testSimpleRefsWithGetter() throws Exception
    {
        SimpleTreeNode2 root = new SimpleTreeNode2("root");
        SimpleTreeNode2 child = new SimpleTreeNode2("kid");
        root.child = child;
        child.parent = root;
        
        String json = MAPPER.writeValueAsString(root);
        
        SimpleTreeNode2 resultNode = MAPPER.readValue(json, SimpleTreeNode2.class);
        assertEquals("root", resultNode.name);
        SimpleTreeNode2 resultChild = resultNode.child;
        assertNotNull(resultChild);
        assertEquals("kid", resultChild.name);
        assertSame(resultChild.parent, resultNode);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testFullRefs
    public void testFullRefs() throws Exception
    {
        FullTreeNode root = new FullTreeNode("root");
        FullTreeNode child1 = new FullTreeNode("kid1");
        FullTreeNode child2 = new FullTreeNode("kid2");
        root.firstChild = child1;
        child1.parent = root;
        child1.next = child2;
        child2.prev = child1;
        
        String json = MAPPER.writeValueAsString(root);
        
        FullTreeNode resultNode = MAPPER.readValue(json, FullTreeNode.class);
        assertEquals("root", resultNode.name);
        FullTreeNode resultChild = resultNode.firstChild;
        assertNotNull(resultChild);
        assertEquals("kid1", resultChild.name);
        assertSame(resultChild.parent, resultNode);

        
        assertNull(resultChild.prev);
        FullTreeNode resultChild2 = resultChild.next;
        assertNotNull(resultChild2);
        assertEquals("kid2", resultChild2.name);
        assertSame(resultChild, resultChild2.prev);
        assertNull(resultChild2.next);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testArrayOfRefs
    public void testArrayOfRefs() throws Exception
    {
        NodeArray root = new NodeArray();
        ArrayNode node1 = new ArrayNode("a");
        ArrayNode node2 = new ArrayNode("b");
        root.nodes = new ArrayNode[] { node1, node2 };
        String json = MAPPER.writeValueAsString(root);
        
        NodeArray result = MAPPER.readValue(json, NodeArray.class);
        ArrayNode[] kids = result.nodes;
        assertNotNull(kids);
        assertEquals(2, kids.length);
        assertEquals("a", kids[0].name);
        assertEquals("b", kids[1].name);
        assertSame(result, kids[0].parent);
        assertSame(result, kids[1].parent);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testListOfRefs
    public void testListOfRefs() throws Exception
    {
        NodeList root = new NodeList();
        NodeForList node1 = new NodeForList("a");
        NodeForList node2 = new NodeForList("b");
        root.nodes = Arrays.asList(node1, node2);
        String json = MAPPER.writeValueAsString(root);
        
        NodeList result = MAPPER.readValue(json, NodeList.class);
        List<NodeForList> kids = result.nodes;
        assertNotNull(kids);
        assertEquals(2, kids.size());
        assertEquals("a", kids.get(0).name);
        assertEquals("b", kids.get(1).name);
        assertSame(result, kids.get(0).parent);
        assertSame(result, kids.get(1).parent);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testMapOfRefs
    public void testMapOfRefs() throws Exception
    {
        NodeMap root = new NodeMap();
        NodeForMap node1 = new NodeForMap("a");
        NodeForMap node2 = new NodeForMap("b");
        Map<String,NodeForMap> nodes = new HashMap<String, NodeForMap>();
        nodes.put("a1", node1);
        nodes.put("b2", node2);
        root.nodes = nodes;
        String json = MAPPER.writeValueAsString(root);
        
        NodeMap result = MAPPER.readValue(json, NodeMap.class);
        Map<String,NodeForMap> kids = result.nodes;
        assertNotNull(kids);
        assertEquals(2, kids.size());
        assertNotNull(kids.get("a1"));
        assertNotNull(kids.get("b2"));
        assertEquals("a", kids.get("a1").name);
        assertEquals("b", kids.get("b2").name);
        assertSame(result, kids.get("a1").parent);
        assertSame(result, kids.get("b2").parent);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testAbstract368
    public void testAbstract368() throws Exception
    {
        AbstractNode parent = new ConcreteNode("p");
        AbstractNode child = new ConcreteNode("c");
        parent.next = child;
        child.prev = parent;

        
        String json = MAPPER.writeValueAsString(parent);

        AbstractNode root = MAPPER.readValue(json, AbstractNode.class);

        assertEquals(ConcreteNode.class, root.getClass());
        assertEquals("p", root.id);
        assertNull(root.prev);
        AbstractNode leaf = root.next;
        assertNotNull(leaf);
        assertEquals("c", leaf.id);
        assertSame(root, leaf.prev);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testIssue693
    public void testIssue693() throws Exception
    {
        Parent parent = new Parent();
        parent.addChild(new Child("foo"));
        parent.addChild(new Child("bar"));
        byte[] bytes = MAPPER.writeValueAsBytes(parent);
        Parent value = MAPPER.readValue(bytes, Parent.class); 
        for (Child child : value.children) {
            assertEquals(value, child.getParent());
        }
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testIssue708
    public void testIssue708() throws Exception
    {
        Advertisement708 ad = MAPPER.readValue("{\"title\":\"Hroch\",\"photos\":[{\"id\":3}]}", Advertisement708.class);      
        assertNotNull(ad);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithTypeInfo::testDefaultUnwrappedWithTypeInfo
	public void testDefaultUnwrappedWithTypeInfo() throws Exception
	{
	    Outer outer = new Outer();
	    outer.setP1("101");

	    Inner inner = new Inner();
	    inner.setP2("202");
	    outer.setInner(inner);

	    ObjectMapper mapper = new ObjectMapper();

	    try {
	        mapper.writeValueAsString(outer);
	         fail("Expected exception to be thrown.");
	    } catch (JsonMappingException ex) {
	        verifyException(ex, "requires use of type information");
	    }
	}

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithTypeInfo::testUnwrappedWithTypeInfoAndFeatureDisabled
	public void testUnwrappedWithTypeInfoAndFeatureDisabled() throws Exception
	{
		Outer outer = new Outer();
		outer.setP1("101");

		Inner inner = new Inner();
		inner.setP2("202");
		outer.setInner(inner);

		ObjectMapper mapper = new ObjectMapper();
		mapper = mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);

		String json = mapper.writeValueAsString(outer);
		assertEquals("{\"@type\":\"OuterType\",\"p1\":\"101\",\"p2\":\"202\"}", json);
	}

// com.fasterxml.jackson.databind.type.RecursiveType1658Test::testRecursive1658
    public void testRecursive1658() throws Exception
    {
        Tree<String> t = new Tree<String>(Arrays.asList("hello", "world"));
        ObjectMapper mapper = new ObjectMapper();

        final TypeResolverBuilder<?> typer = new StdTypeResolverBuilder()
                .init(JsonTypeInfo.Id.CLASS, null)
                .inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);

        String res = mapper.writeValueAsString(t);

        Tree<?> tRead = mapper.readValue(res, Tree.class);

        assertNotNull(tRead);
    }
