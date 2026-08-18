// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testIndexOfAnyBut_WithLoneSurrogates() {
    assertEquals(-1, StringUtils.indexOfAnyBut(CharUSuppCharHigh, CharUSuppCharHigh.toCharArray()));
    assertEquals(0, StringUtils.indexOfAnyBut(CharUSuppCharHigh, CharUSuppCharLow.toCharArray()));
    assertEquals(0, StringUtils.indexOfAnyBut(CharUSuppCharHigh + "a", CharUSuppCharHigh.toCharArray()));
}