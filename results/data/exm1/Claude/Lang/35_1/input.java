// buggy code
    public static <T> T[] add(T[] array, T element) {
        Class<?> type;
        if (array != null){
            type = array.getClass();
        } else if (element != null) {
            type = element.getClass();
        } else {
            type = Object.class;
        }
        @SuppressWarnings("unchecked") // type must be T
        T[] newArray = (T[]) copyArrayGrow1(array, type);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    public static <T> T[] add(T[] array, int index, T element) {
        Class<?> clss = null;
        if (array != null) {
            clss = array.getClass().getComponentType();
        } else if (element != null) {
            clss = element.getClass();
        } else {
            return (T[]) new Object[] { null };
        }
        @SuppressWarnings("unchecked") // the add method creates an array of type clss, which is type T
        final T[] newArray = (T[]) add(array, index, element, clss);
        return newArray;
    }

// relevant test
// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOf_String
    public void testLastIndexOf_String() {
        assertEquals(-1, StringUtils.lastIndexOf(null, null));
        assertEquals(-1, StringUtils.lastIndexOf("", null));
        assertEquals(-1, StringUtils.lastIndexOf("", "a"));
        assertEquals(0, StringUtils.lastIndexOf("", ""));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a"));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastOrdinalIndexOf
    public void testLastOrdinalIndexOf() {
        assertEquals(-1, StringUtils.lastOrdinalIndexOf(null, "*", 42) );
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("*", null, 42) );
        assertEquals(0, StringUtils.lastOrdinalIndexOf("", "", 42) );
        assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1) );
        assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2) );
        assertEquals(5, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 1) );
        assertEquals(2, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 2) );
        assertEquals(4, StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 1) );
        assertEquals(1, StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 2) );
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 1) );
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 2) );
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOf_StringInt
    public void testLastIndexOf_StringInt() {
        assertEquals(-1, StringUtils.lastIndexOf(null, null, 0));
        assertEquals(-1, StringUtils.lastIndexOf(null, null, -1));
        assertEquals(-1, StringUtils.lastIndexOf(null, "", 0));
        assertEquals(-1, StringUtils.lastIndexOf(null, "", -1));
        assertEquals(-1, StringUtils.lastIndexOf("", null, 0));
        assertEquals(-1, StringUtils.lastIndexOf("", null, -1));
        assertEquals(0, StringUtils.lastIndexOf("", "", 0));
        assertEquals(-1, StringUtils.lastIndexOf("", "", -1));
        assertEquals(0, StringUtils.lastIndexOf("", "", 9));
        assertEquals(0, StringUtils.lastIndexOf("abc", "", 0));
        assertEquals(-1, StringUtils.lastIndexOf("abc", "", -1));
        assertEquals(3, StringUtils.lastIndexOf("abc", "", 9));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a", 8));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 8));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab", 8));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", "b", 3));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 9));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", -1));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", 0));
        assertEquals(0, StringUtils.lastIndexOf("aabaabaa", "a", 0));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOfIgnoreCase_String
    public void testLastIndexOfIgnoreCase_String() {
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, null));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", null));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, ""));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", "a"));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("", ""));
        assertEquals(8, StringUtils.lastIndexOfIgnoreCase("aabaabaa", ""));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "a"));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "b"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "ab"));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB"));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("ab", "AAB"));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("aab", "AAB"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOfIgnoreCase_StringInt
    public void testLastIndexOfIgnoreCase_StringInt() {
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, null, 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, null, -1));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "", -1));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", null, 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", null, -1));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("", "", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", "", -1));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("", "", 9));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("abc", "", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("abc", "", -1));
        assertEquals(3, StringUtils.lastIndexOfIgnoreCase("abc", "", 9));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 8));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB", 8));
        assertEquals(2, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 3));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 9));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 0));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 0));
        assertEquals(1, StringUtils.lastIndexOfIgnoreCase("aab", "AB", 1));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsChar
    public void testContainsChar() {
        assertEquals(false, StringUtils.contains(null, ' '));
        assertEquals(false, StringUtils.contains("", ' '));
        assertEquals(false, StringUtils.contains("",null));
        assertEquals(false, StringUtils.contains(null,null));
        assertEquals(true, StringUtils.contains("abc", 'a'));
        assertEquals(true, StringUtils.contains("abc", 'b'));
        assertEquals(true, StringUtils.contains("abc", 'c'));
        assertEquals(false, StringUtils.contains("abc", 'z'));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsString
    public void testContainsString() {
        assertEquals(false, StringUtils.contains(null, null));
        assertEquals(false, StringUtils.contains(null, ""));
        assertEquals(false, StringUtils.contains(null, "a"));
        assertEquals(false, StringUtils.contains("", null));
        assertEquals(true, StringUtils.contains("", ""));
        assertEquals(false, StringUtils.contains("", "a"));
        assertEquals(true, StringUtils.contains("abc", "a"));
        assertEquals(true, StringUtils.contains("abc", "b"));
        assertEquals(true, StringUtils.contains("abc", "c"));
        assertEquals(true, StringUtils.contains("abc", "abc"));
        assertEquals(false, StringUtils.contains("abc", "z"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsIgnoreCase_StringString
    public void testContainsIgnoreCase_StringString() {
        assertFalse(StringUtils.containsIgnoreCase(null, null));
        
        
        assertFalse(StringUtils.containsIgnoreCase(null, ""));
        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase(null, "abc"));
        
        assertFalse(StringUtils.containsIgnoreCase("", null));
        assertFalse(StringUtils.containsIgnoreCase("a", null));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
        
        
        assertTrue(StringUtils.containsIgnoreCase("", ""));
        assertTrue(StringUtils.containsIgnoreCase("a", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", ""));

        
        assertFalse(StringUtils.containsIgnoreCase("", "a"));
        assertTrue(StringUtils.containsIgnoreCase("a", "a"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "a"));
        assertFalse(StringUtils.containsIgnoreCase("", "A"));
        assertTrue(StringUtils.containsIgnoreCase("a", "A"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        
        
        assertFalse(StringUtils.containsIgnoreCase("", "abc"));
        assertFalse(StringUtils.containsIgnoreCase("a", "abc"));
        assertTrue(StringUtils.containsIgnoreCase("xabcz", "abc"));
        assertFalse(StringUtils.containsIgnoreCase("", "ABC"));
        assertFalse(StringUtils.containsIgnoreCase("a", "ABC"));
        assertTrue(StringUtils.containsIgnoreCase("xabcz", "ABC"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsIgnoreCase_LocaleIndependence
    public void testContainsIgnoreCase_LocaleIndependence() {
        Locale orig = Locale.getDefault();

        Locale[] locales = { Locale.ENGLISH, new Locale("tr"), Locale.getDefault() };

        String[][] tdata = { 
            { "i", "I" },
            { "I", "i" },
            { "\u03C2", "\u03C3" },
            { "\u03A3", "\u03C2" },
            { "\u03A3", "\u03C3" },
        };

        String[][] fdata = { 
            { "\u00DF", "SS" },
        };

        try {
            for (int i = 0; i < locales.length; i++) {
                Locale.setDefault(locales[i]);
                for (int j = 0; j < tdata.length; j++) {
                    assertTrue(Locale.getDefault() + ": " + j + " " + tdata[j][0] + " " + tdata[j][1], StringUtils
                            .containsIgnoreCase(tdata[j][0], tdata[j][1]));
                }
                for (int j = 0; j < fdata.length; j++) {
                    assertFalse(Locale.getDefault() + ": " + j + " " + fdata[j][0] + " " + fdata[j][1], StringUtils
                            .containsIgnoreCase(fdata[j][0], fdata[j][1]));
                }
            }
        } finally {
            Locale.setDefault(orig);
        }
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAny_StringStringarray
    public void testIndexOfAny_StringStringarray() {
        assertEquals(-1, StringUtils.indexOfAny(null, (String[]) null));
        assertEquals(-1, StringUtils.indexOfAny(null, FOOBAR_SUB_ARRAY));
        assertEquals(-1, StringUtils.indexOfAny(FOOBAR, (String[]) null));
        assertEquals(2, StringUtils.indexOfAny(FOOBAR, FOOBAR_SUB_ARRAY));
        assertEquals(-1, StringUtils.indexOfAny(FOOBAR, new String[0]));
        assertEquals(-1, StringUtils.indexOfAny(null, new String[0]));
        assertEquals(-1, StringUtils.indexOfAny("", new String[0]));
        assertEquals(-1, StringUtils.indexOfAny(FOOBAR, new String[] {"llll"}));
        assertEquals(0, StringUtils.indexOfAny(FOOBAR, new String[] {""}));
        assertEquals(0, StringUtils.indexOfAny("", new String[] {""}));
        assertEquals(-1, StringUtils.indexOfAny("", new String[] {"a"}));
        assertEquals(-1, StringUtils.indexOfAny("", new String[] {null}));
        assertEquals(-1, StringUtils.indexOfAny(FOOBAR, new String[] {null}));
        assertEquals(-1, StringUtils.indexOfAny(null, new String[] {null}));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOfAny_StringStringarray
    public void testLastIndexOfAny_StringStringarray() {
        assertEquals(-1, StringUtils.lastIndexOfAny(null, null));
        assertEquals(-1, StringUtils.lastIndexOfAny(null, FOOBAR_SUB_ARRAY));
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR, null));
        assertEquals(3, StringUtils.lastIndexOfAny(FOOBAR, FOOBAR_SUB_ARRAY));
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR, new String[0]));
        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[0]));
        assertEquals(-1, StringUtils.lastIndexOfAny("", new String[0]));
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR, new String[] {"llll"}));
        assertEquals(6, StringUtils.lastIndexOfAny(FOOBAR, new String[] {""}));
        assertEquals(0, StringUtils.lastIndexOfAny("", new String[] {""}));
        assertEquals(-1, StringUtils.lastIndexOfAny("", new String[] {"a"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("", new String[] {null}));
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR, new String[] {null}));
        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[] {null}));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAny_StringChararray
    public void testIndexOfAny_StringChararray() {
        assertEquals(-1, StringUtils.indexOfAny(null, (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny(null, new char[0]));
        assertEquals(-1, StringUtils.indexOfAny(null, new char[] {'a','b'}));
        
        assertEquals(-1, StringUtils.indexOfAny("", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("", new char[0]));
        assertEquals(-1, StringUtils.indexOfAny("", new char[] {'a','b'}));
        
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (char[]) null)); 
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new char[0])); 
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new char[] {'z','a'})); 
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", new char[] {'b','y'}));
        assertEquals(-1, StringUtils.indexOfAny("ab", new char[] {'z'}));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAny_StringString
    public void testIndexOfAny_StringString() {
        assertEquals(-1, StringUtils.indexOfAny(null, (String) null));
        assertEquals(-1, StringUtils.indexOfAny(null, ""));
        assertEquals(-1, StringUtils.indexOfAny(null, "ab"));
        
        assertEquals(-1, StringUtils.indexOfAny("", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("", ""));
        assertEquals(-1, StringUtils.indexOfAny("", "ab"));
        
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (String) null)); 
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", "")); 
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za")); 
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", "by"));
        assertEquals(-1, StringUtils.indexOfAny("ab", "z"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsAny_StringChararray
    public void testContainsAny_StringChararray() {
        assertFalse(StringUtils.containsAny(null, (char[]) null));
        assertFalse(StringUtils.containsAny(null, new char[0]));
        assertFalse(StringUtils.containsAny(null, new char[] {'a','b'}));
        
        assertFalse(StringUtils.containsAny("", (char[]) null));
        assertFalse(StringUtils.containsAny("", new char[0]));
        assertFalse(StringUtils.containsAny("", new char[] {'a','b'}));
        
        assertFalse(StringUtils.containsAny("zzabyycdxx", (char[]) null)); 
        assertFalse(StringUtils.containsAny("zzabyycdxx", new char[0])); 
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[] {'z','a'})); 
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[] {'b','y'}));
        assertFalse(StringUtils.containsAny("ab", new char[] {'z'}));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsAny_StringString
    public void testContainsAny_StringString() {
        assertFalse(StringUtils.containsAny(null, (String) null));
        assertFalse(StringUtils.containsAny(null, ""));
        assertFalse(StringUtils.containsAny(null, "ab"));
        
        assertFalse(StringUtils.containsAny("", (String) null));
        assertFalse(StringUtils.containsAny("", ""));
        assertFalse(StringUtils.containsAny("", "ab"));
        
        assertFalse(StringUtils.containsAny("zzabyycdxx", (String) null)); 
        assertFalse(StringUtils.containsAny("zzabyycdxx", "")); 
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za")); 
        assertTrue(StringUtils.containsAny("zzabyycdxx", "by"));
        assertFalse(StringUtils.containsAny("ab", "z"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAnyBut_StringChararray
    public void testIndexOfAnyBut_StringChararray() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, (char[]) null));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[0]));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[] {'a','b'}));
        
        assertEquals(-1, StringUtils.indexOfAnyBut("", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("", new char[0]));
        assertEquals(-1, StringUtils.indexOfAnyBut("", new char[] {'a','b'}));
        
        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", new char[0]));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", new char[] {'z','a'})); 
        assertEquals(0, StringUtils.indexOfAnyBut("zzabyycdxx", new char[] {'b','y'})); 
        assertEquals(0, StringUtils.indexOfAnyBut("ab", new char[] {'z'}));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAnyBut_StringString
    public void testIndexOfAnyBut_StringString() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, (String) null));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, ""));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, "ab"));
        
        assertEquals(-1, StringUtils.indexOfAnyBut("", (String) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("", ""));
        assertEquals(-1, StringUtils.indexOfAnyBut("", "ab"));
        
        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", (String) null)); 
        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", "")); 
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za")); 
        assertEquals(0, StringUtils.indexOfAnyBut("zzabyycdxx", "by"));
        assertEquals(0, StringUtils.indexOfAnyBut("ab", "z"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsOnly_String
    public void testContainsOnly_String() {
        String str1 = "a";
        String str2 = "b";
        String str3 = "ab";
        String chars1= "b";
        String chars2= "a";
        String chars3= "ab";
        assertEquals(false, StringUtils.containsOnly(null, (String) null));
        assertEquals(false, StringUtils.containsOnly("", (String) null));
        assertEquals(false, StringUtils.containsOnly(null, ""));
        assertEquals(false, StringUtils.containsOnly(str1, ""));
        assertEquals(true, StringUtils.containsOnly("", ""));
        assertEquals(true, StringUtils.containsOnly("", chars1));
        assertEquals(false, StringUtils.containsOnly(str1, chars1));
        assertEquals(true, StringUtils.containsOnly(str1, chars2));
        assertEquals(true, StringUtils.containsOnly(str1, chars3));
        assertEquals(true, StringUtils.containsOnly(str2, chars1));
        assertEquals(false, StringUtils.containsOnly(str2, chars2));
        assertEquals(true, StringUtils.containsOnly(str2, chars3));
        assertEquals(false, StringUtils.containsOnly(str3, chars1));
        assertEquals(false, StringUtils.containsOnly(str3, chars2));
        assertEquals(true, StringUtils.containsOnly(str3, chars3));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsOnly_Chararray
    public void testContainsOnly_Chararray() {
        String str1 = "a";
        String str2 = "b";
        String str3 = "ab";
        char[] chars1= {'b'};
        char[] chars2= {'a'};
        char[] chars3= {'a', 'b'};
        char[] emptyChars = new char[0];
        assertEquals(false, StringUtils.containsOnly(null, (char[]) null));
        assertEquals(false, StringUtils.containsOnly("", (char[]) null));
        assertEquals(false, StringUtils.containsOnly(null, emptyChars));
        assertEquals(false, StringUtils.containsOnly(str1, emptyChars));
        assertEquals(true, StringUtils.containsOnly("", emptyChars));
        assertEquals(true, StringUtils.containsOnly("", chars1));
        assertEquals(false, StringUtils.containsOnly(str1, chars1));
        assertEquals(true, StringUtils.containsOnly(str1, chars2));
        assertEquals(true, StringUtils.containsOnly(str1, chars3));
        assertEquals(true, StringUtils.containsOnly(str2, chars1));
        assertEquals(false, StringUtils.containsOnly(str2, chars2));
        assertEquals(true, StringUtils.containsOnly(str2, chars3));
        assertEquals(false, StringUtils.containsOnly(str3, chars1));
        assertEquals(false, StringUtils.containsOnly(str3, chars2));
        assertEquals(true, StringUtils.containsOnly(str3, chars3));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsNone_String
    public void testContainsNone_String() {
        String str1 = "a";
        String str2 = "b";
        String str3 = "ab.";
        String chars1= "b";
        String chars2= ".";
        String chars3= "cd";
        assertEquals(true, StringUtils.containsNone(null, (String) null));
        assertEquals(true, StringUtils.containsNone("", (String) null));
        assertEquals(true, StringUtils.containsNone(null, ""));
        assertEquals(true, StringUtils.containsNone(str1, ""));
        assertEquals(true, StringUtils.containsNone("", ""));
        assertEquals(true, StringUtils.containsNone("", chars1));
        assertEquals(true, StringUtils.containsNone(str1, chars1));
        assertEquals(true, StringUtils.containsNone(str1, chars2));
        assertEquals(true, StringUtils.containsNone(str1, chars3));
        assertEquals(false, StringUtils.containsNone(str2, chars1));
        assertEquals(true, StringUtils.containsNone(str2, chars2));
        assertEquals(true, StringUtils.containsNone(str2, chars3));
        assertEquals(false, StringUtils.containsNone(str3, chars1));
        assertEquals(false, StringUtils.containsNone(str3, chars2));
        assertEquals(true, StringUtils.containsNone(str3, chars3));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsNone_Chararray
    public void testContainsNone_Chararray() {
        String str1 = "a";
        String str2 = "b";
        String str3 = "ab.";
        char[] chars1= {'b'};
        char[] chars2= {'.'};
        char[] chars3= {'c', 'd'};
        char[] emptyChars = new char[0];
        assertEquals(true, StringUtils.containsNone(null, (char[]) null));
        assertEquals(true, StringUtils.containsNone("", (char[]) null));
        assertEquals(true, StringUtils.containsNone(null, emptyChars));
        assertEquals(true, StringUtils.containsNone(str1, emptyChars));
        assertEquals(true, StringUtils.containsNone("", emptyChars));
        assertEquals(true, StringUtils.containsNone("", chars1));
        assertEquals(true, StringUtils.containsNone(str1, chars1));
        assertEquals(true, StringUtils.containsNone(str1, chars2));
        assertEquals(true, StringUtils.containsNone(str1, chars3));
        assertEquals(false, StringUtils.containsNone(str2, chars1));
        assertEquals(true, StringUtils.containsNone(str2, chars2));
        assertEquals(true, StringUtils.containsNone(str2, chars3));
        assertEquals(false, StringUtils.containsNone(str3, chars1));
        assertEquals(false, StringUtils.containsNone(str3, chars2));
        assertEquals(true, StringUtils.containsNone(str3, chars3));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testSubstring_StringInt
    public void testSubstring_StringInt() {
        assertEquals(null, StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("", StringUtils.substring("", 2));
        
        assertEquals("", StringUtils.substring(SENTENCE, 80));
        assertEquals(BAZ, StringUtils.substring(SENTENCE, 8));
        assertEquals(BAZ, StringUtils.substring(SENTENCE, -3));
        assertEquals(SENTENCE, StringUtils.substring(SENTENCE, 0));
        assertEquals("abc", StringUtils.substring("abc", -4));
        assertEquals("abc", StringUtils.substring("abc", -3));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("c", StringUtils.substring("abc", -1));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("", StringUtils.substring("abc", 3));
        assertEquals("", StringUtils.substring("abc", 4));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testSubstring_StringIntInt
    public void testSubstring_StringIntInt() {
        assertEquals(null, StringUtils.substring(null, 0, 0));
        assertEquals(null, StringUtils.substring(null, 1, 2));
        assertEquals("", StringUtils.substring("", 0, 0));
        assertEquals("", StringUtils.substring("", 1, 2));
        assertEquals("", StringUtils.substring("", -2, -1));
        
        assertEquals("", StringUtils.substring(SENTENCE, 8, 6));
        assertEquals(FOO, StringUtils.substring(SENTENCE, 0, 3));
        assertEquals("o", StringUtils.substring(SENTENCE, -9, 3));
        assertEquals(FOO, StringUtils.substring(SENTENCE, 0, -8));
        assertEquals("o", StringUtils.substring(SENTENCE, -9, -8));
        assertEquals(SENTENCE, StringUtils.substring(SENTENCE, 0, 80));
        assertEquals("", StringUtils.substring(SENTENCE, 2, 2));
        assertEquals("b",StringUtils.substring("abc", -2, -1));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testLeft_String
    public void testLeft_String() {
        assertSame(null, StringUtils.left(null, -1));
        assertSame(null, StringUtils.left(null, 0));
        assertSame(null, StringUtils.left(null, 2));
        
        assertEquals("", StringUtils.left("", -1));
        assertEquals("", StringUtils.left("", 0));
        assertEquals("", StringUtils.left("", 2));
        
        assertEquals("", StringUtils.left(FOOBAR, -1));
        assertEquals("", StringUtils.left(FOOBAR, 0));
        assertEquals(FOO, StringUtils.left(FOOBAR, 3));
        assertSame(FOOBAR, StringUtils.left(FOOBAR, 80));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testRight_String
    public void testRight_String() {
        assertSame(null, StringUtils.right(null, -1));
        assertSame(null, StringUtils.right(null, 0));
        assertSame(null, StringUtils.right(null, 2));
        
        assertEquals("", StringUtils.right("", -1));
        assertEquals("", StringUtils.right("", 0));
        assertEquals("", StringUtils.right("", 2));
        
        assertEquals("", StringUtils.right(FOOBAR, -1));
        assertEquals("", StringUtils.right(FOOBAR, 0));
        assertEquals(BAR, StringUtils.right(FOOBAR, 3));
        assertSame(FOOBAR, StringUtils.right(FOOBAR, 80));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testMid_String
    public void testMid_String() {
        assertSame(null, StringUtils.mid(null, -1, 0));
        assertSame(null, StringUtils.mid(null, 0, -1));
        assertSame(null, StringUtils.mid(null, 3, 0));
        assertSame(null, StringUtils.mid(null, 3, 2));
        
        assertEquals("", StringUtils.mid("", 0, -1));
        assertEquals("", StringUtils.mid("", 0, 0));
        assertEquals("", StringUtils.mid("", 0, 2));
        
        assertEquals("", StringUtils.mid(FOOBAR, 3, -1));
        assertEquals("", StringUtils.mid(FOOBAR, 3, 0));
        assertEquals("b", StringUtils.mid(FOOBAR, 3, 1));
        assertEquals(FOO, StringUtils.mid(FOOBAR, 0, 3));
        assertEquals(BAR, StringUtils.mid(FOOBAR, 3, 3));
        assertEquals(FOOBAR, StringUtils.mid(FOOBAR, 0, 80));
        assertEquals(BAR, StringUtils.mid(FOOBAR, 3, 80));
        assertEquals("", StringUtils.mid(FOOBAR, 9, 3));
        assertEquals(FOO, StringUtils.mid(FOOBAR, -1, 3));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testSubstringBefore_StringString
    public void testSubstringBefore_StringString() {
        assertEquals("foo", StringUtils.substringBefore("fooXXbarXXbaz", "XX"));

        assertEquals(null, StringUtils.substringBefore(null, null));
        assertEquals(null, StringUtils.substringBefore(null, ""));
        assertEquals(null, StringUtils.substringBefore(null, "XX"));
        assertEquals("", StringUtils.substringBefore("", null));
        assertEquals("", StringUtils.substringBefore("", ""));
        assertEquals("", StringUtils.substringBefore("", "XX"));
        
        assertEquals("foo", StringUtils.substringBefore("foo", null));
        assertEquals("foo", StringUtils.substringBefore("foo", "b"));
        assertEquals("f", StringUtils.substringBefore("foot", "o"));
        assertEquals("", StringUtils.substringBefore("abc", "a"));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("ab", StringUtils.substringBefore("abc", "c"));
        assertEquals("", StringUtils.substringBefore("abc", ""));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testSubstringAfter_StringString
    public void testSubstringAfter_StringString() {
        assertEquals("barXXbaz", StringUtils.substringAfter("fooXXbarXXbaz", "XX"));
        
        assertEquals(null, StringUtils.substringAfter(null, null));
        assertEquals(null, StringUtils.substringAfter(null, ""));
        assertEquals(null, StringUtils.substringAfter(null, "XX"));
        assertEquals("", StringUtils.substringAfter("", null));
        assertEquals("", StringUtils.substringAfter("", ""));
        assertEquals("", StringUtils.substringAfter("", "XX"));
        
        assertEquals("", StringUtils.substringAfter("foo", null));
        assertEquals("ot", StringUtils.substringAfter("foot", "o"));
        assertEquals("bc", StringUtils.substringAfter("abc", "a"));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("", StringUtils.substringAfter("abc", "c"));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));
        assertEquals("", StringUtils.substringAfter("abc", "d"));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testSubstringBeforeLast_StringString
    public void testSubstringBeforeLast_StringString() {
        assertEquals("fooXXbar", StringUtils.substringBeforeLast("fooXXbarXXbaz", "XX"));

        assertEquals(null, StringUtils.substringBeforeLast(null, null));
        assertEquals(null, StringUtils.substringBeforeLast(null, ""));
        assertEquals(null, StringUtils.substringBeforeLast(null, "XX"));
        assertEquals("", StringUtils.substringBeforeLast("", null));
        assertEquals("", StringUtils.substringBeforeLast("", ""));
        assertEquals("", StringUtils.substringBeforeLast("", "XX"));

        assertEquals("foo", StringUtils.substringBeforeLast("foo", null));
        assertEquals("foo", StringUtils.substringBeforeLast("foo", "b"));
        assertEquals("fo", StringUtils.substringBeforeLast("foo", "o"));
        assertEquals("abc\r\n", StringUtils.substringBeforeLast("abc\r\n", "d"));
        assertEquals("abc", StringUtils.substringBeforeLast("abcdabc", "d"));
        assertEquals("abcdabc", StringUtils.substringBeforeLast("abcdabcd", "d"));
        assertEquals("a", StringUtils.substringBeforeLast("abc", "b"));
        assertEquals("abc ", StringUtils.substringBeforeLast("abc \n", "\n"));
        assertEquals("a", StringUtils.substringBeforeLast("a", null));
        assertEquals("a", StringUtils.substringBeforeLast("a", ""));
        assertEquals("", StringUtils.substringBeforeLast("a", "a"));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testSubstringAfterLast_StringString
    public void testSubstringAfterLast_StringString() {
        assertEquals("baz", StringUtils.substringAfterLast("fooXXbarXXbaz", "XX"));

        assertEquals(null, StringUtils.substringAfterLast(null, null));
        assertEquals(null, StringUtils.substringAfterLast(null, ""));
        assertEquals(null, StringUtils.substringAfterLast(null, "XX"));
        assertEquals("", StringUtils.substringAfterLast("", null));
        assertEquals("", StringUtils.substringAfterLast("", ""));
        assertEquals("", StringUtils.substringAfterLast("", "a"));

        assertEquals("", StringUtils.substringAfterLast("foo", null));
        assertEquals("", StringUtils.substringAfterLast("foo", "b"));
        assertEquals("t", StringUtils.substringAfterLast("foot", "o"));
        assertEquals("bc", StringUtils.substringAfterLast("abc", "a"));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        assertEquals("", StringUtils.substringAfterLast("abc", "c"));
        assertEquals("", StringUtils.substringAfterLast("", "d"));
        assertEquals("", StringUtils.substringAfterLast("abc", ""));
    }

// org.apache.commons.lang3.StringUtilsSubstringTest::testSubstringBetween_StringString
    public void testSubstringBetween_StringString() {
        assertEquals(null, StringUtils.substringBetween(null, "tag"));
        assertEquals("", StringUtils.substringBetween("", ""));
        assertEquals(null, StringUtils.substringBetween("", "abc"));
        assertEquals("", StringUtils.substringBetween("    ", " "));
        assertEquals(null, StringUtils.substringBetween("abc", null));
        assertEquals("", StringUtils.substringBetween("abc", ""));
        assertEquals(null, StringUtils.substringBetween("abc", "a"));
        assertEquals("bc", StringUtils.substringBetween("abca", "a"));
        assertEquals("bc", StringUtils.substringBetween("abcabca", "a"));
        assertEquals("bar", StringUtils.substringBetween("\nbar\n", "\n"));
    }

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

// org.apache.commons.lang3.StringUtilsTest::testLength
    public void testLength() {
        assertEquals(0, StringUtils.length(null));
        assertEquals(0, StringUtils.length(""));
        assertEquals(0, StringUtils.length(StringUtils.EMPTY));
        assertEquals(1, StringUtils.length("A"));
        assertEquals(1, StringUtils.length(" "));
        assertEquals(8, StringUtils.length("ABCDEFGH"));
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

// org.apache.commons.lang3.builder.CompareToBuilderTest::testReflectionCompare
    public void testReflectionCompare() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(4);
        assertTrue(CompareToBuilder.reflectionCompare(o1, o1) == 0);
        assertTrue(CompareToBuilder.reflectionCompare(o1, o2) == 0);
        o2.setA(5);
        assertTrue(CompareToBuilder.reflectionCompare(o1, o2) < 0);
        assertTrue(CompareToBuilder.reflectionCompare(o2, o1) > 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testReflectionCompareEx1
    public void testReflectionCompareEx1() {
        TestObject o1 = new TestObject(4);
        try {
            CompareToBuilder.reflectionCompare(o1, null);
        } catch (NullPointerException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testReflectionCompareEx2
    public void testReflectionCompareEx2() {
        TestObject o1 = new TestObject(4);
        Object o2 = new Object();
        try {
            CompareToBuilder.reflectionCompare(o1, o2);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testReflectionHierarchyCompare
    public void testReflectionHierarchyCompare() {
        testReflectionHierarchyCompare(false, null);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testReflectionHierarchyCompareExcludeFields
    public void testReflectionHierarchyCompareExcludeFields() {
        String[] excludeFields = new String[] { "b" };
        testReflectionHierarchyCompare(true, excludeFields);
        
        TestSubObject x;
        TestSubObject y;
        TestSubObject z;
        
        x = new TestSubObject(1, 1);
        y = new TestSubObject(2, 1);
        z = new TestSubObject(3, 1);
        assertXYZCompareOrder(x, y, z, true, excludeFields);

        x = new TestSubObject(1, 3);
        y = new TestSubObject(2, 2);
        z = new TestSubObject(3, 1);
        assertXYZCompareOrder(x, y, z, true, excludeFields);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testReflectionHierarchyCompareTransients
    public void testReflectionHierarchyCompareTransients() {
        testReflectionHierarchyCompare(true, null);

        TestTransientSubObject x;
        TestTransientSubObject y;
        TestTransientSubObject z;

        x = new TestTransientSubObject(1, 1);
        y = new TestTransientSubObject(2, 2);
        z = new TestTransientSubObject(3, 3);
        assertXYZCompareOrder(x, y, z, true, null);
        
        x = new TestTransientSubObject(1, 1);
        y = new TestTransientSubObject(1, 2);
        z = new TestTransientSubObject(1, 3);
        assertXYZCompareOrder(x, y, z, true, null);  
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testReflectionHierarchyCompare
    public void testReflectionHierarchyCompare(boolean testTransients, String[] excludeFields) {
        TestObject to1 = new TestObject(1);
        TestObject to2 = new TestObject(2);
        TestObject to3 = new TestObject(3);
        TestSubObject tso1 = new TestSubObject(1, 1);
        TestSubObject tso2 = new TestSubObject(2, 2);
        TestSubObject tso3 = new TestSubObject(3, 3);
        
        assertReflectionCompareContract(to1, to1, to1, false, excludeFields);
        assertReflectionCompareContract(to1, to2, to3, false, excludeFields);
        assertReflectionCompareContract(tso1, tso1, tso1, false, excludeFields);
        assertReflectionCompareContract(tso1, tso2, tso3, false, excludeFields);
        assertReflectionCompareContract("1", "2", "3", false, excludeFields);
        
        assertTrue(0 != CompareToBuilder.reflectionCompare(tso1, new TestSubObject(1, 0), testTransients));
        assertTrue(0 != CompareToBuilder.reflectionCompare(tso1, new TestSubObject(0, 1), testTransients));

        
        assertXYZCompareOrder(to1, to2, to3, true, null);
        
        assertXYZCompareOrder(tso1, tso2, tso3, true, null);  
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testAppendSuper
    public void testAppendSuper() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(5);
        assertTrue(new CompareToBuilder().appendSuper(0).append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().appendSuper(0).append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().appendSuper(0).append(o2, o1).toComparison() > 0);
        
        assertTrue(new CompareToBuilder().appendSuper(-1).append(o1, o1).toComparison() < 0);
        assertTrue(new CompareToBuilder().appendSuper(-1).append(o1, o2).toComparison() < 0);
        
        assertTrue(new CompareToBuilder().appendSuper(1).append(o1, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().appendSuper(1).append(o1, o2).toComparison() > 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testObject
    public void testObject() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(4);
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() == 0);
        o2.setA(5);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        
        assertTrue(new CompareToBuilder().append(o1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((Object) null, (Object) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, o1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testObjectEx2
    public void testObjectEx2() {
        TestObject o1 = new TestObject(4);
        Object o2 = new Object();
        try {
            new CompareToBuilder().append(o1, o2);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testObjectComparator
    public void testObjectComparator() {
        String o1 = "Fred";
        String o2 = "Fred";
        assertTrue(new CompareToBuilder().append(o1, o1, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        o2 = "FRED";
        assertTrue(new CompareToBuilder().append(o1, o2, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o2, o1, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        o2 = "FREDA";
        assertTrue(new CompareToBuilder().append(o1, o2, String.CASE_INSENSITIVE_ORDER).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1, String.CASE_INSENSITIVE_ORDER).toComparison() > 0);
        
        assertTrue(new CompareToBuilder().append(o1, null, String.CASE_INSENSITIVE_ORDER).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((Object) null, (Object) null, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, o1, String.CASE_INSENSITIVE_ORDER).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testObjectComparatorNull
    public void testObjectComparatorNull() {
        String o1 = "Fred";
        String o2 = "Fred";
        assertTrue(new CompareToBuilder().append(o1, o1, null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2, null).toComparison() == 0);
        o2 = "Zebra";
        assertTrue(new CompareToBuilder().append(o1, o2, null).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1, null).toComparison() > 0);
        
        assertTrue(new CompareToBuilder().append(o1, null, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((Object) null, (Object) null, null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, o1, null).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testLong
    public void testLong() {
        long o1 = 1L;
        long o2 = 2L;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Long.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Long.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Long.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Long.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testInt
    public void testInt() {
        int o1 = 1;
        int o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Integer.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Integer.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Integer.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Integer.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testShort
    public void testShort() {
        short o1 = 1;
        short o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Short.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Short.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Short.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Short.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testChar
    public void testChar() {
        char o1 = 1;
        char o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Character.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Character.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Character.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Character.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testByte
    public void testByte() {
        byte o1 = 1;
        byte o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Byte.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Byte.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Byte.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Byte.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testDouble
    public void testDouble() {
        double o1 = 1;
        double o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Double.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Double.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Double.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Double.MIN_VALUE, o1).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Double.NaN, Double.NaN).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(Double.NaN, Double.MAX_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Double.POSITIVE_INFINITY, Double.MAX_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Double.NEGATIVE_INFINITY, Double.MIN_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o1, Double.NaN).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Double.NaN, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(-0.0, 0.0).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(0.0, -0.0).toComparison() > 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testFloat
    public void testFloat() {
        float o1 = 1;
        float o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Float.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Float.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Float.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Float.MIN_VALUE, o1).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Float.NaN, Float.NaN).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(Float.NaN, Float.MAX_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Float.POSITIVE_INFINITY, Float.MAX_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Float.NEGATIVE_INFINITY, Float.MIN_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o1, Float.NaN).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Float.NaN, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(-0.0, 0.0).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(0.0, -0.0).toComparison() > 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testBoolean
    public void testBoolean() {
        boolean o1 = true;
        boolean o2 = false;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o2, o2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testObjectArray
    public void testObjectArray() {
        TestObject[] obj1 = new TestObject[2];
        obj1[0] = new TestObject(4);
        obj1[1] = new TestObject(5);
        TestObject[] obj2 = new TestObject[2];
        obj2[0] = new TestObject(4);
        obj2[1] = new TestObject(5);
        TestObject[] obj3 = new TestObject[3];
        obj3[0] = new TestObject(4);
        obj3[1] = new TestObject(5);
        obj3[2] = new TestObject(6);
        
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);
        
        obj1[1] = new TestObject(7);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((Object[]) null, (Object[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testLongArray
    public void testLongArray() {
        long[] obj1 = new long[2];
        obj1[0] = 5L;
        obj1[1] = 6L;
        long[] obj2 = new long[2];
        obj2[0] = 5L;
        obj2[1] = 6L;
        long[] obj3 = new long[3];
        obj3[0] = 5L;
        obj3[1] = 6L;
        obj3[2] = 7L;
        
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((long[]) null, (long[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testIntArray
    public void testIntArray() {
        int[] obj1 = new int[2];
        obj1[0] = 5;
        obj1[1] = 6;
        int[] obj2 = new int[2];
        obj2[0] = 5;
        obj2[1] = 6;
        int[] obj3 = new int[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((int[]) null, (int[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testShortArray
    public void testShortArray() {
        short[] obj1 = new short[2];
        obj1[0] = 5;
        obj1[1] = 6;
        short[] obj2 = new short[2];
        obj2[0] = 5;
        obj2[1] = 6;
        short[] obj3 = new short[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((short[]) null, (short[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testCharArray
    public void testCharArray() {
        char[] obj1 = new char[2];
        obj1[0] = 5;
        obj1[1] = 6;
        char[] obj2 = new char[2];
        obj2[0] = 5;
        obj2[1] = 6;
        char[] obj3 = new char[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((char[]) null, (char[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testByteArray
    public void testByteArray() {
        byte[] obj1 = new byte[2];
        obj1[0] = 5;
        obj1[1] = 6;
        byte[] obj2 = new byte[2];
        obj2[0] = 5;
        obj2[1] = 6;
        byte[] obj3 = new byte[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((byte[]) null, (byte[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testDoubleArray
    public void testDoubleArray() {
        double[] obj1 = new double[2];
        obj1[0] = 5;
        obj1[1] = 6;
        double[] obj2 = new double[2];
        obj2[0] = 5;
        obj2[1] = 6;
        double[] obj3 = new double[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((double[]) null, (double[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testFloatArray
    public void testFloatArray() {
        float[] obj1 = new float[2];
        obj1[0] = 5;
        obj1[1] = 6;
        float[] obj2 = new float[2];
        obj2[0] = 5;
        obj2[1] = 6;
        float[] obj3 = new float[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((float[]) null, (float[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testBooleanArray
    public void testBooleanArray() {
        boolean[] obj1 = new boolean[2];
        obj1[0] = true;
        obj1[1] = false;
        boolean[] obj2 = new boolean[2];
        obj2[0] = true;
        obj2[1] = false;
        boolean[] obj3 = new boolean[3];
        obj3[0] = true;
        obj3[1] = false;
        obj3[2] = true;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = true;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((boolean[]) null, (boolean[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testMultiLongArray
    public void testMultiLongArray() {
        long[][] array1 = new long[2][2];
        long[][] array2 = new long[2][2];
        long[][] array3 = new long[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
                array3[i][j] = (i + 1) * (j + 1);
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testMultiIntArray
    public void testMultiIntArray() {
        int[][] array1 = new int[2][2];
        int[][] array2 = new int[2][2];
        int[][] array3 = new int[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
                array3[i][j] = (i + 1) * (j + 1);
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testMultiShortArray
    public void testMultiShortArray() {
        short[][] array1 = new short[2][2];
        short[][] array2 = new short[2][2];
        short[][] array3 = new short[2][3];
        for (short i = 0; i < array1.length; ++i) {
            for (short j = 0; j < array1[0].length; j++) {
                array1[i][j] = (short)((i + 1) * (j + 1));
                array2[i][j] = (short)((i + 1) * (j + 1));
                array3[i][j] = (short)((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testMultiCharArray
    public void testMultiCharArray() {
        char[][] array1 = new char[2][2];
        char[][] array2 = new char[2][2];
        char[][] array3 = new char[2][3];
        for (short i = 0; i < array1.length; ++i) {
            for (short j = 0; j < array1[0].length; j++) {
                array1[i][j] = (char)((i + 1) * (j + 1));
                array2[i][j] = (char)((i + 1) * (j + 1));
                array3[i][j] = (char)((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testMultiByteArray
    public void testMultiByteArray() {
        byte[][] array1 = new byte[2][2];
        byte[][] array2 = new byte[2][2];
        byte[][] array3 = new byte[2][3];
        for (byte i = 0; i < array1.length; ++i) {
            for (byte j = 0; j < array1[0].length; j++) {
                array1[i][j] = (byte)((i + 1) * (j + 1));
                array2[i][j] = (byte)((i + 1) * (j + 1));
                array3[i][j] = (byte)((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 127;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testMultiFloatArray
    public void testMultiFloatArray() {
        float[][] array1 = new float[2][2];
        float[][] array2 = new float[2][2];
        float[][] array3 = new float[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = ((i + 1) * (j + 1));
                array2[i][j] = ((i + 1) * (j + 1));
                array3[i][j] = ((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 127;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testMultiDoubleArray
    public void testMultiDoubleArray() {
        double[][] array1 = new double[2][2];
        double[][] array2 = new double[2][2];
        double[][] array3 = new double[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = ((i + 1) * (j + 1));
                array2[i][j] = ((i + 1) * (j + 1));
                array3[i][j] = ((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 127;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testMultiBooleanArray
    public void testMultiBooleanArray() {
        boolean[][] array1 = new boolean[2][2];
        boolean[][] array2 = new boolean[2][2];
        boolean[][] array3 = new boolean[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = ((i == 1) ^ (j == 1));
                array2[i][j] = ((i == 1) ^ (j == 1));
                array3[i][j] = ((i == 1) ^ (j == 1));
            }
        }
        array3[1][2] = false;
        array3[1][2] = false;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = true;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testRaggedArray
    public void testRaggedArray() {
        long array1[][] = new long[2][];
        long array2[][] = new long[2][];
        long array3[][] = new long[3][];
        for (int i = 0; i < array1.length; ++i) {
            array1[i] = new long[2];
            array2[i] = new long[2];
            array3[i] = new long[3];
            for (int j = 0; j < array1[i].length; ++j) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
                array3[i][j] = (i + 1) * (j + 1);
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testMixedArray
    public void testMixedArray() {
        Object array1[] = new Object[2];
        Object array2[] = new Object[2];
        Object array3[] = new Object[2];
        for (int i = 0; i < array1.length; ++i) {
            array1[i] = new long[2];
            array2[i] = new long[2];
            array3[i] = new long[3];
            for (int j = 0; j < 2; ++j) {
                ((long[]) array1[i])[j] = (i + 1) * (j + 1);
                ((long[]) array2[i])[j] = (i + 1) * (j + 1);
                ((long[]) array3[i])[j] = (i + 1) * (j + 1);
            }
        }
        ((long[]) array3[0])[2] = 1;
        ((long[]) array3[1])[2] = 1;
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        ((long[]) array1[1])[1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testObjectArrayHiddenByObject
    public void testObjectArrayHiddenByObject() {
        TestObject[] array1 = new TestObject[2];
        array1[0] = new TestObject(4);
        array1[1] = new TestObject(5);
        TestObject[] array2 = new TestObject[2];
        array2[0] = new TestObject(4);
        array2[1] = new TestObject(5);
        TestObject[] array3 = new TestObject[3];
        array3[0] = new TestObject(4);
        array3[1] = new TestObject(5);
        array3[2] = new TestObject(6);
        
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = new TestObject(7);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testLongArrayHiddenByObject
    public void testLongArrayHiddenByObject() {
        long[] array1 = new long[2];
        array1[0] = 5L;
        array1[1] = 6L;
        long[] array2 = new long[2];
        array2[0] = 5L;
        array2[1] = 6L;
        long[] array3 = new long[3];
        array3[0] = 5L;
        array3[1] = 6L;
        array3[2] = 7L;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testIntArrayHiddenByObject
    public void testIntArrayHiddenByObject() {
        int[] array1 = new int[2];
        array1[0] = 5;
        array1[1] = 6;
        int[] array2 = new int[2];
        array2[0] = 5;
        array2[1] = 6;
        int[] array3 = new int[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testShortArrayHiddenByObject
    public void testShortArrayHiddenByObject() {
        short[] array1 = new short[2];
        array1[0] = 5;
        array1[1] = 6;
        short[] array2 = new short[2];
        array2[0] = 5;
        array2[1] = 6;
        short[] array3 = new short[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testCharArrayHiddenByObject
    public void testCharArrayHiddenByObject() {
        char[] array1 = new char[2];
        array1[0] = 5;
        array1[1] = 6;
        char[] array2 = new char[2];
        array2[0] = 5;
        array2[1] = 6;
        char[] array3 = new char[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testByteArrayHiddenByObject
    public void testByteArrayHiddenByObject() {
        byte[] array1 = new byte[2];
        array1[0] = 5;
        array1[1] = 6;
        byte[] array2 = new byte[2];
        array2[0] = 5;
        array2[1] = 6;
        byte[] array3 = new byte[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testDoubleArrayHiddenByObject
    public void testDoubleArrayHiddenByObject() {
        double[] array1 = new double[2];
        array1[0] = 5;
        array1[1] = 6;
        double[] array2 = new double[2];
        array2[0] = 5;
        array2[1] = 6;
        double[] array3 = new double[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testFloatArrayHiddenByObject
    public void testFloatArrayHiddenByObject() {
        float[] array1 = new float[2];
        array1[0] = 5;
        array1[1] = 6;
        float[] array2 = new float[2];
        array2[0] = 5;
        array2[1] = 6;
        float[] array3 = new float[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.CompareToBuilderTest::testBooleanArrayHiddenByObject
    public void testBooleanArrayHiddenByObject() {
        boolean[] array1 = new boolean[2];
        array1[0] = true;
        array1[1] = false;
        boolean[] array2 = new boolean[2];
        array2[0] = true;
        array2[1] = false;
        boolean[] array3 = new boolean[3];
        array3[0] = true;
        array3[1] = false;
        array3[2] = true;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = true;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testReflectionEquals
    public void testReflectionEquals() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(5);
        assertTrue(EqualsBuilder.reflectionEquals(o1, o1));
        assertTrue(!EqualsBuilder.reflectionEquals(o1, o2));
        o2.setA(4);
        assertTrue(EqualsBuilder.reflectionEquals(o1, o2));

        assertTrue(!EqualsBuilder.reflectionEquals(o1, this));

        assertTrue(!EqualsBuilder.reflectionEquals(o1, null));
        assertTrue(!EqualsBuilder.reflectionEquals(null, o2));
        assertTrue(EqualsBuilder.reflectionEquals((Object) null, (Object) null));
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testReflectionHierarchyEquals
    public void testReflectionHierarchyEquals() {
        testReflectionHierarchyEquals(false);
        testReflectionHierarchyEquals(true);
        
        assertTrue(EqualsBuilder.reflectionEquals(new TestTTLeafObject(1, 2, 3, 4), new TestTTLeafObject(1, 2, 3, 4), true));
        assertTrue(EqualsBuilder.reflectionEquals(new TestTTLeafObject(1, 2, 3, 4), new TestTTLeafObject(1, 2, 3, 4), false));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestTTLeafObject(1, 0, 0, 4), new TestTTLeafObject(1, 2, 3, 4), true));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestTTLeafObject(1, 2, 3, 4), new TestTTLeafObject(1, 2, 3, 0), true));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestTTLeafObject(0, 2, 3, 4), new TestTTLeafObject(1, 2, 3, 4), true));
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testReflectionHierarchyEquals
    public void testReflectionHierarchyEquals(boolean testTransients) {
        TestObject to1 = new TestObject(4);
        TestObject to1Bis = new TestObject(4);
        TestObject to1Ter = new TestObject(4);
        TestObject to2 = new TestObject(5);
        TestEmptySubObject teso = new TestEmptySubObject(4);
        TestTSubObject ttso = new TestTSubObject(4, 1);
        TestTTSubObject tttso = new TestTTSubObject(4, 1, 2);
        TestTTLeafObject ttlo = new TestTTLeafObject(4, 1, 2, 3);
        TestSubObject tso1 = new TestSubObject(1, 4);
        TestSubObject tso1bis = new TestSubObject(1, 4);
        TestSubObject tso1ter = new TestSubObject(1, 4);
        TestSubObject tso2 = new TestSubObject(2, 5);

        testReflectionEqualsEquivalenceRelationship(to1, to1Bis, to1Ter, to2, new TestObject(), testTransients);
        testReflectionEqualsEquivalenceRelationship(tso1, tso1bis, tso1ter, tso2, new TestSubObject(), testTransients);

        

        
        assertTrue(EqualsBuilder.reflectionEquals(ttlo, ttlo, testTransients));
        assertTrue(EqualsBuilder.reflectionEquals(new TestSubObject(1, 10), new TestSubObject(1, 10), testTransients));
        
        assertTrue(!EqualsBuilder.reflectionEquals(new TestSubObject(1, 10), new TestSubObject(1, 11), testTransients));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestSubObject(1, 11), new TestSubObject(1, 10), testTransients));
        
        assertTrue(!EqualsBuilder.reflectionEquals(new TestSubObject(0, 10), new TestSubObject(1, 10), testTransients));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestSubObject(1, 10), new TestSubObject(0, 10), testTransients));

        
        assertTrue(EqualsBuilder.reflectionEquals(to1, teso, testTransients));
        assertTrue(EqualsBuilder.reflectionEquals(teso, to1, testTransients));

        assertTrue(EqualsBuilder.reflectionEquals(to1, ttso, false)); 
        assertTrue(EqualsBuilder.reflectionEquals(ttso, to1, false)); 

        assertTrue(EqualsBuilder.reflectionEquals(to1, tttso, false)); 
        assertTrue(EqualsBuilder.reflectionEquals(tttso, to1, false)); 

        assertTrue(EqualsBuilder.reflectionEquals(ttso, tttso, false)); 
        assertTrue(EqualsBuilder.reflectionEquals(tttso, ttso, false)); 

        
        assertTrue(!EqualsBuilder.reflectionEquals(new TestObject(0), new TestEmptySubObject(1), testTransients));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestEmptySubObject(1), new TestObject(0), testTransients));

        assertTrue(!EqualsBuilder.reflectionEquals(new TestObject(0), new TestTSubObject(1, 1), testTransients));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestTSubObject(1, 1), new TestObject(0), testTransients));

        assertTrue(!EqualsBuilder.reflectionEquals(new TestObject(1), new TestSubObject(0, 10), testTransients));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestSubObject(0, 10), new TestObject(1), testTransients));

        assertTrue(!EqualsBuilder.reflectionEquals(to1, ttlo));
        assertTrue(!EqualsBuilder.reflectionEquals(tso1, this));
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testReflectionEqualsEquivalenceRelationship
    public void testReflectionEqualsEquivalenceRelationship(
        TestObject to,
        TestObject toBis,
        TestObject toTer,
        TestObject to2,
        TestObject oToChange,
        boolean testTransients) {

        
        assertTrue(EqualsBuilder.reflectionEquals(to, to, testTransients));
        assertTrue(EqualsBuilder.reflectionEquals(to2, to2, testTransients));

        
        assertTrue(EqualsBuilder.reflectionEquals(to, toBis, testTransients) && EqualsBuilder.reflectionEquals(toBis, to, testTransients));

        
        assertTrue(
            EqualsBuilder.reflectionEquals(to, toBis, testTransients)
                && EqualsBuilder.reflectionEquals(toBis, toTer, testTransients)
                && EqualsBuilder.reflectionEquals(to, toTer, testTransients));

        
        oToChange.setA(to.getA());
        if (oToChange instanceof TestSubObject) {
            ((TestSubObject) oToChange).setB(((TestSubObject) to).getB());
        }
        assertTrue(EqualsBuilder.reflectionEquals(oToChange, to, testTransients));
        assertTrue(EqualsBuilder.reflectionEquals(oToChange, to, testTransients));
        oToChange.setA(to.getA() + 1);
        if (oToChange instanceof TestSubObject) {
            ((TestSubObject) oToChange).setB(((TestSubObject) to).getB() + 1);
        }
        assertTrue(!EqualsBuilder.reflectionEquals(oToChange, to, testTransients));
        assertTrue(!EqualsBuilder.reflectionEquals(oToChange, to, testTransients));

        
        assertTrue(!EqualsBuilder.reflectionEquals(to, null, testTransients));
        assertTrue(!EqualsBuilder.reflectionEquals(to2, null, testTransients));
        assertTrue(!EqualsBuilder.reflectionEquals(null, to, testTransients));
        assertTrue(!EqualsBuilder.reflectionEquals(null, to2, testTransients));
        assertTrue(EqualsBuilder.reflectionEquals((Object) null, (Object) null, testTransients));
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testSuper
    public void testSuper() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(5);
        assertEquals(true, new EqualsBuilder().appendSuper(true).append(o1, o1).isEquals());
        assertEquals(false, new EqualsBuilder().appendSuper(false).append(o1, o1).isEquals());
        assertEquals(false, new EqualsBuilder().appendSuper(true).append(o1, o2).isEquals());
        assertEquals(false, new EqualsBuilder().appendSuper(false).append(o1, o2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testObject
    public void testObject() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(5);
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
        o2.setA(4);
        assertTrue(new EqualsBuilder().append(o1, o2).isEquals());

        assertTrue(!new EqualsBuilder().append(o1, this).isEquals());
        
        assertTrue(!new EqualsBuilder().append(o1, null).isEquals());
        assertTrue(!new EqualsBuilder().append(null, o2).isEquals());
        assertTrue(new EqualsBuilder().append((Object) null, (Object) null).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testLong
    public void testLong() {
        long o1 = 1L;
        long o2 = 2L;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testInt
    public void testInt() {
        int o1 = 1;
        int o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testShort
    public void testShort() {
        short o1 = 1;
        short o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testChar
    public void testChar() {
        char o1 = 1;
        char o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testByte
    public void testByte() {
        byte o1 = 1;
        byte o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testDouble
    public void testDouble() {
        double o1 = 1;
        double o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, Double.NaN).isEquals());
        assertTrue(new EqualsBuilder().append(Double.NaN, Double.NaN).isEquals());
        assertTrue(new EqualsBuilder().append(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testFloat
    public void testFloat() {
        float o1 = 1;
        float o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, Float.NaN).isEquals());
        assertTrue(new EqualsBuilder().append(Float.NaN, Float.NaN).isEquals());
        assertTrue(new EqualsBuilder().append(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testAccessors
    public void testAccessors() {
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        assertTrue(equalsBuilder.isEquals());
        equalsBuilder.setEquals(true);
        assertTrue(equalsBuilder.isEquals());
        equalsBuilder.setEquals(false);
        assertFalse(equalsBuilder.isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testReset
    public void testReset() {
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        assertTrue(equalsBuilder.isEquals());
        equalsBuilder.setEquals(false);
        assertFalse(equalsBuilder.isEquals());
        equalsBuilder.reset();
        assertTrue(equalsBuilder.isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testBoolean
    public void testBoolean() {
        boolean o1 = true;
        boolean o2 = false;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testObjectArray
    public void testObjectArray() {
        TestObject[] obj1 = new TestObject[3];
        obj1[0] = new TestObject(4);
        obj1[1] = new TestObject(5);
        obj1[2] = null;
        TestObject[] obj2 = new TestObject[3];
        obj2[0] = new TestObject(4);
        obj2[1] = new TestObject(5);
        obj2[2] = null;
        
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj2, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1].setA(6);
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1].setA(5);
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[2] = obj1[1];
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[2] = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
                       
        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testLongArray
    public void testLongArray() {
        long[] obj1 = new long[2];
        obj1[0] = 5L;
        obj1[1] = 6L;
        long[] obj2 = new long[2];
        obj2[0] = 5L;
        obj2[1] = 6L;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testIntArray
    public void testIntArray() {
        int[] obj1 = new int[2];
        obj1[0] = 5;
        obj1[1] = 6;
        int[] obj2 = new int[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testShortArray
    public void testShortArray() {
        short[] obj1 = new short[2];
        obj1[0] = 5;
        obj1[1] = 6;
        short[] obj2 = new short[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testCharArray
    public void testCharArray() {
        char[] obj1 = new char[2];
        obj1[0] = 5;
        obj1[1] = 6;
        char[] obj2 = new char[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testByteArray
    public void testByteArray() {
        byte[] obj1 = new byte[2];
        obj1[0] = 5;
        obj1[1] = 6;
        byte[] obj2 = new byte[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testDoubleArray
    public void testDoubleArray() {
        double[] obj1 = new double[2];
        obj1[0] = 5;
        obj1[1] = 6;
        double[] obj2 = new double[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testFloatArray
    public void testFloatArray() {
        float[] obj1 = new float[2];
        obj1[0] = 5;
        obj1[1] = 6;
        float[] obj2 = new float[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testBooleanArray
    public void testBooleanArray() {
        boolean[] obj1 = new boolean[2];
        obj1[0] = true;
        obj1[1] = false;
        boolean[] obj2 = new boolean[2];
        obj2[0] = true;
        obj2[1] = false;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = true;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testMultiLongArray
    public void testMultiLongArray() {
        long[][] array1 = new long[2][2];
        long[][] array2 = new long[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testMultiIntArray
    public void testMultiIntArray() {
        int[][] array1 = new int[2][2];
        int[][] array2 = new int[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testMultiShortArray
    public void testMultiShortArray() {
        short[][] array1 = new short[2][2];
        short[][] array2 = new short[2][2];
        for (short i = 0; i < array1.length; ++i) {
            for (short j = 0; j < array1[0].length; j++) {
                array1[i][j] = i;
                array2[i][j] = i;
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testMultiCharArray
    public void testMultiCharArray() {
        char[][] array1 = new char[2][2];
        char[][] array2 = new char[2][2];
        for (char i = 0; i < array1.length; ++i) {
            for (char j = 0; j < array1[0].length; j++) {
                array1[i][j] = i;
                array2[i][j] = i;
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testMultiByteArray
    public void testMultiByteArray() {
        byte[][] array1 = new byte[2][2];
        byte[][] array2 = new byte[2][2];
        for (byte i = 0; i < array1.length; ++i) {
            for (byte j = 0; j < array1[0].length; j++) {
                array1[i][j] = i;
                array2[i][j] = i;
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testMultiFloatArray
    public void testMultiFloatArray() {
        float[][] array1 = new float[2][2];
        float[][] array2 = new float[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testMultiDoubleArray
    public void testMultiDoubleArray() {
        double[][] array1 = new double[2][2];
        double[][] array2 = new double[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testMultiBooleanArray
    public void testMultiBooleanArray() {
        boolean[][] array1 = new boolean[2][2];
        boolean[][] array2 = new boolean[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i == 1) || (j == 1);
                array2[i][j] = (i == 1) || (j == 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = false;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
        
        
        boolean[] array3 = new boolean[]{true, true};
        assertFalse(new EqualsBuilder().append(array1, array3).isEquals());
        assertFalse(new EqualsBuilder().append(array3, array1).isEquals());
        assertFalse(new EqualsBuilder().append(array2, array3).isEquals());
        assertFalse(new EqualsBuilder().append(array3, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testRaggedArray
    public void testRaggedArray() {
        long array1[][] = new long[2][];
        long array2[][] = new long[2][];
        for (int i = 0; i < array1.length; ++i) {
            array1[i] = new long[2];
            array2[i] = new long[2];
            for (int j = 0; j < array1[i].length; ++j) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testMixedArray
    public void testMixedArray() {
        Object array1[] = new Object[2];
        Object array2[] = new Object[2];
        for (int i = 0; i < array1.length; ++i) {
            array1[i] = new long[2];
            array2[i] = new long[2];
            for (int j = 0; j < 2; ++j) {
                ((long[]) array1[i])[j] = (i + 1) * (j + 1);
                ((long[]) array2[i])[j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        ((long[]) array1[1])[1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testObjectArrayHiddenByObject
    public void testObjectArrayHiddenByObject() {
        TestObject[] array1 = new TestObject[2];
        array1[0] = new TestObject(4);
        array1[1] = new TestObject(5);
        TestObject[] array2 = new TestObject[2];
        array2[0] = new TestObject(4);
        array2[1] = new TestObject(5);
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1].setA(6);
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testLongArrayHiddenByObject
    public void testLongArrayHiddenByObject() {
        long[] array1 = new long[2];
        array1[0] = 5L;
        array1[1] = 6L;
        long[] array2 = new long[2];
        array2[0] = 5L;
        array2[1] = 6L;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testIntArrayHiddenByObject
    public void testIntArrayHiddenByObject() {
        int[] array1 = new int[2];
        array1[0] = 5;
        array1[1] = 6;
        int[] array2 = new int[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testShortArrayHiddenByObject
    public void testShortArrayHiddenByObject() {
        short[] array1 = new short[2];
        array1[0] = 5;
        array1[1] = 6;
        short[] array2 = new short[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testCharArrayHiddenByObject
    public void testCharArrayHiddenByObject() {
        char[] array1 = new char[2];
        array1[0] = 5;
        array1[1] = 6;
        char[] array2 = new char[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testByteArrayHiddenByObject
    public void testByteArrayHiddenByObject() {
        byte[] array1 = new byte[2];
        array1[0] = 5;
        array1[1] = 6;
        byte[] array2 = new byte[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testDoubleArrayHiddenByObject
    public void testDoubleArrayHiddenByObject() {
        double[] array1 = new double[2];
        array1[0] = 5;
        array1[1] = 6;
        double[] array2 = new double[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testFloatArrayHiddenByObject
    public void testFloatArrayHiddenByObject() {
        float[] array1 = new float[2];
        array1[0] = 5;
        array1[1] = 6;
        float[] array2 = new float[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testBooleanArrayHiddenByObject
    public void testBooleanArrayHiddenByObject() {
        boolean[] array1 = new boolean[2];
        array1[0] = true;
        array1[1] = false;
        boolean[] array2 = new boolean[2];
        array2[0] = true;
        array2[1] = false;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = true;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testUnrelatedClasses
    public void testUnrelatedClasses() {
        Object[] x = new Object[]{new TestACanEqualB(1)};
        Object[] y = new Object[]{new TestBCanEqualA(1)};

        
        assertTrue(Arrays.equals(x, x));
        assertTrue(Arrays.equals(y, y));
        assertTrue(Arrays.equals(x, y));
        assertTrue(Arrays.equals(y, x));
        
        assertTrue(x[0].equals(x[0]));
        assertTrue(y[0].equals(y[0]));
        assertTrue(x[0].equals(y[0]));
        assertTrue(y[0].equals(x[0]));
        assertTrue(new EqualsBuilder().append(x, x).isEquals());
        assertTrue(new EqualsBuilder().append(y, y).isEquals());
        assertTrue(new EqualsBuilder().append(x, y).isEquals());
        assertTrue(new EqualsBuilder().append(y, x).isEquals());
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testNpeForNullElement
    public void testNpeForNullElement() {
        Object[] x1 = new Object[] { new Integer(1), null, new Integer(3) };
        Object[] x2 = new Object[] { new Integer(1), new Integer(2), new Integer(3) };

        
        
        new EqualsBuilder().append(x1, x2);
    }

// org.apache.commons.lang3.builder.EqualsBuilderTest::testReflectionEqualsExcludeFields
    public void testReflectionEqualsExcludeFields() throws Exception {
        TestObjectWithMultipleFields x1 = new TestObjectWithMultipleFields(1, 2, 3);
        TestObjectWithMultipleFields x2 = new TestObjectWithMultipleFields(1, 3, 4);

        
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2));

        
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, (String[]) null));
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, new String[] {}));
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, new String[] {"xxx"}));

        
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, new String[] {"two"}));
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, new String[] {"three"}));

        
        assertTrue(EqualsBuilder.reflectionEquals(x1, x2, new String[] {"two", "three"}));

        
        assertTrue(EqualsBuilder.reflectionEquals(x1, x2, new String[] {"one", "two", "three"}));
        assertTrue(EqualsBuilder.reflectionEquals(x1, x2, new String[] {"one", "two", "three", "xxx"}));
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testInteger
    public void testInteger(boolean testTransients) {
        Integer i1 = new Integer(12345);
        Integer i2 = new Integer(12345);
        assertEqualsAndHashCodeContract(i1, i2, testTransients);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testInteger
    public void testInteger() {
        testInteger(false);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testIntegerWithTransients
    public void testIntegerWithTransients() {
        testInteger(true);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testFixture
    public void testFixture() {
        testFixture(false);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testFixtureWithTransients
    public void testFixtureWithTransients() {
        testFixture(true);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testFixture
    public void testFixture(boolean testTransients) {
        assertEqualsAndHashCodeContract(new TestFixture(2, 'c', "Test", (short) 2), new TestFixture(2, 'c', "Test", (short) 2), testTransients);
        assertEqualsAndHashCodeContract(
            new AllTransientFixture(2, 'c', "Test", (short) 2),
            new AllTransientFixture(2, 'c', "Test", (short) 2),
            testTransients);
        assertEqualsAndHashCodeContract(
            new SubTestFixture(2, 'c', "Test", (short) 2, "Same"),
            new SubTestFixture(2, 'c', "Test", (short) 2, "Same"),
            testTransients);
        assertEqualsAndHashCodeContract(
            new SubAllTransientFixture(2, 'c', "Test", (short) 2, "Same"),
            new SubAllTransientFixture(2, 'c', "Test", (short) 2, "Same"),
            testTransients);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testConstructorEx1
    public void testConstructorEx1() {
        try {
            new HashCodeBuilder(0, 0);

        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testConstructorEx2
    public void testConstructorEx2() {
        try {
            new HashCodeBuilder(2, 2);

        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCode
    public void testReflectionHashCode() {
        assertEquals(17 * 37, HashCodeBuilder.reflectionHashCode(new TestObject(0)));
        assertEquals(17 * 37 + 123456, HashCodeBuilder.reflectionHashCode(new TestObject(123456)));
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHierarchyHashCode
    public void testReflectionHierarchyHashCode() {
        assertEquals(17 * 37 * 37, HashCodeBuilder.reflectionHashCode(new TestSubObject(0, 0, 0)));
        assertEquals(17 * 37 * 37 * 37, HashCodeBuilder.reflectionHashCode(new TestSubObject(0, 0, 0), true));
        assertEquals((17 * 37 + 7890) * 37 + 123456, HashCodeBuilder.reflectionHashCode(new TestSubObject(123456, 7890,
                0)));
        assertEquals(((17 * 37 + 7890) * 37 + 0) * 37 + 123456, HashCodeBuilder.reflectionHashCode(new TestSubObject(
                123456, 7890, 0), true));
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHierarchyHashCodeEx1
    public void testReflectionHierarchyHashCodeEx1() {
        try {
            HashCodeBuilder.reflectionHashCode(0, 0, new TestSubObject(0, 0, 0), true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHierarchyHashCodeEx2
    public void testReflectionHierarchyHashCodeEx2() {
        try {
            HashCodeBuilder.reflectionHashCode(2, 2, new TestSubObject(0, 0, 0), true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCodeEx1
    public void testReflectionHashCodeEx1() {
        try {
            HashCodeBuilder.reflectionHashCode(0, 0, new TestObject(0), true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCodeEx2
    public void testReflectionHashCodeEx2() {
        try {
            HashCodeBuilder.reflectionHashCode(2, 2, new TestObject(0), true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCodeEx3
    public void testReflectionHashCodeEx3() {
        try {
            HashCodeBuilder.reflectionHashCode(13, 19, null, true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testSuper
    public void testSuper() {
        Object obj = new Object();
        assertEquals(17 * 37 + (19 * 41 + obj.hashCode()), new HashCodeBuilder(17, 37).appendSuper(
                new HashCodeBuilder(19, 41).append(obj).toHashCode()).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testObject
    public void testObject() {
        Object obj = null;
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj = new Object();
        assertEquals(17 * 37 + obj.hashCode(), new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testLong
    public void testLong() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((long) 0L).toHashCode());
        assertEquals(17 * 37 + (int) (123456789L ^ (123456789L >> 32)), new HashCodeBuilder(17, 37).append(
                (long) 123456789L).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testInt
    public void testInt() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((int) 0).toHashCode());
        assertEquals(17 * 37 + 123456, new HashCodeBuilder(17, 37).append((int) 123456).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testShort
    public void testShort() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((short) 0).toHashCode());
        assertEquals(17 * 37 + 12345, new HashCodeBuilder(17, 37).append((short) 12345).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testChar
    public void testChar() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((char) 0).toHashCode());
        assertEquals(17 * 37 + 1234, new HashCodeBuilder(17, 37).append((char) 1234).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testByte
    public void testByte() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((byte) 0).toHashCode());
        assertEquals(17 * 37 + 123, new HashCodeBuilder(17, 37).append((byte) 123).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testDouble
    public void testDouble() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((double) 0d).toHashCode());
        double d = 1234567.89;
        long l = Double.doubleToLongBits(d);
        assertEquals(17 * 37 + (int) (l ^ (l >> 32)), new HashCodeBuilder(17, 37).append(d).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testFloat
    public void testFloat() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((float) 0f).toHashCode());
        float f = 1234.89f;
        int i = Float.floatToIntBits(f);
        assertEquals(17 * 37 + i, new HashCodeBuilder(17, 37).append(f).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testBoolean
    public void testBoolean() {
        assertEquals(17 * 37 + 0, new HashCodeBuilder(17, 37).append(true).toHashCode());
        assertEquals(17 * 37 + 1, new HashCodeBuilder(17, 37).append(false).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testObjectArray
    public void testObjectArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((Object[]) null).toHashCode());
        Object[] obj = new Object[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = new Object();
        assertEquals((17 * 37 + obj[0].hashCode()) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = new Object();
        assertEquals((17 * 37 + obj[0].hashCode()) * 37 + obj[1].hashCode(), new HashCodeBuilder(17, 37).append(obj)
                .toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testObjectArrayAsObject
    public void testObjectArrayAsObject() {
        Object[] obj = new Object[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = new Object();
        assertEquals((17 * 37 + obj[0].hashCode()) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = new Object();
        assertEquals((17 * 37 + obj[0].hashCode()) * 37 + obj[1].hashCode(), new HashCodeBuilder(17, 37).append(
                (Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testLongArray
    public void testLongArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((long[]) null).toHashCode());
        long[] obj = new long[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = 5L;
        int h1 = (int) (5L ^ (5L >> 32));
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = 6L;
        int h2 = (int) (6L ^ (6L >> 32));
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testLongArrayAsObject
    public void testLongArrayAsObject() {
        long[] obj = new long[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = 5L;
        int h1 = (int) (5L ^ (5L >> 32));
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = 6L;
        int h2 = (int) (6L ^ (6L >> 32));
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testIntArray
    public void testIntArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((int[]) null).toHashCode());
        int[] obj = new int[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testIntArrayAsObject
    public void testIntArrayAsObject() {
        int[] obj = new int[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testShortArray
    public void testShortArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((short[]) null).toHashCode());
        short[] obj = new short[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = (short) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = (short) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testShortArrayAsObject
    public void testShortArrayAsObject() {
        short[] obj = new short[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = (short) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = (short) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testCharArray
    public void testCharArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((char[]) null).toHashCode());
        char[] obj = new char[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = (char) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = (char) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testCharArrayAsObject
    public void testCharArrayAsObject() {
        char[] obj = new char[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = (char) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = (char) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testByteArray
    public void testByteArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((byte[]) null).toHashCode());
        byte[] obj = new byte[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = (byte) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = (byte) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testByteArrayAsObject
    public void testByteArrayAsObject() {
        byte[] obj = new byte[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = (byte) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = (byte) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testDoubleArray
    public void testDoubleArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((double[]) null).toHashCode());
        double[] obj = new double[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = 5.4d;
        long l1 = Double.doubleToLongBits(5.4d);
        int h1 = (int) (l1 ^ (l1 >> 32));
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = 6.3d;
        long l2 = Double.doubleToLongBits(6.3d);
        int h2 = (int) (l2 ^ (l2 >> 32));
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testDoubleArrayAsObject
    public void testDoubleArrayAsObject() {
        double[] obj = new double[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = 5.4d;
        long l1 = Double.doubleToLongBits(5.4d);
        int h1 = (int) (l1 ^ (l1 >> 32));
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = 6.3d;
        long l2 = Double.doubleToLongBits(6.3d);
        int h2 = (int) (l2 ^ (l2 >> 32));
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testFloatArray
    public void testFloatArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((float[]) null).toHashCode());
        float[] obj = new float[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = 5.4f;
        int h1 = Float.floatToIntBits(5.4f);
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = 6.3f;
        int h2 = Float.floatToIntBits(6.3f);
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testFloatArrayAsObject
    public void testFloatArrayAsObject() {
        float[] obj = new float[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = 5.4f;
        int h1 = Float.floatToIntBits(5.4f);
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = 6.3f;
        int h2 = Float.floatToIntBits(6.3f);
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testBooleanArray
    public void testBooleanArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((boolean[]) null).toHashCode());
        boolean[] obj = new boolean[2];
        assertEquals((17 * 37 + 1) * 37 + 1, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = true;
        assertEquals((17 * 37 + 0) * 37 + 1, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = false;
        assertEquals((17 * 37 + 0) * 37 + 1, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testBooleanArrayAsObject
    public void testBooleanArrayAsObject() {
        boolean[] obj = new boolean[2];
        assertEquals((17 * 37 + 1) * 37 + 1, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = true;
        assertEquals((17 * 37 + 0) * 37 + 1, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = false;
        assertEquals((17 * 37 + 0) * 37 + 1, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testBooleanMultiArray
    public void testBooleanMultiArray() {
        boolean[][] obj = new boolean[2][];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = new boolean[0];
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = new boolean[1];
        assertEquals((17 * 37 + 1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = new boolean[2];
        assertEquals(((17 * 37 + 1) * 37 + 1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0][0] = true;
        assertEquals(((17 * 37 + 0) * 37 + 1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = new boolean[1];
        assertEquals((((17 * 37 + 0) * 37 + 1) * 37 + 1), new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCodeExcludeFields
    public void testReflectionHashCodeExcludeFields() throws Exception {
        TestObjectWithMultipleFields x = new TestObjectWithMultipleFields(1, 2, 3);

        assertEquals((((17 * 37 + 1) * 37 + 2) * 37 + 3), HashCodeBuilder.reflectionHashCode(x));

        assertEquals((((17 * 37 + 1) * 37 + 2) * 37 + 3), HashCodeBuilder.reflectionHashCode(x, (String[]) null));
        assertEquals((((17 * 37 + 1) * 37 + 2) * 37 + 3), HashCodeBuilder.reflectionHashCode(x, new String[]{}));
        assertEquals((((17 * 37 + 1) * 37 + 2) * 37 + 3), HashCodeBuilder.reflectionHashCode(x, new String[]{"xxx"}));

        assertEquals(((17 * 37 + 1) * 37 + 3), HashCodeBuilder.reflectionHashCode(x, new String[]{"two"}));
        assertEquals(((17 * 37 + 1) * 37 + 2), HashCodeBuilder.reflectionHashCode(x, new String[]{"three"}));

        assertEquals((17 * 37 + 1), HashCodeBuilder.reflectionHashCode(x, new String[]{"two", "three"}));

        assertEquals(17, HashCodeBuilder.reflectionHashCode(x, new String[]{"one", "two", "three"}));
        assertEquals(17, HashCodeBuilder.reflectionHashCode(x, new String[]{"one", "two", "three", "xxx"}));
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionObjectCycle
    public void testReflectionObjectCycle() {
        ReflectionTestCycleA a = new ReflectionTestCycleA();
        ReflectionTestCycleB b = new ReflectionTestCycleB();
        a.b = b;
        b.a = a;
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        a.hashCode();
        b.hashCode();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testToHashCodeEqualsHashCode
    public void testToHashCodeEqualsHashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(new Object()).append('a');
        assertEquals("hashCode() is no longer returning the same value as toHashCode() - see LANG-520", 
                     hcb.toHashCode(), hcb.hashCode());
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExclude
    public void test_toStringExclude() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), SECRET_FIELD);
        this.validateSecretFieldAbsent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeArray
    public void test_toStringExcludeArray() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), new String[]{SECRET_FIELD});
        this.validateSecretFieldAbsent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeArrayWithNull
    public void test_toStringExcludeArrayWithNull() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), new String[]{null});
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeArrayWithNulls
    public void test_toStringExcludeArrayWithNulls() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), new String[]{null, null});
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeCollection
    public void test_toStringExcludeCollection() {
        List<String> excludeList = new ArrayList<String>();
        excludeList.add(SECRET_FIELD);
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), excludeList);
        this.validateSecretFieldAbsent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeCollectionWithNull
    public void test_toStringExcludeCollectionWithNull() {
        List<String> excludeList = new ArrayList<String>();
        excludeList.add(null);
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), excludeList);
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeCollectionWithNulls
    public void test_toStringExcludeCollectionWithNulls() {
        List<String> excludeList = new ArrayList<String>();
        excludeList.add(null);
        excludeList.add(null);
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), excludeList);
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeEmptyArray
    public void test_toStringExcludeEmptyArray() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), ArrayUtils.EMPTY_STRING_ARRAY);
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeEmptyCollection
    public void test_toStringExcludeEmptyCollection() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), new ArrayList<String>());
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeNullArray
    public void test_toStringExcludeNullArray() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), (String[]) null);
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeNullCollection
    public void test_toStringExcludeNullCollection() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), (Collection<String>) null);
        this.validateSecretFieldPresent(toString);
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
