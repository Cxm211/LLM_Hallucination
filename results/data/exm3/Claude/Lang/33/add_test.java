// org/apache/commons/lang3/ClassUtilsTest.java
public void testToClass_objectWithNullAtStart() {
    assertTrue(Arrays.equals(new Class[] { null, String.class, Integer.class },
            ClassUtils.toClass(new Object[] { null, "Test", 1 })));
}