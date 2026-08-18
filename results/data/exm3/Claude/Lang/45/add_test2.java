// org/apache/commons/lang/WordUtilsTest.java
public void testAbbreviateAdditional3() {
    assertEquals("01234567", WordUtils.abbreviate("01234567  89", 8, 10, null));
}