// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testContainsAnyCharArrayEdgeCases() {
	// Test empty and null cases are still handled correctly
	assertEquals(false, StringUtils.containsAny("", new char[]{'a'}));
	assertEquals(false, StringUtils.containsAny("a", new char[]{}));
	// Test single BMP character
	assertEquals(true, StringUtils.containsAny("a", new char[]{'a'}));
	assertEquals(false, StringUtils.containsAny("a", new char[]{'b'}));
	// Test supplementary at different positions
	assertEquals(true, StringUtils.containsAny(CharU20000 + "abc", CharU20000.toCharArray()));
	assertEquals(true, StringUtils.containsAny("abc" + CharU20000, CharU20000.toCharArray()));
	assertEquals(true, StringUtils.containsAny("a" + CharU20000 + "c", CharU20000.toCharArray()));
}