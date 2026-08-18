// org/jsoup/helper/HttpConnectionTest.java
@Test
public void testLooksLikeUtf8ValidAndIncomplete() throws Exception {
    Class<?> clazz = Class.forName("org.jsoup.helper.DataUtil");
    java.lang.reflect.Method method = clazz.getDeclaredMethod("looksLikeUtf8", byte[].class);
    method.setAccessible(true);
    // ASCII only
    org.junit.Assert.assertTrue((Boolean) method.invoke(null, "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    // valid 2-byte sequence
    org.junit.Assert.assertTrue((Boolean) method.invoke(null, "é".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    // valid 3-byte sequence
    org.junit.Assert.assertTrue((Boolean) method.invoke(null, "€".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    // valid 4-byte sequence
    org.junit.Assert.assertTrue((Boolean) method.invoke(null, "\uD83D\uDE00".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    // BOM + ASCII
    byte[] bomWithText = new byte[6];
    bomWithText[0] = (byte) 0xEF;
    bomWithText[1] = (byte) 0xBB;
    bomWithText[2] = (byte) 0xBF;
    System.arraycopy("abc".getBytes(java.nio.charset.StandardCharsets.UTF_8), 0, bomWithText, 3, 3);
    org.junit.Assert.assertTrue((Boolean) method.invoke(null, bomWithText));
    // incomplete 2-byte sequence (only leading byte)
    org.junit.Assert.assertFalse((Boolean) method.invoke(null, new byte[]{(byte) 0xC3}));
}
