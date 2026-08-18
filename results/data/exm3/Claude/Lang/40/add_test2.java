// org/apache/commons/lang/StringUtilsEqualsIndexOfTest.java
public void testContainsIgnoreCase_BoundaryConditions() {
    assertTrue(StringUtils.containsIgnoreCase("a", "A"));
    assertTrue(StringUtils.containsIgnoreCase("ABC", "ABC"));
    assertFalse(StringUtils.containsIgnoreCase("ab", "abc"));
    assertTrue(StringUtils.containsIgnoreCase("abc", "abc"));
}