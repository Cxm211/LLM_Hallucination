// org/apache/commons/lang/ClassUtilsTest.java::test_getPackageName_Class
assertEquals("java.util", ClassUtils.getPackageName(Map.Entry[].class));
assertEquals("java.util", ClassUtils.getPackageName(Map.Entry[][].class));