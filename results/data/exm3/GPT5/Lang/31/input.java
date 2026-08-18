// buggy function
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

// trigger testcase
// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java::testContainsAnyCharArrayWithSupplementaryChars
public void testContainsAnyCharArrayWithSupplementaryChars() {
		assertEquals(true, StringUtils.containsAny(CharU20000 + CharU20001, CharU20000.toCharArray()));
		assertEquals(true, StringUtils.containsAny(CharU20000 + CharU20001, CharU20001.toCharArray()));
		assertEquals(true, StringUtils.containsAny(CharU20000, CharU20000.toCharArray()));
		// Sanity check:
		assertEquals(-1, CharU20000.indexOf(CharU20001));
		assertEquals(0, CharU20000.indexOf(CharU20001.charAt(0)));
		assertEquals(-1, CharU20000.indexOf(CharU20001.charAt(1)));
		// Test:
		assertEquals(false, StringUtils.containsAny(CharU20000, CharU20001.toCharArray()));
		assertEquals(false, StringUtils.containsAny(CharU20001, CharU20000.toCharArray()));
	}

// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java::testContainsAnyStringWithSupplementaryChars
public void testContainsAnyStringWithSupplementaryChars() {
		assertEquals(true, StringUtils.containsAny(CharU20000 + CharU20001, CharU20000));
		assertEquals(true, StringUtils.containsAny(CharU20000 + CharU20001, CharU20001));
		assertEquals(true, StringUtils.containsAny(CharU20000, CharU20000));
		// Sanity check:
		assertEquals(-1, CharU20000.indexOf(CharU20001));
		assertEquals(0, CharU20000.indexOf(CharU20001.charAt(0)));
		assertEquals(-1, CharU20000.indexOf(CharU20001.charAt(1)));
		// Test:
		assertEquals(false, StringUtils.containsAny(CharU20000, CharU20001));
		assertEquals(false, StringUtils.containsAny(CharU20001, CharU20000));
	}
