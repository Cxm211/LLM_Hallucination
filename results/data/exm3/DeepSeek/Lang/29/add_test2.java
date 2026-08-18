// org/apache/commons/lang3/SystemUtilsTest.java
public void testJavaVersionAsIntNoDigits() {
        assertEquals(0, SystemUtils.toJavaVersionInt("no digits"));
        assertEquals(0, SystemUtils.toJavaVersionInt("abc"));
        assertEquals(0, SystemUtils.toJavaVersionInt("-"));
    }
