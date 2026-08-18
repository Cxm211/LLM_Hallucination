// org/apache/commons/lang/WordUtilsTest.java
public void testAbbreviateAdditional2() {
    assertEquals("012   ", WordUtils.abbreviate("012   3456789", 3, 6, ""));
}