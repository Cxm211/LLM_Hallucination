// buggy code
    public static <T> T[] addAll(T[] array1, T... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final Class<?> type1 = array1.getClass().getComponentType();
        T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            // Check if problem is incompatible types
        return joinedArray;
    }

// relevant test
// org.apache.commons.lang3.text.StrBuilderTest::testLang412Right
    public void testLang412Right() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 10, '*');
        assertEquals( "Failed to invoke appendFixedWidthPadRight correctly", "**********", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLang412Left
    public void testLang412Left() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 10, '*');
        assertEquals( "Failed to invoke appendFixedWidthPadLeft correctly", "**********", sb.toString());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::test1
    public void test1() {

        String input = "a;b;c;\"d;\"\"e\";f; ; ;  ";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d;\"e", "f", "", "", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test2
    public void test2() {

        String input = "a;b;c ;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c ", "d;\"e", "f", " ", " ", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test3
    public void test3() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", " c", "d;\"e", "f", " ", " ", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test4
    public void test4() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(true);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d;\"e", "f",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test5
    public void test5() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d;\"e", "f", null, null, null,};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test6
    public void test6() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", " c", "d;\"e", "f", null, null, null,};

        int nextCount = 0;
        while (tok.hasNext()) {
            tok.next();
            nextCount++;
        }

        int prevCount = 0;
        while (tok.hasPrevious()) {
            tok.previous();
            prevCount++;
        }

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);

        assertTrue("could not cycle through entire token list" + " using the 'hasNext' and 'next' methods",
                nextCount == expected.length);

        assertTrue("could not cycle through entire token list" + " using the 'hasPrevious' and 'previous' methods",
                prevCount == expected.length);

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test7
    public void test7() {

        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterMatcher(StrMatcher.spaceMatcher());
        tok.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "", "", "b", "c", "d e", "f", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test8
    public void test8() {

        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterMatcher(StrMatcher.spaceMatcher());
        tok.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(true);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d e", "f",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic1
    public void testBasic1() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic2
    public void testBasic2() {
        String input = "a \nb\fc";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic3
    public void testBasic3() {
        String input = "a \nb\u0001\fc";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b\u0001", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic4
    public void testBasic4() {
        String input = "a \"b\" c";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("\"b\"", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic5
    public void testBasic5() {
        String input = "a:b':c";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        assertEquals("a", tok.next());
        assertEquals("b'", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicDelim1
    public void testBasicDelim1() {
        String input = "a:b:c";
        StrTokenizer tok = new StrTokenizer(input, ':');
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicDelim2
    public void testBasicDelim2() {
        String input = "a:b:c";
        StrTokenizer tok = new StrTokenizer(input, ',');
        assertEquals("a:b:c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicEmpty1
    public void testBasicEmpty1() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setIgnoreEmptyTokens(false);
        assertEquals("a", tok.next());
        assertEquals("", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicEmpty2
    public void testBasicEmpty2() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals(null, tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted1
    public void testBasicQuoted1() {
        String input = "a 'b' c";
        StrTokenizer tok = new StrTokenizer(input, ' ', '\'');
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted2
    public void testBasicQuoted2() {
        String input = "a:'b':";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted3
    public void testBasicQuoted3() {
        String input = "a:'b''c'";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b'c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted4
    public void testBasicQuoted4() {
        String input = "a: 'b' 'c' :d";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b c", tok.next());
        assertEquals("d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted5
    public void testBasicQuoted5() {
        String input = "a: 'b'x'c' :d";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bxc", tok.next());
        assertEquals("d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted6
    public void testBasicQuoted6() {
        String input = "a:'b'\"c':d";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setQuoteMatcher(StrMatcher.quoteMatcher());
        assertEquals("a", tok.next());
        assertEquals("b\"c:d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted7
    public void testBasicQuoted7() {
        String input = "a:\"There's a reason here\":b";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setQuoteMatcher(StrMatcher.quoteMatcher());
        assertEquals("a", tok.next());
        assertEquals("There's a reason here", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuotedTrimmed1
    public void testBasicQuotedTrimmed1() {
        String input = "a: 'b' :";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicTrimmed1
    public void testBasicTrimmed1() {
        String input = "a: b :  ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicTrimmed2
    public void testBasicTrimmed2() {
        String input = "a:  b  :";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setTrimmerMatcher(StrMatcher.stringMatcher("  "));
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed1
    public void testBasicIgnoreTrimmed1() {
        String input = "a: bIGNOREc : ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bc", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed2
    public void testBasicIgnoreTrimmed2() {
        String input = "IGNOREaIGNORE: IGNORE bIGNOREc IGNORE : IGNORE ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bc", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed3
    public void testBasicIgnoreTrimmed3() {
        String input = "IGNOREaIGNORE: IGNORE bIGNOREc IGNORE : IGNORE ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("  bc  ", tok.next());
        assertEquals("  ", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed4
    public void testBasicIgnoreTrimmed4() {
        String input = "IGNOREaIGNORE: IGNORE 'bIGNOREc'IGNORE'd' IGNORE : IGNORE ";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bIGNOREcd", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testListArray
    public void testListArray() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        String[] array = tok.getTokenArray();
        List<?> list = tok.getTokenList();
        
        assertEquals(Arrays.asList(array), list);
        assertEquals(3, list.size());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSV
    public void testCSV(String data) {
        this.testXSVAbc(StrTokenizer.getCSVInstance(data));
        this.testXSVAbc(StrTokenizer.getCSVInstance(data.toCharArray()));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSVEmpty
    public void testCSVEmpty() {
        this.testEmpty(StrTokenizer.getCSVInstance());
        this.testEmpty(StrTokenizer.getCSVInstance(""));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSVSimple
    public void testCSVSimple() {
        this.testCSV(CSV_SIMPLE_FIXTURE);
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSVSimpleNeedsTrim
    public void testCSVSimpleNeedsTrim() {
        this.testCSV("   " + CSV_SIMPLE_FIXTURE);
        this.testCSV("   \n\t  " + CSV_SIMPLE_FIXTURE);
        this.testCSV("   \n  " + CSV_SIMPLE_FIXTURE + "\n\n\r");
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testGetContent
    public void testGetContent() {
        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals(input, tok.getContent());

        tok = new StrTokenizer(input.toCharArray());
        assertEquals(input, tok.getContent());
        
        tok = new StrTokenizer();
        assertEquals(null, tok.getContent());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testChaining
    public void testChaining() {
        StrTokenizer tok = new StrTokenizer();
        assertEquals(tok, tok.reset());
        assertEquals(tok, tok.reset(""));
        assertEquals(tok, tok.reset(new char[0]));
        assertEquals(tok, tok.setDelimiterChar(' '));
        assertEquals(tok, tok.setDelimiterString(" "));
        assertEquals(tok, tok.setDelimiterMatcher(null));
        assertEquals(tok, tok.setQuoteChar(' '));
        assertEquals(tok, tok.setQuoteMatcher(null));
        assertEquals(tok, tok.setIgnoredChar(' '));
        assertEquals(tok, tok.setIgnoredMatcher(null));
        assertEquals(tok, tok.setTrimmerMatcher(null));
        assertEquals(tok, tok.setEmptyTokenAsNull(false));
        assertEquals(tok, tok.setIgnoreEmptyTokens(false));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCloneNotSupportedException
    public void testCloneNotSupportedException() {
        Object notCloned = (new StrTokenizer() {
            @Override
            Object cloneReset() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("test");
            }
        }).clone();
        assertNull(notCloned);
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCloneNull
    public void testCloneNull() {
        StrTokenizer tokenizer = new StrTokenizer((char[]) null);
        
        assertEquals(null, tokenizer.nextToken());
        tokenizer.reset();
        assertEquals(null, tokenizer.nextToken());
        
        StrTokenizer clonedTokenizer = (StrTokenizer) tokenizer.clone();
        tokenizer.reset();
        assertEquals(null, tokenizer.nextToken());
        assertEquals(null, clonedTokenizer.nextToken());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCloneReset
    public void testCloneReset() {
        char[] input = new char[]{'a'};
        StrTokenizer tokenizer = new StrTokenizer(input);
        
        assertEquals("a", tokenizer.nextToken());
        tokenizer.reset(input);
        assertEquals("a", tokenizer.nextToken());
        
        StrTokenizer clonedTokenizer = (StrTokenizer) tokenizer.clone();
        input[0] = 'b';
        tokenizer.reset(input);
        assertEquals("b", tokenizer.nextToken());
        assertEquals("a", clonedTokenizer.nextToken());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_String
    public void testConstructor_String() {
        StrTokenizer tok = new StrTokenizer("a b");
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer("");
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((String) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_String_char
    public void testConstructor_String_char() {
        StrTokenizer tok = new StrTokenizer("a b", ' ');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer("", ' ');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((String) null, ' ');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_String_char_char
    public void testConstructor_String_char_char() {
        StrTokenizer tok = new StrTokenizer("a b", ' ', '"');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals(1, tok.getQuoteMatcher().isMatch("\"".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer("", ' ', '"');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((String) null, ' ', '"');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_charArray
    public void testConstructor_charArray() {
        StrTokenizer tok = new StrTokenizer("a b".toCharArray());
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer(new char[0]);
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((char[]) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_charArray_char
    public void testConstructor_charArray_char() {
        StrTokenizer tok = new StrTokenizer("a b".toCharArray(), ' ');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer(new char[0], ' ');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((char[]) null, ' ');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_charArray_char_char
    public void testConstructor_charArray_char_char() {
        StrTokenizer tok = new StrTokenizer("a b".toCharArray(), ' ', '"');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals(1, tok.getQuoteMatcher().isMatch("\"".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer(new char[0], ' ', '"');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((char[]) null, ' ', '"');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testReset
    public void testReset() {
        StrTokenizer tok = new StrTokenizer("a b c");
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset();
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testReset_String
    public void testReset_String() {
        StrTokenizer tok = new StrTokenizer("x x x");
        tok.reset("d e");
        assertEquals("d", tok.next());
        assertEquals("e", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset((String) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testReset_charArray
    public void testReset_charArray() {
        StrTokenizer tok = new StrTokenizer("x x x");
        
        char[] array = new char[] {'a', 'b', 'c'};
        tok.reset(array);
        assertEquals("abc", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset((char[]) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTSV
    public void testTSV() {
        this.testXSVAbc(StrTokenizer.getTSVInstance(TSV_SIMPLE_FIXTURE));
        this.testXSVAbc(StrTokenizer.getTSVInstance(TSV_SIMPLE_FIXTURE.toCharArray()));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTSVEmpty
    public void testTSVEmpty() {
        this.testEmpty(StrTokenizer.getCSVInstance());
        this.testEmpty(StrTokenizer.getCSVInstance(""));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testIteration
    public void testIteration() {
        StrTokenizer tkn = new StrTokenizer("a b c");
        assertEquals(false, tkn.hasPrevious());
        try {
            tkn.previous();
            fail();
        } catch (NoSuchElementException ex) {}
        assertEquals(true, tkn.hasNext());
        
        assertEquals("a", tkn.next());
        try {
            tkn.remove();
            fail();
        } catch (UnsupportedOperationException ex) {}
        try {
            tkn.set("x");
            fail();
        } catch (UnsupportedOperationException ex) {}
        try {
            tkn.add("y");
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertEquals(true, tkn.hasPrevious());
        assertEquals(true, tkn.hasNext());
        
        assertEquals("b", tkn.next());
        assertEquals(true, tkn.hasPrevious());
        assertEquals(true, tkn.hasNext());
        
        assertEquals("c", tkn.next());
        assertEquals(true, tkn.hasPrevious());
        assertEquals(false, tkn.hasNext());
        
        try {
            tkn.next();
            fail();
        } catch (NoSuchElementException ex) {}
        assertEquals(true, tkn.hasPrevious());
        assertEquals(false, tkn.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTokenizeSubclassInputChange
    public void testTokenizeSubclassInputChange() {
        StrTokenizer tkn = new StrTokenizer("a b c d e") {
            @Override
            protected List<String> tokenize(char[] chars, int offset, int count) {
                return super.tokenize("w x y z".toCharArray(), 2, 5);
            }
        };
        assertEquals("x", tkn.next());
        assertEquals("y", tkn.next());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTokenizeSubclassOutputChange
    public void testTokenizeSubclassOutputChange() {
        StrTokenizer tkn = new StrTokenizer("a b c") {
            @Override
            protected List<String> tokenize(char[] chars, int offset, int count) {
                List<String> list = super.tokenize(chars, offset, count);
                Collections.reverse(list);
                return list;
            }
        };
        assertEquals("c", tkn.next());
        assertEquals("b", tkn.next());
        assertEquals("a", tkn.next());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testToString
    public void testToString() {
        StrTokenizer tkn = new StrTokenizer("a b c d e");
        assertEquals("StrTokenizer[not tokenized yet]", tkn.toString());
        tkn.next();
        assertEquals("StrTokenizer[a, b, c, d, e]", tkn.toString());
    }
