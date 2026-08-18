// org/apache/commons/lang3/SystemUtilsTest.java
public void testJavaVersionAsIntMoreParts() {
        assertEquals(123, SystemUtils.toJavaVersionInt("1.2.3.4"));
        assertEquals(0, SystemUtils.toJavaVersionInt("0.0.0.0"));
        assertEquals(1230, SystemUtils.toJavaVersionInt("10.20.30.40"));
    }
