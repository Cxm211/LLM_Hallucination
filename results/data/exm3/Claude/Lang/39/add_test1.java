// org/apache/commons/lang3/StringUtilsTest.java
public void testReplaceEach_EmptySearchString() {
    assertEquals("abc", StringUtils.replaceEach("abc", new String[]{""}, new String[]{"x"}));
    assertEquals("abc", StringUtils.replaceEach("abc", new String[]{"", "a"}, new String[]{"x", "y"}));
}