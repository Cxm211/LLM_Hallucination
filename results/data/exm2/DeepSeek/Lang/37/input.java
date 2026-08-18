    public static <T> T[] addAll(T[] array1, T... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final Class<?> type1 = array1.getClass().getComponentType();
        T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            // Check if problem is incompatible types
        return joinedArray;
    }

// trigger testcase
public void testJira567(){
        Number[] n;
        // Valid array construction
        n = ArrayUtils.addAll(new Number[]{Integer.valueOf(1)}, new Long[]{Long.valueOf(2)});
        assertEquals(2,n.length);
        assertEquals(Number.class,n.getClass().getComponentType());
        try {
            // Invalid - can't store Long in Integer array
               n = ArrayUtils.addAll(new Integer[]{Integer.valueOf(1)}, new Long[]{Long.valueOf(2)});
               fail("Should have generated IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }
