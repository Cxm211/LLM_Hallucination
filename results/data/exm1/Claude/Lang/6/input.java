// buggy code
    public final void translate(CharSequence input, Writer out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (input == null) {
            return;
        }
        int pos = 0;
        int len = input.length();
        while (pos < len) {
            int consumed = translate(input, pos, out);
            if (consumed == 0) {
                char[] c = Character.toChars(Character.codePointAt(input, pos));
                out.write(c);
                pos+= c.length;
                continue;
            }
//          // contract with translators is that they have to understand codepoints 
//          // and they just took care of a surrogate pair
            for (int pt = 0; pt < consumed; pt++) {
                pos += Character.charCount(Character.codePointAt(input, pos));
            }
        }
    }

// relevant test
// org.apache.commons.lang3.StringEscapeUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new StringEscapeUtils());
        Constructor<?>[] cons = StringEscapeUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(StringEscapeUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(StringEscapeUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeJava
    public void testEscapeJava() throws IOException {
        assertEquals(null, StringEscapeUtils.escapeJava(null));
        try {
            StringEscapeUtils.ESCAPE_JAVA.translate(null, null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.ESCAPE_JAVA.translate("", null);
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
        assertEscapeJava("Should use capitalized Unicode hex", "\\uABCD", "\uabcd");

        assertEscapeJava("He didn't say, \\\"stop!\\\"",
                "He didn't say, \"stop!\"");
        assertEscapeJava("non-breaking space", "This space is non-breaking:" + "\\u00A0",
                "This space is non-breaking:\u00a0");
        assertEscapeJava("\\uABCD\\u1234\\u012C",
                "\uABCD\u1234\u012C");
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeJavaWithSlash
    public void testEscapeJavaWithSlash() {
        final String input = "String with a slash (/) in it";

        final String expected = input;
        final String actual = StringEscapeUtils.escapeJava(input);

        
        assertEquals(expected, actual);
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeJava
    public void testUnescapeJava() throws IOException {
        assertEquals(null, StringEscapeUtils.unescapeJava(null));
        try {
            StringEscapeUtils.UNESCAPE_JAVA.translate(null, null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.UNESCAPE_JAVA.translate("", null);
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
        assertUnescapeJava("", "\\");
        
        assertUnescapeJava("lowercase Unicode", "\uABCDx", "\\uabcdx");
        assertUnescapeJava("uppercase Unicode", "\uABCDx", "\\uABCDx");
        assertUnescapeJava("Unicode as final character", "\uABCD", "\\uabcd");
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeEcmaScript
    public void testEscapeEcmaScript() {
        assertEquals(null, StringEscapeUtils.escapeEcmaScript(null));
        try {
            StringEscapeUtils.ESCAPE_ECMASCRIPT.translate(null, null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.ESCAPE_ECMASCRIPT.translate("", null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        assertEquals("He didn\\'t say, \\\"stop!\\\"", StringEscapeUtils.escapeEcmaScript("He didn't say, \"stop!\""));
        assertEquals("document.getElementById(\\\"test\\\").value = \\'<script>alert(\\'aaa\\');<\\/script>\\';", 
                StringEscapeUtils.escapeEcmaScript("document.getElementById(\"test\").value = '<script>alert('aaa');</script>';"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHtml
    public void testEscapeHtml() {
        for (int i = 0; i < HTML_ESCAPES.length; ++i) {
            String message = HTML_ESCAPES[i][0];
            String expected = HTML_ESCAPES[i][1];
            String original = HTML_ESCAPES[i][2];
            assertEquals(message, expected, StringEscapeUtils.escapeHtml4(original));
            StringWriter sw = new StringWriter();
            try {
                StringEscapeUtils.ESCAPE_HTML4.translate(original, sw);
            } catch (IOException e) {
            }
            String actual = original == null ? null : sw.toString();
            assertEquals(message, expected, actual);
        }
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeHtml4
    public void testUnescapeHtml4() {
        for (int i = 0; i < HTML_ESCAPES.length; ++i) {
            String message = HTML_ESCAPES[i][0];
            String expected = HTML_ESCAPES[i][2];
            String original = HTML_ESCAPES[i][1];
            assertEquals(message, expected, StringEscapeUtils.unescapeHtml4(original));
            
            StringWriter sw = new StringWriter();
            try {
                StringEscapeUtils.UNESCAPE_HTML4.translate(original, sw);
            } catch (IOException e) {
            }
            String actual = original == null ? null : sw.toString();
            assertEquals(message, expected, actual);
        }
        
        
        
        assertEquals("funny chars pass through OK", "Fran\u00E7ais", StringEscapeUtils.unescapeHtml4("Fran\u00E7ais"));
        
        assertEquals("Hello&;World", StringEscapeUtils.unescapeHtml4("Hello&;World"));
        assertEquals("Hello&#;World", StringEscapeUtils.unescapeHtml4("Hello&#;World"));
        assertEquals("Hello&# ;World", StringEscapeUtils.unescapeHtml4("Hello&# ;World"));
        assertEquals("Hello&##;World", StringEscapeUtils.unescapeHtml4("Hello&##;World"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeHexCharsHtml
    public void testUnescapeHexCharsHtml() {
        
        assertEquals("hex number unescape", "\u0080\u009F", StringEscapeUtils.unescapeHtml4("&#x80;&#x9F;"));
        assertEquals("hex number unescape", "\u0080\u009F", StringEscapeUtils.unescapeHtml4("&#X80;&#X9F;"));
        
        for (char i = Character.MIN_VALUE; i < Character.MAX_VALUE; i++) {
            Character c1 = new Character(i);
            Character c2 = new Character((char)(i+1));
            String expected = c1.toString() + c2.toString();
            String escapedC1 = "&#x" + Integer.toHexString((c1.charValue())) + ";";
            String escapedC2 = "&#x" + Integer.toHexString((c2.charValue())) + ";";
            assertEquals("hex number unescape index " + (int)i, expected, StringEscapeUtils.unescapeHtml4(escapedC1 + escapedC2));
        }
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeUnknownEntity
    public void testUnescapeUnknownEntity() throws Exception {
        assertEquals("&zzzz;", StringEscapeUtils.unescapeHtml4("&zzzz;"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHtmlVersions
    public void testEscapeHtmlVersions() throws Exception {
        assertEquals("&Beta;", StringEscapeUtils.escapeHtml4("\u0392"));
        assertEquals("\u0392", StringEscapeUtils.unescapeHtml4("&Beta;"));

        
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeXml
    public void testEscapeXml() throws Exception {
        assertEquals("&lt;abc&gt;", StringEscapeUtils.escapeXml("<abc>"));
        assertEquals("<abc>", StringEscapeUtils.unescapeXml("&lt;abc&gt;"));

        assertEquals("XML should not escape >0x7f values",
                "\u00A1", StringEscapeUtils.escapeXml("\u00A1"));
        assertEquals("XML should be able to unescape >0x7f values",
                "\u00A0", StringEscapeUtils.unescapeXml("&#160;"));
        assertEquals("XML should be able to unescape >0x7f values with one leading 0",
                "\u00A0", StringEscapeUtils.unescapeXml("&#0160;"));
        assertEquals("XML should be able to unescape >0x7f values with two leading 0s",
                "\u00A0", StringEscapeUtils.unescapeXml("&#00160;"));
        assertEquals("XML should be able to unescape >0x7f values with three leading 0s",
                "\u00A0", StringEscapeUtils.unescapeXml("&#000160;"));

        assertEquals("ain't", StringEscapeUtils.unescapeXml("ain&apos;t"));
        assertEquals("ain&apos;t", StringEscapeUtils.escapeXml("ain't"));
        assertEquals("", StringEscapeUtils.escapeXml(""));
        assertEquals(null, StringEscapeUtils.escapeXml(null));
        assertEquals(null, StringEscapeUtils.unescapeXml(null));

        StringWriter sw = new StringWriter();
        try {
            StringEscapeUtils.ESCAPE_XML.translate("<abc>", sw);
        } catch (IOException e) {
        }
        assertEquals("XML was escaped incorrectly", "&lt;abc&gt;", sw.toString() );

        sw = new StringWriter();
        try {
            StringEscapeUtils.UNESCAPE_XML.translate("&lt;abc&gt;", sw);
        } catch (IOException e) {
        }
        assertEquals("XML was unescaped incorrectly", "<abc>", sw.toString() );
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeXmlSupplementaryCharacters
    public void testEscapeXmlSupplementaryCharacters() {
        CharSequenceTranslator escapeXml = 
            StringEscapeUtils.ESCAPE_XML.with( NumericEntityEscaper.between(0x7f, Integer.MAX_VALUE) );

        assertEquals("Supplementary character must be represented using a single escape", "&#144308;",
                escapeXml.translate("\uD84C\uDFB4"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeXmlSupplementaryCharacters
    public void testUnescapeXmlSupplementaryCharacters() {
        assertEquals("Supplementary character must be represented using a single escape", "\uD84C\uDFB4",
                StringEscapeUtils.unescapeXml("&#144308;") );
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testStandaloneAmphersand
    public void testStandaloneAmphersand() {
        assertEquals("<P&O>", StringEscapeUtils.unescapeHtml4("&lt;P&O&gt;"));
        assertEquals("test & <", StringEscapeUtils.unescapeHtml4("test & &lt;"));
        assertEquals("<P&O>", StringEscapeUtils.unescapeXml("&lt;P&O&gt;"));
        assertEquals("test & <", StringEscapeUtils.unescapeXml("test & &lt;"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testLang313
    public void testLang313() {
        assertEquals("& &", StringEscapeUtils.unescapeHtml4("& &amp;"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeCsvString
    public void testEscapeCsvString() throws Exception {
        assertEquals("foo.bar",          StringEscapeUtils.escapeCsv("foo.bar"));
        assertEquals("\"foo,bar\"",      StringEscapeUtils.escapeCsv("foo,bar"));
        assertEquals("\"foo\nbar\"",     StringEscapeUtils.escapeCsv("foo\nbar"));
        assertEquals("\"foo\rbar\"",     StringEscapeUtils.escapeCsv("foo\rbar"));
        assertEquals("\"foo\"\"bar\"",   StringEscapeUtils.escapeCsv("foo\"bar"));
        assertEquals("",   StringEscapeUtils.escapeCsv(""));
        assertEquals(null, StringEscapeUtils.escapeCsv(null));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeCsvWriter
    public void testEscapeCsvWriter() throws Exception {
        checkCsvEscapeWriter("foo.bar",        "foo.bar");
        checkCsvEscapeWriter("\"foo,bar\"",    "foo,bar");
        checkCsvEscapeWriter("\"foo\nbar\"",   "foo\nbar");
        checkCsvEscapeWriter("\"foo\rbar\"",   "foo\rbar");
        checkCsvEscapeWriter("\"foo\"\"bar\"", "foo\"bar");
        checkCsvEscapeWriter("", null);
        checkCsvEscapeWriter("", "");
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeCsvString
    public void testUnescapeCsvString() throws Exception {
        assertEquals("foo.bar",          StringEscapeUtils.unescapeCsv("foo.bar"));
        assertEquals("foo,bar",      StringEscapeUtils.unescapeCsv("\"foo,bar\""));
        assertEquals("foo\nbar",     StringEscapeUtils.unescapeCsv("\"foo\nbar\""));
        assertEquals("foo\rbar",     StringEscapeUtils.unescapeCsv("\"foo\rbar\""));
        assertEquals("foo\"bar",   StringEscapeUtils.unescapeCsv("\"foo\"\"bar\""));
        assertEquals("",   StringEscapeUtils.unescapeCsv(""));
        assertEquals(null, StringEscapeUtils.unescapeCsv(null));

        assertEquals("\"foo.bar\"",          StringEscapeUtils.unescapeCsv("\"foo.bar\""));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeCsvWriter
    public void testUnescapeCsvWriter() throws Exception {
        checkCsvUnescapeWriter("foo.bar",        "foo.bar");
        checkCsvUnescapeWriter("foo,bar",    "\"foo,bar\"");
        checkCsvUnescapeWriter("foo\nbar",   "\"foo\nbar\"");
        checkCsvUnescapeWriter("foo\rbar",   "\"foo\rbar\"");
        checkCsvUnescapeWriter("foo\"bar", "\"foo\"\"bar\"");
        checkCsvUnescapeWriter("", null);
        checkCsvUnescapeWriter("", "");

        checkCsvUnescapeWriter("\"foo.bar\"",        "\"foo.bar\"");
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHtmlHighUnicode
    public void testEscapeHtmlHighUnicode() throws java.io.UnsupportedEncodingException {
        
        
        
        
        byte[] data = new byte[] { (byte)0xF0, (byte)0x9D, (byte)0x8D, (byte)0xA2 };

        String original = new String(data, "UTF8");

        String escaped = StringEscapeUtils.escapeHtml4( original );
        assertEquals( "High Unicode should not have been escaped", original, escaped);

        String unescaped = StringEscapeUtils.unescapeHtml4( escaped );
        assertEquals( "High Unicode should have been unchanged", original, unescaped);

    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHiragana
    public void testEscapeHiragana() {
        
        String original = "\u304B\u304C\u3068";
        String escaped = StringEscapeUtils.escapeHtml4(original);
        assertEquals( "Hiragana character Unicode behaviour should not be being escaped by escapeHtml4",
        original, escaped);

        String unescaped = StringEscapeUtils.unescapeHtml4( escaped );

        assertEquals( "Hiragana character Unicode behaviour has changed - expected no unescaping", escaped, unescaped);
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testLang708
    public void testLang708() throws IOException {
        String input = IOUtils.toString(new FileInputStream("src/test/resources/lang-708-input.txt"), "UTF-8");
        String escaped = StringEscapeUtils.escapeEcmaScript(input);
        
        assertTrue(escaped, escaped.endsWith("}]"));
        
        assertTrue(escaped, escaped.endsWith("\"valueCode\\\":\\\"\\\"}]"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testLang720
    public void testLang720() {
        String input = new StringBuilder("\ud842\udfb7").append("A").toString();
        String escaped = StringEscapeUtils.escapeXml(input);
        assertEquals(input, escaped);
    }

// org.apache.commons.lang3.StringUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new StringUtils());
        Constructor<?>[] cons = StringUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(StringUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(StringUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.StringUtilsTest::testCaseFunctions
    public void testCaseFunctions() {
        assertEquals(null, StringUtils.upperCase(null));
        assertEquals(null, StringUtils.upperCase(null, Locale.ENGLISH));
        assertEquals(null, StringUtils.lowerCase(null));
        assertEquals(null, StringUtils.lowerCase(null, Locale.ENGLISH));
        assertEquals(null, StringUtils.capitalize(null));
        assertEquals(null, StringUtils.uncapitalize(null));

        assertEquals("capitalize(empty-string) failed",
                     "", StringUtils.capitalize("") );
        assertEquals("capitalize(single-char-string) failed",
                "X", StringUtils.capitalize("x") );
        
        assertEquals("uncapitalize(String) failed",
                     FOO_UNCAP, StringUtils.uncapitalize(FOO_CAP) );
        assertEquals("uncapitalize(empty-string) failed",
                     "", StringUtils.uncapitalize("") );
        assertEquals("uncapitalize(single-char-string) failed",
                "x", StringUtils.uncapitalize("X") );
                     
        
        assertEquals("uncapitalize(capitalize(String)) failed",
                     SENTENCE_UNCAP, StringUtils.uncapitalize(StringUtils.capitalize(SENTENCE_UNCAP)) );
        assertEquals("capitalize(uncapitalize(String)) failed",
                     SENTENCE_CAP, StringUtils.capitalize(StringUtils.uncapitalize(SENTENCE_CAP)) );

        
        assertEquals("uncapitalize(capitalize(String)) failed",
                     FOO_UNCAP, StringUtils.uncapitalize(StringUtils.capitalize(FOO_UNCAP)) );
        assertEquals("capitalize(uncapitalize(String)) failed",
                     FOO_CAP, StringUtils.capitalize(StringUtils.uncapitalize(FOO_CAP)) );

        assertEquals("upperCase(String) failed",
                     "FOO TEST THING", StringUtils.upperCase("fOo test THING") );
        assertEquals("upperCase(empty-string) failed",
                     "", StringUtils.upperCase("") );
        assertEquals("lowerCase(String) failed",
                     "foo test thing", StringUtils.lowerCase("fOo test THING") );
        assertEquals("lowerCase(empty-string) failed",
                     "", StringUtils.lowerCase("") );

        assertEquals("upperCase(String, Locale) failed",
                     "FOO TEST THING", StringUtils.upperCase("fOo test THING", Locale.ENGLISH) );
        assertEquals("upperCase(empty-string, Locale) failed",
                     "", StringUtils.upperCase("", Locale.ENGLISH) );
        assertEquals("lowerCase(String, Locale) failed",
                     "foo test thing", StringUtils.lowerCase("fOo test THING", Locale.ENGLISH) );
        assertEquals("lowerCase(empty-string, Locale) failed",
                     "", StringUtils.lowerCase("", Locale.ENGLISH) );
    }

// org.apache.commons.lang3.StringUtilsTest::testSwapCase_String
    public void testSwapCase_String() {
        assertEquals(null, StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));
        assertEquals("  ", StringUtils.swapCase("  "));
        
        assertEquals("i", WordUtils.swapCase("I") );
        assertEquals("I", WordUtils.swapCase("i") );
        assertEquals("I AM HERE 123", StringUtils.swapCase("i am here 123") );
        assertEquals("i aM hERE 123", StringUtils.swapCase("I Am Here 123") );
        assertEquals("I AM here 123", StringUtils.swapCase("i am HERE 123") );
        assertEquals("i am here 123", StringUtils.swapCase("I AM HERE 123") );
        
        String test = "This String contains a TitleCase character: \u01C8";
        String expect = "tHIS sTRING CONTAINS A tITLEcASE CHARACTER: \u01C9";
        assertEquals(expect, WordUtils.swapCase(test));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_Objects
    public void testJoin_Objects() {
        assertEquals("abc", StringUtils.join("a", "b", "c"));
        assertEquals("a",   StringUtils.join(null, "", "a"));
        assertEquals(null,  StringUtils.join((Object[])null));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_Objectarray
    public void testJoin_Objectarray() {

        assertEquals(null, StringUtils.join((Object[]) null)); 
        
        assertEquals("", StringUtils.join()); 
        assertEquals("", StringUtils.join((Object) null)); 

        assertEquals("", StringUtils.join(EMPTY_ARRAY_LIST));
        assertEquals("", StringUtils.join(NULL_ARRAY_LIST));
        assertEquals("null", StringUtils.join(NULL_TO_STRING_LIST));
        assertEquals("abc", StringUtils.join(new String[] {"a", "b", "c"}));
        assertEquals("a", StringUtils.join(new String[] {null, "a", ""}));
        assertEquals("foo", StringUtils.join(MIXED_ARRAY_LIST));
        assertEquals("foo2", StringUtils.join(MIXED_TYPE_LIST));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayCharSeparator
    public void testJoin_ArrayCharSeparator() {
        assertEquals(null, StringUtils.join((Object[]) null, ','));
        assertEquals(TEXT_LIST_CHAR, StringUtils.join(ARRAY_LIST, SEPARATOR_CHAR));
        assertEquals("", StringUtils.join(EMPTY_ARRAY_LIST, SEPARATOR_CHAR));
        assertEquals(";;foo", StringUtils.join(MIXED_ARRAY_LIST, SEPARATOR_CHAR));
        assertEquals("foo;2", StringUtils.join(MIXED_TYPE_LIST, SEPARATOR_CHAR));

        assertEquals("/", StringUtils.join(MIXED_ARRAY_LIST, '/', 0, MIXED_ARRAY_LIST.length-1));
        assertEquals("foo", StringUtils.join(MIXED_TYPE_LIST, '/', 0, 1));
        assertEquals("null", StringUtils.join(NULL_TO_STRING_LIST,'/', 0, 1));
        assertEquals("foo/2", StringUtils.join(MIXED_TYPE_LIST, '/', 0, 2));
        assertEquals("2", StringUtils.join(MIXED_TYPE_LIST, '/', 1, 2));
        assertEquals("", StringUtils.join(MIXED_TYPE_LIST, '/', 2, 1));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayOfChars
    public void testJoin_ArrayOfChars() {
        assertEquals(null, StringUtils.join((char[]) null, ','));
        assertEquals("1;2", StringUtils.join(CHAR_PRIM_LIST, SEPARATOR_CHAR));
        assertEquals("2", StringUtils.join(CHAR_PRIM_LIST, SEPARATOR_CHAR, 1, 2));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayOfBytes
    public void testJoin_ArrayOfBytes() {
        assertEquals(null, StringUtils.join((byte[]) null, ','));
        assertEquals("1;2", StringUtils.join(BYTE_PRIM_LIST, SEPARATOR_CHAR));
        assertEquals("2", StringUtils.join(BYTE_PRIM_LIST, SEPARATOR_CHAR, 1, 2));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayOfInts
    public void testJoin_ArrayOfInts() {
        assertEquals(null, StringUtils.join((int[]) null, ','));
        assertEquals("1;2", StringUtils.join(INT_PRIM_LIST, SEPARATOR_CHAR));
        assertEquals("2", StringUtils.join(INT_PRIM_LIST, SEPARATOR_CHAR, 1, 2));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayOfLongs
    public void testJoin_ArrayOfLongs() {
        assertEquals(null, StringUtils.join((long[]) null, ','));
        assertEquals("1;2", StringUtils.join(LONG_PRIM_LIST, SEPARATOR_CHAR));
        assertEquals("2", StringUtils.join(LONG_PRIM_LIST, SEPARATOR_CHAR, 1, 2));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayOfFloats
    public void testJoin_ArrayOfFloats() {
        assertEquals(null, StringUtils.join((float[]) null, ','));
        assertEquals("1.0;2.0", StringUtils.join(FLOAT_PRIM_LIST, SEPARATOR_CHAR));
        assertEquals("2.0", StringUtils.join(FLOAT_PRIM_LIST, SEPARATOR_CHAR, 1, 2));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayOfDoubles
    public void testJoin_ArrayOfDoubles() {
        assertEquals(null, StringUtils.join((double[]) null, ','));
        assertEquals("1.0;2.0", StringUtils.join(DOUBLE_PRIM_LIST, SEPARATOR_CHAR));
        assertEquals("2.0", StringUtils.join(DOUBLE_PRIM_LIST, SEPARATOR_CHAR, 1, 2));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayOfShorts
    public void testJoin_ArrayOfShorts() {
        assertEquals(null, StringUtils.join((short[]) null, ','));
        assertEquals("1;2", StringUtils.join(SHORT_PRIM_LIST, SEPARATOR_CHAR));
        assertEquals("2", StringUtils.join(SHORT_PRIM_LIST, SEPARATOR_CHAR, 1, 2));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayString
    public void testJoin_ArrayString() {
        assertEquals(null, StringUtils.join((Object[]) null, null));
        assertEquals(TEXT_LIST_NOSEP, StringUtils.join(ARRAY_LIST, null));
        assertEquals(TEXT_LIST_NOSEP, StringUtils.join(ARRAY_LIST, ""));
        
        assertEquals("", StringUtils.join(NULL_ARRAY_LIST, null));
        
        assertEquals("", StringUtils.join(EMPTY_ARRAY_LIST, null));
        assertEquals("", StringUtils.join(EMPTY_ARRAY_LIST, ""));
        assertEquals("", StringUtils.join(EMPTY_ARRAY_LIST, SEPARATOR));

        assertEquals(TEXT_LIST, StringUtils.join(ARRAY_LIST, SEPARATOR));
        assertEquals(",,foo", StringUtils.join(MIXED_ARRAY_LIST, SEPARATOR));
        assertEquals("foo,2", StringUtils.join(MIXED_TYPE_LIST, SEPARATOR));

        assertEquals("/", StringUtils.join(MIXED_ARRAY_LIST, "/", 0, MIXED_ARRAY_LIST.length-1));
        assertEquals("", StringUtils.join(MIXED_ARRAY_LIST, "", 0, MIXED_ARRAY_LIST.length-1));
        assertEquals("foo", StringUtils.join(MIXED_TYPE_LIST, "/", 0, 1));
        assertEquals("foo/2", StringUtils.join(MIXED_TYPE_LIST, "/", 0, 2));
        assertEquals("2", StringUtils.join(MIXED_TYPE_LIST, "/", 1, 2));
        assertEquals("", StringUtils.join(MIXED_TYPE_LIST, "/", 2, 1));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_IteratorChar
    public void testJoin_IteratorChar() {
        assertEquals(null, StringUtils.join((Iterator<?>) null, ','));
        assertEquals(TEXT_LIST_CHAR, StringUtils.join(Arrays.asList(ARRAY_LIST).iterator(), SEPARATOR_CHAR));
        assertEquals("", StringUtils.join(Arrays.asList(NULL_ARRAY_LIST).iterator(), SEPARATOR_CHAR));
        assertEquals("", StringUtils.join(Arrays.asList(EMPTY_ARRAY_LIST).iterator(), SEPARATOR_CHAR));
        assertEquals("foo", StringUtils.join(Collections.singleton("foo").iterator(), 'x'));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_IteratorString
    public void testJoin_IteratorString() {
        assertEquals(null, StringUtils.join((Iterator<?>) null, null));
        assertEquals(TEXT_LIST_NOSEP, StringUtils.join(Arrays.asList(ARRAY_LIST).iterator(), null));
        assertEquals(TEXT_LIST_NOSEP, StringUtils.join(Arrays.asList(ARRAY_LIST).iterator(), ""));
        assertEquals("foo", StringUtils.join(Collections.singleton("foo").iterator(), "x"));
        assertEquals("foo", StringUtils.join(Collections.singleton("foo").iterator(), null));

        assertEquals("", StringUtils.join(Arrays.asList(NULL_ARRAY_LIST).iterator(), null));
        
        assertEquals("", StringUtils.join(Arrays.asList(EMPTY_ARRAY_LIST).iterator(), null));
        assertEquals("", StringUtils.join(Arrays.asList(EMPTY_ARRAY_LIST).iterator(), ""));
        assertEquals("", StringUtils.join(Arrays.asList(EMPTY_ARRAY_LIST).iterator(), SEPARATOR));
        
        assertEquals(TEXT_LIST, StringUtils.join(Arrays.asList(ARRAY_LIST).iterator(), SEPARATOR));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_IterableChar
    public void testJoin_IterableChar() {
        assertEquals(null, StringUtils.join((Iterable<?>) null, ','));
        assertEquals(TEXT_LIST_CHAR, StringUtils.join(Arrays.asList(ARRAY_LIST), SEPARATOR_CHAR));
        assertEquals("", StringUtils.join(Arrays.asList(NULL_ARRAY_LIST), SEPARATOR_CHAR));
        assertEquals("", StringUtils.join(Arrays.asList(EMPTY_ARRAY_LIST), SEPARATOR_CHAR));
        assertEquals("foo", StringUtils.join(Collections.singleton("foo"), 'x'));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_IterableString
    public void testJoin_IterableString() {
        assertEquals(null, StringUtils.join((Iterable<?>) null, null));
        assertEquals(TEXT_LIST_NOSEP, StringUtils.join(Arrays.asList(ARRAY_LIST), null));
        assertEquals(TEXT_LIST_NOSEP, StringUtils.join(Arrays.asList(ARRAY_LIST), ""));
        assertEquals("foo", StringUtils.join(Collections.singleton("foo"), "x"));
        assertEquals("foo", StringUtils.join(Collections.singleton("foo"), null));

        assertEquals("", StringUtils.join(Arrays.asList(NULL_ARRAY_LIST), null));

        assertEquals("", StringUtils.join(Arrays.asList(EMPTY_ARRAY_LIST), null));
        assertEquals("", StringUtils.join(Arrays.asList(EMPTY_ARRAY_LIST), ""));
        assertEquals("", StringUtils.join(Arrays.asList(EMPTY_ARRAY_LIST), SEPARATOR));

        assertEquals(TEXT_LIST, StringUtils.join(Arrays.asList(ARRAY_LIST), SEPARATOR));
    }

// org.apache.commons.lang3.StringUtilsTest::testSplit_String
    public void testSplit_String() {
        assertArrayEquals(null, StringUtils.split(null));
        assertEquals(0, StringUtils.split("").length);
        
        String str = "a b  .c";
        String[] res = StringUtils.split(str);
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals(".c", res[2]);
        
        str = " a ";
        res = StringUtils.split(str);
        assertEquals(1, res.length);
        assertEquals("a", res[0]);
        
        str = "a" + WHITESPACE + "b" + NON_WHITESPACE + "c";
        res = StringUtils.split(str);
        assertEquals(2, res.length);
        assertEquals("a", res[0]);
        assertEquals("b" + NON_WHITESPACE + "c", res[1]);                       
    }

// org.apache.commons.lang3.StringUtilsTest::testSplit_StringChar
    public void testSplit_StringChar() {
        assertArrayEquals(null, StringUtils.split(null, '.'));
        assertEquals(0, StringUtils.split("", '.').length);

        String str = "a.b.. c";
        String[] res = StringUtils.split(str, '.');
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals(" c", res[2]);
            
        str = ".a.";
        res = StringUtils.split(str, '.');
        assertEquals(1, res.length);
        assertEquals("a", res[0]);
        
        str = "a b c";
        res = StringUtils.split(str,' ');
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals("c", res[2]);
    }

// org.apache.commons.lang3.StringUtilsTest::testSplit_StringString_StringStringInt
    public void testSplit_StringString_StringStringInt() {
        assertArrayEquals(null, StringUtils.split(null, "."));
        assertArrayEquals(null, StringUtils.split(null, ".", 3));
        
        assertEquals(0, StringUtils.split("", ".").length);
        assertEquals(0, StringUtils.split("", ".", 3).length);
        
        innerTestSplit('.', ".", ' ');
        innerTestSplit('.', ".", ',');
        innerTestSplit('.', ".,", 'x');
        for (int i = 0; i < WHITESPACE.length(); i++) {
            for (int j = 0; j < NON_WHITESPACE.length(); j++) {
                innerTestSplit(WHITESPACE.charAt(i), null, NON_WHITESPACE.charAt(j));
                innerTestSplit(WHITESPACE.charAt(i), String.valueOf(WHITESPACE.charAt(i)), NON_WHITESPACE.charAt(j));
            }
        }
        
        String[] results;
        String[] expectedResults = {"ab", "de fg"};
        results = StringUtils.split("ab   de fg", null, 2);
        assertEquals(expectedResults.length, results.length);
        for (int i = 0; i < expectedResults.length; i++) {
            assertEquals(expectedResults[i], results[i]);
        }
        
        String[] expectedResults2 = {"ab", "cd:ef"};
        results = StringUtils.split("ab:cd:ef",":", 2);
        assertEquals(expectedResults2.length, results.length);
        for (int i = 0; i < expectedResults2.length; i++) {
            assertEquals(expectedResults2[i], results[i]);
        }
    }

// org.apache.commons.lang3.StringUtilsTest::testSplitByWholeString_StringStringBoolean
    public void testSplitByWholeString_StringStringBoolean() {
        assertArrayEquals( null, StringUtils.splitByWholeSeparator( null, "." ) ) ;

        assertEquals( 0, StringUtils.splitByWholeSeparator( "", "." ).length ) ;

        String stringToSplitOnNulls = "ab   de fg" ;
        String[] splitOnNullExpectedResults = { "ab", "de", "fg" } ;

        String[] splitOnNullResults = StringUtils.splitByWholeSeparator( stringToSplitOnNulls, null ) ;
        assertEquals( splitOnNullExpectedResults.length, splitOnNullResults.length ) ;
        for ( int i = 0 ; i < splitOnNullExpectedResults.length ; i+= 1 ) {
            assertEquals( splitOnNullExpectedResults[i], splitOnNullResults[i] ) ;
        }

        String stringToSplitOnCharactersAndString = "abstemiouslyaeiouyabstemiously" ;

        String[] splitOnStringExpectedResults = { "abstemiously", "abstemiously" } ;
        String[] splitOnStringResults = StringUtils.splitByWholeSeparator( stringToSplitOnCharactersAndString, "aeiouy" ) ;
        assertEquals( splitOnStringExpectedResults.length, splitOnStringResults.length ) ;
        for ( int i = 0 ; i < splitOnStringExpectedResults.length ; i+= 1 ) {
            assertEquals( splitOnStringExpectedResults[i], splitOnStringResults[i] ) ;
        }

        String[] splitWithMultipleSeparatorExpectedResults = {"ab", "cd", "ef"};
        String[] splitWithMultipleSeparator = StringUtils.splitByWholeSeparator("ab:cd::ef", ":");
        assertEquals( splitWithMultipleSeparatorExpectedResults.length, splitWithMultipleSeparator.length );
        for( int i = 0; i < splitWithMultipleSeparatorExpectedResults.length ; i++ ) {
            assertEquals( splitWithMultipleSeparatorExpectedResults[i], splitWithMultipleSeparator[i] ) ;
        }
    }

// org.apache.commons.lang3.StringUtilsTest::testSplitByWholeString_StringStringBooleanInt
    public void testSplitByWholeString_StringStringBooleanInt() {
        assertArrayEquals( null, StringUtils.splitByWholeSeparator( null, ".", 3 ) ) ;

        assertEquals( 0, StringUtils.splitByWholeSeparator( "", ".", 3 ).length ) ;

        String stringToSplitOnNulls = "ab   de fg" ;
        String[] splitOnNullExpectedResults = { "ab", "de fg" } ;
        

        String[] splitOnNullResults = StringUtils.splitByWholeSeparator( stringToSplitOnNulls, null, 2 ) ;
        assertEquals( splitOnNullExpectedResults.length, splitOnNullResults.length ) ;
        for ( int i = 0 ; i < splitOnNullExpectedResults.length ; i+= 1 ) {
            assertEquals( splitOnNullExpectedResults[i], splitOnNullResults[i] ) ;
        }

        String stringToSplitOnCharactersAndString = "abstemiouslyaeiouyabstemiouslyaeiouyabstemiously" ;

        String[] splitOnStringExpectedResults = { "abstemiously", "abstemiouslyaeiouyabstemiously" } ;
        
        String[] splitOnStringResults = StringUtils.splitByWholeSeparator( stringToSplitOnCharactersAndString, "aeiouy", 2 ) ;
        assertEquals( splitOnStringExpectedResults.length, splitOnStringResults.length ) ;
        for ( int i = 0 ; i < splitOnStringExpectedResults.length ; i++ ) {
            assertEquals( splitOnStringExpectedResults[i], splitOnStringResults[i] ) ;
        }
    }

// org.apache.commons.lang3.StringUtilsTest::testSplitByWholeSeparatorPreserveAllTokens_StringStringInt
    public void testSplitByWholeSeparatorPreserveAllTokens_StringStringInt() {
        assertArrayEquals( null, StringUtils.splitByWholeSeparatorPreserveAllTokens( null, ".", -1 ) ) ;

        assertEquals( 0, StringUtils.splitByWholeSeparatorPreserveAllTokens( "", ".", -1 ).length ) ;

        
        String input = "ab   de fg" ;
        String[] expected = new String[] { "ab", "", "", "de", "fg" } ;

        String[] actual = StringUtils.splitByWholeSeparatorPreserveAllTokens( input, null, -1 ) ;
        assertEquals( expected.length, actual.length ) ;
        for ( int i = 0 ; i < actual.length ; i+= 1 ) {
            assertEquals( expected[i], actual[i] );
        }

        
        input = "1::2:::3::::4";
        expected = new String[] { "1", "", "2", "", "", "3", "", "", "", "4" };

        actual = StringUtils.splitByWholeSeparatorPreserveAllTokens( input, ":", -1 ) ;
        assertEquals( expected.length, actual.length ) ;
        for ( int i = 0 ; i < actual.length ; i+= 1 ) {
            assertEquals( expected[i], actual[i] );
        }

        
        input = "1::2:::3::::4";
        expected = new String[] { "1", "2", ":3", "", "4" };

        actual = StringUtils.splitByWholeSeparatorPreserveAllTokens( input, "::", -1 ) ;
        assertEquals( expected.length, actual.length ) ;
        for ( int i = 0 ; i < actual.length ; i+= 1 ) {
            assertEquals( expected[i], actual[i] );
        }

        
        input = "1::2::3:4";
        expected = new String[] { "1", "", "2", ":3:4" };

        actual = StringUtils.splitByWholeSeparatorPreserveAllTokens( input, ":", 4 ) ;
        assertEquals( expected.length, actual.length ) ;
        for ( int i = 0 ; i < actual.length ; i+= 1 ) {
            assertEquals( expected[i], actual[i] );
        }
    }

// org.apache.commons.lang3.StringUtilsTest::testSplitPreserveAllTokens_String
    public void testSplitPreserveAllTokens_String() {
        assertArrayEquals(null, StringUtils.splitPreserveAllTokens(null));
        assertEquals(0, StringUtils.splitPreserveAllTokens("").length);
        
        String str = "abc def";
        String[] res = StringUtils.splitPreserveAllTokens(str);
        assertEquals(2, res.length);
        assertEquals("abc", res[0]);
        assertEquals("def", res[1]);
        
        str = "abc  def";
        res = StringUtils.splitPreserveAllTokens(str);
        assertEquals(3, res.length);
        assertEquals("abc", res[0]);
        assertEquals("", res[1]);
        assertEquals("def", res[2]);
        
        str = " abc ";
        res = StringUtils.splitPreserveAllTokens(str);
        assertEquals(3, res.length);
        assertEquals("", res[0]);
        assertEquals("abc", res[1]);
        assertEquals("", res[2]);
        
        str = "a b .c";
        res = StringUtils.splitPreserveAllTokens(str);
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals(".c", res[2]);
        
        str = " a b .c";
        res = StringUtils.splitPreserveAllTokens(str);
        assertEquals(4, res.length);
        assertEquals("", res[0]);
        assertEquals("a", res[1]);
        assertEquals("b", res[2]);
        assertEquals(".c", res[3]);
        
        str = "a  b  .c";
        res = StringUtils.splitPreserveAllTokens(str);
        assertEquals(5, res.length);
        assertEquals("a", res[0]);
        assertEquals("", res[1]);
        assertEquals("b", res[2]);
        assertEquals("", res[3]);
        assertEquals(".c", res[4]);
        
        str = " a  ";
        res = StringUtils.splitPreserveAllTokens(str);
        assertEquals(4, res.length);
        assertEquals("", res[0]);
        assertEquals("a", res[1]);
        assertEquals("", res[2]);
        assertEquals("", res[3]);

        str = " a  b";
        res = StringUtils.splitPreserveAllTokens(str);
        assertEquals(4, res.length);
        assertEquals("", res[0]);
        assertEquals("a", res[1]);
        assertEquals("", res[2]);
        assertEquals("b", res[3]);

        str = "a" + WHITESPACE + "b" + NON_WHITESPACE + "c";
        res = StringUtils.splitPreserveAllTokens(str);
        assertEquals(WHITESPACE.length() + 1, res.length);
        assertEquals("a", res[0]);
        for(int i = 1; i < WHITESPACE.length()-1; i++)
        {
          assertEquals("", res[i]);
        }
        assertEquals("b" + NON_WHITESPACE + "c", res[WHITESPACE.length()]);                       
    }

// org.apache.commons.lang3.StringUtilsTest::testSplitPreserveAllTokens_StringChar
    public void testSplitPreserveAllTokens_StringChar() {
        assertArrayEquals(null, StringUtils.splitPreserveAllTokens(null, '.'));
        assertEquals(0, StringUtils.splitPreserveAllTokens("", '.').length);

        String str = "a.b. c";
        String[] res = StringUtils.splitPreserveAllTokens(str, '.');
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals(" c", res[2]);
            
        str = "a.b.. c";
        res = StringUtils.splitPreserveAllTokens(str, '.');
        assertEquals(4, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals("", res[2]);
        assertEquals(" c", res[3]);

        str = ".a.";
        res = StringUtils.splitPreserveAllTokens(str, '.');
        assertEquals(3, res.length);
        assertEquals("", res[0]);
        assertEquals("a", res[1]);
        assertEquals("", res[2]);
       
        str = ".a..";
        res = StringUtils.splitPreserveAllTokens(str, '.');
        assertEquals(4, res.length);
        assertEquals("", res[0]);
        assertEquals("a", res[1]);
        assertEquals("", res[2]);
        assertEquals("", res[3]);
        
        str = "..a.";
        res = StringUtils.splitPreserveAllTokens(str, '.');
        assertEquals(4, res.length);
        assertEquals("", res[0]);
        assertEquals("", res[1]);
        assertEquals("a", res[2]);
        assertEquals("", res[3]);
        
        str = "..a";
        res = StringUtils.splitPreserveAllTokens(str, '.');
        assertEquals(3, res.length);
        assertEquals("", res[0]);
        assertEquals("", res[1]);
        assertEquals("a", res[2]);
        
        str = "a b c";
        res = StringUtils.splitPreserveAllTokens(str,' ');
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals("c", res[2]);

        str = "a  b  c";
        res = StringUtils.splitPreserveAllTokens(str,' ');
        assertEquals(5, res.length);
        assertEquals("a", res[0]);
        assertEquals("", res[1]);
        assertEquals("b", res[2]);
        assertEquals("", res[3]);
        assertEquals("c", res[4]);
        
        str = " a b c";
        res = StringUtils.splitPreserveAllTokens(str,' ');
        assertEquals(4, res.length);
        assertEquals("", res[0]);
        assertEquals("a", res[1]);
        assertEquals("b", res[2]);
        assertEquals("c", res[3]);

        str = "  a b c";
        res = StringUtils.splitPreserveAllTokens(str,' ');
        assertEquals(5, res.length);
        assertEquals("", res[0]);
        assertEquals("", res[1]);
        assertEquals("a", res[2]);
        assertEquals("b", res[3]);
        assertEquals("c", res[4]);

        str = "a b c ";
        res = StringUtils.splitPreserveAllTokens(str,' ');
        assertEquals(4, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals("c", res[2]);
        assertEquals("", res[3]);

        str = "a b c  ";
        res = StringUtils.splitPreserveAllTokens(str,' ');
        assertEquals(5, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals("c", res[2]);
        assertEquals("", res[3]);
        assertEquals("", res[3]);

        
        {
          String[] results;
          String[] expectedResults = {"a", "", "b", "c"};
          results = StringUtils.splitPreserveAllTokens("a..b.c",'.');
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
    }

// org.apache.commons.lang3.StringUtilsTest::testSplitPreserveAllTokens_StringString_StringStringInt
    public void testSplitPreserveAllTokens_StringString_StringStringInt() {
        assertArrayEquals(null, StringUtils.splitPreserveAllTokens(null, "."));
        assertArrayEquals(null, StringUtils.splitPreserveAllTokens(null, ".", 3));
        
        assertEquals(0, StringUtils.splitPreserveAllTokens("", ".").length);
        assertEquals(0, StringUtils.splitPreserveAllTokens("", ".", 3).length);
        
        innerTestSplitPreserveAllTokens('.', ".", ' ');
        innerTestSplitPreserveAllTokens('.', ".", ',');
        innerTestSplitPreserveAllTokens('.', ".,", 'x');
        for (int i = 0; i < WHITESPACE.length(); i++) {
            for (int j = 0; j < NON_WHITESPACE.length(); j++) {
                innerTestSplitPreserveAllTokens(WHITESPACE.charAt(i), null, NON_WHITESPACE.charAt(j));
                innerTestSplitPreserveAllTokens(WHITESPACE.charAt(i), String.valueOf(WHITESPACE.charAt(i)), NON_WHITESPACE.charAt(j));
            }
        }

        {
          String[] results;
          String[] expectedResults = {"ab", "de fg"};
          results = StringUtils.splitPreserveAllTokens("ab de fg", null, 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results;
          String[] expectedResults = {"ab", "  de fg"};
          results = StringUtils.splitPreserveAllTokens("ab   de fg", null, 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
        
        {
          String[] results;
          String[] expectedResults = {"ab", "::de:fg"};
          results = StringUtils.splitPreserveAllTokens("ab:::de:fg", ":", 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
        
        {
          String[] results;
          String[] expectedResults = {"ab", "", " de fg"};
          results = StringUtils.splitPreserveAllTokens("ab   de fg", null, 3);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
        
        {
          String[] results;
          String[] expectedResults = {"ab", "", "", "de fg"};
          results = StringUtils.splitPreserveAllTokens("ab   de fg", null, 4);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] expectedResults = {"ab", "cd:ef"};
          String[] results;
          results = StringUtils.splitPreserveAllTokens("ab:cd:ef",":", 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results;
          String[] expectedResults = {"ab", ":cd:ef"};
          results = StringUtils.splitPreserveAllTokens("ab::cd:ef",":", 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results;
          String[] expectedResults = {"ab", "", ":cd:ef"};
          results = StringUtils.splitPreserveAllTokens("ab:::cd:ef",":", 3);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results;
          String[] expectedResults = {"ab", "", "", "cd:ef"};
          results = StringUtils.splitPreserveAllTokens("ab:::cd:ef",":", 4);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results;
          String[] expectedResults = {"", "ab", "", "", "cd:ef"};
          results = StringUtils.splitPreserveAllTokens(":ab:::cd:ef",":", 5);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
        
        {
          String[] results;
          String[] expectedResults = {"", "", "ab", "", "", "cd:ef"};
          results = StringUtils.splitPreserveAllTokens("::ab:::cd:ef",":", 6);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
        
    }

// org.apache.commons.lang3.StringUtilsTest::testSplitByCharacterType
    public void testSplitByCharacterType() {
        assertNull(StringUtils.splitByCharacterType(null));
        assertEquals(0, StringUtils.splitByCharacterType("").length);
        
        assertTrue(ArrayUtils.isEquals(new String[] { "ab", " ", "de", " ",
        "fg" }, StringUtils.splitByCharacterType("ab de fg")));
        
        assertTrue(ArrayUtils.isEquals(new String[] { "ab", "   ", "de", " ",
        "fg" }, StringUtils.splitByCharacterType("ab   de fg")));
        
        assertTrue(ArrayUtils.isEquals(new String[] { "ab", ":", "cd", ":",
        "ef" }, StringUtils.splitByCharacterType("ab:cd:ef")));
        
        assertTrue(ArrayUtils.isEquals(new String[] { "number", "5" },
                StringUtils.splitByCharacterType("number5")));
        
        assertTrue(ArrayUtils.isEquals(new String[] { "foo", "B", "ar" },
                StringUtils.splitByCharacterType("fooBar")));
        
        assertTrue(ArrayUtils.isEquals(new String[] { "foo", "200", "B", "ar" },
                StringUtils.splitByCharacterType("foo200Bar")));
        
        assertTrue(ArrayUtils.isEquals(new String[] { "ASFR", "ules" },
                StringUtils.splitByCharacterType("ASFRules")));
    }

// org.apache.commons.lang3.StringUtilsTest::testSplitByCharacterTypeCamelCase
    public void testSplitByCharacterTypeCamelCase() {
        assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
        assertEquals(0, StringUtils.splitByCharacterTypeCamelCase("").length);

        assertTrue(ArrayUtils.isEquals(new String[] { "ab", " ", "de", " ",
                "fg" }, StringUtils.splitByCharacterTypeCamelCase("ab de fg")));

        assertTrue(ArrayUtils.isEquals(new String[] { "ab", "   ", "de", " ",
                "fg" }, StringUtils.splitByCharacterTypeCamelCase("ab   de fg")));

        assertTrue(ArrayUtils.isEquals(new String[] { "ab", ":", "cd", ":",
                "ef" }, StringUtils.splitByCharacterTypeCamelCase("ab:cd:ef")));
        
        assertTrue(ArrayUtils.isEquals(new String[] { "number", "5" },
                StringUtils.splitByCharacterTypeCamelCase("number5")));

        assertTrue(ArrayUtils.isEquals(new String[] { "foo", "Bar" },
                StringUtils.splitByCharacterTypeCamelCase("fooBar")));

        assertTrue(ArrayUtils.isEquals(new String[] { "foo", "200", "Bar" },
                StringUtils.splitByCharacterTypeCamelCase("foo200Bar")));

        assertTrue(ArrayUtils.isEquals(new String[] { "ASF", "Rules" },
                StringUtils.splitByCharacterTypeCamelCase("ASFRules")));
    }

// org.apache.commons.lang3.StringUtilsTest::testDeleteWhitespace_String
    public void testDeleteWhitespace_String() {
        assertEquals(null, StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("", StringUtils.deleteWhitespace("  \u000C  \t\t\u001F\n\n \u000B  "));
        assertEquals("", StringUtils.deleteWhitespace(StringUtilsTest.WHITESPACE));
        assertEquals(StringUtilsTest.NON_WHITESPACE, StringUtils.deleteWhitespace(StringUtilsTest.NON_WHITESPACE));
        
        
        assertEquals("\u00A0\u202F", StringUtils.deleteWhitespace("  \u00A0  \t\t\n\n \u202F  "));
        assertEquals("\u00A0\u202F", StringUtils.deleteWhitespace("\u00A0\u202F"));
        assertEquals("test", StringUtils.deleteWhitespace("\u000Bt  \t\n\u0009e\rs\n\n   \tt"));
    }

// org.apache.commons.lang3.StringUtilsTest::testLang623
    public void testLang623() {
        assertEquals("t", StringUtils.replaceChars("\u00DE", '\u00DE', 't'));
        assertEquals("t", StringUtils.replaceChars("\u00FE", '\u00FE', 't'));
    }

// org.apache.commons.lang3.StringUtilsTest::testReplace_StringStringString
    public void testReplace_StringStringString() {
        assertEquals(null, StringUtils.replace(null, null, null));
        assertEquals(null, StringUtils.replace(null, null, "any"));
        assertEquals(null, StringUtils.replace(null, "any", null));
        assertEquals(null, StringUtils.replace(null, "any", "any"));

        assertEquals("", StringUtils.replace("", null, null));
        assertEquals("", StringUtils.replace("", null, "any"));
        assertEquals("", StringUtils.replace("", "any", null));
        assertEquals("", StringUtils.replace("", "any", "any"));

        assertEquals("FOO", StringUtils.replace("FOO", "", "any"));
        assertEquals("FOO", StringUtils.replace("FOO", null, "any"));
        assertEquals("FOO", StringUtils.replace("FOO", "F", null));
        assertEquals("FOO", StringUtils.replace("FOO", null, null));

        assertEquals("", StringUtils.replace("foofoofoo", "foo", ""));
        assertEquals("barbarbar", StringUtils.replace("foofoofoo", "foo", "bar"));
        assertEquals("farfarfar", StringUtils.replace("foofoofoo", "oo", "ar"));
    }

// org.apache.commons.lang3.StringUtilsTest::testReplacePattern
    public void testReplacePattern() {
        assertEquals("X", StringUtils.replacePattern("<A>\nxy\n</A>", "<A>.*</A>", "X"));
    }

// org.apache.commons.lang3.StringUtilsTest::testRemovePattern
    public void testRemovePattern() {
        assertEquals("", StringUtils.removePattern("<A>x\\ny</A>", "<A>.*</A>"));
    }

// org.apache.commons.lang3.StringUtilsTest::testReplace_StringStringStringInt
    public void testReplace_StringStringStringInt() {
        assertEquals(null, StringUtils.replace(null, null, null, 2));
        assertEquals(null, StringUtils.replace(null, null, "any", 2));
        assertEquals(null, StringUtils.replace(null, "any", null, 2));
        assertEquals(null, StringUtils.replace(null, "any", "any", 2));

        assertEquals("", StringUtils.replace("", null, null, 2));
        assertEquals("", StringUtils.replace("", null, "any", 2));
        assertEquals("", StringUtils.replace("", "any", null, 2));
        assertEquals("", StringUtils.replace("", "any", "any", 2));
        
        String str = new String(new char[] {'o', 'o', 'f', 'o', 'o'});
        assertSame(str, StringUtils.replace(str, "x", "", -1));
        
        assertEquals("f", StringUtils.replace("oofoo", "o", "", -1));
        assertEquals("oofoo", StringUtils.replace("oofoo", "o", "", 0));
        assertEquals("ofoo", StringUtils.replace("oofoo", "o", "", 1));
        assertEquals("foo", StringUtils.replace("oofoo", "o", "", 2));
        assertEquals("fo", StringUtils.replace("oofoo", "o", "", 3));
        assertEquals("f", StringUtils.replace("oofoo", "o", "", 4));
        
        assertEquals("f", StringUtils.replace("oofoo", "o", "", -5));
        assertEquals("f", StringUtils.replace("oofoo", "o", "", 1000));
    }

// org.apache.commons.lang3.StringUtilsTest::testReplaceOnce_StringStringString
    public void testReplaceOnce_StringStringString() {
        assertEquals(null, StringUtils.replaceOnce(null, null, null));
        assertEquals(null, StringUtils.replaceOnce(null, null, "any"));
        assertEquals(null, StringUtils.replaceOnce(null, "any", null));
        assertEquals(null, StringUtils.replaceOnce(null, "any", "any"));

        assertEquals("", StringUtils.replaceOnce("", null, null));
        assertEquals("", StringUtils.replaceOnce("", null, "any"));
        assertEquals("", StringUtils.replaceOnce("", "any", null));
        assertEquals("", StringUtils.replaceOnce("", "any", "any"));

        assertEquals("FOO", StringUtils.replaceOnce("FOO", "", "any"));
        assertEquals("FOO", StringUtils.replaceOnce("FOO", null, "any"));
        assertEquals("FOO", StringUtils.replaceOnce("FOO", "F", null));
        assertEquals("FOO", StringUtils.replaceOnce("FOO", null, null));

        assertEquals("foofoo", StringUtils.replaceOnce("foofoofoo", "foo", ""));
    }

// org.apache.commons.lang3.StringUtilsTest::testReplace_StringStringArrayStringArray
    public void testReplace_StringStringArrayStringArray() {
        
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals(StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}),"");
        assertEquals(StringUtils.replaceEach("aba", null, null),"aba");
        assertEquals(StringUtils.replaceEach("aba", new String[0], null),"aba");
        assertEquals(StringUtils.replaceEach("aba", null, new String[0]),"aba");
        assertEquals(StringUtils.replaceEach("aba", new String[]{"a"}, null),"aba");

        assertEquals(StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}),"b");
        assertEquals(StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}),"aba");
        assertEquals(StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}),"wcte");
        assertEquals(StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}),"dcte");
        

        assertEquals("bcc", StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"b", "c"}));
        assertEquals("q651.506bera", StringUtils.replaceEach("d216.102oren",
            new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", 
                "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", 
                "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", 
                "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9"},
            new String[]{"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "a", 
                "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "N", "O", "P", "Q", 
                "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "A", "B", "C", "D", "E", "F", "G", 
                "H", "I", "J", "K", "L", "M", "5", "6", "7", "8", "9", "1", "2", "3", "4"}));

        
        assertEquals(StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{null}),"aba");
        assertEquals(StringUtils.replaceEach("aba", new String[]{"a", "b"}, new String[]{"c", null}),"cbc");
    }

// org.apache.commons.lang3.StringUtilsTest::testReplace_StringStringArrayStringArrayBoolean
    public void testReplace_StringStringArrayStringArrayBoolean() {
        
        assertNull(StringUtils.replaceEachRepeatedly(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals(StringUtils.replaceEachRepeatedly("", new String[]{"a"}, new String[]{"b"}),"");
        assertEquals(StringUtils.replaceEachRepeatedly("aba", null, null),"aba");
        assertEquals(StringUtils.replaceEachRepeatedly("aba", new String[0], null),"aba");
        assertEquals(StringUtils.replaceEachRepeatedly("aba", null, new String[0]),"aba");
        assertEquals(StringUtils.replaceEachRepeatedly("aba", new String[0], null),"aba");

        assertEquals(StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, new String[]{""}),"b");
        assertEquals(StringUtils.replaceEachRepeatedly("aba", new String[]{null}, new String[]{"a"}),"aba");
        assertEquals(StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}),"wcte");
        assertEquals(StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}),"tcte");

        try {
            StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"});
            fail("Should be a circular reference");
        } catch (IllegalStateException e) {}

        
    }

// org.apache.commons.lang3.StringUtilsTest::testReplaceChars_StringCharChar
    public void testReplaceChars_StringCharChar() {
        assertEquals(null, StringUtils.replaceChars(null, 'b', 'z'));
        assertEquals("", StringUtils.replaceChars("", 'b', 'z'));
        assertEquals("azcza", StringUtils.replaceChars("abcba", 'b', 'z'));
        assertEquals("abcba", StringUtils.replaceChars("abcba", 'x', 'z'));
    }

// org.apache.commons.lang3.StringUtilsTest::testReplaceChars_StringStringString
    public void testReplaceChars_StringStringString() {
        assertEquals(null, StringUtils.replaceChars(null, null, null));
        assertEquals(null, StringUtils.replaceChars(null, "", null));
        assertEquals(null, StringUtils.replaceChars(null, "a", null));
        assertEquals(null, StringUtils.replaceChars(null, null, ""));
        assertEquals(null, StringUtils.replaceChars(null, null, "x"));
        
        assertEquals("", StringUtils.replaceChars("", null, null));
        assertEquals("", StringUtils.replaceChars("", "", null));
        assertEquals("", StringUtils.replaceChars("", "a", null));
        assertEquals("", StringUtils.replaceChars("", null, ""));
        assertEquals("", StringUtils.replaceChars("", null, "x"));

        assertEquals("abc", StringUtils.replaceChars("abc", null, null));
        assertEquals("abc", StringUtils.replaceChars("abc", null, ""));
        assertEquals("abc", StringUtils.replaceChars("abc", null, "x"));
        
        assertEquals("abc", StringUtils.replaceChars("abc", "", null));
        assertEquals("abc", StringUtils.replaceChars("abc", "", ""));
        assertEquals("abc", StringUtils.replaceChars("abc", "", "x"));
        
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", ""));
        assertEquals("axc", StringUtils.replaceChars("abc", "b", "x"));
        
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yzx"));
        
        assertEquals("abcba", StringUtils.replaceChars("abcba", "z", "w"));
        assertSame("abcba", StringUtils.replaceChars("abcba", "z", "w"));
        
        
        assertEquals("jelly", StringUtils.replaceChars("hello", "ho", "jy"));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yzx"));
        
        
        assertEquals("bcc", StringUtils.replaceChars("abc", "ab", "bc"));
        assertEquals("q651.506bera", StringUtils.replaceChars("d216.102oren",
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789",
            "nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM567891234"));
    }

// org.apache.commons.lang3.StringUtilsTest::testOverlay_StringStringIntInt
    public void testOverlay_StringStringIntInt() {
        assertEquals(null, StringUtils.overlay(null, null, 2, 4));
        assertEquals(null, StringUtils.overlay(null, null, -2, -4));
        
        assertEquals("", StringUtils.overlay("", null, 0, 0));
        assertEquals("", StringUtils.overlay("", "", 0, 0));
        assertEquals("zzzz", StringUtils.overlay("", "zzzz", 0, 0));
        assertEquals("zzzz", StringUtils.overlay("", "zzzz", 2, 4));
        assertEquals("zzzz", StringUtils.overlay("", "zzzz", -2, -4));
        
        assertEquals("abef", StringUtils.overlay("abcdef", null, 2, 4));
        assertEquals("abef", StringUtils.overlay("abcdef", null, 4, 2));
        assertEquals("abef", StringUtils.overlay("abcdef", "", 2, 4));
        assertEquals("abef", StringUtils.overlay("abcdef", "", 4, 2));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", 4, -1));
        assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -2, -1));
        assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -1, -2));
        assertEquals("abcdzzzz", StringUtils.overlay("abcdef", "zzzz", 4, 10));
        assertEquals("abcdzzzz", StringUtils.overlay("abcdef", "zzzz", 10, 4));
        assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 8, 10));
        assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 10, 8));
    }

// org.apache.commons.lang3.StringUtilsTest::testRepeat_StringInt
    public void testRepeat_StringInt() {
        assertEquals(null, StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("ab", 0));
        assertEquals("", StringUtils.repeat("", 3));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("ababab", StringUtils.repeat("ab", 3));
        assertEquals("abcabcabc", StringUtils.repeat("abc", 3));
        String str = StringUtils.repeat("a", 10000);  
        assertEquals(10000, str.length());
        assertTrue(StringUtils.containsOnly(str, new char[] {'a'}));
    }

// org.apache.commons.lang3.StringUtilsTest::testRepeat_StringStringInt
    public void testRepeat_StringStringInt() {
        assertEquals(null, StringUtils.repeat(null, null, 2));
        assertEquals(null, StringUtils.repeat(null, "x", 2));
        assertEquals("", StringUtils.repeat("", null, 2));

        assertEquals("", StringUtils.repeat("ab", "", 0));
        assertEquals("", StringUtils.repeat("", "", 2));

        assertEquals("xx", StringUtils.repeat("", "x", 3));

        assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));
    }

// org.apache.commons.lang3.StringUtilsTest::testChop
    public void testChop() {

        String[][] chopCases = {
            { FOO_UNCAP + "\r\n", FOO_UNCAP } ,
            { FOO_UNCAP + "\n" , FOO_UNCAP } ,
            { FOO_UNCAP + "\r", FOO_UNCAP },
            { FOO_UNCAP + " \r", FOO_UNCAP + " " },
            { "foo", "fo"},
            { "foo\nfoo", "foo\nfo" },
            { "\n", "" },
            { "\r", "" },
            { "\r\n", "" },
            { null, null },
            { "", "" },
            { "a", "" },
        };
        for (String[] chopCase : chopCases) {
            String original = chopCase[0];
            String expectedResult = chopCase[1];
            assertEquals("chop(String) failed",
                    expectedResult, StringUtils.chop(original));
        }
    }

// org.apache.commons.lang3.StringUtilsTest::testChomp
    public void testChomp() {

        String[][] chompCases = {
            { FOO_UNCAP + "\r\n", FOO_UNCAP },
            { FOO_UNCAP + "\n" , FOO_UNCAP },
            { FOO_UNCAP + "\r", FOO_UNCAP },
            { FOO_UNCAP + " \r", FOO_UNCAP + " " },
            { FOO_UNCAP, FOO_UNCAP },
            { FOO_UNCAP + "\n\n", FOO_UNCAP + "\n"},
            { FOO_UNCAP + "\r\n\r\n", FOO_UNCAP + "\r\n" },
            { "foo\nfoo", "foo\nfoo" },
            { "foo\n\rfoo", "foo\n\rfoo" },
            { "\n", "" },
            { "\r", "" },
            { "a", "a" },
            { "\r\n", "" },
            { "", "" },
            { null, null },
            { FOO_UNCAP + "\n\r", FOO_UNCAP + "\n"}
        };
        for (String[] chompCase : chompCases) {
            String original = chompCase[0];
            String expectedResult = chompCase[1];
            assertEquals("chomp(String) failed",
                    expectedResult, StringUtils.chomp(original));
        }

        assertEquals("chomp(String, String) failed",
                "foo", StringUtils.chomp("foobar", "bar"));
        assertEquals("chomp(String, String) failed",
                "foobar", StringUtils.chomp("foobar", "baz"));
        assertEquals("chomp(String, String) failed",
                "foo", StringUtils.chomp("foo", "foooo"));
        assertEquals("chomp(String, String) failed",
                "foobar", StringUtils.chomp("foobar", ""));
        assertEquals("chomp(String, String) failed",
                "foobar", StringUtils.chomp("foobar", null));
        assertEquals("chomp(String, String) failed",
                "", StringUtils.chomp("", "foo"));
        assertEquals("chomp(String, String) failed",
                "", StringUtils.chomp("", null));
        assertEquals("chomp(String, String) failed",
                "", StringUtils.chomp("", ""));
        assertEquals("chomp(String, String) failed",
                null, StringUtils.chomp(null, "foo"));
        assertEquals("chomp(String, String) failed",
                null, StringUtils.chomp(null, null));
        assertEquals("chomp(String, String) failed",
                null, StringUtils.chomp(null, ""));
        assertEquals("chomp(String, String) failed",
                "", StringUtils.chomp("foo", "foo"));
        assertEquals("chomp(String, String) failed",
                " ", StringUtils.chomp(" foo", "foo"));
        assertEquals("chomp(String, String) failed",
                "foo ", StringUtils.chomp("foo ", "foo"));
    }

// org.apache.commons.lang3.StringUtilsTest::testRightPad_StringInt
    public void testRightPad_StringInt() {
        assertEquals(null, StringUtils.rightPad(null, 5));
        assertEquals("     ", StringUtils.rightPad("", 5));
        assertEquals("abc  ", StringUtils.rightPad("abc", 5));
        assertEquals("abc", StringUtils.rightPad("abc", 2));
        assertEquals("abc", StringUtils.rightPad("abc", -1));
    }

// org.apache.commons.lang3.StringUtilsTest::testRightPad_StringIntChar
    public void testRightPad_StringIntChar() {
        assertEquals(null, StringUtils.rightPad(null, 5, ' '));
        assertEquals("     ", StringUtils.rightPad("", 5, ' '));
        assertEquals("abc  ", StringUtils.rightPad("abc", 5, ' '));
        assertEquals("abc", StringUtils.rightPad("abc", 2, ' '));
        assertEquals("abc", StringUtils.rightPad("abc", -1, ' '));
        assertEquals("abcxx", StringUtils.rightPad("abc", 5, 'x'));
        String str = StringUtils.rightPad("aaa", 10000, 'a');  
        assertEquals(10000, str.length());
        assertTrue(StringUtils.containsOnly(str, new char[] {'a'}));
    }

// org.apache.commons.lang3.StringUtilsTest::testRightPad_StringIntString
    public void testRightPad_StringIntString() {
        assertEquals(null, StringUtils.rightPad(null, 5, "-+"));
        assertEquals("     ", StringUtils.rightPad("", 5, " "));
        assertEquals(null, StringUtils.rightPad(null, 8, null));
        assertEquals("abc-+-+", StringUtils.rightPad("abc", 7, "-+"));
        assertEquals("abc-+~", StringUtils.rightPad("abc", 6, "-+~"));
        assertEquals("abc-+", StringUtils.rightPad("abc", 5, "-+~"));
        assertEquals("abc", StringUtils.rightPad("abc", 2, " "));
        assertEquals("abc", StringUtils.rightPad("abc", -1, " "));
        assertEquals("abc  ", StringUtils.rightPad("abc", 5, null));
        assertEquals("abc  ", StringUtils.rightPad("abc", 5, ""));
    }

// org.apache.commons.lang3.StringUtilsTest::testLeftPad_StringInt
    public void testLeftPad_StringInt() {
        assertEquals(null, StringUtils.leftPad(null, 5));
        assertEquals("     ", StringUtils.leftPad("", 5));
        assertEquals("  abc", StringUtils.leftPad("abc", 5));
        assertEquals("abc", StringUtils.leftPad("abc", 2));
    }

// org.apache.commons.lang3.StringUtilsTest::testLeftPad_StringIntChar
    public void testLeftPad_StringIntChar() {
        assertEquals(null, StringUtils.leftPad(null, 5, ' '));
        assertEquals("     ", StringUtils.leftPad("", 5, ' '));
        assertEquals("  abc", StringUtils.leftPad("abc", 5, ' '));
        assertEquals("xxabc", StringUtils.leftPad("abc", 5, 'x'));
        assertEquals("\uffff\uffffabc", StringUtils.leftPad("abc", 5, '\uffff'));
        assertEquals("abc", StringUtils.leftPad("abc", 2, ' '));
        String str = StringUtils.leftPad("aaa", 10000, 'a');  
        assertEquals(10000, str.length());
        assertTrue(StringUtils.containsOnly(str, new char[] {'a'}));
    }

// org.apache.commons.lang3.StringUtilsTest::testLeftPad_StringIntString
    public void testLeftPad_StringIntString() {
        assertEquals(null, StringUtils.leftPad(null, 5, "-+"));
        assertEquals(null, StringUtils.leftPad(null, 5, null));
        assertEquals("     ", StringUtils.leftPad("", 5, " "));
        assertEquals("-+-+abc", StringUtils.leftPad("abc", 7, "-+"));
        assertEquals("-+~abc", StringUtils.leftPad("abc", 6, "-+~"));
        assertEquals("-+abc", StringUtils.leftPad("abc", 5, "-+~"));
        assertEquals("abc", StringUtils.leftPad("abc", 2, " "));
        assertEquals("abc", StringUtils.leftPad("abc", -1, " "));
        assertEquals("  abc", StringUtils.leftPad("abc", 5, null));
        assertEquals("  abc", StringUtils.leftPad("abc", 5, ""));
    }

// org.apache.commons.lang3.StringUtilsTest::testLengthString
    public void testLengthString() {
        assertEquals(0, StringUtils.length(null));
        assertEquals(0, StringUtils.length(""));
        assertEquals(0, StringUtils.length(StringUtils.EMPTY));
        assertEquals(1, StringUtils.length("A"));
        assertEquals(1, StringUtils.length(" "));
        assertEquals(8, StringUtils.length("ABCDEFGH"));
    }

// org.apache.commons.lang3.StringUtilsTest::testLengthStringBuffer
    public void testLengthStringBuffer() {
        assertEquals(0, StringUtils.length(new StringBuffer("")));
        assertEquals(0, StringUtils.length(new StringBuffer(StringUtils.EMPTY)));
        assertEquals(1, StringUtils.length(new StringBuffer("A")));
        assertEquals(1, StringUtils.length(new StringBuffer(" ")));
        assertEquals(8, StringUtils.length(new StringBuffer("ABCDEFGH")));
    }

// org.apache.commons.lang3.StringUtilsTest::testLengthStringBuilder
    public void testLengthStringBuilder() {
        assertEquals(0, StringUtils.length(new StringBuilder("")));
        assertEquals(0, StringUtils.length(new StringBuilder(StringUtils.EMPTY)));
        assertEquals(1, StringUtils.length(new StringBuilder("A")));
        assertEquals(1, StringUtils.length(new StringBuilder(" ")));
        assertEquals(8, StringUtils.length(new StringBuilder("ABCDEFGH")));
    }

// org.apache.commons.lang3.StringUtilsTest::testLength_CharBuffer
    public void testLength_CharBuffer() {
        assertEquals(0, StringUtils.length(CharBuffer.wrap("")));
        assertEquals(1, StringUtils.length(CharBuffer.wrap("A")));
        assertEquals(1, StringUtils.length(CharBuffer.wrap(" ")));
        assertEquals(8, StringUtils.length(CharBuffer.wrap("ABCDEFGH")));
    }

// org.apache.commons.lang3.StringUtilsTest::testCenter_StringInt
    public void testCenter_StringInt() {
        assertEquals(null, StringUtils.center(null, -1));
        assertEquals(null, StringUtils.center(null, 4));
        assertEquals("    ", StringUtils.center("", 4));
        assertEquals("ab", StringUtils.center("ab", 0));
        assertEquals("ab", StringUtils.center("ab", -1));
        assertEquals("ab", StringUtils.center("ab", 1));
        assertEquals("    ", StringUtils.center("", 4));
        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("abcd", StringUtils.center("abcd", 2));
        assertEquals(" a  ", StringUtils.center("a", 4));
        assertEquals("  a  ", StringUtils.center("a", 5));
    }

// org.apache.commons.lang3.StringUtilsTest::testCenter_StringIntChar
    public void testCenter_StringIntChar() {
        assertEquals(null, StringUtils.center(null, -1, ' '));
        assertEquals(null, StringUtils.center(null, 4, ' '));
        assertEquals("    ", StringUtils.center("", 4, ' '));
        assertEquals("ab", StringUtils.center("ab", 0, ' '));
        assertEquals("ab", StringUtils.center("ab", -1, ' '));
        assertEquals("ab", StringUtils.center("ab", 1, ' '));
        assertEquals("    ", StringUtils.center("", 4, ' '));
        assertEquals(" ab ", StringUtils.center("ab", 4, ' '));
        assertEquals("abcd", StringUtils.center("abcd", 2, ' '));
        assertEquals(" a  ", StringUtils.center("a", 4, ' '));
        assertEquals("  a  ", StringUtils.center("a", 5, ' '));
        assertEquals("xxaxx", StringUtils.center("a", 5, 'x'));
    }

// org.apache.commons.lang3.StringUtilsTest::testCenter_StringIntString
    public void testCenter_StringIntString() {
        assertEquals(null, StringUtils.center(null, 4, null));
        assertEquals(null, StringUtils.center(null, -1, " "));
        assertEquals(null, StringUtils.center(null, 4, " "));
        assertEquals("    ", StringUtils.center("", 4, " "));
        assertEquals("ab", StringUtils.center("ab", 0, " "));
        assertEquals("ab", StringUtils.center("ab", -1, " "));
        assertEquals("ab", StringUtils.center("ab", 1, " "));
        assertEquals("    ", StringUtils.center("", 4, " "));
        assertEquals(" ab ", StringUtils.center("ab", 4, " "));
        assertEquals("abcd", StringUtils.center("abcd", 2, " "));
        assertEquals(" a  ", StringUtils.center("a", 4, " "));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("yzyayzy", StringUtils.center("a", 7, "yz"));
        assertEquals("  abc  ", StringUtils.center("abc", 7, null));
        assertEquals("  abc  ", StringUtils.center("abc", 7, ""));
    }

// org.apache.commons.lang3.StringUtilsTest::testReverse_String
    public void testReverse_String() {
        assertEquals(null, StringUtils.reverse(null) );
        assertEquals("", StringUtils.reverse("") );
        assertEquals("sdrawkcab", StringUtils.reverse("backwards") );
    }

// org.apache.commons.lang3.StringUtilsTest::testReverseDelimited_StringChar
    public void testReverseDelimited_StringChar() {
        assertEquals(null, StringUtils.reverseDelimited(null, '.') );
        assertEquals("", StringUtils.reverseDelimited("", '.') );
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.') );
        assertEquals("a b c", StringUtils.reverseDelimited("a b c", '.') );
        assertEquals("", StringUtils.reverseDelimited("", '.') );
    }

// org.apache.commons.lang3.StringUtilsTest::testDefault_String
    public void testDefault_String() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("", StringUtils.defaultString(""));
        assertEquals("abc", StringUtils.defaultString("abc"));
    }

// org.apache.commons.lang3.StringUtilsTest::testDefault_StringString
    public void testDefault_StringString() {
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("", StringUtils.defaultString("", "NULL"));
        assertEquals("abc", StringUtils.defaultString("abc", "NULL"));
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfEmpty_StringString
    public void testDefaultIfEmpty_StringString() {
        assertEquals("NULL", StringUtils.defaultIfEmpty(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfEmpty("", "NULL"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "NULL"));
        assertNull(StringUtils.defaultIfEmpty("", null));
        
        String s = StringUtils.defaultIfEmpty("abc", "NULL");
        assertEquals("abc", s);
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfBlank_StringString
    public void testDefaultIfBlank_StringString() {
        assertEquals("NULL", StringUtils.defaultIfBlank(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfBlank("", "NULL"));
        assertEquals("NULL", StringUtils.defaultIfBlank(" ", "NULL"));
        assertEquals("abc", StringUtils.defaultIfBlank("abc", "NULL"));
        assertNull(StringUtils.defaultIfBlank("", null));
        
        String s = StringUtils.defaultIfBlank("abc", "NULL");
        assertEquals("abc", s);
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfEmpty_StringBuilders
    public void testDefaultIfEmpty_StringBuilders() {
        assertEquals("NULL", StringUtils.defaultIfEmpty(new StringBuilder(""), new StringBuilder("NULL")).toString());
        assertEquals("abc", StringUtils.defaultIfEmpty(new StringBuilder("abc"), new StringBuilder("NULL")).toString());
        assertNull(StringUtils.defaultIfEmpty(new StringBuilder(""), null));
        
        StringBuilder s = StringUtils.defaultIfEmpty(new StringBuilder("abc"), new StringBuilder("NULL"));
        assertEquals("abc", s.toString());
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfBlank_StringBuilders
    public void testDefaultIfBlank_StringBuilders() {
        assertEquals("NULL", StringUtils.defaultIfBlank(new StringBuilder(""), new StringBuilder("NULL")).toString());
        assertEquals("NULL", StringUtils.defaultIfBlank(new StringBuilder(" "), new StringBuilder("NULL")).toString());
        assertEquals("abc", StringUtils.defaultIfBlank(new StringBuilder("abc"), new StringBuilder("NULL")).toString());
        assertNull(StringUtils.defaultIfBlank(new StringBuilder(""), null));
        
        StringBuilder s = StringUtils.defaultIfBlank(new StringBuilder("abc"), new StringBuilder("NULL"));
        assertEquals("abc", s.toString());
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfEmpty_StringBuffers
    public void testDefaultIfEmpty_StringBuffers() {
        assertEquals("NULL", StringUtils.defaultIfEmpty(new StringBuffer(""), new StringBuffer("NULL")).toString());
        assertEquals("abc", StringUtils.defaultIfEmpty(new StringBuffer("abc"), new StringBuffer("NULL")).toString());
        assertNull(StringUtils.defaultIfEmpty(new StringBuffer(""), null));
        
        StringBuffer s = StringUtils.defaultIfEmpty(new StringBuffer("abc"), new StringBuffer("NULL"));
        assertEquals("abc", s.toString());
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfBlank_StringBuffers
    public void testDefaultIfBlank_StringBuffers() {
        assertEquals("NULL", StringUtils.defaultIfBlank(new StringBuffer(""), new StringBuffer("NULL")).toString());
        assertEquals("NULL", StringUtils.defaultIfBlank(new StringBuffer(" "), new StringBuffer("NULL")).toString());
        assertEquals("abc", StringUtils.defaultIfBlank(new StringBuffer("abc"), new StringBuffer("NULL")).toString());
        assertNull(StringUtils.defaultIfBlank(new StringBuffer(""), null));
        
        StringBuffer s = StringUtils.defaultIfBlank(new StringBuffer("abc"), new StringBuffer("NULL"));
        assertEquals("abc", s.toString());
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfEmpty_CharBuffers
    public void testDefaultIfEmpty_CharBuffers() {
        assertEquals("NULL", StringUtils.defaultIfEmpty(CharBuffer.wrap(""), CharBuffer.wrap("NULL")).toString());
        assertEquals("abc", StringUtils.defaultIfEmpty(CharBuffer.wrap("abc"), CharBuffer.wrap("NULL")).toString());
        assertNull(StringUtils.defaultIfEmpty(CharBuffer.wrap(""), null));
        
        CharBuffer s = StringUtils.defaultIfEmpty(CharBuffer.wrap("abc"), CharBuffer.wrap("NULL"));
        assertEquals("abc", s.toString());
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfBlank_CharBuffers
    public void testDefaultIfBlank_CharBuffers() {
        assertEquals("NULL", StringUtils.defaultIfBlank(CharBuffer.wrap(""), CharBuffer.wrap("NULL")).toString());
        assertEquals("NULL", StringUtils.defaultIfBlank(CharBuffer.wrap(" "), CharBuffer.wrap("NULL")).toString());
        assertEquals("abc", StringUtils.defaultIfBlank(CharBuffer.wrap("abc"), CharBuffer.wrap("NULL")).toString());
        assertNull(StringUtils.defaultIfBlank(CharBuffer.wrap(""), null));
        
        CharBuffer s = StringUtils.defaultIfBlank(CharBuffer.wrap("abc"), CharBuffer.wrap("NULL"));
        assertEquals("abc", s.toString());
    }

// org.apache.commons.lang3.StringUtilsTest::testAbbreviate_StringInt
    public void testAbbreviate_StringInt() {
        assertEquals(null, StringUtils.abbreviate(null, 10));
        assertEquals("", StringUtils.abbreviate("", 10));
        assertEquals("short", StringUtils.abbreviate("short", 10));
        assertEquals("Now is ...", StringUtils.abbreviate("Now is the time for all good men to come to the aid of their party.", 10));

        String raspberry = "raspberry peach";
        assertEquals("raspberry p...", StringUtils.abbreviate(raspberry, 14));
        assertEquals("raspberry peach", StringUtils.abbreviate("raspberry peach", 15));
        assertEquals("raspberry peach", StringUtils.abbreviate("raspberry peach", 16));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 8));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        
        try {
            @SuppressWarnings("unused")
            String res = StringUtils.abbreviate("abc", 3);
            fail("StringUtils.abbreviate expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
                
        }              
    }

// org.apache.commons.lang3.StringUtilsTest::testAbbreviate_StringIntInt
    public void testAbbreviate_StringIntInt() {
        assertEquals(null, StringUtils.abbreviate(null, 10, 12));
        assertEquals("", StringUtils.abbreviate("", 0, 10));
        assertEquals("", StringUtils.abbreviate("", 2, 10));
        
        try {
            @SuppressWarnings("unused")
            String res = StringUtils.abbreviate("abcdefghij", 0, 3);
            fail("StringUtils.abbreviate expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
                
        }      
        try {
            @SuppressWarnings("unused")
            String res = StringUtils.abbreviate("abcdefghij", 5, 6);
            fail("StringUtils.abbreviate expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
                
        }      
        

        String raspberry = "raspberry peach";
        assertEquals("raspberry peach", StringUtils.abbreviate(raspberry, 11, 15));

        assertEquals(null, StringUtils.abbreviate(null, 7, 14));
        assertAbbreviateWithOffset("abcdefg...", -1, 10);
        assertAbbreviateWithOffset("abcdefg...", 0, 10);
        assertAbbreviateWithOffset("abcdefg...", 1, 10);
        assertAbbreviateWithOffset("abcdefg...", 2, 10);
        assertAbbreviateWithOffset("abcdefg...", 3, 10);
        assertAbbreviateWithOffset("abcdefg...", 4, 10);
        assertAbbreviateWithOffset("...fghi...", 5, 10);
        assertAbbreviateWithOffset("...ghij...", 6, 10);
        assertAbbreviateWithOffset("...hijk...", 7, 10);
        assertAbbreviateWithOffset("...ijklmno", 8, 10);
        assertAbbreviateWithOffset("...ijklmno", 9, 10);
        assertAbbreviateWithOffset("...ijklmno", 10, 10);
        assertAbbreviateWithOffset("...ijklmno", 10, 10);
        assertAbbreviateWithOffset("...ijklmno", 11, 10);
        assertAbbreviateWithOffset("...ijklmno", 12, 10);
        assertAbbreviateWithOffset("...ijklmno", 13, 10);
        assertAbbreviateWithOffset("...ijklmno", 14, 10);
        assertAbbreviateWithOffset("...ijklmno", 15, 10);
        assertAbbreviateWithOffset("...ijklmno", 16, 10);
        assertAbbreviateWithOffset("...ijklmno", Integer.MAX_VALUE, 10);
    }

// org.apache.commons.lang3.StringUtilsTest::testAbbreviateMiddle
    public void testAbbreviateMiddle() {
        
        assertNull( StringUtils.abbreviateMiddle(null, null, 0) );
        assertEquals( "abc", StringUtils.abbreviateMiddle("abc", null, 0) );
        assertEquals( "abc", StringUtils.abbreviateMiddle("abc", ".", 0) );
        assertEquals( "abc", StringUtils.abbreviateMiddle("abc", ".", 3) );
        assertEquals( "ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4) );

        
        assertEquals( 
            "A very long text with un...f the text is complete.",
            StringUtils.abbreviateMiddle(
                "A very long text with unimportant stuff in the middle but interesting start and " +
                "end to see if the text is complete.", "...", 50) );

        
        String longText = "Start text" + StringUtils.repeat("x", 10000) + "Close text";
        assertEquals( 
            "Start text->Close text",
            StringUtils.abbreviateMiddle( longText, "->", 22 ) );

        
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", -1));

        
        
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 1));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 2));

        
        assertEquals("a", StringUtils.abbreviateMiddle("a", ".", 1));

        
        assertEquals("a.d", StringUtils.abbreviateMiddle("abcd", ".", 3));

        
        assertEquals("a..f", StringUtils.abbreviateMiddle("abcdef", "..", 4));
        assertEquals("ab.ef", StringUtils.abbreviateMiddle("abcdef", ".", 5));
    }

// org.apache.commons.lang3.StringUtilsTest::testDifference_StringString
    public void testDifference_StringString() {
        assertEquals(null, StringUtils.difference(null, null));
        assertEquals("", StringUtils.difference("", ""));
        assertEquals("abc", StringUtils.difference("", "abc"));
        assertEquals("", StringUtils.difference("abc", ""));
        assertEquals("i am a robot", StringUtils.difference(null, "i am a robot"));
        assertEquals("i am a machine", StringUtils.difference("i am a machine", null));
        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals("", StringUtils.difference("abc", "abc"));
        assertEquals("you are a robot", StringUtils.difference("i am a robot", "you are a robot"));
    }

// org.apache.commons.lang3.StringUtilsTest::testDifferenceAt_StringString
    public void testDifferenceAt_StringString() {
        assertEquals(-1, StringUtils.indexOfDifference(null, null));
        assertEquals(0, StringUtils.indexOfDifference(null, "i am a robot"));
        assertEquals(-1, StringUtils.indexOfDifference("", ""));
        assertEquals(0, StringUtils.indexOfDifference("", "abc"));
        assertEquals(0, StringUtils.indexOfDifference("abc", ""));
        assertEquals(0, StringUtils.indexOfDifference("i am a machine", null));
        assertEquals(7, StringUtils.indexOfDifference("i am a machine", "i am a robot"));
        assertEquals(-1, StringUtils.indexOfDifference("foo", "foo"));
        assertEquals(0, StringUtils.indexOfDifference("i am a robot", "you are a robot"));
        
    }

// org.apache.commons.lang3.StringUtilsTest::testGetLevenshteinDistance_StringString
    public void testGetLevenshteinDistance_StringString() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", "") );
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a") );
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "") );
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog") );
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant") );
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo") );
        assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant") );
        assertEquals(8, StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz") );
        assertEquals(8, StringUtils.getLevenshteinDistance("zzzzzzzz", "hippo") );
        assertEquals(1, StringUtils.getLevenshteinDistance("hello", "hallo") );
        try {
            @SuppressWarnings("unused")
            int d = StringUtils.getLevenshteinDistance("a", null);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            @SuppressWarnings("unused")
            int d = StringUtils.getLevenshteinDistance(null, "a");
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.lang3.StringUtilsTest::testGetLevenshteinDistance_StringStringInt
    public void testGetLevenshteinDistance_StringStringInt() {
        
        assertEquals(0, StringUtils.getLevenshteinDistance("", "", 0));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "", 8));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "", 7));
        assertEquals(-1, StringUtils.getLevenshteinDistance("aaapppp", "", 6));

        
        assertEquals(-1, StringUtils.getLevenshteinDistance("b", "a", 0));
        assertEquals(-1, StringUtils.getLevenshteinDistance("a", "b", 0));
    
        
        assertEquals(0, StringUtils.getLevenshteinDistance("aa", "aa", 0));
        assertEquals(0, StringUtils.getLevenshteinDistance("aa", "aa", 2));

        
        assertEquals(-1, StringUtils.getLevenshteinDistance("aaa", "bbb", 2));
        assertEquals(3, StringUtils.getLevenshteinDistance("aaa", "bbb", 3));
    
        
        assertEquals(6, StringUtils.getLevenshteinDistance("aaaaaa", "b", 10));

        
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "b", 8));
        assertEquals(3, StringUtils.getLevenshteinDistance("a", "bbb", 4));
    
        
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "b", 7));
        assertEquals(3, StringUtils.getLevenshteinDistance("a", "bbb", 3));

        
        assertEquals(-1, StringUtils.getLevenshteinDistance("a", "bbb", 2));
        assertEquals(-1, StringUtils.getLevenshteinDistance("bbb", "a", 2));
        assertEquals(-1, StringUtils.getLevenshteinDistance("aaapppp", "b", 6));

        
        assertEquals(-1, StringUtils.getLevenshteinDistance("a", "bbb", 1));
        assertEquals(-1, StringUtils.getLevenshteinDistance("bbb", "a", 1));

        
        assertEquals(-1, StringUtils.getLevenshteinDistance("12345", "1234567", 1));
        assertEquals(-1, StringUtils.getLevenshteinDistance("1234567", "12345", 1));

        
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog",1) );
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant",3) );
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo",7) );
        assertEquals(-1, StringUtils.getLevenshteinDistance("elephant", "hippo",6) );
        assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant",7) );
        assertEquals(-1, StringUtils.getLevenshteinDistance("hippo", "elephant",6) );
        assertEquals(8, StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz",8) );
        assertEquals(8, StringUtils.getLevenshteinDistance("zzzzzzzz", "hippo",8) );
        assertEquals(1, StringUtils.getLevenshteinDistance("hello", "hallo",1) );

        
        try {
            @SuppressWarnings("unused")
            int d = StringUtils.getLevenshteinDistance("a", null, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            @SuppressWarnings("unused")
            int d = StringUtils.getLevenshteinDistance(null, "a", 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            @SuppressWarnings("unused")
            int d = StringUtils.getLevenshteinDistance("a", "a", -1);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.lang3.StringUtilsTest::testEMPTY
    public void testEMPTY() {
        assertNotNull(StringUtils.EMPTY);
        assertEquals("", StringUtils.EMPTY);
        assertEquals(0, StringUtils.EMPTY.length());
    }

// org.apache.commons.lang3.StringUtilsTest::testIsAllLowerCase
    public void testIsAllLowerCase() {
        assertFalse(StringUtils.isAllLowerCase(null));
        assertFalse(StringUtils.isAllLowerCase(StringUtils.EMPTY));
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abc "));
        assertFalse(StringUtils.isAllLowerCase("abC"));
    }

// org.apache.commons.lang3.StringUtilsTest::testIsAllUpperCase
    public void testIsAllUpperCase() {
        assertFalse(StringUtils.isAllUpperCase(null));
        assertFalse(StringUtils.isAllUpperCase(StringUtils.EMPTY));
        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("ABC "));
        assertFalse(StringUtils.isAllUpperCase("aBC"));
    }

// org.apache.commons.lang3.StringUtilsTest::testRemoveStart
    public void testRemoveStart() {
        
        assertNull(StringUtils.removeStart(null, null));
        assertNull(StringUtils.removeStart(null, ""));
        assertNull(StringUtils.removeStart(null, "a"));
        
        
        assertEquals(StringUtils.removeStart("", null), "");
        assertEquals(StringUtils.removeStart("", ""), "");
        assertEquals(StringUtils.removeStart("", "a"), "");
        
        
        assertEquals(StringUtils.removeStart("www.domain.com", "www."), "domain.com");
        assertEquals(StringUtils.removeStart("domain.com", "www."), "domain.com");
        assertEquals(StringUtils.removeStart("domain.com", ""), "domain.com");        
        assertEquals(StringUtils.removeStart("domain.com", null), "domain.com");        
    }

// org.apache.commons.lang3.StringUtilsTest::testRemoveStartIgnoreCase
    public void testRemoveStartIgnoreCase() {
        
        assertNull("removeStartIgnoreCase(null, null)", StringUtils.removeStartIgnoreCase(null, null));
        assertNull("removeStartIgnoreCase(null, \"\")", StringUtils.removeStartIgnoreCase(null, ""));
        assertNull("removeStartIgnoreCase(null, \"a\")", StringUtils.removeStartIgnoreCase(null, "a"));
        
        
        assertEquals("removeStartIgnoreCase(\"\", null)", StringUtils.removeStartIgnoreCase("", null), "");
        assertEquals("removeStartIgnoreCase(\"\", \"\")", StringUtils.removeStartIgnoreCase("", ""), "");
        assertEquals("removeStartIgnoreCase(\"\", \"a\")", StringUtils.removeStartIgnoreCase("", "a"), "");
        
        
        assertEquals("removeStartIgnoreCase(\"www.domain.com\", \"www.\")", StringUtils.removeStartIgnoreCase("www.domain.com", "www."), "domain.com");
        assertEquals("removeStartIgnoreCase(\"domain.com\", \"www.\")", StringUtils.removeStartIgnoreCase("domain.com", "www."), "domain.com");
        assertEquals("removeStartIgnoreCase(\"domain.com\", \"\")", StringUtils.removeStartIgnoreCase("domain.com", ""), "domain.com");        
        assertEquals("removeStartIgnoreCase(\"domain.com\", null)", StringUtils.removeStartIgnoreCase("domain.com", null), "domain.com");        
        
        
        assertEquals("removeStartIgnoreCase(\"www.domain.com\", \"WWW.\")", StringUtils.removeStartIgnoreCase("www.domain.com", "WWW."), "domain.com");
    }

// org.apache.commons.lang3.StringUtilsTest::testRemoveEnd
    public void testRemoveEnd() {
        
        assertNull(StringUtils.removeEnd(null, null));
        assertNull(StringUtils.removeEnd(null, ""));
        assertNull(StringUtils.removeEnd(null, "a"));
        
        
        assertEquals(StringUtils.removeEnd("", null), "");
        assertEquals(StringUtils.removeEnd("", ""), "");
        assertEquals(StringUtils.removeEnd("", "a"), "");
        
        
        assertEquals(StringUtils.removeEnd("www.domain.com.", ".com"), "www.domain.com.");
        assertEquals(StringUtils.removeEnd("www.domain.com", ".com"), "www.domain");
        assertEquals(StringUtils.removeEnd("www.domain", ".com"), "www.domain");
        assertEquals(StringUtils.removeEnd("domain.com", ""), "domain.com");   
        assertEquals(StringUtils.removeEnd("domain.com", null), "domain.com");   
    }

// org.apache.commons.lang3.StringUtilsTest::testRemoveEndIgnoreCase
    public void testRemoveEndIgnoreCase() {
        
        assertNull("removeEndIgnoreCase(null, null)", StringUtils.removeEndIgnoreCase(null, null));
        assertNull("removeEndIgnoreCase(null, \"\")", StringUtils.removeEndIgnoreCase(null, ""));
        assertNull("removeEndIgnoreCase(null, \"a\")", StringUtils.removeEndIgnoreCase(null, "a"));
        
        
        assertEquals("removeEndIgnoreCase(\"\", null)", StringUtils.removeEndIgnoreCase("", null), "");
        assertEquals("removeEndIgnoreCase(\"\", \"\")", StringUtils.removeEndIgnoreCase("", ""), "");
        assertEquals("removeEndIgnoreCase(\"\", \"a\")", StringUtils.removeEndIgnoreCase("", "a"), "");
        
        
        assertEquals("removeEndIgnoreCase(\"www.domain.com.\", \".com\")", StringUtils.removeEndIgnoreCase("www.domain.com.", ".com"), "www.domain.com.");
        assertEquals("removeEndIgnoreCase(\"www.domain.com\", \".com\")", StringUtils.removeEndIgnoreCase("www.domain.com", ".com"), "www.domain");
        assertEquals("removeEndIgnoreCase(\"www.domain\", \".com\")", StringUtils.removeEndIgnoreCase("www.domain", ".com"), "www.domain");
        assertEquals("removeEndIgnoreCase(\"domain.com\", \"\")", StringUtils.removeEndIgnoreCase("domain.com", ""), "domain.com");   
        assertEquals("removeEndIgnoreCase(\"domain.com\", null)", StringUtils.removeEndIgnoreCase("domain.com", null), "domain.com");   

        
        assertEquals("removeEndIgnoreCase(\"www.domain.com\", \".COM\")", StringUtils.removeEndIgnoreCase("www.domain.com", ".COM"), "www.domain");
        assertEquals("removeEndIgnoreCase(\"www.domain.COM\", \".com\")", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"), "www.domain");
    }

// org.apache.commons.lang3.StringUtilsTest::testRemove_String
    public void testRemove_String() {
        
        assertEquals(null, StringUtils.remove(null, null));
        assertEquals(null, StringUtils.remove(null, ""));
        assertEquals(null, StringUtils.remove(null, "a"));
        
        
        assertEquals("", StringUtils.remove("", null));
        assertEquals("", StringUtils.remove("", ""));
        assertEquals("", StringUtils.remove("", "a"));
        
        
        assertEquals(null, StringUtils.remove(null, null));
        assertEquals("", StringUtils.remove("", null));
        assertEquals("a", StringUtils.remove("a", null));
        
        
        assertEquals(null, StringUtils.remove(null, ""));
        assertEquals("", StringUtils.remove("", ""));
        assertEquals("a", StringUtils.remove("a", ""));
        
        
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        
        
        assertEquals("queued", StringUtils.remove("queued", "zz"));
    }

// org.apache.commons.lang3.StringUtilsTest::testRemove_char
    public void testRemove_char() {
        
        assertEquals(null, StringUtils.remove(null, 'a'));
        assertEquals(null, StringUtils.remove(null, 'a'));
        assertEquals(null, StringUtils.remove(null, 'a'));
        
        
        assertEquals("", StringUtils.remove("", 'a'));
        assertEquals("", StringUtils.remove("", 'a'));
        assertEquals("", StringUtils.remove("", 'a'));
        
        
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        
        
        assertEquals("queued", StringUtils.remove("queued", 'z'));
    }

// org.apache.commons.lang3.StringUtilsTest::testDifferenceAt_StringArray
    public void testDifferenceAt_StringArray() {        
        assertEquals(-1, StringUtils.indexOfDifference((String[])null));
        assertEquals(-1, StringUtils.indexOfDifference(new String[] {}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[] {"abc"}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[] {null, null}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[] {"", ""}));
        assertEquals(0, StringUtils.indexOfDifference(new String[] {"", null}));
        assertEquals(0, StringUtils.indexOfDifference(new String[] {"abc", null, null}));
        assertEquals(0, StringUtils.indexOfDifference(new String[] {null, null, "abc"}));
        assertEquals(0, StringUtils.indexOfDifference(new String[] {"", "abc"}));
        assertEquals(0, StringUtils.indexOfDifference(new String[] {"abc", ""}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[] {"abc", "abc"}));
        assertEquals(1, StringUtils.indexOfDifference(new String[] {"abc", "a"}));
        assertEquals(2, StringUtils.indexOfDifference(new String[] {"ab", "abxyz"}));
        assertEquals(2, StringUtils.indexOfDifference(new String[] {"abcde", "abxyz"}));
        assertEquals(0, StringUtils.indexOfDifference(new String[] {"abcde", "xyz"}));
        assertEquals(0, StringUtils.indexOfDifference(new String[] {"xyz", "abcde"}));
        assertEquals(7, StringUtils.indexOfDifference(new String[] {"i am a machine", "i am a robot"}));
    }

// org.apache.commons.lang3.StringUtilsTest::testGetCommonPrefix_StringArray
    public void testGetCommonPrefix_StringArray() {
        assertEquals("", StringUtils.getCommonPrefix((String[])null));
        assertEquals("", StringUtils.getCommonPrefix());
        assertEquals("abc", StringUtils.getCommonPrefix("abc"));
        assertEquals("", StringUtils.getCommonPrefix(null, null));
        assertEquals("", StringUtils.getCommonPrefix("", ""));
        assertEquals("", StringUtils.getCommonPrefix("", null));
        assertEquals("", StringUtils.getCommonPrefix("abc", null, null));
        assertEquals("", StringUtils.getCommonPrefix(null, null, "abc"));
        assertEquals("", StringUtils.getCommonPrefix("", "abc"));
        assertEquals("", StringUtils.getCommonPrefix("abc", ""));
        assertEquals("abc", StringUtils.getCommonPrefix("abc", "abc"));
        assertEquals("a", StringUtils.getCommonPrefix("abc", "a"));
        assertEquals("ab", StringUtils.getCommonPrefix("ab", "abxyz"));
        assertEquals("ab", StringUtils.getCommonPrefix("abcde", "abxyz"));
        assertEquals("", StringUtils.getCommonPrefix("abcde", "xyz"));
        assertEquals("", StringUtils.getCommonPrefix("xyz", "abcde"));
        assertEquals("i am a ", StringUtils.getCommonPrefix("i am a machine", "i am a robot"));
    }

// org.apache.commons.lang3.StringUtilsTest::testNormalizeSpace
    public void testNormalizeSpace() {
        assertEquals(null, StringUtils.normalizeSpace(null));
        assertEquals("", StringUtils.normalizeSpace(""));
        assertEquals("", StringUtils.normalizeSpace(" "));
        assertEquals("", StringUtils.normalizeSpace("\t"));
        assertEquals("", StringUtils.normalizeSpace("\n"));        
        assertEquals("", StringUtils.normalizeSpace("\u0009"));
        assertEquals("", StringUtils.normalizeSpace("\u000B"));
        assertEquals("", StringUtils.normalizeSpace("\u000C"));
        assertEquals("", StringUtils.normalizeSpace("\u001C"));
        assertEquals("", StringUtils.normalizeSpace("\u001D"));
        assertEquals("", StringUtils.normalizeSpace("\u001E"));
        assertEquals("", StringUtils.normalizeSpace("\u001F"));
        assertEquals("", StringUtils.normalizeSpace("\f"));
        assertEquals("", StringUtils.normalizeSpace("\r"));
        assertEquals("a", StringUtils.normalizeSpace("  a  "));
        assertEquals("a b c", StringUtils.normalizeSpace("  a  b   c  "));
        assertEquals("a b c", StringUtils.normalizeSpace("a\t\f\r  b\u000B   c\n"));
    }

// org.apache.commons.lang3.StringUtilsTest::testLANG666
    public void testLANG666() {
        assertEquals("12",StringUtils.stripEnd("120.00", ".0"));
        assertEquals("121",StringUtils.stripEnd("121.00", ".0"));
    }

// org.apache.commons.lang3.StringUtilsTest::testStringUtilsCharSequenceContract
    public void testStringUtilsCharSequenceContract() {
        Class<StringUtils> c = StringUtils.class;
        Method[] methods = c.getMethods();
        for (Method m : methods) {
            if (m.getReturnType() == String.class || m.getReturnType() == String[].class) {
                
                
                
                Class<?>[] params = m.getParameterTypes();
                if ( params.length > 0 && (params[0] == CharSequence.class || params[0] == CharSequence[].class)) {
                    fail("The method " + m + " appears to be mutable in spirit and therefore must not accept a CharSequence");
                }
            } else {
                
                
                Class<?>[] params = m.getParameterTypes();
                if ( params.length > 0 && (params[0] == String.class || params[0] == String[].class)) {
                    fail("The method " + m + " appears to be immutable in spirit and therefore must not accept a String");
                }
            }
        }
    }

// org.apache.commons.lang3.StringUtilsTest::testToString
    public void testToString() throws UnsupportedEncodingException {
        final String expectedString = "The quick brown fox jumped over the lazy dog.";
        String encoding = SystemUtils.FILE_ENCODING;
        byte[] expectedBytes = expectedString.getBytes(encoding);
        
        assertArrayEquals(expectedBytes, expectedString.getBytes());
        
        assertEquals(expectedString, StringUtils.toString(expectedBytes, null));
        assertEquals(expectedString, StringUtils.toString(expectedBytes, encoding));
        encoding = "UTF-16";
        expectedBytes = expectedString.getBytes(encoding);
        assertEquals(expectedString, StringUtils.toString(expectedBytes, encoding));
    }

// org.apache.commons.lang3.StringUtilsTest::testEscapeSurrogatePairs
    public void testEscapeSurrogatePairs() throws Exception {
        assertEquals("\uD83D\uDE30", StringEscapeUtils.escapeCsv("\uD83D\uDE30"));
        
        assertEquals("\uD800\uDC00", StringEscapeUtils.escapeCsv("\uD800\uDC00"));
        assertEquals("\uD834\uDD1E", StringEscapeUtils.escapeCsv("\uD834\uDD1E"));
        assertEquals("\uDBFF\uDFFD", StringEscapeUtils.escapeCsv("\uDBFF\uDFFD"));
        
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testIsAssignable
    public void testIsAssignable() throws SecurityException, NoSuchMethodException,
            NoSuchFieldException {
        List list0 = null;
        List<Object> list1 = null;
        List<?> list2 = null;
        List<? super Object> list3 = null;
        List<String> list4 = null;
        List<? extends String> list5 = null;
        List<? super String> list6 = null;
        List[] list7 = null;
        List<Object>[] list8 = null;
        List<?>[] list9 = null;
        List<? super Object>[] list10 = null;
        List<String>[] list11 = null;
        List<? extends String>[] list12 = null;
        List<? super String>[] list13;
        Class<?> clazz = getClass();
        Method method = clazz.getMethod("dummyMethod", List.class, List.class, List.class,
                List.class, List.class, List.class, List.class, List[].class, List[].class,
                List[].class, List[].class, List[].class, List[].class, List[].class);
        Type[] types = method.getGenericParameterTypes();

        delegateBooleanAssertion(types, 0, 0, true);
        list1 = list0;
        delegateBooleanAssertion(types, 0, 1, true);
        list0 = list1;
        delegateBooleanAssertion(types, 1, 0, true);
        list2 = list0;
        delegateBooleanAssertion(types, 0, 2, true);
        list0 = list2;
        delegateBooleanAssertion(types, 2, 0, true);
        list3 = list0;
        delegateBooleanAssertion(types, 0, 3, true);
        list0 = list3;
        delegateBooleanAssertion(types, 3, 0, true);
        list4 = list0;
        delegateBooleanAssertion(types, 0, 4, true);
        list0 = list4;
        delegateBooleanAssertion(types, 4, 0, true);
        list5 = list0;
        delegateBooleanAssertion(types, 0, 5, true);
        list0 = list5;
        delegateBooleanAssertion(types, 5, 0, true);
        list6 = list0;
        delegateBooleanAssertion(types, 0, 6, true);
        list0 = list6;
        delegateBooleanAssertion(types, 6, 0, true);

        delegateBooleanAssertion(types, 1, 1, true);
        list2 = list1;
        delegateBooleanAssertion(types, 1, 2, true);
        list1 = (List<Object>) list2;
        delegateBooleanAssertion(types, 2, 1, false);
        list3 = list1;
        delegateBooleanAssertion(types, 1, 3, true);
        list1 = (List<Object>) list3;
        delegateBooleanAssertion(types, 3, 1, false);
        
        delegateBooleanAssertion(types, 1, 4, false);
        
        delegateBooleanAssertion(types, 4, 1, false);
        
        delegateBooleanAssertion(types, 1, 5, false);
        
        delegateBooleanAssertion(types, 5, 1, false);
        list6 = list1;
        delegateBooleanAssertion(types, 1, 6, true);
        list1 = (List<Object>) list6;
        delegateBooleanAssertion(types, 6, 1, false);

        delegateBooleanAssertion(types, 2, 2, true);
        list2 = list3;
        delegateBooleanAssertion(types, 2, 3, false);
        list2 = list4;
        delegateBooleanAssertion(types, 3, 2, true);
        list3 = (List<? super Object>) list2;
        delegateBooleanAssertion(types, 2, 4, false);
        list2 = list5;
        delegateBooleanAssertion(types, 4, 2, true);
        list4 = (List<String>) list2;
        delegateBooleanAssertion(types, 2, 5, false);
        list2 = list6;
        delegateBooleanAssertion(types, 5, 2, true);
        list5 = (List<? extends String>) list2;
        delegateBooleanAssertion(types, 2, 6, false);

        delegateBooleanAssertion(types, 6, 2, true);
        list6 = (List<? super String>) list2;
        delegateBooleanAssertion(types, 3, 3, true);
        
        delegateBooleanAssertion(types, 3, 4, false);
        
        delegateBooleanAssertion(types, 4, 3, false);
        
        delegateBooleanAssertion(types, 3, 5, false);
        
        delegateBooleanAssertion(types, 5, 3, false);
        list6 = list3;
        delegateBooleanAssertion(types, 3, 6, true);
        list3 = (List<? super Object>) list6;
        delegateBooleanAssertion(types, 6, 3, false);

        delegateBooleanAssertion(types, 4, 4, true);
        list5 = list4;
        delegateBooleanAssertion(types, 4, 5, true);
        list4 = (List<String>) list5;
        delegateBooleanAssertion(types, 5, 4, false);
        list6 = list4;
        delegateBooleanAssertion(types, 4, 6, true);
        list4 = (List<String>) list6;
        delegateBooleanAssertion(types, 6, 4, false);

        delegateBooleanAssertion(types, 5, 5, true);
        list6 = (List<? super String>) list5;
        delegateBooleanAssertion(types, 5, 6, false);
        list5 = (List<? extends String>) list6;
        delegateBooleanAssertion(types, 6, 5, false);

        delegateBooleanAssertion(types, 6, 6, true);

        delegateBooleanAssertion(types, 7, 7, true);
        list8 = list7;
        delegateBooleanAssertion(types, 7, 8, true);
        list7 = list8;
        delegateBooleanAssertion(types, 8, 7, true);
        list9 = list7;
        delegateBooleanAssertion(types, 7, 9, true);
        list7 = list9;
        delegateBooleanAssertion(types, 9, 7, true);
        list10 = list7;
        delegateBooleanAssertion(types, 7, 10, true);
        list7 = list10;
        delegateBooleanAssertion(types, 10, 7, true);
        list11 = list7;
        delegateBooleanAssertion(types, 7, 11, true);
        list7 = list11;
        delegateBooleanAssertion(types, 11, 7, true);
        list12 = list7;
        delegateBooleanAssertion(types, 7, 12, true);
        list7 = list12;
        delegateBooleanAssertion(types, 12, 7, true);
        list13 = list7;
        delegateBooleanAssertion(types, 7, 13, true);
        list7 = list13;
        delegateBooleanAssertion(types, 13, 7, true);

        delegateBooleanAssertion(types, 8, 8, true);
        list9 = list8;
        delegateBooleanAssertion(types, 8, 9, true);
        list8 = (List<Object>[]) list9;
        delegateBooleanAssertion(types, 9, 8, false);
        list10 = list8;
        delegateBooleanAssertion(types, 8, 10, true);
        list8 = (List<Object>[]) list10; 
        delegateBooleanAssertion(types, 10, 8, false);
        
        delegateBooleanAssertion(types, 8, 11, false);
        
        delegateBooleanAssertion(types, 11, 8, false);
        
        delegateBooleanAssertion(types, 8, 12, false);
        
        delegateBooleanAssertion(types, 12, 8, false);
        list13 = list8;
        delegateBooleanAssertion(types, 8, 13, true);
        list8 = (List<Object>[]) list13;
        delegateBooleanAssertion(types, 13, 8, false);

        delegateBooleanAssertion(types, 9, 9, true);
        list10 = (List<? super Object>[]) list9;
        delegateBooleanAssertion(types, 9, 10, false);
        list9 = list10;
        delegateBooleanAssertion(types, 10, 9, true);
        list11 = (List<String>[]) list9;
        delegateBooleanAssertion(types, 9, 11, false);
        list9 = list11;
        delegateBooleanAssertion(types, 11, 9, true);
        list12 = (List<? extends String>[]) list9;
        delegateBooleanAssertion(types, 9, 12, false);
        list9 = list12;
        delegateBooleanAssertion(types, 12, 9, true);
        list13 = (List<? super String>[]) list9;
        delegateBooleanAssertion(types, 9, 13, false);
        list9 = list13;
        delegateBooleanAssertion(types, 13, 9, true);

        delegateBooleanAssertion(types, 10, 10, true);
        
        delegateBooleanAssertion(types, 10, 11, false);
        
        delegateBooleanAssertion(types, 11, 10, false);
        
        delegateBooleanAssertion(types, 10, 12, false);
        
        delegateBooleanAssertion(types, 12, 10, false);
        list13 = list10;
        delegateBooleanAssertion(types, 10, 13, true);
        list10 = (List<? super Object>[]) list13;
        delegateBooleanAssertion(types, 13, 10, false);

        delegateBooleanAssertion(types, 11, 11, true);
        list12 = list11;
        delegateBooleanAssertion(types, 11, 12, true);
        list11 = (List<String>[]) list12;
        delegateBooleanAssertion(types, 12, 11, false);
        list13 = list11;
        delegateBooleanAssertion(types, 11, 13, true);
        list11 = (List<String>[]) list13;
        delegateBooleanAssertion(types, 13, 11, false);

        delegateBooleanAssertion(types, 12, 12, true);
        list13 = (List<? super String>[]) list12;
        delegateBooleanAssertion(types, 12, 13, false);
        list12 = (List<? extends String>[]) list13;
        delegateBooleanAssertion(types, 13, 12, false);

        delegateBooleanAssertion(types, 13, 13, true);
        Type disType = getClass().getField("dis").getGenericType();
        
        
        Type datType = getClass().getField("dat").getGenericType();
        Type daType = getClass().getField("da").getGenericType();
        Type uhderType = getClass().getField("uhder").getGenericType();
        Type dingType = getClass().getField("ding").getGenericType();
        Type testerType = getClass().getField("tester").getGenericType();
        Type tester2Type = getClass().getField("tester2").getGenericType();
        Type dat2Type = getClass().getField("dat2").getGenericType();
        Type dat3Type = getClass().getField("dat3").getGenericType();
        dis = dat;
        Assert.assertTrue(TypeUtils.isAssignable(datType, disType));
        
        Assert.assertFalse(TypeUtils.isAssignable(daType, disType));
        dis = uhder;
        Assert.assertTrue(TypeUtils.isAssignable(uhderType, disType));
        dis = ding;
        Assert.assertFalse(String.format("type %s not assignable to %s!", dingType, disType),
                TypeUtils.isAssignable(dingType, disType));
        dis = tester;
        Assert.assertTrue(TypeUtils.isAssignable(testerType, disType));
        
        Assert.assertFalse(TypeUtils.isAssignable(tester2Type, disType));
        
        Assert.assertFalse(TypeUtils.isAssignable(dat2Type, datType));
        
        Assert.assertFalse(TypeUtils.isAssignable(datType, dat2Type));
        
        Assert.assertFalse(TypeUtils.isAssignable(dat3Type, datType));
        char ch = 0;
        boolean bo = false;
        byte by = 0;
        short sh = 0;
        int in = 0;
        long lo = 0;
        float fl = 0;
        double du = 0;
        du = ch;
        Assert.assertTrue(TypeUtils.isAssignable(char.class, double.class));
        du = by;
        Assert.assertTrue(TypeUtils.isAssignable(byte.class, double.class));
        du = sh;
        Assert.assertTrue(TypeUtils.isAssignable(short.class, double.class));
        du = in;
        Assert.assertTrue(TypeUtils.isAssignable(int.class, double.class));
        du = lo;
        Assert.assertTrue(TypeUtils.isAssignable(long.class, double.class));
        du = fl;
        Assert.assertTrue(TypeUtils.isAssignable(float.class, double.class));
        lo = in;
        Assert.assertTrue(TypeUtils.isAssignable(int.class, long.class));
        lo = Integer.valueOf(0);
        Assert.assertTrue(TypeUtils.isAssignable(Integer.class, long.class));
        
        Assert.assertFalse(TypeUtils.isAssignable(int.class, Long.class));
        
        Assert.assertFalse(TypeUtils.isAssignable(Integer.class, Long.class));
        in = Integer.valueOf(0);
        Assert.assertTrue(TypeUtils.isAssignable(Integer.class, int.class));
        Integer inte = in;
        Assert.assertTrue(TypeUtils.isAssignable(int.class, Integer.class));
        Assert.assertTrue(TypeUtils.isAssignable(int.class, Number.class));
        Assert.assertTrue(TypeUtils.isAssignable(int.class, Object.class));
        Type intComparableType = getClass().getField("intComparable").getGenericType();
        intComparable = 1;
        Assert.assertTrue(TypeUtils.isAssignable(int.class, intComparableType));
        Assert.assertTrue(TypeUtils.isAssignable(int.class, Comparable.class));
        Serializable ser = 1;
        Assert.assertTrue(TypeUtils.isAssignable(int.class, Serializable.class));
        Type longComparableType = getClass().getField("longComparable").getGenericType();
        
        Assert.assertFalse(TypeUtils.isAssignable(int.class, longComparableType));
        
        Assert.assertFalse(TypeUtils.isAssignable(Integer.class, longComparableType));
        
        
        Assert.assertFalse(TypeUtils.isAssignable(int[].class, long[].class));
        Integer[] ia = null;
        Type caType = getClass().getField("intWildcardComparable").getGenericType();
        intWildcardComparable = ia;
        Assert.assertTrue(TypeUtils.isAssignable(Integer[].class, caType));
        
        Assert.assertFalse(TypeUtils.isAssignable(Integer[].class, int[].class));
        int[] ina = null;
        Object[] oa;
        
        Assert.assertFalse(TypeUtils.isAssignable(int[].class, Object[].class));
        oa = new Integer[0];
        Assert.assertTrue(TypeUtils.isAssignable(Integer[].class, Object[].class));
        Type bClassType = AClass.class.getField("bClass").getGenericType();
        Type cClassType = AClass.class.getField("cClass").getGenericType();
        Type dClassType = AClass.class.getField("dClass").getGenericType();
        Type eClassType = AClass.class.getField("eClass").getGenericType();
        Type fClassType = AClass.class.getField("fClass").getGenericType();
        AClass aClass = new AClass(new AAClass<String>());
        aClass.bClass = aClass.cClass;
        Assert.assertTrue(TypeUtils.isAssignable(cClassType, bClassType));
        aClass.bClass = aClass.dClass;
        Assert.assertTrue(TypeUtils.isAssignable(dClassType, bClassType));
        aClass.bClass = aClass.eClass;
        Assert.assertTrue(TypeUtils.isAssignable(eClassType, bClassType));
        aClass.bClass = aClass.fClass;
        Assert.assertTrue(TypeUtils.isAssignable(fClassType, bClassType));
        aClass.cClass = aClass.dClass;
        Assert.assertTrue(TypeUtils.isAssignable(dClassType, cClassType));
        aClass.cClass = aClass.eClass;
        Assert.assertTrue(TypeUtils.isAssignable(eClassType, cClassType));
        aClass.cClass = aClass.fClass;
        Assert.assertTrue(TypeUtils.isAssignable(fClassType, cClassType));
        aClass.dClass = aClass.eClass;
        Assert.assertTrue(TypeUtils.isAssignable(eClassType, dClassType));
        aClass.dClass = aClass.fClass;
        Assert.assertTrue(TypeUtils.isAssignable(fClassType, dClassType));
        aClass.eClass = aClass.fClass;
        Assert.assertTrue(TypeUtils.isAssignable(fClassType, eClassType));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testIsInstance
    public void testIsInstance() throws SecurityException, NoSuchFieldException {
        Type intComparableType = getClass().getField("intComparable").getGenericType();
        Type uriComparableType = getClass().getField("uriComparable").getGenericType();
        intComparable = 1;
        Assert.assertTrue(TypeUtils.isInstance(1, intComparableType));
        
        Assert.assertFalse(TypeUtils.isInstance(1, uriComparableType));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testGetTypeArguments
    public void testGetTypeArguments() {
        Map<TypeVariable<?>, Type> typeVarAssigns;
        TypeVariable<?> treeSetTypeVar;
        Type typeArg;

        typeVarAssigns = TypeUtils.getTypeArguments(Integer.class, Comparable.class);
        treeSetTypeVar = Comparable.class.getTypeParameters()[0];
        Assert.assertTrue("Type var assigns for Comparable from Integer: " + typeVarAssigns,
                typeVarAssigns.containsKey(treeSetTypeVar));
        typeArg = typeVarAssigns.get(treeSetTypeVar);
        Assert.assertEquals("Type argument of Comparable from Integer: " + typeArg, Integer.class,
                typeVarAssigns.get(treeSetTypeVar));

        typeVarAssigns = TypeUtils.getTypeArguments(int.class, Comparable.class);
        treeSetTypeVar = Comparable.class.getTypeParameters()[0];
        Assert.assertTrue("Type var assigns for Comparable from int: " + typeVarAssigns,
                typeVarAssigns.containsKey(treeSetTypeVar));
        typeArg = typeVarAssigns.get(treeSetTypeVar);
        Assert.assertEquals("Type argument of Comparable from int: " + typeArg, Integer.class,
                typeVarAssigns.get(treeSetTypeVar));

        Collection<Integer> col = Arrays.asList(new Integer[0]);
        typeVarAssigns = TypeUtils.getTypeArguments(List.class, Collection.class);
        treeSetTypeVar = Comparable.class.getTypeParameters()[0];
        Assert.assertFalse("Type var assigns for Collection from List: " + typeVarAssigns,
                typeVarAssigns.containsKey(treeSetTypeVar));

        typeVarAssigns = TypeUtils.getTypeArguments(AAAClass.BBBClass.class, AAClass.BBClass.class);
        Assert.assertTrue(typeVarAssigns.size() == 2);
        Assert.assertEquals(String.class, typeVarAssigns.get(AAClass.class.getTypeParameters()[0]));
        Assert.assertEquals(String.class, typeVarAssigns.get(AAClass.BBClass.class.getTypeParameters()[0]));

        typeVarAssigns = TypeUtils.getTypeArguments(Other.class, This.class);
        Assert.assertEquals(2, typeVarAssigns.size());
        Assert.assertEquals(String.class, typeVarAssigns.get(This.class.getTypeParameters()[0]));
        Assert.assertEquals(Other.class.getTypeParameters()[0], typeVarAssigns.get(This.class.getTypeParameters()[1]));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testTypesSatisfyVariables
    public void testTypesSatisfyVariables() throws SecurityException, NoSuchFieldException,
            NoSuchMethodException {
        Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<TypeVariable<?>, Type>();
        Integer max = TypeUtilsTest.<Integer> stub();
        typeVarAssigns.put(getClass().getMethod("stub").getTypeParameters()[0], Integer.class);
        Assert.assertTrue(TypeUtils.typesSatisfyVariables(typeVarAssigns));
        typeVarAssigns.clear();
        typeVarAssigns.put(getClass().getMethod("stub2").getTypeParameters()[0], Integer.class);
        Assert.assertTrue(TypeUtils.typesSatisfyVariables(typeVarAssigns));
        typeVarAssigns.clear();
        typeVarAssigns.put(getClass().getMethod("stub3").getTypeParameters()[0], Integer.class);
        Assert.assertTrue(TypeUtils.typesSatisfyVariables(typeVarAssigns));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testDetermineTypeVariableAssignments
    public void testDetermineTypeVariableAssignments() throws SecurityException,
            NoSuchFieldException, NoSuchMethodException {
        ParameterizedType iterableType = (ParameterizedType) getClass().getField("iterable")
                .getGenericType();
        Map<TypeVariable<?>, Type> typeVarAssigns = TypeUtils.determineTypeArguments(TreeSet.class,
                iterableType);
        TypeVariable<?> treeSetTypeVar = TreeSet.class.getTypeParameters()[0];
        Assert.assertTrue(typeVarAssigns.containsKey(treeSetTypeVar));
        Assert.assertEquals(iterableType.getActualTypeArguments()[0], typeVarAssigns
                .get(treeSetTypeVar));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testGetRawType
    public void testGetRawType() throws SecurityException, NoSuchFieldException {
        Type stringParentFieldType = GenericTypeHolder.class.getDeclaredField("stringParent")
                .getGenericType();
        Type integerParentFieldType = GenericTypeHolder.class.getDeclaredField("integerParent")
                .getGenericType();
        Type foosFieldType = GenericTypeHolder.class.getDeclaredField("foos").getGenericType();
        Type genericParentT = GenericParent.class.getTypeParameters()[0];
        Assert.assertEquals(GenericParent.class, TypeUtils.getRawType(stringParentFieldType, null));
        Assert
                .assertEquals(GenericParent.class, TypeUtils.getRawType(integerParentFieldType,
                        null));
        Assert.assertEquals(List.class, TypeUtils.getRawType(foosFieldType, null));
        Assert.assertEquals(String.class, TypeUtils.getRawType(genericParentT,
                StringParameterizedChild.class));
        Assert.assertEquals(String.class, TypeUtils.getRawType(genericParentT,
                stringParentFieldType));
        Assert.assertEquals(Foo.class, TypeUtils.getRawType(Iterable.class.getTypeParameters()[0],
                foosFieldType));
        Assert.assertEquals(Foo.class, TypeUtils.getRawType(List.class.getTypeParameters()[0],
                foosFieldType));
        Assert.assertNull(TypeUtils.getRawType(genericParentT, GenericParent.class));
        Assert.assertEquals(GenericParent[].class, TypeUtils.getRawType(GenericTypeHolder.class
                .getDeclaredField("barParents").getGenericType(), null));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testIsArrayTypeClasses
    public void testIsArrayTypeClasses() {
        Assert.assertTrue(TypeUtils.isArrayType(boolean[].class));
        Assert.assertTrue(TypeUtils.isArrayType(byte[].class));
        Assert.assertTrue(TypeUtils.isArrayType(short[].class));
        Assert.assertTrue(TypeUtils.isArrayType(int[].class));
        Assert.assertTrue(TypeUtils.isArrayType(char[].class));
        Assert.assertTrue(TypeUtils.isArrayType(long[].class));
        Assert.assertTrue(TypeUtils.isArrayType(float[].class));
        Assert.assertTrue(TypeUtils.isArrayType(double[].class));
        Assert.assertTrue(TypeUtils.isArrayType(Object[].class));
        Assert.assertTrue(TypeUtils.isArrayType(String[].class));

        Assert.assertFalse(TypeUtils.isArrayType(boolean.class));
        Assert.assertFalse(TypeUtils.isArrayType(byte.class));
        Assert.assertFalse(TypeUtils.isArrayType(short.class));
        Assert.assertFalse(TypeUtils.isArrayType(int.class));
        Assert.assertFalse(TypeUtils.isArrayType(char.class));
        Assert.assertFalse(TypeUtils.isArrayType(long.class));
        Assert.assertFalse(TypeUtils.isArrayType(float.class));
        Assert.assertFalse(TypeUtils.isArrayType(double.class));
        Assert.assertFalse(TypeUtils.isArrayType(Object.class));
        Assert.assertFalse(TypeUtils.isArrayType(String.class));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testIsArrayGenericTypes
    public void testIsArrayGenericTypes() throws Exception {
        Method method = getClass().getMethod("dummyMethod", List.class, List.class, List.class,
                List.class, List.class, List.class, List.class, List[].class, List[].class,
                List[].class, List[].class, List[].class, List[].class, List[].class);

        Type[] types = method.getGenericParameterTypes();

        Assert.assertFalse(TypeUtils.isArrayType(types[0]));
        Assert.assertFalse(TypeUtils.isArrayType(types[1]));
        Assert.assertFalse(TypeUtils.isArrayType(types[2]));
        Assert.assertFalse(TypeUtils.isArrayType(types[3]));
        Assert.assertFalse(TypeUtils.isArrayType(types[4]));
        Assert.assertFalse(TypeUtils.isArrayType(types[5]));
        Assert.assertFalse(TypeUtils.isArrayType(types[6]));
        Assert.assertTrue(TypeUtils.isArrayType(types[7]));
        Assert.assertTrue(TypeUtils.isArrayType(types[8]));
        Assert.assertTrue(TypeUtils.isArrayType(types[9]));
        Assert.assertTrue(TypeUtils.isArrayType(types[10]));
        Assert.assertTrue(TypeUtils.isArrayType(types[11]));
        Assert.assertTrue(TypeUtils.isArrayType(types[12]));
        Assert.assertTrue(TypeUtils.isArrayType(types[13]));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testGetPrimitiveArrayComponentType
    public void testGetPrimitiveArrayComponentType() throws Exception {
        Assert.assertEquals(boolean.class, TypeUtils.getArrayComponentType(boolean[].class));
        Assert.assertEquals(byte.class, TypeUtils.getArrayComponentType(byte[].class));
        Assert.assertEquals(short.class, TypeUtils.getArrayComponentType(short[].class));
        Assert.assertEquals(int.class, TypeUtils.getArrayComponentType(int[].class));
        Assert.assertEquals(char.class, TypeUtils.getArrayComponentType(char[].class));
        Assert.assertEquals(long.class, TypeUtils.getArrayComponentType(long[].class));
        Assert.assertEquals(float.class, TypeUtils.getArrayComponentType(float[].class));
        Assert.assertEquals(double.class, TypeUtils.getArrayComponentType(double[].class));

        Assert.assertNull(TypeUtils.getArrayComponentType(boolean.class));
        Assert.assertNull(TypeUtils.getArrayComponentType(byte.class));
        Assert.assertNull(TypeUtils.getArrayComponentType(short.class));
        Assert.assertNull(TypeUtils.getArrayComponentType(int.class));
        Assert.assertNull(TypeUtils.getArrayComponentType(char.class));
        Assert.assertNull(TypeUtils.getArrayComponentType(long.class));
        Assert.assertNull(TypeUtils.getArrayComponentType(float.class));
        Assert.assertNull(TypeUtils.getArrayComponentType(double.class));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testGetArrayComponentType
    public void testGetArrayComponentType() throws Exception {
        Method method = getClass().getMethod("dummyMethod", List.class, List.class, List.class,
                List.class, List.class, List.class, List.class, List[].class, List[].class,
                List[].class, List[].class, List[].class, List[].class, List[].class);

        Type[] types = method.getGenericParameterTypes();

        Assert.assertNull(TypeUtils.getArrayComponentType(types[0]));
        Assert.assertNull(TypeUtils.getArrayComponentType(types[1]));
        Assert.assertNull(TypeUtils.getArrayComponentType(types[2]));
        Assert.assertNull(TypeUtils.getArrayComponentType(types[3]));
        Assert.assertNull(TypeUtils.getArrayComponentType(types[4]));
        Assert.assertNull(TypeUtils.getArrayComponentType(types[5]));
        Assert.assertNull(TypeUtils.getArrayComponentType(types[6]));
        Assert.assertEquals(types[0], TypeUtils.getArrayComponentType(types[7]));
        Assert.assertEquals(types[1], TypeUtils.getArrayComponentType(types[8]));
        Assert.assertEquals(types[2], TypeUtils.getArrayComponentType(types[9]));
        Assert.assertEquals(types[3], TypeUtils.getArrayComponentType(types[10]));
        Assert.assertEquals(types[4], TypeUtils.getArrayComponentType(types[11]));
        Assert.assertEquals(types[5], TypeUtils.getArrayComponentType(types[12]));
        Assert.assertEquals(types[6], TypeUtils.getArrayComponentType(types[13]));
    }

// org.apache.commons.lang3.reflect.TypeUtilsTest::testLang820
    public void testLang820() throws Exception {
        Type[] typeArray = {String.class, String.class};
        Type[] expectedArray = {String.class};
        Assert.assertArrayEquals(expectedArray, TypeUtils.normalizeUpperBounds(typeArray));
    }

// org.apache.commons.lang3.text.translate.LookupTranslatorTest::testBasicLookup
    public void testBasicLookup() throws IOException {
        LookupTranslator lt = new LookupTranslator(new CharSequence[][] { { "one", "two" } });
        StringWriter out = new StringWriter();
        int result = lt.translate("one", 0, out);
        assertEquals("Incorrect codepoint consumption", 3, result);
        assertEquals("Incorrect value", "two", out.toString());
    }

// org.apache.commons.lang3.text.translate.NumericEntityEscaperTest::testBelow
    public void testBelow() {
        NumericEntityEscaper nee = NumericEntityEscaper.below('F');

        String input = "ADFGZ";
        String result = nee.translate(input);
        assertEquals("Failed to escape numeric entities via the below method", "&#65;&#68;FGZ", result);
    }

// org.apache.commons.lang3.text.translate.NumericEntityEscaperTest::testBetween
    public void testBetween() {
        NumericEntityEscaper nee = NumericEntityEscaper.between('F', 'L');

        String input = "ADFGZ";
        String result = nee.translate(input);
        assertEquals("Failed to escape numeric entities via the between method", "AD&#70;&#71;Z", result);
    }

// org.apache.commons.lang3.text.translate.NumericEntityEscaperTest::testAbove
    public void testAbove() {
        NumericEntityEscaper nee = NumericEntityEscaper.above('F');

        String input = "ADFGZ";
        String result = nee.translate(input);
        assertEquals("Failed to escape numeric entities via the above method", "ADF&#71;&#90;", result);
    }

// org.apache.commons.lang3.text.translate.NumericEntityEscaperTest::testSupplementary
    public void testSupplementary() {
        NumericEntityEscaper nee = new NumericEntityEscaper();
        String input = "\uD803\uDC22";
        String expected = "&#68642;";

        String result = nee.translate(input);
        assertEquals("Failed to escape numeric entities supplementary characters", expected, result);

    }

// org.apache.commons.lang3.text.translate.NumericEntityUnescaperTest::testSupplementaryUnescaping
    public void testSupplementaryUnescaping() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#68642;";
        String expected = "\uD803\uDC22";

        String result = neu.translate(input);
        assertEquals("Failed to unescape numeric entities supplementary characters", expected, result);
    }

// org.apache.commons.lang3.text.translate.NumericEntityUnescaperTest::testOutOfBounds
    public void testOutOfBounds() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();

        assertEquals("Failed to ignore when last character is &", "Test &", neu.translate("Test &"));
        assertEquals("Failed to ignore when last character is &", "Test &#", neu.translate("Test &#"));
        assertEquals("Failed to ignore when last character is &", "Test &#x", neu.translate("Test &#x"));
        assertEquals("Failed to ignore when last character is &", "Test &#X", neu.translate("Test &#X"));
    }

// org.apache.commons.lang3.text.translate.NumericEntityUnescaperTest::testUnfinishedEntity
    public void testUnfinishedEntity() {
        
        NumericEntityUnescaper neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.semiColonOptional);
        String input = "Test &#x30 not test";
        String expected = "Test \u0030 not test";

        String result = neu.translate(input);
        assertEquals("Failed to support unfinished entities (i.e. missing semi-colon)", expected, result);

        
        neu = new NumericEntityUnescaper();
        input = "Test &#x30 not test";
        expected = input;

        result = neu.translate(input);
        assertEquals("Failed to ignore unfinished entities (i.e. missing semi-colon)", expected, result);

        
        neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.errorIfNoSemiColon);
        input = "Test &#x30 not test";

        try {
            result = neu.translate(input);
            fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.lang3.text.translate.OctalUnescaperTest::testBetween
    public void testBetween() {
        OctalUnescaper oue = new OctalUnescaper();   

        String input = "\\45";
        String result = oue.translate(input);
        assertEquals("Failed to unescape octal characters via the between method", "\45", result);

        input = "\\377";
        result = oue.translate(input);
        assertEquals("Failed to unescape octal characters via the between method", "\377", result);

        input = "\\377 and";
        result = oue.translate(input);
        assertEquals("Failed to unescape octal characters via the between method", "\377 and", result);

        input = "\\378 and";
        result = oue.translate(input);
        assertEquals("Failed to unescape octal characters via the between method", "\378 and", result);

        input = "\\378";
        result = oue.translate(input);
        assertEquals("Failed to unescape octal characters via the between method", "\378", result);

        input = "\\1";
        result = oue.translate(input);
        assertEquals("Failed to unescape octal characters via the between method", "\1", result);
    }

// org.apache.commons.lang3.text.translate.UnicodeEscaperTest::testBelow
    public void testBelow() {
        UnicodeEscaper ue = UnicodeEscaper.below('F');

        String input = "ADFGZ";
        String result = ue.translate(input);
        assertEquals("Failed to escape Unicode characters via the below method", "\\u0041\\u0044FGZ", result);
    }

// org.apache.commons.lang3.text.translate.UnicodeEscaperTest::testBetween
    public void testBetween() {
        UnicodeEscaper ue = UnicodeEscaper.between('F', 'L');

        String input = "ADFGZ";
        String result = ue.translate(input);
        assertEquals("Failed to escape Unicode characters via the between method", "AD\\u0046\\u0047Z", result);
    }

// org.apache.commons.lang3.text.translate.UnicodeEscaperTest::testAbove
    public void testAbove() {
        UnicodeEscaper ue = UnicodeEscaper.above('F');

        String input = "ADFGZ";
        String result = ue.translate(input);
        assertEquals("Failed to escape Unicode characters via the above method", "ADF\\u0047\\u005A", result);
    }

// org.apache.commons.lang3.text.translate.UnicodeUnescaperTest::testUPlus
    public void testUPlus() {
        UnicodeUnescaper uu = new UnicodeUnescaper();

        String input = "\\u+0047";
        assertEquals("Failed to unescape Unicode characters with 'u+' notation", "G", uu.translate(input));
    }

// org.apache.commons.lang3.text.translate.UnicodeUnescaperTest::testUuuuu
    public void testUuuuu() {
        UnicodeUnescaper uu = new UnicodeUnescaper();

        String input = "\\uuuuuuuu0047";
        String result = uu.translate(input);
        assertEquals("Failed to unescape Unicode characters with many 'u' characters", "G", result);
    }

// org.apache.commons.lang3.text.translate.UnicodeUnescaperTest::testLessThanFour
    public void testLessThanFour() {
        UnicodeUnescaper uu = new UnicodeUnescaper();

        String input = "\\0047\\u006";
        try {
            uu.translate(input);
            fail("A lack of digits in a Unicode escape sequence failed to throw an exception");
        } catch(IllegalArgumentException iae) {
            
        }
    }
