// buggy code
    public int translate(CharSequence input, int index, Writer out) throws IOException {
        int seqEnd = input.length();
        // Uses -2 to ensure there is something after the &#
        if(input.charAt(index) == '&' && index < seqEnd - 1 && input.charAt(index + 1) == '#') {
            int start = index + 2;
            boolean isHex = false;

            char firstChar = input.charAt(start);
            if(firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;

                // Check there's more than just an x after the &#
            }

            int end = start;
            // Note that this supports character codes without a ; on the end
            while(input.charAt(end) != ';') 
            {
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
            System.err.println("FAIL: " + input.subSequence(start, end) + "[" + start +"]["+ end +"]");
                return 0;
            }

            if(entityValue > 0xFFFF) {
                char[] chrs = Character.toChars(entityValue);
                out.write(chrs[0]);
                out.write(chrs[1]);
            } else {
                out.write(entityValue);
            }


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
        Assert.assertTrue("WRONG!", TypeUtils.isAssignable(dingType, disType));
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
        lo = new Integer(0);
        Assert.assertTrue(TypeUtils.isAssignable(Integer.class, long.class));
        
        Assert.assertFalse(TypeUtils.isAssignable(int.class, Long.class));
        
        Assert.assertFalse(TypeUtils.isAssignable(Integer.class, Long.class));
        in = new Integer(0);
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
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "Test &#x30 not test";
        String expected = "Test \u0030 not test";

        String result = neu.translate(input);
        assertEquals("Failed to support unfinished entities (i.e. missing semi-colon", expected, result);
    }
