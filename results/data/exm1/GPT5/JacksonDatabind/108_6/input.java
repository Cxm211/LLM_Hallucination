// buggy code
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        return (T) _bindAsTree(p);
    }

    protected final JsonNode _bindAsTree(JsonParser p) throws IOException
    {
        // Need to inline `_initForReading()` due to tree reading handling end-of-input specially
        _config.initialize(p);
        if (_schema != null) {
            p.setSchema(_schema);
        }

        JsonToken t = p.getCurrentToken();
        if (t == null) {
            t = p.nextToken();
            if (t == null) {
                return _config.getNodeFactory().missingNode();
            }
        }
        final JsonNode resultNode;
        if (t == JsonToken.VALUE_NULL) {
            resultNode = _config.getNodeFactory().nullNode();
        } else {
            final DeserializationContext ctxt = createDeserializationContext(p);
            final JsonDeserializer<Object> deser = _findTreeDeserializer(ctxt);
            if (_unwrapRoot) {
                resultNode = (JsonNode) _unwrapAndDeserialize(p, ctxt, JSON_NODE_TYPE, deser);
            } else {
                resultNode = (JsonNode) deser.deserialize(p, ctxt);
                if (_config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
                    _verifyNoTrailingTokens(p, ctxt, JSON_NODE_TYPE);
                }
            }
        }
        return resultNode;
    }

// relevant test
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

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateNumeric
    public void testDateNumeric() throws IOException
    {
        
        assertTrue(MAPPER.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        
        String json = MAPPER.writeValueAsString(new Date(199L));
        assertEquals("199", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601
    public void testDateISO8601() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        serialize(mapper, judate(1970, 1, 1,  02, 00, 00, 0, "GMT+2"), "1970-01-01T00:00:00.000+0000");
        serialize(mapper, judate(1970, 1, 1,  00, 00, 00, 0, "UTC"),   "1970-01-01T00:00:00.000+0000");

        
        serialize(mapper, judate(911, 1, 1,  00, 00, 00, 0, "UTC"),   "0911-01-01T00:00:00.000+0000");
        serialize(mapper, judate(87, 1, 1,  00, 00, 00, 0, "UTC"),   "0087-01-01T00:00:00.000+0000");
        serialize(mapper, judate(1, 1, 1,  00, 00, 00, 0, "UTC"),   "0001-01-01T00:00:00.000+0000");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601_10k
    public void testDateISO8601_10k() throws IOException
    {
        ObjectWriter w = MAPPER.writer()
                .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        serialize(w, judate(10204, 1, 1,  00, 00, 00, 0, "UTC"),   "+10204-01-01T00:00:00.000+0000");
        
        serialize(w, judate(123456, 1, 1,  00, 00, 00, 0, "UTC"),   "+123456-01-01T00:00:00.000+0000");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601_BCE
    public void testDateISO8601_BCE() throws IOException
    {
        ObjectWriter w = MAPPER.writer()
                .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        
        

        serialize(w, judate(0, 1, 1,  00, 00, 00, 0, "UTC"),   "+0000-01-01T00:00:00.000+0000");
        serialize(w, judate(-1, 1, 1,  00, 00, 00, 0, "UTC"),   "-0001-01-01T00:00:00.000+0000");
        serialize(w, judate(-49, 1, 1,  00, 00, 00, 0, "UTC"),   "-0049-01-01T00:00:00.000+0000"); 
        serialize(w, judate(-264, 1, 1,  00, 00, 00, 0, "UTC"),   "-0264-01-01T00:00:00.000+0000"); 
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601_customTZ
    public void testDateISO8601_customTZ() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+2"));

        serialize( mapper, judate(1970, 1, 1,  00, 00, 00, 0, "GMT+2"), "1970-01-01T00:00:00.000+0200");
        serialize( mapper, judate(1970, 1, 1,  00, 00, 00, 0, "UTC"),   "1970-01-01T02:00:00.000+0200");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601_colonInTZ
    public void testDateISO8601_colonInTZ() throws IOException
    {
        StdDateFormat dateFormat = new StdDateFormat();
        assertFalse(dateFormat.isColonIncludedInTimeZone());
        dateFormat = dateFormat.withColonInTimeZone(true);
        assertTrue(dateFormat.isColonIncludedInTimeZone());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(dateFormat);
        
        serialize( mapper, judate(1970, 1, 1,  02, 00, 00, 0, "GMT+2"), "1970-01-01T00:00:00.000+00:00");
        serialize( mapper, judate(1970, 1, 1,  00, 00, 00, 0, "UTC"),   "1970-01-01T00:00:00.000+00:00");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateOther
    public void testDateOther() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        mapper.setDateFormat(df);
        mapper.setTimeZone(TimeZone.getTimeZone("PST"));

        
        serialize( mapper, judate(1970, 1, 1,  00, 00, 00, 0, "UTC"), "1969-12-31X16:00:00");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testTimeZone
    public void testTimeZone() throws IOException
    {
        TimeZone input = TimeZone.getTimeZone("PST");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(quote("PST"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testTimeZoneInBean
    public void testTimeZoneInBean() throws IOException
    {
        String json = MAPPER.writeValueAsString(new TimeZoneBean("PST"));
        assertEquals("{\"tz\":\"PST\"}", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateUsingObjectWriter
    public void testDateUsingObjectWriter() throws IOException
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        TimeZone tz = TimeZone.getTimeZone("PST");
        assertEquals(quote("1969-12-31X16:00:00"),
                MAPPER.writer(df)
                    .with(tz)
                    .writeValueAsString(new Date(0L)));
        ObjectWriter w = MAPPER.writer((DateFormat)null);
        assertEquals("0", w.writeValueAsString(new Date(0L)));

        w = w.with(df).with(tz);
        assertEquals(quote("1969-12-31X16:00:00"), w.writeValueAsString(new Date(0L)));
        w = w.with((DateFormat) null);
        assertEquals("0", w.writeValueAsString(new Date(0L)));
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDatesAsMapKeys
    public void testDatesAsMapKeys() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<Date,Integer> map = new HashMap<Date,Integer>();
        assertFalse(mapper.isEnabled(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS));
        map.put(new Date(0L), Integer.valueOf(1));
        
        assertEquals("{\"1970-01-01T00:00:00.000+0000\":1}", mapper.writeValueAsString(map));
        
        
        mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, true);
        assertEquals("{\"0\":1}", mapper.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateWithJsonFormat
    public void testDateWithJsonFormat() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json;

        
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsNumberBean(0L));
        assertEquals(aposToQuotes("{'date':0}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writer().with(getUTCTimeZone()).writeValueAsString(new DateAsStringBean(0L));
        assertEquals("{\"date\":\"1970-01-01\"}", json);

        
        json = mapper.writeValueAsString(new DateInCETBean(0L));
        assertEquals("{\"date\":\"1970-01-01,01:00\"}", json);
        
        
        json = mapper.writer().with(getUTCTimeZone()).writeValueAsString(new CalendarAsStringBean(0L));
        assertEquals("{\"value\":\"1970-01-01\"}", json);

        
        json = mapper.writeValueAsString(new DateAsDefaultStringBean(0L));
        assertEquals("{\"date\":\"1970-01-01T00:00:00.000+0000\"}", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testWithTimeZoneOverride
    public void testWithTimeZoneOverride() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd/HH:mm z"));
        mapper.setTimeZone(TimeZone.getTimeZone("PST"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        
        serialize( mapper, judate(1969, 12, 31, 16, 00, 00, 00, "PST"), "1969-12-31/16:00 PST");

        
        mapper.setLocale(Locale.FRANCE);
        serialize( mapper, judate(1969, 12, 31, 16, 00, 00, 00, "PST"), "1969-12-31/16:00 PST");

        
        ObjectWriter w = mapper.writer();
        w = w.with(TimeZone.getTimeZone("EST"));
        String json = w.writeValueAsString(new Date(0));
        assertEquals(quote("1969-12-31/19:00 EST"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateDefaultShape
    public void testDateDefaultShape() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = mapper.writeValueAsString(new DateAsDefaultBean(0L));
        assertEquals(aposToQuotes("{'date':0}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBean(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T00:00:00.000+0000'}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithEmptyJsonFormat(0L));
        assertEquals(aposToQuotes("{'date':0}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithEmptyJsonFormat(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T00:00:00.000+0000'}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithPattern(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01'}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithPattern(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01'}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithLocale(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T00:00:00.000+0000'}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithLocale(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T00:00:00.000+0000'}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T01:00:00.000+0100'}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T01:00:00.000+0100'}"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testFormatWithoutPattern
    public void testFormatWithoutPattern() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss"));
        String json = mapper.writeValueAsString(new DateAsDefaultBeanWithTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01X01:00:00'}"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testBigDecimal
    public void testBigDecimal() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.14159265";
        map.put("pi", new BigDecimal(PI_STR));
        String str = MAPPER.writeValueAsString(map);
        assertEquals("{\"pi\":3.14159265}", str);
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testBigDecimalAsPlainString
    public void testBigDecimalAsPlainString() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.00000000";
        map.put("pi", new BigDecimal(PI_STR));
        String str = mapper.writeValueAsString(map);
        assertEquals("{\"pi\":3.00000000}", str);
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testFile
    public void testFile() throws IOException
    {
        
        File f = new File(new File("/tmp"), "foo.text");
        String str = MAPPER.writeValueAsString(f);
        
        String escapedAbsPath = f.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"); 
        assertEquals(quote(escapedAbsPath), str);
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testRegexps
    public void testRegexps() throws IOException
    {
        final String PATTERN_STR = "\\s+([a-b]+)\\w?";
        Pattern p = Pattern.compile(PATTERN_STR);
        Map<String,Object> input = new HashMap<String,Object>();
        input.put("p", p);
        Map<String,Object> result = writeAndMap(MAPPER, input);
        assertEquals(p.pattern(), result.get("p"));
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testCurrency
    public void testCurrency() throws IOException
    {
        Currency usd = Currency.getInstance("USD");
        assertEquals(quote("USD"), MAPPER.writeValueAsString(usd));
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testLocale
    public void testLocale() throws IOException
    {
        assertEquals(quote("en"), MAPPER.writeValueAsString(new Locale("en")));
        assertEquals(quote("es_ES"), MAPPER.writeValueAsString(new Locale("es", "ES")));
        assertEquals(quote("fi_FI_savo"), MAPPER.writeValueAsString(new Locale("FI", "fi", "savo")));

        assertEquals(quote("en_US"), MAPPER.writeValueAsString(Locale.US));

        
        assertEquals(quote(""), MAPPER.writeValueAsString(Locale.ROOT));
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testInetAddress
    public void testInetAddress() throws IOException
    {
        assertEquals(quote("127.0.0.1"), MAPPER.writeValueAsString(InetAddress.getByName("127.0.0.1")));
        InetAddress input = InetAddress.getByName("google.com");
        assertEquals(quote("google.com"), MAPPER.writeValueAsString(input));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(InetAddress.class)
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER));
        String json = mapper.writeValueAsString(input);
        assertEquals(quote(input.getHostAddress()), json);

        assertEquals(String.format("{\"value\":\"%s\"}", input.getHostAddress()),
                mapper.writeValueAsString(new InetAddressBean(input)));
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testInetSocketAddress
    public void testInetSocketAddress() throws IOException
    {
        assertEquals(quote("127.0.0.1:8080"),
                MAPPER.writeValueAsString(new InetSocketAddress("127.0.0.1", 8080)));
        assertEquals(quote("google.com:6667"),
                MAPPER.writeValueAsString(new InetSocketAddress("google.com", 6667)));
        assertEquals(quote("[2001:db8:85a3:8d3:1319:8a2e:370:7348]:443"),
                MAPPER.writeValueAsString(new InetSocketAddress("2001:db8:85a3:8d3:1319:8a2e:370:7348", 443)));
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testClass
    public void testClass() throws IOException
    {
        assertEquals(quote("java.lang.String"), MAPPER.writeValueAsString(String.class));
        assertEquals(quote("int"), MAPPER.writeValueAsString(Integer.TYPE));
        assertEquals(quote("boolean"), MAPPER.writeValueAsString(Boolean.TYPE));
        assertEquals(quote("void"), MAPPER.writeValueAsString(Void.TYPE));
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testCharset
    public void testCharset() throws IOException
    {
        assertEquals(quote("UTF-8"), MAPPER.writeValueAsString(Charset.forName("UTF-8")));
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testByteBuffer
    public void testByteBuffer() throws IOException
    {
        final byte[] INPUT_BYTES = new byte[] { 1, 2, 3, 4, 5 };
        String exp = MAPPER.writeValueAsString(INPUT_BYTES);
        ByteBuffer bbuf = ByteBuffer.wrap(INPUT_BYTES);
        assertEquals(exp, MAPPER.writeValueAsString(bbuf));

        
        ByteBuffer bbuf2 = ByteBuffer.allocateDirect(5);
        bbuf2.put(INPUT_BYTES);
        assertEquals(exp, MAPPER.writeValueAsString(bbuf2));
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testSlicedByteBuffer
    public void testSlicedByteBuffer() throws IOException
    {
        final byte[] INPUT_BYTES = new byte[] { 1, 2, 3, 4, 5 };
        String exp = MAPPER.writeValueAsString(new byte[] { 3, 4, 5 });
        ByteBuffer bbuf = ByteBuffer.wrap(INPUT_BYTES);

        bbuf.position(2);
        ByteBuffer slicedBuf = bbuf.slice();

        assertEquals(exp, MAPPER.writeValueAsString(slicedBuf));
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testUUIDs
    public void testUUIDs() throws IOException
    {
        
        for (String value : new String[] {
                "76e6d183-5f68-4afa-b94a-922c1fdb83f8",
                "540a88d1-e2d8-4fb1-9396-9212280d0a7f",
                "2c9e441d-1cd0-472d-9bab-69838f877574",
                "591b2869-146e-41d7-8048-e8131f1fdec5",
                "82994ac2-7b23-49f2-8cc5-e24cf6ed77be",
                "00000007-0000-0000-0000-000000000000"
        }) {
            UUID uuid = UUID.fromString(value);
            String json = MAPPER.writeValueAsString(uuid);
            assertEquals(quote(uuid.toString()), json);

            
            String str = MAPPER.convertValue(uuid, String.class);
            assertEquals(value, str);
        }
        
        
        
        final String TEMPL = "00000000-0000-0000-0000-000000000000";
        final String chars = "123456789abcdef";

        for (int i = 0; i < chars.length(); ++i) {
            String value = TEMPL.replace('0', chars.charAt(i));
            UUID uuid = UUID.fromString(value);
            String json = MAPPER.writeValueAsString(uuid);
            assertEquals(quote(uuid.toString()), json);
        }
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testVoidSerialization
    public void testVoidSerialization() throws Exception
    {
        assertEquals(aposToQuotes("{'value':null}"),
                MAPPER.writeValueAsString(new VoidBean()));
    }

// com.fasterxml.jackson.databind.ser.jdk.KeySerializers1679Test::testRecursion1679
    public void testRecursion1679() throws Exception
    {
        Map<Object, Object> objectMap = new HashMap<Object, Object>();
        objectMap.put(new Object(), "foo");
        String json = MAPPER.writeValueAsString(objectMap);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.ser.jdk.MapKeySerializationTest::testMapJsonValueKey47
    public void testMapJsonValueKey47() throws Exception
    {
        WatMap input = new WatMap();
        input.put(new Wat("3"), true);

        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'3':true}"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.MapKeySerializationTest::testClassKey
    public void testClassKey() throws IOException
    {
        Map<Class<?>,Integer> map = new LinkedHashMap<Class<?>,Integer>();
        map.put(String.class, 2);
        String json = MAPPER.writeValueAsString(map);
        assertEquals(aposToQuotes("{'java.lang.String':2}"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.MapKeySerializationTest::testDefaultKeySerializer
    public void testDefaultKeySerializer() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.getSerializerProvider().setDefaultKeySerializer(new DefaultKeySerializer());
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", "b");
        assertEquals("{\"DEFAULT:a\":\"b\"}", m.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.jdk.MapKeySerializationTest::testMapsWithBinaryKeys
    public void testMapsWithBinaryKeys() throws Exception
    {
        byte[] binary = new byte[] { 1, 2, 3, 4, 5 };

        
        MapWrapper<byte[], String> input = new MapWrapper<>(binary, "stuff");
        String expBase64 = Base64Variants.MIME.encode(binary);
        
        assertEquals(aposToQuotes("{'map':{'"+expBase64+"':'stuff'}}"),
                MAPPER.writeValueAsString(input));

        
        Map<byte[],String> map = new LinkedHashMap<>();
        map.put(binary, "xyz");
        assertEquals(aposToQuotes("{'"+expBase64+"':'xyz'}"),
                MAPPER.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.jdk.NumberSerTest::testDouble
    public void testDouble() throws Exception
    {
        double[] values = new double[] {
            0.0, 1.0, 0.1, -37.01, 999.99, 0.3, 33.3, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        };
        for (double d : values) {
            String expected = String.valueOf(d);
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                expected = "\""+d+"\"";
            }
            assertEquals(expected, MAPPER.writeValueAsString(Double.valueOf(d)));
        }
    }

// com.fasterxml.jackson.databind.ser.jdk.NumberSerTest::testBigInteger
    public void testBigInteger() throws Exception
    {
        BigInteger[] values = new BigInteger[] {
                BigInteger.ONE, BigInteger.TEN, BigInteger.ZERO,
                BigInteger.valueOf(1234567890L),
                new BigInteger("123456789012345678901234568"),
                new BigInteger("-1250000124326904597090347547457")
                };

        for (BigInteger value : values) {
            String expected = value.toString();
            assertEquals(expected, MAPPER.writeValueAsString(value));
        }
    }

// com.fasterxml.jackson.databind.ser.jdk.NumberSerTest::testNumbersAsString
    public void testNumbersAsString() throws Exception
    {
        assertEquals(aposToQuotes("{'value':'3'}"), MAPPER.writeValueAsString(new IntAsString()));
        assertEquals(aposToQuotes("{'value':'4'}"), MAPPER.writeValueAsString(new LongAsString()));
        assertEquals(aposToQuotes("{'value':'-0.5'}"), MAPPER.writeValueAsString(new DoubleAsString()));
        assertEquals(aposToQuotes("{'value':'0.25'}"), MAPPER.writeValueAsString(new BigDecimalAsString()));
        assertEquals(aposToQuotes("{'value':'123456'}"), MAPPER.writeValueAsString(new BigIntegerAsString()));
    }

// com.fasterxml.jackson.databind.ser.jdk.NumberSerTest::testConfigOverridesForNumbers
    public void testConfigOverridesForNumbers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Integer.TYPE) 
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        mapper.configOverride(Double.TYPE) 
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        mapper.configOverride(BigDecimal.class)
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));

        assertEquals(aposToQuotes("{'i':'3'}"),
                mapper.writeValueAsString(new IntWrapper(3)));
        assertEquals(aposToQuotes("{'value':'0.75'}"),
                mapper.writeValueAsString(new DoubleWrapper(0.75)));
        assertEquals(aposToQuotes("{'value':'-0.5'}"),
                mapper.writeValueAsString(new BigDecimalWrapper(BigDecimal.valueOf(-0.5))));
    }

// com.fasterxml.jackson.databind.ser.jdk.NumberSerTest::testNumberType
    public void testNumberType() throws Exception
    {
        assertEquals(aposToQuotes("{'value':1}"), MAPPER.writeValueAsString(new NumberWrapper(Byte.valueOf((byte) 1))));
        assertEquals(aposToQuotes("{'value':2}"), MAPPER.writeValueAsString(new NumberWrapper(Short.valueOf((short) 2))));
        assertEquals(aposToQuotes("{'value':3}"), MAPPER.writeValueAsString(new NumberWrapper(Integer.valueOf(3))));
        assertEquals(aposToQuotes("{'value':4}"), MAPPER.writeValueAsString(new NumberWrapper(Long.valueOf(4L))));
        assertEquals(aposToQuotes("{'value':0.5}"), MAPPER.writeValueAsString(new NumberWrapper(Float.valueOf(0.5f))));
        assertEquals(aposToQuotes("{'value':0.05}"), MAPPER.writeValueAsString(new NumberWrapper(Double.valueOf(0.05))));
        assertEquals(aposToQuotes("{'value':123}"), MAPPER.writeValueAsString(new NumberWrapper(BigInteger.valueOf(123))));
        assertEquals(aposToQuotes("{'value':0.025}"), MAPPER.writeValueAsString(new NumberWrapper(BigDecimal.valueOf(0.025))));
    }

// com.fasterxml.jackson.databind.ser.jdk.SqlDateSerializationTest::testSqlDate
    public void testSqlDate() throws IOException
    {
        
        final java.sql.Date date99 = new java.sql.Date(99, Calendar.APRIL, 1);
        final java.sql.Date date0 = new java.sql.Date(0);

        
        

        assertEquals(String.valueOf(date99.getTime()),
                MAPPER.writeValueAsString(date99));

        assertEquals(aposToQuotes("{'date':0}"),
                MAPPER.writeValueAsString(new SqlDateAsDefaultBean(0L)));

        
        assertEquals(aposToQuotes("{'date':0}"),
                MAPPER.writeValueAsString(new SqlDateAsNumberBean(0L)));

        
        ObjectWriter w = MAPPER.writer().without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
               
        assertEquals(quote("1999-04-01"), w.writeValueAsString(date99));
        assertEquals(quote(date0.toString()), w.writeValueAsString(date0));
        assertEquals(aposToQuotes("{'date':'"+date0.toString()+"'}"),
                w.writeValueAsString(new SqlDateAsDefaultBean(0L)));
    }

// com.fasterxml.jackson.databind.ser.jdk.SqlDateSerializationTest::testSqlTime
    public void testSqlTime() throws IOException
    {
        java.sql.Time time = new java.sql.Time(0L);
        
        
        assertEquals(quote(time.toString()), MAPPER.writeValueAsString(time));
    }

// com.fasterxml.jackson.databind.ser.jdk.SqlDateSerializationTest::testSqlTimestamp
    public void testSqlTimestamp() throws IOException
    {
        java.sql.Timestamp input = new java.sql.Timestamp(0L);
        
        Date altTnput = new Date(0L);
        assertEquals(MAPPER.writeValueAsString(altTnput),
                MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.jdk.SqlDateSerializationTest::testPatternWithSqlDate
    public void testPatternWithSqlDate() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.setTimeZone(TimeZone.getDefault());

        Person i = new Person();
        i.dateOfBirth = java.sql.Date.valueOf("1980-04-14");
        assertEquals(aposToQuotes("{'dateOfBirth':'1980.04.14'}"),
                mapper.writeValueAsString(i));
    }

// com.fasterxml.jackson.databind.ser.jdk.SqlDateSerializationTest::testSqlDateConfigOverride
    public void testSqlDateConfigOverride() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(java.sql.Date.class)
            .setFormat(JsonFormat.Value.forPattern("yyyy+MM+dd"));        
        assertEquals("\"1980+04+14\"",
            mapper.writeValueAsString(java.sql.Date.valueOf("1980-04-14")));
    }

// com.fasterxml.jackson.databind.ser.jdk.UntypedSerializationTest::testFromArray
    public void testFromArray()
        throws Exception
    {
        ArrayList<Object> doc = new ArrayList<Object>();
        doc.add("Elem1");
        doc.add(Integer.valueOf(3));
        Map<String,Object> struct = new LinkedHashMap<String, Object>();
        struct.put("first", Boolean.TRUE);
        struct.put("Second", new ArrayList<Object>());
        doc.add(struct);
        doc.add(Boolean.FALSE);

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory f =  new JsonFactory();

        
        for (int i = 0; i < 3; ++i) {
            String str = mapper.writeValueAsString(doc);
            
            JsonParser jp = f.createParser(str);
            assertEquals(JsonToken.START_ARRAY, jp.nextToken());
            
            assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals("Elem1", getAndVerifyText(jp));
            
            assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(3, jp.getIntValue());
            
            assertEquals(JsonToken.START_OBJECT, jp.nextToken());
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("first", getAndVerifyText(jp));
            
            assertEquals(JsonToken.VALUE_TRUE, jp.nextToken());
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("Second", getAndVerifyText(jp));
            
            if (jp.nextToken() != JsonToken.START_ARRAY) {
                fail("Expected START_ARRAY: JSON == '"+str+"'");
            }
            assertEquals(JsonToken.END_ARRAY, jp.nextToken());
            assertEquals(JsonToken.END_OBJECT, jp.nextToken());
            
            assertEquals(JsonToken.VALUE_FALSE, jp.nextToken());
            
            assertEquals(JsonToken.END_ARRAY, jp.nextToken());
            assertNull(jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.jdk.UntypedSerializationTest::testFromMap
    public void testFromMap()
        throws Exception
    {
        LinkedHashMap<String,Object> doc = new LinkedHashMap<String,Object>();
        JsonFactory f =  new JsonFactory();

        doc.put("a1", "\"text\"");
        doc.put("int", Integer.valueOf(137));
        doc.put("foo bar", Long.valueOf(1234567890L));

        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < 3; ++i) {
            String str = mapper.writeValueAsString(doc);
            JsonParser jp = f.createParser(str);
            
            assertEquals(JsonToken.START_OBJECT, jp.nextToken());
            
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("a1", getAndVerifyText(jp));
            assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals("\"text\"", getAndVerifyText(jp));
            
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("int", getAndVerifyText(jp));
            assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(137, jp.getIntValue());
            
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("foo bar", getAndVerifyText(jp));
            assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(1234567890L, jp.getLongValue());
            
            assertEquals(JsonToken.END_OBJECT, jp.nextToken());

            assertNull(jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.struct.BackReference1878Test::testChildDeserialization
    public void testChildDeserialization() throws Exception {
        Child child = MAPPER.readValue("{\"b\": {}}", Child.class);
        assertNotNull(child.b);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testSettings
    public void testSettings() {
        assertFalse(MAPPER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(DEFAULT_READER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertTrue(READER_WITH_ARRAYS.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testPOJOFromEmptyArray
    public void testPOJOFromEmptyArray() throws Exception
    {
        
        try {
            DEFAULT_READER.forType(Bean.class)
                .readValue(EMPTY_ARRAY);
            fail("Should not accept Empty Array for POJO by default");
        } catch (JsonMappingException e) {
            verifyException(e, "START_ARRAY token");
            assertValidLocation(e.getLocation());
        }

        
        Bean result = READER_WITH_ARRAYS.forType(Bean.class)
                .readValue(EMPTY_ARRAY);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testMapFromEmptyArray
    public void testMapFromEmptyArray() throws Exception
    {
        
        try {
            DEFAULT_READER.forType(Map.class)
                .readValue(EMPTY_ARRAY);
            fail("Should not accept Empty Array for Map by default");
        } catch (JsonMappingException e) {
            verifyException(e, "START_ARRAY token");
        }
        
        Map<?,?> result = READER_WITH_ARRAYS.forType(Map.class)
                .readValue(EMPTY_ARRAY);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testEnumMapFromEmptyArray
    public void testEnumMapFromEmptyArray() throws Exception
    {
    
        EnumMap<?,?> result2 = READER_WITH_ARRAYS.forType(new TypeReference<EnumMap<ABC,String>>() { })
                .readValue(EMPTY_ARRAY);
        assertNull(result2);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testWrapperFromEmptyArray
    public void testWrapperFromEmptyArray() throws Exception
    {
        _testNullWrapper(Boolean.class);
        _testNullWrapper(Byte.class);
        _testNullWrapper(Character.class);
        _testNullWrapper(Short.class);
        _testNullWrapper(Integer.class);
        _testNullWrapper(Long.class);
        _testNullWrapper(Float.class);
        _testNullWrapper(Double.class);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testNullStringFromEmptyArray
    public void testNullStringFromEmptyArray() throws Exception {
        _testNullWrapper(String.class);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testNullEnumFromEmptyArray
    public void testNullEnumFromEmptyArray() throws Exception {
        _testNullWrapper(ABC.class);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testStdJdkTypesFromEmptyArray
    public void testStdJdkTypesFromEmptyArray() throws Exception
    {
        _testNullWrapper(BigInteger.class);
        _testNullWrapper(BigDecimal.class);

        _testNullWrapper(UUID.class);

        _testNullWrapper(Date.class);
        _testNullWrapper(Calendar.class);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleStringArrayRead
    public void testSingleStringArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 'first' }");
        StringArrayWrapper result = MAPPER.readValue(json, StringArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals("first", result.values[0]);

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(String[].class).setFormat(JsonFormat.Value.empty()
                .withFeature(JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        StringArrayNotAnnoted result2 = mapper.readValue(json, StringArrayNotAnnoted.class);
        assertNotNull(result2.values);
        assertEquals(1, result2.values.length);
        assertEquals("first", result2.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleIntArrayRead
    public void testSingleIntArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 123 }");
        IntArrayWrapper result = MAPPER.readValue(json, IntArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(123, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleLongArrayRead
    public void testSingleLongArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': -205 }");
        LongArrayWrapper result = MAPPER.readValue(json, LongArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(-205L, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleBooleanArrayRead
    public void testSingleBooleanArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': true }");
        BooleanArrayWrapper result = MAPPER.readValue(json, BooleanArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(true, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleDoubleArrayRead
    public void testSingleDoubleArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': -0.5 }");
        DoubleArrayWrapper result = MAPPER.readValue(json, DoubleArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(-0.5, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleFloatArrayRead
    public void testSingleFloatArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 0.25 }");
        FloatArrayWrapper result = MAPPER.readValue(json, FloatArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(0.25f, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleElementArrayRead
    public void testSingleElementArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'roles': { 'Name': 'User', 'ID': '333' } }");
        RolesInArray response = MAPPER.readValue(json, RolesInArray.class);
        assertNotNull(response.roles);
        assertEquals(1, response.roles.length);
        assertEquals("333", response.roles[0].ID);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleStringListRead
    public void testSingleStringListRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 'first' }");
        StringListWrapper result = MAPPER.readValue(json, StringListWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals("first", result.values.get(0));
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleElementListRead
    public void testSingleElementListRead() throws Exception {
        String json = aposToQuotes(
                "{ 'roles': { 'Name': 'User', 'ID': '333' } }");
        RolesInList response = MAPPER.readValue(json, RolesInList.class);
        assertNotNull(response.roles);
        assertEquals(1, response.roles.size());
        assertEquals("333", response.roles.get(0).ID);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleEnumSetRead
    public void testSingleEnumSetRead() throws Exception {
        EnumSetWrapper result = MAPPER.readValue(aposToQuotes("{ 'values': 'B' }"),
                EnumSetWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(ABC.B, result.values.iterator().next());
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureOrderedMapTest::testOrderedMaps
    public void testOrderedMaps() throws Exception {
        SortedKeysMap map = new SortedKeysMap()
            .put("b", 2)
            .put("a", 1);
        assertEquals(aposToQuotes("{'values':{'a':1,'b':2}}"),
                MAPPER.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureUnwrapSingleTest::testWithArrayTypes
    public void testWithArrayTypes() throws Exception
    {
        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true]}"),
                MAPPER.writeValueAsString(new WrapWriteWithArrays()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':true}"),
                MAPPER.writer().with(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithArrays()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true]}"),
                MAPPER.writer().without(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithArrays()));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(String[].class).setFormat(JsonFormat.Value.empty()
                .withFeature(JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED));
        assertEquals(aposToQuotes("{'values':'a'}"),
                mapper.writeValueAsString(new StringArrayNotAnnoted("a")));
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureUnwrapSingleTest::testWithCollectionTypes
    public void testWithCollectionTypes() throws Exception
    {
        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true],'enums':'B'}"),
                MAPPER.writeValueAsString(new WrapWriteWithCollections()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':true,'enums':'B'}"),
                MAPPER.writer().with(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithCollections()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true],'enums':'B'}"),
                MAPPER.writer().without(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithCollections()));
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureUnwrapSingleTest::testUnwrapWithPrimitiveArraysEtc
    public void testUnwrapWithPrimitiveArraysEtc() throws Exception {
        assertEquals("{\"v\":7}", MAPPER.writeValueAsString(new UnwrapShortArray()));
        assertEquals("{\"v\":3}", MAPPER.writeValueAsString(new UnwrapIntArray()));
        assertEquals("{\"v\":1}", MAPPER.writeValueAsString(new UnwrapLongArray()));
        assertEquals("{\"v\":true}", MAPPER.writeValueAsString(new UnwrapBooleanArray()));

        assertEquals("{\"v\":0.5}", MAPPER.writeValueAsString(new UnwrapFloatArray()));
        assertEquals("{\"v\":0.25}", MAPPER.writeValueAsString(new UnwrapDoubleArray()));
        assertEquals("0.5",
                MAPPER.writer().with(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new double[] { 0.5 }));

        assertEquals("{\"v\":\"foo\"}", MAPPER.writeValueAsString(new UnwrapIterable()));
        assertEquals("{\"v\":\"x\"}", MAPPER.writeValueAsString(new UnwrapIterable("x")));
        assertEquals("{\"v\":[\"x\",null]}", MAPPER.writeValueAsString(new UnwrapIterable("x", null)));

        assertEquals("{\"v\":\"foo\"}", MAPPER.writeValueAsString(new UnwrapCollection()));
        assertEquals("{\"v\":\"x\"}", MAPPER.writeValueAsString(new UnwrapCollection("x")));
        assertEquals("{\"v\":[\"x\",null]}", MAPPER.writeValueAsString(new UnwrapCollection("x", null)));

        assertEquals("{\"v\":\"http://foo\"}", MAPPER.writeValueAsString(new UnwrapStringLike()));
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

// com.fasterxml.jackson.databind.struct.ScalarCoercionTest::testNullValueFromEmpty
    public void testNullValueFromEmpty() throws Exception
    {
        
        _verifyNullOkFromEmpty(Boolean.class, null);
        
        _verifyNullOkFromEmpty(Boolean.TYPE, Boolean.FALSE);

        _verifyNullOkFromEmpty(Byte.class, null);
        _verifyNullOkFromEmpty(Byte.TYPE, Byte.valueOf((byte) 0));
        _verifyNullOkFromEmpty(Short.class, null);
        _verifyNullOkFromEmpty(Short.TYPE, Short.valueOf((short) 0));
        _verifyNullOkFromEmpty(Character.class, null);
        _verifyNullOkFromEmpty(Character.TYPE, Character.valueOf((char) 0));
        _verifyNullOkFromEmpty(Integer.class, null);
        _verifyNullOkFromEmpty(Integer.TYPE, Integer.valueOf(0));
        _verifyNullOkFromEmpty(Long.class, null);
        _verifyNullOkFromEmpty(Long.TYPE, Long.valueOf(0L));
        _verifyNullOkFromEmpty(Float.class, null);
        _verifyNullOkFromEmpty(Float.TYPE, Float.valueOf(0.0f));
        _verifyNullOkFromEmpty(Double.class, null);
        _verifyNullOkFromEmpty(Double.TYPE, Double.valueOf(0.0));

        _verifyNullOkFromEmpty(BigInteger.class, null);
        _verifyNullOkFromEmpty(BigDecimal.class, null);
    }

// com.fasterxml.jackson.databind.struct.ScalarCoercionTest::testNullFailFromEmpty
    public void testNullFailFromEmpty() throws Exception
    {
        _verifyNullFail(Boolean.class);
        _verifyNullFail(Boolean.TYPE);

        _verifyNullFail(Byte.class);
        _verifyNullFail(Byte.TYPE);
        _verifyNullFail(Short.class);
        _verifyNullFail(Short.TYPE);
        _verifyNullFail(Character.class);
        _verifyNullFail(Character.TYPE);
        _verifyNullFail(Integer.class);
        _verifyNullFail(Integer.TYPE);
        _verifyNullFail(Long.class);
        _verifyNullFail(Long.TYPE);
        _verifyNullFail(Float.class);
        _verifyNullFail(Float.TYPE);
        _verifyNullFail(Double.class);
        _verifyNullFail(Double.TYPE);

        _verifyNullFail(BigInteger.class);
        _verifyNullFail(BigDecimal.class);
    }

// com.fasterxml.jackson.databind.struct.ScalarCoercionTest::testStringCoercionOk
    public void testStringCoercionOk() throws Exception
    {
        
        _verifyCoerceSuccess("1", Boolean.TYPE, Boolean.TRUE);
        _verifyCoerceSuccess("1", Boolean.class, Boolean.TRUE);
        _verifyCoerceSuccess(quote("true"), Boolean.TYPE, Boolean.TRUE);
        _verifyCoerceSuccess(quote("true"), Boolean.class, Boolean.TRUE);
        _verifyCoerceSuccess(quote("True"), Boolean.TYPE, Boolean.TRUE);
        _verifyCoerceSuccess(quote("True"), Boolean.class, Boolean.TRUE);
        _verifyCoerceSuccess("0", Boolean.TYPE, Boolean.FALSE);
        _verifyCoerceSuccess("0", Boolean.class, Boolean.FALSE);
        _verifyCoerceSuccess(quote("false"), Boolean.TYPE, Boolean.FALSE);
        _verifyCoerceSuccess(quote("false"), Boolean.class, Boolean.FALSE);
        _verifyCoerceSuccess(quote("False"), Boolean.TYPE, Boolean.FALSE);
        _verifyCoerceSuccess(quote("False"), Boolean.class, Boolean.FALSE);

        _verifyCoerceSuccess(quote("123"), Byte.TYPE, Byte.valueOf((byte) 123));
        _verifyCoerceSuccess(quote("123"), Byte.class, Byte.valueOf((byte) 123));
        _verifyCoerceSuccess(quote("123"), Short.TYPE, Short.valueOf((short) 123));
        _verifyCoerceSuccess(quote("123"), Short.class, Short.valueOf((short) 123));
        _verifyCoerceSuccess(quote("123"), Integer.TYPE, Integer.valueOf(123));
        _verifyCoerceSuccess(quote("123"), Integer.class, Integer.valueOf(123));
        _verifyCoerceSuccess(quote("123"), Long.TYPE, Long.valueOf(123));
        _verifyCoerceSuccess(quote("123"), Long.class, Long.valueOf(123));
        _verifyCoerceSuccess(quote("123.5"), Float.TYPE, Float.valueOf(123.5f));
        _verifyCoerceSuccess(quote("123.5"), Float.class, Float.valueOf(123.5f));
        _verifyCoerceSuccess(quote("123.5"), Double.TYPE, Double.valueOf(123.5));
        _verifyCoerceSuccess(quote("123.5"), Double.class, Double.valueOf(123.5));

        _verifyCoerceSuccess(quote("123"), BigInteger.class, BigInteger.valueOf(123));
        _verifyCoerceSuccess(quote("123.0"), BigDecimal.class, new BigDecimal("123.0"));
    }

// com.fasterxml.jackson.databind.struct.ScalarCoercionTest::testStringCoercionFail
    public void testStringCoercionFail() throws Exception
    {
        _verifyCoerceFail(quote("true"), Boolean.TYPE);
        _verifyCoerceFail(quote("true"), Boolean.class);
        _verifyCoerceFail(quote("123"), Byte.TYPE);
        _verifyCoerceFail(quote("123"), Byte.class);
        _verifyCoerceFail(quote("123"), Short.TYPE);
        _verifyCoerceFail(quote("123"), Short.class);
        _verifyCoerceFail(quote("123"), Integer.TYPE);
        _verifyCoerceFail(quote("123"), Integer.class);
        _verifyCoerceFail(quote("123"), Long.TYPE);
        _verifyCoerceFail(quote("123"), Long.class);
        _verifyCoerceFail(quote("123.5"), Float.TYPE);
        _verifyCoerceFail(quote("123.5"), Float.class);
        _verifyCoerceFail(quote("123.5"), Double.TYPE);
        _verifyCoerceFail(quote("123.5"), Double.class);

        _verifyCoerceFail(quote("123"), BigInteger.class);
        _verifyCoerceFail(quote("123.0"), BigDecimal.class);
    }

// com.fasterxml.jackson.databind.struct.ScalarCoercionTest::testMiscCoercionFail
    public void testMiscCoercionFail() throws Exception
    {
        
        _verifyCoerceFail("1", Boolean.TYPE);
        _verifyCoerceFail("1", Boolean.class);

        _verifyCoerceFail("65", Character.class);
        _verifyCoerceFail("65", Character.TYPE);
    }

// com.fasterxml.jackson.databind.struct.SingleValueAsArrayTest::testSuccessfulDeserializationOfObjectWithChainedArrayCreators
    public void testSuccessfulDeserializationOfObjectWithChainedArrayCreators() throws IOException
    {
        Bean1421A result = MAPPER.readValue(JSON, Bean1421A.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.struct.SingleValueAsArrayTest::testWithSingleString
    public void testWithSingleString() throws Exception {
        Bean1421B<List<String>> a = MAPPER.readValue(quote("test2"),
                new TypeReference<Bean1421B<List<String>>>() {});
        List<String> expected = new ArrayList<>();
        expected.add("test2");
        assertEquals(expected, a.value);
    }

// com.fasterxml.jackson.databind.struct.SingleValueAsArrayTest::testPrimitives
    public void testPrimitives() throws Exception {
        int[] i = MAPPER.readValue("16", int[].class);
        assertEquals(1, i.length);
        assertEquals(16, i[0]);

        long[] l = MAPPER.readValue("1234", long[].class);
        assertEquals(1, l.length);
        assertEquals(1234L, l[0]);

        double[] d = MAPPER.readValue("12.5", double[].class);
        assertEquals(1, d.length);
        assertEquals(12.5, d[0]);

        boolean[] b = MAPPER.readValue("true", boolean[].class);
        assertEquals(1, d.length);
        assertEquals(true, b[0]);
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

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testReadSimplePropertyValue
    public void testReadSimplePropertyValue() throws Exception
    {
        String json = "{\"value\":[true,\"Foobar\",42,13]}";
        PojoAsArrayWrapper p = MAPPER.readValue(json, PojoAsArrayWrapper.class);
        assertNotNull(p.value);
        assertTrue(p.value.complete);
        assertEquals("Foobar", p.value.name);
        assertEquals(42, p.value.x);
        assertEquals(13, p.value.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testReadSimpleRootValue
    public void testReadSimpleRootValue() throws Exception
    {
        String json = "[false,\"Bubba\",1,2]";
        FlatPojo p = MAPPER.readValue(json, FlatPojo.class);
        assertFalse(p.complete);
        assertEquals("Bubba", p.name);
        assertEquals(1, p.x);
        assertEquals(2, p.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testWriteSimplePropertyValue
    public void testWriteSimplePropertyValue() throws Exception
    {
        String json = MAPPER.writeValueAsString(new PojoAsArrayWrapper("Foobar", 42, 13, true));
        
        assertEquals("{\"value\":[true,\"Foobar\",42,13]}", json);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testWriteSimpleRootValue
    public void testWriteSimpleRootValue() throws Exception
    {
        String json = MAPPER.writeValueAsString(new FlatPojo("Bubba", 1, 2, false));
        
        assertEquals("[false,\"Bubba\",1,2]", json);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testNullColumn
    public void testNullColumn() throws Exception
    {
        assertEquals("[null,\"bar\"]", MAPPER.writeValueAsString(new TwoStringsBean()));
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testSerializeAsArrayWithSingleProperty
    public void testSerializeAsArrayWithSingleProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        String json = mapper.writeValueAsString(new SingleBean());
        assertEquals("\"foo\"", json);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testBeanAsArrayUnwrapped
    public void testBeanAsArrayUnwrapped() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        SingleBean result = mapper.readValue("[\"foobar\"]", SingleBean.class);
        assertNotNull(result);
        assertEquals("foobar", result.name);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testAnnotationOverride
    public void testAnnotationOverride() throws Exception
    {
        
        assertEquals("{\"value\":{\"x\":1,\"y\":2}}", MAPPER.writeValueAsString(new A()));

        
        ObjectMapper mapper2 = new ObjectMapper();
        mapper2.setAnnotationIntrospector(new ForceArraysIntrospector());
        assertEquals("[[1,2]]", mapper2.writeValueAsString(new A()));

        
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testWithMaps
    public void testWithMaps() throws Exception
    {
        AsArrayWithMap input = new AsArrayWithMap(1, 2);
        String json = MAPPER.writeValueAsString(input);
        AsArrayWithMap output = MAPPER.readValue(json, AsArrayWithMap.class);
        assertNotNull(output);
        assertNotNull(output.attrs);
        assertEquals(1, output.attrs.size());
        assertEquals(Integer.valueOf(2), output.attrs.get(1));
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testSimpleWithIndex
    public void testSimpleWithIndex() throws Exception
    {
        

        CreatorWithIndex value = MAPPER.readValue(aposToQuotes("[2,1]"),
                CreatorWithIndex.class);
        assertEquals(2, value._a);
        assertEquals(1, value._b);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testWithConfigOverrides
    public void testWithConfigOverrides() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(NonAnnotatedXY.class)
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.ARRAY));
        String json = mapper.writeValueAsString(new NonAnnotatedXY(2, 3));
        assertEquals("[2,3]", json);

        
        NonAnnotatedXY result = mapper.readValue(json, NonAnnotatedXY.class);
        assertNotNull(result);
        assertEquals(3, result.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testUnknownExtraProp
    public void testUnknownExtraProp() throws Exception
    {
        String json = "{\"value\":[true,\"Foobar\",42,13, false]}";
        try {
            MAPPER.readValue(json, PojoAsArrayWrapper.class);
            fail("should not pass with extra element");
        } catch (MismatchedInputException e) {
            verifyException(e, "Unexpected JSON values");
        }

        
        PojoAsArrayWrapper v = MAPPER.readerFor(PojoAsArrayWrapper.class)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(json);
        assertNotNull(v);
        
        assertEquals(v.value.x, 42);
        assertEquals(v.value.y, 13);
        assertTrue(v.value.complete);
        assertEquals("Foobar", v.value.name);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayAdvanced::testWithView
    public void testWithView() throws Exception
    {
        
        AsArrayWithView input = new AsArrayWithView();
        input.a = 1;
        input.b = 2;
        input.c = 3;
        String json = MAPPER.writerWithView(ViewA.class).writeValueAsString(input);
        assertEquals("[1,null,3]", json);

        
        AsArrayWithView result = MAPPER.readerFor(AsArrayWithView.class).withView(ViewB.class)
                .readValue("[1,2,3]");
        
        assertEquals(3, result.c);
        assertEquals(2, result.b);
        assertEquals(0, result.a);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayAdvanced::testWithViewAndCreator
    public void testWithViewAndCreator() throws Exception
    {
        AsArrayWithViewAndCreator result = MAPPER.readerFor(AsArrayWithViewAndCreator.class)
                .withView(ViewB.class)
                .readValue("[1,2,3]");
        
        assertEquals(3, result.c);
        assertEquals(2, result.b);
        assertEquals(0, result.a);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayAdvanced::testWithCreatorsOrdered
    public void testWithCreatorsOrdered() throws Exception
    {
        CreatorAsArray input = new CreatorAsArray(3, 4);
        input.a = 1;
        input.b = 2;

        
        String json = MAPPER.writeValueAsString(input);
        assertEquals("[3,4,1,2]", json);

        
        CreatorAsArray output = MAPPER.readValue(json, CreatorAsArray.class);
        assertEquals(1, output.a);
        assertEquals(2, output.b);
        assertEquals(3, output.x);
        assertEquals(4, output.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayAdvanced::testWithCreatorsShuffled
    public void testWithCreatorsShuffled() throws Exception
    {
        CreatorAsArrayShuffled input = new CreatorAsArrayShuffled(3, 4);
        input.a = 1;
        input.b = 2;

        
        String json = MAPPER.writeValueAsString(input);
        assertEquals("[1,2,3,4]", json);

        
        CreatorAsArrayShuffled output = MAPPER.readValue(json, CreatorAsArrayShuffled.class);
        assertEquals(1, output.a);
        assertEquals(2, output.b);
        assertEquals(3, output.x);
        assertEquals(4, output.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayWithBuilder::testSimpleBuilder
    public void testSimpleBuilder() throws Exception
    {
        
        ValueClassXY value = MAPPER.readValue("[1,2]", ValueClassXY.class);
        assertEquals(2, value._x);
        assertEquals(3, value._y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayWithBuilder::testBuilderWithUpdate
    public void testBuilderWithUpdate() throws Exception
    {
        
        try {
             MAPPER.readerFor(ValueClassXY.class)
                    .withValueToUpdate(new ValueClassXY(6, 7))
                    .readValue("[1,2]");
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Deserialization of");
            verifyException(e, "by passing existing instance");
            verifyException(e, "ValueClassXY");
        }
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayWithBuilder::testWithCreator
    public void testWithCreator() throws Exception
    {
        CreatorValue value = MAPPER.readValue("[1,2,3]", CreatorValue.class);
        assertEquals(1, value.a);
        assertEquals(2, value.b);
        assertEquals(3, value.c);

        
        value = MAPPER.readValue("[1,2]", CreatorValue.class);
        assertEquals(1, value.a);
        assertEquals(2, value.b);
        assertEquals(0, value.c);

        value = MAPPER.readValue("[1]", CreatorValue.class);
        assertEquals(1, value.a);
        assertEquals(0, value.b);
        assertEquals(0, value.c);

        value = MAPPER.readValue("[]", CreatorValue.class);
        assertEquals(0, value.a);
        assertEquals(0, value.b);
        assertEquals(0, value.c);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayWithBuilder::testWithCreatorAndView
    public void testWithCreatorAndView() throws Exception
    {
        ObjectReader reader = MAPPER.readerFor(CreatorValue.class);
        CreatorValue value;

        
        value = reader.withView(String.class).readValue("[1,2,3]");
        assertEquals(1, value.a);
        assertEquals(2, value.b);
        assertEquals(3, value.c);

        
        value = reader.withView(Character.class).readValue("[1,2,3]");
        assertEquals(1, value.a);
        assertEquals(2, value.b);
        assertEquals(0, value.c);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayWithBuilder::testUnknownExtraProp
    public void testUnknownExtraProp() throws Exception
    {
        String json = "[1, 2, 3, 4]";
        try {
            MAPPER.readValue(json, ValueClassXY.class);
            fail("should not pass with extra element");
        } catch (MismatchedInputException e) {
            verifyException(e, "Unexpected JSON values");
        }

        
        ValueClassXY v = MAPPER.readerFor(ValueClassXY.class)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(json);
        assertNotNull(v);
        
        assertEquals(v._x, 2);
        assertEquals(v._y, 3);
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

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testSimpleUnwrappingSerialize
    public void testSimpleUnwrappingSerialize() throws Exception {
        assertEquals("{\"name\":\"Tatu\",\"x\":1,\"y\":2}",
                MAPPER.writeValueAsString(new Unwrapping("Tatu", 1, 2)));
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testDeepUnwrappingSerialize
    public void testDeepUnwrappingSerialize() throws Exception {
        assertEquals("{\"name\":\"Tatu\",\"x\":1,\"y\":2}",
                MAPPER.writeValueAsString(new DeepUnwrapping("Tatu", 1, 2)));
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testSimpleUnwrappedDeserialize
    public void testSimpleUnwrappedDeserialize() throws Exception
    {
        Unwrapping bean = MAPPER.readValue("{\"name\":\"Tatu\",\"y\":7,\"x\":-13}",
                Unwrapping.class);
        assertEquals("Tatu", bean.name);
        Location loc = bean.location;
        assertNotNull(loc);
        assertEquals(-13, loc.x);
        assertEquals(7, loc.y);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testDoubleUnwrapping
    public void testDoubleUnwrapping() throws Exception
    {
        TwoUnwrappedProperties bean = MAPPER.readValue("{\"first\":\"Joe\",\"y\":7,\"last\":\"Smith\",\"x\":-13}",
                TwoUnwrappedProperties.class);
        Location loc = bean.location;
        assertNotNull(loc);
        assertEquals(-13, loc.x);
        assertEquals(7, loc.y);
        Name name = bean.name;
        assertNotNull(name);
        assertEquals("Joe", name.first);
        assertEquals("Smith", name.last);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testDeepUnwrapping
    public void testDeepUnwrapping() throws Exception
    {
        DeepUnwrapping bean = MAPPER.readValue("{\"x\":3,\"name\":\"Bob\",\"y\":27}",
                DeepUnwrapping.class);
        Unwrapping uw = bean.unwrapped;
        assertNotNull(uw);
        assertEquals("Bob", uw.name);
        Location loc = uw.location;
        assertNotNull(loc);
        assertEquals(3, loc.x);
        assertEquals(27, loc.y);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testUnwrappedDeserializeWithCreator
    public void testUnwrappedDeserializeWithCreator() throws Exception
    {
        UnwrappingWithCreator bean = MAPPER.readValue("{\"x\":1,\"y\":2,\"name\":\"Tatu\"}",
                UnwrappingWithCreator.class);
        assertEquals("Tatu", bean.name);
        Location loc = bean.location;
        assertNotNull(loc);
        assertEquals(1, loc.x);
        assertEquals(2, loc.y);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testIssue615
    public void testIssue615() throws Exception
    {
        Parent input = new Parent("name");
        String json = MAPPER.writeValueAsString(input);
        Parent output = MAPPER.readValue(json, Parent.class);
        assertEquals("name", output.c1.field);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testUnwrappedAsPropertyIndicator
    public void testUnwrappedAsPropertyIndicator() throws Exception
    {
        Inner inner = new Inner();
        inner.animal = "Zebra";

        Outer outer = new Outer();
        outer.inner = inner;

        String actual = MAPPER.writeValueAsString(outer);

        assertTrue(actual.contains("animal"));
        assertTrue(actual.contains("Zebra"));
        assertFalse(actual.contains("inner"));
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testCaseInsensitiveUnwrap
    public void testCaseInsensitiveUnwrap() throws Exception
    {
        ObjectMapper mapper = objectMapperBuilder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .build();
        Person p = mapper.readValue("{ }", Person.class);
        assertNotNull(p);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testIssue2088UnwrappedFieldsAfterLastCreatorProp
    public void testIssue2088UnwrappedFieldsAfterLastCreatorProp() throws Exception
    {
        Issue2088Bean bean = MAPPER.readValue("{\"x\":1,\"a\":2,\"y\":3,\"b\":4}", Issue2088Bean.class);
        assertEquals(1, bean.x);
        assertEquals(2, bean.w.a);
        assertEquals(3, bean.y);
        assertEquals(4, bean.w.b);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedRecursive383::testRecursiveUsage
    public void testRecursiveUsage() throws Exception
    {
        final String JSON = "{ 'name': 'Bob', 'age': 45, 'gender': 0, 'child.name': 'Bob jr', 'child.age': 15 }";
        RecursivePerson p = MAPPER.readValue(aposToQuotes(JSON), RecursivePerson.class);
        assertNotNull(p);
        assertEquals("Bob", p.name);
        assertNotNull(p.child);
        assertEquals("Bob jr", p.child.name);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testPrefixedUnwrappingSerialize
    public void testPrefixedUnwrappingSerialize() throws Exception
    {
        assertEquals("{\"name\":\"Tatu\",\"_x\":1,\"_y\":2}",
                MAPPER.writeValueAsString(new PrefixUnwrap("Tatu", 1, 2)));
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testDeepPrefixedUnwrappingSerialize
    public void testDeepPrefixedUnwrappingSerialize() throws Exception
    {
        String json = MAPPER.writeValueAsString(new DeepPrefixUnwrap("Bubba", 1, 1));
        assertEquals("{\"u.name\":\"Bubba\",\"u._x\":1,\"u._y\":1}", json);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testHierarchicConfigSerialize
    public void testHierarchicConfigSerialize() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ConfigRoot("Fred", 25));
        assertEquals("{\"general.names.name\":\"Fred\",\"misc.value\":25}", json);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testPrefixedUnwrapping
    public void testPrefixedUnwrapping() throws Exception
    {
        PrefixUnwrap bean = MAPPER.readValue("{\"name\":\"Axel\",\"_x\":4,\"_y\":7}", PrefixUnwrap.class);
        assertNotNull(bean);
        assertEquals("Axel", bean.name);
        assertNotNull(bean.location);
        assertEquals(4, bean.location.x);
        assertEquals(7, bean.location.y);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testDeepPrefixedUnwrappingDeserialize
    public void testDeepPrefixedUnwrappingDeserialize() throws Exception
    {
        DeepPrefixUnwrap bean = MAPPER.readValue("{\"u.name\":\"Bubba\",\"u._x\":2,\"u._y\":3}",
                DeepPrefixUnwrap.class);
        assertNotNull(bean.unwrapped);
        assertNotNull(bean.unwrapped.location);
        assertEquals(2, bean.unwrapped.location.x);
        assertEquals(3, bean.unwrapped.location.y);
        assertEquals("Bubba", bean.unwrapped.name);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testHierarchicConfigDeserialize
    public void testHierarchicConfigDeserialize() throws Exception
    {
        ConfigRoot root = MAPPER.readValue("{\"general.names.name\":\"Bob\",\"misc.value\":3}",
                ConfigRoot.class);
        assertNotNull(root.general);
        assertNotNull(root.general.names);
        assertNotNull(root.misc);
        assertEquals(3, root.misc.value);
        assertEquals("Bob", root.general.names.name);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testHierarchicConfigRoundTrip
    public void testHierarchicConfigRoundTrip() throws Exception
    {
        ConfigAlternate input = new ConfigAlternate(123, "Joe", 42);
        String json = MAPPER.writeValueAsString(input);

        ConfigAlternate root = MAPPER.readValue(json, ConfigAlternate.class);
        assertEquals(123, root.id);
        assertNotNull(root.general);
        assertNotNull(root.general.names);
        assertNotNull(root.misc);
        assertEquals("Joe", root.general.names.name);
        assertEquals(42, root.misc.value);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testIssue226
    public void testIssue226() throws Exception
    {
        Parent input = new Parent();
        input.c1 = new Child();
        input.c1.sc1 = new SubChild();
        input.c1.sc1.value = "a";
        input.c2 = new Child();
        input.c2.sc1 = new SubChild();
        input.c2.sc1.value = "b";

        String json = MAPPER.writeValueAsString(input);

        Parent output = MAPPER.readValue(json, Parent.class);
        assertNotNull(output.c1);
        assertNotNull(output.c2);

        assertNotNull(output.c1.sc1);
        assertNotNull(output.c2.sc1);
        
        assertEquals("a", output.c1.sc1.value);
        assertEquals("b", output.c2.sc1.value);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithSameName647::testUnwrappedWithSamePropertyName
    public void testUnwrappedWithSamePropertyName() throws Exception {
        final String JSON = "{'mail': {'mail': 'the mail text'}}";
        UnwrappedWithSamePropertyName result = MAPPER.readValue(aposToQuotes(JSON), UnwrappedWithSamePropertyName.class);
        assertNotNull(result.mail);
        assertNotNull(result.mail.mail);
        assertEquals("the mail text", result.mail.mail.mail);
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

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testBooleanPrimitiveArrayUnwrap
    public void testBooleanPrimitiveArrayUnwrap() throws Exception
    {
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        BooleanBean result = mapper.readValue(new StringReader("{\"v\":[true]}"), BooleanBean.class);
        assertTrue(result._v);

        _verifyMultiValueArrayFail("[{\"v\":[true,true]}]", BooleanBean.class);

        result = mapper.readValue("{\"v\":[null]}", BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        
        result = mapper.readValue("[{\"v\":[null]}]", BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        
        boolean[] array = mapper.readValue(new StringReader("[ [ null ] ]"), boolean[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertFalse(array[0]);
    }

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testSingleElementScalarArrays
    public void testSingleElementScalarArrays() throws Exception {
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

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testSingleElementArrayDisabled
    public void testSingleElementArrayDisabled() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("[42]", Integer.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }
        try {
            mapper.readValue("[42]", Integer.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }
        try {
            mapper.readValue("[42342342342342]", Long.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }
        try {
            mapper.readValue("[42342342342342342]", Long.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }

        try {
            mapper.readValue("[42]", Short.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }
        try {
            mapper.readValue("[42]", Short.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }

        try {
            mapper.readValue("[327.2323]", Float.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }
        try {
            mapper.readValue("[82.81902]", Float.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }

        try {
            mapper.readValue("[22]", Byte.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }
        try {
            mapper.readValue("[22]", Byte.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }

        try {
            mapper.readValue("['d']", Character.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }
        try {
            mapper.readValue("['d']", Character.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }

        try {
            mapper.readValue("[true]", Boolean.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }
        try {
            mapper.readValue("[true]", Boolean.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testMultiValueArrayException
    public void testMultiValueArrayException() throws IOException {
        _verifyMultiValueArrayFail("[42,42]", Integer.class);
        _verifyMultiValueArrayFail("[42,42]", Integer.TYPE);
        _verifyMultiValueArrayFail("[42342342342342,42342342342342]", Long.class);
        _verifyMultiValueArrayFail("[42342342342342342,42342342342342]", Long.TYPE);
        _verifyMultiValueArrayFail("[42,42]", Short.class);
        _verifyMultiValueArrayFail("[42,42]", Short.TYPE);
        _verifyMultiValueArrayFail("[22,23]", Byte.class);
        _verifyMultiValueArrayFail("[22,23]", Byte.TYPE);
        _verifyMultiValueArrayFail("[327.2323,327.2323]", Float.class);
        _verifyMultiValueArrayFail("[82.81902,327.2323]", Float.TYPE);
        _verifyMultiValueArrayFail("[42.273,42.273]", Double.class);
        _verifyMultiValueArrayFail("[42.2723,42.273]", Double.TYPE);
        _verifyMultiValueArrayFail(asArray(quote("c") + ","  + quote("d")), Character.class);
        _verifyMultiValueArrayFail(asArray(quote("c") + ","  + quote("d")), Character.TYPE);
        _verifyMultiValueArrayFail("[true,false]", Boolean.class);
        _verifyMultiValueArrayFail("[true,false]", Boolean.TYPE);
    }

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testSingleString
    public void testSingleString() throws Exception
    {
        String value = "FOO!";
        String result = MAPPER.readValue("\""+value+"\"", String.class);
        assertEquals(value, result);
    }

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testSingleStringWrapped
    public void testSingleStringWrapped() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        String value = "FOO!";
        try {
            mapper.readValue("[\""+value+"\"]", String.class);
            fail("Exception not thrown when attempting to unwrap a single value 'String' array into a simple String");
        } catch (MismatchedInputException exp) {
            verifyException(exp, "Cannot deserialize");
            verifyException(exp, "out of START_ARRAY");
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
            mapper.readValue("[\""+value+"\",\""+value+"\"]", String.class);
            fail("Exception not thrown when attempting to unwrap a single value 'String' array that contained more than one value into a simple String");
        } catch (MismatchedInputException exp) {
            verifyException(exp, "Attempted to unwrap");
        }
        String result = mapper.readValue("[\""+value+"\"]", String.class);
        assertEquals(value, result);
    }

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testBigDecimal
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
        } catch (MismatchedInputException exp) {
            verifyException(exp, "Cannot deserialize");
            verifyException(exp, "out of START_ARRAY");
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue("[" + value.toString() + "]", BigDecimal.class);
        assertEquals(value, result);
        
        try {
            mapper.readValue("[" + value.toString() + "," + value.toString() + "]", BigDecimal.class);
            fail("Exception was not thrown when attempting to read a muti value array of BigDecimal when UNWRAP_SINGLE_VALUE_ARRAYS feature is enabled");
        } catch (MismatchedInputException exp) {
            verifyException(exp, "Attempted to unwrap");
        }
    }

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testBigInteger
    public void testBigInteger() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        BigInteger value = new BigInteger("-1234567890123456789012345567809");
        BigInteger result = mapper.readValue(value.toString(), BigInteger.class);
        assertEquals(value, result);

        try {
            mapper.readValue("[" + value.toString() + "]", BigInteger.class);
            fail("Exception was not thrown when attempting to read a single value array of BigInteger when UNWRAP_SINGLE_VALUE_ARRAYS feature is disabled");
        } catch (MismatchedInputException exp) {
            verifyException(exp, "Cannot deserialize");
            verifyException(exp, "out of START_ARRAY");
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue("[" + value.toString() + "]", BigInteger.class);
        assertEquals(value, result);
        
        try {
            mapper.readValue("[" + value.toString() + "," + value.toString() + "]", BigInteger.class);
            fail("Exception was not thrown when attempting to read a multi-value array of BigInteger when UNWRAP_SINGLE_VALUE_ARRAYS feature is enabled");
        } catch (MismatchedInputException exp) {
            verifyException(exp, "Attempted to unwrap");
        }        
    }

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testClassAsArray
    public void testClassAsArray() throws Exception
    {
        Class<?> result = MAPPER
                    .readerFor(Class.class)
                    .with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                    .readValue(quote(String.class.getName()));
        assertEquals(String.class, result);

        try {
            MAPPER.readerFor(Class.class)
                .without(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .readValue("[" + quote(String.class.getName()) + "]");
            fail("Did not throw exception when UNWRAP_SINGLE_VALUE_ARRAYS feature was disabled and attempted to read a Class array containing one element");
        } catch (MismatchedInputException e) {
            verifyException(e, "out of START_ARRAY token");
        }

        _verifyMultiValueArrayFail("[" + quote(Object.class.getName()) + "," + quote(Object.class.getName()) +"]",
                Class.class);
        result = MAPPER.readerFor(Class.class)
                .with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .readValue("[" + quote(String.class.getName()) + "]");
        assertEquals(String.class, result);
    }

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testURIAsArray
    public void testURIAsArray() throws Exception
    {
        final ObjectReader reader = MAPPER.readerFor(URI.class);
        final URI value = new URI("http://foo.com");
        try {
            reader.without(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .readValue("[\""+value.toString()+"\"]");
            fail("Did not throw exception for single value array when UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (MismatchedInputException e) {
            verifyException(e, "out of START_ARRAY token");
        }
        
        _verifyMultiValueArrayFail("[\""+value.toString()+"\",\""+value.toString()+"\"]", URI.class);
    }

// com.fasterxml.jackson.databind.struct.UnwrapSingleArrayScalarsTest::testUUIDAsArray
    public void testUUIDAsArray() throws Exception
    {
        final ObjectReader reader = MAPPER.readerFor(UUID.class);
        final String uuidStr = "76e6d183-5f68-4afa-b94a-922c1fdb83f8";
        UUID uuid = UUID.fromString(uuidStr);
        try {
            NO_UNWRAPPING_READER.forType(UUID.class)
                .readValue("[" + quote(uuidStr) + "]");
            fail("Exception was not thrown when UNWRAP_SINGLE_VALUE_ARRAYS is disabled and attempted to read a single value array as a single element");
        } catch (MismatchedInputException e) {
            verifyException(e, "out of START_ARRAY token");
        }
        assertEquals(uuid,
                reader.with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                    .readValue("[" + quote(uuidStr) + "]"));
        _verifyMultiValueArrayFail("[" + quote(uuidStr) + "," + quote(uuidStr) + "]", UUID.class);
    }

// com.fasterxml.jackson.databind.struct.UnwrappedCreatorParam265Test::testUnwrappedWithUnnamedCreatorParam
    public void testUnwrappedWithUnnamedCreatorParam() throws Exception
    {
        JPersonWithoutName person = new JPersonWithoutName("MyName", new JAddress("main street", "springfield", "WA"));
        ObjectMapper mapper = new ObjectMapper();
        
        String json = mapper.writeValueAsString(person);

        
        try {
             mapper.readValue(json, JPersonWithoutName.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Cannot define Creator parameter");
            verifyException(e, "@JsonUnwrapped");
        }
    }

// com.fasterxml.jackson.databind.struct.UnwrappedCreatorParam265Test::testUnwrappedWithNamedCreatorParam
    public void testUnwrappedWithNamedCreatorParam() throws Exception
    {
        JPersonWithName person = new JPersonWithName("MyName", new JAddress("main street", "springfield", "WA"));
        ObjectMapper mapper = new ObjectMapper();
        
        String json = mapper.writeValueAsString(person);
        try {
             mapper.readValue(json, JPersonWithName.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Cannot define Creator property \"address\"");
            verifyException(e, "@JsonUnwrapped");
        }
    }

// com.fasterxml.jackson.databind.struct.UnwrappedWithView1559Test::testCanSerializeSimpleWithDefaultView
    public void testCanSerializeSimpleWithDefaultView() throws Exception {
        String json = objectMapperBuilder().configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .build()
                .writeValueAsString(new Health());
        assertEquals(aposToQuotes("{}"), json);
        
        json = objectMapperBuilder().configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
                .build()
                .writeValueAsString(new Health());
        assertEquals(aposToQuotes("{}"), json);
    }

// com.fasterxml.jackson.databind.type.ContainerTypesTest::testExplicitCollectionType
    public void testExplicitCollectionType() throws Exception
    {
        JavaType t = MAPPER.getTypeFactory()
                .constructCollectionType(LongList.class, Long.class);
        assertEquals(LongList.class, t.getRawClass());
        assertEquals(Long.class, t.getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.ContainerTypesTest::testImplicitCollectionType
    public void testImplicitCollectionType() throws Exception
    {
        JavaType t = MAPPER.getTypeFactory()
                .constructParametricType(List.class, Long.class);
        assertEquals(CollectionType.class, t.getClass());
        assertEquals(List.class, t.getRawClass());
        assertEquals(Long.class, t.getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.ContainerTypesTest::testMissingCollectionType
    public void testMissingCollectionType() throws Exception
    {
        TypeFactory tf = MAPPER.getTypeFactory().withCache(new LRUMap<Object,JavaType>(4, 8));
        JavaType t = tf.constructParametricType(List.class, HashMap.class);
        assertEquals(CollectionType.class, t.getClass());
        assertEquals(List.class, t.getRawClass());
        assertEquals(HashMap.class, t.getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.ContainerTypesTest::testExplicitMapType
    public void testExplicitMapType() throws Exception
    {
        JavaType t = MAPPER.getTypeFactory()
                .constructMapType(StringLongMap.class,
                        String.class, Long.class);
        assertEquals(StringLongMap.class, t.getRawClass());
        assertEquals(String.class, t.getKeyType().getRawClass());
        assertEquals(Long.class, t.getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.ContainerTypesTest::testImplicitMapType
    public void testImplicitMapType() throws Exception
    {
        JavaType t = MAPPER.getTypeFactory()
                .constructParametricType(Map.class, Long.class, Boolean.class);
        assertEquals(MapType.class, t.getClass());
        assertEquals(Long.class, t.getKeyType().getRawClass());
        assertEquals(Boolean.class, t.getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.ContainerTypesTest::testMismatchedCollectionType
    public void testMismatchedCollectionType() throws Exception
    {
        try {
            MAPPER.getTypeFactory()
                .constructCollectionType(LongList.class, String.class);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "`"+getClass().getName()+"$LongList` did not resolve to something");
            verifyException(e, "element type");
        }
    }

// com.fasterxml.jackson.databind.type.ContainerTypesTest::testMismatchedMapType
    public void testMismatchedMapType() throws Exception
    {
        
        try {
            MAPPER.getTypeFactory()
                .constructMapType(StringLongMap.class, Boolean.class, Long.class);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "`"+getClass().getName()+"$StringLongMap` did not resolve to something");
            verifyException(e, "key type");
        }
        
        try {
            MAPPER.getTypeFactory()
                .constructMapType(StringLongMap.class, String.class, Class.class);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "`"+getClass().getName()+"$StringLongMap` did not resolve to something");
            verifyException(e, "value type");
        }
    }

// com.fasterxml.jackson.databind.type.DeprecatedConstructType1456Test::testGenericResolutionUsingDeprecated
    public void testGenericResolutionUsingDeprecated() throws Exception
    {
        Method proceed = BaseController.class.getMethod("process", BaseEntity.class);
        Type entityType = proceed.getGenericParameterTypes()[0];

        JavaType resolvedType = MAPPER.getTypeFactory().constructType(entityType, ImplController.class);
        assertEquals(ImplEntity.class, resolvedType.getRawClass());
    }

// com.fasterxml.jackson.databind.type.DeprecatedConstructType1456Test::testGenericParameterViaClass
    public void testGenericParameterViaClass() throws Exception
    {
        BeanDescription desc = MAPPER.getDeserializationConfig().introspect(
                MAPPER.constructType(ImplController.class));
        AnnotatedClass ac = desc.getClassInfo();
        AnnotatedMethod m = ac.findMethod("process", new Class<?>[] { BaseEntity.class });
        assertNotNull(m);
        assertEquals(1, m.getParameterCount());
        AnnotatedParameter param = m.getParameter(0);
        assertEquals(ImplEntity.class, param.getType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.LocalTypeTest::testLocalPartialType609
    public void testLocalPartialType609() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        EntityContainer input = new EntityContainer(); 
        input.entity = new RuleForm(12);
        String json = mapper.writeValueAsString(input);
        
        EntityContainer output = mapper.readValue(json, EntityContainer.class);
        assertEquals(12, output.getEntity().value);
    }

// com.fasterxml.jackson.databind.type.NestedTypes1604Test::testIssue1604Simple
    public void testIssue1604Simple() throws Exception
    {
        List<Inner> inners = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            inners.add(new Inner(i));
        }
        BadOuter badOuter = new BadOuter(Data.of(inners));

        
        String json = objectMapper.writeValueAsString(badOuter);
        assertNotNull(json);
   }

// com.fasterxml.jackson.databind.type.NestedTypes1604Test::testIssue1604Subtype
    public void testIssue1604Subtype() throws Exception
    {
        List<Inner> inners = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            inners.add(new Inner(i));
        }
        BadOuter badOuter = new BadOuter(Data.ofRefined(inners));
        String json = objectMapper.writeValueAsString(badOuter);
        assertNotNull(json);
   }

// com.fasterxml.jackson.databind.type.NestedTypes1604Test::testIssue1604Sneaky
    public void testIssue1604Sneaky() throws Exception
    {
        List<Inner> inners = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            inners.add(new Inner(i));
        }
        BadOuter badOuter = new BadOuter(Data.ofSneaky(inners));
        String json = objectMapper.writeValueAsString(badOuter);
        assertNotNull(json);
   }

// com.fasterxml.jackson.databind.type.PolymorphicList036Test::testPolymorphicWithOverride
    public void testPolymorphicWithOverride() throws Exception
    {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(StringyList.class, String.class);
        
        StringyList<String> list = new StringyList<String>();
        list.add("value 1");
        list.add("value 2");
        
        String serialized = MAPPER.writeValueAsString(list);

        
        StringyList<String> deserialized = MAPPER.readValue(serialized, type);

        
        assertNotNull(deserialized);
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

// com.fasterxml.jackson.databind.type.RecursiveTypeTest::testRecursiveType
    public void testRecursiveType()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(HashTree.class);
        assertNotNull(type);
    }

// com.fasterxml.jackson.databind.type.RecursiveTypeTest::testRecursivePair
    public void testRecursivePair() throws Exception
    {
        JavaType t = MAPPER.constructType(ImmutablePair.class);

        assertNotNull(t);
        assertEquals(ImmutablePair.class, t.getRawClass());

        List<ImmutablePair<String, Double>> list = new ArrayList<ImmutablePair<String, Double>>();
        list.add(ImmutablePair.of("Hello World!", 123d));
        String json = MAPPER.writeValueAsString(list);

        assertNotNull(json);

        
    }

// com.fasterxml.jackson.databind.type.RecursiveTypeTest::testJavaTypeToString
    public void testJavaTypeToString() throws Exception
    {
        TypeFactory tf = objectMapper().getTypeFactory();
        String desc = tf.constructType(DataDefinition.class).toString();
        assertNotNull(desc);
        
        if (!desc.contains("map type")) {
            fail("Description should contain 'map type', did not: "+desc);
        }
        if (!desc.contains("recursive type")) {
            fail("Description should contain 'recursive type', did not: "+desc);
        }
    }

// com.fasterxml.jackson.databind.type.RecursiveTypeTest::testSuperClassWithReferencedJavaType
    public void testSuperClassWithReferencedJavaType() {
        TypeFactory tf = objectMapper().getTypeFactory();
        tf.constructType(Base.class); 
        JavaType subType = tf.constructType(Sub.class);
        
        JavaType baseTypeFromSub = subType.getSuperClass();
        assertNotNull(baseTypeFromSub.getSuperClass());
    }

// com.fasterxml.jackson.databind.type.TestAnnotatedClass::testFieldIntrospection
    public void testFieldIntrospection()
    {
        SerializationConfig config = MAPPER.getSerializationConfig();
        JavaType t = MAPPER.constructType(FieldBean.class);
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, t, config);
        
        assertEquals(2, ac.getFieldCount());
        for (AnnotatedField f : ac.fields()) {
            String fname = f.getName();
            if (!"bar".equals(fname) && !"props".equals(fname)) {
                fail("Unexpected field name '"+fname+"'");
            }
        }
    }

// com.fasterxml.jackson.databind.type.TestAnnotatedClass::testConstructorIntrospection
    public void testConstructorIntrospection()
    {
        
        
        Bean1005 bean = new Bean1005(13);
        SerializationConfig config = MAPPER.getSerializationConfig();
        JavaType t = MAPPER.constructType(bean.getClass());
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, t, config);
        assertEquals(1, ac.getConstructors().size());
    }

// com.fasterxml.jackson.databind.type.TestAnnotatedClass::testArrayTypeIntrospection
    public void testArrayTypeIntrospection() throws Exception
    {
        AnnotatedClass ac = AnnotatedClassResolver.resolve(MAPPER.getSerializationConfig(),
                MAPPER.constructType(int[].class), null);
        
        
        assertFalse(ac.memberMethods().iterator().hasNext());
        assertFalse(ac.fields().iterator().hasNext());
    }

// com.fasterxml.jackson.databind.type.TestAnnotatedClass::testIntrospectionWithRawClass
    public void testIntrospectionWithRawClass() throws Exception
    {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(MAPPER.getSerializationConfig(),
                String.class, null);
        
        
        assertFalse(ac.memberMethods().iterator().hasNext());
        assertFalse(ac.fields().iterator().hasNext());
    }

// com.fasterxml.jackson.databind.type.TestGenericFieldInSubtype::test677
    public void test677() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        JavaType t677 = mapper.constructType(Result677.Success677.class);
        assertNotNull(t677);
        Result677.Success677<Integer> s = new Result677.Success677<Integer>(Integer.valueOf(4));
        String json = mapper.writeValueAsString(s);
        assertEquals("{\"value\":4}", json);
    }

// com.fasterxml.jackson.databind.type.TestGenericFieldInSubtype::testInnerType
    public void testInnerType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        BaseType.SubType<?> r = mapper.readValue("{}", BaseType.SubType.class);
        assertNotNull(r);
    }

// com.fasterxml.jackson.databind.type.TestGenericsBounded::testLowerBound
    public void testLowerBound() throws Exception
    {
        IntBeanWrapper<?> result = MAPPER.readValue("{\"wrapped\":{\"x\":3}}",
                IntBeanWrapper.class);
        assertNotNull(result);
        assertEquals(IntBean.class, result.wrapped.getClass());
        assertEquals(3, result.wrapped.x);
    }

// com.fasterxml.jackson.databind.type.TestGenericsBounded::testBounded
    public void testBounded() throws Exception
    {
        BoundedWrapper<IntBean> result = MAPPER.readValue
            ("{\"values\":[ {\"x\":3} ] } ", new TypeReference<BoundedWrapper<IntBean>>() {});
        List<?> list = result.values;
        assertEquals(1, list.size());
        Object ob = list.get(0);
        assertEquals(IntBean.class, ob.getClass());
        assertEquals(3, result.values.get(0).x);
    }

// com.fasterxml.jackson.databind.type.TestGenericsBounded::testGenericsComplex
    public void testGenericsComplex() throws Exception
    {
        DoubleRange in = new DoubleRange(-0.5, 0.5);
        String json = MAPPER.writeValueAsString(in);
        DoubleRange out = MAPPER.readValue(json, DoubleRange.class);
        assertNotNull(out);
        assertEquals(-0.5, out.start);
        assertEquals(0.5, out.end);
    }

// com.fasterxml.jackson.databind.type.TestGenericsBounded::testIssue778
    public void testIssue778() throws Exception
    {
        String json = "{\"rows\":[{\"d\":{}}]}";

        final TypeReference<?> typeRef = new TypeReference<ResultSetWithDoc<MyDoc>>() {};

        

        JavaType type = MAPPER.getTypeFactory().constructType(typeRef);
        JavaType resultSetType = type.findSuperType(ResultSet.class);
        assertNotNull(resultSetType);
        assertEquals(1, resultSetType.containedTypeCount());

        JavaType rowType = resultSetType.containedType(0);
        assertNotNull(rowType);
        assertEquals(RowWithDoc.class, rowType.getRawClass());
        
        assertEquals(1, rowType.containedTypeCount());
        JavaType docType = rowType.containedType(0);
        assertEquals(MyDoc.class, docType.getRawClass());

        
        ResultSetWithDoc<MyDoc> rs = MAPPER.readValue(json, type);
        Document d = rs.rows.iterator().next().d;
    
        assertEquals(MyDoc.class, d.getClass()); 
    }

// com.fasterxml.jackson.databind.type.TestGenericsBounded::test
    public void test() throws Exception
    {
        AnnotatedValueSimple<Integer> item = new AnnotatedValueSimple<Integer>(5);
        CbFailing<AnnotatedValueSimple<Integer>, Integer> codebook = new CbFailing<AnnotatedValueSimple<Integer>, Integer>(item);
        String json = MAPPER.writeValueAsString(codebook);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testLocalType728
    public void testLocalType728() throws Exception
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        Method m = Issue728.class.getMethod("method", CharSequence.class);
        assertNotNull(m);

        
        
        JavaType t = tf.constructType(m.getReturnType());
        assertEquals(CharSequence.class, t.getRawClass());
        
        t = tf.constructType(m.getGenericReturnType());
        assertEquals(CharSequence.class, t.getRawClass());

        
        t = tf.constructType(m.getParameterTypes()[0]);
        assertEquals(CharSequence.class, t.getRawClass());
        t = tf.constructType(m.getGenericParameterTypes()[0]);
        assertEquals(CharSequence.class, t.getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testSimpleClass
    public void testSimpleClass()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(BaseType.class);
        assertSame(BaseType.class, baseType.getRawClass());
        assertTrue(baseType.hasRawClass(BaseType.class));
        assertFalse(baseType.isTypeOrSubTypeOf(SubType.class));

        assertFalse(baseType.isArrayType());
        assertFalse(baseType.isContainerType());
        assertFalse(baseType.isEnumType());
        assertFalse(baseType.isInterface());
        assertFalse(baseType.isPrimitive());
        assertFalse(baseType.isReferenceType());
        assertFalse(baseType.hasContentType());

        assertNull(baseType.getContentType());
        assertNull(baseType.getKeyType());
        assertNull(baseType.getValueHandler());

        assertEquals("Lcom/fasterxml/jackson/databind/type/TestJavaType$BaseType;", baseType.getGenericSignature());
        assertEquals("Lcom/fasterxml/jackson/databind/type/TestJavaType$BaseType;", baseType.getErasedSignature());
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testDeprecated
    public void testDeprecated()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(BaseType.class);
        assertTrue(baseType.hasRawClass(BaseType.class));
        assertNull(baseType.getParameterSource());
        assertNull(baseType.getContentTypeHandler());
        assertNull(baseType.getContentValueHandler());
        assertFalse(baseType.hasValueHandler());
        assertFalse(baseType.hasHandlers());

        assertSame(baseType, baseType.forcedNarrowBy(BaseType.class));
        JavaType sub = baseType.forcedNarrowBy(SubType.class);
        assertTrue(sub.hasRawClass(SubType.class));
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testArrayType
    public void testArrayType()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType arrayT = ArrayType.construct(tf.constructType(String.class), null);
        assertNotNull(arrayT);
        assertTrue(arrayT.isContainerType());
        assertFalse(arrayT.isReferenceType());
        assertTrue(arrayT.hasContentType());

        assertNotNull(arrayT.toString());
        assertNotNull(arrayT.getContentType());
        assertNull(arrayT.getKeyType());

        assertTrue(arrayT.equals(arrayT));
        assertFalse(arrayT.equals(null));
        assertFalse(arrayT.equals("xyz"));

        assertTrue(arrayT.equals(ArrayType.construct(tf.constructType(String.class), null)));
        assertFalse(arrayT.equals(ArrayType.construct(tf.constructType(Integer.class), null)));
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testMapType
    public void testMapType()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType mapT = tf.constructType(HashMap.class);
        assertTrue(mapT.isContainerType());
        assertFalse(mapT.isReferenceType());
        assertTrue(mapT.hasContentType());

        assertNotNull(mapT.toString());
        assertNotNull(mapT.getContentType());
        assertNotNull(mapT.getKeyType());

        assertEquals("Ljava/util/HashMap<Ljava/lang/Object;Ljava/lang/Object;>;", mapT.getGenericSignature());
        assertEquals("Ljava/util/HashMap;", mapT.getErasedSignature());
        
        assertTrue(mapT.equals(mapT));
        assertFalse(mapT.equals(null));
        assertFalse(mapT.equals("xyz"));
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testEnumType
    public void testEnumType()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType enumT = tf.constructType(MyEnum.class);
        assertTrue(enumT.isEnumType());
        assertFalse(enumT.hasHandlers());
        assertTrue(enumT.isTypeOrSubTypeOf(MyEnum.class));
        assertTrue(enumT.isTypeOrSubTypeOf(Object.class));
        assertNull(enumT.containedType(3));
        assertTrue(enumT.containedTypeOrUnknown(3).isJavaLangObject());

        assertEquals("Lcom/fasterxml/jackson/databind/type/TestJavaType$MyEnum;", enumT.getGenericSignature());
        assertEquals("Lcom/fasterxml/jackson/databind/type/TestJavaType$MyEnum;", enumT.getErasedSignature());

        assertTrue(tf.constructType(MyEnum2.class).isEnumType());
        assertTrue(tf.constructType(MyEnum.A.getClass()).isEnumType());
        assertTrue(tf.constructType(MyEnum2.A.getClass()).isEnumType());
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testClassKey
    public void testClassKey()
    {
        ClassKey key = new ClassKey(String.class);
        assertEquals(0, key.compareTo(key));
        assertTrue(key.equals(key));
        assertFalse(key.equals(null));
        assertFalse(key.equals("foo"));
        assertFalse(key.equals(new ClassKey(Integer.class)));
        assertEquals(String.class.getName(), key.toString());
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testJavaTypeAsJLRType
    public void testJavaTypeAsJLRType()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t1 = tf.constructType(getClass());
        
        JavaType t2 = tf.constructType(t1);
        assertSame(t1, t2);
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testGenericSignature1194
    public void testGenericSignature1194() throws Exception
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        Method m;
        JavaType t;

        m = Generic1194.class.getMethod("getList");
        t  = tf.constructType(m.getGenericReturnType());
        assertEquals("Ljava/util/List<Ljava/lang/String;>;", t.getGenericSignature());
        assertEquals("Ljava/util/List;", t.getErasedSignature());
        
        m = Generic1194.class.getMethod("getMap");
        t  = tf.constructType(m.getGenericReturnType());
        assertEquals("Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;",
                t.getGenericSignature());

        m = Generic1194.class.getMethod("getGeneric");
        t  = tf.constructType(m.getGenericReturnType());
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", t.getGenericSignature());
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testAnchorTypeForRefTypes
    public void testAnchorTypeForRefTypes() throws Exception
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t  = tf.constructType(AtomicStringReference.class);
        assertTrue(t.isReferenceType());
        assertTrue(t.hasContentType());
        ReferenceType rt = (ReferenceType) t;
        assertFalse(rt.isAnchorType());
        assertEquals(AtomicReference.class, rt.getAnchorType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestJavaType::testObjectToReferenceSpecialization
    public void testObjectToReferenceSpecialization() throws Exception
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType base = tf.constructType(Object.class);
        assertTrue(base.isJavaLangObject());

        JavaType sub = tf.constructSpecializedType(base, AtomicReference.class);
        assertEquals(AtomicReference.class, sub.getRawClass());
        assertTrue(sub.isReferenceType());
    }

// com.fasterxml.jackson.databind.type.TestTypeBindings::testInnerType
    public void testInnerType() throws Exception
    {
        JavaType type = DEFAULT_TF.constructType(InnerGenericTyping.InnerClass.class);
        assertEquals(MapType.class, type.getClass());
        JavaType keyType = type.getKeyType();
        assertEquals(Object.class, keyType.getRawClass());
        JavaType valueType = type.getContentType();
        assertEquals(Collection.class, valueType.getRawClass());
        JavaType vt2 = valueType.getContentType();
        assertEquals(Object.class, vt2.getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeBindings::testRecursiveType
    public void testRecursiveType()
    {
        JavaType type = DEFAULT_TF.constructType(HashTree.class);
        assertNotNull(type);
    }

// com.fasterxml.jackson.databind.type.TestTypeBindings::testBindingsBasics
    public void testBindingsBasics()
    {
        TypeBindings b = TypeBindings.create(Collection.class,
                TypeFactory.unknownType());
        
        assertNotNull(b.toString());
        assertEquals(Object.class, b.getBoundType(0).getRawClass());
        assertNull(b.getBoundName(-1));
        assertNull(b.getBoundType(-1));
        assertNull(b.getBoundName(1));
        assertNull(b.getBoundType(1));

        assertFalse(b.equals("foo"));
    }

// com.fasterxml.jackson.databind.type.TestTypeBindings::testInvalidBindings
    public void testInvalidBindings()
    {
        JavaType unknown = TypeFactory.unknownType();
        try {
            TypeBindings.create(AbstractType.class, unknown);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot create TypeBindings");
            verifyException(e, "class expects 2");
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSimpleTypes
    public void testSimpleTypes()
    {
        Class<?>[] classes = new Class<?>[] {
            boolean.class, byte.class, char.class,
                short.class, int.class, long.class,
                float.class, double.class,

            Boolean.class, Byte.class, Character.class,
                Short.class, Integer.class, Long.class,
                Float.class, Double.class,

                String.class,
                Object.class,

                Calendar.class,
                Date.class,
        };

        TypeFactory tf = TypeFactory.defaultInstance();
        for (Class<?> clz : classes) {
            assertSame(clz, tf.constructType(clz).getRawClass());
            assertSame(clz, tf.constructType(clz).getRawClass());
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testArrays
    public void testArrays()
    {
        Class<?>[] classes = new Class<?>[] {
            boolean[].class, byte[].class, char[].class,
                short[].class, int[].class, long[].class,
                float[].class, double[].class,

                String[].class, Object[].class,
                Calendar[].class,
        };

        TypeFactory tf = TypeFactory.defaultInstance();
        for (Class<?> clz : classes) {
            assertSame(clz, tf.constructType(clz).getRawClass());
            Class<?> elemType = clz.getComponentType();
            assertSame(clz, tf.constructArrayType(elemType).getRawClass());
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testProperties
    public void testProperties()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(Properties.class);
        assertEquals(MapType.class, t.getClass());
        assertSame(Properties.class, t.getRawClass());

        MapType mt = (MapType) t;

        
        assertSame(String.class, mt.getKeyType().getRawClass());
        assertSame(String.class, mt.getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testIterator
    public void testIterator()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(new TypeReference<Iterator<String>>() { });
        assertEquals(SimpleType.class, t.getClass());
        assertSame(Iterator.class, t.getRawClass());
        assertEquals(1, t.containedTypeCount());
        assertEquals(tf.constructType(String.class), t.containedType(0));
        assertNull(t.containedType(1));
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testParametricTypes
    public void testParametricTypes()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        
        JavaType t = tf.constructParametrizedType(ArrayList.class, Collection.class, String.class); 
        assertEquals(CollectionType.class, t.getClass());
        JavaType strC = tf.constructType(String.class);
        assertEquals(1, t.containedTypeCount());
        assertEquals(strC, t.containedType(0));
        assertNull(t.containedType(1));

        
        JavaType t2 = tf.constructParametrizedType(Map.class, Map.class, strC, t); 
        
        assertEquals(MapType.class, t2.getClass());
        assertEquals(2, t2.containedTypeCount());
        assertEquals(strC, t2.containedType(0));
        assertEquals(t, t2.containedType(1));
        assertNull(t2.containedType(2));

        
        JavaType custom = tf.constructParametrizedType(SingleArgGeneric.class, SingleArgGeneric.class,
                String.class);
        assertEquals(SimpleType.class, custom.getClass());
        assertEquals(1, custom.containedTypeCount());
        assertEquals(strC, custom.containedType(0));
        assertNull(custom.containedType(1));

        
        assertEquals("X", custom.containedTypeName(0));

        
        try {
            
            tf.constructParametrizedType(Map.class, Map.class, strC);
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot create TypeBindings for class java.util.Map");
        }

        try {
            
            tf.constructParametrizedType(SingleArgGeneric.class, SingleArgGeneric.class, strC, strC);
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot create TypeBindings for class ");
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCanonicalNames
    public void testCanonicalNames()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(java.util.Calendar.class);
        String can = t.toCanonical();
        assertEquals("java.util.Calendar", can);
        assertEquals(t, tf.constructFromCanonical(can));

        
        t = tf.constructType(java.util.ArrayList.class);
        can = t.toCanonical();
        assertEquals("java.util.ArrayList<java.lang.Object>", can);
        assertEquals(t, tf.constructFromCanonical(can));

        t = tf.constructType(java.util.TreeMap.class);
        can = t.toCanonical();
        assertEquals("java.util.TreeMap<java.lang.Object,java.lang.Object>", can);
        assertEquals(t, tf.constructFromCanonical(can));

        
        t = tf.constructMapType(EnumMap.class, EnumForCanonical.class, String.class);
        can = t.toCanonical();
        assertEquals("java.util.EnumMap<com.fasterxml.jackson.databind.type.TestTypeFactory$EnumForCanonical,java.lang.String>",
                can);
        assertEquals(t, tf.constructFromCanonical(can));

        
        t = tf.constructType(new TypeReference<AtomicReference<Long>>() { });
        can = t.toCanonical();
        assertEquals("java.util.concurrent.atomic.AtomicReference<java.lang.Long>",
                can);
        assertEquals(t, tf.constructFromCanonical(can));

        
        t = tf.constructFromCanonical("java.util.List");
        assertEquals(List.class, t.getRawClass());
        assertEquals(CollectionType.class, t.getClass());
        
        
        
        assertEquals(Object.class, t.getContentType().getRawClass());
        can = t.toCanonical();
        assertEquals("java.util.List<java.lang.Object>", can);
        assertEquals(t, tf.constructFromCanonical(can));
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCanonicalWithSpaces
    public void testCanonicalWithSpaces()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        Object objects = new TreeMap<Object, Object>() { }; 
        String reflectTypeName = objects.getClass().getGenericSuperclass().toString();
        JavaType t1 = tf.constructType(objects.getClass().getGenericSuperclass());
        
        JavaType t2 = tf.constructFromCanonical(reflectTypeName);
        assertNotNull(t2);
        assertEquals(t2, t1);
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCollections
    public void testCollections()
    {
        
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(ArrayList.class);
        assertEquals(CollectionType.class, t.getClass());
        assertSame(ArrayList.class, t.getRawClass());

        
        t = tf.constructType(new TypeReference<ArrayList<String>>() { });
        assertEquals(CollectionType.class, t.getClass());
        assertSame(ArrayList.class, t.getRawClass());

        JavaType elemType = ((CollectionType) t).getContentType();
        assertNotNull(elemType);
        assertSame(SimpleType.class, elemType.getClass());
        assertSame(String.class, elemType.getRawClass());

        
        t = tf.constructCollectionType(ArrayList.class, String.class);
        assertEquals(CollectionType.class, t.getClass());
        assertSame(String.class, ((CollectionType) t).getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCollectionTypesRefined
    public void testCollectionTypesRefined()
    {
        TypeFactory tf = newTypeFactory();
        JavaType type = tf.constructType(new TypeReference<List<Long>>() { });
        assertEquals(List.class, type.getRawClass());
        assertEquals(Long.class, type.getContentType().getRawClass());
        
        assertNull(type.getSuperClass());

        
        JavaType subtype = tf.constructSpecializedType(type, ArrayList.class);
        assertEquals(ArrayList.class, subtype.getRawClass());
        assertEquals(Long.class, subtype.getContentType().getRawClass());

        
        JavaType superType = subtype.getSuperClass();
        assertNotNull(superType);
        assertEquals(AbstractList.class, superType.getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMaps
    public void testMaps()
    {
        TypeFactory tf = newTypeFactory();

        
        JavaType t = tf.constructType(HashMap.class);
        assertEquals(MapType.class, t.getClass());
        assertSame(HashMap.class, t.getRawClass());

        
        t = tf.constructMapType(TreeMap.class, String.class, Integer.class);
        assertEquals(MapType.class, t.getClass());
        assertSame(String.class, ((MapType) t).getKeyType().getRawClass());
        assertSame(Integer.class, ((MapType) t).getContentType().getRawClass());

        
        t = tf.constructType(new TypeReference<HashMap<String,Integer>>() { });
        assertEquals(MapType.class, t.getClass());
        assertSame(HashMap.class, t.getRawClass());
        MapType mt = (MapType) t;
        assertEquals(tf.constructType(String.class), mt.getKeyType());
        assertEquals(tf.constructType(Integer.class), mt.getContentType());

        t = tf.constructType(new TypeReference<LongValuedMap<Boolean>>() { });
        assertEquals(MapType.class, t.getClass());
        assertSame(LongValuedMap.class, t.getRawClass());
        mt = (MapType) t;
        assertEquals(tf.constructType(Boolean.class), mt.getKeyType());
        assertEquals(tf.constructType(Long.class), mt.getContentType());

        JavaType type = tf.constructType(new TypeReference<Map<String,Boolean>>() { });
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(String.class), mapType.getKeyType());
        assertEquals(tf.constructType(Boolean.class), mapType.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapTypesRefined
    public void testMapTypesRefined()
    {
        TypeFactory tf = newTypeFactory();
        JavaType type = tf.constructType(new TypeReference<Map<String,List<Integer>>>() { });
        MapType mapType = (MapType) type;
        assertEquals(Map.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(List.class, mapType.getContentType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getContentType().getRawClass());
        
        assertNull(type.getSuperClass());
        
        
        JavaType subtype = tf.constructSpecializedType(type, LinkedHashMap.class);
        assertEquals(LinkedHashMap.class, subtype.getRawClass());
        assertEquals(String.class, subtype.getKeyType().getRawClass());
        assertEquals(List.class, subtype.getContentType().getRawClass());
        assertEquals(Integer.class, subtype.getContentType().getContentType().getRawClass());

        

        JavaType superType = subtype.getSuperClass();
        assertNotNull(superType);
        assertEquals(HashMap.class, superType.getRawClass());
        
        assertEquals(String.class, superType.getKeyType().getRawClass());
        assertEquals(List.class, superType.getContentType().getRawClass());
        assertEquals(Integer.class, superType.getContentType().getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testTypeGeneralization
    public void testTypeGeneralization()
    {
        TypeFactory tf = newTypeFactory();
        MapType t = tf.constructMapType(HashMap.class, String.class, Long.class);
        JavaType superT = tf.constructGeneralizedType(t, Map.class);
        assertEquals(String.class, superT.getKeyType().getRawClass());
        assertEquals(Long.class, superT.getContentType().getRawClass());

        assertSame(t, tf.constructGeneralizedType(t, HashMap.class));

        
        try {
            tf.constructGeneralizedType(t, TreeMap.class);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "not a super-type of");
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapTypesRaw
    public void testMapTypesRaw()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(HashMap.class);
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(Object.class), mapType.getKeyType());
        assertEquals(tf.constructType(Object.class), mapType.getContentType());        
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapTypesAdvanced
    public void testMapTypesAdvanced()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(MyMap.class);
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(String.class), mapType.getKeyType());
        assertEquals(tf.constructType(Long.class), mapType.getContentType());
        
        type = tf.constructType(MapInterface.class);
        mapType = (MapType) type;

        assertEquals(tf.constructType(String.class), mapType.getKeyType());
        assertEquals(tf.constructType(Integer.class), mapType.getContentType());

        type = tf.constructType(MyStringIntMap.class);
        mapType = (MapType) type;
        assertEquals(tf.constructType(String.class), mapType.getKeyType());
        assertEquals(tf.constructType(Integer.class), mapType.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapTypesSneaky
    public void testMapTypesSneaky()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(IntLongMap.class);
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(Integer.class), mapType.getKeyType());
        assertEquals(tf.constructType(Long.class), mapType.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSneakyFieldTypes
    public void testSneakyFieldTypes() throws Exception
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        Field field = SneakyBean.class.getDeclaredField("intMap");
        JavaType type = tf.constructType(field.getGenericType());
        assertTrue(type instanceof MapType);
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(Integer.class), mapType.getKeyType());
        assertEquals(tf.constructType(Long.class), mapType.getContentType());

        field = SneakyBean.class.getDeclaredField("longList");
        type = tf.constructType(field.getGenericType());
        assertTrue(type instanceof CollectionType);
        CollectionType collectionType = (CollectionType) type;
        assertEquals(tf.constructType(Long.class), collectionType.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSneakyBeanProperties
    public void testSneakyBeanProperties() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        StringLongMapBean bean = mapper.readValue("{\"value\":{\"a\":123}}", StringLongMapBean.class);
        assertNotNull(bean);
        Map<String,Long> map = bean.value;
        assertEquals(1, map.size());
        assertEquals(Long.valueOf(123), map.get("a"));

        StringListBean bean2 = mapper.readValue("{\"value\":[\"...\"]}", StringListBean.class);
        assertNotNull(bean2);
        List<String> list = bean2.value;
        assertSame(GenericList.class, list.getClass());
        assertEquals(1, list.size());
        assertEquals("...", list.get(0));
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSneakySelfRefs
    public void testSneakySelfRefs() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new SneakyBean2());
        assertEquals("{\"foobar\":null}", json);
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testAtomicArrayRefParameters
    public void testAtomicArrayRefParameters()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<AtomicReference<long[]>>() { });
        JavaType[] params = tf.findTypeParameters(type, AtomicReference.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(tf.constructType(long[].class), params[0]);
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapEntryResolution
    public void testMapEntryResolution()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(StringIntMapEntry.class);
        JavaType mapEntryType = t.findSuperType(Map.Entry.class);
        assertNotNull(mapEntryType);
        assertTrue(mapEntryType.hasGenericTypes());
        assertEquals(2, mapEntryType.containedTypeCount());
        assertEquals(String.class, mapEntryType.containedType(0).getRawClass());
        assertEquals(Integer.class, mapEntryType.containedType(1).getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testRawCollections
    public void testRawCollections()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructRawCollectionType(ArrayList.class);
        assertTrue(type.isContainerType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());
        type = tf.constructRawCollectionLikeType(CollectionLike.class); 
        assertTrue(type.isCollectionLikeType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());

        
        type = tf.constructRawCollectionLikeType(String.class);
        assertTrue(type.isCollectionLikeType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testRawMaps
    public void testRawMaps()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructRawMapType(HashMap.class);
        assertTrue(type.isContainerType());
        assertEquals(TypeFactory.unknownType(), type.getKeyType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());

        type = tf.constructRawMapLikeType(MapLike.class); 
        assertTrue(type.isMapLikeType());
        assertEquals(TypeFactory.unknownType(), type.getKeyType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());

        
        type = tf.constructRawMapLikeType(String.class);
        assertTrue(type.isMapLikeType());
        assertEquals(TypeFactory.unknownType(), type.getKeyType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMoreSpecificType
    public void testMoreSpecificType()
    {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType t1 = tf.constructCollectionType(Collection.class, Object.class);
        JavaType t2 = tf.constructCollectionType(List.class, Object.class);
        assertSame(t2, tf.moreSpecificType(t1, t2));
        assertSame(t2, tf.moreSpecificType(t2, t1));

        t1 = tf.constructType(Double.class);
        t2 = tf.constructType(Number.class);
        assertSame(t1, tf.moreSpecificType(t1, t2));
        assertSame(t1, tf.moreSpecificType(t2, t1));

        
        t1 = tf.constructType(Double.class);
        t2 = tf.constructType(String.class);
        assertSame(t1, tf.moreSpecificType(t1, t2));
        assertSame(t2, tf.moreSpecificType(t2, t1));
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCacheClearing
    public void testCacheClearing()
    {
        TypeFactory tf = TypeFactory.defaultInstance().withModifier(null);
        assertEquals(0, tf._typeCache.size());
        tf.constructType(getClass());
        
        assertEquals(6, tf._typeCache.size());
        tf.clearCache();
        assertEquals(0, tf._typeCache.size());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testRawMapType
    public void testRawMapType()
    {
        TypeFactory tf = TypeFactory.defaultInstance().withModifier(null); 

        JavaType type = tf.constructParametricType(Wrapper1297.class, Map.class);
        assertNotNull(type);
        assertEquals(Wrapper1297.class, type.getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory1604::testCustomTypesRefinedSimple
    public void testCustomTypesRefinedSimple()
    {
        TypeFactory tf = newTypeFactory();
        JavaType base = tf.constructType(new TypeReference<Data1604<List<Long>>>() { });
        assertEquals(Data1604.class, base.getRawClass());
        assertEquals(1, base.containedTypeCount());
        assertEquals(List.class, base.containedType(0).getRawClass());

        JavaType subtype = tf.constructSpecializedType(base, DataList1604.class);
        assertEquals(DataList1604.class, subtype.getRawClass());
        assertEquals(1, subtype.containedTypeCount());
        JavaType paramType = subtype.containedType(0);
        assertEquals(Long.class, paramType.getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory1604::testCustomTypesRefinedNested
    public void testCustomTypesRefinedNested()
    {
        TypeFactory tf = newTypeFactory();
        JavaType base = tf.constructType(new TypeReference<Data1604<List<Long>>>() { });
        assertEquals(Data1604.class, base.getRawClass());

        JavaType subtype = tf.constructSpecializedType(base, RefinedDataList1604.class);
        assertEquals(RefinedDataList1604.class, subtype.getRawClass());
        assertEquals(DataList1604.class, subtype.getSuperClass().getRawClass());

        assertEquals(1, subtype.containedTypeCount());
        JavaType paramType = subtype.containedType(0);
        assertEquals(Long.class, paramType.getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory1604::testCustomTypesRefinedSneaky
    public void testCustomTypesRefinedSneaky()
    {
        TypeFactory tf = newTypeFactory();
        JavaType base = tf.constructType(new TypeReference<Data1604<List<Long>>>() { });
        assertEquals(Data1604.class, base.getRawClass());

        JavaType subtype = tf.constructSpecializedType(base, SneakyDataList1604.class);
        assertEquals(SneakyDataList1604.class, subtype.getRawClass());
        assertEquals(2, subtype.containedTypeCount());
        assertEquals(Long.class, subtype.containedType(1).getRawClass());
        
        assertEquals(Object.class, subtype.containedType(0).getRawClass());

        
        assertEquals(Data1604.class, subtype.getSuperClass().getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory1604::testTwoParamSneakyCustom
    public void testTwoParamSneakyCustom()
    {
        TypeFactory tf = newTypeFactory();
        JavaType type = tf.constructType(new TypeReference<TwoParam1604<String,List<Long>>>() { });
        assertEquals(TwoParam1604.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        JavaType ct = type.containedType(1);
        assertEquals(List.class, ct.getRawClass());
        assertEquals(Long.class, ct.getContentType().getRawClass());

        JavaType subtype = tf.constructSpecializedType(type, SneakyTwoParam1604.class);
        assertEquals(SneakyTwoParam1604.class, subtype.getRawClass());
        assertEquals(TwoParam1604.class, subtype.getSuperClass().getRawClass());
        assertEquals(2, subtype.containedTypeCount());

        
        
        JavaType first = subtype.containedType(0);
        assertEquals(Long.class, first.getRawClass());
        JavaType second = subtype.containedType(1);
        assertEquals(String.class, second.getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory1604::testErrorForMismatch
    public void testErrorForMismatch()
    {
        TypeFactory tf = newTypeFactory();
        
        JavaType base = tf.constructType(new TypeReference<Data1604<String>>() { });

        try {
            tf.constructSpecializedType(base, DataList1604.class);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Failed to specialize");
            verifyException(e, "Data1604");
            verifyException(e, "DataList1604");
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testUsesCorrectClassLoaderWhenThreadClassLoaderIsNull
  public void testUsesCorrectClassLoaderWhenThreadClassLoaderIsNull() throws ClassNotFoundException {
	Thread.currentThread().setContextClassLoader(null);
	TypeFactory spySut = spy(mapper.getTypeFactory().withModifier(typeModifier).withClassLoader(classLoader));
	Class<?> clazz = spySut.findClass(aClassName);
	verify(spySut).getClassLoader();
	verify(spySut).classForName(any(String.class), any(Boolean.class), eq(classLoader));
	Assert.assertNotNull(clazz);
	Assert.assertEquals(classLoader, spySut.getClassLoader());
	Assert.assertEquals(typeModifier,spySut._modifiers[0]);
	Assert.assertEquals(null, Thread.currentThread().getContextClassLoader());
  }

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testUsesCorrectClassLoaderWhenThreadClassLoaderIsNotNull
public void testUsesCorrectClassLoaderWhenThreadClassLoaderIsNotNull() throws ClassNotFoundException {
	TypeFactory spySut = spy(mapper.getTypeFactory().withModifier(typeModifier).withClassLoader(classLoader));
	Class<?> clazz = spySut.findClass(aClassName);
	verify(spySut).getClassLoader();
	verify(spySut).classForName(any(String.class), any(Boolean.class), eq(classLoader));
	Assert.assertNotNull(clazz);
	Assert.assertEquals(classLoader, spySut.getClassLoader());
	Assert.assertEquals(typeModifier,spySut._modifiers[0]);
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testCallingOnlyWithModifierGivesExpectedResults
public void testCallingOnlyWithModifierGivesExpectedResults(){
	TypeFactory sut = mapper.getTypeFactory().withModifier(typeModifier);
	Assert.assertNull(sut.getClassLoader());
	Assert.assertEquals(typeModifier,sut._modifiers[0]);
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testCallingOnlyWithClassLoaderGivesExpectedResults
public void testCallingOnlyWithClassLoaderGivesExpectedResults(){
	TypeFactory sut = mapper.getTypeFactory().withClassLoader(classLoader);
	Assert.assertNotNull(sut.getClassLoader());
	Assert.assertArrayEquals(null,sut._modifiers);
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testDefaultTypeFactoryNotAffectedByWithConstructors
public void testDefaultTypeFactoryNotAffectedByWithConstructors() {
	TypeFactory sut = mapper.getTypeFactory().withModifier(typeModifier).withClassLoader(classLoader);
	Assert.assertEquals(classLoader, sut.getClassLoader());
	Assert.assertEquals(typeModifier,sut._modifiers[0]);
	Assert.assertNull(mapper.getTypeFactory().getClassLoader());
	Assert.assertArrayEquals(null,mapper.getTypeFactory()._modifiers);
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testSetsTheCorrectClassLoderIfUsingWithModifierFollowedByWithClassLoader
public void testSetsTheCorrectClassLoderIfUsingWithModifierFollowedByWithClassLoader() {
	TypeFactory sut = mapper.getTypeFactory().withModifier(typeModifier).withClassLoader(classLoader);
	Assert.assertNotNull(sut.getClassLoader());
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testSetsTheCorrectClassLoderIfUsingWithClassLoaderFollowedByWithModifier
public void testSetsTheCorrectClassLoderIfUsingWithClassLoaderFollowedByWithModifier() {
	TypeFactory sut = mapper.getTypeFactory().withClassLoader(classLoader).withModifier(typeModifier);
	Assert.assertNotNull(sut.getClassLoader());
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testThreadContextClassLoaderIsUsedIfNotUsingWithClassLoader
public void testThreadContextClassLoaderIsUsedIfNotUsingWithClassLoader() throws ClassNotFoundException {
	TypeFactory spySut = spy(mapper.getTypeFactory());
	Assert.assertNull(spySut.getClassLoader());
	Class<?> clazz = spySut.findClass(aClassName);
	Assert.assertNotNull(clazz);
	verify(spySut).classForName(any(String.class), any(Boolean.class), eq(threadClassLoader));
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testUsesFallBackClassLoaderIfNoThreadClassLoaderAndNoWithClassLoader
public void testUsesFallBackClassLoaderIfNoThreadClassLoaderAndNoWithClassLoader() throws ClassNotFoundException {
	Thread.currentThread().setContextClassLoader(null);
	TypeFactory spySut = spy(mapper.getTypeFactory());
	Assert.assertNull(spySut.getClassLoader());
	Assert.assertArrayEquals(null,spySut._modifiers);
	Class<?> clazz = spySut.findClass(aClassName);
	Assert.assertNotNull(clazz);
	verify(spySut).classForName(any(String.class));
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithRecursiveTypes::testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedAfterBaseType
    public void testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedAfterBaseType() throws IOException {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructType(Base.class);
        tf.constructType(Sub.class);
        Sub sub = new Sub();
        String serialized = objectMapper().writeValueAsString(sub);
        assertEquals("{\"base\":1,\"sub\":2}", serialized);
    }

// com.fasterxml.jackson.databind.type.TestTypeResolution::testMaps
    public void testMaps()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(new TypeReference<LongValuedMap<String>>() { });
        MapType type = (MapType) t;
        assertSame(LongValuedMap.class, type.getRawClass());
        assertEquals(tf.constructType(String.class), type.getKeyType());
        assertEquals(tf.constructType(Long.class), type.getContentType());        
    }

// com.fasterxml.jackson.databind.type.TestTypeResolution::testListViaTypeRef
    public void testListViaTypeRef()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(new TypeReference<MyLongList<Integer>>() {});
        CollectionType type = (CollectionType) t;
        assertSame(MyLongList.class, type.getRawClass());
        assertEquals(tf.constructType(Long.class), type.getContentType());        
    }

// com.fasterxml.jackson.databind.type.TestTypeResolution::testListViaClass
    public void testListViaClass()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(LongList.class);
        JavaType type = (CollectionType) t;
        assertSame(LongList.class, type.getRawClass());
        assertEquals(tf.constructType(Long.class), type.getContentType());        
    }

// com.fasterxml.jackson.databind.type.TestTypeResolution::testGeneric
    public void testGeneric()
    {
        TypeFactory tf = TypeFactory.defaultInstance();

        
        JavaType t = tf.constructType(DoubleRange.class);
        JavaType rangeParams = t.findSuperType(Range.class);
        assertEquals(1, rangeParams.containedTypeCount());
        assertEquals(Double.class, rangeParams.containedType(0).getRawClass());

        
        t = tf.constructType(new TypeReference<DoubleRange>() { });
        rangeParams = t.findSuperType(Range.class);
        assertEquals(1, rangeParams.containedTypeCount());
        assertEquals(Double.class, rangeParams.containedType(0).getRawClass());
    }

// com.fasterxml.jackson.databind.type.TypeAliasesTest::testAliasResolutionIssue743
    public void testAliasResolutionIssue743() throws Exception
    {
        String s3 = "{\"dataObj\" : [ \"one\", \"two\", \"three\" ] }";
        ObjectMapper m = new ObjectMapper();
   
        Child.ChildData d = m.readValue(s3, Child.ChildData.class);
        assertNotNull(d.dataObj);
        assertEquals(3, d.dataObj.size());
    }

// com.fasterxml.jackson.databind.util.ArrayBuildersTest::testInsertInListNoDup
	public void testInsertInListNoDup()
	{
        String [] arr = new String[]{"me", "you", "him"};
        String [] newarr;
        
        newarr = ArrayBuilders.insertInListNoDup(arr, "you");
        Assert.assertArrayEquals(new String[]{"you", "me", "him"}, newarr);

        newarr = ArrayBuilders.insertInListNoDup(arr, "me");
        Assert.assertArrayEquals(new String[]{"me", "you","him"}, newarr);

        newarr = ArrayBuilders.insertInListNoDup(arr, "him");
        Assert.assertArrayEquals(new String[]{"him", "me", "you"}, newarr);

        newarr = ArrayBuilders.insertInListNoDup(arr, "foobar");
        Assert.assertArrayEquals(new String[]{"foobar", "me", "you", "him"}, newarr);
	}

// com.fasterxml.jackson.databind.util.ArrayBuildersTest::testBuilderAccess
     public void testBuilderAccess()
     {
         ArrayBuilders builders = new ArrayBuilders();

         BooleanBuilder bb = builders.getBooleanBuilder();
         assertNotNull(bb);
         assertSame(bb, builders.getBooleanBuilder());

         ByteBuilder b2 = builders.getByteBuilder();
         assertNotNull(b2);
         assertSame(b2, builders.getByteBuilder());

         ShortBuilder sb = builders.getShortBuilder();
         assertNotNull(sb);
         assertSame(sb, builders.getShortBuilder());

         IntBuilder ib = builders.getIntBuilder();
         assertNotNull(ib);
         assertSame(ib, builders.getIntBuilder());

         LongBuilder lb = builders.getLongBuilder();
         assertNotNull(lb);
         assertSame(lb, builders.getLongBuilder());

         FloatBuilder fb = builders.getFloatBuilder();
         assertNotNull(fb);
         assertSame(fb, builders.getFloatBuilder());

         DoubleBuilder db = builders.getDoubleBuilder();
         assertNotNull(db);
         assertSame(db, builders.getDoubleBuilder());
     }

// com.fasterxml.jackson.databind.util.ArrayBuildersTest::testArrayComparator
     public void testArrayComparator()
     {
         final int[] INT3 = new int[] { 3, 4, 5 };
         Object comp = ArrayBuilders.getArrayComparator(INT3);
         assertFalse(comp.equals(null));
         assertTrue(comp.equals(INT3));
         assertTrue(comp.equals(new int[] { 3, 4, 5 }));
         assertFalse(comp.equals(new int[] { 5 }));
         assertFalse(comp.equals(new int[] { 3, 4 }));
         assertFalse(comp.equals(new int[] { 3, 5, 4 }));
         assertFalse(comp.equals(new int[] { 3, 4, 5, 6 }));
     }

// com.fasterxml.jackson.databind.util.ArrayBuildersTest::testArraySet
     public void testArraySet()
     {
         HashSet<String> set = ArrayBuilders.arrayToSet(new String[] { "foo", "bar" });
         assertEquals(2, set.size());
         assertEquals(new HashSet<String>(Arrays.asList("bar", "foo")), set);
     }

// com.fasterxml.jackson.databind.util.BeanUtilTest::testNameMangle
    public void testNameMangle()
    {
        assertEquals("foo", BeanUtil.legacyManglePropertyName("getFoo", 3));
        assertEquals("foo", BeanUtil.stdManglePropertyName("getFoo", 3));

        assertEquals("url", BeanUtil.legacyManglePropertyName("getURL", 3));
        assertEquals("URL", BeanUtil.stdManglePropertyName("getURL", 3));
    }

// com.fasterxml.jackson.databind.util.BeanUtilTest::testGetDefaultValue
    public void testGetDefaultValue()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        
        assertEquals(JsonInclude.Include.NON_EMPTY,
                BeanUtil.getDefaultValue(tf.constructType(Map.class)));
        assertEquals(JsonInclude.Include.NON_EMPTY,
                BeanUtil.getDefaultValue(tf.constructType(List.class)));
        assertEquals(JsonInclude.Include.NON_EMPTY,
                BeanUtil.getDefaultValue(tf.constructType(Object[].class)));
        
        assertEquals(JsonInclude.Include.NON_EMPTY,
                BeanUtil.getDefaultValue(tf.constructType(AtomicReference.class)));
        assertEquals("",
                BeanUtil.getDefaultValue(tf.constructType(String.class)));
        
        assertEquals(Integer.valueOf(0),
                BeanUtil.getDefaultValue(tf.constructType(Integer.class)));
        

        
        assertNull(BeanUtil.getDefaultValue(tf.constructType(getClass())));
    }

// com.fasterxml.jackson.databind.util.BeanUtilTest::testIsGetter
    public void testIsGetter() throws Exception
    {
        _testIsGetter("isPrimitive", "primitive");
        _testIsGetter("isWrapper", "wrapper");
        _testIsGetter("isNotGetter", null);
        _testIsGetter("is", null);
    }

// com.fasterxml.jackson.databind.util.BeanUtilTest::testOkNameForGetter
    public void testOkNameForGetter() throws Exception
    {
        
        _testOkNameForGetter("getCallbacks", "callbacks");
        _testOkNameForGetter("getMetaClass", "metaClass");
        _testOkNameForGetter("get", null);
    }

// com.fasterxml.jackson.databind.util.BeanUtilTest::testOkNameForSetter
    public void testOkNameForSetter() throws Exception
    {
        _testOkNameForSetter("setFoo", "foo");
        _testOkNameForSetter("notSetter", null);
        _testOkNameForSetter("set", null);
    }

// com.fasterxml.jackson.databind.util.ByteBufferUtilsTest::testByteBufferInput
    public void testByteBufferInput() throws Exception {
        byte[] input = new byte[] { 1, 2, 3 };
        ByteBufferBackedInputStream wrapped = new ByteBufferBackedInputStream(ByteBuffer.wrap(input));
        assertEquals(3, wrapped.available());
        assertEquals(1, wrapped.read());
        byte[] buffer = new byte[10];
        assertEquals(2, wrapped.read(buffer, 0, 5));
        wrapped.close();
    }

// com.fasterxml.jackson.databind.util.ByteBufferUtilsTest::testByteBufferOutput
    public void testByteBufferOutput() throws Exception {
        ByteBuffer b = ByteBuffer.wrap(new byte[10]);
        ByteBufferBackedOutputStream wrappedOut = new ByteBufferBackedOutputStream(b);
        wrappedOut.write(1);
        wrappedOut.write(new byte[] { 2, 3 });
        assertEquals(3, b.position());
        assertEquals(7, b.remaining());
        wrappedOut.close();
    }

// com.fasterxml.jackson.databind.util.ClassUtilTest::testIsConcrete
    public void testIsConcrete() throws Exception
    {
        assertTrue(ClassUtil.isConcrete(getClass()));
        assertFalse(ClassUtil.isConcrete(BaseClass.class));
        assertFalse(ClassUtil.isConcrete(BaseInt.class));

        assertFalse(ClassUtil.isConcrete(ConcreteAndAbstract.class.getDeclaredMethod("a")));
        assertTrue(ClassUtil.isConcrete(ConcreteAndAbstract.class.getDeclaredMethod("c")));
    }

// com.fasterxml.jackson.databind.util.ClassUtilTest::testCanBeABeanType
    public void testCanBeABeanType()
    {
        assertEquals("annotation", ClassUtil.canBeABeanType(java.lang.annotation.Retention.class));
        assertEquals("array", ClassUtil.canBeABeanType(String[].class));
        assertEquals("enum", ClassUtil.canBeABeanType(TestEnum.class));
        assertEquals("primitive", ClassUtil.canBeABeanType(Integer.TYPE));
        assertNull(ClassUtil.canBeABeanType(Integer.class));

        assertEquals("non-static member class", ClassUtil.isLocalType(InnerNonStatic.class, false));
        assertNull(ClassUtil.isLocalType(Integer.class, false));
    }

// com.fasterxml.jackson.databind.util.ClassUtilTest::testExceptionHelpers
    public void testExceptionHelpers()
    {
        RuntimeException e = new RuntimeException("test");
        RuntimeException wrapper = new RuntimeException(e);

        assertSame(e, ClassUtil.getRootCause(wrapper));

        try {
            ClassUtil.throwAsIAE(e);
            fail("Shouldn't get this far");
        } catch (RuntimeException e2) {
            assertSame(e, e2);
        }

        Error err = new Error();
        try {
            ClassUtil.throwAsIAE(err);
            fail("Shouldn't get this far");
        } catch (Error errAct) {
            assertSame(err, errAct);
        }
        
        try {
            ClassUtil.unwrapAndThrowAsIAE(wrapper);
            fail("Shouldn't get this far");
        } catch (RuntimeException e2) {
            assertSame(e, e2);
        }
    }

// com.fasterxml.jackson.databind.util.ClassUtilTest::testFailedCreateInstance
    public void testFailedCreateInstance()
    {
        try {
            ClassUtil.createInstance(BaseClass.class, true);
        } catch (IllegalArgumentException e) {
            verifyException(e, "has no default");
        }

        try {
            
            ClassUtil.createInstance(Inner.class, false);
        } catch (IllegalArgumentException e) {
            verifyException(e, "is not accessible");
        }

        
        try {
            ClassUtil.createInstance(Inner.class, true);
        } catch (IllegalStateException e) {
            verifyException(e, "test");
        }
    }

// com.fasterxml.jackson.databind.util.ClassUtilTest::testPrimitiveDefaultValue
    public void testPrimitiveDefaultValue()
    {
        assertEquals(Integer.valueOf(0), ClassUtil.defaultValue(Integer.TYPE));
        assertEquals(Long.valueOf(0L), ClassUtil.defaultValue(Long.TYPE));
        assertEquals(Character.valueOf('\0'), ClassUtil.defaultValue(Character.TYPE));
        assertEquals(Short.valueOf((short) 0), ClassUtil.defaultValue(Short.TYPE));
        assertEquals(Byte.valueOf((byte) 0), ClassUtil.defaultValue(Byte.TYPE));

        assertEquals(Double.valueOf(0.0), ClassUtil.defaultValue(Double.TYPE));
        assertEquals(Float.valueOf(0.0f), ClassUtil.defaultValue(Float.TYPE));

        assertEquals(Boolean.FALSE, ClassUtil.defaultValue(Boolean.TYPE));
        
        try {
            ClassUtil.defaultValue(String.class);
        } catch (IllegalArgumentException e) {
            verifyException(e, "String is not a primitive type");
        }
    }

// com.fasterxml.jackson.databind.util.ClassUtilTest::testPrimitiveWrapperType
    public void testPrimitiveWrapperType()
    {
        assertEquals(Byte.class, ClassUtil.wrapperType(Byte.TYPE));
        assertEquals(Short.class, ClassUtil.wrapperType(Short.TYPE));
        assertEquals(Character.class, ClassUtil.wrapperType(Character.TYPE));
        assertEquals(Integer.class, ClassUtil.wrapperType(Integer.TYPE));
        assertEquals(Long.class, ClassUtil.wrapperType(Long.TYPE));

        assertEquals(Double.class, ClassUtil.wrapperType(Double.TYPE));
        assertEquals(Float.class, ClassUtil.wrapperType(Float.TYPE));

        assertEquals(Boolean.class, ClassUtil.wrapperType(Boolean.TYPE));
        
        try {
            ClassUtil.wrapperType(String.class);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "String is not a primitive type");
        }
    }

// com.fasterxml.jackson.databind.util.ClassUtilTest::testWrapperToPrimitiveType
    public void testWrapperToPrimitiveType()
    {
        assertEquals(Integer.TYPE, ClassUtil.primitiveType(Integer.class));
        assertEquals(Long.TYPE, ClassUtil.primitiveType(Long.class));
        assertEquals(Character.TYPE, ClassUtil.primitiveType(Character.class));
        assertEquals(Short.TYPE, ClassUtil.primitiveType(Short.class));
        assertEquals(Byte.TYPE, ClassUtil.primitiveType(Byte.class));
        assertEquals(Float.TYPE, ClassUtil.primitiveType(Float.class));
        assertEquals(Double.TYPE, ClassUtil.primitiveType(Double.class));
        assertEquals(Boolean.TYPE, ClassUtil.primitiveType(Boolean.class));
        
        assertNull(ClassUtil.primitiveType(String.class));
    }

// com.fasterxml.jackson.databind.util.ClassUtilTest::testFindEnumType
    public void testFindEnumType()
    {
        assertEquals(TestEnum.class, ClassUtil.findEnumType(TestEnum.A));
        
        assertEquals(TestEnum.class, ClassUtil.findEnumType(EnumSet.allOf(TestEnum.class)));
        assertEquals(TestEnum.class, ClassUtil.findEnumType(EnumSet.noneOf(TestEnum.class)));

        assertEquals(TestEnum.class, ClassUtil.findEnumType(new EnumMap<TestEnum,Integer>(TestEnum.class)));
    }

// com.fasterxml.jackson.databind.util.ClassUtilTest::testDescs
    public void testDescs()
    {
        final String exp = "`java.lang.String`";
        assertEquals(exp, ClassUtil.getClassDescription("foo"));
        assertEquals(exp, ClassUtil.getClassDescription(String.class));
    }

// com.fasterxml.jackson.databind.util.CompactStringObjectMapTest::testBig
    public void testBig()
    {
        Map<String,String> all = new LinkedHashMap<>();
        for (int i = 0; i < 1000; ++i) {
            String key = "key"+i;
            all.put(key, key);
        }
        CompactStringObjectMap map = CompactStringObjectMap.construct(all);
        assertEquals(1000, map.keys().size());

        for (String key : all.keySet()) {
            assertEquals(key, map.find(key));
        }

        
        assertNull(map.find("key1000"));
        assertNull(map.find("keyXXX"));
        assertNull(map.find(""));
    }

// com.fasterxml.jackson.databind.util.EnumValuesTest::testConstructFromName
    public void testConstructFromName() {
        SerializationConfig cfg = MAPPER.getSerializationConfig()
                .without(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        Class<Enum<?>> enumClass = (Class<Enum<?>>)(Class<?>) ABC.class;
        EnumValues values = EnumValues.construct(cfg, enumClass);
        assertEquals("A", values.serializedValueFor(ABC.A).toString());
        assertEquals("B", values.serializedValueFor(ABC.B).toString());
        assertEquals("C", values.serializedValueFor(ABC.C).toString());
        assertEquals(3, values.values().size());
        assertEquals(3, values.internalMap().size());
    }

// com.fasterxml.jackson.databind.util.EnumValuesTest::testConstructWithToString
    public void testConstructWithToString() {
        SerializationConfig cfg = MAPPER.getSerializationConfig()
                .with(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        Class<Enum<?>> enumClass = (Class<Enum<?>>)(Class<?>) ABC.class;
        EnumValues values = EnumValues.construct(cfg, enumClass);
        assertEquals("A", values.serializedValueFor(ABC.A).toString());
        assertEquals("b", values.serializedValueFor(ABC.B).toString());
        assertEquals("C", values.serializedValueFor(ABC.C).toString());
        assertEquals(3, values.values().size());
        assertEquals(3, values.internalMap().size());
    }

// com.fasterxml.jackson.databind.util.EnumValuesTest::testEnumResolver
    public void testEnumResolver()
    {
        EnumResolver enumRes = EnumResolver.constructUnsafeUsingToString(ABC.class, null);
        assertEquals(ABC.B, enumRes.getEnum(1));
        assertNull(enumRes.getEnum(-1));
        assertNull(enumRes.getEnum(3));
        assertEquals(2, enumRes.lastValidIndex());
        List<Enum<?>> enums = enumRes.getEnums();
        assertEquals(3, enums.size());
        assertEquals(ABC.A, enums.get(0));
        assertEquals(ABC.B, enums.get(1));
        assertEquals(ABC.C, enums.get(2));
    }

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testFormat
    public void testFormat() {
        String result = df.format(date);
        assertEquals("2007-08-13T19:51:23Z", result);
    }

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testParse
    public void testParse() throws Exception {
        Date result = df.parse("2007-08-13T19:51:23Z");
        assertEquals(date, result);

        
        Date dateOnly = df.parse("2007-08-14");
        Calendar cal = new GregorianCalendar(2007, 8-1, 14);
        assertEquals(cal.getTime(), dateOnly);

        dateOnly = df.parse("2007-08-14Z");
        cal = new GregorianCalendar(2007, 8-1, 14);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(cal.getTime(), dateOnly);
    }

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testPartialParse
    public void testPartialParse() throws Exception {
        java.text.ParsePosition pos = new java.text.ParsePosition(0);
        String timestamp = "2007-08-13T19:51:23Z";
        Date result = df.parse(timestamp + "hello", pos);
        
        assertEquals(date, result);
        assertEquals(timestamp.length(), pos.getIndex());
    }

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testCloneObject
    public void testCloneObject() throws Exception {
        DateFormat clone = (DateFormat)df.clone();
        assertSame(df, clone);
    }

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testHashCodeEquals
    public void testHashCodeEquals() throws Exception {
        
        DateFormat defaultDF = StdDateFormat.instance;
        defaultDF.hashCode();
        assertTrue(defaultDF.equals(defaultDF));
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testFormat
    public void testFormat() {
        String result = ISO8601Utils.format(date);
        assertEquals("2007-08-13T19:51:23Z", result);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testFormatMillis
    public void testFormatMillis() {
        String result = ISO8601Utils.format(date, true);
        assertEquals("2007-08-13T19:51:23.789Z", result);

        result = ISO8601Utils.format(date, false);
        assertEquals("2007-08-13T19:51:23Z", result);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testFormatTimeZone
    public void testFormatTimeZone() {
        String result = ISO8601Utils.format(date, false, TimeZone.getTimeZone("GMT+02:00"));
        assertEquals("2007-08-13T21:51:23+02:00", result);
        result = ISO8601Utils.format(date, true, TimeZone.getTimeZone("GMT+02:00"));
        assertEquals("2007-08-13T21:51:23.789+02:00", result);
        result = ISO8601Utils.format(date, true, TimeZone.getTimeZone("GMT"));
        assertEquals("2007-08-13T19:51:23.789Z", result);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParse
    public void testParse() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("2007-08-13T19:51:23.789Z", new ParsePosition(0));
        assertEquals(date, d);

        d = ISO8601Utils.parse("2007-08-13T19:51:23Z", new ParsePosition(0));
        assertEquals(dateZeroMillis, d);

        d = ISO8601Utils.parse("2007-08-13T21:51:23.789+02:00", new ParsePosition(0));
        assertEquals(date, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseShortDate
    public void testParseShortDate() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("20070813T19:51:23.789Z", new ParsePosition(0));
        assertEquals(date, d);

        d = ISO8601Utils.parse("20070813T19:51:23Z", new ParsePosition(0));
        assertEquals(dateZeroMillis, d);

        d = ISO8601Utils.parse("20070813T21:51:23.789+02:00", new ParsePosition(0));
        assertEquals(date, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseShortTime
    public void testParseShortTime() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("2007-08-13T195123.789Z", new ParsePosition(0));
        assertEquals(date, d);

        d = ISO8601Utils.parse("2007-08-13T195123Z", new ParsePosition(0));
        assertEquals(dateZeroMillis, d);

        d = ISO8601Utils.parse("2007-08-13T215123.789+02:00", new ParsePosition(0));
        assertEquals(date, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseShortDateTime
    public void testParseShortDateTime() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("20070813T195123.789Z", new ParsePosition(0));
        assertEquals(date, d);

        d = ISO8601Utils.parse("20070813T195123Z", new ParsePosition(0));
        assertEquals(dateZeroMillis, d);

        d = ISO8601Utils.parse("20070813T215123.789+02:00", new ParsePosition(0));
        assertEquals(date, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseWithoutTime
    public void testParseWithoutTime() throws ParseException {
        Date d = ISO8601Utils.parse("2007-08-13Z", new ParsePosition(0));
        assertEquals(dateWithoutTime, d);

        d = ISO8601Utils.parse("20070813Z", new ParsePosition(0));
        assertEquals(dateWithoutTime, d);

        d = ISO8601Utils.parse("2007-08-13+00:00", new ParsePosition(0));
        assertEquals(dateWithoutTime, d);

        d = ISO8601Utils.parse("20070813+00:00", new ParsePosition(0));
        assertEquals(dateWithoutTime, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseOptional
    public void testParseOptional() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("2007-08-13T19:51Z", new ParsePosition(0));
        assertEquals(dateZeroSecondAndMillis, d);

        d = ISO8601Utils.parse("2007-08-13T1951Z", new ParsePosition(0));
        assertEquals(dateZeroSecondAndMillis, d);

        d = ISO8601Utils.parse("2007-08-13T21:51+02:00", new ParsePosition(0));
        assertEquals(dateZeroSecondAndMillis, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseRfc3339Examples
    public void testParseRfc3339Examples() throws java.text.ParseException {
        
        Date d = ISO8601Utils.parse("1985-04-12T23:20:50.52Z", new ParsePosition(0));
        assertEquals(newDate(1985, 4, 12, 23, 20, 50, 520, 0), d);

        d = ISO8601Utils.parse("1996-12-19T16:39:57-08:00", new ParsePosition(0));
        assertEquals(newDate(1996, 12, 19, 16, 39, 57, 0, -8 * 60), d);

        
        d = ISO8601Utils.parse("1990-12-31T23:59:60Z", new ParsePosition(0));
        assertEquals(newDate(1990, 12, 31, 23, 59, 59, 0, 0), d);

        
        d = ISO8601Utils.parse("1990-12-31T15:59:60-08:00", new ParsePosition(0));
        assertEquals(newDate(1990, 12, 31, 15, 59, 59, 0, -8 * 60), d);

        
        d = ISO8601Utils.parse("1937-01-01T12:00:27.87+00:20", new ParsePosition(0));
        assertEquals(newDate(1937, 1, 1, 12, 0, 27, 870, 20), d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testFractionalSeconds
    public void testFractionalSeconds() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("1970-01-01T00:00:00.9Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 900, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.09Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 90, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.009Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 9, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.0009Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 0, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.2147483647Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 214, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.2147483648Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 214, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.9+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 900, 2 * 60), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.09+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 90, 2 * 60), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.009+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 9, 2 * 60), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.0009+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 0, 2 * 60), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.2147483648+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 214, 2 * 60), d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testDecimalWithoutDecimalPointButNoFractionalSeconds
    public void testDecimalWithoutDecimalPointButNoFractionalSeconds() throws java.text.ParseException {
        try {
            ISO8601Utils.parse("1970-01-01T00:00:00.Z", new ParsePosition(0));
            fail();
        } catch (ParseException expected) {
        }
    }

// com.fasterxml.jackson.databind.util.JSONPObjectTest::testU2028Escaped
  public void testU2028Escaped() throws IOException {
    String containsU2028 = String.format("This string contains %c char", '\u2028');
    JSONPObject jsonpObject = new JSONPObject(CALLBACK, containsU2028);
    String valueAsString = MAPPER.writeValueAsString(jsonpObject);
    assertFalse(valueAsString.contains("\u2028"));
  }

// com.fasterxml.jackson.databind.util.JSONPObjectTest::testU2029Escaped
  public void testU2029Escaped() throws IOException {
    String containsU2029 = String.format("This string contains %c char", '\u2029');
    JSONPObject jsonpObject = new JSONPObject(CALLBACK, containsU2029);
    String valueAsString = MAPPER.writeValueAsString(jsonpObject);
    assertFalse(valueAsString.contains("\u2029"));
  }

// com.fasterxml.jackson.databind.util.JSONPObjectTest::testU2030NotEscaped
  public void testU2030NotEscaped() throws IOException {
    String containsU2030 = String.format("This string contains %c char", '\u2030');
    JSONPObject jsonpObject = new JSONPObject(CALLBACK, containsU2030);
    String valueAsString = MAPPER.writeValueAsString(jsonpObject);
    assertTrue(valueAsString.contains("\u2030"));
  }

// com.fasterxml.jackson.databind.util.JsonParserSequenceTest::testJsonParserSequenceOverridesSkipChildren
    public void testJsonParserSequenceOverridesSkipChildren() throws Exception
    {
        
        TokenBuffer buf1 = new TokenBuffer(MAPPER, false);
        buf1.writeStartObject();
        buf1.writeFieldName("foo");
        buf1.writeStartObject();
        JsonParser parser1 = buf1.asParser();

        
        TokenBuffer buf2 = new TokenBuffer(MAPPER, false);
        buf2.writeEndObject();
        buf2.writeEndObject();
        JsonParser parser2 = buf2.asParser();

        
        JsonParser parserSequence = JsonParserSequence.createFlattened(false, parser1, parser2);
        assertToken(JsonToken.START_OBJECT, parserSequence.nextToken());
        assertToken(JsonToken.FIELD_NAME, parserSequence.nextToken());
        assertToken(JsonToken.START_OBJECT, parserSequence.nextToken());

        
        
        parserSequence.skipChildren();

        
        assertToken(JsonToken.END_OBJECT, parserSequence.nextToken());
    }

// com.fasterxml.jackson.databind.util.NameTransformerTest::testSimpleTransformer
    public void testSimpleTransformer() throws Exception
    {
        NameTransformer xfer;

        xfer = NameTransformer.simpleTransformer("a", null);
        assertEquals("aFoo", xfer.transform("Foo"));
        assertEquals("Foo", xfer.reverse("aFoo"));

        xfer = NameTransformer.simpleTransformer(null, "++");
        assertEquals("foo++", xfer.transform("foo"));
        assertEquals("foo", xfer.reverse("foo++"));

        xfer = NameTransformer.simpleTransformer("(", ")");
        assertEquals("(foo)", xfer.transform("foo"));
        assertEquals("foo", xfer.reverse("(foo)"));
    }

// com.fasterxml.jackson.databind.util.RawValueTest::testEquality
    public void testEquality()
    {
        RawValue raw1 = new RawValue("foo");
        RawValue raw1b = new RawValue("foo");
        RawValue raw2 = new RawValue("bar");

        assertTrue(raw1.equals(raw1));
        assertTrue(raw1.equals(raw1b));

        assertFalse(raw1.equals(raw2));
        assertFalse(raw1.equals(null));

        assertFalse(new RawValue((JsonSerializable) null).equals(raw1));

        assertNotNull(raw1.toString());
    }

// com.fasterxml.jackson.databind.util.TestObjectBuffer::testUntyped
    public void testUntyped()
    {
        _testObjectBuffer(null);
    }

// com.fasterxml.jackson.databind.util.TestObjectBuffer::testTyped
    public void testTyped()
    {
        _testObjectBuffer(Integer.class);
    }

// com.fasterxml.jackson.databind.util.TestStdDateFormat::testFactories
    public void testFactories() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.US;
        assertNotNull(StdDateFormat.getISO8601Format(tz, loc));
        assertNotNull(StdDateFormat.getRFC1123Format(tz, loc));
    }

// com.fasterxml.jackson.databind.util.TestStdDateFormat::testLenientDefaults
    public void testLenientDefaults() throws Exception
    {
        StdDateFormat f = StdDateFormat.instance;

        
        assertTrue(f.isLenient());

        StdDateFormat f2 = f.clone();
        assertTrue(f2.isLenient());

        f2.setLenient(false);
        assertFalse(f2.isLenient());

        f2.setLenient(true);
        assertTrue(f2.isLenient());

        
        f2.setLenient(false);
        assertFalse(f2.isLenient());
        StdDateFormat f3 = f2.clone();
        assertFalse(f3.isLenient());
    }
