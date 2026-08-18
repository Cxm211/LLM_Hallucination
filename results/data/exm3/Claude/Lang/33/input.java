// buggy function
    public static Class<?>[] toClass(Object[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        Class<?>[] classes = new Class[array.length];
        for (int i = 0; i < array.length; i++) {
            classes[i] = array[i].getClass();
        }
        return classes;
    }

// trigger testcase
// org/apache/commons/lang3/ClassUtilsTest.java::testToClass_object
public void testToClass_object() {
        assertNull(ClassUtils.toClass(null));

        assertSame(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass(ArrayUtils.EMPTY_OBJECT_ARRAY));

        assertTrue(Arrays.equals(new Class[] { String.class, Integer.class, Double.class },
                ClassUtils.toClass(new Object[] { "Test", 1, 99d })));

        assertTrue(Arrays.equals(new Class[] { String.class, null, Double.class },
                ClassUtils.toClass(new Object[] { "Test", null, 99d })));
    }
