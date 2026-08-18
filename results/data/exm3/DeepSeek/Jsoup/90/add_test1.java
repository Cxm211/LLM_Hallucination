// org/jsoup/helper/HttpConnectionTest.java
@Test
public void testLooksLikeUtf8InvalidLeading() throws Exception {
    Class<?> clazz = Class.forName("org.jsoup.helper.DataUtil");
    java.lang.reflect.Method method = clazz.getDeclaredMethod("looksLikeUtf8", byte[].class);
    method.setAccessible(true);
    // invalid leading byte 0xF8 (should return false)
    org.junit.Assert.assertFalse((Boolean) method.invoke(null, new byte[]{(byte) 0xF8}));
    // invalid leading byte 0xFF
    org.junit.Assert.assertFalse((Boolean) method.invoke(null, new byte[]{(byte) 0xFF}));
    // incomplete 3-byte sequence (only two bytes)
    org.junit.Assert.assertFalse((Boolean) method.invoke(null, new byte[]{(byte) 0xE0, (byte) 0xA0}));
}
