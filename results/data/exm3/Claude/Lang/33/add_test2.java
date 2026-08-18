// org/apache/commons/lang3/ClassUtilsTest.java
public void testToClass_objectWithMultipleNulls() {
    assertTrue(Arrays.equals(new Class[] { null, Integer.class, null, String.class, null },
            ClassUtils.toClass(new Object[] { null, 42, null, "Test", null })));
}