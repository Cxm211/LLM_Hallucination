// org/apache/commons/lang3/StringUtilsTest.java::testReplace_StringStringArrayStringArray
assertEquals("xyc", StringUtils.replaceEach("abc", new String[]{"a","b",null}, new String[]{"x","y","z"}));