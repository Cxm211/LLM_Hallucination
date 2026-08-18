// org/apache/commons/lang/StringUtilsEqualsIndexOfTest.java
public void testContainsIgnoreCase_MultipleOccurrences() {
    assertTrue(StringUtils.containsIgnoreCase("abcABC", "BC"));
    assertTrue(StringUtils.containsIgnoreCase("Hello World", "world"));
    assertTrue(StringUtils.containsIgnoreCase("HELLO", "ell"));
}