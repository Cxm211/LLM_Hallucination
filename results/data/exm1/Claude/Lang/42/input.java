// buggy code
    public void escape(Writer writer, String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            String entityName = this.entityName(c);
            if (entityName == null) {
                if (c > 0x7F) {
                    writer.write("&#");
                    writer.write(Integer.toString(c, 10));
                    writer.write(';');
                } else {
                    writer.write(c);
                }
            } else {
                writer.write('&');
                writer.write(entityName);
                writer.write(';');
            }
        }
    }

// relevant test
// org.apache.commons.lang.EntitiesPerformanceTest::testBuildHash
    public void testBuildHash() throws Exception {
        for (int i = 0; i < COUNT; ++i) {
            hashEntities = build(new Entities.HashEntityMap());
        }
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testBuildTree
    public void testBuildTree() throws Exception {
        for (int i = 0; i < COUNT; ++i) {
            treeEntities = build(new Entities.TreeEntityMap());
        }
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testBuildArray
    public void testBuildArray() throws Exception {
        for (int i = 0; i < COUNT; ++i) {
            arrayEntities = build(new Entities.ArrayEntityMap());
        }
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testBuildBinary
    public void testBuildBinary() throws Exception {
        for (int i = 0; i < COUNT; ++i) {
            binaryEntities = build(new Entities.BinaryEntityMap());
        }
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testBuildPrimitive
    public void testBuildPrimitive() throws Exception {
        for (int i = 0; i < COUNT; ++i) {
            buildPrimitive();
        }
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testBuildLookup
    public void testBuildLookup() throws Exception {
        for (int i = 0; i < COUNT; ++i) {
            buildLookup();
        }
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testLookupHash
    public void testLookupHash() throws Exception {
        lookup(hashEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testLookupTree
    public void testLookupTree() throws Exception {
        lookup(treeEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testLookupArray
    public void testLookupArray() throws Exception {
        lookup(arrayEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testLookupBinary
    public void testLookupBinary() throws Exception {
        lookup(binaryEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testLookupPrimitive
    public void testLookupPrimitive() throws Exception {
        if (primitiveEntities == null) buildPrimitive();
        lookup(primitiveEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testLookupLookup
    public void testLookupLookup() throws Exception {
        if (lookupEntities == null) buildLookup();
        lookup(lookupEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testEscapeHash
    public void testEscapeHash() throws Exception {
        escapeIt(hashEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testEscapeTree
    public void testEscapeTree() throws Exception {
        escapeIt(treeEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testEscapeArray
    public void testEscapeArray() throws Exception {
        escapeIt(arrayEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testEscapeBinary
    public void testEscapeBinary() throws Exception {
        escapeIt(binaryEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testEscapePrimitive
    public void testEscapePrimitive() throws Exception {
        escapeIt(primitiveEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testEscapeLookup
    public void testEscapeLookup() throws Exception {
        escapeIt(lookupEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testUnescapeHash
    public void testUnescapeHash() throws Exception {
        unescapeIt(hashEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testUnescapeTree
    public void testUnescapeTree() throws Exception {
        unescapeIt(treeEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testUnescapeArray
    public void testUnescapeArray() throws Exception {
        unescapeIt(arrayEntities);
    }

// org.apache.commons.lang.EntitiesPerformanceTest::testUnescapeBinary
    public void testUnescapeBinary() throws Exception {
        unescapeIt(binaryEntities);
    }

// org.apache.commons.lang.EntitiesTest::testEscapeNamedEntity
    public void testEscapeNamedEntity() throws Exception
    {
        doTestEscapeNamedEntity("&foo;", "\u00A1");
        doTestEscapeNamedEntity("x&foo;", "x\u00A1");
        doTestEscapeNamedEntity("&foo;x", "\u00A1x");
        doTestEscapeNamedEntity("x&foo;x", "x\u00A1x");
        doTestEscapeNamedEntity("&foo;&bar;", "\u00A1\u00A2");
    }

// org.apache.commons.lang.EntitiesTest::testUnescapeNamedEntity
    public void testUnescapeNamedEntity() throws Exception
    {
        assertEquals("\u00A1", entities.unescape("&foo;"));
        assertEquals("x\u00A1", entities.unescape("x&foo;"));
        assertEquals("\u00A1x", entities.unescape("&foo;x"));
        assertEquals("x\u00A1x", entities.unescape("x&foo;x"));
        assertEquals("\u00A1\u00A2", entities.unescape("&foo;&bar;"));
    }

// org.apache.commons.lang.EntitiesTest::testUnescapeUnknownEntity
    public void testUnescapeUnknownEntity() throws Exception
    {
        doTestUnescapeEntity("&zzzz;", "&zzzz;");
    }

// org.apache.commons.lang.EntitiesTest::testUnescapeMiscellaneous
    public void testUnescapeMiscellaneous() throws Exception
    {
      doTestUnescapeEntity("&hello", "&hello");
      doTestUnescapeEntity("&;", "&;");
      doTestUnescapeEntity("&#;", "&#;");
      doTestUnescapeEntity("&#invalid;", "&#invalid;");
      doTestUnescapeEntity("A", "&#X41;");
    }

// org.apache.commons.lang.EntitiesTest::testAddEntitiesArray
    public void testAddEntitiesArray() throws Exception
    {
        String[][] array = {{"foo", "100"}, {"bar", "101"}};
        Entities e = new Entities();
        e.addEntities(array);
        assertEquals("foo", e.entityName(100));
        assertEquals("bar", e.entityName(101));
        assertEquals(100, e.entityValue("foo"));
        assertEquals(101, e.entityValue("bar"));
    }

// org.apache.commons.lang.EntitiesTest::testEntitiesXmlObject
    public void testEntitiesXmlObject() throws Exception
    {
        assertEquals("gt", Entities.XML.entityName('>'));
        assertEquals('>', Entities.XML.entityValue("gt"));
        assertEquals(-1, Entities.XML.entityValue("xyzzy"));
    }

// org.apache.commons.lang.EntitiesTest::testArrayIntMap
    public void testArrayIntMap() throws Exception
    {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap(2);
        checkSomeEntityMap(map);
        Entities.ArrayEntityMap map1 = new Entities.ArrayEntityMap();
        checkSomeEntityMap(map1);
        assertEquals(-1, map.value("null"));
        assertNull(map.name(-1));
    }

// org.apache.commons.lang.EntitiesTest::testTreeIntMap
    public void testTreeIntMap() throws Exception
    {
        Entities.EntityMap map = new Entities.TreeEntityMap();
        checkSomeEntityMap(map);
    }

// org.apache.commons.lang.EntitiesTest::testHashIntMap
    public void testHashIntMap() throws Exception
    {
        Entities.EntityMap map = new Entities.HashEntityMap();
        checkSomeEntityMap(map);
        assertEquals(-1, map.value("noname"));
    }

// org.apache.commons.lang.EntitiesTest::testBinaryIntMap
    public void testBinaryIntMap() throws Exception
    {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap(2);
        checkSomeEntityMap(map);
        Entities.BinaryEntityMap map1 = new Entities.BinaryEntityMap();
        checkSomeEntityMap(map1);
        
        
        map1.add("baz4a", 4);
        map1.add("baz4b", 4);
        assertEquals(-1, map1.value("baz4b"));
        assertEquals("baz4a", map1.name(4));
        assertNull(map1.name(99));
        
        Entities.BinaryEntityMap map2 = new Entities.BinaryEntityMap();
        map2.add("val1", 1);
        map2.add("val2", 2);
        map2.add("val3", 3);
        map2.add("val4", 4);
        map2.add("val5", 5);
        assertEquals("val5", map2.name(5));
        assertEquals("val4", map2.name(4));
        assertEquals("val3", map2.name(3));
        assertEquals("val2", map2.name(2));
        assertEquals("val1", map2.name(1));
    }

// org.apache.commons.lang.EntitiesTest::testPrimitiveIntMap
    public void testPrimitiveIntMap() throws Exception
    {
        Entities.PrimitiveEntityMap map = new Entities.PrimitiveEntityMap();
        checkSomeEntityMap(map);
    }

// org.apache.commons.lang.EntitiesTest::testHtml40Nbsp
    public void testHtml40Nbsp() throws Exception
    {
        assertEquals("&nbsp;", Entities.HTML40.escape("\u00A0"));
        Entities e = new Entities();
        e.map = new Entities.PrimitiveEntityMap();
        Entities.fillWithHtml40Entities(e);
        assertEquals("&nbsp;", e.escape("\u00A0"));
    }

// org.apache.commons.lang.EntitiesTest::testNumberOverflow
    public void testNumberOverflow() throws Exception {
        doTestUnescapeEntity("&#12345678;", "&#12345678;");
        doTestUnescapeEntity("x&#12345678;y", "x&#12345678;y");
        doTestUnescapeEntity("&#x12345678;", "&#x12345678;");
        doTestUnescapeEntity("x&#x12345678;y", "x&#x12345678;y");
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new StringEscapeUtils());
        Constructor[] cons = StringEscapeUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(StringEscapeUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(StringEscapeUtils.class.getModifiers()));
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeJava
    public void testEscapeJava() throws IOException {
        assertEquals(null, StringEscapeUtils.escapeJava(null));
        try {
            StringEscapeUtils.escapeJava(null, null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.escapeJava(null, "");
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        assertEscapeJava("empty string", "", "");
        assertEscapeJava(FOO, FOO);
        assertEscapeJava("tab", "\\t", "\t");
        assertEscapeJava("backslash", "\\\\", "\\");
        assertEscapeJava("single quote should not be escaped", "'", "'");
        assertEscapeJava("\\\\\\b\\t\\r", "\\\b\t\r");
        assertEscapeJava("\\u1234", "\u1234");
        assertEscapeJava("\\u0234", "\u0234");
        assertEscapeJava("\\u00EF", "\u00ef");
        assertEscapeJava("\\u0001", "\u0001");
        assertEscapeJava("Should use capitalized unicode hex", "\\uABCD", "\uabcd");

        assertEscapeJava("He didn't say, \\\"stop!\\\"",
                "He didn't say, \"stop!\"");
        assertEscapeJava("non-breaking space", "This space is non-breaking:" + "\\u00A0",
                "This space is non-breaking:\u00a0");
        assertEscapeJava("\\uABCD\\u1234\\u012C",
                "\uABCD\u1234\u012C");
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeJavaWithSlash
    public void testEscapeJavaWithSlash() {
        final String input = "String with a slash (/) in it";

        final String expected = input;
        final String actual = StringEscapeUtils.escapeJava(input);

        
        assertEquals(expected, actual);
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testUnescapeJava
    public void testUnescapeJava() throws IOException {
        assertEquals(null, StringEscapeUtils.unescapeJava(null));
        try {
            StringEscapeUtils.unescapeJava(null, null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.unescapeJava(null, "");
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.unescapeJava("\\u02-3");
            fail();
        } catch (RuntimeException ex) {
        }
        
        assertUnescapeJava("", "");
        assertUnescapeJava("test", "test");
        assertUnescapeJava("\ntest\b", "\\ntest\\b");
        assertUnescapeJava("\u123425foo\ntest\b", "\\u123425foo\\ntest\\b");
        assertUnescapeJava("'\foo\teste\r", "\\'\\foo\\teste\\r");
        assertUnescapeJava("\\", "\\");
        
        assertUnescapeJava("lowercase unicode", "\uABCDx", "\\uabcdx");
        assertUnescapeJava("uppercase unicode", "\uABCDx", "\\uABCDx");
        assertUnescapeJava("unicode as final character", "\uABCD", "\\uabcd");
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeJavaScript
    public void testEscapeJavaScript() {
        assertEquals(null, StringEscapeUtils.escapeJavaScript(null));
        try {
            StringEscapeUtils.escapeJavaScript(null, null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.escapeJavaScript(null, "");
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        assertEquals("He didn\\'t say, \\\"stop!\\\"", StringEscapeUtils.escapeJavaScript("He didn't say, \"stop!\""));
        assertEquals("document.getElementById(\\\"test\\\").value = \\'<script>alert(\\'aaa\\');<\\/script>\\';", 
                StringEscapeUtils.escapeJavaScript("document.getElementById(\"test\").value = '<script>alert('aaa');</script>';"));
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeHtml
    public void testEscapeHtml() {
        for (int i = 0; i < htmlEscapes.length; ++i) {
            String message = htmlEscapes[i][0];
            String expected = htmlEscapes[i][1];
            String original = htmlEscapes[i][2];
            assertEquals(message, expected, StringEscapeUtils.escapeHtml(original));
            StringWriter sw = new StringWriter();
            try {
            StringEscapeUtils.escapeHtml(sw, original);
            } catch (IOException e) {
            }
            String actual = original == null ? null : sw.toString();
            assertEquals(message, expected, actual);
        }
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testUnescapeHtml
    public void testUnescapeHtml() {
        for (int i = 0; i < htmlEscapes.length; ++i) {
            String message = htmlEscapes[i][0];
            String expected = htmlEscapes[i][2];
            String original = htmlEscapes[i][1];
            assertEquals(message, expected, StringEscapeUtils.unescapeHtml(original));
            
            StringWriter sw = new StringWriter();
            try {
            StringEscapeUtils.unescapeHtml(sw, original);
            } catch (IOException e) {
            }
            String actual = original == null ? null : sw.toString();
            assertEquals(message, expected, actual);
        }
        
        
        
        assertEquals("funny chars pass through OK", "Fran\u00E7ais", StringEscapeUtils.unescapeHtml("Fran\u00E7ais"));
        
        assertEquals("Hello&;World", StringEscapeUtils.unescapeHtml("Hello&;World"));
        assertEquals("Hello&#;World", StringEscapeUtils.unescapeHtml("Hello&#;World"));
        assertEquals("Hello&# ;World", StringEscapeUtils.unescapeHtml("Hello&# ;World"));
        assertEquals("Hello&##;World", StringEscapeUtils.unescapeHtml("Hello&##;World"));
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testUnescapeHexCharsHtml
    public void testUnescapeHexCharsHtml() {
        
        assertEquals("hex number unescape", "\u0080\u009F", StringEscapeUtils.unescapeHtml("&#x80;&#x9F;"));
        assertEquals("hex number unescape", "\u0080\u009F", StringEscapeUtils.unescapeHtml("&#X80;&#X9F;"));
        
        for (char i = Character.MIN_VALUE; i < Character.MAX_VALUE; i++) {
            Character c1 = new Character(i);
            Character c2 = new Character((char)(i+1));
            String expected = c1.toString() + c2.toString();
            String escapedC1 = "&#x" + Integer.toHexString((c1.charValue())) + ";";
            String escapedC2 = "&#x" + Integer.toHexString((c2.charValue())) + ";";
            assertEquals("hex number unescape index " + (int)i, expected, StringEscapeUtils.unescapeHtml(escapedC1 + escapedC2));
        }
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testUnescapeUnknownEntity
    public void testUnescapeUnknownEntity() throws Exception
    {
        assertEquals("&zzzz;", StringEscapeUtils.unescapeHtml("&zzzz;"));
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeHtmlVersions
    public void testEscapeHtmlVersions() throws Exception
    {
        assertEquals("&Beta;", StringEscapeUtils.escapeHtml("\u0392"));
        assertEquals("\u0392", StringEscapeUtils.unescapeHtml("&Beta;"));

        

    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeXml
    public void testEscapeXml() throws Exception {
        assertEquals("&lt;abc&gt;", StringEscapeUtils.escapeXml("<abc>"));
        assertEquals("<abc>", StringEscapeUtils.unescapeXml("&lt;abc&gt;"));

        assertEquals("XML should use numbers, not names for HTML entities",
                "&#161;", StringEscapeUtils.escapeXml("\u00A1"));
        assertEquals("XML should use numbers, not names for HTML entities",
                "\u00A0", StringEscapeUtils.unescapeXml("&#160;"));

        assertEquals("ain't", StringEscapeUtils.unescapeXml("ain&apos;t"));
        assertEquals("ain&apos;t", StringEscapeUtils.escapeXml("ain't"));
        assertEquals("", StringEscapeUtils.escapeXml(""));
        assertEquals(null, StringEscapeUtils.escapeXml(null));
        assertEquals(null, StringEscapeUtils.unescapeXml(null));

        StringWriter sw = new StringWriter();
        try {
            StringEscapeUtils.escapeXml(sw, "<abc>");
        } catch (IOException e) {
        }
        assertEquals("XML was escaped incorrectly", "&lt;abc&gt;", sw.toString() );

        sw = new StringWriter();
        try {
            StringEscapeUtils.unescapeXml(sw, "&lt;abc&gt;");
        } catch (IOException e) {
        }
        assertEquals("XML was unescaped incorrectly", "<abc>", sw.toString() );
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeSql
    public void testEscapeSql() throws Exception
    {
        assertEquals("don''t stop", StringEscapeUtils.escapeSql("don't stop"));
        assertEquals("", StringEscapeUtils.escapeSql(""));
        assertEquals(null, StringEscapeUtils.escapeSql(null));
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testStandaloneAmphersand
    public void testStandaloneAmphersand() {
        assertEquals("<P&O>", StringEscapeUtils.unescapeHtml("&lt;P&O&gt;"));
        assertEquals("test & <", StringEscapeUtils.unescapeHtml("test & &lt;"));
        assertEquals("<P&O>", StringEscapeUtils.unescapeXml("&lt;P&O&gt;"));
        assertEquals("test & <", StringEscapeUtils.unescapeXml("test & &lt;"));
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testLang313
    public void testLang313() {
        assertEquals("& &", StringEscapeUtils.unescapeHtml("& &amp;"));
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeCsvString
    public void testEscapeCsvString() throws Exception
    {
        assertEquals("foo.bar",          StringEscapeUtils.escapeCsv("foo.bar"));
        assertEquals("\"foo,bar\"",      StringEscapeUtils.escapeCsv("foo,bar"));
        assertEquals("\"foo\nbar\"",     StringEscapeUtils.escapeCsv("foo\nbar"));
        assertEquals("\"foo\rbar\"",     StringEscapeUtils.escapeCsv("foo\rbar"));
        assertEquals("\"foo\"\"bar\"",   StringEscapeUtils.escapeCsv("foo\"bar"));
        assertEquals("",   StringEscapeUtils.escapeCsv(""));
        assertEquals(null, StringEscapeUtils.escapeCsv(null));
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeCsvWriter
    public void testEscapeCsvWriter() throws Exception
    {
        checkCsvEscapeWriter("foo.bar",        "foo.bar");
        checkCsvEscapeWriter("\"foo,bar\"",    "foo,bar");
        checkCsvEscapeWriter("\"foo\nbar\"",   "foo\nbar");
        checkCsvEscapeWriter("\"foo\rbar\"",   "foo\rbar");
        checkCsvEscapeWriter("\"foo\"\"bar\"", "foo\"bar");
        checkCsvEscapeWriter("", null);
        checkCsvEscapeWriter("", "");
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testUnescapeCsvString
    public void testUnescapeCsvString() throws Exception
    {
        assertEquals("foo.bar",          StringEscapeUtils.unescapeCsv("foo.bar"));
        assertEquals("foo,bar",      StringEscapeUtils.unescapeCsv("\"foo,bar\""));
        assertEquals("foo\nbar",     StringEscapeUtils.unescapeCsv("\"foo\nbar\""));
        assertEquals("foo\rbar",     StringEscapeUtils.unescapeCsv("\"foo\rbar\""));
        assertEquals("foo\"bar",   StringEscapeUtils.unescapeCsv("\"foo\"\"bar\""));
        assertEquals("",   StringEscapeUtils.unescapeCsv(""));
        assertEquals(null, StringEscapeUtils.unescapeCsv(null));

        assertEquals("\"foo.bar\"",          StringEscapeUtils.unescapeCsv("\"foo.bar\""));
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testUnescapeCsvWriter
    public void testUnescapeCsvWriter() throws Exception
    {
        checkCsvUnescapeWriter("foo.bar",        "foo.bar");
        checkCsvUnescapeWriter("foo,bar",    "\"foo,bar\"");
        checkCsvUnescapeWriter("foo\nbar",   "\"foo\nbar\"");
        checkCsvUnescapeWriter("foo\rbar",   "\"foo\rbar\"");
        checkCsvUnescapeWriter("foo\"bar", "\"foo\"\"bar\"");
        checkCsvUnescapeWriter("", null);
        checkCsvUnescapeWriter("", "");

        checkCsvUnescapeWriter("\"foo.bar\"",        "\"foo.bar\"");
    }

// org.apache.commons.lang.StringEscapeUtilsTest::testEscapeHtmlHighUnicode
    public void testEscapeHtmlHighUnicode() throws java.io.UnsupportedEncodingException {
        
        
        
        
        byte[] data = new byte[] { (byte)0xF0, (byte)0x9D, (byte)0x8D, (byte)0xA2 };

        String escaped = StringEscapeUtils.escapeHtml( new String(data, "UTF8") );
        String unescaped = StringEscapeUtils.unescapeHtml( escaped );

        assertEquals( "High unicode was not escaped correctly", "&#119650;", escaped);
    }
