// org/apache/commons/lang3/SystemUtilsTest.java
@Test
public void testJavaVersionAsIntAdditional() {
    assertEquals(0, SystemUtils.toJavaVersionInt("abc"));
    assertEquals(170, SystemUtils.toJavaVersionInt("1.7"));
    assertEquals(180, SystemUtils.toJavaVersionInt("1.8.0"));
    assertEquals(0, SystemUtils.toJavaVersionInt("   "));
    assertEquals(100, SystemUtils.toJavaVersionInt("1.0"));
}