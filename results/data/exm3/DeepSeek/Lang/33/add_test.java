// org/apache/commons/lang3/ClassUtilsTest.java
public void testToClass_objectNullElements() {
        // single null element
        assertTrue(Arrays.equals(new Class[] { null },
                ClassUtils.toClass(new Object[] { null })));
        // all nulls
        assertTrue(Arrays.equals(new Class[] { null, null },
                ClassUtils.toClass(new Object[] { null, null })));
        // null first, then non-null
        assertTrue(Arrays.equals(new Class[] { null, String.class },
                ClassUtils.toClass(new Object[] { null, "hello" })));
    }
