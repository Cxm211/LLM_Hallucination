// org/apache/commons/lang3/SystemUtilsTest.java::testJavaVersionAsInt
public void testJavaVersionAsInt_additional() {
        assertEquals(170, SystemUtils.toJavaVersionInt("1.7.0"));
        assertEquals(180, SystemUtils.toJavaVersionInt("1.8.0"));
    }