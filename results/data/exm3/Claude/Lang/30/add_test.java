// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testIndexOfAny_WithMixedSupplementaryAndBMP() {
    assertEquals(0, StringUtils.indexOfAny("abc" + CharU20000, CharU20000.toCharArray()));
    assertEquals(3, StringUtils.indexOfAny("abc" + CharU20000, CharU20000.toCharArray()));
    assertEquals(0, StringUtils.indexOfAny(CharU20000 + "abc", CharU20000.toCharArray()));
    assertEquals(1, StringUtils.indexOfAny("a" + CharU20000 + "bc", CharU20000.toCharArray()));
}