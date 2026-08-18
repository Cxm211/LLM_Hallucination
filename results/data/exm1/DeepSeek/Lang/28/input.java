// buggy code
    public int translate(CharSequence input, int index, Writer out) throws IOException {
        // TODO: Protect from ArrayIndexOutOfBounds
        if(input.charAt(index) == '&' && input.charAt(index + 1) == '#') {
            int start = index + 2;
            boolean isHex = false;

            char firstChar = input.charAt(start);
            if(firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;
            }

            int end = start;
            while(input.charAt(end) != ';') {
                end++;
            }

            int entityValue;
            try {
                if(isHex) {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 16);
                } else {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 10);
                }
            } catch(NumberFormatException nfe) {
                return 0;
            }

                out.write(entityValue);
            return 2 + (end - start) + (isHex ? 1 : 0) + 1;
        }
        return 0;
    }

// relevant test
// org.apache.commons.lang3.StringEscapeUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new StringEscapeUtils());
        Constructor<?>[] cons = StringEscapeUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(StringEscapeUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(StringEscapeUtils.class.getModifiers()));
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
        assertEscapeJava("Should use capitalized unicode hex", "\\uABCD", "\uabcd");

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
        
        assertUnescapeJava("lowercase unicode", "\uABCDx", "\\uabcdx");
        assertUnescapeJava("uppercase unicode", "\uABCDx", "\\uABCDx");
        assertUnescapeJava("unicode as final character", "\uABCD", "\\uabcd");
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
        for (int i = 0; i < htmlEscapes.length; ++i) {
            String message = htmlEscapes[i][0];
            String expected = htmlEscapes[i][1];
            String original = htmlEscapes[i][2];
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
        for (int i = 0; i < htmlEscapes.length; ++i) {
            String message = htmlEscapes[i][0];
            String expected = htmlEscapes[i][2];
            String original = htmlEscapes[i][1];
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
    public void testUnescapeUnknownEntity() throws Exception
    {
        assertEquals("&zzzz;", StringEscapeUtils.unescapeHtml4("&zzzz;"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHtmlVersions
    public void testEscapeHtmlVersions() throws Exception
    {
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

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeCsvWriter
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

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeCsvString
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

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeCsvWriter
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

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHtmlHighUnicode
    public void testEscapeHtmlHighUnicode() throws java.io.UnsupportedEncodingException {
        
        
        
        
        byte[] data = new byte[] { (byte)0xF0, (byte)0x9D, (byte)0x8D, (byte)0xA2 };

        String original = new String(data, "UTF8");

        String escaped = StringEscapeUtils.escapeHtml4( original );
        assertEquals( "High unicode should not have been escaped", original, escaped);

        String unescaped = StringEscapeUtils.unescapeHtml4( escaped );
        assertEquals( "High unicode should have been unchanged", original, unescaped);

    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHiragana
    public void testEscapeHiragana() {
        
        String original = "\u304B\u304C\u3068";
        String escaped = StringEscapeUtils.escapeHtml4(original);
        assertEquals( "Hiragana character unicode behaviour should not be being escaped by escapeHtml4",
        original, escaped);

        String unescaped = StringEscapeUtils.unescapeHtml4( escaped );

        assertEquals( "Hiragana character unicode behaviour has changed - expected no unescaping", escaped, unescaped);
    }

// org.apache.commons.lang3.text.translate.NumericEntityUnescaperTest::testSupplementaryUnescaping
    public void testSupplementaryUnescaping() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#68642;";
        String expected = "\uD803\uDC22";

        String result = neu.translate(input);
        assertEquals("Failed to unescape numeric entities supplementary characters", expected, result);
    }
