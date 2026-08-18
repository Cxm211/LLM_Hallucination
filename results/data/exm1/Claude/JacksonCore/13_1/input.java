// buggy code
    public JsonGenerator enable(Feature f) {
        super.enable(f);
        if (f == Feature.QUOTE_FIELD_NAMES) {
            _cfgUnqNames = false;
        }
        return this;
    }

// relevant test
// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testWindowsLinefeed
    public void testWindowsLinefeed() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter("  ", "\r\n"));
        String EXP = "{\r\n" +
            "  \"name\" : \"John Doe\",\r\n" +
            "  \"age\" : 3.14\r\n" +
            "}";
        assertEquals(EXP, _printTestData(pp, false));
        assertEquals(EXP, _printTestData(pp, true));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testTabIndent
    public void testTabIndent() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter("\t", "\n"));
        String EXP = "{\n" +
            "\t\"name\" : \"John Doe\",\n" +
            "\t\"age\" : 3.14\n" +
            "}";
        assertEquals(EXP, _printTestData(pp, false));
        assertEquals(EXP, _printTestData(pp, true));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testRootSeparator
    public void testRootSeparator() throws IOException
    {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter()
            .withRootSeparator("|");
        final String EXP = "1|2|3";

        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
        gen.setPrettyPrinter(pp);

        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.writeNumber(3);
        gen.close();
        assertEquals(EXP, sw.toString());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        gen = JSON_F.createGenerator(bytes);
        gen.setPrettyPrinter(pp);

        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.writeNumber(3);
        gen.close();
        assertEquals(EXP, bytes.toString("UTF-8"));

        
        pp = pp.withRootSeparator((String) null)
                .withArrayIndenter(null)
                .withObjectIndenter(null)
                .withoutSpacesInObjectEntries();
        sw = new StringWriter();
        gen = JSON_F.createGenerator(sw);
        gen.setPrettyPrinter(pp);

        gen.writeNumber(1);
        gen.writeStartArray();
        gen.writeNumber(2);
        gen.writeEndArray();
        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeNumber(3);
        gen.writeEndObject();
        gen.close();
        
        assertEquals("1[2]{\"a\":3}", sw.toString());
    }

// com.fasterxml.jackson.core.util.TestDelegates::testParserDelegate
    public void testParserDelegate() throws IOException
    {
        final String TOKEN ="foo";

        JsonParser parser = JSON_F.createParser("[ 1, true, null, { } ]");
        JsonParserDelegate del = new JsonParserDelegate(parser);
        
        assertNull(del.getCurrentToken());
        assertToken(JsonToken.START_ARRAY, del.nextToken());
        assertEquals("[", del.getText());
        assertToken(JsonToken.VALUE_NUMBER_INT, del.nextToken());
        assertEquals(1, del.getIntValue());

        assertToken(JsonToken.VALUE_TRUE, del.nextToken());
        assertTrue(del.getBooleanValue());

        assertToken(JsonToken.VALUE_NULL, del.nextToken());
        assertNull(del.getCurrentValue());
        del.setCurrentValue(TOKEN);

        assertToken(JsonToken.START_OBJECT, del.nextToken());
        assertNull(del.getCurrentValue());

        assertToken(JsonToken.END_OBJECT, del.nextToken());
        assertEquals(TOKEN, del.getCurrentValue());

        assertToken(JsonToken.END_ARRAY, del.nextToken());

        del.close();
        assertTrue(del.isClosed());
        assertTrue(parser.isClosed());

        parser.close();
    }

// com.fasterxml.jackson.core.util.TestDelegates::testGeneratorDelegate
    public void testGeneratorDelegate() throws IOException
    {
        final String TOKEN ="foo";

        StringWriter sw = new StringWriter();
        JsonGenerator g0 = JSON_F.createGenerator(sw);
        JsonGeneratorDelegate del = new JsonGeneratorDelegate(g0);
        del.writeStartArray();

        assertEquals(1, del.getOutputBuffered());
        
        del.writeNumber(13);
        del.writeNull();
        del.writeBoolean(false);
        del.writeString("foo");

        
        assertNull(del.getCurrentValue());
        del.setCurrentValue(TOKEN);

        del.writeStartObject();
        assertNull(del.getCurrentValue());
        del.writeEndObject();
        assertEquals(TOKEN, del.getCurrentValue());

        del.writeStartArray(0);
        del.writeEndArray();

        del.writeEndArray();
        
        del.flush();
        del.close();
        assertTrue(del.isClosed());        
        assertTrue(g0.isClosed());        
        assertEquals("[13,null,false,\"foo\",{},[]]", sw.toString());

        g0.close();
    }

// com.fasterxml.jackson.core.util.TestDelegates::testNotDelegateCopyMethods
    public void testNotDelegateCopyMethods() throws IOException
    {
        JsonParser jp = JSON_F.createParser("[{\"a\":[1,2,{\"b\":3}],\"c\":\"d\"},{\"e\":false},null]");
        StringWriter sw = new StringWriter();
        JsonGenerator jg = new JsonGeneratorDelegate(JSON_F.createGenerator(sw), false) {
            @Override
            public void writeFieldName(String name) throws IOException, JsonGenerationException {
                super.writeFieldName(name+"-test");
                super.writeBoolean(true);
                super.writeFieldName(name);
            }
        };
        jp.nextToken();
        jg.copyCurrentStructure(jp);
        jg.flush();
        assertEquals("[{\"a-test\":true,\"a\":[1,2,{\"b-test\":true,\"b\":3}],\"c-test\":true,\"c\":\"d\"},{\"e-test\":true,\"e\":false},null]", sw.toString());
        jp.close();
        jg.close();
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testVersionPartParsing
    public void testVersionPartParsing()
    {
        assertEquals(13, VersionUtil.parseVersionPart("13"));
        assertEquals(27, VersionUtil.parseVersionPart("27.8"));
        assertEquals(0, VersionUtil.parseVersionPart("-3"));
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testVersionParsing
    public void testVersionParsing()
    {
        assertEquals(new Version(1, 2, 15, "foo", "group", "artifact"),
                VersionUtil.parseVersion("1.2.15-foo", "group", "artifact"));
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testMavenVersionParsing
    public void testMavenVersionParsing() {
        assertEquals(new Version(1, 2, 3, "SNAPSHOT", "foo.bar", "foo-bar"),
                VersionUtil.mavenVersionFor(TestVersionUtil.class.getClassLoader(), "foo.bar", "foo-bar"));
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testPackageVersionMatches
    public void testPackageVersionMatches() {
        assertEquals(PackageVersion.VERSION, VersionUtil.versionFor(UTF8JsonGenerator.class));
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testVersionForUnknownVersion
    public void testVersionForUnknownVersion() {
        
        assertEquals(Version.unknownVersion(), VersionUtil.versionFor(TestVersionUtil.class));
    }
