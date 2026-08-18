// org/apache/commons/lang/WordUtilsTest.java
public void testAbbreviateAdditional4() {
    assertEquals("012...", WordUtils.abbreviate("012     3456", 4, 6, "..."));
}