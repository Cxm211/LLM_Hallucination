// buggy code
    public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass)
    {
        // simple optimization to avoid costly introspection if type-erased type does NOT differ
        final Class<?> rawBase = baseType.getRawClass();
        if (rawBase == subclass) {
            return baseType;
        }

        JavaType newType;

        // also: if we start from untyped, not much to save
        do { // bogus loop to be able to break
            if (rawBase == Object.class) {
                newType = _fromClass(null, subclass, TypeBindings.emptyBindings());
                break;
            }
            if (!rawBase.isAssignableFrom(subclass)) {
                throw new IllegalArgumentException(String.format(
                        "Class %s not subtype of %s", subclass.getName(), baseType));
            }
            // A few special cases where we can simplify handling:

            // (1) Original target type has no generics -- just resolve subtype
            if (baseType.getBindings().isEmpty()) {
                newType = _fromClass(null, subclass, TypeBindings.emptyBindings());     
                break;
            }
            // (2) A small set of "well-known" List/Map subtypes where can take a short-cut
            if (baseType.isContainerType()) {
                if (baseType.isMapLikeType()) {
                    if ((subclass == HashMap.class)
                            || (subclass == LinkedHashMap.class)
                            || (subclass == EnumMap.class)
                            || (subclass == TreeMap.class)) {
                        newType = _fromClass(null, subclass,
                                TypeBindings.create(subclass, baseType.getKeyType(), baseType.getContentType()));
                        break;
                    }
                } else if (baseType.isCollectionLikeType()) {
                    if ((subclass == ArrayList.class)
                            || (subclass == LinkedList.class)
                            || (subclass == HashSet.class)
                            || (subclass == TreeSet.class)) {
                        newType = _fromClass(null, subclass,
                                TypeBindings.create(subclass, baseType.getContentType()));
                        break;
                    }
                    // 29-Oct-2015, tatu: One further shortcut: there are variants of `EnumSet`,
                    //    but they are impl details and we basically do not care...
                    if (rawBase == EnumSet.class) {
                        return baseType;
                    }
                }
            }
            // (3) Sub-class does not take type parameters -- just resolve subtype
            int typeParamCount = subclass.getTypeParameters().length;
            if (typeParamCount == 0) {
                newType = _fromClass(null, subclass, TypeBindings.emptyBindings());     
                break;
            }
            // (4) If all else fails, do the full traversal using placeholders
            TypeBindings tb = _bindingsForSubtype(baseType, typeParamCount, subclass);
            newType = _fromClass(null, subclass, tb);

        } while (false);

        // 25-Sep-2016, tatu: As per [databind#1384] also need to ensure handlers get
        //   copied as well
        newType = newType.withHandlersFrom(baseType);
        return newType;
    }

    public JavaType constructParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        int len = parameterClasses.length;
        JavaType[] pt = new JavaType[len];
        for (int i = 0; i < len; ++i) {
            pt[i] = _fromClass(null, parameterClasses[i], null);
        }
        return constructParametricType(parametrized, pt);
    }

    protected JavaType parseType(MyTokenizer tokens)
        throws IllegalArgumentException
    {
        if (!tokens.hasMoreTokens()) {
            throw _problem(tokens, "Unexpected end-of-string");
        }
        Class<?> base = findClass(tokens.nextToken(), tokens);

        // either end (ok, non generic type), or generics
        if (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if ("<".equals(token)) {
                List<JavaType> parameterTypes = parseTypes(tokens);
                TypeBindings b = TypeBindings.create(base, parameterTypes);
                return _factory._fromClass(null, base, b);
            }
            // can be comma that separates types, or closing '>'
            tokens.pushBack(token);
        }
        return _factory._fromClass(null, base, null);
    }

// relevant test
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

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithObject
    public void testDeserializationWithObject() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("{\"type\": \"mine\", \"blah\": [\"a\", \"b\", \"c\"]}");
        assertTrue(inter instanceof MyInter);
        assertFalse(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b", "c"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithString
    public void testDeserializationWithString() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("\"a,b,c,d\"");
        assertTrue(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b", "c", "d"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithArray
    public void testDeserializationWithArray() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("[\"a\", \"b\", \"c\", \"d\"]");
        assertTrue(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b", "c", "d"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithArrayOfSize2
    public void testDeserializationWithArrayOfSize2() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("[\"a\", \"b\"]");
        assertTrue(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDefaultAsNoClass
    public void testDefaultAsNoClass() throws Exception
    {
        Object ob = MAPPER.readerFor(DefaultWithNoClass.class).readValue("{ }");
        assertNull(ob);
        ob = MAPPER.readerFor(DefaultWithNoClass.class).readValue("{ \"bogus\":3 }");
        assertNull(ob);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDefaultAsVoid
    public void testDefaultAsVoid() throws Exception
    {
        Object ob = MAPPER.readerFor(DefaultWithVoidAsDefault.class).readValue("{ }");
        assertNull(ob);
        ob = MAPPER.readerFor(DefaultWithVoidAsDefault.class).readValue("{ \"bogus\":3 }");
        assertNull(ob);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testBadTypeAsNull
    public void testBadTypeAsNull() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        Object ob = mapper.readValue("{}", MysteryPolymorphic.class);
        assertNull(ob);
        ob = mapper.readValue("{ \"whatever\":13}", MysteryPolymorphic.class);
        assertNull(ob);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testInvalidTypeId511
    public void testInvalidTypeId511() throws Exception {
        ObjectReader reader = MAPPER.reader().without(
                DeserializationFeature.FAIL_ON_INVALID_SUBTYPE,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
        );
        String json = "{\"many\":[{\"sub1\":{\"a\":\"foo\"}},{\"sub2\":{\"b\":\"bar\"}}]}" ;
        Good goodResult = reader.forType(Good.class).readValue(json) ;
        assertNotNull(goodResult) ;
        Bad badResult = reader.forType(Bad.class).readValue(json);
        assertNotNull(badResult);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDefaultImplWithObjectWrapper
    public void testDefaultImplWithObjectWrapper() throws Exception
    {
        BaseFor656 value = MAPPER.readValue(aposToQuotes("{'foobar':{'a':3}}"), BaseFor656.class);
        assertNotNull(value);
        assertEquals(ImplFor656.class, value.getClass());
        assertEquals(3, ((ImplFor656) value).a);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testUnknownTypeIDRecovery
    public void testUnknownTypeIDRecovery() throws Exception
    {
        ObjectReader reader = MAPPER.readerFor(CallRecord.class).without(
                DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        String json = aposToQuotes("{'version':0.0,'application':'123',"
                +"'item':{'type':'xevent','location':'location1'},"
                +"'item2':{'type':'event','location':'location1'}}");
        
        CallRecord r = reader.readValue(json);
        assertNull(r.item);
        assertNotNull(r.item2);

        json = aposToQuotes("{'item':{'type':'xevent','location':'location1'}, 'version':0.0,'application':'123'}");
        CallRecord r3 = reader.readValue(json);
        assertNull(r3.item);
        assertEquals("123", r3.application);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testUnknownClassAsSubtype
    public void testUnknownClassAsSubtype() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        BaseWrapper w = mapper.readValue(aposToQuotes
                ("{'value':{'clazz':'com.foobar.Nothing'}}'"),
                BaseWrapper.class);
        assertNotNull(w);
        assertNull(w.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testWithoutEmptyStringAsNullObject1533
    public void testWithoutEmptyStringAsNullObject1533() throws Exception
    {
        ObjectReader r = MAPPER.readerFor(AsPropertyWrapper.class)
                .without(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        try {
            r.readValue("{ \"value\": \"\" }");
            fail("Expected " + JsonMappingException.class);
        } catch (JsonMappingException e) {
            verifyException(e, "missing property 'type'");
            verifyException(e, "contain type id");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testWithEmptyStringAsNullObject1533
    public void testWithEmptyStringAsNullObject1533() throws Exception
    {
        ObjectReader r = MAPPER.readerFor(AsPropertyWrapper.class)
                .with(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        AsPropertyWrapper wrapper = r.readValue("{ \"value\": \"\" }");
        assertNull(wrapper.value);
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

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testSubclassLimits
    public void testSubclassLimits() throws Exception
    {
        try {
            MAPPER.readValue(aposToQuotes("{'value':['"
                    +TheBomb.class.getName()+"',{'a':13}] }"), DateWrapper.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "not subtype of");
            verifyException(e, TheBomb.class.getName());
        }
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

// com.fasterxml.jackson.databind.jsontype.TestTypedArrayDeserialization::testIntList
    public void testIntList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        String JSON = "{\""+TypedListAsWrapper.class.getName()+"\":[4,5, 6]}";
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(TypedListAsWrapper.class, Integer.class);        
        TypedListAsWrapper<Integer> result = m.readValue(JSON, type);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(4), result.get(0));
        assertEquals(Integer.valueOf(5), result.get(1));
        assertEquals(Integer.valueOf(6), result.get(2));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArrayDeserialization::testBooleanListAsProp
    public void testBooleanListAsProp() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        String JSON = "[\""+TypedListAsProp.class.getName()+"\",[true, false]]";
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(TypedListAsProp.class, Boolean.class);        
        TypedListAsProp<Object> result = m.readValue(JSON, type);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Boolean.TRUE, result.get(0));
        assertEquals(Boolean.FALSE, result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArrayDeserialization::testLongListAsWrapper
    public void testLongListAsWrapper() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        
        String JSON = "{\""+TypedListAsWrapper.class.getName()+"\":[1, 3]}";
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(TypedListAsWrapper.class, Long.class);        
        TypedListAsWrapper<Object> result = m.readValue(JSON, type);
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(Long.class, result.get(0).getClass());
        assertEquals(Long.valueOf(1), result.get(0));
        assertEquals(Long.class, result.get(1).getClass());
        assertEquals(Long.valueOf(3), result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArrayDeserialization::testLongArray
    public void testLongArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        m.addMixIn(long[].class, WrapperMixIn.class);
        String JSON = "{\""+long[].class.getName()+"\":[5, 6, 7]}";
        long[] value = m.readValue(JSON, long[].class);
        assertNotNull(value);
        assertEquals(3, value.length);
        assertArrayEquals(new long[] { 5L, 6L, 7L} , value);
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
    public void testIntList() throws Exception
    {
        TypedList<Integer> input = new TypedList<Integer>();
        input.add(5);
        input.add(13);
        
        assertEquals("[\""+TypedList.class.getName()+"\",[5,13]]", serializeAsString(input));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testStringListAsProp
    public void testStringListAsProp() throws Exception
    {
        TypedListAsProp<String> input = new TypedListAsProp<String>();
        input.add("a");
        input.add("b");
        assertEquals("[\""+TypedListAsProp.class.getName()+"\",[\"a\",\"b\"]]",
                serializeAsString(input));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testStringListAsObjectWrapper
    public void testStringListAsObjectWrapper() throws Exception
    {
        TypedListAsWrapper<Boolean> input = new TypedListAsWrapper<Boolean>();
        input.add(true);
        input.add(null);
        input.add(false);
        
        
        
        String expName = "TestTypedArraySerialization$TypedListAsWrapper";
        assertEquals("{\""+expName+"\":[true,null,false]}",
                serializeAsString(input));
    }

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
    public void testSimpleClassAsProperty() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Animal a = m.readValue(asJSONObjectValueString("@classy", Cat.class.getName(),
                "furColor", "tabby", "name", "Garfield"), Animal.class);
        assertNotNull(a);
        assertEquals(Cat.class, a.getClass());
        Cat c = (Cat) a;
        assertEquals("Garfield", c.name);
        assertEquals("tabby", c.furColor);
    }

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

// com.fasterxml.jackson.databind.jsontype.UnknownSubClassTest::testUnknownClassAsSubtype
    public void testUnknownClassAsSubtype() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        BaseWrapper w = mapper.readValue(aposToQuotes
                ("{'value':{'clazz':'com.foobar.Nothing'}}'"),
                BaseWrapper.class);
        assertNotNull(w);
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

// com.fasterxml.jackson.databind.misc.AccessFixTest::testCauseOfThrowableIgnoral
    public void testCauseOfThrowableIgnoral() throws Exception
    {
        final SecurityManager origSecMan = System.getSecurityManager();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        try {
            System.setSecurityManager(new CauseBlockingSecurityManager());
            _testCauseOfThrowableIgnoral(mapper);
        } finally {
            System.setSecurityManager(origSecMan);
        }
    }

// com.fasterxml.jackson.databind.misc.BeanPropertyMapTest::testArrayOutOfBounds884
    public void testArrayOutOfBounds884() throws Exception
    {
        List<SettableBeanProperty> props = new ArrayList<SettableBeanProperty>();
        PropertyMetadata md = PropertyMetadata.STD_REQUIRED;
        props.add(new ObjectIdValueProperty(new MyObjectIdReader("pk"), md));
        props.add(new ObjectIdValueProperty(new MyObjectIdReader("firstName"), md));
        BeanPropertyMap propMap = new BeanPropertyMap(false, props);
        propMap = propMap.withProperty(new ObjectIdValueProperty(new MyObjectIdReader("@id"), md));
        assertNotNull(propMap);
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitiveDeserialization
    public void testCaseInsensitiveDeserialization() throws Exception
    {
        final String JSON = "{\"Value1\" : {\"nAme\" : \"fruit\", \"vALUe\" : \"apple\"}, \"valUE2\" : {\"NAME\" : \"color\", \"value\" : \"red\"}}";
        
        
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        try {
            mapper.readValue(JSON, Issue476Bean.class);
            
            fail("Should not accept improper case properties by default");
        } catch (JsonProcessingException e) {
            verifyException(e, "Unrecognized field");
            assertValidLocation(e.getLocation());
        }

        
        ObjectReader r = INSENSITIVE_MAPPER.readerFor(Issue476Bean.class);
        Issue476Bean result = r.readValue(JSON);
        assertEquals(result.value1.name, "fruit");
        assertEquals(result.value1.value, "apple");
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitive1036
    public void testCaseInsensitive1036() throws Exception
    {
        final String json = "{\"ErrorCode\":2,\"DebugMessage\":\"Signature not valid!\"}";

        BaseResponse response = INSENSITIVE_MAPPER.readValue(json, BaseResponse.class);
        assertEquals(2, response.errorCode);
        assertEquals("Signature not valid!", response.debugMessage);
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCreatorWithInsensitive
    public void testCreatorWithInsensitive() throws Exception
    {
        final String json = aposToQuotes("{'VALUE':3}");
        InsensitiveCreator bean = INSENSITIVE_MAPPER.readValue(json, InsensitiveCreator.class);
        assertEquals(3, bean.v);
    }

// com.fasterxml.jackson.databind.misc.RaceCondition738Test::testRepeatedly
    public void testRepeatedly() throws Exception {
        final int COUNT = 2000;
        for (int i = 0; i < COUNT; i++) {
            runOnce(i, COUNT);
        }
    }

// com.fasterxml.jackson.databind.misc.TestBlocking::testEagerAdvance
    public void testEagerAdvance() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = createParserUsingReader("[ 1  ");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());

        
        Integer I = mapper.readValue(jp, Integer.class);
        assertEquals(Integer.valueOf(1), I);

        
        try {
            jp.nextToken();
        } catch (IOException ioe) {
            verifyException(ioe, "Unexpected end-of-input: expected close marker for ARRAY");
        }
        jp.close();
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

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testClassMixInsTopLevel
    public void testClassMixInsTopLevel() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        
        LeafClass result = m.readValue("{\"a\":\"value\"}", LeafClass.class);
        assertEquals("XXXvalue", result.a);

        
        m = new ObjectMapper();
        m.addMixIn(LeafClass.class, MixIn.class);
        result = m.readValue("{\"a\":\"value\"}", LeafClass.class);
        assertEquals("value", result.a);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testClassMixInsMidLevel
    public void testClassMixInsMidLevel() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BaseClass.class, MixIn.class);
        {
            BaseClass result = m.readValue("{\"a\":\"value\"}", BaseClass.class);
            assertEquals("value", result.a);
        }

        
        {
            LeafClass result = m.readValue("{\"a\":\"value\"}", LeafClass.class);
            assertEquals("XXXvalue", result.a);
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testClassMixInsForObjectClass
    public void testClassMixInsForObjectClass() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(Object.class, MixIn.class);
        
        {
            BaseClass result = m.readValue("{\"a\":\"\"}", BaseClass.class);
            assertEquals("", result.a);
        }

        
        {
            LeafClass result = m.readValue("{\"a\":\"\"}", LeafClass.class);
            assertEquals("XXX", result.a);
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testForConstructor
    public void testForConstructor() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BaseClassWithPrivateCtor.class, MixInForPrivate.class);
        BaseClassWithPrivateCtor result = m.readValue("\"?\"", BaseClassWithPrivateCtor.class);
        assertEquals("?...", result._a);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testForFactoryAndCtor
    public void testForFactoryAndCtor() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        BaseClass result;

        
        result = m.readValue("\"string\"", BaseClass.class);
        assertEquals("string...", result._a);

        
        m = new ObjectMapper();
        m.addMixIn(BaseClass.class, MixIn.class);
        result = m.readValue("\"string\"", BaseClass.class);
        assertEquals("stringX", result._a);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testFactoryMixIn
    public void testFactoryMixIn() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(StringWrapper.class, StringWrapperMixIn.class);
        StringWrapper result = m.readValue("\"a\"", StringWrapper.class);
        assertEquals("a", result._value);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForMethods::testWithAnySetter
    public void testWithAnySetter() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BaseClass.class, MixIn.class);
        BaseClass result = m.readValue("{ \"a\" : 3, \"b\" : true }", BaseClass.class);
        assertNotNull(result);
        assertEquals(2, result.values.size());
        assertEquals(Integer.valueOf(3), result.values.get("a"));
        assertEquals(Boolean.TRUE, result.values.get("b"));
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

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testCollectionDefaulting
    public void testCollectionDefaulting() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(Collection.class, List.class);
        mod.addAbstractTypeMapping(List.class, LinkedList.class);
        mapper.registerModule(mod);
        Collection<?> result = mapper.readValue("[]", Collection.class);
        assertEquals(LinkedList.class, result.getClass());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testMapDefaultingBasic
    public void testMapDefaultingBasic() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(Map.class, TreeMap.class);
        mapper.registerModule(mod);
        Map<?,?> result = mapper.readValue("{}", Map.class);
        assertEquals(TreeMap.class, result.getClass());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testDefaultingRecursive
    public void testDefaultingRecursive() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());

        
        mod.addAbstractTypeMapping(Map.class, TreeMap.class);
        mod.addAbstractTypeMapping(List.class, LinkedList.class);

        mapper.registerModule(mod);
        Object result;

        result = mapper.readValue("[ {} ]", Object.class);
        assertEquals(LinkedList.class, result.getClass());
        Object v = ((List<?>) result).get(0);
        assertNotNull(v);
        assertEquals(TreeMap.class, v.getClass());

        result = mapper.readValue("{ \"x\": [ 3 ] }", Object.class);
        assertEquals(TreeMap.class, result.getClass());
        Map<?,?> map = (Map<?,?>) result;
        assertEquals(1, map.size());
        v = map.get("x");
        assertNotNull(v);
        assertEquals(LinkedList.class, v.getClass());
        assertEquals(1, ((List<?>) v).size());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testInterfaceDefaulting
    public void testInterfaceDefaulting() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(CharSequence.class, MyString.class);
        mapper.registerModule(mod);
        Object result = mapper.readValue(quote("abc"), CharSequence.class);
        assertEquals(MyString.class, result.getClass());
        assertEquals("abc", ((MyString) result).value);

        
        mod = new SimpleModule();
        mod.addAbstractTypeMapping(Abstract.class, AbstractImpl.class);
        mapper = new ObjectMapper()
                .registerModule(mod);
        Abstract a = mapper.readValue("{}", Abstract.class);
        assertNotNull(a);
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

// com.fasterxml.jackson.databind.module.TestDuplicateRegistration::testDuplicateRegistration
    public void testDuplicateRegistration() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.isEnabled(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS));
        MyModule module = new MyModule();
        mapper.registerModule(module);
        mapper.registerModule(module);
        mapper.registerModule(module);
        assertEquals(1, module.regCount);

        
        mapper.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        mapper.registerModule(module);
        assertEquals(2, module.regCount);

        
        ObjectMapper mapper2 = new ObjectMapper();
        mapper2.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        MyModule module2 = new MyModule();
        mapper.registerModule(module2);
        mapper.registerModule(module2);
        mapper.registerModule(module2);
        assertEquals(3, module2.regCount);
    }

// com.fasterxml.jackson.databind.module.TestKeyDeserializers::testKeyDeserializers
    public void testKeyDeserializers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addKeyDeserializer(Foo.class, new FooKeyDeserializer());
        mapper.registerModule(mod);
        Map<Foo,Integer> map = mapper.readValue("{\"a\":3}",
                new TypeReference<Map<Foo,Integer>>() {} );
        assertNotNull(map);
        assertEquals(1, map.size());
        Foo foo = map.keySet().iterator().next();
        assertEquals("a", foo.value);
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

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testMapLikeTypeConstruction
    public void testMapLikeTypeConstruction() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));

        JavaType type = mapper.constructType(MyMapLikeType.class);
        assertTrue(type.isMapLikeType());
        
        JavaType param = ((MapLikeType) type).getKeyType();
        assertNotNull(param);
        assertSame(String.class, param.getRawClass());
        param = ((MapLikeType) type).getContentType();
        assertNotNull(param);
        assertSame(Integer.class, param.getRawClass());
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testCollectionLikeTypeConstruction
    public void testCollectionLikeTypeConstruction() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));

        JavaType type = mapper.constructType(MyCollectionLikeType.class);
        assertTrue(type.isCollectionLikeType());
        JavaType param = ((CollectionLikeType) type).getContentType();
        assertNotNull(param);
        assertSame(Integer.class, param.getRawClass());
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testCollectionLikeSerialization
    public void testCollectionLikeSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));
        mapper.registerModule(new ModifierModule());
        assertEquals("[19]", mapper.writeValueAsString(new MyCollectionLikeType(19)));
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testMapLikeSerialization
    public void testMapLikeSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));
        mapper.registerModule(new ModifierModule());
        
        assertEquals("{\"x\":\"xxx:3\"}", mapper.writeValueAsString(new MyMapLikeType("x", 3)));
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testCollectionLikeDeserialization
    public void testCollectionLikeDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));
        mapper.registerModule(new ModifierModule());
        
        MyMapLikeType result = mapper.readValue("{\"a\":13}", MyMapLikeType.class);
        assertEquals("a", result.getKey());
        assertEquals(Integer.valueOf(13), result.getValue());
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testMapLikeDeserialization
    public void testMapLikeDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));
        mapper.registerModule(new ModifierModule());
        
        MyCollectionLikeType result = mapper.readValue("[-37]", MyCollectionLikeType.class);
        assertEquals(Integer.valueOf(-37), result.getValue());
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

// com.fasterxml.jackson.databind.node.TestArrayNode::testBasics
    public void testBasics() throws IOException
    {
        ArrayNode n = new ArrayNode(JsonNodeFactory.instance);
        assertStandardEquals(n);
        assertFalse(n.elements().hasNext());
        assertFalse(n.fieldNames().hasNext());
        TextNode text = TextNode.valueOf("x");
        n.add(text);
        assertEquals(1, n.size());
        assertFalse(0 == n.hashCode());
        assertTrue(n.elements().hasNext());
        
        assertFalse(n.fieldNames().hasNext());
        assertNull(n.get("x")); 
        assertTrue(n.path("x").isMissingNode());
        assertSame(text, n.get(0));

        
        assertFalse(n.has("field"));
        assertFalse(n.hasNonNull("field"));
        assertTrue(n.has(0));
        assertTrue(n.hasNonNull(0));
        assertFalse(n.has(1));
        assertFalse(n.hasNonNull(1));
        
        
        n.add((JsonNode) null);
        assertEquals(2, n.size());
        assertTrue(n.get(1).isNull());
        assertTrue(n.has(1));
        assertFalse(n.hasNonNull(1));
        
        n.set(1, text);
        assertSame(text, n.get(1));
        n.set(0, null);
        assertTrue(n.get(0).isNull());

        
        ArrayNode n2 = new ArrayNode(JsonNodeFactory.instance);
        n2.add("foobar");
        assertFalse(n.equals(n2));
        n.addAll(n2);
        assertEquals(3, n.size());

        assertFalse(n.get(0).isTextual());
        assertNotNull(n.remove(0));
        assertEquals(2, n.size());
        assertTrue(n.get(0).isTextual());

        ArrayList<JsonNode> nodes = new ArrayList<JsonNode>();
        nodes.add(text);
        n.addAll(nodes);
        assertEquals(3, n.size());
        assertNull(n.get(10000));
        assertNull(n.remove(-4));

        TextNode text2 = TextNode.valueOf("b");
        n.insert(0, text2);
        assertEquals(4, n.size());
        assertSame(text2, n.get(0));

        assertNotNull(n.addArray());
        assertEquals(5, n.size());
        n.addPOJO("foo");
        assertEquals(6, n.size());

        
        JsonGenerator jg = new MappingJsonFactory().createGenerator(new StringWriter());
        n.serialize(jg, null);

        n.removeAll();
        assertEquals(0, n.size());
        jg.close();
    }

// com.fasterxml.jackson.databind.node.TestArrayNode::testAdds
    public void testAdds()
    {
        ArrayNode n = new ArrayNode(JsonNodeFactory.instance);
        assertNotNull(n.addArray());
        assertNotNull(n.addObject());
        n.addPOJO("foobar");
        n.add(1);
        n.add(1L);
        n.add(0.5);
        n.add(0.5f);
        assertEquals(7, n.size());

        assertNotNull(n.insertArray(0));
        assertNotNull(n.insertObject(0));
        n.insertPOJO(2, "xxx");
        assertEquals(10, n.size());
    }

// com.fasterxml.jackson.databind.node.TestArrayNode::testNullChecking
    public void testNullChecking()
    {
        ArrayNode a1 = JsonNodeFactory.instance.arrayNode();
        ArrayNode a2 = JsonNodeFactory.instance.arrayNode();
        
        a1.addAll(a2);
        assertEquals(0, a1.size());
        assertEquals(0, a2.size());

        a2.addAll(a1);
        assertEquals(0, a1.size());
        assertEquals(0, a2.size());
    }

// com.fasterxml.jackson.databind.node.TestArrayNode::testNullChecking2
    public void testNullChecking2()
    {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode src = mapper.createArrayNode();
        ArrayNode dest = mapper.createArrayNode();
        src.add("element");
        dest.addAll(src);
    }

// com.fasterxml.jackson.databind.node.TestArrayNode::testParser
    public void testParser() throws Exception
    {
        ArrayNode n = new ArrayNode(JsonNodeFactory.instance);
        n.add(123);
        TreeTraversingParser p = new TreeTraversingParser(n, null);
        p.setCodec(null);
        assertNull(p.getCodec());
        assertNotNull(p.getParsingContext());
        assertNotNull(p.getTokenLocation());
        assertNotNull(p.getCurrentLocation());
        assertNull(p.getEmbeddedObject());
        assertNull(p.currentNode());

        

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        p.skipChildren();
        assertToken(JsonToken.END_ARRAY, p.getCurrentToken());
        p.close();

        p = new TreeTraversingParser(n, null);
        p.nextToken();
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        p.close();
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

// com.fasterxml.jackson.databind.node.TestConversions::testBufferedLongViaCoercion
    public void testBufferedLongViaCoercion() throws Exception {
        long EXP = 1519348261000L;
        JsonNode tree = MAPPER.readTree("{\"longObj\": "+EXP+".0, \"_class\": \""+LongContainer1940.class.getName()+"\"}");
        LongContainer1940 obj = MAPPER.treeToValue(tree, LongContainer1940.class);
        assertEquals(Long.valueOf(EXP), obj.longObj);
    }

// com.fasterxml.jackson.databind.node.TestDeepCopy::testWithObjectSimple
    public void testWithObjectSimple()
    {
        ObjectNode root = mapper.createObjectNode();
        root.put("a", 3);
        assertEquals(1, root.size());
        
        ObjectNode copy = root.deepCopy();
        assertEquals(1, copy.size());

        
        root.put("b", 7);
        assertEquals(2, root.size());
        assertEquals(1, copy.size());

        
        copy.put("c", 3);
        assertEquals(2, root.size());
        assertEquals(2, copy.size());
    }

// com.fasterxml.jackson.databind.node.TestDeepCopy::testWithArraySimple
    public void testWithArraySimple()
    {
        ArrayNode root = mapper.createArrayNode();
        root.add("a");
        assertEquals(1, root.size());
        
        ArrayNode copy = root.deepCopy();
        assertEquals(1, copy.size());

        
        root.add( 7);
        assertEquals(2, root.size());
        assertEquals(1, copy.size());

        
        copy.add(3);
        assertEquals(2, root.size());
        assertEquals(2, copy.size());
    }

// com.fasterxml.jackson.databind.node.TestDeepCopy::testWithNested
    public void testWithNested()
    {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode leafObject = root.putObject("ob");
        ArrayNode leafArray = root.putArray("arr");
        assertEquals(2, root.size());

        leafObject.put("a", 3);
        assertEquals(1, leafObject.size());
        leafArray.add(true);
        assertEquals(1, leafArray.size());
        
        ObjectNode copy = root.deepCopy();
        assertNotSame(copy, root);
        assertEquals(2, copy.size());

        

        leafObject.put("x", 9);
        assertEquals(2, leafObject.size());
        assertEquals(1, copy.get("ob").size());

        leafArray.add("foobar");
        assertEquals(2, leafArray.size());
        assertEquals(1, copy.get("arr").size());

        
        ((ObjectNode) copy.get("ob")).put("c", 3);
        assertEquals(2, leafObject.size());
        assertEquals(2, copy.get("ob").size());

        ((ArrayNode) copy.get("arr")).add(13);
        assertEquals(2, leafArray.size());
        assertEquals(2, copy.get("arr").size());
    }

// com.fasterxml.jackson.databind.node.TestEndOfInputHandling::testErrorHandling
  public void testErrorHandling() throws IOException {
      ObjectMapper mapper = new ObjectMapper();

      String json = "{\"A\":{\"B\":\n";
      JsonParser parser = mapper.getFactory().createParser(json);
      parser.setCodec(new ObjectMapper());
      try {
          parser.readValueAsTree();
      } catch(JsonParseException e) {
          verifyException(e, "Unexpected end-of-input");
      }
      parser.close();

      try {
          mapper.readTree(json);
      }
      catch(JsonParseException e) {
          verifyException(e, "Unexpected end-of-input");
      }
  }

// com.fasterxml.jackson.databind.node.TestFindMethods::testNonMatching
    public void testNonMatching() throws Exception
    {
        JsonNode root = _buildTree();

        assertNull(root.findValue("boogaboo"));
        assertNull(root.findParent("boogaboo"));
        JsonNode n = root.findPath("boogaboo");
        assertNotNull(n);
        assertTrue(n.isMissingNode());

        assertTrue(root.findValues("boogaboo").isEmpty());
        assertTrue(root.findParents("boogaboo").isEmpty());
    }

// com.fasterxml.jackson.databind.node.TestFindMethods::testMatchingSingle
    public void testMatchingSingle() throws Exception
    {
        JsonNode root = _buildTree();

        JsonNode node = root.findValue("b");
        assertNotNull(node);
        assertEquals(3, node.intValue());
        node = root.findParent("b");
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals(1, ((ObjectNode) node).size());
        assertEquals(3, node.path("b").intValue());
    }

// com.fasterxml.jackson.databind.node.TestFindMethods::testMatchingMultiple
    public void testMatchingMultiple() throws Exception
    {
        JsonNode root = _buildTree();

        List<JsonNode> nodes = root.findValues("value");
        assertEquals(2, nodes.size());
        
        assertEquals(3, nodes.get(0).intValue());
        assertEquals(42, nodes.get(1).intValue());

        nodes = root.findParents("value");
        assertEquals(2, nodes.size());
        
        assertTrue(nodes.get(0).isObject());
        assertTrue(nodes.get(1).isObject());
        assertEquals(3, nodes.get(0).path("value").intValue());
        assertEquals(42, nodes.get(1).path("value").intValue());

        
        List<String> values = root.findValuesAsText("value");
        assertEquals(2, values.size());
        assertEquals("3", values.get(0));
        assertEquals("42", values.get(1));
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testText
    public void testText()
    {
        assertNull(TextNode.valueOf(null));
        TextNode empty = TextNode.valueOf("");
        assertStandardEquals(empty);
        assertSame(TextNode.EMPTY_STRING_NODE, empty);

        
        assertNodeNumbers(TextNode.valueOf("-3"), -3, -3.0);
        assertNodeNumbers(TextNode.valueOf("17.75"), 17, 17.75);
    
        
        long value = 127353264013893L;
        TextNode n = TextNode.valueOf(String.valueOf(value));
        assertEquals(value, n.asLong());
        
        
        n = TextNode.valueOf("foobar");
        assertNodeNumbersForNonNumeric(n);

        assertEquals("foobar", n.asText("barf"));
        assertEquals("", empty.asText("xyz"));

        assertTrue(TextNode.valueOf("true").asBoolean(true));
        assertTrue(TextNode.valueOf("true").asBoolean(false));
        assertFalse(TextNode.valueOf("false").asBoolean(true));
        assertFalse(TextNode.valueOf("false").asBoolean(false));
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testBoolean
    public void testBoolean()
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
                    double d1 = ((NumericNode) o1).asDouble();
                    double d2 = ((NumericNode) o2).asDouble();
                    if (d1 == d2) { 
                        return 0;
                    }
                }
                return 0;
            }
        };
        assertTrue(root1.equals(cmp, root2));
        assertTrue(root2.equals(cmp, root1));
        assertTrue(root1.equals(cmp, root1));
        assertTrue(root2.equals(cmp, root2));
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

// com.fasterxml.jackson.databind.node.TestJsonPointer::testIt
    public void testIt() throws Exception
    {
        final JsonNode SAMPLE_ROOT = objectMapper().readTree(SAMPLE_DOC_JSON_SPEC);
        
        
        assertSame(SAMPLE_ROOT, SAMPLE_ROOT.at(JsonPointer.compile("")));

        
        assertTrue(SAMPLE_ROOT.at(JsonPointer.compile("/Image")).isObject());

        JsonNode n = SAMPLE_ROOT.at(JsonPointer.compile("/Image/Width"));
        assertTrue(n.isNumber());
        assertEquals(SAMPLE_SPEC_VALUE_WIDTH, n.asInt());

        
        assertEquals(SAMPLE_SPEC_VALUE_HEIGHT,
                SAMPLE_ROOT.at("/Image/Height").asInt());

        assertEquals(SAMPLE_SPEC_VALUE_TN_ID3,
                SAMPLE_ROOT.at(JsonPointer.compile("/Image/IDs/2")).asInt());

        
        assertTrue(SAMPLE_ROOT.at("/Image/Depth").isMissingNode());
        assertTrue(SAMPLE_ROOT.at("/Image/1").isMissingNode());
    }

// com.fasterxml.jackson.databind.node.TestJsonPointer::testLongNumbers
    public void testLongNumbers() throws Exception
    {
        
        
        JsonNode root = objectMapper().readTree("{\"123\" : 456}");
        JsonNode jn2 = root.at("/123"); 
        assertEquals(456, jn2.asInt());

        
        root = objectMapper().readTree("{\"35361706045\" : 1234}");
        jn2 = root.at("/35361706045"); 
        assertEquals(1234, jn2.asInt());
    }

// com.fasterxml.jackson.databind.node.TestMissingNode::testMissing
    public void testMissing()
    {
        MissingNode n = MissingNode.getInstance();
        assertTrue(n.isMissingNode());
        assertEquals(JsonToken.NOT_AVAILABLE, n.asToken());
        
        assertEquals("", n.asText());
        assertStandardEquals(n);
        assertEquals("", n.toString());

        
        assertNodeNumbersForNonNumeric(n);

        
        assertTrue(n.asBoolean(true));
        assertEquals(4, n.asInt(4));
        assertEquals(5L, n.asLong(5));
        assertEquals(0.25, n.asDouble(0.25));

        assertEquals("foo", n.asText("foo"));
    }

// com.fasterxml.jackson.databind.node.TestNullNode::testBasicsWithNullNode
    public void testBasicsWithNullNode() throws Exception
    {
        
        NullNode n = NullNode.instance;

        
        assertFalse(n.isContainerNode());
        assertFalse(n.isBigDecimal());
        assertFalse(n.isBigInteger());
        assertFalse(n.isBinary());
        assertFalse(n.isBoolean());
        assertFalse(n.isPojo());
        assertFalse(n.isMissingNode());

        
        assertFalse(n.booleanValue());
        assertNull(n.numberValue());
        assertEquals(0, n.intValue());
        assertEquals(0L, n.longValue());
        assertEquals(BigDecimal.ZERO, n.decimalValue());
        assertEquals(BigInteger.ZERO, n.bigIntegerValue());

        assertEquals(0, n.size());
        assertFalse(n.elements().hasNext());
        assertFalse(n.fieldNames().hasNext());
        
        assertNotNull(n.path("xyz"));
        assertTrue(n.path("xyz").isMissingNode());

        assertFalse(n.has("field"));
        assertFalse(n.has(3));

        assertNodeNumbersForNonNumeric(n);

        
        assertEquals("foo", n.asText("foo"));
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testShort
    public void testShort()
    {
        ShortNode n = ShortNode.valueOf((short) 1);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_INT, n.asToken());
        assertEquals(JsonParser.NumberType.INT, n.numberType());	
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigDecimal.ONE, n.decimalValue());
        assertEquals(BigInteger.ONE, n.bigIntegerValue());
        assertEquals("1", n.asText());

        assertNodeNumbers(n, 1, 1.0);

        assertTrue(ShortNode.valueOf((short) 0).canConvertToInt());
        assertTrue(ShortNode.valueOf(Short.MAX_VALUE).canConvertToInt());
        assertTrue(ShortNode.valueOf(Short.MIN_VALUE).canConvertToInt());

        assertTrue(ShortNode.valueOf((short) 0).canConvertToLong());
        assertTrue(ShortNode.valueOf(Short.MAX_VALUE).canConvertToLong());
        assertTrue(ShortNode.valueOf(Short.MIN_VALUE).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testInt
	public void testInt()
    {
        IntNode n = IntNode.valueOf(1);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_INT, n.asToken());
        assertEquals(JsonParser.NumberType.INT, n.numberType());
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigDecimal.ONE, n.decimalValue());
        assertEquals(BigInteger.ONE, n.bigIntegerValue());
        assertEquals("1", n.asText());
        
        assertEquals("1", n.asText("foo"));
        
        assertNodeNumbers(n, 1, 1.0);

        assertTrue(IntNode.valueOf(0).canConvertToInt());
        assertTrue(IntNode.valueOf(Integer.MAX_VALUE).canConvertToInt());
        assertTrue(IntNode.valueOf(Integer.MIN_VALUE).canConvertToInt());

        assertTrue(IntNode.valueOf(0).canConvertToLong());
        assertTrue(IntNode.valueOf(Integer.MAX_VALUE).canConvertToLong());
        assertTrue(IntNode.valueOf(Integer.MIN_VALUE).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testLong
    public void testLong()
    {
        LongNode n = LongNode.valueOf(1L);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_INT, n.asToken());
        assertEquals(JsonParser.NumberType.LONG, n.numberType());
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigDecimal.ONE, n.decimalValue());
        assertEquals(BigInteger.ONE, n.bigIntegerValue());
        assertEquals("1", n.asText());

        assertNodeNumbers(n, 1, 1.0);

        
        assertTrue(LongNode.valueOf(0).canConvertToInt());
        assertTrue(LongNode.valueOf(Integer.MAX_VALUE).canConvertToInt());
        assertTrue(LongNode.valueOf(Integer.MIN_VALUE).canConvertToInt());
        
        assertFalse(LongNode.valueOf(1L + Integer.MAX_VALUE).canConvertToInt());
        assertFalse(LongNode.valueOf(-1L + Integer.MIN_VALUE).canConvertToInt());

        assertTrue(LongNode.valueOf(0L).canConvertToLong());
        assertTrue(LongNode.valueOf(Long.MAX_VALUE).canConvertToLong());
        assertTrue(LongNode.valueOf(Long.MIN_VALUE).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testDouble
    public void testDouble() throws Exception
    {
        DoubleNode n = DoubleNode.valueOf(0.25);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, n.asToken());
        assertEquals(JsonParser.NumberType.DOUBLE, n.numberType());
        assertEquals(0, n.intValue());
        assertEquals(0.25, n.doubleValue());
        assertNotNull(n.decimalValue());
        assertEquals(BigInteger.ZERO, n.bigIntegerValue());
        assertEquals("0.25", n.asText());

        
        assertNodeNumbers(DoubleNode.valueOf(4.5), 4, 4.5);

        assertTrue(DoubleNode.valueOf(0).canConvertToInt());
        assertTrue(DoubleNode.valueOf(Integer.MAX_VALUE).canConvertToInt());
        assertTrue(DoubleNode.valueOf(Integer.MIN_VALUE).canConvertToInt());
        assertFalse(DoubleNode.valueOf(1L + Integer.MAX_VALUE).canConvertToInt());
        assertFalse(DoubleNode.valueOf(-1L + Integer.MIN_VALUE).canConvertToInt());

        assertTrue(DoubleNode.valueOf(0L).canConvertToLong());
        assertTrue(DoubleNode.valueOf(Long.MAX_VALUE).canConvertToLong());
        assertTrue(DoubleNode.valueOf(Long.MIN_VALUE).canConvertToLong());

        JsonNode num = objectMapper().readTree(" -0.0");
        assertTrue(num.isDouble());
        n = (DoubleNode) num;
        assertEquals(-0.0, n.doubleValue());
        assertEquals("-0.0", String.valueOf(n.doubleValue()));
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testFloat
    public void testFloat()
    {
        FloatNode n = FloatNode.valueOf(0.45f);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, n.asToken());
        assertEquals(JsonParser.NumberType.FLOAT, n.numberType());
        assertEquals(0, n.intValue());
        
        
        assertEquals(0.45f, n.floatValue());
        assertEquals("0.45", n.asText());

        
        
        assertEquals("0.45",  String.valueOf((float) n.doubleValue()));

        assertNotNull(n.decimalValue());
        
        assertEquals(BigInteger.ZERO, n.bigIntegerValue());
        assertEquals("0.45", n.asText());

        
        assertNodeNumbers(FloatNode.valueOf(4.5f), 4, 4.5f);

        assertTrue(FloatNode.valueOf(0).canConvertToInt());
        assertTrue(FloatNode.valueOf(Integer.MAX_VALUE).canConvertToInt());
        assertTrue(FloatNode.valueOf(Integer.MIN_VALUE).canConvertToInt());

        
        assertFalse(FloatNode.valueOf(1000L + Integer.MAX_VALUE).canConvertToInt());
        assertFalse(FloatNode.valueOf(-1000L + Integer.MIN_VALUE).canConvertToInt());

        assertTrue(FloatNode.valueOf(0L).canConvertToLong());
        assertTrue(FloatNode.valueOf(Integer.MAX_VALUE).canConvertToLong());
        assertTrue(FloatNode.valueOf(Integer.MIN_VALUE).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testDecimalNode
    public void testDecimalNode() throws Exception
    {
        DecimalNode n = DecimalNode.valueOf(BigDecimal.ONE);
        assertStandardEquals(n);
        assertTrue(n.equals(new DecimalNode(BigDecimal.ONE)));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, n.asToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, n.numberType());
        assertTrue(n.isNumber());
        assertFalse(n.isIntegralNumber());
        assertTrue(n.isBigDecimal());
        assertEquals(BigDecimal.ONE, n.numberValue());
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigDecimal.ONE, n.decimalValue());
        assertEquals("1", n.asText());

        
        assertNodeNumbers(n, 1, 1.0);

        assertTrue(DecimalNode.valueOf(BigDecimal.ZERO).canConvertToInt());
        assertTrue(DecimalNode.valueOf(BigDecimal.valueOf(Integer.MAX_VALUE)).canConvertToInt());
        assertTrue(DecimalNode.valueOf(BigDecimal.valueOf(Integer.MIN_VALUE)).canConvertToInt());
        assertFalse(DecimalNode.valueOf(BigDecimal.valueOf(1L + Integer.MAX_VALUE)).canConvertToInt());
        assertFalse(DecimalNode.valueOf(BigDecimal.valueOf(-1L + Integer.MIN_VALUE)).canConvertToInt());

        assertTrue(DecimalNode.valueOf(BigDecimal.ZERO).canConvertToLong());
        assertTrue(DecimalNode.valueOf(BigDecimal.valueOf(Long.MAX_VALUE)).canConvertToLong());
        assertTrue(DecimalNode.valueOf(BigDecimal.valueOf(Long.MIN_VALUE)).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testDecimalNodeEqualsHashCode
    public void testDecimalNodeEqualsHashCode()
    {
        
        
        
        BigDecimal b1 = BigDecimal.ONE;
        BigDecimal b2 = new BigDecimal("1.0");
        BigDecimal b3 = new BigDecimal("0.01e2");
        BigDecimal b4 = new BigDecimal("1000e-3");

        DecimalNode node1 = new DecimalNode(b1);
        DecimalNode node2 = new DecimalNode(b2);
        DecimalNode node3 = new DecimalNode(b3);
        DecimalNode node4 = new DecimalNode(b4);

        assertEquals(node1.hashCode(), node2.hashCode());
        assertEquals(node2.hashCode(), node3.hashCode());
        assertEquals(node3.hashCode(), node4.hashCode());

        assertEquals(node1, node2);
        assertEquals(node2, node1);
        assertEquals(node2, node3);
        assertEquals(node3, node4);
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testBigIntegerNode
    public void testBigIntegerNode() throws Exception
    {
        BigIntegerNode n = BigIntegerNode.valueOf(BigInteger.ONE);
        assertStandardEquals(n);
        assertTrue(n.equals(new BigIntegerNode(BigInteger.ONE)));
        assertEquals(JsonToken.VALUE_NUMBER_INT, n.asToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, n.numberType());
        assertTrue(n.isNumber());
        assertTrue(n.isIntegralNumber());
        assertTrue(n.isBigInteger());
        assertEquals(BigInteger.ONE, n.numberValue());
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigInteger.ONE, n.bigIntegerValue());
        assertEquals("1", n.asText());
        assertNodeNumbers(n, 1, 1.0);

        BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
        
        n = BigIntegerNode.valueOf(maxLong);
        assertEquals(Long.MAX_VALUE, n.longValue());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode n2 = mapper.readTree(maxLong.toString());
        assertEquals(Long.MAX_VALUE, n2.longValue());

        
        BigInteger beyondLong = maxLong.shiftLeft(2); 
        n2 = mapper.readTree(beyondLong.toString());
        assertEquals(beyondLong, n2.bigIntegerValue());

        assertTrue(BigIntegerNode.valueOf(BigInteger.ZERO).canConvertToInt());
        assertTrue(BigIntegerNode.valueOf(BigInteger.valueOf(Integer.MAX_VALUE)).canConvertToInt());
        assertTrue(BigIntegerNode.valueOf(BigInteger.valueOf(Integer.MIN_VALUE)).canConvertToInt());
        assertFalse(BigIntegerNode.valueOf(BigInteger.valueOf(1L + Integer.MAX_VALUE)).canConvertToInt());
        assertFalse(BigIntegerNode.valueOf(BigInteger.valueOf(-1L + Integer.MIN_VALUE)).canConvertToInt());

        assertTrue(BigIntegerNode.valueOf(BigInteger.ZERO).canConvertToLong());
        assertTrue(BigIntegerNode.valueOf(BigInteger.valueOf(Long.MAX_VALUE)).canConvertToLong());
        assertTrue(BigIntegerNode.valueOf(BigInteger.valueOf(Long.MIN_VALUE)).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testBigDecimalAsPlain
    public void testBigDecimalAsPlain() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        final String INPUT = "{\"x\":1e2}";
        final JsonNode node = mapper.readTree(INPUT);
        String result = mapper.writeValueAsString(node);
        assertEquals("{\"x\":100}", result);

        
        assertEquals("{\"x\":100}", mapper.writer().writeValueAsString(node));

        
        BigDecimal bigDecimal = new BigDecimal(100);
        JsonNode tree = mapper.valueToTree(bigDecimal);
        assertEquals("100", mapper.writeValueAsString(tree));
    }

// com.fasterxml.jackson.databind.node.TestNumberNodes::testCanonicalNumbers
    public void testCanonicalNumbers() throws Exception
    {
        JsonNodeFactory f = new JsonNodeFactory();
        NumericNode n = f.numberNode(123);
        assertTrue(n.isInt());
        n = f.numberNode(1L + Integer.MAX_VALUE);
        assertFalse(n.isInt());
        assertTrue(n.isLong());

        
        
        n = f.numberNode(123L);
        assertTrue(n.isLong());
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

// com.fasterxml.jackson.databind.node.TestTreeDeserialization::testMixed
    public void testMixed() throws IOException
    {
        ObjectMapper om = new ObjectMapper();
        String JSON = "{\"node\" : { \"a\" : 3 }, \"x\" : 9 }";
        Bean bean = om.readValue(JSON, Bean.class);

        assertEquals(9, bean._x);
        JsonNode n = bean._node;
        assertNotNull(n);
        assertEquals(1, n.size());
        ObjectNode on = (ObjectNode) n;
        assertEquals(3, on.get("a").intValue());
    }

// com.fasterxml.jackson.databind.node.TestTreeDeserialization::testArrayNodeEquality
    public void testArrayNodeEquality()
    {
        ArrayNode n1 = new ArrayNode(null);
        ArrayNode n2 = new ArrayNode(null);

        assertTrue(n1.equals(n2));
        assertTrue(n2.equals(n1));

        n1.add(TextNode.valueOf("Test"));

        assertFalse(n1.equals(n2));
        assertFalse(n2.equals(n1));

        n2.add(TextNode.valueOf("Test"));

        assertTrue(n1.equals(n2));
        assertTrue(n2.equals(n1));
    }

// com.fasterxml.jackson.databind.node.TestTreeDeserialization::testObjectNodeEquality
    public void testObjectNodeEquality()
    {
        ObjectNode n1 = new ObjectNode(null);
        ObjectNode n2 = new ObjectNode(null);

        assertTrue(n1.equals(n2));
        assertTrue(n2.equals(n1));

        n1.set("x", TextNode.valueOf("Test"));

        assertFalse(n1.equals(n2));
        assertFalse(n2.equals(n1));

        n2.set("x", TextNode.valueOf("Test"));

        assertTrue(n1.equals(n2));
        assertTrue(n2.equals(n1));
    }

// com.fasterxml.jackson.databind.node.TestTreeDeserialization::testReadFromString
    public void testReadFromString() throws Exception
    {
        String json = "{\"field\":\"{\\\"name\\\":\\\"John Smith\\\"}\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jNode = mapper.readValue(json, JsonNode.class);

        String generated = mapper.writeValueAsString( jNode);  
        JsonNode out = mapper.readValue( generated, JsonNode.class );   
        assertTrue(out.isObject());
        assertEquals(1, out.size());
        String value = out.path("field").asText();
        assertNotNull(value);
    }

// com.fasterxml.jackson.databind.node.TestTreeDeserialization::testNullHandling
    public void testNullHandling() throws Exception
    {
        
        JsonNode n = objectReader().readTree("null");
        assertNotNull(n);
        assertTrue(n.isNull());

        n = objectMapper().readTree("null");
        assertNotNull(n);
        assertTrue(n.isNull());
        
        
        ObjectNode root = (ObjectNode) objectReader().readTree("{\"x\":null}");
        assertEquals(1, root.size());
        n = root.get("x");
        assertNotNull(n);
        assertTrue(n.isNull());
    }

// com.fasterxml.jackson.databind.node.TestTreeDeserialization::testNullHandlingCovariance
    public void testNullHandlingCovariance() throws Exception
    {
        String JSON = "{\"object\" : null, \"array\" : null }";
        CovarianceBean bean = objectMapper().readValue(JSON, CovarianceBean.class);

        ObjectNode on = bean._object;
        assertNull(on);

        ArrayNode an = bean._array;
        assertNull(an);
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testSimple
	public void testSimple()
        throws Exception
    {
        final String JSON = SAMPLE_DOC_JSON_SPEC;

        for (int type = 0; type < 2; ++type) {
            JsonNode result;

            if (type == 0) {
                result = objectMapper().readTree(new StringReader(JSON));
            } else {
                result = objectMapper().readTree(JSON);
            }

            assertType(result, ObjectNode.class);
            assertEquals(1, result.size());
            assertTrue(result.isObject());
            
            ObjectNode main = (ObjectNode) result;
            assertEquals("Image", main.fieldNames().next());
            JsonNode ob = main.elements().next();
            assertType(ob, ObjectNode.class);
            ObjectNode imageMap = (ObjectNode) ob;
            
            assertEquals(5, imageMap.size());
            ob = imageMap.get("Width");
            assertTrue(ob.isIntegralNumber());
            assertFalse(ob.isFloatingPointNumber());
            assertEquals(SAMPLE_SPEC_VALUE_WIDTH, ob.intValue());
            ob = imageMap.get("Height");
            assertTrue(ob.isIntegralNumber());
            assertEquals(SAMPLE_SPEC_VALUE_HEIGHT, ob.intValue());
            
            ob = imageMap.get("Title");
            assertTrue(ob.isTextual());
            assertEquals(SAMPLE_SPEC_VALUE_TITLE, ob.textValue());
            
            ob = imageMap.get("Thumbnail");
            assertType(ob, ObjectNode.class);
            ObjectNode tn = (ObjectNode) ob;
            ob = tn.get("Url");
            assertTrue(ob.isTextual());
            assertEquals(SAMPLE_SPEC_VALUE_TN_URL, ob.textValue());
            ob = tn.get("Height");
            assertTrue(ob.isIntegralNumber());
            assertEquals(SAMPLE_SPEC_VALUE_TN_HEIGHT, ob.intValue());
            ob = tn.get("Width");
            assertTrue(ob.isTextual());
            assertEquals(SAMPLE_SPEC_VALUE_TN_WIDTH, ob.textValue());
            
            ob = imageMap.get("IDs");
            assertTrue(ob.isArray());
            ArrayNode idList = (ArrayNode) ob;
            assertEquals(4, idList.size());
            assertEquals(4, calcLength(idList.elements()));
            assertEquals(4, calcLength(idList.iterator()));
            {
                int[] values = new int[] {
                    SAMPLE_SPEC_VALUE_TN_ID1,
                    SAMPLE_SPEC_VALUE_TN_ID2,
                    SAMPLE_SPEC_VALUE_TN_ID3,
                    SAMPLE_SPEC_VALUE_TN_ID4
                };
                for (int i = 0; i < values.length; ++i) {
                    assertEquals(values[i], idList.get(i).intValue());
                }
                int i = 0;
                for (JsonNode n : idList) {
                    assertEquals(values[i], n.intValue());
                    ++i;
                }
            }
        }
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testBoolean
    public void testBoolean()
        throws Exception
    {
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

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testDouble
    public void testDouble()
        throws Exception
    {
        double value = 3.04;
        JsonNode result = objectMapper().readTree(String.valueOf(value));
        assertTrue(result.isNumber());
        assertFalse(result.isNull());
        assertType(result, DoubleNode.class);
        assertTrue(result.isFloatingPointNumber());
        assertTrue(result.isDouble());
        assertFalse(result.isInt());
        assertFalse(result.isLong());
        assertFalse(result.isIntegralNumber());
        assertFalse(result.isTextual());
        assertFalse(result.isMissingNode());

        assertEquals(value, result.doubleValue());
        assertEquals(value, result.numberValue().doubleValue());
        assertEquals((int) value, result.intValue());
        assertEquals((long) value, result.longValue());
        assertEquals(String.valueOf(value), result.asText());

        
        assertEquals(result, DoubleNode.valueOf(value));
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testInt
    public void testInt()
        throws Exception
    {
        int value = -90184;
        JsonNode result = objectMapper().readTree(String.valueOf(value));
        assertTrue(result.isNumber());
        assertTrue(result.isIntegralNumber());
        assertTrue(result.isInt());
        assertType(result, IntNode.class);
        assertFalse(result.isLong());
        assertFalse(result.isFloatingPointNumber());
        assertFalse(result.isDouble());
        assertFalse(result.isNull());
        assertFalse(result.isTextual());
        assertFalse(result.isMissingNode());

        assertEquals(value, result.numberValue().intValue());
        assertEquals(value, result.intValue());
        assertEquals(String.valueOf(value), result.asText());
        assertEquals((double) value, result.doubleValue());
        assertEquals((long) value, result.longValue());

        
        assertEquals(result, IntNode.valueOf(value));
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testLong
    public void testLong() throws Exception
    {
        
        long value = 12345678L << 32;
        JsonNode result = objectMapper().readTree(String.valueOf(value));
        assertTrue(result.isNumber());
        assertTrue(result.isIntegralNumber());
        assertTrue(result.isLong());
        assertType(result, LongNode.class);
        assertFalse(result.isInt());
        assertFalse(result.isFloatingPointNumber());
        assertFalse(result.isDouble());
        assertFalse(result.isNull());
        assertFalse(result.isTextual());
        assertFalse(result.isMissingNode());

        assertEquals(value, result.numberValue().longValue());
        assertEquals(value, result.longValue());
        assertEquals(String.valueOf(value), result.asText());
        assertEquals((double) value, result.doubleValue());

        
        assertEquals(result, LongNode.valueOf(value));
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testNull
    public void testNull() throws Exception
    {
        JsonNode result = objectMapper().readTree("   null ");
        
        assertNotNull(result);
        assertTrue(result.isNull());
        assertFalse(result.isNumber());
        assertFalse(result.isTextual());
        assertEquals("null", result.asText());

        
        assertEquals(result, NullNode.instance);
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testDecimalNode
    public void testDecimalNode()
        throws Exception
    {
        
        BigDecimal value = new BigDecimal("0.1");
        JsonNode result = DecimalNode.valueOf(value);

        assertFalse(result.isArray());
        assertFalse(result.isObject());
        assertTrue(result.isNumber());
        assertFalse(result.isIntegralNumber());
        assertFalse(result.isLong());
        assertType(result, DecimalNode.class);
        assertFalse(result.isInt());
        assertTrue(result.isFloatingPointNumber());
        assertTrue(result.isBigDecimal());
        assertFalse(result.isDouble());
        assertFalse(result.isNull());
        assertFalse(result.isTextual());
        assertFalse(result.isMissingNode());

        assertEquals(value, result.numberValue());
        assertEquals(value.toString(), result.asText());

        
        assertEquals(result, DecimalNode.valueOf(value));
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testSimpleArray
    public void testSimpleArray() throws Exception
    {
        ArrayNode result = objectMapper().createArrayNode();

        assertTrue(result.isArray());
        assertType(result, ArrayNode.class);

        assertFalse(result.isObject());
        assertFalse(result.isNumber());
        assertFalse(result.isNull());
        assertFalse(result.isTextual());

        
        result.add(false);
        result.insertNull(0);

        
        assertEquals(result, result);
        assertFalse(result.equals(null)); 

        
        assertEquals(NullNode.instance, result.path(0));
        assertEquals(NullNode.instance, result.get(0));
        assertEquals(BooleanNode.FALSE, result.path(1));
        assertEquals(BooleanNode.FALSE, result.get(1));
        assertEquals(2, result.size());

        assertNull(result.get(-1));
        assertNull(result.get(2));
        JsonNode missing = result.path(2);
        assertTrue(missing.isMissingNode());
        assertTrue(result.path(-100).isMissingNode());

        
        ArrayNode array2 = objectMapper().createArrayNode();
        array2.addNull();
        array2.add(false);
        assertEquals(result, array2);

        
        JsonNode rm1 = array2.remove(0);
        assertEquals(NullNode.instance, rm1);
        assertEquals(1, array2.size());
        assertEquals(BooleanNode.FALSE, array2.get(0));
        assertFalse(result.equals(array2));

        JsonNode rm2 = array2.remove(0);
        assertEquals(BooleanNode.FALSE, rm2);
        assertEquals(0, array2.size());
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testEOF
    public void testEOF() throws Exception
    {
        String JSON =
            "{ \"key\": [ { \"a\" : { \"name\": \"foo\",  \"type\": 1\n"
            +"},  \"type\": 3, \"url\": \"http://www.google.com\" } ],\n"
            +"\"name\": \"xyz\", \"type\": 1, \"url\" : null }\n  "
            ;
        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser(new StringReader(JSON));
        JsonNode result = objectMapper().readTree(jp);

        assertTrue(result.isObject());
        assertEquals(4, result.size());

        assertNull(objectMapper().readTree(jp));
        jp.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testMultiple
    public void testMultiple() throws Exception
    {
        String JSON = "12  \"string\" [ 1, 2, 3 ]";
        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser(new StringReader(JSON));
        final ObjectMapper mapper = objectMapper();
        JsonNode result = mapper.readTree(jp);

        assertTrue(result.isIntegralNumber());
        assertTrue(result.isInt());
        assertFalse(result.isTextual());
        assertEquals(12, result.intValue());

        result = mapper.readTree(jp);
        assertTrue(result.isTextual());
        assertFalse(result.isIntegralNumber());
        assertFalse(result.isInt());
        assertEquals("string", result.textValue());

        result = mapper.readTree(jp);
        assertTrue(result.isArray());
        assertEquals(3, result.size());

        assertNull(mapper.readTree(jp));
        jp.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testMissingNode
    public void testMissingNode()
        throws Exception
    {
        String JSON = "[ { }, [ ] ]";
        JsonNode result = objectMapper().readTree(new StringReader(JSON));

        assertTrue(result.isContainerNode());
        assertTrue(result.isArray());
        assertEquals(2, result.size());

        int count = 0;
        for (JsonNode node : result) {
            ++count;
        }
        assertEquals(2, count);

        Iterator<JsonNode> it = result.iterator();

        JsonNode onode = it.next();
        assertTrue(onode.isContainerNode());
        assertTrue(onode.isObject());
        assertEquals(0, onode.size());
        assertFalse(onode.isMissingNode()); 
        assertNull(onode.textValue());

        
        assertNull(onode.get(0));
        JsonNode dummyNode = onode.path(0);
        assertNotNull(dummyNode);
        assertTrue(dummyNode.isMissingNode());
        assertNull(dummyNode.get(3));
        assertNull(dummyNode.get("whatever"));
        JsonNode dummyNode2 = dummyNode.path(98);
        assertNotNull(dummyNode2);
        assertTrue(dummyNode2.isMissingNode());
        JsonNode dummyNode3 = dummyNode.path("field");
        assertNotNull(dummyNode3);
        assertTrue(dummyNode3.isMissingNode());

        

        JsonNode anode = it.next();
        assertTrue(anode.isContainerNode());
        assertTrue(anode.isArray());
        assertFalse(anode.isMissingNode()); 
        assertEquals(0, anode.size());

        assertNull(anode.get(0));
        dummyNode = anode.path(0);
        assertNotNull(dummyNode);
        assertTrue(dummyNode.isMissingNode());
        assertNull(dummyNode.get(0));
        assertNull(dummyNode.get("myfield"));
        dummyNode2 = dummyNode.path(98);
        assertNotNull(dummyNode2);
        assertTrue(dummyNode2.isMissingNode());
        dummyNode3 = dummyNode.path("f");
        assertNotNull(dummyNode3);
        assertTrue(dummyNode3.isMissingNode());
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperDeserializer::testArray
    public void testArray() throws Exception
    {
        final String JSON = "[[[-0.027512,51.503221],[-0.008497,51.503221],[-0.008497,51.509744],[-0.027512,51.509744]]]";

        JsonNode n = objectMapper().readTree(JSON);
        assertNotNull(n);
        assertTrue(n.isArray());
        ArrayNode an = (ArrayNode) n;
        assertEquals(1, an.size());
        ArrayNode an2 = (ArrayNode) n.get(0);
        assertTrue(an2.isArray());
        assertEquals(4, an2.size());
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperSerializer::testFromArray
    public void testFromArray()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = mapper.createArrayNode();
        root.add(TEXT1);
        root.add(3);
        ObjectNode obj = root.addObject();
        obj.put(FIELD1, true);
        obj.putArray(FIELD2);
        root.add(false);

        
        for (int i = 0; i < 2; ++i) {
            StringWriter sw = new StringWriter();
            if (i == 0) {
                JsonGenerator gen = new JsonFactory().createGenerator(sw);
                root.serialize(gen, null);
                gen.close();
            } else {
                mapper.writeValue(sw, root);
            }
            verifyFromArray(sw.toString());
        }
            
        
        verifyFromArray(root.toString());
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperSerializer::testFromMap
    public void testFromMap()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put(FIELD4, TEXT2);
        root.put(FIELD3, -1);
        root.putArray(FIELD2);
        root.put(FIELD1, DOUBLE_VALUE);

        
        for (int i = 0; i < 2; ++i) {
            StringWriter sw = new StringWriter();
            if (i == 0) {
                JsonGenerator gen = new JsonFactory().createGenerator(sw);
                root.serialize(gen, null);
                gen.close();
            } else {
                mapper.writeValue(sw, root);
            }
            verifyFromMap(sw.toString());
        }

        
        verifyFromMap(root.toString());
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperSerializer::testSmallNumbers
    public void testSmallNumbers()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = mapper.createArrayNode();
        for (int i = -20; i <= 20; ++i) {
            JsonNode n = root.numberNode(i);
            root.add(n);
            
            assertEquals(String.valueOf(i), n.toString());
        }

        
        for (int type = 0; type < 2; ++type) {
            StringWriter sw = new StringWriter();
            if (type == 0) {
                JsonGenerator gen = new JsonFactory().createGenerator(sw);
                root.serialize(gen, null);
                gen.close();
            } else {
                mapper.writeValue(sw, root);
            }
            
            String doc = sw.toString();
            JsonParser jp = new JsonFactory().createParser(new StringReader(doc));
            
            assertEquals(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = -20; i <= 20; ++i) {
                assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                assertEquals(i, jp.getIntValue());
                assertEquals(""+i, jp.getText());
            }
            assertEquals(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperSerializer::testNull
    public void testNull() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, NullNode.instance);
        assertEquals("null", sw.toString());
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperSerializer::testBinary
    public void testBinary()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        final int LENGTH = 13045;
        byte[] data = new byte[LENGTH];
        for (int i = 0; i < LENGTH; ++i) {
            data[i] = (byte) i;
        }
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, BinaryNode.valueOf(data));

        JsonParser jp = new JsonFactory().createParser(sw.toString());
        
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertArrayEquals(data, jp.getBinaryValue());
        jp.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testSimple
    public void testSimple() throws Exception
    {
        
        final String JSON =
            "{ \"a\" : 123, \"list\" : [ 12.25, null, true, { }, [ ] ] }";
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree(JSON);
        JsonParser jp = tree.traverse();

        assertNull(jp.getCurrentToken());
        assertNull(jp.getCurrentName());

        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertNull(jp.getCurrentName());
        assertEquals("Expected START_OBJECT", JsonToken.START_OBJECT.asString(), jp.getText());

        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("a", jp.getCurrentName());
        assertEquals("a", jp.getText());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals("a", jp.getCurrentName());
        assertEquals(123, jp.getIntValue());
        assertEquals("123", jp.getText());

        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("list", jp.getCurrentName());
        assertEquals("list", jp.getText());

        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertEquals("list", jp.getCurrentName());
        assertEquals(JsonToken.START_ARRAY.asString(), jp.getText());

        assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertNull(jp.getCurrentName());
        assertEquals(12.25, jp.getDoubleValue(), 0);
        assertEquals("12.25", jp.getText());

        assertToken(JsonToken.VALUE_NULL, jp.nextToken());
        assertNull(jp.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL.asString(), jp.getText());

        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertNull(jp.getCurrentName());
        assertTrue(jp.getBooleanValue());
        assertEquals(JsonToken.VALUE_TRUE.asString(), jp.getText());

        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertNull(jp.getCurrentName());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.getCurrentName());

        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertNull(jp.getCurrentName());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.getCurrentName());

        assertToken(JsonToken.END_ARRAY, jp.nextToken());

        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.getCurrentName());

        assertNull(jp.nextToken());

        jp.close();
        assertTrue(jp.isClosed());
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testArray
    public void testArray() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();

        JsonParser jp = m.readTree("[]").traverse();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();

        jp = m.readTree("[[]]").traverse();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();

        jp = m.readTree("[[ 12.1 ]]").traverse();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testNested
    public void testNested() throws Exception
    {
        
        final String JSON =
            "{\"coordinates\":[[[-3,\n1],[179.859681,51.175092]]]}"
            ;
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree(JSON);
        JsonParser jp = tree.traverse();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());

        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());

        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());

        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testSpecDoc
    public void testSpecDoc() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree(SAMPLE_DOC_JSON_SPEC);
        JsonParser jp = tree.traverse();
        verifyJsonSpecSampleDoc(jp, true);
        jp.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testBinaryPojo
    public void testBinaryPojo() throws Exception
    {
        byte[] inputBinary = new byte[] { 1, 2, 100 };
        POJONode n = new POJONode(inputBinary);
        JsonParser jp = n.traverse();

        assertNull(jp.getCurrentToken());
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, jp.nextToken());
        byte[] data = jp.getBinaryValue();
        assertNotNull(data);
        assertArrayEquals(inputBinary, data);
        Object pojo = jp.getEmbeddedObject();
        assertSame(data, pojo);
        jp.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testBinaryNode
    public void testBinaryNode() throws Exception
    {
        byte[] inputBinary = new byte[] { 0, -5 };
        BinaryNode n = new BinaryNode(inputBinary);
        JsonParser jp = n.traverse();

        assertNull(jp.getCurrentToken());
        
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, jp.nextToken());
        byte[] data = jp.getBinaryValue();
        assertNotNull(data);
        assertArrayEquals(inputBinary, data);

        
        assertEquals("APs=", jp.getText());

        assertNull(jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testTextAsBinary
    public void testTextAsBinary() throws Exception
    {
        TextNode n = new TextNode("   APs=\n");
        JsonParser jp = n.traverse();
        assertNull(jp.getCurrentToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        byte[] data = jp.getBinaryValue();
        assertNotNull(data);
        assertArrayEquals(new byte[] { 0, -5 }, data);

        assertNull(jp.nextToken());
        jp.close();
        assertTrue(jp.isClosed());

        
        n = new TextNode("?!??");
        jp = n.traverse();
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        try {
            jp.getBinaryValue();
        } catch (JsonParseException e) {
            verifyException(e, "Illegal character");
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testDataBind
    public void testDataBind() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree
            ("{ \"name\" : \"Tatu\", \n"
             +"\"magicNumber\" : 42,"
             +"\"kids\" : [ \"Leo\", \"Lila\", \"Leia\" ] \n"
             +"}");
        Person tatu = m.treeToValue(tree, Person.class);
        assertNotNull(tatu);
        assertEquals(42, tatu.magicNumber);
        assertEquals("Tatu", tatu.name);
        assertNotNull(tatu.kids);
        assertEquals(3, tatu.kids.size());
        assertEquals("Leo", tatu.kids.get(0));
        assertEquals("Lila", tatu.kids.get(1));
        assertEquals("Leia", tatu.kids.get(2));
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testSkipChildrenWrt370
    public void testSkipChildrenWrt370() throws Exception
    {
        ObjectMapper o = new ObjectMapper();
        ObjectNode n = o.createObjectNode();
        n.putObject("inner").put("value", "test");
        n.putObject("unknown").putNull("inner");
        Jackson370Bean obj = o.readValue(n.traverse(), Jackson370Bean.class);
        assertNotNull(obj.inner);
        assertEquals("test", obj.inner.value);        
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
        Foo mo = new Foo();
        mo.bar1 = new Bar();
        mo.bar2 = mo.bar1;

        String json = MAPPER.writeValueAsString(mo);

        Foo result = MAPPER.readValue(json, Foo.class);
        assertNotNull(result);
    }
