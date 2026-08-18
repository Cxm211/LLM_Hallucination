// org/apache/commons/lang3/StringUtilsTest.java
public void testReplaceEachRepeatedlyNoChange() throws Exception {
    java.lang.reflect.Method replaceEachMethod = org.apache.commons.lang3.StringUtils.class.getDeclaredMethod("replaceEach", String.class, String[].class, String[].class, boolean.class, int.class);
    replaceEachMethod.setAccessible(true);
    String result = (String) replaceEachMethod.invoke(null, "abc", new String[]{"a"}, new String[]{"a"}, true, 5);
    org.junit.Assert.assertEquals("abc", result);
}
