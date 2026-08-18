// org/apache/commons/lang/WordUtilsTest.java
public void testAbbreviateAdditional1() {
    assertEquals("012  345-", WordUtils.abbreviate("012  345 6789", 5, 10, "-"));
}