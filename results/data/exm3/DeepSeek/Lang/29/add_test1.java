// org/apache/commons/lang3/SystemUtilsTest.java
public void testJavaVersionAsIntNonDigitsInterspersed() {
        assertEquals(123, SystemUtils.toJavaVersionInt("a1.2.3b"));
        assertEquals(123, SystemUtils.toJavaVersionInt("1-2-3"));
        assertEquals(120, SystemUtils.toJavaVersionInt("1.2."));
        assertEquals(120, SystemUtils.toJavaVersionInt(".1.2"));
    }
