// buggy code
	public static boolean containsAny(CharSequence cs, char[] searchChars) {
		if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
			return false;
		}
		int csLength = cs.length();
		int searchLength = searchChars.length;
		for (int i = 0; i < csLength; i++) {
			char ch = cs.charAt(i);
			for (int j = 0; j < searchLength; j++) {
				if (searchChars[j] == ch) {
						// ch is a supplementary character
						// ch is in the Basic Multilingual Plane
						return true;
				}
			}
		}
		return false;
	}

// relevant test
// org.apache.commons.lang3.StringUtilsSubstringTest::testSubstringBetween_StringStringString
    public void testSubstringBetween_StringStringString() {
        assertEquals(null, StringUtils.substringBetween(null, "", ""));
        assertEquals(null, StringUtils.substringBetween("", null, ""));
        assertEquals(null, StringUtils.substringBetween("", "", null));
        assertEquals("", StringUtils.substringBetween("", "", ""));
        assertEquals("", StringUtils.substringBetween("foo", "", ""));
        assertEquals(null, StringUtils.substringBetween("foo", "", "]"));
        assertEquals(null, StringUtils.substringBetween("foo", "[", "]"));
        assertEquals("", StringUtils.substringBetween("    ", " ", "  "));
        assertEquals("bar", StringUtils.substringBetween("<foo>bar</foo>", "<foo>", "</foo>") );
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testSubstringsBetween_StringStringString
    public void testSubstringsBetween_StringStringString() {

        String[] results = StringUtils.substringsBetween("[one], [two], [three]", "[", "]");
        assertEquals(3, results.length);
        assertEquals("one", results[0]);
        assertEquals("two", results[1]);
        assertEquals("three", results[2]);

        results = StringUtils.substringsBetween("[one], [two], three", "[", "]");
        assertEquals(2, results.length);
        assertEquals("one", results[0]);
        assertEquals("two", results[1]);

        results = StringUtils.substringsBetween("[one], [two], three]", "[", "]");
        assertEquals(2, results.length);
        assertEquals("one", results[0]);
        assertEquals("two", results[1]);

        results = StringUtils.substringsBetween("[one], two], three]", "[", "]");
        assertEquals(1, results.length);
        assertEquals("one", results[0]);

        results = StringUtils.substringsBetween("one], two], [three]", "[", "]");
        assertEquals(1, results.length);
        assertEquals("three", results[0]);

        
        
        results = StringUtils.substringsBetween("aabhellobabnonba", "ab", "ba");
        assertEquals(1, results.length);
        assertEquals("hello", results[0]);

        results = StringUtils.substringsBetween("one, two, three", "[", "]");
        assertNull(results);

        results = StringUtils.substringsBetween("[one, two, three", "[", "]");
        assertNull(results);

        results = StringUtils.substringsBetween("one, two, three]", "[", "]");
        assertNull(results);

        results = StringUtils.substringsBetween("[one], [two], [three]", "[", null);
        assertNull(results);

        results = StringUtils.substringsBetween("[one], [two], [three]", null, "]");
        assertNull(results);

        results = StringUtils.substringsBetween("[one], [two], [three]", "", "");
        assertNull(results);

        results = StringUtils.substringsBetween(null, "[", "]");
        assertNull(results);

        results = StringUtils.substringsBetween("", "[", "]");
        assertEquals(0, results.length);
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testCountMatches_String
    public void testCountMatches_String() {
        assertEquals(0, StringUtils.countMatches(null, null));
        assertEquals(0, StringUtils.countMatches("blah", null));
        assertEquals(0, StringUtils.countMatches(null, "DD"));

        assertEquals(0, StringUtils.countMatches("x", ""));
        assertEquals(0, StringUtils.countMatches("", ""));

        assertEquals(3, 
             StringUtils.countMatches("one long someone sentence of one", "one"));
        assertEquals(0, 
             StringUtils.countMatches("one long someone sentence of one", "two"));
        assertEquals(4, 
             StringUtils.countMatches("oooooooooooo", "ooo"));
    }

// org.apache.commons.lang3.StringUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new StringUtils());
        Constructor<?>[] cons = StringUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(StringUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(StringUtils.class.getModifiers()));
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
        assertEquals("capitalize(single-char-string) failed",
                "X", StringUtils.capitalize(new StringBuilder("x")) );
        assertEquals("capitalize(single-char-string) failed",
                "X", StringUtils.capitalize(new StringBuffer("x")) );
        assertEquals("capitalize(single-char-string) failed",
                "X", StringUtils.capitalize(CharBuffer.wrap("x")) );
        
        assertEquals("uncapitalize(String) failed",
                     FOO_UNCAP, StringUtils.uncapitalize(FOO_CAP) );
        assertEquals("uncapitalize(empty-string) failed",
                     "", StringUtils.uncapitalize("") );
        assertEquals("uncapitalize(single-char-string) failed",
                "x", StringUtils.uncapitalize("X") );
        assertEquals("uncapitalize(single-char-string) failed",
                "x", StringUtils.uncapitalize(new StringBuilder("X")) );
        assertEquals("uncapitalize(single-char-string) failed",
                "x", StringUtils.uncapitalize(new StringBuffer("X")) );
        assertEquals("uncapitalize(single-char-string) failed",
                "x", StringUtils.uncapitalize(CharBuffer.wrap("X")) );
                     
        
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

// org.apache.commons.lang3.StringUtilsTest::testJoin_Objectarray
    public void testJoin_Objectarray() {
        assertEquals(null, StringUtils.join(null));
        assertEquals("", StringUtils.join(EMPTY_ARRAY_LIST));
        assertEquals("", StringUtils.join(NULL_ARRAY_LIST));
        assertEquals("abc", StringUtils.join(new String[] {"a", "b", "c"}));
        assertEquals("a", StringUtils.join(new String[] {null, "a", ""}));
        assertEquals("foo", StringUtils.join(MIXED_ARRAY_LIST));
        assertEquals("foo2", StringUtils.join(MIXED_TYPE_LIST));
    }

// org.apache.commons.lang3.StringUtilsTest::testJoin_ArrayChar
    public void testJoin_ArrayChar() {
        assertEquals(null, StringUtils.join((Object[]) null, ','));
        assertEquals(TEXT_LIST_CHAR, StringUtils.join(ARRAY_LIST, SEPARATOR_CHAR));
        assertEquals("", StringUtils.join(EMPTY_ARRAY_LIST, SEPARATOR_CHAR));
        assertEquals(";;foo", StringUtils.join(MIXED_ARRAY_LIST, SEPARATOR_CHAR));
        assertEquals("foo;2", StringUtils.join(MIXED_TYPE_LIST, SEPARATOR_CHAR));

        assertEquals("/", StringUtils.join(MIXED_ARRAY_LIST, '/', 0, MIXED_ARRAY_LIST.length-1));
        assertEquals("foo", StringUtils.join(MIXED_TYPE_LIST, '/', 0, 1));
        assertEquals("foo/2", StringUtils.join(MIXED_TYPE_LIST, '/', 0, 2));
        assertEquals("2", StringUtils.join(MIXED_TYPE_LIST, '/', 1, 2));
        assertEquals("", StringUtils.join(MIXED_TYPE_LIST, '/', 2, 1));
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
        assertEquals(null, StringUtils.split(null));
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
        assertEquals(null, StringUtils.split(null, '.'));
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
        assertEquals(null, StringUtils.split(null, "."));
        assertEquals(null, StringUtils.split(null, ".", 3));
        
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
        
        String[] results = null;
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
        assertEquals( null, StringUtils.splitByWholeSeparator( null, "." ) ) ;

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
        assertEquals( null, StringUtils.splitByWholeSeparator( null, ".", 3 ) ) ;

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
        assertEquals( null, StringUtils.splitByWholeSeparatorPreserveAllTokens( null, ".", -1 ) ) ;

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
        assertEquals(null, StringUtils.splitPreserveAllTokens(null));
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
        assertEquals(null, StringUtils.splitPreserveAllTokens(null, '.'));
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
          String[] results = null;
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
        assertEquals(null, StringUtils.splitPreserveAllTokens(null, "."));
        assertEquals(null, StringUtils.splitPreserveAllTokens(null, ".", 3));
        
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
          String[] results = null;
          String[] expectedResults = {"ab", "de fg"};
          results = StringUtils.splitPreserveAllTokens("ab de fg", null, 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results = null;
          String[] expectedResults = {"ab", "  de fg"};
          results = StringUtils.splitPreserveAllTokens("ab   de fg", null, 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
        
        {
          String[] results = null;
          String[] expectedResults = {"ab", "::de:fg"};
          results = StringUtils.splitPreserveAllTokens("ab:::de:fg", ":", 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
        
        {
          String[] results = null;
          String[] expectedResults = {"ab", "", " de fg"};
          results = StringUtils.splitPreserveAllTokens("ab   de fg", null, 3);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
        
        {
          String[] results = null;
          String[] expectedResults = {"ab", "", "", "de fg"};
          results = StringUtils.splitPreserveAllTokens("ab   de fg", null, 4);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] expectedResults = {"ab", "cd:ef"};
          String[] results = null;
          results = StringUtils.splitPreserveAllTokens("ab:cd:ef",":", 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results = null;
          String[] expectedResults = {"ab", ":cd:ef"};
          results = StringUtils.splitPreserveAllTokens("ab::cd:ef",":", 2);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results = null;
          String[] expectedResults = {"ab", "", ":cd:ef"};
          results = StringUtils.splitPreserveAllTokens("ab:::cd:ef",":", 3);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results = null;
          String[] expectedResults = {"ab", "", "", "cd:ef"};
          results = StringUtils.splitPreserveAllTokens("ab:::cd:ef",":", 4);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }

        {
          String[] results = null;
          String[] expectedResults = {"", "ab", "", "", "cd:ef"};
          results = StringUtils.splitPreserveAllTokens(":ab:::cd:ef",":", 5);
          assertEquals(expectedResults.length, results.length);
          for (int i = 0; i < expectedResults.length; i++) {
              assertEquals(expectedResults[i], results[i]);
          }
        }
        
        {
          String[] results = null;
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
        assertEquals(true, StringUtils.containsOnly(str, new char[] {'a'}));
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
        for (int i = 0; i < chopCases.length; i++) {
            String original = chopCases[i][0];
            String expectedResult = chopCases[i][1];
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
        for (int i = 0; i < chompCases.length; i++) {
            String original = chompCases[i][0];
            String expectedResult = chompCases[i][1];
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
        assertEquals(true, StringUtils.containsOnly(str, new char[] {'a'}));
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
        assertEquals(true, StringUtils.containsOnly(str, new char[] {'a'}));
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
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfEmpty_StringBuilders
    public void testDefaultIfEmpty_StringBuilders() {
        assertEquals("NULL", StringUtils.defaultIfEmpty(new StringBuilder(""), new StringBuilder("NULL")).toString());
        assertEquals("abc", StringUtils.defaultIfEmpty(new StringBuilder("abc"), new StringBuilder("NULL")).toString());
        assertNull(StringUtils.defaultIfEmpty(new StringBuilder(""), null));
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfEmpty_StringBuffers
    public void testDefaultIfEmpty_StringBuffers() {
        assertEquals("NULL", StringUtils.defaultIfEmpty(new StringBuffer(""), new StringBuffer("NULL")).toString());
        assertEquals("abc", StringUtils.defaultIfEmpty(new StringBuffer("abc"), new StringBuffer("NULL")).toString());
        assertNull(StringUtils.defaultIfEmpty(new StringBuffer(""), null));
    }

// org.apache.commons.lang3.StringUtilsTest::testDefaultIfEmpty_CharBuffers
    public void testDefaultIfEmpty_CharBuffers() {
        assertEquals("NULL", StringUtils.defaultIfEmpty(CharBuffer.wrap(""), CharBuffer.wrap("NULL")).toString());
        assertEquals("abc", StringUtils.defaultIfEmpty(CharBuffer.wrap("abc"), CharBuffer.wrap("NULL")).toString());
        assertNull(StringUtils.defaultIfEmpty(CharBuffer.wrap(""), null));
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
    public void testDifferenceAt_StringArray(){        
        assertEquals(-1, StringUtils.indexOfDifference(null));
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
    public void testGetCommonPrefix_StringArray(){        
        assertEquals("", StringUtils.getCommonPrefix(null));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {}));
        assertEquals("abc", StringUtils.getCommonPrefix(new String[] {"abc"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {null, null}));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {"", ""}));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {"", null}));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {"abc", null, null}));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {null, null, "abc"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {"", "abc"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {"abc", ""}));
        assertEquals("abc", StringUtils.getCommonPrefix(new String[] {"abc", "abc"}));
        assertEquals("a", StringUtils.getCommonPrefix(new String[] {"abc", "a"}));
        assertEquals("ab", StringUtils.getCommonPrefix(new String[] {"ab", "abxyz"}));
        assertEquals("ab", StringUtils.getCommonPrefix(new String[] {"abcde", "abxyz"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {"abcde", "xyz"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[] {"xyz", "abcde"}));
        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[] {"i am a machine", "i am a robot"}));
    }

// org.apache.commons.lang3.StringUtilsTest::testStartsWithAny
    public void testStartsWithAny() {
        assertFalse(StringUtils.startsWithAny(null, null));
        assertFalse(StringUtils.startsWithAny(null, new String[] {"abc"}));
        assertFalse(StringUtils.startsWithAny("abcxyz", null));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[] {}));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[] {"abc"}));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[] {null, "xyz", "abc"}));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[] {null, "xyz", "abcd"}));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testIsEmpty
    public void testIsEmpty() {
        assertEquals(true, StringUtils.isEmpty(null));
        assertEquals(true, StringUtils.isEmpty(""));
        assertEquals(false, StringUtils.isEmpty(" "));
        assertEquals(false, StringUtils.isEmpty("foo"));
        assertEquals(false, StringUtils.isEmpty("  foo  "));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testIsNotEmpty
    public void testIsNotEmpty() {
        assertEquals(false, StringUtils.isNotEmpty(null));
        assertEquals(false, StringUtils.isNotEmpty(""));
        assertEquals(true, StringUtils.isNotEmpty(" "));
        assertEquals(true, StringUtils.isNotEmpty("foo"));
        assertEquals(true, StringUtils.isNotEmpty("  foo  "));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testIsBlank
    public void testIsBlank() {
        assertEquals(true, StringUtils.isBlank(null));
        assertEquals(true, StringUtils.isBlank(""));
        assertEquals(true, StringUtils.isBlank(StringUtilsTest.WHITESPACE));
        assertEquals(false, StringUtils.isBlank("foo"));
        assertEquals(false, StringUtils.isBlank("  foo  "));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testIsNotBlank
    public void testIsNotBlank() {
        assertEquals(false, StringUtils.isNotBlank(null));
        assertEquals(false, StringUtils.isNotBlank(""));
        assertEquals(false, StringUtils.isNotBlank(StringUtilsTest.WHITESPACE));
        assertEquals(true, StringUtils.isNotBlank("foo"));
        assertEquals(true, StringUtils.isNotBlank("  foo  "));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testTrim
    public void testTrim() {
        assertEquals(FOO, StringUtils.trim(FOO + "  "));
        assertEquals(FOO, StringUtils.trim(" " + FOO + "  "));
        assertEquals(FOO, StringUtils.trim(" " + FOO));
        assertEquals(FOO, StringUtils.trim(FOO + ""));
        assertEquals("", StringUtils.trim(" \t\r\n\b "));
        assertEquals("", StringUtils.trim(StringUtilsTest.TRIMMABLE));
        assertEquals(StringUtilsTest.NON_TRIMMABLE, StringUtils.trim(StringUtilsTest.NON_TRIMMABLE));
        assertEquals("", StringUtils.trim(""));
        assertEquals(null, StringUtils.trim(null));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testTrimToNull
    public void testTrimToNull() {
        assertEquals(FOO, StringUtils.trimToNull(FOO + "  "));
        assertEquals(FOO, StringUtils.trimToNull(" " + FOO + "  "));
        assertEquals(FOO, StringUtils.trimToNull(" " + FOO));
        assertEquals(FOO, StringUtils.trimToNull(FOO + ""));
        assertEquals(null, StringUtils.trimToNull(" \t\r\n\b "));
        assertEquals(null, StringUtils.trimToNull(StringUtilsTest.TRIMMABLE));
        assertEquals(StringUtilsTest.NON_TRIMMABLE, StringUtils.trimToNull(StringUtilsTest.NON_TRIMMABLE));
        assertEquals(null, StringUtils.trimToNull(""));
        assertEquals(null, StringUtils.trimToNull(null));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testTrimToEmpty
    public void testTrimToEmpty() {
        assertEquals(FOO, StringUtils.trimToEmpty(FOO + "  "));
        assertEquals(FOO, StringUtils.trimToEmpty(" " + FOO + "  "));
        assertEquals(FOO, StringUtils.trimToEmpty(" " + FOO));
        assertEquals(FOO, StringUtils.trimToEmpty(FOO + ""));
        assertEquals("", StringUtils.trimToEmpty(" \t\r\n\b "));
        assertEquals("", StringUtils.trimToEmpty(StringUtilsTest.TRIMMABLE));
        assertEquals(StringUtilsTest.NON_TRIMMABLE, StringUtils.trimToEmpty(StringUtilsTest.NON_TRIMMABLE));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty(null));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStrip_String
    public void testStrip_String() {
        assertEquals(null, StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("", StringUtils.strip("        "));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals(StringUtilsTest.NON_WHITESPACE, 
            StringUtils.strip(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripToNull_String
    public void testStripToNull_String() {
        assertEquals(null, StringUtils.stripToNull(null));
        assertEquals(null, StringUtils.stripToNull(""));
        assertEquals(null, StringUtils.stripToNull("        "));
        assertEquals(null, StringUtils.stripToNull(StringUtilsTest.WHITESPACE));
        assertEquals("ab c", StringUtils.stripToNull("  ab c  "));
        assertEquals(StringUtilsTest.NON_WHITESPACE, 
            StringUtils.stripToNull(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripToEmpty_String
    public void testStripToEmpty_String() {
        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty(""));
        assertEquals("", StringUtils.stripToEmpty("        "));
        assertEquals("", StringUtils.stripToEmpty(StringUtilsTest.WHITESPACE));
        assertEquals("ab c", StringUtils.stripToEmpty("  ab c  "));
        assertEquals(StringUtilsTest.NON_WHITESPACE, 
            StringUtils.stripToEmpty(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStrip_StringString
    public void testStrip_StringString() {
        
        assertEquals(null, StringUtils.strip(null, null));
        assertEquals("", StringUtils.strip("", null));
        assertEquals("", StringUtils.strip("        ", null));
        assertEquals("abc", StringUtils.strip("  abc  ", null));
        assertEquals(StringUtilsTest.NON_WHITESPACE, 
            StringUtils.strip(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE, null));

        
        assertEquals(null, StringUtils.strip(null, ""));
        assertEquals("", StringUtils.strip("", ""));
        assertEquals("        ", StringUtils.strip("        ", ""));
        assertEquals("  abc  ", StringUtils.strip("  abc  ", ""));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.strip(StringUtilsTest.WHITESPACE, ""));
        
        
        assertEquals(null, StringUtils.strip(null, " "));
        assertEquals("", StringUtils.strip("", " "));
        assertEquals("", StringUtils.strip("        ", " "));
        assertEquals("abc", StringUtils.strip("  abc  ", " "));
        
        
        assertEquals(null, StringUtils.strip(null, "ab"));
        assertEquals("", StringUtils.strip("", "ab"));
        assertEquals("        ", StringUtils.strip("        ", "ab"));
        assertEquals("  abc  ", StringUtils.strip("  abc  ", "ab"));
        assertEquals("c", StringUtils.strip("abcabab", "ab"));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.strip(StringUtilsTest.WHITESPACE, ""));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripStart_StringString
    public void testStripStart_StringString() {
        
        assertEquals(null, StringUtils.stripStart(null, null));
        assertEquals("", StringUtils.stripStart("", null));
        assertEquals("", StringUtils.stripStart("        ", null));
        assertEquals("abc  ", StringUtils.stripStart("  abc  ", null));
        assertEquals(StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE, 
            StringUtils.stripStart(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE, null));

        
        assertEquals(null, StringUtils.stripStart(null, ""));
        assertEquals("", StringUtils.stripStart("", ""));
        assertEquals("        ", StringUtils.stripStart("        ", ""));
        assertEquals("  abc  ", StringUtils.stripStart("  abc  ", ""));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.stripStart(StringUtilsTest.WHITESPACE, ""));
        
        
        assertEquals(null, StringUtils.stripStart(null, " "));
        assertEquals("", StringUtils.stripStart("", " "));
        assertEquals("", StringUtils.stripStart("        ", " "));
        assertEquals("abc  ", StringUtils.stripStart("  abc  ", " "));
        
        
        assertEquals(null, StringUtils.stripStart(null, "ab"));
        assertEquals("", StringUtils.stripStart("", "ab"));
        assertEquals("        ", StringUtils.stripStart("        ", "ab"));
        assertEquals("  abc  ", StringUtils.stripStart("  abc  ", "ab"));
        assertEquals("cabab", StringUtils.stripStart("abcabab", "ab"));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.stripStart(StringUtilsTest.WHITESPACE, ""));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripEnd_StringString
    public void testStripEnd_StringString() {
        
        assertEquals(null, StringUtils.stripEnd(null, null));
        assertEquals("", StringUtils.stripEnd("", null));
        assertEquals("", StringUtils.stripEnd("        ", null));
        assertEquals("  abc", StringUtils.stripEnd("  abc  ", null));
        assertEquals(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE, 
            StringUtils.stripEnd(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE, null));

        
        assertEquals(null, StringUtils.stripEnd(null, ""));
        assertEquals("", StringUtils.stripEnd("", ""));
        assertEquals("        ", StringUtils.stripEnd("        ", ""));
        assertEquals("  abc  ", StringUtils.stripEnd("  abc  ", ""));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.stripEnd(StringUtilsTest.WHITESPACE, ""));
        
        
        assertEquals(null, StringUtils.stripEnd(null, " "));
        assertEquals("", StringUtils.stripEnd("", " "));
        assertEquals("", StringUtils.stripEnd("        ", " "));
        assertEquals("  abc", StringUtils.stripEnd("  abc  ", " "));
        
        
        assertEquals(null, StringUtils.stripEnd(null, "ab"));
        assertEquals("", StringUtils.stripEnd("", "ab"));
        assertEquals("        ", StringUtils.stripEnd("        ", "ab"));
        assertEquals("  abc  ", StringUtils.stripEnd("  abc  ", "ab"));
        assertEquals("abc", StringUtils.stripEnd("abcabab", "ab"));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.stripEnd(StringUtilsTest.WHITESPACE, ""));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripAll
    public void testStripAll() {
        
        String[] empty = new String[0];
        String[] fooSpace = new String[] { "  "+FOO+"  ", "  "+FOO, FOO+"  " };
        String[] fooDots = new String[] { ".."+FOO+"..", ".."+FOO, FOO+".." };
        String[] foo = new String[] { FOO, FOO, FOO };

        assertEquals(null, StringUtils.stripAll(null));
        assertArrayEquals(empty, StringUtils.stripAll(empty));
        assertArrayEquals(foo, StringUtils.stripAll(fooSpace));
        
        assertEquals(null, StringUtils.stripAll(null, null));
        assertArrayEquals(foo, StringUtils.stripAll(fooSpace, null));
        assertArrayEquals(foo, StringUtils.stripAll(fooDots, "."));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripAccents
    public void testStripAccents() {
        if(SystemUtils.isJavaVersionAtLeast(1.6f)) {
            String cue = "\u00C7\u00FA\u00EA";
            assertEquals( "Failed to strip accents from " + cue, "Cue", StringUtils.stripAccents(cue));

            String lots = "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C7\u00C8\u00C9" + 
                          "\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D1\u00D2\u00D3" + 
                          "\u00D4\u00D5\u00D6\u00D9\u00DA\u00DB\u00DC\u00DD";
            assertEquals( "Failed to strip accents from " + lots, 
                          "AAAAAACEEEEIIIINOOOOOUUUUY", 
                          StringUtils.stripAccents(lots));

            assertNull( "Failed null safety", StringUtils.stripAccents(null) );
            assertEquals( "Failed empty String", "", StringUtils.stripAccents("") );
            assertEquals( "Failed to handle non-accented text", "control", StringUtils.stripAccents("control") );
            assertEquals( "Failed to handle easy example", "eclair", StringUtils.stripAccents("\u00E9clair") );
        } else {
            try {
                StringUtils.stripAccents("string");
                fail("Before JDK 1.6, stripAccents is not expected to work");
            } catch(UnsupportedOperationException uoe) {
                assertEquals("The stripAccents(String) method is not supported until Java 1.6", uoe.getMessage());
            }
        }
    }

// org.apache.commons.lang3.ValidateTest::testIsTrue1
    public void testIsTrue1() {
        Validate.isTrue(true);
        try {
            Validate.isTrue(false);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("The validated expression is false", ex.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testIsTrue2
    public void testIsTrue2() {
        Validate.isTrue(true, "MSG");
        try {
            Validate.isTrue(false, "MSG");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testIsTrue3
    public void testIsTrue3() {
        Validate.isTrue(true, "MSG", new Integer(6));
        try {
            Validate.isTrue(false, "MSG", new Integer(6));
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testIsTrue4
    public void testIsTrue4() {
        Validate.isTrue(true, "MSG", 7);
        try {
            Validate.isTrue(false, "MSG", 7);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testIsTrue5
    public void testIsTrue5() {
        Validate.isTrue(true, "MSG", 7.4d);
        try {
            Validate.isTrue(false, "MSG", 7.4d);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testNotNull1
    public void testNotNull1() {
        Validate.notNull(new Object());
        try {
            Validate.notNull(null);
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("The validated object is null", ex.getMessage());
        }
        
        String str = "Hi";
        String testStr = Validate.notNull(str);
        assertSame(str, testStr);
    }

// org.apache.commons.lang3.ValidateTest::testNotNull2
    public void testNotNull2() {
        Validate.notNull(new Object(), "MSG");
        try {
            Validate.notNull(null, "MSG");
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        
        String str = "Hi";
        String testStr = Validate.notNull(str, "Message");
        assertSame(str, testStr);
    }

// org.apache.commons.lang3.ValidateTest::testNotEmptyArray1
    public void testNotEmptyArray1() {
        Validate.notEmpty(new Object[] {null});
        try {
            Validate.notEmpty((Object[]) null);
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("The validated array is empty", ex.getMessage());
        }
        try {
            Validate.notEmpty(new Object[0]);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("The validated array is empty", ex.getMessage());
        }
        
        String[] array = new String[] {"hi"};
        String[] test = Validate.notEmpty(array);
        assertSame(array, test);
    }

// org.apache.commons.lang3.ValidateTest::testNotEmptyArray2
    public void testNotEmptyArray2() {
        Validate.notEmpty(new Object[] {null}, "MSG");
        try {
            Validate.notEmpty((Object[]) null, "MSG");
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        try {
            Validate.notEmpty(new Object[0], "MSG");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        
        String[] array = new String[] {"hi"};
        String[] test = Validate.notEmpty(array, "Message");
        assertSame(array, test);
    }

// org.apache.commons.lang3.ValidateTest::testNotEmptyCollection1
    public void testNotEmptyCollection1() {
        Collection<Integer> coll = new ArrayList<Integer>();
        try {
            Validate.notEmpty((Collection<?>) null);
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("The validated collection is empty", ex.getMessage());
        }
        try {
            Validate.notEmpty(coll);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("The validated collection is empty", ex.getMessage());
        }
        coll.add(new Integer(8));
        Validate.notEmpty(coll);
        
        Collection<Integer> test = Validate.notEmpty(coll);
        assertSame(coll, test);
    }

// org.apache.commons.lang3.ValidateTest::testNotEmptyCollection2
    public void testNotEmptyCollection2() {
        Collection<Integer> coll = new ArrayList<Integer>();
        try {
            Validate.notEmpty((Collection<?>) null, "MSG");
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        try {
            Validate.notEmpty(coll, "MSG");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        coll.add(new Integer(8));
        Validate.notEmpty(coll, "MSG");
        
        Collection<Integer> test = Validate.notEmpty(coll, "Message");
        assertSame(coll, test);
    }

// org.apache.commons.lang3.ValidateTest::testNotEmptyMap1
    public void testNotEmptyMap1() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        try {
            Validate.notEmpty((Map<?, ?>) null);
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("The validated map is empty", ex.getMessage());
        }
        try {
            Validate.notEmpty(map);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("The validated map is empty", ex.getMessage());
        }
        map.put("ll", new Integer(8));
        Validate.notEmpty(map);
        
        Map<String, Integer> test = Validate.notEmpty(map);
        assertSame(map, test);
    }

// org.apache.commons.lang3.ValidateTest::testNotEmptyMap2
    public void testNotEmptyMap2() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        try {
            Validate.notEmpty((Map<?, ?>) null, "MSG");
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        try {
            Validate.notEmpty(map, "MSG");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        map.put("ll", new Integer(8));
        Validate.notEmpty(map, "MSG");
        
        Map<String, Integer> test = Validate.notEmpty(map, "Message");
        assertSame(map, test);
    }

// org.apache.commons.lang3.ValidateTest::testNotEmptyString1
    public void testNotEmptyString1() {
        Validate.notEmpty("hjl");
        try {
            Validate.notEmpty((String) null);
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("The validated character sequence is empty", ex.getMessage());
        }
        try {
            Validate.notEmpty("");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("The validated character sequence is empty", ex.getMessage());
        }
        
        String str = "Hi";
        String testStr = Validate.notEmpty(str);
        assertSame(str, testStr);
    }

// org.apache.commons.lang3.ValidateTest::testNotEmptyString2
    public void testNotEmptyString2() {
        Validate.notEmpty("a", "MSG");
        try {
            Validate.notEmpty((String) null, "MSG");
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        try {
            Validate.notEmpty("", "MSG");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        
        String str = "Hi";
        String testStr = Validate.notEmpty(str, "Message");
        assertSame(str, testStr);
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankNullStringShouldThrow
    public void testNotBlankNullStringShouldThrow() {
        
        String string = null;

        try {
            
            Validate.notBlank(string);
            fail("Expecting NullPointerException");
        } catch (NullPointerException e) {
            
            assertEquals("The validated character sequence is blank", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankMsgNullStringShouldThrow
    public void testNotBlankMsgNullStringShouldThrow() {
        
        String string = null;

        try {
            
            Validate.notBlank(string, "Message");
            fail("Expecting NullPointerException");
        } catch (NullPointerException e) {
            
            assertEquals("Message", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankEmptyStringShouldThrow
    public void testNotBlankEmptyStringShouldThrow() {
        
        String string = "";

        try {
            
            Validate.notBlank(string);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
            assertEquals("The validated character sequence is blank", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankBlankStringWithWhitespacesShouldThrow
    public void testNotBlankBlankStringWithWhitespacesShouldThrow() {
        
        String string = "   ";

        try {
            
            Validate.notBlank(string);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
            assertEquals("The validated character sequence is blank", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankBlankStringWithNewlinesShouldThrow
    public void testNotBlankBlankStringWithNewlinesShouldThrow() {
        
        String string = " \n \t \r \n ";

        try {
            
            Validate.notBlank(string);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
            assertEquals("The validated character sequence is blank", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankMsgBlankStringShouldThrow
    public void testNotBlankMsgBlankStringShouldThrow() {
        
        String string = " \n \t \r \n ";

        try {
            
            Validate.notBlank(string, "Message");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
            assertEquals("Message", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankMsgBlankStringWithWhitespacesShouldThrow
    public void testNotBlankMsgBlankStringWithWhitespacesShouldThrow() {
        
        String string = "   ";

        try {
            
            Validate.notBlank(string, "Message");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
            assertEquals("Message", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankMsgEmptyStringShouldThrow
    public void testNotBlankMsgEmptyStringShouldThrow() {
        
        String string = "";

        try {
            
            Validate.notBlank(string, "Message");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
            assertEquals("Message", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankNotBlankStringShouldNotThrow
    public void testNotBlankNotBlankStringShouldNotThrow() {
        
        String string = "abc";

        
        Validate.notBlank(string);

        
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankNotBlankStringWithWhitespacesShouldNotThrow
    public void testNotBlankNotBlankStringWithWhitespacesShouldNotThrow() {
        
        String string = "  abc   ";

        
        Validate.notBlank(string);

        
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankNotBlankStringWithNewlinesShouldNotThrow
    public void testNotBlankNotBlankStringWithNewlinesShouldNotThrow() {
        
        String string = " \n \t abc \r \n ";

        
        Validate.notBlank(string);

        
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankMsgNotBlankStringShouldNotThrow
    public void testNotBlankMsgNotBlankStringShouldNotThrow() {
        
        String string = "abc";

        
        Validate.notBlank(string, "Message");

        
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankMsgNotBlankStringWithWhitespacesShouldNotThrow
    public void testNotBlankMsgNotBlankStringWithWhitespacesShouldNotThrow() {
        
        String string = "  abc   ";

        
        Validate.notBlank(string, "Message");

        
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankMsgNotBlankStringWithNewlinesShouldNotThrow
    public void testNotBlankMsgNotBlankStringWithNewlinesShouldNotThrow() {
        
        String string = " \n \t abc \r \n ";

        
        Validate.notBlank(string, "Message");

        
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankReturnValues1
    public void testNotBlankReturnValues1() {
        String str = "Hi";
        String test = Validate.notBlank(str);
        assertSame(str, test);
    }

// org.apache.commons.lang3.ValidateTest::testNotBlankReturnValues2
    public void testNotBlankReturnValues2() {
        String str = "Hi";
        String test = Validate.notBlank(str, "Message");
        assertSame(str, test);
    }

// org.apache.commons.lang3.ValidateTest::testNoNullElementsArray1
    public void testNoNullElementsArray1() {
        String[] array = new String[] {"a", "b"};
        Validate.noNullElements(array);
        try {
            Validate.noNullElements((Object[]) null);
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("The validated object is null", ex.getMessage());
        }
        array[1] = null;
        try {
            Validate.noNullElements(array);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("The validated array contains null element at index: 1", ex.getMessage());
        }
        
        array = new String[] {"a", "b"};
        String[] test = Validate.noNullElements(array);
        assertSame(array, test);
    }

// org.apache.commons.lang3.ValidateTest::testNoNullElementsArray2
    public void testNoNullElementsArray2() {
        String[] array = new String[] {"a", "b"};
        Validate.noNullElements(array, "MSG");
        try {
            Validate.noNullElements((Object[]) null, "MSG");
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("The validated object is null", ex.getMessage());
        }
        array[1] = null;
        try {
            Validate.noNullElements(array, "MSG");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        
        array = new String[] {"a", "b"};
        String[] test = Validate.noNullElements(array, "Message");
        assertSame(array, test);
    }

// org.apache.commons.lang3.ValidateTest::testNoNullElementsCollection1
    public void testNoNullElementsCollection1() {
        List<String> coll = new ArrayList<String>();
        coll.add("a");
        coll.add("b");
        Validate.noNullElements(coll);
        try {
            Validate.noNullElements((Collection<?>) null);
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("The validated object is null", ex.getMessage());
        }
        coll.set(1, null);
        try {
            Validate.noNullElements(coll);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("The validated collection contains null element at index: 1", ex.getMessage());
        }
        
        coll.set(1, "b");
        List<String> test = Validate.noNullElements(coll);
        assertSame(coll, test);
    }

// org.apache.commons.lang3.ValidateTest::testNoNullElementsCollection2
    public void testNoNullElementsCollection2() {
        List<String> coll = new ArrayList<String>();
        coll.add("a");
        coll.add("b");
        Validate.noNullElements(coll, "MSG");
        try {
            Validate.noNullElements((Collection<?>) null, "MSG");
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            assertEquals("The validated object is null", ex.getMessage());
        }
        coll.set(1, null);
        try {
            Validate.noNullElements(coll, "MSG");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("MSG", ex.getMessage());
        }
        
        coll.set(1, "b");
        List<String> test = Validate.noNullElements(coll, "Message");
        assertSame(coll, test);
    }

// org.apache.commons.lang3.ValidateTest::testConstructor
    public void testConstructor() {
        assertNotNull(new Validate());
        Constructor<?>[] cons = Validate.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(Validate.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(Validate.class.getModifiers()));
    }

// org.apache.commons.lang3.ValidateTest::testValidIndex_withMessage_array
    public void testValidIndex_withMessage_array() {
        Object[] array = new Object[2];
        Validate.validIndex(array, 0, "Broken: ");
        Validate.validIndex(array, 1, "Broken: ");
        try {
            Validate.validIndex(array, -1, "Broken: ");
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("Broken: ", ex.getMessage());
        }
        try {
            Validate.validIndex(array, 2, "Broken: ");
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("Broken: ", ex.getMessage());
        }
        
        String[] strArray = new String[] {"Hi"};
        String[] test = Validate.noNullElements(strArray, "Message");
        assertSame(strArray, test);
    }

// org.apache.commons.lang3.ValidateTest::testValidIndex_array
    public void testValidIndex_array() {
        Object[] array = new Object[2];
        Validate.validIndex(array, 0);
        Validate.validIndex(array, 1);
        try {
            Validate.validIndex(array, -1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("The validated array index is invalid: -1", ex.getMessage());
        }
        try {
            Validate.validIndex(array, 2);
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("The validated array index is invalid: 2", ex.getMessage());
        }
        
        String[] strArray = new String[] {"Hi"};
        String[] test = Validate.noNullElements(strArray);
        assertSame(strArray, test);
    }

// org.apache.commons.lang3.ValidateTest::testValidIndex_withMessage_collection
    public void testValidIndex_withMessage_collection() {
        Collection<String> coll = new ArrayList<String>();
        coll.add(null);
        coll.add(null);
        Validate.validIndex(coll, 0, "Broken: ");
        Validate.validIndex(coll, 1, "Broken: ");
        try {
            Validate.validIndex(coll, -1, "Broken: ");
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("Broken: ", ex.getMessage());
        }
        try {
            Validate.validIndex(coll, 2, "Broken: ");
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("Broken: ", ex.getMessage());
        }
        
        List<String> strColl = Arrays.asList(new String[] {"Hi"});
        List<String> test = Validate.validIndex(strColl, 0, "Message");
        assertSame(strColl, test);
    }

// org.apache.commons.lang3.ValidateTest::testValidIndex_collection
    public void testValidIndex_collection() {
        Collection<String> coll = new ArrayList<String>();
        coll.add(null);
        coll.add(null);
        Validate.validIndex(coll, 0);
        Validate.validIndex(coll, 1);
        try {
            Validate.validIndex(coll, -1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("The validated collection index is invalid: -1", ex.getMessage());
        }
        try {
            Validate.validIndex(coll, 2);
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("The validated collection index is invalid: 2", ex.getMessage());
        }
        
        List<String> strColl = Arrays.asList(new String[] {"Hi"});
        List<String> test = Validate.validIndex(strColl, 0);
        assertSame(strColl, test);
    }

// org.apache.commons.lang3.ValidateTest::testValidIndex_withMessage_charSequence
    public void testValidIndex_withMessage_charSequence() {
        CharSequence str = "Hi";
        Validate.validIndex(str, 0, "Broken: ");
        Validate.validIndex(str, 1, "Broken: ");
        try {
            Validate.validIndex(str, -1, "Broken: ");
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("Broken: ", ex.getMessage());
        }
        try {
            Validate.validIndex(str, 2, "Broken: ");
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("Broken: ", ex.getMessage());
        }
        
        String input = "Hi";
        String test = Validate.validIndex(input, 0, "Message");
        assertSame(input, test);
    }

// org.apache.commons.lang3.ValidateTest::testValidIndex_charSequence
    public void testValidIndex_charSequence() {
        CharSequence str = "Hi";
        Validate.validIndex(str, 0);
        Validate.validIndex(str, 1);
        try {
            Validate.validIndex(str, -1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("The validated character sequence index is invalid: -1", ex.getMessage());
        }
        try {
            Validate.validIndex(str, 2);
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("The validated character sequence index is invalid: 2", ex.getMessage());
        }
        
        String input = "Hi";
        String test = Validate.validIndex(input, 0);
        assertSame(input, test);
    }

// org.apache.commons.lang3.ValidateTest::testMatchesPattern
    public void testMatchesPattern()
    {
        CharSequence str = "hi";
        Validate.matchesPattern(str, "[a-z]*");
        try
        {
            Validate.matchesPattern(str, "[0-9]*");
            fail("Expecting IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("The string hi does not match the pattern [0-9]*", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testMatchesPattern_withMessage
    public void testMatchesPattern_withMessage()
    {
        CharSequence str = "hi";
        Validate.matchesPattern(str, "[a-z]*", "Does not match");
        try
        {
            Validate.matchesPattern(str, "[0-9]*", "Does not match");
            fail("Expecting IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Does not match", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testInclusiveBetween
    public void testInclusiveBetween()
    {
        Validate.inclusiveBetween("a", "c", "b");
        Validate.inclusiveBetween(0, 2, 1);
        Validate.inclusiveBetween(0, 2, 2);
        try {
            Validate.inclusiveBetween(0, 5, 6);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("The value 6 is not in the specified inclusive range of 0 to 5", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testInclusiveBetween_withMessage
    public void testInclusiveBetween_withMessage()
    {
        Validate.inclusiveBetween("a", "c", "b", "Error");
        Validate.inclusiveBetween(0, 2, 1, "Error");
        Validate.inclusiveBetween(0, 2, 2, "Error");
        try {
            Validate.inclusiveBetween(0, 5, 6, "Error");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Error", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testExclusiveBetween
    public void testExclusiveBetween()
    {
        Validate.exclusiveBetween("a", "c", "b");
        Validate.exclusiveBetween(0, 2, 1);
        try {
            Validate.exclusiveBetween(0, 5, 6);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("The value 6 is not in the specified exclusive range of 0 to 5", e.getMessage());
        }
        try {
            Validate.exclusiveBetween(0, 5, 5);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("The value 5 is not in the specified exclusive range of 0 to 5", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testExclusiveBetween_withMessage
    public void testExclusiveBetween_withMessage()
    {
        Validate.exclusiveBetween("a", "c", "b", "Error");
        Validate.exclusiveBetween(0, 2, 1, "Error");
        try {
            Validate.exclusiveBetween(0, 5, 6, "Error");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Error", e.getMessage());
        }
        try {
            Validate.exclusiveBetween(0, 5, 5, "Error");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Error", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testIsInstanceOf
    public void testIsInstanceOf() {
        Validate.isInstanceOf(String.class, "hi");
        Validate.isInstanceOf(Integer.class, 1);
        try {
            Validate.isInstanceOf(List.class, "hi");
            fail("Expecting IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            assertEquals("The validated object is not an instance of java.util.List", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testIsInstanceOf_withMessage
    public void testIsInstanceOf_withMessage() {
        Validate.isInstanceOf(String.class, "hi", "Error");
        Validate.isInstanceOf(Integer.class, 1, "Error");
        try {
            Validate.isInstanceOf(List.class, "hi", "Error");
            fail("Expecting IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            assertEquals("Error", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testIsAssignable
    public void testIsAssignable() {
        Validate.isAssignableFrom(CharSequence.class, String.class);
        Validate.isAssignableFrom(AbstractList.class, ArrayList.class);
        try {
            Validate.isAssignableFrom(List.class, String.class);
            fail("Expecting IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            assertEquals("The validated class can not be converted to the java.util.List class", e.getMessage());
        }
    }

// org.apache.commons.lang3.ValidateTest::testIsAssignable_withMessage
    public void testIsAssignable_withMessage() {
        Validate.isAssignableFrom(CharSequence.class, String.class, "Error");
        Validate.isAssignableFrom(AbstractList.class, ArrayList.class, "Error");
        try {
            Validate.isAssignableFrom(List.class, String.class, "Error");
            fail("Expecting IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            assertEquals("Error", e.getMessage());
        }
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedException
    public void testContextedException() {
        contextedException = new ContextedException();
        String message = contextedException.getMessage();
        String trace = ExceptionUtils.getStackTrace(contextedException);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(StringUtils.isEmpty(message));
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedExceptionString
    public void testContextedExceptionString() {
        contextedException = new ContextedException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, contextedException.getMessage());
        
        String trace = ExceptionUtils.getStackTrace(contextedException);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedExceptionThrowable
    public void testContextedExceptionThrowable() {
        contextedException = new ContextedException(new Exception(TEST_MESSAGE));
        String message = contextedException.getMessage();
        String trace = ExceptionUtils.getStackTrace(contextedException);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE)>=0);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedExceptionStringThrowable
    public void testContextedExceptionStringThrowable() {
        contextedException = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE));
        String message = contextedException.getMessage();
        String trace = ExceptionUtils.getStackTrace(contextedException);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE_2)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE_2)>=0);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedExceptionStringThrowableContext
    public void testContextedExceptionStringThrowableContext() {
        contextedException = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        String message = contextedException.getMessage();
        String trace = ExceptionUtils.getStackTrace(contextedException);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE_2)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE_2)>=0);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testAddValue
    public void testAddValue() {
        contextedException = new ContextedException(new Exception(TEST_MESSAGE))
        .addValue("test1", null)
        .addValue("test2", "some value")
        .addValue("test Date", new Date())
        .addValue("test Nbr", new Integer(5))
        .addValue("test Poorly written obj", new ObjectWithFaultyToString());
        
        String message = contextedException.getMessage();
        assertTrue(message.indexOf(TEST_MESSAGE)>=0);
        assertTrue(message.indexOf("test1")>=0);
        assertTrue(message.indexOf("test2")>=0);
        assertTrue(message.indexOf("test Date")>=0);
        assertTrue(message.indexOf("test Nbr")>=0);
        assertTrue(message.indexOf("test Poorly written obj")>=0);
        assertTrue(message.indexOf("some value")>=0);
        assertTrue(message.indexOf("5")>=0);
        assertTrue(message.indexOf("Crap")>=0);
        
        assertTrue(contextedException.getValue("test1") == null);
        assertTrue(contextedException.getValue("test2").equals("some value"));
        assertTrue(contextedException.getValue("crap") == null);
        assertTrue(contextedException.getValue("test Poorly written obj") instanceof ObjectWithFaultyToString);
        
        assertTrue(contextedException.getLabelSet().size() == 5);
        assertTrue(contextedException.getLabelSet().contains("test1"));
        assertTrue(contextedException.getLabelSet().contains("test2"));
        assertTrue(contextedException.getLabelSet().contains("test Date"));
        assertTrue(contextedException.getLabelSet().contains("test Nbr"));
        assertTrue(contextedException.getLabelSet().contains("test Poorly written obj"));
        
        assertTrue(!contextedException.getLabelSet().contains("crap"));

        contextedException.addValue("test Poorly written obj", "replacement");
        
        String contextMessage = contextedException.getFormattedExceptionMessage(null);
        assertTrue(contextMessage.indexOf(TEST_MESSAGE) == -1);
        assertTrue(contextedException.getMessage().endsWith(contextMessage));
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testNullExceptionPassing
    public void testNullExceptionPassing() {
        contextedException = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), null)
        .addValue("test1", null)
        .addValue("test2", "some value")
        .addValue("test Date", new Date())
        .addValue("test Nbr", new Integer(5))
        .addValue("test Poorly written obj", new ObjectWithFaultyToString());
        
        String message = contextedException.getMessage();
        assertTrue(message != null);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testGetMessage
    public void testGetMessage() {
        testAddValue();
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedException
    public void testContextedException() {
        contextedRuntimeException = new ContextedRuntimeException();
        String message = contextedRuntimeException.getMessage();
        String trace = ExceptionUtils.getStackTrace(contextedRuntimeException);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(StringUtils.isEmpty(message));
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedExceptionString
    public void testContextedExceptionString() {
        contextedRuntimeException = new ContextedRuntimeException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, contextedRuntimeException.getMessage());
        
        String trace = ExceptionUtils.getStackTrace(contextedRuntimeException);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedExceptionThrowable
    public void testContextedExceptionThrowable() {
        contextedRuntimeException = new ContextedRuntimeException(new Exception(TEST_MESSAGE));
        String message = contextedRuntimeException.getMessage();
        String trace = ExceptionUtils.getStackTrace(contextedRuntimeException);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE)>=0);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedExceptionStringThrowable
    public void testContextedExceptionStringThrowable() {
        contextedRuntimeException = new ContextedRuntimeException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE));
        String message = contextedRuntimeException.getMessage();
        String trace = ExceptionUtils.getStackTrace(contextedRuntimeException);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE_2)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE_2)>=0);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedExceptionStringThrowableContext
    public void testContextedExceptionStringThrowableContext() {
        contextedRuntimeException = new ContextedRuntimeException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        String message = contextedRuntimeException.getMessage();
        String trace = ExceptionUtils.getStackTrace(contextedRuntimeException);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE_2)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE_2)>=0);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testAddValue
    public void testAddValue() {
        contextedRuntimeException = new ContextedRuntimeException(new Exception(TEST_MESSAGE))
        .addValue("test1", null)
        .addValue("test2", "some value")
        .addValue("test Date", new Date())
        .addValue("test Nbr", new Integer(5))
        .addValue("test Poorly written obj", new ObjectWithFaultyToString());
        
        String message = contextedRuntimeException.getMessage();
        assertTrue(message.indexOf(TEST_MESSAGE)>=0);
        assertTrue(message.indexOf("test1")>=0);
        assertTrue(message.indexOf("test2")>=0);
        assertTrue(message.indexOf("test Date")>=0);
        assertTrue(message.indexOf("test Nbr")>=0);
        assertTrue(message.indexOf("test Poorly written obj")>=0);
        assertTrue(message.indexOf("some value")>=0);
        assertTrue(message.indexOf("5")>=0);
        assertTrue(message.indexOf("Crap")>=0);
        
        assertTrue(contextedRuntimeException.getValue("test1") == null);
        assertTrue(contextedRuntimeException.getValue("test2").equals("some value"));
        assertTrue(contextedRuntimeException.getValue("crap") == null);
        assertTrue(contextedRuntimeException.getValue("test Poorly written obj") instanceof ObjectWithFaultyToString);
        
        assertTrue(contextedRuntimeException.getLabelSet().size() == 5);
        assertTrue(contextedRuntimeException.getLabelSet().contains("test1"));
        assertTrue(contextedRuntimeException.getLabelSet().contains("test2"));
        assertTrue(contextedRuntimeException.getLabelSet().contains("test Date"));
        assertTrue(contextedRuntimeException.getLabelSet().contains("test Nbr"));
        assertTrue(contextedRuntimeException.getLabelSet().contains("test Poorly written obj"));
        
        assertTrue(!contextedRuntimeException.getLabelSet().contains("crap"));

        contextedRuntimeException.addValue("test Poorly written obj", "replacement");
        
        String contextMessage = contextedRuntimeException.getFormattedExceptionMessage(null);
        assertTrue(contextMessage.indexOf(TEST_MESSAGE) == -1);
        assertTrue(contextedRuntimeException.getMessage().endsWith(contextMessage));
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testNullExceptionPassing
    public void testNullExceptionPassing() {
        contextedRuntimeException = new ContextedRuntimeException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), null)
        .addValue("test1", null)
        .addValue("test2", "some value")
        .addValue("test Date", new Date())
        .addValue("test Nbr", new Integer(5))
        .addValue("test Poorly written obj", new ObjectWithFaultyToString());
        
        String message = contextedRuntimeException.getMessage();
        assertTrue(message != null);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testGetMessage
    public void testGetMessage() {
        testAddValue();
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new ExceptionUtils());
        Constructor<?>[] cons = ExceptionUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(ExceptionUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ExceptionUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetCause_Throwable
    public void testGetCause_Throwable() {
        assertSame(null, ExceptionUtils.getCause(null));
        assertSame(null, ExceptionUtils.getCause(withoutCause));
        assertSame(withoutCause, ExceptionUtils.getCause(nested));
        assertSame(nested, ExceptionUtils.getCause(withCause));
        assertSame(null, ExceptionUtils.getCause(jdkNoCause));
        assertSame(cyclicCause.getCause(), ExceptionUtils.getCause(cyclicCause));
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), ExceptionUtils.getCause(cyclicCause.getCause()));
        assertSame(cyclicCause.getCause(), ExceptionUtils.getCause(((ExceptionWithCause) cyclicCause.getCause()).getCause()));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetCause_ThrowableArray
    public void testGetCause_ThrowableArray() {
        assertSame(null, ExceptionUtils.getCause(null, null));
        assertSame(null, ExceptionUtils.getCause(null, new String[0]));

        
        assertSame(nested, ExceptionUtils.getCause(withCause, null));  
        assertSame(null, ExceptionUtils.getCause(withCause, new String[0]));
        assertSame(null, ExceptionUtils.getCause(withCause, new String[] {null}));
        assertSame(nested, ExceptionUtils.getCause(withCause, new String[] {"getCause"}));
        
        
        assertSame(null, ExceptionUtils.getCause(withoutCause, null));
        assertSame(null, ExceptionUtils.getCause(withoutCause, new String[0]));
        assertSame(null, ExceptionUtils.getCause(withoutCause, new String[] {null}));
        assertSame(null, ExceptionUtils.getCause(withoutCause, new String[] {"getCause"}));
        assertSame(null, ExceptionUtils.getCause(withoutCause, new String[] {"getTargetException"}));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetRootCause_Throwable
    public void testGetRootCause_Throwable() {
        assertSame(null, ExceptionUtils.getRootCause(null));
        assertSame(null, ExceptionUtils.getRootCause(withoutCause));
        assertSame(withoutCause, ExceptionUtils.getRootCause(nested));
        assertSame(withoutCause, ExceptionUtils.getRootCause(withCause));
        assertSame(null, ExceptionUtils.getRootCause(jdkNoCause));
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), ExceptionUtils.getRootCause(cyclicCause));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableCount_Throwable
    public void testGetThrowableCount_Throwable() {
        assertEquals(0, ExceptionUtils.getThrowableCount(null));
        assertEquals(1, ExceptionUtils.getThrowableCount(withoutCause));
        assertEquals(2, ExceptionUtils.getThrowableCount(nested));
        assertEquals(3, ExceptionUtils.getThrowableCount(withCause));
        assertEquals(1, ExceptionUtils.getThrowableCount(jdkNoCause));
        assertEquals(3, ExceptionUtils.getThrowableCount(cyclicCause));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_null
    public void testGetThrowables_Throwable_null() {
        assertEquals(0, ExceptionUtils.getThrowables(null).length);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_withoutCause
    public void testGetThrowables_Throwable_withoutCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(withoutCause);
        assertEquals(1, throwables.length);
        assertSame(withoutCause, throwables[0]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_nested
    public void testGetThrowables_Throwable_nested() {
        Throwable[] throwables = ExceptionUtils.getThrowables(nested);
        assertEquals(2, throwables.length);
        assertSame(nested, throwables[0]);
        assertSame(withoutCause, throwables[1]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_withCause
    public void testGetThrowables_Throwable_withCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(withCause);
        assertEquals(3, throwables.length);
        assertSame(withCause, throwables[0]);
        assertSame(nested, throwables[1]);
        assertSame(withoutCause, throwables[2]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_jdkNoCause
    public void testGetThrowables_Throwable_jdkNoCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(jdkNoCause);
        assertEquals(1, throwables.length);
        assertSame(jdkNoCause, throwables[0]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_recursiveCause
    public void testGetThrowables_Throwable_recursiveCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(cyclicCause);
        assertEquals(3, throwables.length);
        assertSame(cyclicCause, throwables[0]);
        assertSame(cyclicCause.getCause(), throwables[1]);
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), throwables[2]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_null
    public void testGetThrowableList_Throwable_null() {
        List<?> throwables = ExceptionUtils.getThrowableList(null);
        assertEquals(0, throwables.size());
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_withoutCause
    public void testGetThrowableList_Throwable_withoutCause() {
        List<?> throwables = ExceptionUtils.getThrowableList(withoutCause);
        assertEquals(1, throwables.size());
        assertSame(withoutCause, throwables.get(0));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_nested
    public void testGetThrowableList_Throwable_nested() {
        List<?> throwables = ExceptionUtils.getThrowableList(nested);
        assertEquals(2, throwables.size());
        assertSame(nested, throwables.get(0));
        assertSame(withoutCause, throwables.get(1));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_withCause
    public void testGetThrowableList_Throwable_withCause() {
        List<?> throwables = ExceptionUtils.getThrowableList(withCause);
        assertEquals(3, throwables.size());
        assertSame(withCause, throwables.get(0));
        assertSame(nested, throwables.get(1));
        assertSame(withoutCause, throwables.get(2));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_jdkNoCause
    public void testGetThrowableList_Throwable_jdkNoCause() {
        List<?> throwables = ExceptionUtils.getThrowableList(jdkNoCause);
        assertEquals(1, throwables.size());
        assertSame(jdkNoCause, throwables.get(0));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_recursiveCause
    public void testGetThrowableList_Throwable_recursiveCause() {
        List<?> throwables = ExceptionUtils.getThrowableList(cyclicCause);
        assertEquals(3, throwables.size());
        assertSame(cyclicCause, throwables.get(0));
        assertSame(cyclicCause.getCause(), throwables.get(1));
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), throwables.get(2));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testIndexOf_ThrowableClass
    public void testIndexOf_ThrowableClass() {
        assertEquals(-1, ExceptionUtils.indexOfThrowable(null, null));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(null, NestableException.class));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, null));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, ExceptionWithCause.class));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, NestableException.class));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withoutCause, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(nested, null));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(nested, ExceptionWithCause.class));
        assertEquals(0, ExceptionUtils.indexOfThrowable(nested, NestableException.class));
        assertEquals(1, ExceptionUtils.indexOfThrowable(nested, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, null));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class));
        assertEquals(1, ExceptionUtils.indexOfThrowable(withCause, NestableException.class));
        assertEquals(2, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, Exception.class));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testIndexOf_ThrowableClassInt
    public void testIndexOf_ThrowableClassInt() {
        assertEquals(-1, ExceptionUtils.indexOfThrowable(null, null, 0));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(null, NestableException.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, null));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, ExceptionWithCause.class, 0));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, NestableException.class, 0));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withoutCause, ExceptionWithoutCause.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(nested, null, 0));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(nested, ExceptionWithCause.class, 0));
        assertEquals(0, ExceptionUtils.indexOfThrowable(nested, NestableException.class, 0));
        assertEquals(1, ExceptionUtils.indexOfThrowable(nested, ExceptionWithoutCause.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, null));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, 0));
        assertEquals(1, ExceptionUtils.indexOfThrowable(withCause, NestableException.class, 0));
        assertEquals(2, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithoutCause.class, 0));

        assertEquals(0, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, -1));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, 0));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, 1));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, 9));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, Exception.class, 0));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testIndexOfType_ThrowableClass
    public void testIndexOfType_ThrowableClass() {
        assertEquals(-1, ExceptionUtils.indexOfType(null, null));
        assertEquals(-1, ExceptionUtils.indexOfType(null, NestableException.class));
        
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, null));
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, ExceptionWithCause.class));
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, NestableException.class));
        assertEquals(0, ExceptionUtils.indexOfType(withoutCause, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfType(nested, null));
        assertEquals(-1, ExceptionUtils.indexOfType(nested, ExceptionWithCause.class));
        assertEquals(0, ExceptionUtils.indexOfType(nested, NestableException.class));
        assertEquals(1, ExceptionUtils.indexOfType(nested, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfType(withCause, null));
        assertEquals(0, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class));
        assertEquals(1, ExceptionUtils.indexOfType(withCause, NestableException.class));
        assertEquals(2, ExceptionUtils.indexOfType(withCause, ExceptionWithoutCause.class));
        
        assertEquals(0, ExceptionUtils.indexOfType(withCause, Exception.class));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testIndexOfType_ThrowableClassInt
    public void testIndexOfType_ThrowableClassInt() {
        assertEquals(-1, ExceptionUtils.indexOfType(null, null, 0));
        assertEquals(-1, ExceptionUtils.indexOfType(null, NestableException.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, null));
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, ExceptionWithCause.class, 0));
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, NestableException.class, 0));
        assertEquals(0, ExceptionUtils.indexOfType(withoutCause, ExceptionWithoutCause.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfType(nested, null, 0));
        assertEquals(-1, ExceptionUtils.indexOfType(nested, ExceptionWithCause.class, 0));
        assertEquals(0, ExceptionUtils.indexOfType(nested, NestableException.class, 0));
        assertEquals(1, ExceptionUtils.indexOfType(nested, ExceptionWithoutCause.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfType(withCause, null));
        assertEquals(0, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, 0));
        assertEquals(1, ExceptionUtils.indexOfType(withCause, NestableException.class, 0));
        assertEquals(2, ExceptionUtils.indexOfType(withCause, ExceptionWithoutCause.class, 0));

        assertEquals(0, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, -1));
        assertEquals(0, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, 0));
        assertEquals(-1, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, 1));
        assertEquals(-1, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, 9));
        
        assertEquals(0, ExceptionUtils.indexOfType(withCause, Exception.class, 0));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testPrintRootCauseStackTrace_Throwable
    public void testPrintRootCauseStackTrace_Throwable() throws Exception {
        ExceptionUtils.printRootCauseStackTrace(null);
        
        
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testPrintRootCauseStackTrace_ThrowableStream
    public void testPrintRootCauseStackTrace_ThrowableStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        ExceptionUtils.printRootCauseStackTrace(null, (PrintStream) null);
        ExceptionUtils.printRootCauseStackTrace(null, new PrintStream(out));
        assertEquals(0, out.toString().length());
        
        out = new ByteArrayOutputStream(1024);
        try {
            ExceptionUtils.printRootCauseStackTrace(withCause, (PrintStream) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        out = new ByteArrayOutputStream(1024);
        Throwable withCause = createExceptionWithCause();
        ExceptionUtils.printRootCauseStackTrace(withCause, new PrintStream(out));
        String stackTrace = out.toString();
        assertTrue(stackTrace.indexOf(ExceptionUtils.WRAPPED_MARKER) != -1);
        
        out = new ByteArrayOutputStream(1024);
        ExceptionUtils.printRootCauseStackTrace(withoutCause, new PrintStream(out));
        stackTrace = out.toString();
        assertTrue(stackTrace.indexOf(ExceptionUtils.WRAPPED_MARKER) == -1);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testPrintRootCauseStackTrace_ThrowableWriter
    public void testPrintRootCauseStackTrace_ThrowableWriter() throws Exception {
        StringWriter writer = new StringWriter(1024);
        ExceptionUtils.printRootCauseStackTrace(null, (PrintWriter) null);
        ExceptionUtils.printRootCauseStackTrace(null, new PrintWriter(writer));
        assertEquals(0, writer.getBuffer().length());
        
        writer = new StringWriter(1024);
        try {
            ExceptionUtils.printRootCauseStackTrace(withCause, (PrintWriter) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        writer = new StringWriter(1024);
        Throwable withCause = createExceptionWithCause();
        ExceptionUtils.printRootCauseStackTrace(withCause, new PrintWriter(writer));
        String stackTrace = writer.toString();
        assertTrue(stackTrace.indexOf(ExceptionUtils.WRAPPED_MARKER) != -1);
        
        writer = new StringWriter(1024);
        ExceptionUtils.printRootCauseStackTrace(withoutCause, new PrintWriter(writer));
        stackTrace = writer.toString();
        assertTrue(stackTrace.indexOf(ExceptionUtils.WRAPPED_MARKER) == -1);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetRootCauseStackTrace_Throwable
    public void testGetRootCauseStackTrace_Throwable() throws Exception {
        assertEquals(0, ExceptionUtils.getRootCauseStackTrace(null).length);
        
        Throwable withCause = createExceptionWithCause();
        String[] stackTrace = ExceptionUtils.getRootCauseStackTrace(withCause);
        boolean match = false;
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].startsWith(ExceptionUtils.WRAPPED_MARKER)) {
                match = true;
                break;
            }
        }
        assertEquals(true, match);
        
        stackTrace = ExceptionUtils.getRootCauseStackTrace(withoutCause);
        match = false;
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].startsWith(ExceptionUtils.WRAPPED_MARKER)) {
                match = true;
                break;
            }
        }
        assertEquals(false, match);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testRemoveCommonFrames_ListList
    public void testRemoveCommonFrames_ListList() throws Exception {
        try {
            ExceptionUtils.removeCommonFrames(null, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::test_getMessage_Throwable
    public void test_getMessage_Throwable() {
        Throwable th = null;
        assertEquals("", ExceptionUtils.getMessage(th));
        
        th = new IllegalArgumentException("Base");
        assertEquals("IllegalArgumentException: Base", ExceptionUtils.getMessage(th));
        
        th = new ExceptionWithCause("Wrapper", th);
        assertEquals("ExceptionUtilsTest.ExceptionWithCause: Wrapper", ExceptionUtils.getMessage(th));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::test_getRootCauseMessage_Throwable
    public void test_getRootCauseMessage_Throwable() {
        Throwable th = null;
        assertEquals("", ExceptionUtils.getRootCauseMessage(th));
        
        th = new IllegalArgumentException("Base");
        assertEquals("IllegalArgumentException: Base", ExceptionUtils.getRootCauseMessage(th));
        
        th = new ExceptionWithCause("Wrapper", th);
        assertEquals("IllegalArgumentException: Base", ExceptionUtils.getRootCauseMessage(th));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new NumberUtils());
        Constructor<?>[] cons = NumberUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(NumberUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(NumberUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToIntString
    public void testToIntString() {
        assertTrue("toInt(String) 1 failed", NumberUtils.toInt("12345") == 12345);
        assertTrue("toInt(String) 2 failed", NumberUtils.toInt("abc") == 0);
        assertTrue("toInt(empty) failed", NumberUtils.toInt("") == 0);
        assertTrue("toInt(null) failed", NumberUtils.toInt(null) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToIntStringI
    public void testToIntStringI() {
        assertTrue("toInt(String,int) 1 failed", NumberUtils.toInt("12345", 5) == 12345);
        assertTrue("toInt(String,int) 2 failed", NumberUtils.toInt("1234.5", 5) == 5);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToLongString
    public void testToLongString() {
        assertTrue("toLong(String) 1 failed", NumberUtils.toLong("12345") == 12345l);
        assertTrue("toLong(String) 2 failed", NumberUtils.toLong("abc") == 0l);
        assertTrue("toLong(String) 3 failed", NumberUtils.toLong("1L") == 0l);
        assertTrue("toLong(String) 4 failed", NumberUtils.toLong("1l") == 0l);
        assertTrue("toLong(Long.MAX_VALUE) failed", NumberUtils.toLong(Long.MAX_VALUE+"") == Long.MAX_VALUE);
        assertTrue("toLong(Long.MIN_VALUE) failed", NumberUtils.toLong(Long.MIN_VALUE+"") == Long.MIN_VALUE);
        assertTrue("toLong(empty) failed", NumberUtils.toLong("") == 0l);
        assertTrue("toLong(null) failed", NumberUtils.toLong(null) == 0l);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToLongStringL
    public void testToLongStringL() {
        assertTrue("toLong(String,long) 1 failed", NumberUtils.toLong("12345", 5l) == 12345l);
        assertTrue("toLong(String,long) 2 failed", NumberUtils.toLong("1234.5", 5l) == 5l);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToFloatString
    public void testToFloatString() {
        assertTrue("toFloat(String) 1 failed", NumberUtils.toFloat("-1.2345") == -1.2345f);
        assertTrue("toFloat(String) 2 failed", NumberUtils.toFloat("1.2345") == 1.2345f);
        assertTrue("toFloat(String) 3 failed", NumberUtils.toFloat("abc") == 0.0f);
        assertTrue("toFloat(Float.MAX_VALUE) failed", NumberUtils.toFloat(Float.MAX_VALUE+"") ==  Float.MAX_VALUE);
        assertTrue("toFloat(Float.MIN_VALUE) failed", NumberUtils.toFloat(Float.MIN_VALUE+"") == Float.MIN_VALUE);
        assertTrue("toFloat(empty) failed", NumberUtils.toFloat("") == 0.0f);
        assertTrue("toFloat(null) failed", NumberUtils.toFloat(null) == 0.0f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToFloatStringF
    public void testToFloatStringF() {
        assertTrue("toFloat(String,int) 1 failed", NumberUtils.toFloat("1.2345", 5.1f) == 1.2345f);
        assertTrue("toFloat(String,int) 2 failed", NumberUtils.toFloat("a", 5.0f) == 5.0f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testStringToDoubleString
    public void testStringToDoubleString() {
        assertTrue("toDouble(String) 1 failed", NumberUtils.toDouble("-1.2345") == -1.2345d);
        assertTrue("toDouble(String) 2 failed", NumberUtils.toDouble("1.2345") == 1.2345d);
        assertTrue("toDouble(String) 3 failed", NumberUtils.toDouble("abc") == 0.0d);
        assertTrue("toDouble(Double.MAX_VALUE) failed", NumberUtils.toDouble(Double.MAX_VALUE+"") == Double.MAX_VALUE);
        assertTrue("toDouble(Double.MIN_VALUE) failed", NumberUtils.toDouble(Double.MIN_VALUE+"") == Double.MIN_VALUE);
        assertTrue("toDouble(empty) failed", NumberUtils.toDouble("") == 0.0d);
        assertTrue("toDouble(null) failed", NumberUtils.toDouble(null) == 0.0d);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testStringToDoubleStringD
    public void testStringToDoubleStringD() {
        assertTrue("toDouble(String,int) 1 failed", NumberUtils.toDouble("1.2345", 5.1d) == 1.2345d);
        assertTrue("toDouble(String,int) 2 failed", NumberUtils.toDouble("a", 5.0d) == 5.0d);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToByteString
    public void testToByteString() {
        assertTrue("toByte(String) 1 failed", NumberUtils.toByte("123") == 123);
        assertTrue("toByte(String) 2 failed", NumberUtils.toByte("abc") == 0);
        assertTrue("toByte(empty) failed", NumberUtils.toByte("") == 0);
        assertTrue("toByte(null) failed", NumberUtils.toByte(null) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToByteStringI
    public void testToByteStringI() {
        assertTrue("toByte(String,byte) 1 failed", NumberUtils.toByte("123", (byte) 5) == 123);
        assertTrue("toByte(String,byte) 2 failed", NumberUtils.toByte("12.3", (byte) 5) == 5);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToShortString
    public void testToShortString() {
        assertTrue("toShort(String) 1 failed", NumberUtils.toShort("12345") == 12345);
        assertTrue("toShort(String) 2 failed", NumberUtils.toShort("abc") == 0);
        assertTrue("toShort(empty) failed", NumberUtils.toShort("") == 0);
        assertTrue("toShort(null) failed", NumberUtils.toShort(null) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToShortStringI
    public void testToShortStringI() {
        assertTrue("toShort(String,short) 1 failed", NumberUtils.toShort("12345", (short) 5) == 12345);
        assertTrue("toShort(String,short) 2 failed", NumberUtils.toShort("1234.5", (short) 5) == 5);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateNumber
    public void testCreateNumber() {
        
        assertEquals("createNumber(String) 1 failed", new Float("1234.5"), NumberUtils.createNumber("1234.5"));
        assertEquals("createNumber(String) 2 failed", new Integer("12345"), NumberUtils.createNumber("12345"));
        assertEquals("createNumber(String) 3 failed", new Double("1234.5"), NumberUtils.createNumber("1234.5D"));
        assertEquals("createNumber(String) 3 failed", new Double("1234.5"), NumberUtils.createNumber("1234.5d"));
        assertEquals("createNumber(String) 4 failed", new Float("1234.5"), NumberUtils.createNumber("1234.5F"));
        assertEquals("createNumber(String) 4 failed", new Float("1234.5"), NumberUtils.createNumber("1234.5f"));
        assertEquals("createNumber(String) 5 failed", new Long(Integer.MAX_VALUE + 1L), NumberUtils.createNumber(""
            + (Integer.MAX_VALUE + 1L)));
        assertEquals("createNumber(String) 6 failed", new Long(12345), NumberUtils.createNumber("12345L"));
        assertEquals("createNumber(String) 6 failed", new Long(12345), NumberUtils.createNumber("12345l"));
        assertEquals("createNumber(String) 7 failed", new Float("-1234.5"), NumberUtils.createNumber("-1234.5"));
        assertEquals("createNumber(String) 8 failed", new Integer("-12345"), NumberUtils.createNumber("-12345"));
        assertTrue("createNumber(String) 9 failed", 0xFADE == NumberUtils.createNumber("0xFADE").intValue());
        assertTrue("createNumber(String) 10 failed", -0xFADE == NumberUtils.createNumber("-0xFADE").intValue());
        assertEquals("createNumber(String) 11 failed", new Double("1.1E200"), NumberUtils.createNumber("1.1E200"));
        assertEquals("createNumber(String) 12 failed", new Float("1.1E20"), NumberUtils.createNumber("1.1E20"));
        assertEquals("createNumber(String) 13 failed", new Double("-1.1E200"), NumberUtils.createNumber("-1.1E200"));
        assertEquals("createNumber(String) 14 failed", new Double("1.1E-200"), NumberUtils.createNumber("1.1E-200"));
        assertEquals("createNumber(null) failed", null, NumberUtils.createNumber(null));
        assertEquals("createNumber(String) failed", new BigInteger("12345678901234567890"), NumberUtils
                .createNumber("12345678901234567890L"));

        
        if (SystemUtils.isJavaVersionAtLeast(1.3f)) {
            assertEquals("createNumber(String) 15 failed", new BigDecimal("1.1E-700"), NumberUtils
                    .createNumber("1.1E-700F"));
        }
        assertEquals("createNumber(String) 16 failed", new Long("10" + Integer.MAX_VALUE), NumberUtils
                .createNumber("10" + Integer.MAX_VALUE + "L"));
        assertEquals("createNumber(String) 17 failed", new Long("10" + Integer.MAX_VALUE), NumberUtils
                .createNumber("10" + Integer.MAX_VALUE));
        assertEquals("createNumber(String) 18 failed", new BigInteger("10" + Long.MAX_VALUE), NumberUtils
                .createNumber("10" + Long.MAX_VALUE));

        
        assertEquals("createNumber(String) LANG-521 failed", new Float("2."), NumberUtils.createNumber("2."));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateFloat
    public void testCreateFloat() {
        assertEquals("createFloat(String) failed", new Float("1234.5"), NumberUtils.createFloat("1234.5"));
        assertEquals("createFloat(null) failed", null, NumberUtils.createFloat(null));
        this.testCreateFloatFailure("");
        this.testCreateFloatFailure(" ");
        this.testCreateFloatFailure("\b\t\n\f\r");
        
        this.testCreateFloatFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateFloatFailure
    protected void testCreateFloatFailure(String str) {
        try {
            Float value = NumberUtils.createFloat(str);
            fail("createFloat(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateDouble
    public void testCreateDouble() {
        assertEquals("createDouble(String) failed", new Double("1234.5"), NumberUtils.createDouble("1234.5"));
        assertEquals("createDouble(null) failed", null, NumberUtils.createDouble(null));
        this.testCreateDoubleFailure("");
        this.testCreateDoubleFailure(" ");
        this.testCreateDoubleFailure("\b\t\n\f\r");
        
        this.testCreateDoubleFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateDoubleFailure
    protected void testCreateDoubleFailure(String str) {
        try {
            Double value = NumberUtils.createDouble(str);
            fail("createDouble(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateInteger
    public void testCreateInteger() {
        assertEquals("createInteger(String) failed", new Integer("12345"), NumberUtils.createInteger("12345"));
        assertEquals("createInteger(null) failed", null, NumberUtils.createInteger(null));
        this.testCreateIntegerFailure("");
        this.testCreateIntegerFailure(" ");
        this.testCreateIntegerFailure("\b\t\n\f\r");
        
        this.testCreateIntegerFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateIntegerFailure
    protected void testCreateIntegerFailure(String str) {
        try {
            Integer value = NumberUtils.createInteger(str);
            fail("createInteger(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateLong
    public void testCreateLong() {
        assertEquals("createLong(String) failed", new Long("12345"), NumberUtils.createLong("12345"));
        assertEquals("createLong(null) failed", null, NumberUtils.createLong(null));
        this.testCreateLongFailure("");
        this.testCreateLongFailure(" ");
        this.testCreateLongFailure("\b\t\n\f\r");
        
        this.testCreateLongFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateLongFailure
    protected void testCreateLongFailure(String str) {
        try {
            Long value = NumberUtils.createLong(str);
            fail("createLong(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateBigInteger
    public void testCreateBigInteger() {
        assertEquals("createBigInteger(String) failed", new BigInteger("12345"), NumberUtils.createBigInteger("12345"));
        assertEquals("createBigInteger(null) failed", null, NumberUtils.createBigInteger(null));
        this.testCreateBigIntegerFailure("");
        this.testCreateBigIntegerFailure(" ");
        this.testCreateBigIntegerFailure("\b\t\n\f\r");
        
        this.testCreateBigIntegerFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateBigIntegerFailure
    protected void testCreateBigIntegerFailure(String str) {
        try {
            BigInteger value = NumberUtils.createBigInteger(str);
            fail("createBigInteger(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateBigDecimal
    public void testCreateBigDecimal() {
        assertEquals("createBigDecimal(String) failed", new BigDecimal("1234.5"), NumberUtils.createBigDecimal("1234.5"));
        assertEquals("createBigDecimal(null) failed", null, NumberUtils.createBigDecimal(null));
        this.testCreateBigDecimalFailure("");
        this.testCreateBigDecimalFailure(" ");
        this.testCreateBigDecimalFailure("\b\t\n\f\r");
        
        this.testCreateBigDecimalFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateBigDecimalFailure
    protected void testCreateBigDecimalFailure(String str) {
        try {
            BigDecimal value = NumberUtils.createBigDecimal(str);
            fail("createBigDecimal(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinLong
    public void testMinLong() {
        final long[] l = null;
        try {
            NumberUtils.min(l);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new long[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(long[]) failed for array length 1",
            5,
            NumberUtils.min(new long[] { 5 }));

        assertEquals(
            "min(long[]) failed for array length 2",
            6,
            NumberUtils.min(new long[] { 6, 9 }));

        assertEquals(-10, NumberUtils.min(new long[] { -10, -5, 0, 5, 10 }));
        assertEquals(-10, NumberUtils.min(new long[] { -5, 0, -10, 5, 10 }));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinInt
    public void testMinInt() {
        final int[] i = null;
        try {
            NumberUtils.min(i);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new int[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(int[]) failed for array length 1",
            5,
            NumberUtils.min(new int[] { 5 }));

        assertEquals(
            "min(int[]) failed for array length 2",
            6,
            NumberUtils.min(new int[] { 6, 9 }));

        assertEquals(-10, NumberUtils.min(new int[] { -10, -5, 0, 5, 10 }));
        assertEquals(-10, NumberUtils.min(new int[] { -5, 0, -10, 5, 10 }));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinShort
    public void testMinShort() {
        final short[] s = null;
        try {
            NumberUtils.min(s);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new short[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(short[]) failed for array length 1",
            5,
            NumberUtils.min(new short[] { 5 }));

        assertEquals(
            "min(short[]) failed for array length 2",
            6,
            NumberUtils.min(new short[] { 6, 9 }));

        assertEquals(-10, NumberUtils.min(new short[] { -10, -5, 0, 5, 10 }));
        assertEquals(-10, NumberUtils.min(new short[] { -5, 0, -10, 5, 10 }));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinByte
    public void testMinByte() {
        final byte[] b = null;
        try {
            NumberUtils.min(b);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new byte[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(byte[]) failed for array length 1",
            5,
            NumberUtils.min(new byte[] { 5 }));

        assertEquals(
            "min(byte[]) failed for array length 2",
            6,
            NumberUtils.min(new byte[] { 6, 9 }));

        assertEquals(-10, NumberUtils.min(new byte[] { -10, -5, 0, 5, 10 }));
        assertEquals(-10, NumberUtils.min(new byte[] { -5, 0, -10, 5, 10 }));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinDouble
    public void testMinDouble() {
        final double[] d = null;
        try {
            NumberUtils.min(d);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new double[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(double[]) failed for array length 1",
            5.12,
            NumberUtils.min(new double[] { 5.12 }),
            0);

        assertEquals(
            "min(double[]) failed for array length 2",
            6.23,
            NumberUtils.min(new double[] { 6.23, 9.34 }),
            0);

        assertEquals(
            "min(double[]) failed for array length 5",
            -10.45,
            NumberUtils.min(new double[] { -10.45, -5.56, 0, 5.67, 10.78 }),
            0);
        assertEquals(-10, NumberUtils.min(new double[] { -10, -5, 0, 5, 10 }), 0.0001);
        assertEquals(-10, NumberUtils.min(new double[] { -5, 0, -10, 5, 10 }), 0.0001);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinFloat
    public void testMinFloat() {
        final float[] f = null;
        try {
            NumberUtils.min(f);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new float[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(float[]) failed for array length 1",
            5.9f,
            NumberUtils.min(new float[] { 5.9f }),
            0);

        assertEquals(
            "min(float[]) failed for array length 2",
            6.8f,
            NumberUtils.min(new float[] { 6.8f, 9.7f }),
            0);

        assertEquals(
            "min(float[]) failed for array length 5",
            -10.6f,
            NumberUtils.min(new float[] { -10.6f, -5.5f, 0, 5.4f, 10.3f }),
            0);
        assertEquals(-10, NumberUtils.min(new float[] { -10, -5, 0, 5, 10 }), 0.0001f);
        assertEquals(-10, NumberUtils.min(new float[] { -5, 0, -10, 5, 10 }), 0.0001f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxLong
    public void testMaxLong() {
        final long[] l = null;
        try {
            NumberUtils.max(l);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new long[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(long[]) failed for array length 1",
            5,
            NumberUtils.max(new long[] { 5 }));

        assertEquals(
            "max(long[]) failed for array length 2",
            9,
            NumberUtils.max(new long[] { 6, 9 }));

        assertEquals(
            "max(long[]) failed for array length 5",
            10,
            NumberUtils.max(new long[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new long[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new long[] { -5, 0, 10, 5, -10 }));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxInt
    public void testMaxInt() {
        final int[] i = null;
        try {
            NumberUtils.max(i);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new int[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(int[]) failed for array length 1",
            5,
            NumberUtils.max(new int[] { 5 }));

        assertEquals(
            "max(int[]) failed for array length 2",
            9,
            NumberUtils.max(new int[] { 6, 9 }));

        assertEquals(
            "max(int[]) failed for array length 5",
            10,
            NumberUtils.max(new int[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new int[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new int[] { -5, 0, 10, 5, -10 }));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxShort
    public void testMaxShort() {
        final short[] s = null;
        try {
            NumberUtils.max(s);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new short[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(short[]) failed for array length 1",
            5,
            NumberUtils.max(new short[] { 5 }));

        assertEquals(
            "max(short[]) failed for array length 2",
            9,
            NumberUtils.max(new short[] { 6, 9 }));

        assertEquals(
            "max(short[]) failed for array length 5",
            10,
            NumberUtils.max(new short[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new short[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new short[] { -5, 0, 10, 5, -10 }));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxByte
    public void testMaxByte() {
        final byte[] b = null;
        try {
            NumberUtils.max(b);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new byte[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(byte[]) failed for array length 1",
            5,
            NumberUtils.max(new byte[] { 5 }));

        assertEquals(
            "max(byte[]) failed for array length 2",
            9,
            NumberUtils.max(new byte[] { 6, 9 }));

        assertEquals(
            "max(byte[]) failed for array length 5",
            10,
            NumberUtils.max(new byte[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new byte[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new byte[] { -5, 0, 10, 5, -10 }));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxDouble
    public void testMaxDouble() {
        final double[] d = null;
        try {
            NumberUtils.max(d);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new double[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(double[]) failed for array length 1",
            5.1f,
            NumberUtils.max(new double[] { 5.1f }),
            0);

        assertEquals(
            "max(double[]) failed for array length 2",
            9.2f,
            NumberUtils.max(new double[] { 6.3f, 9.2f }),
            0);

        assertEquals(
            "max(double[]) failed for float length 5",
            10.4f,
            NumberUtils.max(new double[] { -10.5f, -5.6f, 0, 5.7f, 10.4f }),
            0);
        assertEquals(10, NumberUtils.max(new double[] { -10, -5, 0, 5, 10 }), 0.0001);
        assertEquals(10, NumberUtils.max(new double[] { -5, 0, 10, 5, -10 }), 0.0001);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxFloat
    public void testMaxFloat() {
        final float[] f = null;
        try {
            NumberUtils.max(f);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new float[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(float[]) failed for array length 1",
            5.1f,
            NumberUtils.max(new float[] { 5.1f }),
            0);

        assertEquals(
            "max(float[]) failed for array length 2",
            9.2f,
            NumberUtils.max(new float[] { 6.3f, 9.2f }),
            0);

        assertEquals(
            "max(float[]) failed for float length 5",
            10.4f,
            NumberUtils.max(new float[] { -10.5f, -5.6f, 0, 5.7f, 10.4f }),
            0);
        assertEquals(10, NumberUtils.max(new float[] { -10, -5, 0, 5, 10 }), 0.0001f);
        assertEquals(10, NumberUtils.max(new float[] { -5, 0, 10, 5, -10 }), 0.0001f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumLong
    public void testMinimumLong() {
        assertEquals("minimum(long,long,long) 1 failed", 12345L, NumberUtils.min(12345L, 12345L + 1L, 12345L + 2L));
        assertEquals("minimum(long,long,long) 2 failed", 12345L, NumberUtils.min(12345L + 1L, 12345L, 12345 + 2L));
        assertEquals("minimum(long,long,long) 3 failed", 12345L, NumberUtils.min(12345L + 1L, 12345L + 2L, 12345L));
        assertEquals("minimum(long,long,long) 4 failed", 12345L, NumberUtils.min(12345L + 1L, 12345L, 12345L));
        assertEquals("minimum(long,long,long) 5 failed", 12345L, NumberUtils.min(12345L, 12345L, 12345L));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumInt
    public void testMinimumInt() {
        assertEquals("minimum(int,int,int) 1 failed", 12345, NumberUtils.min(12345, 12345 + 1, 12345 + 2));
        assertEquals("minimum(int,int,int) 2 failed", 12345, NumberUtils.min(12345 + 1, 12345, 12345 + 2));
        assertEquals("minimum(int,int,int) 3 failed", 12345, NumberUtils.min(12345 + 1, 12345 + 2, 12345));
        assertEquals("minimum(int,int,int) 4 failed", 12345, NumberUtils.min(12345 + 1, 12345, 12345));
        assertEquals("minimum(int,int,int) 5 failed", 12345, NumberUtils.min(12345, 12345, 12345));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumShort
    public void testMinimumShort() {
        short low = 1234;
        short mid = 1234 + 1;
        short high = 1234 + 2;
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(low, mid, high));
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(mid, low, high));
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(mid, high, low));
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(low, mid, low));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumByte
    public void testMinimumByte() {
        byte low = 123;
        byte mid = 123 + 1;
        byte high = 123 + 2;
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(low, mid, high));
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(mid, low, high));
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(mid, high, low));
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(low, mid, low));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumDouble
    public void testMinimumDouble() {
        double low = 12.3;
        double mid = 12.3 + 1;
        double high = 12.3 + 2;
        assertEquals(low, NumberUtils.min(low, mid, high), 0.0001);
        assertEquals(low, NumberUtils.min(mid, low, high), 0.0001);
        assertEquals(low, NumberUtils.min(mid, high, low), 0.0001);
        assertEquals(low, NumberUtils.min(low, mid, low), 0.0001);
        assertEquals(mid, NumberUtils.min(high, mid, high), 0.0001);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumFloat
    public void testMinimumFloat() {
        float low = 12.3f;
        float mid = 12.3f + 1;
        float high = 12.3f + 2;
        assertEquals(low, NumberUtils.min(low, mid, high), 0.0001f);
        assertEquals(low, NumberUtils.min(mid, low, high), 0.0001f);
        assertEquals(low, NumberUtils.min(mid, high, low), 0.0001f);
        assertEquals(low, NumberUtils.min(low, mid, low), 0.0001f);
        assertEquals(mid, NumberUtils.min(high, mid, high), 0.0001f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumLong
    public void testMaximumLong() {
        assertEquals("maximum(long,long,long) 1 failed", 12345L, NumberUtils.max(12345L, 12345L - 1L, 12345L - 2L));
        assertEquals("maximum(long,long,long) 2 failed", 12345L, NumberUtils.max(12345L - 1L, 12345L, 12345L - 2L));
        assertEquals("maximum(long,long,long) 3 failed", 12345L, NumberUtils.max(12345L - 1L, 12345L - 2L, 12345L));
        assertEquals("maximum(long,long,long) 4 failed", 12345L, NumberUtils.max(12345L - 1L, 12345L, 12345L));
        assertEquals("maximum(long,long,long) 5 failed", 12345L, NumberUtils.max(12345L, 12345L, 12345L));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumInt
    public void testMaximumInt() {
        assertEquals("maximum(int,int,int) 1 failed", 12345, NumberUtils.max(12345, 12345 - 1, 12345 - 2));
        assertEquals("maximum(int,int,int) 2 failed", 12345, NumberUtils.max(12345 - 1, 12345, 12345 - 2));
        assertEquals("maximum(int,int,int) 3 failed", 12345, NumberUtils.max(12345 - 1, 12345 - 2, 12345));
        assertEquals("maximum(int,int,int) 4 failed", 12345, NumberUtils.max(12345 - 1, 12345, 12345));
        assertEquals("maximum(int,int,int) 5 failed", 12345, NumberUtils.max(12345, 12345, 12345));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumShort
    public void testMaximumShort() {
        short low = 1234;
        short mid = 1234 + 1;
        short high = 1234 + 2;
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(low, mid, high));
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(mid, low, high));
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(mid, high, low));
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(high, mid, high));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumByte
    public void testMaximumByte() {
        byte low = 123;
        byte mid = 123 + 1;
        byte high = 123 + 2;
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(low, mid, high));
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(mid, low, high));
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(mid, high, low));
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(high, mid, high));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumDouble
    public void testMaximumDouble() {
        double low = 12.3;
        double mid = 12.3 + 1;
        double high = 12.3 + 2;
        assertEquals(high, NumberUtils.max(low, mid, high), 0.0001);
        assertEquals(high, NumberUtils.max(mid, low, high), 0.0001);
        assertEquals(high, NumberUtils.max(mid, high, low), 0.0001);
        assertEquals(mid, NumberUtils.max(low, mid, low), 0.0001);
        assertEquals(high, NumberUtils.max(high, mid, high), 0.0001);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumFloat
    public void testMaximumFloat() {
        float low = 12.3f;
        float mid = 12.3f + 1;
        float high = 12.3f + 2;
        assertEquals(high, NumberUtils.max(low, mid, high), 0.0001f);
        assertEquals(high, NumberUtils.max(mid, low, high), 0.0001f);
        assertEquals(high, NumberUtils.max(mid, high, low), 0.0001f);
        assertEquals(mid, NumberUtils.max(low, mid, low), 0.0001f);
        assertEquals(high, NumberUtils.max(high, mid, high), 0.0001f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCompareDouble
    public void testCompareDouble() {
        assertTrue(Double.compare(Double.NaN, Double.NaN) == 0);
        assertTrue(Double.compare(Double.NaN, Double.POSITIVE_INFINITY) == +1);
        assertTrue(Double.compare(Double.NaN, Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.NaN, 1.2d) == +1);
        assertTrue(Double.compare(Double.NaN, 0.0d) == +1);
        assertTrue(Double.compare(Double.NaN, -0.0d) == +1);
        assertTrue(Double.compare(Double.NaN, -1.2d) == +1);
        assertTrue(Double.compare(Double.NaN, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.NaN, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.NaN) == -1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY) == 0);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, 1.2d) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, 0.0d) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, -0.0d) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, -1.2d) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(Double.MAX_VALUE, Double.NaN) == -1);
        assertTrue(Double.compare(Double.MAX_VALUE, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(Double.MAX_VALUE, Double.MAX_VALUE) == 0);
        assertTrue(Double.compare(Double.MAX_VALUE, 1.2d) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, 0.0d) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, -0.0d) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, -1.2d) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(1.2d, Double.NaN) == -1);
        assertTrue(Double.compare(1.2d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(1.2d, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(1.2d, 1.2d) == 0);
        assertTrue(Double.compare(1.2d, 0.0d) == +1);
        assertTrue(Double.compare(1.2d, -0.0d) == +1);
        assertTrue(Double.compare(1.2d, -1.2d) == +1);
        assertTrue(Double.compare(1.2d, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(1.2d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(0.0d, Double.NaN) == -1);
        assertTrue(Double.compare(0.0d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(0.0d, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(0.0d, 1.2d) == -1);
        assertTrue(Double.compare(0.0d, 0.0d) == 0);
        assertTrue(Double.compare(0.0d, -0.0d) == +1);
        assertTrue(Double.compare(0.0d, -1.2d) == +1);
        assertTrue(Double.compare(0.0d, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(0.0d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(-0.0d, Double.NaN) == -1);
        assertTrue(Double.compare(-0.0d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(-0.0d, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(-0.0d, 1.2d) == -1);
        assertTrue(Double.compare(-0.0d, 0.0d) == -1);
        assertTrue(Double.compare(-0.0d, -0.0d) == 0);
        assertTrue(Double.compare(-0.0d, -1.2d) == +1);
        assertTrue(Double.compare(-0.0d, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(-0.0d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(-1.2d, Double.NaN) == -1);
        assertTrue(Double.compare(-1.2d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(-1.2d, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(-1.2d, 1.2d) == -1);
        assertTrue(Double.compare(-1.2d, 0.0d) == -1);
        assertTrue(Double.compare(-1.2d, -0.0d) == -1);
        assertTrue(Double.compare(-1.2d, -1.2d) == 0);
        assertTrue(Double.compare(-1.2d, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(-1.2d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(-Double.MAX_VALUE, Double.NaN) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, 1.2d) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, 0.0d) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, -0.0d) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, -1.2d) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, -Double.MAX_VALUE) == 0);
        assertTrue(Double.compare(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.NaN) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, 1.2d) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, 0.0d) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, -0.0d) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, -1.2d) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCompareFloat
    public void testCompareFloat() {
        assertTrue(Float.compare(Float.NaN, Float.NaN) == 0);
        assertTrue(Float.compare(Float.NaN, Float.POSITIVE_INFINITY) == +1);
        assertTrue(Float.compare(Float.NaN, Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.NaN, 1.2f) == +1);
        assertTrue(Float.compare(Float.NaN, 0.0f) == +1);
        assertTrue(Float.compare(Float.NaN, -0.0f) == +1);
        assertTrue(Float.compare(Float.NaN, -1.2f) == +1);
        assertTrue(Float.compare(Float.NaN, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.NaN, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.NaN) == -1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) == 0);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, 1.2f) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, 0.0f) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, -0.0f) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, -1.2f) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(Float.MAX_VALUE, Float.NaN) == -1);
        assertTrue(Float.compare(Float.MAX_VALUE, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(Float.MAX_VALUE, Float.MAX_VALUE) == 0);
        assertTrue(Float.compare(Float.MAX_VALUE, 1.2f) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, 0.0f) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, -0.0f) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, -1.2f) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(1.2f, Float.NaN) == -1);
        assertTrue(Float.compare(1.2f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(1.2f, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(1.2f, 1.2f) == 0);
        assertTrue(Float.compare(1.2f, 0.0f) == +1);
        assertTrue(Float.compare(1.2f, -0.0f) == +1);
        assertTrue(Float.compare(1.2f, -1.2f) == +1);
        assertTrue(Float.compare(1.2f, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(1.2f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(0.0f, Float.NaN) == -1);
        assertTrue(Float.compare(0.0f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(0.0f, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(0.0f, 1.2f) == -1);
        assertTrue(Float.compare(0.0f, 0.0f) == 0);
        assertTrue(Float.compare(0.0f, -0.0f) == +1);
        assertTrue(Float.compare(0.0f, -1.2f) == +1);
        assertTrue(Float.compare(0.0f, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(0.0f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(-0.0f, Float.NaN) == -1);
        assertTrue(Float.compare(-0.0f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(-0.0f, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(-0.0f, 1.2f) == -1);
        assertTrue(Float.compare(-0.0f, 0.0f) == -1);
        assertTrue(Float.compare(-0.0f, -0.0f) == 0);
        assertTrue(Float.compare(-0.0f, -1.2f) == +1);
        assertTrue(Float.compare(-0.0f, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(-0.0f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(-1.2f, Float.NaN) == -1);
        assertTrue(Float.compare(-1.2f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(-1.2f, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(-1.2f, 1.2f) == -1);
        assertTrue(Float.compare(-1.2f, 0.0f) == -1);
        assertTrue(Float.compare(-1.2f, -0.0f) == -1);
        assertTrue(Float.compare(-1.2f, -1.2f) == 0);
        assertTrue(Float.compare(-1.2f, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(-1.2f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(-Float.MAX_VALUE, Float.NaN) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, 1.2f) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, 0.0f) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, -0.0f) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, -1.2f) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, -Float.MAX_VALUE) == 0);
        assertTrue(Float.compare(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.NaN) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, 1.2f) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, 0.0f) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, -0.0f) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, -1.2f) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testIsDigits
    public void testIsDigits() {
        assertEquals("isDigits(null) failed", false, NumberUtils.isDigits(null));
        assertEquals("isDigits('') failed", false, NumberUtils.isDigits(""));
        assertEquals("isDigits(String) failed", true, NumberUtils.isDigits("12345"));
        assertEquals("isDigits(String) neg 1 failed", false, NumberUtils.isDigits("1234.5"));
        assertEquals("isDigits(String) neg 3 failed", false, NumberUtils.isDigits("1ab"));
        assertEquals("isDigits(String) neg 4 failed", false, NumberUtils.isDigits("abc"));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testIsNumber
    public void testIsNumber() {
        String val = "12345";
        assertTrue("isNumber(String) 1 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 1 failed", checkCreateNumber(val));
        val = "1234.5";
        assertTrue("isNumber(String) 2 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 2 failed", checkCreateNumber(val));
        val = ".12345";
        assertTrue("isNumber(String) 3 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 3 failed", checkCreateNumber(val));
        val = "1234E5";
        assertTrue("isNumber(String) 4 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 4 failed", checkCreateNumber(val));
        val = "1234E+5";
        assertTrue("isNumber(String) 5 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 5 failed", checkCreateNumber(val));
        val = "1234E-5";
        assertTrue("isNumber(String) 6 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 6 failed", checkCreateNumber(val));
        val = "123.4E5";
        assertTrue("isNumber(String) 7 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 7 failed", checkCreateNumber(val));
        val = "-1234";
        assertTrue("isNumber(String) 8 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 8 failed", checkCreateNumber(val));
        val = "-1234.5";
        assertTrue("isNumber(String) 9 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 9 failed", checkCreateNumber(val));
        val = "-.12345";
        assertTrue("isNumber(String) 10 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 10 failed", checkCreateNumber(val));
        val = "-1234E5";
        assertTrue("isNumber(String) 11 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 11 failed", checkCreateNumber(val));
        val = "0";
        assertTrue("isNumber(String) 12 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 12 failed", checkCreateNumber(val));
        val = "-0";
        assertTrue("isNumber(String) 13 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 13 failed", checkCreateNumber(val));
        val = "01234";
        assertTrue("isNumber(String) 14 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 14 failed", checkCreateNumber(val));
        val = "-01234";
        assertTrue("isNumber(String) 15 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 15 failed", checkCreateNumber(val));
        val = "0xABC123";
        assertTrue("isNumber(String) 16 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 16 failed", checkCreateNumber(val));
        val = "0x0";
        assertTrue("isNumber(String) 17 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 17 failed", checkCreateNumber(val));
        val = "123.4E21D";
        assertTrue("isNumber(String) 19 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 19 failed", checkCreateNumber(val));
        val = "-221.23F";
        assertTrue("isNumber(String) 20 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 20 failed", checkCreateNumber(val));
        val = "22338L";
        assertTrue("isNumber(String) 21 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 21 failed", checkCreateNumber(val));
        val = null;
        assertTrue("isNumber(String) 1 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 1 Neg failed", !checkCreateNumber(val));
        val = "";
        assertTrue("isNumber(String) 2 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 2 Neg failed", !checkCreateNumber(val));
        val = "--2.3";
        assertTrue("isNumber(String) 3 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 3 Neg failed", !checkCreateNumber(val));
        val = ".12.3";
        assertTrue("isNumber(String) 4 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 4 Neg failed", !checkCreateNumber(val));
        val = "-123E";
        assertTrue("isNumber(String) 5 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 5 Neg failed", !checkCreateNumber(val));
        val = "-123E+-212";
        assertTrue("isNumber(String) 6 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 6 Neg failed", !checkCreateNumber(val));
        val = "-123E2.12";
        assertTrue("isNumber(String) 7 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 7 Neg failed", !checkCreateNumber(val));
        val = "0xGF";
        assertTrue("isNumber(String) 8 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 8 Neg failed", !checkCreateNumber(val));
        val = "0xFAE-1";
        assertTrue("isNumber(String) 9 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 9 Neg failed", !checkCreateNumber(val));
        val = ".";
        assertTrue("isNumber(String) 10 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 10 Neg failed", !checkCreateNumber(val));
        val = "-0ABC123";
        assertTrue("isNumber(String) 11 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 11 Neg failed", !checkCreateNumber(val));
        val = "123.4E-D";
        assertTrue("isNumber(String) 12 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 12 Neg failed", !checkCreateNumber(val));
        val = "123.4ED";
        assertTrue("isNumber(String) 13 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 13 Neg failed", !checkCreateNumber(val));
        val = "1234E5l";
        assertTrue("isNumber(String) 14 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 14 Neg failed", !checkCreateNumber(val));
        val = "11a";
        assertTrue("isNumber(String) 15 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 15 Neg failed", !checkCreateNumber(val)); 
        val = "1a";
        assertTrue("isNumber(String) 16 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 16 Neg failed", !checkCreateNumber(val)); 
        val = "a";
        assertTrue("isNumber(String) 17 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 17 Neg failed", !checkCreateNumber(val)); 
        val = "11g";
        assertTrue("isNumber(String) 18 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 18 Neg failed", !checkCreateNumber(val)); 
        val = "11z";
        assertTrue("isNumber(String) 19 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 19 Neg failed", !checkCreateNumber(val)); 
        val = "11def";
        assertTrue("isNumber(String) 20 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 20 Neg failed", !checkCreateNumber(val)); 
        val = "11d11";
        assertTrue("isNumber(String) 21 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 21 Neg failed", !checkCreateNumber(val)); 
        val = "11 11";
        assertTrue("isNumber(String) 22 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 22 Neg failed", !checkCreateNumber(val));
        val = " 1111";
        assertTrue("isNumber(String) 23 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 23 Neg failed", !checkCreateNumber(val));
        val = "1111 ";
        assertTrue("isNumber(String) 24 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 24 Neg failed", !checkCreateNumber(val));

        
        val = "2.";
        assertTrue("isNumber(String) LANG-521 failed", NumberUtils.isNumber(val));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testConstants
    public void testConstants() {
        assertTrue(NumberUtils.LONG_ZERO instanceof Long);
        assertTrue(NumberUtils.LONG_ONE instanceof Long);
        assertTrue(NumberUtils.LONG_MINUS_ONE instanceof Long);
        assertTrue(NumberUtils.INTEGER_ZERO instanceof Integer);
        assertTrue(NumberUtils.INTEGER_ONE instanceof Integer);
        assertTrue(NumberUtils.INTEGER_MINUS_ONE instanceof Integer);
        assertTrue(NumberUtils.SHORT_ZERO instanceof Short);
        assertTrue(NumberUtils.SHORT_ONE instanceof Short);
        assertTrue(NumberUtils.SHORT_MINUS_ONE instanceof Short);
        assertTrue(NumberUtils.BYTE_ZERO instanceof Byte);
        assertTrue(NumberUtils.BYTE_ONE instanceof Byte);
        assertTrue(NumberUtils.BYTE_MINUS_ONE instanceof Byte);
        assertTrue(NumberUtils.DOUBLE_ZERO instanceof Double);
        assertTrue(NumberUtils.DOUBLE_ONE instanceof Double);
        assertTrue(NumberUtils.DOUBLE_MINUS_ONE instanceof Double);
        assertTrue(NumberUtils.FLOAT_ZERO instanceof Float);
        assertTrue(NumberUtils.FLOAT_ONE instanceof Float);
        assertTrue(NumberUtils.FLOAT_MINUS_ONE instanceof Float);
        
        assertTrue(NumberUtils.LONG_ZERO.longValue() == 0);
        assertTrue(NumberUtils.LONG_ONE.longValue() == 1);
        assertTrue(NumberUtils.LONG_MINUS_ONE.longValue() == -1);
        assertTrue(NumberUtils.INTEGER_ZERO.intValue() == 0);
        assertTrue(NumberUtils.INTEGER_ONE.intValue() == 1);
        assertTrue(NumberUtils.INTEGER_MINUS_ONE.intValue() == -1);
        assertTrue(NumberUtils.SHORT_ZERO.shortValue() == 0);
        assertTrue(NumberUtils.SHORT_ONE.shortValue() == 1);
        assertTrue(NumberUtils.SHORT_MINUS_ONE.shortValue() == -1);
        assertTrue(NumberUtils.BYTE_ZERO.byteValue() == 0);
        assertTrue(NumberUtils.BYTE_ONE.byteValue() == 1);
        assertTrue(NumberUtils.BYTE_MINUS_ONE.byteValue() == -1);
        assertTrue(NumberUtils.DOUBLE_ZERO.doubleValue() == 0.0d);
        assertTrue(NumberUtils.DOUBLE_ONE.doubleValue() == 1.0d);
        assertTrue(NumberUtils.DOUBLE_MINUS_ONE.doubleValue() == -1.0d);
        assertTrue(NumberUtils.FLOAT_ZERO.floatValue() == 0.0f);
        assertTrue(NumberUtils.FLOAT_ONE.floatValue() == 1.0f);
        assertTrue(NumberUtils.FLOAT_MINUS_ONE.floatValue() == -1.0f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testLang300
    public void testLang300() {
        NumberUtils.createNumber("-1l");
        NumberUtils.createNumber("01l");
        NumberUtils.createNumber("1l");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testLang381
    public void testLang381() {
        assertTrue(Double.isNaN(NumberUtils.min(1.2, 2.5, Double.NaN)));
        assertTrue(Double.isNaN(NumberUtils.max(1.2, 2.5, Double.NaN)));
        assertTrue(Float.isNaN(NumberUtils.min(1.2f, 2.5f, Float.NaN)));
        assertTrue(Float.isNaN(NumberUtils.max(1.2f, 2.5f, Float.NaN)));

        double[] a = new double[] { 1.2, Double.NaN, 3.7, 27.0, 42.0, Double.NaN };
        assertTrue(Double.isNaN(NumberUtils.max(a)));
        assertTrue(Double.isNaN(NumberUtils.min(a)));

        double[] b = new double[] { Double.NaN, 1.2, Double.NaN, 3.7, 27.0, 42.0, Double.NaN };
        assertTrue(Double.isNaN(NumberUtils.max(b)));
        assertTrue(Double.isNaN(NumberUtils.min(b)));

        float[] aF = new float[] { 1.2f, Float.NaN, 3.7f, 27.0f, 42.0f, Float.NaN };
        assertTrue(Float.isNaN(NumberUtils.max(aF)));

        float[] bF = new float[] { Float.NaN, 1.2f, Float.NaN, 3.7f, 27.0f, 42.0f, Float.NaN };
        assertTrue(Float.isNaN(NumberUtils.max(bF)));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new WordUtils());
        Constructor<?>[] cons = WordUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(WordUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(WordUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testWrap_StringInt
    public void testWrap_StringInt() {
        assertEquals(null, WordUtils.wrap(null, 20));
        assertEquals(null, WordUtils.wrap(null, -1));
        
        assertEquals("", WordUtils.wrap("", 20));
        assertEquals("", WordUtils.wrap("", -1));
        
        
        String systemNewLine = System.getProperty("line.separator");
        String input = "Here is one line of text that is going to be wrapped after 20 columns.";
        String expected = "Here is one line of" + systemNewLine + "text that is going" 
            + systemNewLine + "to be wrapped after" + systemNewLine + "20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20));
        
        
        input = "Click here to jump to the jakarta website - http://jakarta.apache.org";
        expected = "Click here to jump" + systemNewLine + "to the jakarta" + systemNewLine 
            + "website -" + systemNewLine + "http://jakarta.apache.org";
        assertEquals(expected, WordUtils.wrap(input, 20));
        
        
        input = "Click here, http://jakarta.apache.org, to jump to the jakarta website";
        expected = "Click here," + systemNewLine + "http://jakarta.apache.org," + systemNewLine 
            + "to jump to the" + systemNewLine + "jakarta website";
        assertEquals(expected, WordUtils.wrap(input, 20));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testWrap_StringIntStringBoolean
    public void testWrap_StringIntStringBoolean() {
        assertEquals(null, WordUtils.wrap(null, 20, "\n", false));
        assertEquals(null, WordUtils.wrap(null, 20, "\n", true));
        assertEquals(null, WordUtils.wrap(null, 20, null, true));
        assertEquals(null, WordUtils.wrap(null, 20, null, false));
        assertEquals(null, WordUtils.wrap(null, -1, null, true));
        assertEquals(null, WordUtils.wrap(null, -1, null, false));
        
        assertEquals("", WordUtils.wrap("", 20, "\n", false));
        assertEquals("", WordUtils.wrap("", 20, "\n", true));
        assertEquals("", WordUtils.wrap("", 20, null, false));
        assertEquals("", WordUtils.wrap("", 20, null, true));
        assertEquals("", WordUtils.wrap("", -1, null, false));
        assertEquals("", WordUtils.wrap("", -1, null, true));
        
        
        String input = "Here is one line of text that is going to be wrapped after 20 columns.";
        String expected = "Here is one line of\ntext that is going\nto be wrapped after\n20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));

        
        input = "Here is one line of text that is going to be wrapped after 20 columns.";
        expected = "Here is one line of<br />text that is going<br />to be wrapped after<br />20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "<br />", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "<br />", true));

        
        input = "Here is one line";
        expected = "Here\nis one\nline";
        assertEquals(expected, WordUtils.wrap(input, 6, "\n", false));
        expected = "Here\nis\none\nline";
        assertEquals(expected, WordUtils.wrap(input, 2, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, -1, "\n", false));

        
        String systemNewLine = System.getProperty("line.separator");
        input = "Here is one line of text that is going to be wrapped after 20 columns.";
        expected = "Here is one line of" + systemNewLine + "text that is going" + systemNewLine 
            + "to be wrapped after" + systemNewLine + "20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, null, false));
        assertEquals(expected, WordUtils.wrap(input, 20, null, true));

        
        input = " Here:  is  one  line  of  text  that  is  going  to  be  wrapped  after  20  columns.";
        expected = "Here:  is  one  line\nof  text  that  is \ngoing  to  be \nwrapped  after  20 \ncolumns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));
        
        
        input = "Here is\tone line of text that is going to be wrapped after 20 columns.";
        expected = "Here is\tone line of\ntext that is going\nto be wrapped after\n20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));
        
        
        input = "Here is one line of\ttext that is going to be wrapped after 20 columns.";
        expected = "Here is one line\nof\ttext that is\ngoing to be wrapped\nafter 20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));
        
        
        input = "Click here to jump to the jakarta website - http://jakarta.apache.org";
        expected = "Click here to jump\nto the jakarta\nwebsite -\nhttp://jakarta.apache.org";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        expected = "Click here to jump\nto the jakarta\nwebsite -\nhttp://jakarta.apach\ne.org";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));
        
        
        input = "Click here, http://jakarta.apache.org, to jump to the jakarta website";
        expected = "Click here,\nhttp://jakarta.apache.org,\nto jump to the\njakarta website";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        expected = "Click here,\nhttp://jakarta.apach\ne.org, to jump to\nthe jakarta website";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));

    }

// org.apache.commons.lang3.text.WordUtilsTest::testCapitalize_String
    public void testCapitalize_String() {
        assertEquals(null, WordUtils.capitalize(null));
        assertEquals("", WordUtils.capitalize(""));
        assertEquals("  ", WordUtils.capitalize("  "));
        
        assertEquals("I", WordUtils.capitalize("I") );
        assertEquals("I", WordUtils.capitalize("i") );
        assertEquals("I Am Here 123", WordUtils.capitalize("i am here 123") );
        assertEquals("I Am Here 123", WordUtils.capitalize("I Am Here 123") );
        assertEquals("I Am HERE 123", WordUtils.capitalize("i am HERE 123") );
        assertEquals("I AM HERE 123", WordUtils.capitalize("I AM HERE 123") );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testCapitalizeWithDelimiters_String
    public void testCapitalizeWithDelimiters_String() {
        assertEquals(null, WordUtils.capitalize(null, null));
        assertEquals("", WordUtils.capitalize("", new char[0]));
        assertEquals("  ", WordUtils.capitalize("  ", new char[0]));
        
        char[] chars = new char[] { '-', '+', ' ', '@' };
        assertEquals("I", WordUtils.capitalize("I", chars) );
        assertEquals("I", WordUtils.capitalize("i", chars) );
        assertEquals("I-Am Here+123", WordUtils.capitalize("i-am here+123", chars) );
        assertEquals("I Am+Here-123", WordUtils.capitalize("I Am+Here-123", chars) );
        assertEquals("I+Am-HERE 123", WordUtils.capitalize("i+am-HERE 123", chars) );
        assertEquals("I-AM HERE+123", WordUtils.capitalize("I-AM HERE+123", chars) );
        chars = new char[] {'.'};
        assertEquals("I aM.Fine", WordUtils.capitalize("i aM.fine", chars) );
        assertEquals("I Am.fine", WordUtils.capitalize("i am.fine", null) );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testCapitalizeFully_String
    public void testCapitalizeFully_String() {
        assertEquals(null, WordUtils.capitalizeFully(null));
        assertEquals("", WordUtils.capitalizeFully(""));
        assertEquals("  ", WordUtils.capitalizeFully("  "));
        
        assertEquals("I", WordUtils.capitalizeFully("I") );
        assertEquals("I", WordUtils.capitalizeFully("i") );
        assertEquals("I Am Here 123", WordUtils.capitalizeFully("i am here 123") );
        assertEquals("I Am Here 123", WordUtils.capitalizeFully("I Am Here 123") );
        assertEquals("I Am Here 123", WordUtils.capitalizeFully("i am HERE 123") );
        assertEquals("I Am Here 123", WordUtils.capitalizeFully("I AM HERE 123") );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testCapitalizeFullyWithDelimiters_String
    public void testCapitalizeFullyWithDelimiters_String() {
        assertEquals(null, WordUtils.capitalizeFully(null, null));
        assertEquals("", WordUtils.capitalizeFully("", new char[0]));
        assertEquals("  ", WordUtils.capitalizeFully("  ", new char[0]));
        
        char[] chars = new char[] { '-', '+', ' ', '@' };
        assertEquals("I", WordUtils.capitalizeFully("I", chars) );
        assertEquals("I", WordUtils.capitalizeFully("i", chars) );
        assertEquals("I-Am Here+123", WordUtils.capitalizeFully("i-am here+123", chars) );
        assertEquals("I Am+Here-123", WordUtils.capitalizeFully("I Am+Here-123", chars) );
        assertEquals("I+Am-Here 123", WordUtils.capitalizeFully("i+am-HERE 123", chars) );
        assertEquals("I-Am Here+123", WordUtils.capitalizeFully("I-AM HERE+123", chars) );
        chars = new char[] {'.'};
        assertEquals("I am.Fine", WordUtils.capitalizeFully("i aM.fine", chars) );
        assertEquals("I Am.fine", WordUtils.capitalizeFully("i am.fine", null) );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testUncapitalize_String
    public void testUncapitalize_String() {
        assertEquals(null, WordUtils.uncapitalize(null));
        assertEquals("", WordUtils.uncapitalize(""));
        assertEquals("  ", WordUtils.uncapitalize("  "));
        
        assertEquals("i", WordUtils.uncapitalize("I") );
        assertEquals("i", WordUtils.uncapitalize("i") );
        assertEquals("i am here 123", WordUtils.uncapitalize("i am here 123") );
        assertEquals("i am here 123", WordUtils.uncapitalize("I Am Here 123") );
        assertEquals("i am hERE 123", WordUtils.uncapitalize("i am HERE 123") );
        assertEquals("i aM hERE 123", WordUtils.uncapitalize("I AM HERE 123") );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testUncapitalizeWithDelimiters_String
    public void testUncapitalizeWithDelimiters_String() {
        assertEquals(null, WordUtils.uncapitalize(null, null));
        assertEquals("", WordUtils.uncapitalize("", new char[0]));
        assertEquals("  ", WordUtils.uncapitalize("  ", new char[0]));
        
        char[] chars = new char[] { '-', '+', ' ', '@' };
        assertEquals("i", WordUtils.uncapitalize("I", chars) );
        assertEquals("i", WordUtils.uncapitalize("i", chars) );
        assertEquals("i am-here+123", WordUtils.uncapitalize("i am-here+123", chars) );
        assertEquals("i+am here-123", WordUtils.uncapitalize("I+Am Here-123", chars) );
        assertEquals("i-am+hERE 123", WordUtils.uncapitalize("i-am+HERE 123", chars) );
        assertEquals("i aM-hERE+123", WordUtils.uncapitalize("I AM-HERE+123", chars) );
        chars = new char[] {'.'};
        assertEquals("i AM.fINE", WordUtils.uncapitalize("I AM.FINE", chars) );
        assertEquals("i aM.FINE", WordUtils.uncapitalize("I AM.FINE", null) );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testInitials_String
    public void testInitials_String() {
        assertEquals(null, WordUtils.initials(null));
        assertEquals("", WordUtils.initials(""));
        assertEquals("", WordUtils.initials("  "));

        assertEquals("I", WordUtils.initials("I"));
        assertEquals("i", WordUtils.initials("i"));
        assertEquals("BJL", WordUtils.initials("Ben John Lee"));
        assertEquals("BJ", WordUtils.initials("Ben J.Lee"));
        assertEquals("BJ.L", WordUtils.initials(" Ben   John  . Lee"));
        assertEquals("iah1", WordUtils.initials("i am here 123"));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testInitials_String_charArray
    public void testInitials_String_charArray() {
        char[] array = null;
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("I", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("S", WordUtils.initials("SJC", array));
        assertEquals("BJL", WordUtils.initials("Ben John Lee", array));
        assertEquals("BJ", WordUtils.initials("Ben J.Lee", array));
        assertEquals("BJ.L", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("KO", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("iah1", WordUtils.initials("i am here 123", array));
        
        array = new char[0];
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("", WordUtils.initials("I", array));
        assertEquals("", WordUtils.initials("i", array));
        assertEquals("", WordUtils.initials("SJC", array));
        assertEquals("", WordUtils.initials("Ben John Lee", array));
        assertEquals("", WordUtils.initials("Ben J.Lee", array));
        assertEquals("", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("", WordUtils.initials("i am here 123", array));
        
        array = " ".toCharArray();
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("I", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("S", WordUtils.initials("SJC", array));
        assertEquals("BJL", WordUtils.initials("Ben John Lee", array));
        assertEquals("BJ", WordUtils.initials("Ben J.Lee", array));
        assertEquals("BJ.L", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("KO", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("iah1", WordUtils.initials("i am here 123", array));
        
        array = " .".toCharArray();
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("I", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("S", WordUtils.initials("SJC", array));
        assertEquals("BJL", WordUtils.initials("Ben John Lee", array));
        assertEquals("BJL", WordUtils.initials("Ben J.Lee", array));
        assertEquals("BJL", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("KO", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("iah1", WordUtils.initials("i am here 123", array));
        
        array = " .'".toCharArray();
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("I", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("S", WordUtils.initials("SJC", array));
        assertEquals("BJL", WordUtils.initials("Ben John Lee", array));
        assertEquals("BJL", WordUtils.initials("Ben J.Lee", array));
        assertEquals("BJL", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("KOM", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("iah1", WordUtils.initials("i am here 123", array));
        
        array = "SIJo1".toCharArray();
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals(" ", WordUtils.initials("  ", array));
        assertEquals("", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("C", WordUtils.initials("SJC", array));
        assertEquals("Bh", WordUtils.initials("Ben John Lee", array));
        assertEquals("B.", WordUtils.initials("Ben J.Lee", array));
        assertEquals(" h", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("K", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("i2", WordUtils.initials("i am here 123", array));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testSwapCase_String
    public void testSwapCase_String() {
        assertEquals(null, WordUtils.swapCase(null));
        assertEquals("", WordUtils.swapCase(""));
        assertEquals("  ", WordUtils.swapCase("  "));
        
        assertEquals("i", WordUtils.swapCase("I") );
        assertEquals("I", WordUtils.swapCase("i") );
        assertEquals("I AM HERE 123", WordUtils.swapCase("i am here 123") );
        assertEquals("i aM hERE 123", WordUtils.swapCase("I Am Here 123") );
        assertEquals("I AM here 123", WordUtils.swapCase("i am HERE 123") );
        assertEquals("i am here 123", WordUtils.swapCase("I AM HERE 123") );

        String test = "This String contains a TitleCase character: \u01C8";
        String expect = "tHIS sTRING CONTAINS A tITLEcASE CHARACTER: \u01C9";
        assertEquals(expect, WordUtils.swapCase(test));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testAbbreviate
    public void testAbbreviate() {
        
        assertNull(WordUtils.abbreviate(null, 1,-1,""));
        assertEquals(StringUtils.EMPTY, WordUtils.abbreviate("", 1,-1,""));

        
        assertEquals("01234", WordUtils.abbreviate("0123456789", 0,5,""));
        assertEquals("01234", WordUtils.abbreviate("0123456789", 5, 2,""));
        assertEquals("012", WordUtils.abbreviate("012 3456789", 2, 5,""));
        assertEquals("012 3", WordUtils.abbreviate("012 3456789", 5, 2,""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 0,-1,""));

        
        assertEquals("01234-", WordUtils.abbreviate("0123456789", 0,5,"-"));
        assertEquals("01234-", WordUtils.abbreviate("0123456789", 5, 2,"-"));
        assertEquals("012", WordUtils.abbreviate("012 3456789", 2, 5, null));
        assertEquals("012 3", WordUtils.abbreviate("012 3456789", 5, 2,""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 0,-1,""));

        
        assertEquals("012", WordUtils.abbreviate("012 3456789", 0,5, null));
        assertEquals("01234", WordUtils.abbreviate("01234 56789", 5, 10, null));
        assertEquals("01 23 45 67", WordUtils.abbreviate("01 23 45 67 89", 9, -1, null));
        assertEquals("01 23 45 6", WordUtils.abbreviate("01 23 45 67 89", 9, 10, null));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, 20, null));

        
        assertEquals("012", WordUtils.abbreviate("012 3456789", 0,5, null));
        assertEquals("01234-", WordUtils.abbreviate("01234 56789", 5, 10, "-"));
        assertEquals("01 23 45 67abc", WordUtils.abbreviate("01 23 45 67 89", 9, -1, "abc"));
        assertEquals("01 23 45 6", WordUtils.abbreviate("01 23 45 67 89", 9, 10, ""));

        
        assertEquals("", WordUtils.abbreviate("0123456790", 0,0,""));
        assertEquals("", WordUtils.abbreviate(" 0123456790", 0,-1,""));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new DurationFormatUtils());
        Constructor<?>[] cons = DurationFormatUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(DurationFormatUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DurationFormatUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationWords
    public void testFormatDurationWords() {
        String text = null;

        text = DurationFormatUtils.formatDurationWords(50 * 1000, true, false);
        assertEquals("50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, true, false);
        assertEquals("1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, true, false);
        assertEquals("2 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, true, false);
        assertEquals("2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, true, false);
        assertEquals("1 hour 12 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000, true, false);
        assertEquals("1 day 0 hours 0 minutes 0 seconds", text);

        text = DurationFormatUtils.formatDurationWords(50 * 1000, true, true);
        assertEquals("50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, true, true);
        assertEquals("1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, true, true);
        assertEquals("2 minutes", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, true, true);
        assertEquals("2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, true, true);
        assertEquals("1 hour 12 minutes", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000, true, true);
        assertEquals("1 day", text);

        text = DurationFormatUtils.formatDurationWords(50 * 1000, false, true);
        assertEquals("0 days 0 hours 0 minutes 50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, false, true);
        assertEquals("0 days 0 hours 1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, false, true);
        assertEquals("0 days 0 hours 2 minutes", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, false, true);
        assertEquals("0 days 0 hours 2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, false, true);
        assertEquals("0 days 1 hour 12 minutes", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000, false, true);
        assertEquals("1 day", text);

        text = DurationFormatUtils.formatDurationWords(50 * 1000, false, false);
        assertEquals("0 days 0 hours 0 minutes 50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, false, false);
        assertEquals("0 days 0 hours 1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, false, false);
        assertEquals("0 days 0 hours 2 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, false, false);
        assertEquals("0 days 0 hours 2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, false, false);
        assertEquals("0 days 1 hour 12 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000 + 72 * 60 * 1000, false, false);
        assertEquals("1 day 1 hour 12 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(2 * 24 * 60 * 60 * 1000 + 72 * 60 * 1000, false, false);
        assertEquals("2 days 1 hour 12 minutes 0 seconds", text);
        for (int i = 2; i < 31; i++) {
            text = DurationFormatUtils.formatDurationWords(i * 24 * 60 * 60 * 1000L, false, false);
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
        }
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationPluralWords
    public void testFormatDurationPluralWords() {
        long oneSecond = 1000;
        long oneMinute = oneSecond * 60;
        long oneHour = oneMinute * 60;
        long oneDay = oneHour * 24;
        String text = null;

        text = DurationFormatUtils.formatDurationWords(oneSecond, false, false);
        assertEquals("0 days 0 hours 0 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(oneSecond * 2, false, false);
        assertEquals("0 days 0 hours 0 minutes 2 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneSecond * 11, false, false);
        assertEquals("0 days 0 hours 0 minutes 11 seconds", text);

        text = DurationFormatUtils.formatDurationWords(oneMinute, false, false);
        assertEquals("0 days 0 hours 1 minute 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneMinute * 2, false, false);
        assertEquals("0 days 0 hours 2 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneMinute * 11, false, false);
        assertEquals("0 days 0 hours 11 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneMinute + oneSecond, false, false);
        assertEquals("0 days 0 hours 1 minute 1 second", text);

        text = DurationFormatUtils.formatDurationWords(oneHour, false, false);
        assertEquals("0 days 1 hour 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneHour * 2, false, false);
        assertEquals("0 days 2 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneHour * 11, false, false);
        assertEquals("0 days 11 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneHour + oneMinute + oneSecond, false, false);
        assertEquals("0 days 1 hour 1 minute 1 second", text);

        text = DurationFormatUtils.formatDurationWords(oneDay, false, false);
        assertEquals("1 day 0 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneDay * 2, false, false);
        assertEquals("2 days 0 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneDay * 11, false, false);
        assertEquals("11 days 0 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneDay + oneHour + oneMinute + oneSecond, false, false);
        assertEquals("1 day 1 hour 1 minute 1 second", text);
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationHMS
    public void testFormatDurationHMS() {
        long time = 0;
        assertEquals("0:00:00.000", DurationFormatUtils.formatDurationHMS(time));

        time = 1;
        assertEquals("0:00:00.001", DurationFormatUtils.formatDurationHMS(time));

        time = 15;
        assertEquals("0:00:00.015", DurationFormatUtils.formatDurationHMS(time));

        time = 165;
        assertEquals("0:00:00.165", DurationFormatUtils.formatDurationHMS(time));

        time = 1675;
        assertEquals("0:00:01.675", DurationFormatUtils.formatDurationHMS(time));

        time = 13465;
        assertEquals("0:00:13.465", DurationFormatUtils.formatDurationHMS(time));

        time = 72789;
        assertEquals("0:01:12.789", DurationFormatUtils.formatDurationHMS(time));

        time = 12789 + 32 * 60000;
        assertEquals("0:32:12.789", DurationFormatUtils.formatDurationHMS(time));

        time = 12789 + 62 * 60000;
        assertEquals("1:02:12.789", DurationFormatUtils.formatDurationHMS(time));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationISO
    public void testFormatDurationISO() {
        assertEquals("P0Y0M0DT0H0M0.000S", DurationFormatUtils.formatDurationISO(0L));
        assertEquals("P0Y0M0DT0H0M0.001S", DurationFormatUtils.formatDurationISO(1L));
        assertEquals("P0Y0M0DT0H0M0.010S", DurationFormatUtils.formatDurationISO(10L));
        assertEquals("P0Y0M0DT0H0M0.100S", DurationFormatUtils.formatDurationISO(100L));
        assertEquals("P0Y0M0DT0H1M15.321S", DurationFormatUtils.formatDurationISO(75321L));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDuration
    public void testFormatDuration() {
        long duration = 0;
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "y"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "M"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "d"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "H"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "m"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "s"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "S"));
        assertEquals("0000", DurationFormatUtils.formatDuration(duration, "SSSS"));
        assertEquals("0000", DurationFormatUtils.formatDuration(duration, "yyyy"));
        assertEquals("0000", DurationFormatUtils.formatDuration(duration, "yyMM"));

        duration = 60 * 1000;
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "y"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "M"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "d"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "H"));
        assertEquals("1", DurationFormatUtils.formatDuration(duration, "m"));
        assertEquals("60", DurationFormatUtils.formatDuration(duration, "s"));
        assertEquals("60000", DurationFormatUtils.formatDuration(duration, "S"));
        assertEquals("01:00", DurationFormatUtils.formatDuration(duration, "mm:ss"));

        Calendar base = Calendar.getInstance();
        base.set(2000, 0, 1, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);

        Calendar cal = Calendar.getInstance();
        cal.set(2003, 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        duration = cal.getTime().getTime() - base.getTime().getTime(); 
        
        
        int days = 366 + 365 + 365 + 31;
        assertEquals("0 0 " + days, DurationFormatUtils.formatDuration(duration, "y M d"));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatPeriodISO
    public void testFormatPeriodISO() {
        TimeZone timeZone = TimeZone.getTimeZone("GMT-3");
        Calendar base = Calendar.getInstance(timeZone);
        base.set(1970, 0, 1, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);

        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002, 1, 23, 9, 11, 12);
        cal.set(Calendar.MILLISECOND, 1);
        String text;
        
        text = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(cal);
        assertEquals("2002-02-23T09:11:12-03:00", text);
        
        text = DurationFormatUtils.formatPeriod(base.getTime().getTime(), cal.getTime().getTime(),
                DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN, false, timeZone);
        assertEquals("P32Y1M22DT9H11M12.001S", text);
        
        cal.set(1971, 1, 3, 10, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        text = DurationFormatUtils.formatPeriod(base.getTime().getTime(), cal.getTime().getTime(),
                DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN, false, timeZone);
        assertEquals("P1Y1M2DT10H30M0.000S", text);
        
        
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatPeriod
    public void testFormatPeriod() {
        Calendar cal1970 = Calendar.getInstance();
        cal1970.set(1970, 0, 1, 0, 0, 0);
        cal1970.set(Calendar.MILLISECOND, 0);
        long time1970 = cal1970.getTime().getTime();

        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "y"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "M"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "d"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "H"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "m"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "s"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "S"));
        assertEquals("0000", DurationFormatUtils.formatPeriod(time1970, time1970, "SSSS"));
        assertEquals("0000", DurationFormatUtils.formatPeriod(time1970, time1970, "yyyy"));
        assertEquals("0000", DurationFormatUtils.formatPeriod(time1970, time1970, "yyMM"));

        long time = time1970 + 60 * 1000;
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "y"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "M"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "d"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "H"));
        assertEquals("1", DurationFormatUtils.formatPeriod(time1970, time, "m"));
        assertEquals("60", DurationFormatUtils.formatPeriod(time1970, time, "s"));
        assertEquals("60000", DurationFormatUtils.formatPeriod(time1970, time, "S"));
        assertEquals("01:00", DurationFormatUtils.formatPeriod(time1970, time, "mm:ss"));

        Calendar cal = Calendar.getInstance();
        cal.set(1973, 6, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = cal.getTime().getTime();
        assertEquals("36", DurationFormatUtils.formatPeriod(time1970, time, "yM"));
        assertEquals("3 years 6 months", DurationFormatUtils.formatPeriod(time1970, time, "y' years 'M' months'"));
        assertEquals("03/06", DurationFormatUtils.formatPeriod(time1970, time, "yy/MM"));

        cal.set(1973, 10, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = cal.getTime().getTime();
        assertEquals("310", DurationFormatUtils.formatPeriod(time1970, time, "yM"));
        assertEquals("3 years 10 months", DurationFormatUtils.formatPeriod(time1970, time, "y' years 'M' months'"));
        assertEquals("03/10", DurationFormatUtils.formatPeriod(time1970, time, "yy/MM"));

        cal.set(1974, 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = cal.getTime().getTime();
        assertEquals("40", DurationFormatUtils.formatPeriod(time1970, time, "yM"));
        assertEquals("4 years 0 months", DurationFormatUtils.formatPeriod(time1970, time, "y' years 'M' months'"));
        assertEquals("04/00", DurationFormatUtils.formatPeriod(time1970, time, "yy/MM"));
        assertEquals("48", DurationFormatUtils.formatPeriod(time1970, time, "M"));
        assertEquals("48", DurationFormatUtils.formatPeriod(time1970, time, "MM"));
        assertEquals("048", DurationFormatUtils.formatPeriod(time1970, time, "MMM"));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testLexx
    public void testLexx() {
        
        assertArrayEquals(new DurationFormatUtils.Token[]{
            new DurationFormatUtils.Token(DurationFormatUtils.y, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.M, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.d, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.H, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.m, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.s, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.S, 1)}, DurationFormatUtils.lexx("yMdHmsS"));

        
        assertArrayEquals(new DurationFormatUtils.Token[]{
            new DurationFormatUtils.Token(DurationFormatUtils.H, 1),
            new DurationFormatUtils.Token(new StringBuffer(":"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.m, 2),
            new DurationFormatUtils.Token(new StringBuffer(":"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.s, 2),
            new DurationFormatUtils.Token(new StringBuffer("."), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.S, 3)}, DurationFormatUtils.lexx("H:mm:ss.SSS"));

        
        assertArrayEquals(new DurationFormatUtils.Token[]{
            new DurationFormatUtils.Token(new StringBuffer("P"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.y, 4),
            new DurationFormatUtils.Token(new StringBuffer("Y"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.M, 1),
            new DurationFormatUtils.Token(new StringBuffer("M"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.d, 1),
            new DurationFormatUtils.Token(new StringBuffer("DT"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.H, 1),
            new DurationFormatUtils.Token(new StringBuffer("H"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.m, 1),
            new DurationFormatUtils.Token(new StringBuffer("M"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.s, 1),
            new DurationFormatUtils.Token(new StringBuffer("."), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.S, 1),
            new DurationFormatUtils.Token(new StringBuffer("S"), 1)}, DurationFormatUtils
                .lexx(DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN));

        
        DurationFormatUtils.Token token = new DurationFormatUtils.Token(DurationFormatUtils.y, 4);
        assertFalse("Token equal to non-Token class. ", token.equals(new Object()));
        assertFalse("Token equal to Token with wrong value class. ", token.equals(new DurationFormatUtils.Token(
                new Object())));
        assertFalse("Token equal to Token with different count. ", token.equals(new DurationFormatUtils.Token(
                DurationFormatUtils.y, 1)));
        DurationFormatUtils.Token numToken = new DurationFormatUtils.Token(new Integer(1), 4);
        assertTrue("Token with Number value not equal to itself. ", numToken.equals(numToken));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testBugzilla38401
    public void testBugzilla38401() {
        assertEqualDuration( "0000/00/30 16:00:00 000", new int[] { 2006, 0, 26, 18, 47, 34 }, 
                             new int[] { 2006, 1, 26, 10, 47, 34 }, "yyyy/MM/dd HH:mm:ss SSS");
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testJiraLang281
    public void testJiraLang281() {
        assertEqualDuration( "09", new int[] { 2005, 11, 31, 0, 0, 0 }, 
                             new int[] { 2006, 9, 6, 0, 0, 0 }, "MM");
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testLowDurations
    public void testLowDurations() {
        for(int hr=0; hr < 24; hr++) {
            for(int min=0; min < 60; min++) {
                for(int sec=0; sec < 60; sec++) {
                    assertEqualDuration( hr + ":" + min + ":" + sec, 
                                         new int[] { 2000, 0, 1, 0, 0, 0, 0 },
                                         new int[] { 2000, 0, 1, hr, min, sec },
                                         "H:m:s"
                                       );
                }
            }
        }
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testEdgeDurations
    public void testEdgeDurations() {
        assertEqualDuration( "01", new int[] { 2006, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 2, 10, 0, 0, 0 }, "MM");
        assertEqualDuration( "12", new int[] { 2005, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 0, 15, 0, 0, 0 }, "MM");
        assertEqualDuration( "12", new int[] { 2005, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 0, 16, 0, 0, 0 }, "MM");
        assertEqualDuration( "11", new int[] { 2005, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 0, 14, 0, 0, 0 }, "MM");
        
        assertEqualDuration( "01 26", new int[] { 2006, 0, 15, 0, 0, 0 },
                             new int[] { 2006, 2, 10, 0, 0, 0 }, "MM dd");
        assertEqualDuration( "54", new int[] { 2006, 0, 15, 0, 0, 0 },
                             new int[] { 2006, 2, 10, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "09 12", new int[] { 2006, 1, 20, 0, 0, 0 },
                             new int[] { 2006, 11, 4, 0, 0, 0 }, "MM dd");
        assertEqualDuration( "287", new int[] { 2006, 1, 20, 0, 0, 0 },
                             new int[] { 2006, 11, 4, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "11 30", new int[] { 2006, 0, 2, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "MM dd"); 
        assertEqualDuration( "364", new int[] { 2006, 0, 2, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "12 00", new int[] { 2006, 0, 1, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "MM dd"); 
        assertEqualDuration( "365", new int[] { 2006, 0, 1, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "dd"); 
    
        assertEqualDuration( "31", new int[] { 2006, 0, 1, 0, 0, 0 },
                new int[] { 2006, 1, 1, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "92", new int[] { 2005, 9, 1, 0, 0, 0 },
                new int[] { 2006, 0, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "77", new int[] { 2005, 9, 16, 0, 0, 0 },
                new int[] { 2006, 0, 1, 0, 0, 0 }, "dd"); 

        
        assertEqualDuration( "136", new int[] { 2005, 9, 16, 0, 0, 0 },
                new int[] { 2006, 2, 1, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "136", new int[] { 2004, 9, 16, 0, 0, 0 },
                new int[] { 2005, 2, 1, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "137", new int[] { 2003, 9, 16, 0, 0, 0 },
                new int[] { 2004, 2, 1, 0, 0, 0 }, "dd");         
        
        assertEqualDuration( "135", new int[] { 2003, 9, 16, 0, 0, 0 },
                new int[] { 2004, 1, 28, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "364", new int[] { 2007, 0, 2, 0, 0, 0 },
                new int[] { 2008, 0, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "729", new int[] { 2006, 0, 2, 0, 0, 0 },
                new int[] { 2008, 0, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "365", new int[] { 2007, 2, 2, 0, 0, 0 },
                new int[] { 2008, 2, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "333", new int[] { 2007, 1, 2, 0, 0, 0 },
                new int[] { 2008, 0, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "28", new int[] { 2008, 1, 2, 0, 0, 0 },
                new int[] { 2008, 2, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "393", new int[] { 2007, 1, 2, 0, 0, 0 },
                new int[] { 2008, 2, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "369", new int[] { 2004, 0, 29, 0, 0, 0 },
                new int[] { 2005, 1, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "338", new int[] { 2004, 1, 29, 0, 0, 0 },
                new int[] { 2005, 1, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "28", new int[] { 2004, 2, 8, 0, 0, 0 },
                new int[] { 2004, 3, 5, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "48", new int[] { 1992, 1, 29, 0, 0, 0 },
                new int[] { 1996, 1, 29, 0, 0, 0 }, "M"); 
        
        
        
        
        assertEqualDuration( "11", new int[] { 1996, 1, 29, 0, 0, 0 },
                new int[] { 1997, 1, 28, 0, 0, 0 }, "M"); 
        
        assertEqualDuration( "11 28", new int[] { 1996, 1, 29, 0, 0, 0 },
                new int[] { 1997, 1, 28, 0, 0, 0 }, "M d"); 
        
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testDurationsByBruteForce
    public void testDurationsByBruteForce() {
        bruteForce(2006, 0, 1, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2006, 0, 2, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2007, 1, 2, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2004, 1, 29, "d", Calendar.DAY_OF_MONTH);
        bruteForce(1996, 1, 29, "d", Calendar.DAY_OF_MONTH);

        bruteForce(1969, 1, 28, "M", Calendar.MONTH);  
        
    }

// org.apache.commons.lang3.time.StopWatchTest::testStopWatchSimple
    public void testStopWatchSimple(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long time = watch.getTime();
        assertEquals(time, watch.getTime());
        
        assertTrue(time >= 500);
        assertTrue(time < 700);
        
        watch.reset();
        assertEquals(0, watch.getTime());
    }

// org.apache.commons.lang3.time.StopWatchTest::testStopWatchSimpleGet
    public void testStopWatchSimpleGet(){
        StopWatch watch = new StopWatch();
        assertEquals(0, watch.getTime());
        assertEquals("0:00:00.000", watch.toString());
        
        watch.start();
            try {Thread.sleep(500);} catch (InterruptedException ex) {}
        assertTrue(watch.getTime() < 2000);
    }

// org.apache.commons.lang3.time.StopWatchTest::testStopWatchSplit
    public void testStopWatchSplit(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.split();
        long splitTime = watch.getSplitTime();
        String splitStr = watch.toSplitString();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.unsplit();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();

        assertEquals("Formatted split string not the correct length", 
                     splitStr.length(), 11);
        assertTrue(splitTime >= 500);
        assertTrue(splitTime < 700);
        assertTrue(totalTime >= 1500);
        assertTrue(totalTime < 1900);
    }

// org.apache.commons.lang3.time.StopWatchTest::testStopWatchSuspend
    public void testStopWatchSuspend(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.suspend();
        long suspendTime = watch.getTime();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.resume();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();
        
        assertTrue(suspendTime >= 500);
        assertTrue(suspendTime < 700);
        assertTrue(totalTime >= 1000);
        assertTrue(totalTime < 1300);
    }

// org.apache.commons.lang3.time.StopWatchTest::testLang315
    public void testLang315() {
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(200);} catch (InterruptedException ex) {}
        watch.suspend();
        long suspendTime = watch.getTime();
            try {Thread.sleep(200);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();
        assertTrue( suspendTime == totalTime );
    }

// org.apache.commons.lang3.time.StopWatchTest::testBadStates
    public void testBadStates() {
        StopWatch watch = new StopWatch();
        try {
            watch.stop();
            fail("Calling stop on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.stop();
            fail("Calling stop on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.suspend();
            fail("Calling suspend on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.split();
            fail("Calling split on a non-running StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.resume();
            fail("Calling resume on an unsuspended StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        watch.start();

        try {
            watch.start();
            fail("Calling start on a started StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.getSplitTime();
            fail("Calling getSplitTime on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.resume();
            fail("Calling resume on an unsuspended StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        watch.stop();

        try {
            watch.start();
            fail("Calling start on a stopped StopWatch should throw an exception as it needs to be reset. ");
        } catch(IllegalStateException ise) {
            
        }
    }

// org.apache.commons.lang3.time.StopWatchTest::testGetStartTime
    public void testGetStartTime() {
        long beforeStopWatch = System.currentTimeMillis();
        StopWatch watch = new StopWatch();
        try {
            watch.getStartTime();
            fail("Calling getStartTime on an unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
            
        }
        watch.start();
        try {
            watch.getStartTime();
            Assert.assertTrue(watch.getStartTime() >= beforeStopWatch);
        } catch (IllegalStateException ex) {
            fail("Start time should be available: " + ex.getMessage());
        }
        watch.reset();
        try {
            watch.getStartTime();
            fail("Calling getStartTime on a reset, but unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
            
        }
    }
