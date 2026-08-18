    public static int indexOfAny(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        int csLen = cs.length();
        int searchLen = searchChars.length;
        for (int i = 0; i < csLen; i++) {
            char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                        // ch is a supplementary character
                        return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    public static boolean containsAny(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return false;
        }
        int csLength = cs.length();
        int searchLength = searchChars.length;
        int csLast = csLength - 1;
        int searchLast = searchLength - 1;
        for (int i = 0; i < csLength; i++) {
            char ch = cs.charAt(i);
            for (int j = 0; j < searchLength; j++) {
                if (searchChars[j] == ch) {
                    if (i < csLast && j < searchLast && ch >= Character.MIN_HIGH_SURROGATE && ch <= Character.MAX_HIGH_SURROGATE) {
                            // missing low surrogate, fine, like String.indexOf(String)
                        if (searchChars[j + 1] == cs.charAt(i + 1)) {
                            return true;
                        }
                    } else {
                        // ch is in the Basic Multilingual Plane
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean containsAny(CharSequence cs, String searchChars) {
        if (searchChars == null) {
            return false;
        }
        return containsAny(cs, searchChars.toCharArray());
    }

    public static int indexOfAnyBut(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        int csLen = cs.length();
        int searchLen = searchChars.length;
        outer:
        for (int i = 0; i < csLen; i++) {
            char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                        continue outer;
                }
            }
            return i;
        }
        return INDEX_NOT_FOUND;
    }

    public static int indexOfAnyBut(String str, String searchChars) {
        if (isEmpty(str) || isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);
            if (searchChars.indexOf(ch) < 0) {
                    return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    public static boolean containsNone(CharSequence cs, char[] searchChars) {
        if (cs == null || searchChars == null) {
            return true;
        }
        int csLen = cs.length();
        int searchLen = searchChars.length;
        for (int i = 0; i < csLen; i++) {
            char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                            // missing low surrogate, fine, like String.indexOf(String)
                        // ch is in the Basic Multilingual Plane
                        return false;
                }
            }
        }
        return true;
    }

// trigger testcase
public void testContainsAny_StringCharArrayWithBadSupplementaryChars() {
        // Test edge case: 1/2 of a (broken) supplementary char
        assertEquals(false, StringUtils.containsAny(CharUSuppCharHigh, CharU20001.toCharArray()));
        assertEquals(false, StringUtils.containsAny("abc" + CharUSuppCharHigh + "xyz", CharU20001.toCharArray()));
        assertEquals(-1, CharUSuppCharLow.indexOf(CharU20001));
        assertEquals(false, StringUtils.containsAny(CharUSuppCharLow, CharU20001.toCharArray()));
        assertEquals(false, StringUtils.containsAny(CharU20001, CharUSuppCharHigh.toCharArray()));
        assertEquals(0, CharU20001.indexOf(CharUSuppCharLow));
        assertEquals(true, StringUtils.containsAny(CharU20001, CharUSuppCharLow.toCharArray()));
    }

public void testContainsAny_StringWithBadSupplementaryChars() {
        // Test edge case: 1/2 of a (broken) supplementary char
        assertEquals(false, StringUtils.containsAny(CharUSuppCharHigh, CharU20001));
        assertEquals(-1, CharUSuppCharLow.indexOf(CharU20001));
        assertEquals(false, StringUtils.containsAny(CharUSuppCharLow, CharU20001));
        assertEquals(false, StringUtils.containsAny(CharU20001, CharUSuppCharHigh));
        assertEquals(0, CharU20001.indexOf(CharUSuppCharLow));
        assertEquals(true, StringUtils.containsAny(CharU20001, CharUSuppCharLow));
    }

public void testContainsNone_CharArrayWithBadSupplementaryChars() {
        // Test edge case: 1/2 of a (broken) supplementary char
        assertEquals(true, StringUtils.containsNone(CharUSuppCharHigh, CharU20001.toCharArray()));
        assertEquals(-1, CharUSuppCharLow.indexOf(CharU20001));
        assertEquals(true, StringUtils.containsNone(CharUSuppCharLow, CharU20001.toCharArray()));
        assertEquals(-1, CharU20001.indexOf(CharUSuppCharHigh));
        assertEquals(true, StringUtils.containsNone(CharU20001, CharUSuppCharHigh.toCharArray()));
        assertEquals(0, CharU20001.indexOf(CharUSuppCharLow));
        assertEquals(false, StringUtils.containsNone(CharU20001, CharUSuppCharLow.toCharArray()));
    }

public void testContainsNone_CharArrayWithSupplementaryChars() {
        assertEquals(false, StringUtils.containsNone(CharU20000 + CharU20001, CharU20000.toCharArray()));
        assertEquals(false, StringUtils.containsNone(CharU20000 + CharU20001, CharU20001.toCharArray()));
        assertEquals(false, StringUtils.containsNone(CharU20000, CharU20000.toCharArray()));
        // Sanity check:
        assertEquals(-1, CharU20000.indexOf(CharU20001));
        assertEquals(0, CharU20000.indexOf(CharU20001.charAt(0)));
        assertEquals(-1, CharU20000.indexOf(CharU20001.charAt(1)));
        // Test:
        assertEquals(true, StringUtils.containsNone(CharU20000, CharU20001.toCharArray()));
        assertEquals(true, StringUtils.containsNone(CharU20001, CharU20000.toCharArray()));
    }

public void testContainsNone_StringWithBadSupplementaryChars() {
        // Test edge case: 1/2 of a (broken) supplementary char
        assertEquals(true, StringUtils.containsNone(CharUSuppCharHigh, CharU20001));
        assertEquals(-1, CharUSuppCharLow.indexOf(CharU20001));
        assertEquals(true, StringUtils.containsNone(CharUSuppCharLow, CharU20001));
        assertEquals(-1, CharU20001.indexOf(CharUSuppCharHigh));
        assertEquals(true, StringUtils.containsNone(CharU20001, CharUSuppCharHigh));
        assertEquals(0, CharU20001.indexOf(CharUSuppCharLow));
        assertEquals(false, StringUtils.containsNone(CharU20001, CharUSuppCharLow));        
    }

public void testContainsNone_StringWithSupplementaryChars() {
        assertEquals(false, StringUtils.containsNone(CharU20000 + CharU20001, CharU20000));
        assertEquals(false, StringUtils.containsNone(CharU20000 + CharU20001, CharU20001));
        assertEquals(false, StringUtils.containsNone(CharU20000, CharU20000));
        // Sanity check:
        assertEquals(-1, CharU20000.indexOf(CharU20001));
        assertEquals(0, CharU20000.indexOf(CharU20001.charAt(0)));
        assertEquals(-1, CharU20000.indexOf(CharU20001.charAt(1)));
        // Test:
        assertEquals(true, StringUtils.containsNone(CharU20000, CharU20001));
        assertEquals(true, StringUtils.containsNone(CharU20001, CharU20000));
    }

public void testIndexOfAnyBut_StringCharArrayWithSupplementaryChars() {
        assertEquals(2, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20000.toCharArray()));
        assertEquals(0, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20001.toCharArray()));
        assertEquals(-1, StringUtils.indexOfAnyBut(CharU20000, CharU20000.toCharArray()));
        assertEquals(0, StringUtils.indexOfAnyBut(CharU20000, CharU20001.toCharArray()));        
    }

public void testIndexOfAnyBut_StringStringWithSupplementaryChars() {
        assertEquals(2, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20000));
        assertEquals(0, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20001));
        assertEquals(-1, StringUtils.indexOfAnyBut(CharU20000, CharU20000));
        assertEquals(0, StringUtils.indexOfAnyBut(CharU20000, CharU20001));        
    }

public void testIndexOfAny_StringCharArrayWithSupplementaryChars() {
        assertEquals(0, StringUtils.indexOfAny(CharU20000 + CharU20001, CharU20000.toCharArray()));
        assertEquals(2, StringUtils.indexOfAny(CharU20000 + CharU20001, CharU20001.toCharArray()));
        assertEquals(0, StringUtils.indexOfAny(CharU20000, CharU20000.toCharArray()));
        assertEquals(-1, StringUtils.indexOfAny(CharU20000, CharU20001.toCharArray()));    
    }

public void testIndexOfAny_StringStringWithSupplementaryChars() {
        assertEquals(0, StringUtils.indexOfAny(CharU20000 + CharU20001, CharU20000));
        assertEquals(2, StringUtils.indexOfAny(CharU20000 + CharU20001, CharU20001));
        assertEquals(0, StringUtils.indexOfAny(CharU20000, CharU20000));
        assertEquals(-1, StringUtils.indexOfAny(CharU20000, CharU20001));    
    }
