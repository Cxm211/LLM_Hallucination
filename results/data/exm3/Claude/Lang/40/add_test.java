// org/apache/commons/lang/StringUtilsEqualsIndexOfTest.java
public void testContainsIgnoreCase_EmptyString() {
    assertTrue(StringUtils.containsIgnoreCase("", ""));
    assertTrue(StringUtils.containsIgnoreCase("abc", ""));
    assertFalse(StringUtils.containsIgnoreCase("", "a"));
}