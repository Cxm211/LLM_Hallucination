// org/jsoup/helper/HttpConnectionTest.java
@Test
public void testLooksLikeUtf8InvalidContinuation() throws Exception {
    Class<?> clazz = Class.forName("org.jsoup.helper.DataUtil");
    java.lang.reflect.Method method = clazz.getDeclaredMethod("looksLikeUtf8", byte[].class);
    method.setAccessible(true);
    // valid leading but invalid continuation (not 10xxxxxx)
    org.junit.Assert.assertFalse((Boolean) method.invoke(null, new byte[]{(byte) 0xC3, (byte) 0x00}));
    // incomplete 4-byte sequence (only three bytes)
    org.junit.Assert.assertFalse((Boolean) method.invoke(null, new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98}));
}
