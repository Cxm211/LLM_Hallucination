// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testContainsAnyCharArrayWithMixedSupplementaryAndBMP() {
	// Test mixing BMP and supplementary characters
	assertEquals(true, StringUtils.containsAny("abc" + CharU20000, new char[]{'a'}));
	assertEquals(true, StringUtils.containsAny("abc" + CharU20000, new char[]{'c'}));
	assertEquals(false, StringUtils.containsAny("abc", CharU20000.toCharArray()));
	assertEquals(false, StringUtils.containsAny(CharU20000, new char[]{'a', 'b', 'c'}));
}