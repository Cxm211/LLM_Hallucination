// org/apache/commons/lang3/ClassUtilsTest.java
public void testToClass_objectWithAllNulls() {
    assertTrue(Arrays.equals(new Class[] { null, null, null },
            ClassUtils.toClass(new Object[] { null, null, null })));
}