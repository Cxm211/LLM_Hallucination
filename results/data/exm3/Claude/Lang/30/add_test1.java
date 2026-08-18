// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testContainsAny_WithLoneHighSurrogate() {
    assertEquals(true, StringUtils.containsAny(CharUSuppCharHigh, CharUSuppCharHigh.toCharArray()));
    assertEquals(false, StringUtils.containsAny("abc", CharUSuppCharHigh.toCharArray()));
}