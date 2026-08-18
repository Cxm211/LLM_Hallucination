// org/apache/commons/lang/WordUtilsTest.java
public void testAbbreviateBounds() {
        // Test lower greater than string length
        assertEquals("short", WordUtils.abbreviate("short", 10, 20, ""));
        // Test negative upper not -1
        assertEquals("", WordUtils.abbreviate("test", 0, -2, ""));
    }
