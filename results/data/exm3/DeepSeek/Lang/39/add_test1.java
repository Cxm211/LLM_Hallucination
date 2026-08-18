// org/apache/commons/lang3/StringUtilsTest.java
public void testReplaceEachRepeatedlyTimeToLiveZero() throws Exception {
    java.lang.reflect.Method replaceEachMethod = org.apache.commons.lang3.StringUtils.class.getDeclaredMethod("replaceEach", String.class, String[].class, String[].class, boolean.class, int.class);
    replaceEachMethod.setAccessible(true);
    try {
        replaceEachMethod.invoke(null, "abc", new String[]{"a"}, new String[]{"b"}, true, 0);
        org.junit.Assert.fail("Expected IllegalStateException");
    } catch (java.lang.reflect.InvocationTargetException e) {
        Throwable cause = e.getCause();
        org.junit.Assert.assertTrue(cause instanceof IllegalStateException);
        org.junit.Assert.assertTrue(cause.getMessage().contains("TimeToLive of 0 is exceeded"));
    }
}
