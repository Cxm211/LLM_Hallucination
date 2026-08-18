// buggy code
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

// relevant test
// org.apache.commons.lang3.ArrayUtilsAddTest::testJira567
    public void testJira567(){
        Number[] n;
        
        n = ArrayUtils.addAll(new Number[]{Integer.valueOf(1)}, new Long[]{Long.valueOf(2)});
        assertEquals(2,n.length);
        assertEquals(Number.class,n.getClass().getComponentType());
        try {
            
               n = ArrayUtils.addAll(new Integer[]{Integer.valueOf(1)}, new Long[]{Long.valueOf(2)});
               fail("Should have generated IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayBoolean
    public void testAddObjectArrayBoolean() {
        boolean[] newArray;
        newArray = ArrayUtils.add((boolean[])null, false);
        assertTrue(Arrays.equals(new boolean[]{false}, newArray));
        assertEquals(Boolean.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add((boolean[])null, true);
        assertTrue(Arrays.equals(new boolean[]{true}, newArray));
        assertEquals(Boolean.TYPE, newArray.getClass().getComponentType());
        boolean[] array1 = new boolean[]{true, false, true};
        newArray = ArrayUtils.add(array1, false);
        assertTrue(Arrays.equals(new boolean[]{true, false, true, false}, newArray));
        assertEquals(Boolean.TYPE, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayByte
    public void testAddObjectArrayByte() {
        byte[] newArray;
        newArray = ArrayUtils.add((byte[])null, (byte)0);
        assertTrue(Arrays.equals(new byte[]{0}, newArray));
        assertEquals(Byte.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add((byte[])null, (byte)1);
        assertTrue(Arrays.equals(new byte[]{1}, newArray));
        assertEquals(Byte.TYPE, newArray.getClass().getComponentType());
        byte[] array1 = new byte[]{1, 2, 3};
        newArray = ArrayUtils.add(array1, (byte)0);
        assertTrue(Arrays.equals(new byte[]{1, 2, 3, 0}, newArray));
        assertEquals(Byte.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(array1, (byte)4);
        assertTrue(Arrays.equals(new byte[]{1, 2, 3, 4}, newArray));
        assertEquals(Byte.TYPE, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayChar
    public void testAddObjectArrayChar() {
        char[] newArray;
        newArray = ArrayUtils.add((char[])null, (char)0);
        assertTrue(Arrays.equals(new char[]{0}, newArray));
        assertEquals(Character.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add((char[])null, (char)1);
        assertTrue(Arrays.equals(new char[]{1}, newArray));
        assertEquals(Character.TYPE, newArray.getClass().getComponentType());
        char[] array1 = new char[]{1, 2, 3};
        newArray = ArrayUtils.add(array1, (char)0);
        assertTrue(Arrays.equals(new char[]{1, 2, 3, 0}, newArray));
        assertEquals(Character.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(array1, (char)4);
        assertTrue(Arrays.equals(new char[]{1, 2, 3, 4}, newArray));
        assertEquals(Character.TYPE, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayDouble
    public void testAddObjectArrayDouble() {
        double[] newArray;
        newArray = ArrayUtils.add((double[])null, 0);
        assertTrue(Arrays.equals(new double[]{0}, newArray));
        assertEquals(Double.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add((double[])null, 1);
        assertTrue(Arrays.equals(new double[]{1}, newArray));
        assertEquals(Double.TYPE, newArray.getClass().getComponentType());
        double[] array1 = new double[]{1, 2, 3};
        newArray = ArrayUtils.add(array1, 0);
        assertTrue(Arrays.equals(new double[]{1, 2, 3, 0}, newArray));
        assertEquals(Double.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(array1, 4);
        assertTrue(Arrays.equals(new double[]{1, 2, 3, 4}, newArray));
        assertEquals(Double.TYPE, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayFloat
    public void testAddObjectArrayFloat() {
        float[] newArray;
        newArray = ArrayUtils.add((float[])null, 0);
        assertTrue(Arrays.equals(new float[]{0}, newArray));
        assertEquals(Float.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add((float[])null, 1);
        assertTrue(Arrays.equals(new float[]{1}, newArray));
        assertEquals(Float.TYPE, newArray.getClass().getComponentType());
        float[] array1 = new float[]{1, 2, 3};
        newArray = ArrayUtils.add(array1, 0);
        assertTrue(Arrays.equals(new float[]{1, 2, 3, 0}, newArray));
        assertEquals(Float.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(array1, 4);
        assertTrue(Arrays.equals(new float[]{1, 2, 3, 4}, newArray));
        assertEquals(Float.TYPE, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayInt
    public void testAddObjectArrayInt() {
        int[] newArray;
        newArray = ArrayUtils.add((int[])null, 0);
        assertTrue(Arrays.equals(new int[]{0}, newArray));
        assertEquals(Integer.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add((int[])null, 1);
        assertTrue(Arrays.equals(new int[]{1}, newArray));
        assertEquals(Integer.TYPE, newArray.getClass().getComponentType());
        int[] array1 = new int[]{1, 2, 3};
        newArray = ArrayUtils.add(array1, 0);
        assertTrue(Arrays.equals(new int[]{1, 2, 3, 0}, newArray));
        assertEquals(Integer.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(array1, 4);
        assertTrue(Arrays.equals(new int[]{1, 2, 3, 4}, newArray));
        assertEquals(Integer.TYPE, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayLong
    public void testAddObjectArrayLong() {
        long[] newArray;
        newArray = ArrayUtils.add((long[])null, 0);
        assertTrue(Arrays.equals(new long[]{0}, newArray));
        assertEquals(Long.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add((long[])null, 1);
        assertTrue(Arrays.equals(new long[]{1}, newArray));
        assertEquals(Long.TYPE, newArray.getClass().getComponentType());
        long[] array1 = new long[]{1, 2, 3};
        newArray = ArrayUtils.add(array1, 0);
        assertTrue(Arrays.equals(new long[]{1, 2, 3, 0}, newArray));
        assertEquals(Long.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(array1, 4);
        assertTrue(Arrays.equals(new long[]{1, 2, 3, 4}, newArray));
        assertEquals(Long.TYPE, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayShort
    public void testAddObjectArrayShort() {
        short[] newArray;
        newArray = ArrayUtils.add((short[])null, (short)0);
        assertTrue(Arrays.equals(new short[]{0}, newArray));
        assertEquals(Short.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add((short[])null, (short)1);
        assertTrue(Arrays.equals(new short[]{1}, newArray));
        assertEquals(Short.TYPE, newArray.getClass().getComponentType());
        short[] array1 = new short[]{1, 2, 3};
        newArray = ArrayUtils.add(array1, (short)0);
        assertTrue(Arrays.equals(new short[]{1, 2, 3, 0}, newArray));
        assertEquals(Short.TYPE, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(array1, (short)4);
        assertTrue(Arrays.equals(new short[]{1, 2, 3, 4}, newArray));
        assertEquals(Short.TYPE, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayObject
    public void testAddObjectArrayObject() {
        Object[] newArray;
        newArray = ArrayUtils.add((Object[])null, null);
        assertTrue(Arrays.equals((new Object[]{null}), newArray));
        assertEquals(Object.class, newArray.getClass().getComponentType());

        
        newArray = ArrayUtils.add(null, null);
        assertTrue(Arrays.equals((new Object[]{null}), newArray));
        assertEquals(Object.class, newArray.getClass().getComponentType());

        newArray = ArrayUtils.add((Object[])null, "a");
        assertTrue(Arrays.equals((new String[]{"a"}), newArray));
        assertTrue(Arrays.equals((new Object[]{"a"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());

        
        String[] newStringArray = ArrayUtils.add(null, "a");
        assertTrue(Arrays.equals((new String[]{"a"}), newStringArray));
        assertTrue(Arrays.equals((new Object[]{"a"}), newStringArray));
        assertEquals(String.class, newStringArray.getClass().getComponentType());

        String[] stringArray1 = new String[]{"a", "b", "c"};
        newArray = ArrayUtils.add(stringArray1, null);
        assertTrue(Arrays.equals((new String[]{"a", "b", "c", null}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());

        newArray = ArrayUtils.add(stringArray1, "d");
        assertTrue(Arrays.equals((new String[]{"a", "b", "c", "d"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());

        Number[] numberArray1 = new Number[]{new Integer(1), new Double(2)};
        newArray = ArrayUtils.add(numberArray1, new Float(3));
        assertTrue(Arrays.equals((new Number[]{new Integer(1), new Double(2), new Float(3)}), newArray));
        assertEquals(Number.class, newArray.getClass().getComponentType());

        numberArray1 = null;
        newArray = ArrayUtils.add(numberArray1, new Float(3));
        assertTrue(Arrays.equals((new Float[]{new Float(3)}), newArray));
        assertEquals(Float.class, newArray.getClass().getComponentType());

        newArray = ArrayUtils.add(numberArray1, null);
        assertTrue(Arrays.equals((new Object[]{null}), newArray));
        assertEquals(Object.class, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectArrayToObjectArray
    public void testAddObjectArrayToObjectArray() {
        assertNull(ArrayUtils.addAll((Object[]) null, (Object[]) null));
        Object[] newArray;
        String[] stringArray1 = new String[]{"a", "b", "c"};
        String[] stringArray2 = new String[]{"1", "2", "3"};
        newArray = ArrayUtils.addAll(stringArray1, (String[]) null);
        assertNotSame(stringArray1, newArray);
        assertTrue(Arrays.equals(stringArray1, newArray));
        assertTrue(Arrays.equals((new String[]{"a", "b", "c"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        newArray = ArrayUtils.addAll(null, stringArray2);
        assertNotSame(stringArray2, newArray);
        assertTrue(Arrays.equals(stringArray2, newArray));
        assertTrue(Arrays.equals((new String[]{"1", "2", "3"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        newArray = ArrayUtils.addAll(stringArray1, stringArray2);
        assertTrue(Arrays.equals((new String[]{"a", "b", "c", "1", "2", "3"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        newArray = ArrayUtils.addAll(ArrayUtils.EMPTY_STRING_ARRAY, (String[]) null);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_STRING_ARRAY, newArray));
        assertTrue(Arrays.equals((new String[]{}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        newArray = ArrayUtils.addAll(null, ArrayUtils.EMPTY_STRING_ARRAY);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_STRING_ARRAY, newArray));
        assertTrue(Arrays.equals((new String[]{}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        newArray = ArrayUtils.addAll(ArrayUtils.EMPTY_STRING_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_STRING_ARRAY, newArray));
        assertTrue(Arrays.equals((new String[]{}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        String[] stringArrayNull = new String []{null};
        newArray = ArrayUtils.addAll(stringArrayNull, stringArrayNull);
        assertTrue(Arrays.equals((new String[]{null, null}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());

        
        assertTrue( Arrays.equals( new boolean[] { true, false, false, true },
            ArrayUtils.addAll( new boolean[] { true, false }, new boolean[] { false, true } ) ) );

        assertTrue( Arrays.equals( new boolean[] { false, true },
            ArrayUtils.addAll( null, new boolean[] { false, true } ) ) );

        assertTrue( Arrays.equals( new boolean[] { true, false },
            ArrayUtils.addAll( new boolean[] { true, false }, null ) ) );

        
        assertTrue( Arrays.equals( new char[] { 'a', 'b', 'c', 'd' },
            ArrayUtils.addAll( new char[] { 'a', 'b' }, new char[] { 'c', 'd' } ) ) );

        assertTrue( Arrays.equals( new char[] { 'c', 'd' },
            ArrayUtils.addAll( null, new char[] { 'c', 'd' } ) ) );

        assertTrue( Arrays.equals( new char[] { 'a', 'b' },
            ArrayUtils.addAll( new char[] { 'a', 'b' }, null ) ) );

        
        assertTrue( Arrays.equals( new byte[] { (byte) 0, (byte) 1, (byte) 2, (byte) 3 },
            ArrayUtils.addAll( new byte[] { (byte) 0, (byte) 1 }, new byte[] { (byte) 2, (byte) 3 } ) ) );

        assertTrue( Arrays.equals( new byte[] { (byte) 2, (byte) 3 },
            ArrayUtils.addAll( null, new byte[] { (byte) 2, (byte) 3 } ) ) );

        assertTrue( Arrays.equals( new byte[] { (byte) 0, (byte) 1 },
            ArrayUtils.addAll( new byte[] { (byte) 0, (byte) 1 }, null ) ) );

        
        assertTrue( Arrays.equals( new short[] { (short) 10, (short) 20, (short) 30, (short) 40 },
            ArrayUtils.addAll( new short[] { (short) 10, (short) 20 }, new short[] { (short) 30, (short) 40 } ) ) );

        assertTrue( Arrays.equals( new short[] { (short) 30, (short) 40 },
            ArrayUtils.addAll( null, new short[] { (short) 30, (short) 40 } ) ) );

        assertTrue( Arrays.equals( new short[] { (short) 10, (short) 20 },
            ArrayUtils.addAll( new short[] { (short) 10, (short) 20 }, null ) ) );

        
        assertTrue( Arrays.equals( new int[] { 1, 1000, -1000, -1 },
            ArrayUtils.addAll( new int[] { 1, 1000 }, new int[] { -1000, -1 } ) ) );

        assertTrue( Arrays.equals( new int[] { -1000, -1 },
            ArrayUtils.addAll( null, new int[] { -1000, -1 } ) ) );

        assertTrue( Arrays.equals( new int[] { 1, 1000 },
            ArrayUtils.addAll( new int[] { 1, 1000 }, null ) ) );

        
        assertTrue( Arrays.equals( new long[] { 1L, -1L, 1000L, -1000L },
            ArrayUtils.addAll( new long[] { 1L, -1L }, new long[] { 1000L, -1000L } ) ) );

        assertTrue( Arrays.equals( new long[] { 1000L, -1000L },
            ArrayUtils.addAll( null, new long[] { 1000L, -1000L } ) ) );

        assertTrue( Arrays.equals( new long[] { 1L, -1L },
            ArrayUtils.addAll( new long[] { 1L, -1L }, null ) ) );

        
        assertTrue( Arrays.equals( new float[] { 10.5f, 10.1f, 1.6f, 0.01f },
            ArrayUtils.addAll( new float[] { 10.5f, 10.1f }, new float[] { 1.6f, 0.01f } ) ) );

        assertTrue( Arrays.equals( new float[] { 1.6f, 0.01f },
            ArrayUtils.addAll( null, new float[] { 1.6f, 0.01f } ) ) );

        assertTrue( Arrays.equals( new float[] { 10.5f, 10.1f },
            ArrayUtils.addAll( new float[] { 10.5f, 10.1f }, null ) ) );

        
        assertTrue( Arrays.equals( new double[] { Math.PI, -Math.PI, 0, 9.99 },
            ArrayUtils.addAll( new double[] { Math.PI, -Math.PI }, new double[] { 0, 9.99 } ) ) );

        assertTrue( Arrays.equals( new double[] { 0, 9.99 },
            ArrayUtils.addAll( null, new double[] { 0, 9.99 } ) ) );

        assertTrue( Arrays.equals( new double[] { Math.PI, -Math.PI },
            ArrayUtils.addAll( new double[] { Math.PI, -Math.PI }, null ) ) );

    }

// org.apache.commons.lang3.ArrayUtilsAddTest::testAddObjectAtIndex
    public void testAddObjectAtIndex() {
        Object[] newArray;
        newArray = ArrayUtils.add((Object[])null, 0, null);
        assertTrue(Arrays.equals((new Object[]{null}), newArray));
        assertEquals(Object.class, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add((Object[])null, 0, "a");
        assertTrue(Arrays.equals((new String[]{"a"}), newArray));
        assertTrue(Arrays.equals((new Object[]{"a"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        String[] stringArray1 = new String[]{"a", "b", "c"};
        newArray = ArrayUtils.add(stringArray1, 0, null);
        assertTrue(Arrays.equals((new String[]{null, "a", "b", "c"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(stringArray1, 1, null);
        assertTrue(Arrays.equals((new String[]{"a", null, "b", "c"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(stringArray1, 3, null);
        assertTrue(Arrays.equals((new String[]{"a", "b", "c", null}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        newArray = ArrayUtils.add(stringArray1, 3, "d");
        assertTrue(Arrays.equals((new String[]{"a", "b", "c", "d"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        assertEquals(String.class, newArray.getClass().getComponentType());

        Object[] o = new Object[] {"1", "2", "4"};
        Object[] result = ArrayUtils.add(o, 2, "3");
        Object[] result2 = ArrayUtils.add(o, 3, "5");

        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("1", result[0]);
        assertEquals("2", result[1]);
        assertEquals("3", result[2]);
        assertEquals("4", result[3]);
        assertNotNull(result2);
        assertEquals(4, result2.length);
        assertEquals("1", result2[0]);
        assertEquals("2", result2[1]);
        assertEquals("4", result2[2]);
        assertEquals("5", result2[3]);

        
        boolean[] booleanArray = ArrayUtils.add( null, 0, true );
        assertTrue( Arrays.equals( new boolean[] { true }, booleanArray ) );
        try {
            booleanArray = ArrayUtils.add( null, -1, true );
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 0", e.getMessage());
        }
        booleanArray = ArrayUtils.add( new boolean[] { true }, 0, false);
        assertTrue( Arrays.equals( new boolean[] { false, true }, booleanArray ) );
        booleanArray = ArrayUtils.add( new boolean[] { false }, 1, true);
        assertTrue( Arrays.equals( new boolean[] { false, true }, booleanArray ) );
        booleanArray = ArrayUtils.add( new boolean[] { true, false }, 1, true);
        assertTrue( Arrays.equals( new boolean[] { true, true, false }, booleanArray ) );
        try {
            booleanArray = ArrayUtils.add( new boolean[] { true, false }, 4, true);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: 4, Length: 2", e.getMessage());
        }
        try {
            booleanArray = ArrayUtils.add( new boolean[] { true, false }, -1, true);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 2", e.getMessage());
        }

        
        char[] charArray = ArrayUtils.add( (char[]) null, 0, 'a' );
        assertTrue( Arrays.equals( new char[] { 'a' }, charArray ) );
        try {
            charArray = ArrayUtils.add( (char[]) null, -1, 'a' );
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 0", e.getMessage());
        }
        charArray = ArrayUtils.add( new char[] { 'a' }, 0, 'b');
        assertTrue( Arrays.equals( new char[] { 'b', 'a' }, charArray ) );
        charArray = ArrayUtils.add( new char[] { 'a', 'b' }, 0, 'c');
        assertTrue( Arrays.equals( new char[] { 'c', 'a', 'b' }, charArray ) );
        charArray = ArrayUtils.add( new char[] { 'a', 'b' }, 1, 'k');
        assertTrue( Arrays.equals( new char[] { 'a', 'k', 'b' }, charArray ) );
        charArray = ArrayUtils.add( new char[] { 'a', 'b', 'c' }, 1, 't');
        assertTrue( Arrays.equals( new char[] { 'a', 't', 'b', 'c' }, charArray ) );
        try {
            charArray = ArrayUtils.add( new char[] { 'a', 'b' }, 4, 'c');
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: 4, Length: 2", e.getMessage());
        }
        try {
            charArray = ArrayUtils.add( new char[] { 'a', 'b' }, -1, 'c');
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 2", e.getMessage());
        }

        
        short[] shortArray = ArrayUtils.add( new short[] { 1 }, 0, (short) 2);
        assertTrue( Arrays.equals( new short[] { 2, 1 }, shortArray ) );
        try {
            shortArray = ArrayUtils.add( (short[]) null, -1, (short) 2);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 0", e.getMessage());
        }
        shortArray = ArrayUtils.add( new short[] { 2, 6 }, 2, (short) 10);
        assertTrue( Arrays.equals( new short[] { 2, 6, 10 }, shortArray ) );
        shortArray = ArrayUtils.add( new short[] { 2, 6 }, 0, (short) -4);
        assertTrue( Arrays.equals( new short[] { -4, 2, 6 }, shortArray ) );
        shortArray = ArrayUtils.add( new short[] { 2, 6, 3 }, 2, (short) 1);
        assertTrue( Arrays.equals( new short[] { 2, 6, 1, 3 }, shortArray ) );
        try {
            shortArray = ArrayUtils.add( new short[] { 2, 6 }, 4, (short) 10);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: 4, Length: 2", e.getMessage());
        }
        try {
            shortArray = ArrayUtils.add( new short[] { 2, 6 }, -1, (short) 10);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 2", e.getMessage());
        }

        
        byte[] byteArray = ArrayUtils.add( new byte[] { 1 }, 0, (byte) 2);
        assertTrue( Arrays.equals( new byte[] { 2, 1 }, byteArray ) );
        try {
            byteArray = ArrayUtils.add( (byte[]) null, -1, (byte) 2);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 0", e.getMessage());
        }
        byteArray = ArrayUtils.add( new byte[] { 2, 6 }, 2, (byte) 3);
        assertTrue( Arrays.equals( new byte[] { 2, 6, 3 }, byteArray ) );
        byteArray = ArrayUtils.add( new byte[] { 2, 6 }, 0, (byte) 1);
        assertTrue( Arrays.equals( new byte[] { 1, 2, 6 }, byteArray ) );
        byteArray = ArrayUtils.add( new byte[] { 2, 6, 3 }, 2, (byte) 1);
        assertTrue( Arrays.equals( new byte[] { 2, 6, 1, 3 }, byteArray ) );
        try {
            byteArray = ArrayUtils.add( new byte[] { 2, 6 }, 4, (byte) 3);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: 4, Length: 2", e.getMessage());
        }
        try {
            byteArray = ArrayUtils.add( new byte[] { 2, 6 }, -1, (byte) 3);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 2", e.getMessage());
        }

        
        int[] intArray = ArrayUtils.add( new int[] { 1 }, 0, 2);
        assertTrue( Arrays.equals( new int[] { 2, 1 }, intArray ) );
        try {
            intArray = ArrayUtils.add( (int[]) null, -1, 2);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 0", e.getMessage());
        }
        intArray = ArrayUtils.add( new int[] { 2, 6 }, 2, 10);
        assertTrue( Arrays.equals( new int[] { 2, 6, 10 }, intArray ) );
        intArray = ArrayUtils.add( new int[] { 2, 6 }, 0, -4);
        assertTrue( Arrays.equals( new int[] { -4, 2, 6 }, intArray ) );
        intArray = ArrayUtils.add( new int[] { 2, 6, 3 }, 2, 1);
        assertTrue( Arrays.equals( new int[] { 2, 6, 1, 3 }, intArray ) );
        try {
            intArray = ArrayUtils.add( new int[] { 2, 6 }, 4, 10);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: 4, Length: 2", e.getMessage());
        }
        try {
            intArray = ArrayUtils.add( new int[] { 2, 6 }, -1, 10);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 2", e.getMessage());
        }

        
        long[] longArray = ArrayUtils.add( new long[] { 1L }, 0, 2L);
        assertTrue( Arrays.equals( new long[] { 2L, 1L }, longArray ) );
        try {
            longArray = ArrayUtils.add( (long[]) null, -1, 2L);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 0", e.getMessage());
        }
        longArray = ArrayUtils.add( new long[] { 2L, 6L }, 2, 10L);
        assertTrue( Arrays.equals( new long[] { 2L, 6L, 10L }, longArray ) );
        longArray = ArrayUtils.add( new long[] { 2L, 6L }, 0, -4L);
        assertTrue( Arrays.equals( new long[] { -4L, 2L, 6L }, longArray ) );
        longArray = ArrayUtils.add( new long[] { 2L, 6L, 3L }, 2, 1L);
        assertTrue( Arrays.equals( new long[] { 2L, 6L, 1L, 3L }, longArray ) );
        try {
            longArray = ArrayUtils.add( new long[] { 2L, 6L }, 4, 10L);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: 4, Length: 2", e.getMessage());
        }
        try {
            longArray = ArrayUtils.add( new long[] { 2L, 6L }, -1, 10L);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 2", e.getMessage());
        }

        
        float[] floatArray = ArrayUtils.add( new float[] { 1.1f }, 0, 2.2f);
        assertTrue( Arrays.equals( new float[] { 2.2f, 1.1f }, floatArray ) );
        try {
            floatArray = ArrayUtils.add( (float[]) null, -1, 2.2f);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 0", e.getMessage());
        }
        floatArray = ArrayUtils.add( new float[] { 2.3f, 6.4f }, 2, 10.5f);
        assertTrue( Arrays.equals( new float[] { 2.3f, 6.4f, 10.5f }, floatArray ) );
        floatArray = ArrayUtils.add( new float[] { 2.6f, 6.7f }, 0, -4.8f);
        assertTrue( Arrays.equals( new float[] { -4.8f, 2.6f, 6.7f }, floatArray ) );
        floatArray = ArrayUtils.add( new float[] { 2.9f, 6.0f, 0.3f }, 2, 1.0f);
        assertTrue( Arrays.equals( new float[] { 2.9f, 6.0f, 1.0f, 0.3f }, floatArray ) );
        try {
            floatArray = ArrayUtils.add( new float[] { 2.3f, 6.4f }, 4, 10.5f);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: 4, Length: 2", e.getMessage());
        }
        try {
            floatArray = ArrayUtils.add( new float[] { 2.3f, 6.4f }, -1, 10.5f);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 2", e.getMessage());
        }

        
        double[] doubleArray = ArrayUtils.add( new double[] { 1.1 }, 0, 2.2);
        assertTrue( Arrays.equals( new double[] { 2.2, 1.1 }, doubleArray ) );
        try {
          doubleArray = ArrayUtils.add( (double[]) null, -1, 2.2);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 0", e.getMessage());
        }
        doubleArray = ArrayUtils.add( new double[] { 2.3, 6.4 }, 2, 10.5);
        assertTrue( Arrays.equals( new double[] { 2.3, 6.4, 10.5 }, doubleArray ) );
        doubleArray = ArrayUtils.add( new double[] { 2.6, 6.7 }, 0, -4.8);
        assertTrue( Arrays.equals( new double[] { -4.8, 2.6, 6.7 }, doubleArray ) );
        doubleArray = ArrayUtils.add( new double[] { 2.9, 6.0, 0.3 }, 2, 1.0);
        assertTrue( Arrays.equals( new double[] { 2.9, 6.0, 1.0, 0.3 }, doubleArray ) );
        try {
            doubleArray = ArrayUtils.add( new double[] { 2.3, 6.4 }, 4, 10.5);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: 4, Length: 2", e.getMessage());
        }
        try {
            doubleArray = ArrayUtils.add( new double[] { 2.3, 6.4 }, -1, 10.5);
        } catch(IndexOutOfBoundsException e) {
            assertEquals("Index: -1, Length: 2", e.getMessage());
        }
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveObjectArray
    public void testRemoveObjectArray() {
        Object[] array;
        array = ArrayUtils.remove(new Object[] {"a"}, 0);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_OBJECT_ARRAY, array));
        assertEquals(Object.class, array.getClass().getComponentType());
        array = ArrayUtils.remove(new Object[] {"a", "b"}, 0);
        assertTrue(Arrays.equals(new Object[] {"b"}, array));
        assertEquals(Object.class, array.getClass().getComponentType());
        array = ArrayUtils.remove(new Object[] {"a", "b"}, 1);
        assertTrue(Arrays.equals(new Object[] {"a"}, array));
        assertEquals(Object.class, array.getClass().getComponentType());
        array = ArrayUtils.remove(new Object[] {"a", "b", "c"}, 1);
        assertTrue(Arrays.equals(new Object[] {"a", "c"}, array));
        assertEquals(Object.class, array.getClass().getComponentType());
        try {
            ArrayUtils.remove(new Object[] {"a", "b"}, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove(new Object[] {"a", "b"}, 2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove((Object[]) null, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveBooleanArray
    public void testRemoveBooleanArray() {
        boolean[] array;
        array = ArrayUtils.remove(new boolean[] {true}, 0);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_BOOLEAN_ARRAY, array));
        assertEquals(Boolean.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new boolean[] {true, false}, 0);
        assertTrue(Arrays.equals(new boolean[] {false}, array));
        assertEquals(Boolean.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new boolean[] {true, false}, 1);
        assertTrue(Arrays.equals(new boolean[] {true}, array));
        assertEquals(Boolean.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new boolean[] {true, false, true}, 1);
        assertTrue(Arrays.equals(new boolean[] {true, true}, array));
        assertEquals(Boolean.TYPE, array.getClass().getComponentType());
        try {
            ArrayUtils.remove(new boolean[] {true, false}, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove(new boolean[] {true, false}, 2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove((boolean[]) null, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveByteArray
    public void testRemoveByteArray() {
        byte[] array;
        array = ArrayUtils.remove(new byte[] {1}, 0);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_BYTE_ARRAY, array));
        assertEquals(Byte.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new byte[] {1, 2}, 0);
        assertTrue(Arrays.equals(new byte[] {2}, array));
        assertEquals(Byte.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new byte[] {1, 2}, 1);
        assertTrue(Arrays.equals(new byte[] {1}, array));
        assertEquals(Byte.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new byte[] {1, 2, 1}, 1);
        assertTrue(Arrays.equals(new byte[] {1, 1}, array));
        assertEquals(Byte.TYPE, array.getClass().getComponentType());
        try {
            ArrayUtils.remove(new byte[] {1, 2}, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove(new byte[] {1, 2}, 2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove((byte[]) null, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveCharArray
    public void testRemoveCharArray() {
        char[] array;
        array = ArrayUtils.remove(new char[] {'a'}, 0);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_CHAR_ARRAY, array));
        assertEquals(Character.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new char[] {'a', 'b'}, 0);
        assertTrue(Arrays.equals(new char[] {'b'}, array));
        assertEquals(Character.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new char[] {'a', 'b'}, 1);
        assertTrue(Arrays.equals(new char[] {'a'}, array));
        assertEquals(Character.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new char[] {'a', 'b', 'c'}, 1);
        assertTrue(Arrays.equals(new char[] {'a', 'c'}, array));
        assertEquals(Character.TYPE, array.getClass().getComponentType());
        try {
            ArrayUtils.remove(new char[] {'a', 'b'}, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove(new char[] {'a', 'b'}, 2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove((char[]) null, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveDoubleArray
    public void testRemoveDoubleArray() {
        double[] array;
        array = ArrayUtils.remove(new double[] {1}, 0);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_DOUBLE_ARRAY, array));
        assertEquals(Double.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new double[] {1, 2}, 0);
        assertTrue(Arrays.equals(new double[] {2}, array));
        assertEquals(Double.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new double[] {1, 2}, 1);
        assertTrue(Arrays.equals(new double[] {1}, array));
        assertEquals(Double.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new double[] {1, 2, 1}, 1);
        assertTrue(Arrays.equals(new double[] {1, 1}, array));
        assertEquals(Double.TYPE, array.getClass().getComponentType());
        try {
            ArrayUtils.remove(new double[] {1, 2}, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove(new double[] {1, 2}, 2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove((double[]) null, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveFloatArray
    public void testRemoveFloatArray() {
        float[] array;
        array = ArrayUtils.remove(new float[] {1}, 0);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_FLOAT_ARRAY, array));
        assertEquals(Float.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new float[] {1, 2}, 0);
        assertTrue(Arrays.equals(new float[] {2}, array));
        assertEquals(Float.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new float[] {1, 2}, 1);
        assertTrue(Arrays.equals(new float[] {1}, array));
        assertEquals(Float.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new float[] {1, 2, 1}, 1);
        assertTrue(Arrays.equals(new float[] {1, 1}, array));
        assertEquals(Float.TYPE, array.getClass().getComponentType());
        try {
            ArrayUtils.remove(new float[] {1, 2}, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove(new float[] {1, 2}, 2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove((float[]) null, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveIntArray
    public void testRemoveIntArray() {
        int[] array;
        array = ArrayUtils.remove(new int[] {1}, 0);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_INT_ARRAY, array));
        assertEquals(Integer.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new int[] {1, 2}, 0);
        assertTrue(Arrays.equals(new int[] {2}, array));
        assertEquals(Integer.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new int[] {1, 2}, 1);
        assertTrue(Arrays.equals(new int[] {1}, array));
        assertEquals(Integer.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new int[] {1, 2, 1}, 1);
        assertTrue(Arrays.equals(new int[] {1, 1}, array));
        assertEquals(Integer.TYPE, array.getClass().getComponentType());
        try {
            ArrayUtils.remove(new int[] {1, 2}, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove(new int[] {1, 2}, 2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove((int[]) null, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveLongArray
    public void testRemoveLongArray() {
        long[] array;
        array = ArrayUtils.remove(new long[] {1}, 0);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_LONG_ARRAY, array));
        assertEquals(Long.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new long[] {1, 2}, 0);
        assertTrue(Arrays.equals(new long[] {2}, array));
        assertEquals(Long.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new long[] {1, 2}, 1);
        assertTrue(Arrays.equals(new long[] {1}, array));
        assertEquals(Long.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new long[] {1, 2, 1}, 1);
        assertTrue(Arrays.equals(new long[] {1, 1}, array));
        assertEquals(Long.TYPE, array.getClass().getComponentType());
        try {
            ArrayUtils.remove(new long[] {1, 2}, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove(new long[] {1, 2}, 2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove((long[]) null, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveShortArray
    public void testRemoveShortArray() {
        short[] array;
        array = ArrayUtils.remove(new short[] {1}, 0);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_SHORT_ARRAY, array));
        assertEquals(Short.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new short[] {1, 2}, 0);
        assertTrue(Arrays.equals(new short[] {2}, array));
        assertEquals(Short.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new short[] {1, 2}, 1);
        assertTrue(Arrays.equals(new short[] {1}, array));
        assertEquals(Short.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.remove(new short[] {1, 2, 1}, 1);
        assertTrue(Arrays.equals(new short[] {1, 1}, array));
        assertEquals(Short.TYPE, array.getClass().getComponentType());
        try {
            ArrayUtils.remove(new short[] {1, 2}, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove(new short[] {1, 2}, 2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        try {
            ArrayUtils.remove((short[]) null, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveElementObjectArray
    public void testRemoveElementObjectArray() {
        Object[] array;
        array = ArrayUtils.removeElement((Object[]) null, "a");
        assertNull(array);
        array = ArrayUtils.removeElement(ArrayUtils.EMPTY_OBJECT_ARRAY, "a");
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_OBJECT_ARRAY, array));
        assertEquals(Object.class, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new Object[] {"a"}, "a");
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_OBJECT_ARRAY, array));
        assertEquals(Object.class, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new Object[] {"a", "b"}, "a");
        assertTrue(Arrays.equals(new Object[] {"b"}, array));
        assertEquals(Object.class, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new Object[] {"a", "b", "a"}, "a");
        assertTrue(Arrays.equals(new Object[] {"b", "a"}, array));
        assertEquals(Object.class, array.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveElementBooleanArray
    public void testRemoveElementBooleanArray() {
        boolean[] array;
        array = ArrayUtils.removeElement((boolean[]) null, true);
        assertNull(array);
        array = ArrayUtils.removeElement(ArrayUtils.EMPTY_BOOLEAN_ARRAY, true);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_BOOLEAN_ARRAY, array));
        assertEquals(Boolean.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new boolean[] {true}, true);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_BOOLEAN_ARRAY, array));
        assertEquals(Boolean.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new boolean[] {true, false}, true);
        assertTrue(Arrays.equals(new boolean[] {false}, array));
        assertEquals(Boolean.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new boolean[] {true, false, true}, true);
        assertTrue(Arrays.equals(new boolean[] {false, true}, array));
        assertEquals(Boolean.TYPE, array.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveElementByteArray
    public void testRemoveElementByteArray() {
        byte[] array;
        array = ArrayUtils.removeElement((byte[]) null, (byte) 1);
        assertNull(array);
        array = ArrayUtils.removeElement(ArrayUtils.EMPTY_BYTE_ARRAY, (byte) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_BYTE_ARRAY, array));
        assertEquals(Byte.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new byte[] {1}, (byte) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_BYTE_ARRAY, array));
        assertEquals(Byte.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new byte[] {1, 2}, (byte) 1);
        assertTrue(Arrays.equals(new byte[] {2}, array));
        assertEquals(Byte.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new byte[] {1, 2, 1}, (byte) 1);
        assertTrue(Arrays.equals(new byte[] {2, 1}, array));
        assertEquals(Byte.TYPE, array.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveElementCharArray
    public void testRemoveElementCharArray() {
        char[] array;
        array = ArrayUtils.removeElement((char[]) null, 'a');
        assertNull(array);
        array = ArrayUtils.removeElement(ArrayUtils.EMPTY_CHAR_ARRAY, 'a');
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_CHAR_ARRAY, array));
        assertEquals(Character.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new char[] {'a'}, 'a');
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_CHAR_ARRAY, array));
        assertEquals(Character.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new char[] {'a', 'b'}, 'a');
        assertTrue(Arrays.equals(new char[] {'b'}, array));
        assertEquals(Character.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new char[] {'a', 'b', 'a'}, 'a');
        assertTrue(Arrays.equals(new char[] {'b', 'a'}, array));
        assertEquals(Character.TYPE, array.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveElementDoubleArray
    public void testRemoveElementDoubleArray() {
        double[] array;
        array = ArrayUtils.removeElement((double[]) null, (double) 1);
        assertNull(array);
        array = ArrayUtils.removeElement(ArrayUtils.EMPTY_DOUBLE_ARRAY, (double) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_DOUBLE_ARRAY, array));
        assertEquals(Double.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new double[] {1}, (double) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_DOUBLE_ARRAY, array));
        assertEquals(Double.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new double[] {1, 2}, (double) 1);
        assertTrue(Arrays.equals(new double[] {2}, array));
        assertEquals(Double.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new double[] {1, 2, 1}, (double) 1);
        assertTrue(Arrays.equals(new double[] {2, 1}, array));
        assertEquals(Double.TYPE, array.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveElementFloatArray
    public void testRemoveElementFloatArray() {
        float[] array;
        array = ArrayUtils.removeElement((float[]) null, (float) 1);
        assertNull(array);
        array = ArrayUtils.removeElement(ArrayUtils.EMPTY_FLOAT_ARRAY, (float) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_FLOAT_ARRAY, array));
        assertEquals(Float.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new float[] {1}, (float) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_FLOAT_ARRAY, array));
        assertEquals(Float.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new float[] {1, 2}, (float) 1);
        assertTrue(Arrays.equals(new float[] {2}, array));
        assertEquals(Float.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new float[] {1, 2, 1}, (float) 1);
        assertTrue(Arrays.equals(new float[] {2, 1}, array));
        assertEquals(Float.TYPE, array.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveElementIntArray
    public void testRemoveElementIntArray() {
        int[] array;
        array = ArrayUtils.removeElement((int[]) null, 1);
        assertNull(array);
        array = ArrayUtils.removeElement(ArrayUtils.EMPTY_INT_ARRAY, 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_INT_ARRAY, array));
        assertEquals(Integer.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new int[] {1}, 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_INT_ARRAY, array));
        assertEquals(Integer.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new int[] {1, 2}, 1);
        assertTrue(Arrays.equals(new int[] {2}, array));
        assertEquals(Integer.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new int[] {1, 2, 1}, 1);
        assertTrue(Arrays.equals(new int[] {2, 1}, array));
        assertEquals(Integer.TYPE, array.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveElementLongArray
    public void testRemoveElementLongArray() {
        long[] array;
        array = ArrayUtils.removeElement((long[]) null, (long) 1);
        assertNull(array);
        array = ArrayUtils.removeElement(ArrayUtils.EMPTY_LONG_ARRAY, (long) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_LONG_ARRAY, array));
        assertEquals(Long.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new long[] {1}, (long) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_LONG_ARRAY, array));
        assertEquals(Long.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new long[] {1, 2}, (long) 1);
        assertTrue(Arrays.equals(new long[] {2}, array));
        assertEquals(Long.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new long[] {1, 2, 1}, (long) 1);
        assertTrue(Arrays.equals(new long[] {2, 1}, array));
        assertEquals(Long.TYPE, array.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsRemoveTest::testRemoveElementShortArray
    public void testRemoveElementShortArray() {
        short[] array;
        array = ArrayUtils.removeElement((short[]) null, (short) 1);
        assertNull(array);
        array = ArrayUtils.removeElement(ArrayUtils.EMPTY_SHORT_ARRAY, (short) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_SHORT_ARRAY, array));
        assertEquals(Short.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new short[] {1}, (short) 1);
        assertTrue(Arrays.equals(ArrayUtils.EMPTY_SHORT_ARRAY, array));
        assertEquals(Short.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new short[] {1, 2}, (short) 1);
        assertTrue(Arrays.equals(new short[] {2}, array));
        assertEquals(Short.TYPE, array.getClass().getComponentType());
        array = ArrayUtils.removeElement(new short[] {1, 2, 1}, (short) 1);
        assertTrue(Arrays.equals(new short[] {2, 1}, array));
        assertEquals(Short.TYPE, array.getClass().getComponentType());
    }

// org.apache.commons.lang3.ArrayUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new ArrayUtils());
        Constructor<?>[] cons = ArrayUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(ArrayUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ArrayUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToString
    public void testToString() {
        assertEquals("{}", ArrayUtils.toString(null));
        assertEquals("{}", ArrayUtils.toString(new Object[0]));
        assertEquals("{}", ArrayUtils.toString(new String[0]));
        assertEquals("{<null>}", ArrayUtils.toString(new String[] {null}));
        assertEquals("{pink,blue}", ArrayUtils.toString(new String[] {"pink","blue"}));
        
        assertEquals("<empty>", ArrayUtils.toString(null, "<empty>"));
        assertEquals("{}", ArrayUtils.toString(new Object[0], "<empty>"));
        assertEquals("{}", ArrayUtils.toString(new String[0], "<empty>"));
        assertEquals("{<null>}", ArrayUtils.toString(new String[] {null}, "<empty>"));
        assertEquals("{pink,blue}", ArrayUtils.toString(new String[] {"pink","blue"}, "<empty>"));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIsEquals
    public void testIsEquals() {
        long[][] larray1 = new long[][]{{2, 5}, {4, 5}};
        long[][] larray2 = new long[][]{{2, 5}, {4, 6}};
        long[] larray3 = new long[]{2, 5};
        this.assertIsEquals(larray1, larray2, larray3);

        int[][] iarray1 = new int[][]{{2, 5}, {4, 5}};
        int[][] iarray2 = new int[][]{{2, 5}, {4, 6}};
        int[] iarray3 = new int[]{2, 5};
        this.assertIsEquals(iarray1, iarray2, iarray3);

        short[][] sarray1 = new short[][]{{2, 5}, {4, 5}};
        short[][] sarray2 = new short[][]{{2, 5}, {4, 6}};
        short[] sarray3 = new short[]{2, 5};
        this.assertIsEquals(sarray1, sarray2, sarray3);

        float[][] farray1 = new float[][]{{2, 5}, {4, 5}};
        float[][] farray2 = new float[][]{{2, 5}, {4, 6}};
        float[] farray3 = new float[]{2, 5};
        this.assertIsEquals(farray1, farray2, farray3);

        double[][] darray1 = new double[][]{{2, 5}, {4, 5}};
        double[][] darray2 = new double[][]{{2, 5}, {4, 6}};
        double[] darray3 = new double[]{2, 5};
        this.assertIsEquals(darray1, darray2, darray3);

        byte[][] byteArray1 = new byte[][]{{2, 5}, {4, 5}};
        byte[][] byteArray2 = new byte[][]{{2, 5}, {4, 6}};
        byte[] byteArray3 = new byte[]{2, 5};
        this.assertIsEquals(byteArray1, byteArray2, byteArray3);

        char[][] charArray1 = new char[][]{{2, 5}, {4, 5}};
        char[][] charArray2 = new char[][]{{2, 5}, {4, 6}};
        char[] charArray3 = new char[]{2, 5};
        this.assertIsEquals(charArray1, charArray2, charArray3);

        boolean[][] barray1 = new boolean[][]{{true, false}, {true, true}};
        boolean[][] barray2 = new boolean[][]{{true, false}, {true, false}};
        boolean[] barray3 = new boolean[]{false, true};
        this.assertIsEquals(barray1, barray2, barray3);

        Object[] array3 = new Object[]{new String(new char[]{'A', 'B'})};
        Object[] array4 = new Object[]{"AB"};
        assertEquals(true, ArrayUtils.isEquals(array3, array3));
        assertEquals(true, ArrayUtils.isEquals(array3, array4));

        assertEquals(true, ArrayUtils.isEquals(null, null));
        assertEquals(false, ArrayUtils.isEquals(null, array4));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToMap
    public void testToMap() {
        Map<?, ?> map = ArrayUtils.toMap(new String[][] {{"foo", "bar"}, {"hello", "world"}});
        
        assertEquals("bar", map.get("foo"));
        assertEquals("world", map.get("hello"));
        
        assertEquals(null, ArrayUtils.toMap(null));
        try {
            ArrayUtils.toMap(new String[][] {{"foo", "bar"}, {"short"}});
            fail("exception expected");
        } catch (IllegalArgumentException ex) {}
        try {
            ArrayUtils.toMap(new Object[] {new Object[] {"foo", "bar"}, "illegal type"});
            fail("exception expected");
        } catch (IllegalArgumentException ex) {}
        try {
            ArrayUtils.toMap(new Object[] {new Object[] {"foo", "bar"}, null});
            fail("exception expected");
        } catch (IllegalArgumentException ex) {}
        
        map = ArrayUtils.toMap(new Object[] {new Map.Entry<Object, Object>() {
            public Object getKey() {
                return "foo";
            }
            public Object getValue() {
                return "bar";
            }
            public Object setValue(Object value) {
                throw new UnsupportedOperationException();
            }
            @Override
            public boolean equals(Object o) {
                throw new UnsupportedOperationException();
            }
            @Override
            public int hashCode() {
                throw new UnsupportedOperationException();
            }
        }});
        assertEquals("bar", map.get("foo"));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testClone
    public void testClone() {
        assertEquals(null, ArrayUtils.clone((Object[]) null));
        Object[] original1 = new Object[0];
        Object[] cloned1 = ArrayUtils.clone(original1);
        assertTrue(Arrays.equals(original1, cloned1));
        assertTrue(original1 != cloned1);
        
        StringBuffer buf = new StringBuffer("pick");
        original1 = new Object[] {buf, "a", new String[] {"stick"}};
        cloned1 = ArrayUtils.clone(original1);
        assertTrue(Arrays.equals(original1, cloned1));
        assertTrue(original1 != cloned1);
        assertSame(original1[0], cloned1[0]);
        assertSame(original1[1], cloned1[1]);
        assertSame(original1[2], cloned1[2]);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testCloneBoolean
    public void testCloneBoolean() {
        assertEquals(null, ArrayUtils.clone((boolean[]) null));
        boolean[] original = new boolean[] {true, false};
        boolean[] cloned = ArrayUtils.clone(original);
        assertTrue(Arrays.equals(original, cloned));
        assertTrue(original != cloned);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testCloneLong
    public void testCloneLong() {
        assertEquals(null, ArrayUtils.clone((long[]) null));
        long[] original = new long[] {0L, 1L};
        long[] cloned = ArrayUtils.clone(original);
        assertTrue(Arrays.equals(original, cloned));
        assertTrue(original != cloned);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testCloneInt
    public void testCloneInt() {
        assertEquals(null, ArrayUtils.clone((int[]) null));
        int[] original = new int[] {5, 8};
        int[] cloned = ArrayUtils.clone(original);
        assertTrue(Arrays.equals(original, cloned));
        assertTrue(original != cloned);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testCloneShort
    public void testCloneShort() {
        assertEquals(null, ArrayUtils.clone((short[]) null));
        short[] original = new short[] {1, 4};
        short[] cloned = ArrayUtils.clone(original);
        assertTrue(Arrays.equals(original, cloned));
        assertTrue(original != cloned);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testCloneChar
    public void testCloneChar() {
        assertEquals(null, ArrayUtils.clone((char[]) null));
        char[] original = new char[] {'a', '4'};
        char[] cloned = ArrayUtils.clone(original);
        assertTrue(Arrays.equals(original, cloned));
        assertTrue(original != cloned);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testCloneByte
    public void testCloneByte() {
        assertEquals(null, ArrayUtils.clone((byte[]) null));
        byte[] original = new byte[] {1, 6};
        byte[] cloned = ArrayUtils.clone(original);
        assertTrue(Arrays.equals(original, cloned));
        assertTrue(original != cloned);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testCloneDouble
    public void testCloneDouble() {
        assertEquals(null, ArrayUtils.clone((double[]) null));
        double[] original = new double[] {2.4d, 5.7d};
        double[] cloned = ArrayUtils.clone(original);
        assertTrue(Arrays.equals(original, cloned));
        assertTrue(original != cloned);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testCloneFloat
    public void testCloneFloat() {
        assertEquals(null, ArrayUtils.clone((float[]) null));
        float[] original = new float[] {2.6f, 6.4f};
        float[] cloned = ArrayUtils.clone(original);
        assertTrue(Arrays.equals(original, cloned));
        assertTrue(original != cloned);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSubarrayObject
    public void testSubarrayObject() {
        Object[] nullArray = null;
        Object[] objectArray = { "a", "b", "c", "d", "e", "f"};

        assertEquals("0 start, mid end", "abcd",
            StringUtils.join(ArrayUtils.subarray(objectArray, 0, 4)));
        assertEquals("0 start, length end", "abcdef",
            StringUtils.join(ArrayUtils.subarray(objectArray, 0, objectArray.length)));
        assertEquals("mid start, mid end", "bcd",
            StringUtils.join(ArrayUtils.subarray(objectArray, 1, 4)));
        assertEquals("mid start, length end", "bcdef",
            StringUtils.join(ArrayUtils.subarray(objectArray, 1, objectArray.length)));

        assertNull("null input", ArrayUtils.subarray(nullArray, 0, 3));
        assertEquals("empty array", "",
            StringUtils.join(ArrayUtils.subarray(ArrayUtils.EMPTY_OBJECT_ARRAY, 1, 2)));
        assertEquals("start > end", "",
            StringUtils.join(ArrayUtils.subarray(objectArray, 4, 2)));
        assertEquals("start == end", "",
            StringUtils.join(ArrayUtils.subarray(objectArray, 3, 3)));
        assertEquals("start undershoot, normal end", "abcd",
            StringUtils.join(ArrayUtils.subarray(objectArray, -2, 4)));
        assertEquals("start overshoot, any end", "",
            StringUtils.join(ArrayUtils.subarray(objectArray, 33, 4)));
        assertEquals("normal start, end overshoot", "cdef",
            StringUtils.join(ArrayUtils.subarray(objectArray, 2, 33)));
        assertEquals("start undershoot, end overshoot", "abcdef",
            StringUtils.join(ArrayUtils.subarray(objectArray, -2, 12)));
            
        
        Date[] dateArray = { new java.sql.Date(new Date().getTime()),
            new Date(), new Date(), new Date(), new Date() };

        assertSame("Object type", Object.class,
            ArrayUtils.subarray(objectArray, 2, 4).getClass().getComponentType());
        assertSame("java.util.Date type", java.util.Date.class,
            ArrayUtils.subarray(dateArray, 1, 4).getClass().getComponentType());
        assertNotSame("java.sql.Date type", java.sql.Date.class,
            ArrayUtils.subarray(dateArray, 1, 4).getClass().getComponentType());
        try {
            @SuppressWarnings("unused")
            java.sql.Date[] dummy = (java.sql.Date[])ArrayUtils.subarray(dateArray, 1,3);
            fail("Invalid downcast");
        } catch (ClassCastException e) {}
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSubarrayLong
    public void testSubarrayLong() {
        long[] nullArray = null;
        long[] array = { 999910, 999911, 999912, 999913, 999914, 999915 };
        long[] leftSubarray     = { 999910, 999911, 999912, 999913 };
        long[] midSubarray      = { 999911, 999912, 999913, 999914 };
        long[] rightSubarray    = { 999912, 999913, 999914, 999915 };

        assertTrue("0 start, mid end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, 0, 4)));

        assertTrue("0 start, length end",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, 0, array.length)));

        assertTrue("mid start, mid end",
            ArrayUtils.isEquals(midSubarray,
                ArrayUtils.subarray(array, 1, 5)));

        assertTrue("mid start, length end",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, array.length)));

        assertNull("null input", ArrayUtils.subarray(nullArray, 0, 3));

        assertEquals("empty array", ArrayUtils.EMPTY_LONG_ARRAY,
            ArrayUtils.subarray(ArrayUtils.EMPTY_LONG_ARRAY, 1, 2));

        assertEquals("start > end", ArrayUtils.EMPTY_LONG_ARRAY,
            ArrayUtils.subarray(array, 4, 2));

        assertEquals("start == end", ArrayUtils.EMPTY_LONG_ARRAY,
            ArrayUtils.subarray(array, 3, 3));

        assertTrue("start undershoot, normal end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, -2, 4)));

        assertEquals("start overshoot, any end",
            ArrayUtils.EMPTY_LONG_ARRAY,
                ArrayUtils.subarray(array, 33, 4));

        assertTrue("normal start, end overshoot",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, 33)));

        assertTrue("start undershoot, end overshoot",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, -2, 12)));

        

        assertSame("empty array, object test",
            ArrayUtils.EMPTY_LONG_ARRAY,
                ArrayUtils.subarray(ArrayUtils.EMPTY_LONG_ARRAY, 1, 2));

        assertSame("start > end, object test",
            ArrayUtils.EMPTY_LONG_ARRAY,
                ArrayUtils.subarray(array, 4, 1));

        assertSame("start == end, object test",
            ArrayUtils.EMPTY_LONG_ARRAY,
                ArrayUtils.subarray(array, 3, 3));

        assertSame("start overshoot, any end, object test",
            ArrayUtils.EMPTY_LONG_ARRAY,
                ArrayUtils.subarray(array, 8733, 4));

        

        assertSame("long type", long.class,
            ArrayUtils.subarray(array, 2, 4).getClass().getComponentType());

    }

// org.apache.commons.lang3.ArrayUtilsTest::testSubarrayInt
    public void testSubarrayInt() {
        int[] nullArray = null;
        int[] array = { 10, 11, 12, 13, 14, 15 };
        int[] leftSubarray  = { 10, 11, 12, 13 };
        int[] midSubarray   = { 11, 12, 13, 14 };
        int[] rightSubarray = { 12, 13, 14, 15 };

        assertTrue("0 start, mid end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, 0, 4)));

        assertTrue("0 start, length end",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, 0, array.length)));

        assertTrue("mid start, mid end",
            ArrayUtils.isEquals(midSubarray,
                ArrayUtils.subarray(array, 1, 5)));

        assertTrue("mid start, length end",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, array.length)));

        assertNull("null input", ArrayUtils.subarray(nullArray, 0, 3));

        assertEquals("empty array", ArrayUtils.EMPTY_INT_ARRAY,
            ArrayUtils.subarray(ArrayUtils.EMPTY_INT_ARRAY, 1, 2));

        assertEquals("start > end", ArrayUtils.EMPTY_INT_ARRAY,
            ArrayUtils.subarray(array, 4, 2));

        assertEquals("start == end", ArrayUtils.EMPTY_INT_ARRAY,
            ArrayUtils.subarray(array, 3, 3));

        assertTrue("start undershoot, normal end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, -2, 4)));

        assertEquals("start overshoot, any end",
            ArrayUtils.EMPTY_INT_ARRAY,
                ArrayUtils.subarray(array, 33, 4));

        assertTrue("normal start, end overshoot",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, 33)));

        assertTrue("start undershoot, end overshoot",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, -2, 12)));

        

        assertSame("empty array, object test",
            ArrayUtils.EMPTY_INT_ARRAY,
                ArrayUtils.subarray(ArrayUtils.EMPTY_INT_ARRAY, 1, 2));

        assertSame("start > end, object test",
            ArrayUtils.EMPTY_INT_ARRAY,
                ArrayUtils.subarray(array, 4, 1));

        assertSame("start == end, object test",
            ArrayUtils.EMPTY_INT_ARRAY,
                ArrayUtils.subarray(array, 3, 3));

        assertSame("start overshoot, any end, object test",
            ArrayUtils.EMPTY_INT_ARRAY,
                ArrayUtils.subarray(array, 8733, 4));

        

        assertSame("int type", int.class,
            ArrayUtils.subarray(array, 2, 4).getClass().getComponentType());

    }

// org.apache.commons.lang3.ArrayUtilsTest::testSubarrayShort
    public void testSubarrayShort() {
        short[] nullArray = null;
        short[] array = { 10, 11, 12, 13, 14, 15 };
        short[] leftSubarray    = { 10, 11, 12, 13 };
        short[] midSubarray     = { 11, 12, 13, 14 };
        short[] rightSubarray   = { 12, 13, 14, 15 };

        assertTrue("0 start, mid end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, 0, 4)));

        assertTrue("0 start, length end",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, 0, array.length)));

        assertTrue("mid start, mid end",
            ArrayUtils.isEquals(midSubarray,
                ArrayUtils.subarray(array, 1, 5)));

        assertTrue("mid start, length end",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, array.length)));

        assertNull("null input", ArrayUtils.subarray(nullArray, 0, 3));

        assertEquals("empty array", ArrayUtils.EMPTY_SHORT_ARRAY,
            ArrayUtils.subarray(ArrayUtils.EMPTY_SHORT_ARRAY, 1, 2));

        assertEquals("start > end", ArrayUtils.EMPTY_SHORT_ARRAY,
            ArrayUtils.subarray(array, 4, 2));

        assertEquals("start == end", ArrayUtils.EMPTY_SHORT_ARRAY,
            ArrayUtils.subarray(array, 3, 3));

        assertTrue("start undershoot, normal end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, -2, 4)));

        assertEquals("start overshoot, any end",
            ArrayUtils.EMPTY_SHORT_ARRAY,
                ArrayUtils.subarray(array, 33, 4));

        assertTrue("normal start, end overshoot",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, 33)));

        assertTrue("start undershoot, end overshoot",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, -2, 12)));

        

        assertSame("empty array, object test",
            ArrayUtils.EMPTY_SHORT_ARRAY,
                ArrayUtils.subarray(ArrayUtils.EMPTY_SHORT_ARRAY, 1, 2));

        assertSame("start > end, object test",
            ArrayUtils.EMPTY_SHORT_ARRAY,
                ArrayUtils.subarray(array, 4, 1));

        assertSame("start == end, object test",
            ArrayUtils.EMPTY_SHORT_ARRAY,
                ArrayUtils.subarray(array, 3, 3));

        assertSame("start overshoot, any end, object test",
            ArrayUtils.EMPTY_SHORT_ARRAY,
                ArrayUtils.subarray(array, 8733, 4));

        

        assertSame("short type", short.class,
            ArrayUtils.subarray(array, 2, 4).getClass().getComponentType());

    }

// org.apache.commons.lang3.ArrayUtilsTest::testSubarrChar
    public void testSubarrChar() {
        char[] nullArray = null;
        char[] array = { 'a', 'b', 'c', 'd', 'e', 'f' };
        char[] leftSubarray     = { 'a', 'b', 'c', 'd', };
        char[] midSubarray      = { 'b', 'c', 'd', 'e', };
        char[] rightSubarray    = { 'c', 'd', 'e', 'f', };

        assertTrue("0 start, mid end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, 0, 4)));

        assertTrue("0 start, length end",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, 0, array.length)));

        assertTrue("mid start, mid end",
            ArrayUtils.isEquals(midSubarray,
                ArrayUtils.subarray(array, 1, 5)));

        assertTrue("mid start, length end",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, array.length)));

        assertNull("null input", ArrayUtils.subarray(nullArray, 0, 3));

        assertEquals("empty array", ArrayUtils.EMPTY_CHAR_ARRAY,
            ArrayUtils.subarray(ArrayUtils.EMPTY_CHAR_ARRAY, 1, 2));

        assertEquals("start > end", ArrayUtils.EMPTY_CHAR_ARRAY,
            ArrayUtils.subarray(array, 4, 2));

        assertEquals("start == end", ArrayUtils.EMPTY_CHAR_ARRAY,
            ArrayUtils.subarray(array, 3, 3));

        assertTrue("start undershoot, normal end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, -2, 4)));

        assertEquals("start overshoot, any end",
            ArrayUtils.EMPTY_CHAR_ARRAY,
                ArrayUtils.subarray(array, 33, 4));

        assertTrue("normal start, end overshoot",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, 33)));

        assertTrue("start undershoot, end overshoot",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, -2, 12)));

        

        assertSame("empty array, object test",
            ArrayUtils.EMPTY_CHAR_ARRAY,
                ArrayUtils.subarray(ArrayUtils.EMPTY_CHAR_ARRAY, 1, 2));

        assertSame("start > end, object test",
            ArrayUtils.EMPTY_CHAR_ARRAY,
                ArrayUtils.subarray(array, 4, 1));

        assertSame("start == end, object test",
            ArrayUtils.EMPTY_CHAR_ARRAY,
                ArrayUtils.subarray(array, 3, 3));

        assertSame("start overshoot, any end, object test",
            ArrayUtils.EMPTY_CHAR_ARRAY,
                ArrayUtils.subarray(array, 8733, 4));

        

        assertSame("char type", char.class,
            ArrayUtils.subarray(array, 2, 4).getClass().getComponentType());

    }

// org.apache.commons.lang3.ArrayUtilsTest::testSubarrayByte
    public void testSubarrayByte() {
        byte[] nullArray = null;
        byte[] array = { 10, 11, 12, 13, 14, 15 };
        byte[] leftSubarray     = { 10, 11, 12, 13 };
        byte[] midSubarray      = { 11, 12, 13, 14 };
        byte[] rightSubarray = { 12, 13, 14, 15 };

        assertTrue("0 start, mid end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, 0, 4)));

        assertTrue("0 start, length end",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, 0, array.length)));

        assertTrue("mid start, mid end",
            ArrayUtils.isEquals(midSubarray,
                ArrayUtils.subarray(array, 1, 5)));

        assertTrue("mid start, length end",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, array.length)));

        assertNull("null input", ArrayUtils.subarray(nullArray, 0, 3));

        assertEquals("empty array", ArrayUtils.EMPTY_BYTE_ARRAY,
            ArrayUtils.subarray(ArrayUtils.EMPTY_BYTE_ARRAY, 1, 2));

        assertEquals("start > end", ArrayUtils.EMPTY_BYTE_ARRAY,
            ArrayUtils.subarray(array, 4, 2));

        assertEquals("start == end", ArrayUtils.EMPTY_BYTE_ARRAY,
            ArrayUtils.subarray(array, 3, 3));

        assertTrue("start undershoot, normal end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, -2, 4)));

        assertEquals("start overshoot, any end",
            ArrayUtils.EMPTY_BYTE_ARRAY,
                ArrayUtils.subarray(array, 33, 4));

        assertTrue("normal start, end overshoot",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, 33)));

        assertTrue("start undershoot, end overshoot",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, -2, 12)));

        

        assertSame("empty array, object test",
            ArrayUtils.EMPTY_BYTE_ARRAY,
                ArrayUtils.subarray(ArrayUtils.EMPTY_BYTE_ARRAY, 1, 2));

        assertSame("start > end, object test",
            ArrayUtils.EMPTY_BYTE_ARRAY,
                ArrayUtils.subarray(array, 4, 1));

        assertSame("start == end, object test",
            ArrayUtils.EMPTY_BYTE_ARRAY,
                ArrayUtils.subarray(array, 3, 3));

        assertSame("start overshoot, any end, object test",
            ArrayUtils.EMPTY_BYTE_ARRAY,
                ArrayUtils.subarray(array, 8733, 4));

        

        assertSame("byte type", byte.class,
            ArrayUtils.subarray(array, 2, 4).getClass().getComponentType());

    }

// org.apache.commons.lang3.ArrayUtilsTest::testSubarrayDouble
    public void testSubarrayDouble() {
        double[] nullArray = null;
        double[] array = { 10.123, 11.234, 12.345, 13.456, 14.567, 15.678 };
        double[] leftSubarray   = { 10.123, 11.234, 12.345, 13.456, };
        double[] midSubarray    = { 11.234, 12.345, 13.456, 14.567, };
        double[] rightSubarray  = { 12.345, 13.456, 14.567, 15.678 };

        assertTrue("0 start, mid end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, 0, 4)));

        assertTrue("0 start, length end",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, 0, array.length)));

        assertTrue("mid start, mid end",
            ArrayUtils.isEquals(midSubarray,
                ArrayUtils.subarray(array, 1, 5)));

        assertTrue("mid start, length end",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, array.length)));

        assertNull("null input", ArrayUtils.subarray(nullArray, 0, 3));

        assertEquals("empty array", ArrayUtils.EMPTY_DOUBLE_ARRAY,
            ArrayUtils.subarray(ArrayUtils.EMPTY_DOUBLE_ARRAY, 1, 2));

        assertEquals("start > end", ArrayUtils.EMPTY_DOUBLE_ARRAY,
            ArrayUtils.subarray(array, 4, 2));

        assertEquals("start == end", ArrayUtils.EMPTY_DOUBLE_ARRAY,
            ArrayUtils.subarray(array, 3, 3));

        assertTrue("start undershoot, normal end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, -2, 4)));

        assertEquals("start overshoot, any end",
            ArrayUtils.EMPTY_DOUBLE_ARRAY,
                ArrayUtils.subarray(array, 33, 4));

        assertTrue("normal start, end overshoot",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, 33)));

        assertTrue("start undershoot, end overshoot",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, -2, 12)));

        

        assertSame("empty array, object test",
            ArrayUtils.EMPTY_DOUBLE_ARRAY,
                ArrayUtils.subarray(ArrayUtils.EMPTY_DOUBLE_ARRAY, 1, 2));

        assertSame("start > end, object test",
            ArrayUtils.EMPTY_DOUBLE_ARRAY,
                ArrayUtils.subarray(array, 4, 1));

        assertSame("start == end, object test",
            ArrayUtils.EMPTY_DOUBLE_ARRAY,
                ArrayUtils.subarray(array, 3, 3));

        assertSame("start overshoot, any end, object test",
            ArrayUtils.EMPTY_DOUBLE_ARRAY,
                ArrayUtils.subarray(array, 8733, 4));

        

        assertSame("double type", double.class,
            ArrayUtils.subarray(array, 2, 4).getClass().getComponentType());

    }

// org.apache.commons.lang3.ArrayUtilsTest::testSubarrayFloat
    public void testSubarrayFloat() {
        float[] nullArray = null;
        float[] array = { 10, 11, 12, 13, 14, 15 };
        float[] leftSubarray    = { 10, 11, 12, 13 };
        float[] midSubarray     = { 11, 12, 13, 14 };
        float[] rightSubarray   = { 12, 13, 14, 15 };

        assertTrue("0 start, mid end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, 0, 4)));

        assertTrue("0 start, length end",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, 0, array.length)));

        assertTrue("mid start, mid end",
            ArrayUtils.isEquals(midSubarray,
                ArrayUtils.subarray(array, 1, 5)));

        assertTrue("mid start, length end",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, array.length)));

        assertNull("null input", ArrayUtils.subarray(nullArray, 0, 3));

        assertEquals("empty array", ArrayUtils.EMPTY_FLOAT_ARRAY,
            ArrayUtils.subarray(ArrayUtils.EMPTY_FLOAT_ARRAY, 1, 2));

        assertEquals("start > end", ArrayUtils.EMPTY_FLOAT_ARRAY,
            ArrayUtils.subarray(array, 4, 2));

        assertEquals("start == end", ArrayUtils.EMPTY_FLOAT_ARRAY,
            ArrayUtils.subarray(array, 3, 3));

        assertTrue("start undershoot, normal end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, -2, 4)));

        assertEquals("start overshoot, any end",
            ArrayUtils.EMPTY_FLOAT_ARRAY,
                ArrayUtils.subarray(array, 33, 4));

        assertTrue("normal start, end overshoot",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, 33)));

        assertTrue("start undershoot, end overshoot",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, -2, 12)));

        

        assertSame("empty array, object test",
            ArrayUtils.EMPTY_FLOAT_ARRAY,
                ArrayUtils.subarray(ArrayUtils.EMPTY_FLOAT_ARRAY, 1, 2));

        assertSame("start > end, object test",
            ArrayUtils.EMPTY_FLOAT_ARRAY,
                ArrayUtils.subarray(array, 4, 1));

        assertSame("start == end, object test",
            ArrayUtils.EMPTY_FLOAT_ARRAY,
                ArrayUtils.subarray(array, 3, 3));

        assertSame("start overshoot, any end, object test",
            ArrayUtils.EMPTY_FLOAT_ARRAY,
                ArrayUtils.subarray(array, 8733, 4));

        

        assertSame("float type", float.class,
            ArrayUtils.subarray(array, 2, 4).getClass().getComponentType());

    }

// org.apache.commons.lang3.ArrayUtilsTest::testSubarrayBoolean
    public void testSubarrayBoolean() {
        boolean[] nullArray = null;
        boolean[] array = { true, true, false, true, false, true };
        boolean[] leftSubarray  = { true, true, false, true  };
        boolean[] midSubarray   = { true, false, true, false };
        boolean[] rightSubarray = { false, true, false, true };

        assertTrue("0 start, mid end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, 0, 4)));

        assertTrue("0 start, length end",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, 0, array.length)));

        assertTrue("mid start, mid end",
            ArrayUtils.isEquals(midSubarray,
                ArrayUtils.subarray(array, 1, 5)));

        assertTrue("mid start, length end",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, array.length)));

        assertNull("null input", ArrayUtils.subarray(nullArray, 0, 3));

        assertEquals("empty array", ArrayUtils.EMPTY_BOOLEAN_ARRAY,
            ArrayUtils.subarray(ArrayUtils.EMPTY_BOOLEAN_ARRAY, 1, 2));

        assertEquals("start > end", ArrayUtils.EMPTY_BOOLEAN_ARRAY,
            ArrayUtils.subarray(array, 4, 2));

        assertEquals("start == end", ArrayUtils.EMPTY_BOOLEAN_ARRAY,
            ArrayUtils.subarray(array, 3, 3));

        assertTrue("start undershoot, normal end",
            ArrayUtils.isEquals(leftSubarray,
                ArrayUtils.subarray(array, -2, 4)));

        assertEquals("start overshoot, any end",
            ArrayUtils.EMPTY_BOOLEAN_ARRAY,
                ArrayUtils.subarray(array, 33, 4));

        assertTrue("normal start, end overshoot",
            ArrayUtils.isEquals(rightSubarray,
                ArrayUtils.subarray(array, 2, 33)));

        assertTrue("start undershoot, end overshoot",
            ArrayUtils.isEquals(array,
                ArrayUtils.subarray(array, -2, 12)));

        

        assertSame("empty array, object test",
            ArrayUtils.EMPTY_BOOLEAN_ARRAY,
                ArrayUtils.subarray(ArrayUtils.EMPTY_BOOLEAN_ARRAY, 1, 2));

        assertSame("start > end, object test",
            ArrayUtils.EMPTY_BOOLEAN_ARRAY,
                ArrayUtils.subarray(array, 4, 1));

        assertSame("start == end, object test",
            ArrayUtils.EMPTY_BOOLEAN_ARRAY,
                ArrayUtils.subarray(array, 3, 3));

        assertSame("start overshoot, any end, object test",
            ArrayUtils.EMPTY_BOOLEAN_ARRAY,
                ArrayUtils.subarray(array, 8733, 4));

        

        assertSame("boolean type", boolean.class,
            ArrayUtils.subarray(array, 2, 4).getClass().getComponentType());

    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameLength
    public void testSameLength() {
        Object[] nullArray = null;
        Object[] emptyArray = new Object[0];
        Object[] oneArray = new Object[] {"pick"};
        Object[] twoArray = new Object[] {"pick", "stick"};
        
        assertEquals(true, ArrayUtils.isSameLength(nullArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(nullArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, twoArray));
        
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(oneArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, emptyArray));
        assertEquals(true, ArrayUtils.isSameLength(oneArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(twoArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, oneArray));
        assertEquals(true, ArrayUtils.isSameLength(twoArray, twoArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameLengthBoolean
    public void testSameLengthBoolean() {
        boolean[] nullArray = null;
        boolean[] emptyArray = new boolean[0];
        boolean[] oneArray = new boolean[] {true};
        boolean[] twoArray = new boolean[] {true, false};
        
        assertEquals(true, ArrayUtils.isSameLength(nullArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(nullArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, twoArray));
        
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(oneArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, emptyArray));
        assertEquals(true, ArrayUtils.isSameLength(oneArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(twoArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, oneArray));
        assertEquals(true, ArrayUtils.isSameLength(twoArray, twoArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameLengthLong
    public void testSameLengthLong() {
        long[] nullArray = null;
        long[] emptyArray = new long[0];
        long[] oneArray = new long[] {0L};
        long[] twoArray = new long[] {0L, 76L};
        
        assertEquals(true, ArrayUtils.isSameLength(nullArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(nullArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, twoArray));
        
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(oneArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, emptyArray));
        assertEquals(true, ArrayUtils.isSameLength(oneArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(twoArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, oneArray));
        assertEquals(true, ArrayUtils.isSameLength(twoArray, twoArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameLengthInt
    public void testSameLengthInt() {
        int[] nullArray = null;
        int[] emptyArray = new int[0];
        int[] oneArray = new int[] {4};
        int[] twoArray = new int[] {5, 7};
        
        assertEquals(true, ArrayUtils.isSameLength(nullArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(nullArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, twoArray));
        
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(oneArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, emptyArray));
        assertEquals(true, ArrayUtils.isSameLength(oneArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(twoArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, oneArray));
        assertEquals(true, ArrayUtils.isSameLength(twoArray, twoArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameLengthShort
    public void testSameLengthShort() {
        short[] nullArray = null;
        short[] emptyArray = new short[0];
        short[] oneArray = new short[] {4};
        short[] twoArray = new short[] {6, 8};
        
        assertEquals(true, ArrayUtils.isSameLength(nullArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(nullArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, twoArray));
        
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(oneArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, emptyArray));
        assertEquals(true, ArrayUtils.isSameLength(oneArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(twoArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, oneArray));
        assertEquals(true, ArrayUtils.isSameLength(twoArray, twoArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameLengthChar
    public void testSameLengthChar() {
        char[] nullArray = null;
        char[] emptyArray = new char[0];
        char[] oneArray = new char[] {'f'};
        char[] twoArray = new char[] {'d', 't'};
        
        assertEquals(true, ArrayUtils.isSameLength(nullArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(nullArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, twoArray));
        
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(oneArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, emptyArray));
        assertEquals(true, ArrayUtils.isSameLength(oneArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(twoArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, oneArray));
        assertEquals(true, ArrayUtils.isSameLength(twoArray, twoArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameLengthByte
    public void testSameLengthByte() {
        byte[] nullArray = null;
        byte[] emptyArray = new byte[0];
        byte[] oneArray = new byte[] {3};
        byte[] twoArray = new byte[] {4, 6};
        
        assertEquals(true, ArrayUtils.isSameLength(nullArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(nullArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, twoArray));
        
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(oneArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, emptyArray));
        assertEquals(true, ArrayUtils.isSameLength(oneArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(twoArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, oneArray));
        assertEquals(true, ArrayUtils.isSameLength(twoArray, twoArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameLengthDouble
    public void testSameLengthDouble() {
        double[] nullArray = null;
        double[] emptyArray = new double[0];
        double[] oneArray = new double[] {1.3d};
        double[] twoArray = new double[] {4.5d, 6.3d};
        
        assertEquals(true, ArrayUtils.isSameLength(nullArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(nullArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, twoArray));
        
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(oneArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, emptyArray));
        assertEquals(true, ArrayUtils.isSameLength(oneArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(twoArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, oneArray));
        assertEquals(true, ArrayUtils.isSameLength(twoArray, twoArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameLengthFloat
    public void testSameLengthFloat() {
        float[] nullArray = null;
        float[] emptyArray = new float[0];
        float[] oneArray = new float[] {2.5f};
        float[] twoArray = new float[] {6.4f, 5.8f};
        
        assertEquals(true, ArrayUtils.isSameLength(nullArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(nullArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(nullArray, twoArray));
        
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, nullArray));
        assertEquals(true, ArrayUtils.isSameLength(emptyArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(emptyArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(oneArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, emptyArray));
        assertEquals(true, ArrayUtils.isSameLength(oneArray, oneArray));
        assertEquals(false, ArrayUtils.isSameLength(oneArray, twoArray));
        
        assertEquals(false, ArrayUtils.isSameLength(twoArray, nullArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, emptyArray));
        assertEquals(false, ArrayUtils.isSameLength(twoArray, oneArray));
        assertEquals(true, ArrayUtils.isSameLength(twoArray, twoArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testSameType
    public void testSameType() {
        try {
            ArrayUtils.isSameType(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ArrayUtils.isSameType(null, new Object[0]);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ArrayUtils.isSameType(new Object[0], null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(true, ArrayUtils.isSameType(new Object[0], new Object[0]));
        assertEquals(false, ArrayUtils.isSameType(new String[0], new Object[0]));
        assertEquals(true, ArrayUtils.isSameType(new String[0][0], new String[0][0]));
        assertEquals(false, ArrayUtils.isSameType(new String[0], new String[0][0]));
        assertEquals(false, ArrayUtils.isSameType(new String[0][0], new String[0]));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testReverse
    public void testReverse() {
        StringBuffer str1 = new StringBuffer("pick");
        String str2 = "a";
        String[] str3 = new String[] {"stick"};
        String str4 = "up";
        
        Object[] array = new Object[] {str1, str2, str3};
        ArrayUtils.reverse(array);
        assertEquals(array[0], str3);
        assertEquals(array[1], str2);
        assertEquals(array[2], str1);
        
        array = new Object[] {str1, str2, str3, str4};
        ArrayUtils.reverse(array);
        assertEquals(array[0], str4);
        assertEquals(array[1], str3);
        assertEquals(array[2], str2);
        assertEquals(array[3], str1);

        array = null;
        ArrayUtils.reverse(array);
        assertEquals(null, array);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testReverseLong
    public void testReverseLong() {
        long[] array = new long[] {1L, 2L, 3L};
        ArrayUtils.reverse(array);
        assertEquals(array[0], 3L);
        assertEquals(array[1], 2L);
        assertEquals(array[2], 1L);

        array = null;
        ArrayUtils.reverse(array);
        assertEquals(null, array);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testReverseInt
    public void testReverseInt() {
        int[] array = new int[] {1, 2, 3};
        ArrayUtils.reverse(array);
        assertEquals(array[0], 3);
        assertEquals(array[1], 2);
        assertEquals(array[2], 1);

        array = null;
        ArrayUtils.reverse(array);
        assertEquals(null, array);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testReverseShort
    public void testReverseShort() {
        short[] array = new short[] {1, 2, 3};
        ArrayUtils.reverse(array);
        assertEquals(array[0], 3);
        assertEquals(array[1], 2);
        assertEquals(array[2], 1);

        array = null;
        ArrayUtils.reverse(array);
        assertEquals(null, array);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testReverseChar
    public void testReverseChar() {
        char[] array = new char[] {'a', 'f', 'C'};
        ArrayUtils.reverse(array);
        assertEquals(array[0], 'C');
        assertEquals(array[1], 'f');
        assertEquals(array[2], 'a');

        array = null;
        ArrayUtils.reverse(array);
        assertEquals(null, array);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testReverseByte
    public void testReverseByte() {
        byte[] array = new byte[] {2, 3, 4};
        ArrayUtils.reverse(array);
        assertEquals(array[0], 4);
        assertEquals(array[1], 3);
        assertEquals(array[2], 2);

        array = null;
        ArrayUtils.reverse(array);
        assertEquals(null, array);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testReverseDouble
    public void testReverseDouble() {
        double[] array = new double[] {0.3d, 0.4d, 0.5d};
        ArrayUtils.reverse(array);
        assertEquals(array[0], 0.5d, 0.0d);
        assertEquals(array[1], 0.4d, 0.0d);
        assertEquals(array[2], 0.3d, 0.0d);

        array = null;
        ArrayUtils.reverse(array);
        assertEquals(null, array);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testReverseFloat
    public void testReverseFloat() {
        float[] array = new float[] {0.3f, 0.4f, 0.5f};
        ArrayUtils.reverse(array);
        assertEquals(array[0], 0.5f, 0.0f);
        assertEquals(array[1], 0.4f, 0.0f);
        assertEquals(array[2], 0.3f, 0.0f);

        array = null;
        ArrayUtils.reverse(array);
        assertEquals(null, array);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testReverseBoolean
    public void testReverseBoolean() {
        boolean[] array = new boolean[] {false, false, true};
        ArrayUtils.reverse(array);
        assertEquals(array[0], true);
        assertEquals(array[1], false);
        assertEquals(array[2], false);

        array = null;
        ArrayUtils.reverse(array);
        assertEquals(null, array);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOf
    public void testIndexOf() {
        Object[] array = new Object[] { "0", "1", "2", "3", null, "0" };
        assertEquals(-1, ArrayUtils.indexOf(null, null));
        assertEquals(-1, ArrayUtils.indexOf(null, "0"));
        assertEquals(-1, ArrayUtils.indexOf(new Object[0], "0"));
        assertEquals(0, ArrayUtils.indexOf(array, "0"));
        assertEquals(1, ArrayUtils.indexOf(array, "1"));
        assertEquals(2, ArrayUtils.indexOf(array, "2"));
        assertEquals(3, ArrayUtils.indexOf(array, "3"));
        assertEquals(4, ArrayUtils.indexOf(array, null));
        assertEquals(-1, ArrayUtils.indexOf(array, "notInArray"));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfWithStartIndex
    public void testIndexOfWithStartIndex() {
        Object[] array = new Object[] { "0", "1", "2", "3", null, "0" };
        assertEquals(-1, ArrayUtils.indexOf(null, null, 2));
        assertEquals(-1, ArrayUtils.indexOf(new Object[0], "0", 0));
        assertEquals(-1, ArrayUtils.indexOf(null, "0", 2));
        assertEquals(5, ArrayUtils.indexOf(array, "0", 2));
        assertEquals(-1, ArrayUtils.indexOf(array, "1", 2));
        assertEquals(2, ArrayUtils.indexOf(array, "2", 2));
        assertEquals(3, ArrayUtils.indexOf(array, "3", 2));
        assertEquals(4, ArrayUtils.indexOf(array, null, 2));
        assertEquals(-1, ArrayUtils.indexOf(array, "notInArray", 2));
        
        assertEquals(4, ArrayUtils.indexOf(array, null, -1));
        assertEquals(-1, ArrayUtils.indexOf(array, null, 8));
        assertEquals(-1, ArrayUtils.indexOf(array, "0", 8));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOf
    public void testLastIndexOf() {
        Object[] array = new Object[] { "0", "1", "2", "3", null, "0" };
        assertEquals(-1, ArrayUtils.lastIndexOf(null, null));
        assertEquals(-1, ArrayUtils.lastIndexOf(null, "0"));
        assertEquals(5, ArrayUtils.lastIndexOf(array, "0"));
        assertEquals(1, ArrayUtils.lastIndexOf(array, "1"));
        assertEquals(2, ArrayUtils.lastIndexOf(array, "2"));
        assertEquals(3, ArrayUtils.lastIndexOf(array, "3"));
        assertEquals(4, ArrayUtils.lastIndexOf(array, null));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, "notInArray"));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfWithStartIndex
    public void testLastIndexOfWithStartIndex() {
        Object[] array = new Object[] { "0", "1", "2", "3", null, "0" };
        assertEquals(-1, ArrayUtils.lastIndexOf(null, null, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(null, "0", 2));
        assertEquals(0, ArrayUtils.lastIndexOf(array, "0", 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, "1", 2));
        assertEquals(2, ArrayUtils.lastIndexOf(array, "2", 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, "3", 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, "3", -1));
        assertEquals(4, ArrayUtils.lastIndexOf(array, null, 5));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, null, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, "notInArray", 5));
        
        assertEquals(-1, ArrayUtils.lastIndexOf(array, null, -1));
        assertEquals(5, ArrayUtils.lastIndexOf(array, "0", 88));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContains
    public void testContains() {
        Object[] array = new Object[] { "0", "1", "2", "3", null, "0" };
        assertEquals(false, ArrayUtils.contains(null, null));
        assertEquals(false, ArrayUtils.contains(null, "1"));
        assertEquals(true, ArrayUtils.contains(array, "0"));
        assertEquals(true, ArrayUtils.contains(array, "1"));
        assertEquals(true, ArrayUtils.contains(array, "2"));
        assertEquals(true, ArrayUtils.contains(array, "3"));
        assertEquals(true, ArrayUtils.contains(array, null));
        assertEquals(false, ArrayUtils.contains(array, "notInArray"));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfLong
    public void testIndexOfLong() {
        long[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, 0));
        array = new long[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.indexOf(array, 0));
        assertEquals(1, ArrayUtils.indexOf(array, 1));
        assertEquals(2, ArrayUtils.indexOf(array, 2));
        assertEquals(3, ArrayUtils.indexOf(array, 3));
        assertEquals(-1, ArrayUtils.indexOf(array, 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfLongWithStartIndex
    public void testIndexOfLongWithStartIndex() {
        long[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, 0, 2));
        array = new long[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.indexOf(array, 0, 2));
        assertEquals(-1, ArrayUtils.indexOf(array, 1, 2));
        assertEquals(2, ArrayUtils.indexOf(array, 2, 2));
        assertEquals(3, ArrayUtils.indexOf(array, 3, 2));
        assertEquals(3, ArrayUtils.indexOf(array, 3, -1));
        assertEquals(-1, ArrayUtils.indexOf(array, 99, 0));
        assertEquals(-1, ArrayUtils.indexOf(array, 0, 6));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfLong
    public void testLastIndexOfLong() {
        long[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 0));
        array = new long[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.lastIndexOf(array, 0));
        assertEquals(1, ArrayUtils.lastIndexOf(array, 1));
        assertEquals(2, ArrayUtils.lastIndexOf(array, 2));
        assertEquals(3, ArrayUtils.lastIndexOf(array, 3));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfLongWithStartIndex
    public void testLastIndexOfLongWithStartIndex() {
        long[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 0, 2));
        array = new long[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.lastIndexOf(array, 0, 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, 1, 2));
        assertEquals(2, ArrayUtils.lastIndexOf(array, 2, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 3, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 3, -1));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 99, 4));
        assertEquals(4, ArrayUtils.lastIndexOf(array, 0, 88));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContainsLong
    public void testContainsLong() {
        long[] array = null;
        assertEquals(false, ArrayUtils.contains(array, 1));
        array = new long[] { 0, 1, 2, 3, 0 };
        assertEquals(true, ArrayUtils.contains(array, 0));
        assertEquals(true, ArrayUtils.contains(array, 1));
        assertEquals(true, ArrayUtils.contains(array, 2));
        assertEquals(true, ArrayUtils.contains(array, 3));
        assertEquals(false, ArrayUtils.contains(array, 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfInt
    public void testIndexOfInt() {
        int[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, 0));
        array = new int[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.indexOf(array, 0));
        assertEquals(1, ArrayUtils.indexOf(array, 1));
        assertEquals(2, ArrayUtils.indexOf(array, 2));
        assertEquals(3, ArrayUtils.indexOf(array, 3));
        assertEquals(-1, ArrayUtils.indexOf(array, 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfIntWithStartIndex
    public void testIndexOfIntWithStartIndex() {
        int[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, 0, 2));
        array = new int[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.indexOf(array, 0, 2));
        assertEquals(-1, ArrayUtils.indexOf(array, 1, 2));
        assertEquals(2, ArrayUtils.indexOf(array, 2, 2));
        assertEquals(3, ArrayUtils.indexOf(array, 3, 2));
        assertEquals(3, ArrayUtils.indexOf(array, 3, -1));
        assertEquals(-1, ArrayUtils.indexOf(array, 99, 0));
        assertEquals(-1, ArrayUtils.indexOf(array, 0, 6));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfInt
    public void testLastIndexOfInt() {
        int[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 0));
        array = new int[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.lastIndexOf(array, 0));
        assertEquals(1, ArrayUtils.lastIndexOf(array, 1));
        assertEquals(2, ArrayUtils.lastIndexOf(array, 2));
        assertEquals(3, ArrayUtils.lastIndexOf(array, 3));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfIntWithStartIndex
    public void testLastIndexOfIntWithStartIndex() {
        int[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 0, 2));
        array = new int[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.lastIndexOf(array, 0, 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, 1, 2));
        assertEquals(2, ArrayUtils.lastIndexOf(array, 2, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 3, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 3, -1));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 99));
        assertEquals(4, ArrayUtils.lastIndexOf(array, 0, 88));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContainsInt
    public void testContainsInt() {
        int[] array = null;
        assertEquals(false, ArrayUtils.contains(array, 1));
        array = new int[] { 0, 1, 2, 3, 0 };
        assertEquals(true, ArrayUtils.contains(array, 0));
        assertEquals(true, ArrayUtils.contains(array, 1));
        assertEquals(true, ArrayUtils.contains(array, 2));
        assertEquals(true, ArrayUtils.contains(array, 3));
        assertEquals(false, ArrayUtils.contains(array, 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfShort
    public void testIndexOfShort() {
        short[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (short) 0));
        array = new short[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.indexOf(array, (short) 0));
        assertEquals(1, ArrayUtils.indexOf(array, (short) 1));
        assertEquals(2, ArrayUtils.indexOf(array, (short) 2));
        assertEquals(3, ArrayUtils.indexOf(array, (short) 3));
        assertEquals(-1, ArrayUtils.indexOf(array, (short) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfShortWithStartIndex
    public void testIndexOfShortWithStartIndex() {
        short[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (short) 0, 2));
        array = new short[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.indexOf(array, (short) 0, 2));
        assertEquals(-1, ArrayUtils.indexOf(array, (short) 1, 2));
        assertEquals(2, ArrayUtils.indexOf(array, (short) 2, 2));
        assertEquals(3, ArrayUtils.indexOf(array, (short) 3, 2));
        assertEquals(3, ArrayUtils.indexOf(array, (short) 3, -1));
        assertEquals(-1, ArrayUtils.indexOf(array, (short) 99, 0));
        assertEquals(-1, ArrayUtils.indexOf(array, (short) 0, 6));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfShort
    public void testLastIndexOfShort() {
        short[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (short) 0));
        array = new short[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.lastIndexOf(array, (short) 0));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (short) 1));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (short) 2));
        assertEquals(3, ArrayUtils.lastIndexOf(array, (short) 3));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (short) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfShortWithStartIndex
    public void testLastIndexOfShortWithStartIndex() {
        short[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (short) 0, 2));
        array = new short[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.lastIndexOf(array, (short) 0, 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (short) 1, 2));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (short) 2, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (short) 3, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (short) 3, -1));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (short) 99));
        assertEquals(4, ArrayUtils.lastIndexOf(array, (short) 0, 88));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContainsShort
    public void testContainsShort() {
        short[] array = null;
        assertEquals(false, ArrayUtils.contains(array, (short) 1));
        array = new short[] { 0, 1, 2, 3, 0 };
        assertEquals(true, ArrayUtils.contains(array, (short) 0));
        assertEquals(true, ArrayUtils.contains(array, (short) 1));
        assertEquals(true, ArrayUtils.contains(array, (short) 2));
        assertEquals(true, ArrayUtils.contains(array, (short) 3));
        assertEquals(false, ArrayUtils.contains(array, (short) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfChar
    public void testIndexOfChar() {
        char[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, 'a'));
        array = new char[] { 'a', 'b', 'c', 'd', 'a' };
        assertEquals(0, ArrayUtils.indexOf(array, 'a'));
        assertEquals(1, ArrayUtils.indexOf(array, 'b'));
        assertEquals(2, ArrayUtils.indexOf(array, 'c'));
        assertEquals(3, ArrayUtils.indexOf(array, 'd'));
        assertEquals(-1, ArrayUtils.indexOf(array, 'e'));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfCharWithStartIndex
    public void testIndexOfCharWithStartIndex() {
        char[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, 'a', 2));
        array = new char[] { 'a', 'b', 'c', 'd', 'a' };
        assertEquals(4, ArrayUtils.indexOf(array, 'a', 2));
        assertEquals(-1, ArrayUtils.indexOf(array, 'b', 2));
        assertEquals(2, ArrayUtils.indexOf(array, 'c', 2));
        assertEquals(3, ArrayUtils.indexOf(array, 'd', 2));
        assertEquals(3, ArrayUtils.indexOf(array, 'd', -1));
        assertEquals(-1, ArrayUtils.indexOf(array, 'e', 0));
        assertEquals(-1, ArrayUtils.indexOf(array, 'a', 6));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfChar
    public void testLastIndexOfChar() {
        char[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 'a'));
        array = new char[] { 'a', 'b', 'c', 'd', 'a' };
        assertEquals(4, ArrayUtils.lastIndexOf(array, 'a'));
        assertEquals(1, ArrayUtils.lastIndexOf(array, 'b'));
        assertEquals(2, ArrayUtils.lastIndexOf(array, 'c'));
        assertEquals(3, ArrayUtils.lastIndexOf(array, 'd'));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 'e'));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfCharWithStartIndex
    public void testLastIndexOfCharWithStartIndex() {
        char[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 'a', 2));
        array = new char[] { 'a', 'b', 'c', 'd', 'a' };
        assertEquals(0, ArrayUtils.lastIndexOf(array, 'a', 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, 'b', 2));
        assertEquals(2, ArrayUtils.lastIndexOf(array, 'c', 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 'd', 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 'd', -1));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, 'e'));
        assertEquals(4, ArrayUtils.lastIndexOf(array, 'a', 88));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContainsChar
    public void testContainsChar() {
        char[] array = null;
        assertEquals(false, ArrayUtils.contains(array, 'b'));
        array = new char[] { 'a', 'b', 'c', 'd', 'a' };
        assertEquals(true, ArrayUtils.contains(array, 'a'));
        assertEquals(true, ArrayUtils.contains(array, 'b'));
        assertEquals(true, ArrayUtils.contains(array, 'c'));
        assertEquals(true, ArrayUtils.contains(array, 'd'));
        assertEquals(false, ArrayUtils.contains(array, 'e'));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfByte
    public void testIndexOfByte() {
        byte[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (byte) 0));
        array = new byte[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.indexOf(array, (byte) 0));
        assertEquals(1, ArrayUtils.indexOf(array, (byte) 1));
        assertEquals(2, ArrayUtils.indexOf(array, (byte) 2));
        assertEquals(3, ArrayUtils.indexOf(array, (byte) 3));
        assertEquals(-1, ArrayUtils.indexOf(array, (byte) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfByteWithStartIndex
    public void testIndexOfByteWithStartIndex() {
        byte[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (byte) 0, 2));
        array = new byte[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.indexOf(array, (byte) 0, 2));
        assertEquals(-1, ArrayUtils.indexOf(array, (byte) 1, 2));
        assertEquals(2, ArrayUtils.indexOf(array, (byte) 2, 2));
        assertEquals(3, ArrayUtils.indexOf(array, (byte) 3, 2));
        assertEquals(3, ArrayUtils.indexOf(array, (byte) 3, -1));
        assertEquals(-1, ArrayUtils.indexOf(array, (byte) 99, 0));
        assertEquals(-1, ArrayUtils.indexOf(array, (byte) 0, 6));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfByte
    public void testLastIndexOfByte() {
        byte[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (byte) 0));
        array = new byte[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.lastIndexOf(array, (byte) 0));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (byte) 1));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (byte) 2));
        assertEquals(3, ArrayUtils.lastIndexOf(array, (byte) 3));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (byte) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfByteWithStartIndex
    public void testLastIndexOfByteWithStartIndex() {
        byte[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (byte) 0, 2));
        array = new byte[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.lastIndexOf(array, (byte) 0, 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (byte) 1, 2));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (byte) 2, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (byte) 3, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (byte) 3, -1));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (byte) 99));
        assertEquals(4, ArrayUtils.lastIndexOf(array, (byte) 0, 88));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContainsByte
    public void testContainsByte() {
        byte[] array = null;
        assertEquals(false, ArrayUtils.contains(array, (byte) 1));
        array = new byte[] { 0, 1, 2, 3, 0 };
        assertEquals(true, ArrayUtils.contains(array, (byte) 0));
        assertEquals(true, ArrayUtils.contains(array, (byte) 1));
        assertEquals(true, ArrayUtils.contains(array, (byte) 2));
        assertEquals(true, ArrayUtils.contains(array, (byte) 3));
        assertEquals(false, ArrayUtils.contains(array, (byte) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfDouble
    public void testIndexOfDouble() {
        double[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0));
        array = new double[0];
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.indexOf(array, (double) 0));
        assertEquals(1, ArrayUtils.indexOf(array, (double) 1));
        assertEquals(2, ArrayUtils.indexOf(array, (double) 2));
        assertEquals(3, ArrayUtils.indexOf(array, (double) 3));
        assertEquals(3, ArrayUtils.indexOf(array, (double) 3, -1));
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfDoubleTolerance
    public void testIndexOfDoubleTolerance() {
        double[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0, (double) 0));
        array = new double[0];
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0, (double) 0));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.indexOf(array, (double) 0, (double) 0.3));
        assertEquals(2, ArrayUtils.indexOf(array, (double) 2.2, (double) 0.35));
        assertEquals(3, ArrayUtils.indexOf(array, (double) 4.15, (double) 2.0));
        assertEquals(1, ArrayUtils.indexOf(array, (double) 1.00001324, (double) 0.0001));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfDoubleWithStartIndex
    public void testIndexOfDoubleWithStartIndex() {
        double[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0, 2));
        array = new double[0];
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0, 2));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.indexOf(array, (double) 0, 2));
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 1, 2));
        assertEquals(2, ArrayUtils.indexOf(array, (double) 2, 2));
        assertEquals(3, ArrayUtils.indexOf(array, (double) 3, 2));
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 99, 0));
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0, 6));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfDoubleWithStartIndexTolerance
    public void testIndexOfDoubleWithStartIndexTolerance() {
        double[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0, 2, (double) 0));
        array = new double[0];
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0, 2, (double) 0));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(-1, ArrayUtils.indexOf(array, (double) 0, 99, (double) 0.3));
        assertEquals(0, ArrayUtils.indexOf(array, (double) 0, 0, (double) 0.3));
        assertEquals(4, ArrayUtils.indexOf(array, (double) 0, 3, (double) 0.3));
        assertEquals(2, ArrayUtils.indexOf(array, (double) 2.2, 0, (double) 0.35));
        assertEquals(3, ArrayUtils.indexOf(array, (double) 4.15, 0, (double) 2.0));
        assertEquals(1, ArrayUtils.indexOf(array, (double) 1.00001324, 0, (double) 0.0001));
        assertEquals(3, ArrayUtils.indexOf(array, (double) 4.15, -1, (double) 2.0));
        assertEquals(1, ArrayUtils.indexOf(array, (double) 1.00001324, -300, (double) 0.0001));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfDouble
    public void testLastIndexOfDouble() {
        double[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 0));
        array = new double[0];
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 0));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.lastIndexOf(array, (double) 0));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (double) 1));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (double) 2));
        assertEquals(3, ArrayUtils.lastIndexOf(array, (double) 3));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfDoubleTolerance
    public void testLastIndexOfDoubleTolerance() {
        double[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 0, (double) 0));
        array = new double[0];
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 0, (double) 0));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.lastIndexOf(array, (double) 0, (double) 0.3));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (double) 2.2, (double) 0.35));
        assertEquals(3, ArrayUtils.lastIndexOf(array, (double) 4.15, (double) 2.0));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (double) 1.00001324, (double) 0.0001));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfDoubleWithStartIndex
    public void testLastIndexOfDoubleWithStartIndex() {
        double[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 0, 2));
        array = new double[0];
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 0, 2));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.lastIndexOf(array, (double) 0, 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (double) 1, 2));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (double) 2, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 3, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 3, -1));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 99));
        assertEquals(4, ArrayUtils.lastIndexOf(array, (double) 0, 88));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfDoubleWithStartIndexTolerance
    public void testLastIndexOfDoubleWithStartIndexTolerance() {
        double[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 0, 2, (double) 0));
        array = new double[0];
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 0, 2, (double) 0));
        array = new double[] { (double) 3 };
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 1, 0, (double) 0));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.lastIndexOf(array, (double) 0, 99, (double) 0.3));
        assertEquals(0, ArrayUtils.lastIndexOf(array, (double) 0, 3, (double) 0.3));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (double) 2.2, 3, (double) 0.35));
        assertEquals(3, ArrayUtils.lastIndexOf(array, (double) 4.15, array.length, (double) 2.0));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (double) 1.00001324, array.length, (double) 0.0001));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (double) 4.15, -200, (double) 2.0));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContainsDouble
    public void testContainsDouble() {
        double[] array = null;
        assertEquals(false, ArrayUtils.contains(array, (double) 1));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(true, ArrayUtils.contains(array, (double) 0));
        assertEquals(true, ArrayUtils.contains(array, (double) 1));
        assertEquals(true, ArrayUtils.contains(array, (double) 2));
        assertEquals(true, ArrayUtils.contains(array, (double) 3));
        assertEquals(false, ArrayUtils.contains(array, (double) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContainsDoubleTolerance
    public void testContainsDoubleTolerance() {
        double[] array = null;
        assertEquals(false, ArrayUtils.contains(array, (double) 1, (double) 0));
        array = new double[] { 0, 1, 2, 3, 0 };
        assertEquals(false, ArrayUtils.contains(array, (double) 4.0, (double) 0.33));
        assertEquals(false, ArrayUtils.contains(array, (double) 2.5, (double) 0.49));
        assertEquals(true, ArrayUtils.contains(array, (double) 2.5, (double) 0.50));
        assertEquals(true, ArrayUtils.contains(array, (double) 2.5, (double) 0.51));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfFloat
    public void testIndexOfFloat() {
        float[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (float) 0));
        array = new float[0];
        assertEquals(-1, ArrayUtils.indexOf(array, (float) 0));
        array = new float[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.indexOf(array, (float) 0));
        assertEquals(1, ArrayUtils.indexOf(array, (float) 1));
        assertEquals(2, ArrayUtils.indexOf(array, (float) 2));
        assertEquals(3, ArrayUtils.indexOf(array, (float) 3));
        assertEquals(-1, ArrayUtils.indexOf(array, (float) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfFloatWithStartIndex
    public void testIndexOfFloatWithStartIndex() {
        float[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, (float) 0, 2));
        array = new float[0];
        assertEquals(-1, ArrayUtils.indexOf(array, (float) 0, 2));
        array = new float[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.indexOf(array, (float) 0, 2));
        assertEquals(-1, ArrayUtils.indexOf(array, (float) 1, 2));
        assertEquals(2, ArrayUtils.indexOf(array, (float) 2, 2));
        assertEquals(3, ArrayUtils.indexOf(array, (float) 3, 2));
        assertEquals(3, ArrayUtils.indexOf(array, (float) 3, -1));
        assertEquals(-1, ArrayUtils.indexOf(array, (float) 99, 0));
        assertEquals(-1, ArrayUtils.indexOf(array, (float) 0, 6));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfFloat
    public void testLastIndexOfFloat() {
        float[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (float) 0));
        array = new float[0];
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (float) 0));
        array = new float[] { 0, 1, 2, 3, 0 };
        assertEquals(4, ArrayUtils.lastIndexOf(array, (float) 0));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (float) 1));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (float) 2));
        assertEquals(3, ArrayUtils.lastIndexOf(array, (float) 3));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (float) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfFloatWithStartIndex
    public void testLastIndexOfFloatWithStartIndex() {
        float[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (float) 0, 2));
        array = new float[0];
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (float) 0, 2));
        array = new float[] { 0, 1, 2, 3, 0 };
        assertEquals(0, ArrayUtils.lastIndexOf(array, (float) 0, 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, (float) 1, 2));
        assertEquals(2, ArrayUtils.lastIndexOf(array, (float) 2, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (float) 3, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (float) 3, -1));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, (float) 99));
        assertEquals(4, ArrayUtils.lastIndexOf(array, (float) 0, 88));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContainsFloat
    public void testContainsFloat() {
        float[] array = null;
        assertEquals(false, ArrayUtils.contains(array, (float) 1));
        array = new float[] { 0, 1, 2, 3, 0 };
        assertEquals(true, ArrayUtils.contains(array, (float) 0));
        assertEquals(true, ArrayUtils.contains(array, (float) 1));
        assertEquals(true, ArrayUtils.contains(array, (float) 2));
        assertEquals(true, ArrayUtils.contains(array, (float) 3));
        assertEquals(false, ArrayUtils.contains(array, (float) 99));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfBoolean
    public void testIndexOfBoolean() {
        boolean[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, true));
        array = new boolean[0];
        assertEquals(-1, ArrayUtils.indexOf(array, true));
        array = new boolean[] { true, false, true };
        assertEquals(0, ArrayUtils.indexOf(array, true));
        assertEquals(1, ArrayUtils.indexOf(array, false));
        array = new boolean[] { true, true };
        assertEquals(-1, ArrayUtils.indexOf(array, false));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndexOfBooleanWithStartIndex
    public void testIndexOfBooleanWithStartIndex() {
        boolean[] array = null;
        assertEquals(-1, ArrayUtils.indexOf(array, true, 2));
        array = new boolean[0];
        assertEquals(-1, ArrayUtils.indexOf(array, true, 2));
        array = new boolean[] { true, false, true };
        assertEquals(2, ArrayUtils.indexOf(array, true, 1));
        assertEquals(-1, ArrayUtils.indexOf(array, false, 2));
        assertEquals(1, ArrayUtils.indexOf(array, false, 0));
        assertEquals(1, ArrayUtils.indexOf(array, false, -1));
        array = new boolean[] { true, true };
        assertEquals(-1, ArrayUtils.indexOf(array, false, 0));
        assertEquals(-1, ArrayUtils.indexOf(array, false, -1));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfBoolean
    public void testLastIndexOfBoolean() {
        boolean[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, true));
        array = new boolean[0];
        assertEquals(-1, ArrayUtils.lastIndexOf(array, true));
        array = new boolean[] { true, false, true };
        assertEquals(2, ArrayUtils.lastIndexOf(array, true));
        assertEquals(1, ArrayUtils.lastIndexOf(array, false));
        array = new boolean[] { true, true };
        assertEquals(-1, ArrayUtils.lastIndexOf(array, false));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testLastIndexOfBooleanWithStartIndex
    public void testLastIndexOfBooleanWithStartIndex() {
        boolean[] array = null;
        assertEquals(-1, ArrayUtils.lastIndexOf(array, true, 2));
        array = new boolean[0];
        assertEquals(-1, ArrayUtils.lastIndexOf(array, true, 2));
        array = new boolean[] { true, false, true };
        assertEquals(2, ArrayUtils.lastIndexOf(array, true, 2));
        assertEquals(0, ArrayUtils.lastIndexOf(array, true, 1));
        assertEquals(1, ArrayUtils.lastIndexOf(array, false, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, true, -1));
        array = new boolean[] { true, true };
        assertEquals(-1, ArrayUtils.lastIndexOf(array, false, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, true, -1));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testContainsBoolean
    public void testContainsBoolean() {
        boolean[] array = null;
        assertEquals(false, ArrayUtils.contains(array, true));
        array = new boolean[] { true, false, true };
        assertEquals(true, ArrayUtils.contains(array, true));
        assertEquals(true, ArrayUtils.contains(array, false));
        array = new boolean[] { true, true };
        assertEquals(true, ArrayUtils.contains(array, true));
        assertEquals(false, ArrayUtils.contains(array, false));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_boolean
    public void testToPrimitive_boolean() {
        final Boolean[] b = null;
        assertEquals(null, ArrayUtils.toPrimitive(b));
        assertSame(ArrayUtils.EMPTY_BOOLEAN_ARRAY, ArrayUtils.toPrimitive(new Boolean[0]));
        assertTrue(Arrays.equals(
            new boolean[] {true, false, true},
            ArrayUtils.toPrimitive(new Boolean[] {Boolean.TRUE, Boolean.FALSE, Boolean.TRUE}))
        );

        try {
            ArrayUtils.toPrimitive(new Boolean[] {Boolean.TRUE, null});
            fail();
        } catch (NullPointerException ex) {}
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_boolean_boolean
    public void testToPrimitive_boolean_boolean() {
        assertEquals(null, ArrayUtils.toPrimitive(null, false));
        assertSame(ArrayUtils.EMPTY_BOOLEAN_ARRAY, ArrayUtils.toPrimitive(new Boolean[0], false));
        assertTrue(Arrays.equals(
            new boolean[] {true, false, true},
            ArrayUtils.toPrimitive(new Boolean[] {Boolean.TRUE, Boolean.FALSE, Boolean.TRUE}, false))
        );
        assertTrue(Arrays.equals(
            new boolean[] {true, false, false},
            ArrayUtils.toPrimitive(new Boolean[] {Boolean.TRUE, null, Boolean.FALSE}, false))
        );
        assertTrue(Arrays.equals(
            new boolean[] {true, true, false},
            ArrayUtils.toPrimitive(new Boolean[] {Boolean.TRUE, null, Boolean.FALSE}, true))
        );
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToObject_boolean
    public void testToObject_boolean() {
        final boolean[] b = null;
        assertEquals(null, ArrayUtils.toObject(b));
        assertSame(ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY, ArrayUtils.toObject(new boolean[0]));
        assertTrue(Arrays.equals(
            new Boolean[] {Boolean.TRUE, Boolean.FALSE, Boolean.TRUE},
            ArrayUtils.toObject(new boolean[] {true, false, true}))
        );
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_char
    public void testToPrimitive_char() {
        final Character[] b = null;
        assertEquals(null, ArrayUtils.toPrimitive(b));
        
        assertSame(ArrayUtils.EMPTY_CHAR_ARRAY, ArrayUtils.toPrimitive(new Character[0]));
        
        assertTrue(Arrays.equals(
            new char[] {Character.MIN_VALUE, Character.MAX_VALUE, '0'},
            ArrayUtils.toPrimitive(new Character[] {new Character(Character.MIN_VALUE), 
                new Character(Character.MAX_VALUE), new Character('0')}))
        );

        try {
            ArrayUtils.toPrimitive(new Character[] {new Character(Character.MIN_VALUE), null});
            fail();
        } catch (NullPointerException ex) {}
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_char_char
    public void testToPrimitive_char_char() {
        final Character[] b = null;
        assertEquals(null, ArrayUtils.toPrimitive(b, Character.MIN_VALUE));
        
        assertSame(ArrayUtils.EMPTY_CHAR_ARRAY, 
            ArrayUtils.toPrimitive(new Character[0], (char)0));
        
        assertTrue(Arrays.equals(
            new char[] {Character.MIN_VALUE, Character.MAX_VALUE, '0'},
            ArrayUtils.toPrimitive(new Character[] {new Character(Character.MIN_VALUE), 
                new Character(Character.MAX_VALUE), new Character('0')}, 
                Character.MIN_VALUE))
        );
        
        assertTrue(Arrays.equals(
            new char[] {Character.MIN_VALUE, Character.MAX_VALUE, '0'},
            ArrayUtils.toPrimitive(new Character[] {new Character(Character.MIN_VALUE), null, 
                new Character('0')}, Character.MAX_VALUE))
        );
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToObject_char
    public void testToObject_char() {
        final char[] b = null;
        assertEquals(null, ArrayUtils.toObject(b));
        
        assertSame(ArrayUtils.EMPTY_CHARACTER_OBJECT_ARRAY, 
            ArrayUtils.toObject(new char[0]));
        
        assertTrue(Arrays.equals(
            new Character[] {new Character(Character.MIN_VALUE), 
                new Character(Character.MAX_VALUE), new Character('0')},
                ArrayUtils.toObject(new char[] {Character.MIN_VALUE, Character.MAX_VALUE, 
                '0'} ))
        );
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_byte
    public void testToPrimitive_byte() {
        final Byte[] b = null;
        assertEquals(null, ArrayUtils.toPrimitive(b));
        
        assertSame(ArrayUtils.EMPTY_BYTE_ARRAY, ArrayUtils.toPrimitive(new Byte[0]));
        
        assertTrue(Arrays.equals(
            new byte[] {Byte.MIN_VALUE, Byte.MAX_VALUE, (byte)9999999},
            ArrayUtils.toPrimitive(new Byte[] {Byte.valueOf(Byte.MIN_VALUE), 
                Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)9999999)}))
        );

        try {
            ArrayUtils.toPrimitive(new Byte[] {Byte.valueOf(Byte.MIN_VALUE), null});
            fail();
        } catch (NullPointerException ex) {}
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_byte_byte
    public void testToPrimitive_byte_byte() {
        final Byte[] b = null;
        assertEquals(null, ArrayUtils.toPrimitive(b, Byte.MIN_VALUE));
        
        assertSame(ArrayUtils.EMPTY_BYTE_ARRAY, 
            ArrayUtils.toPrimitive(new Byte[0], (byte)1));
        
        assertTrue(Arrays.equals(
            new byte[] {Byte.MIN_VALUE, Byte.MAX_VALUE, (byte)9999999},
            ArrayUtils.toPrimitive(new Byte[] {Byte.valueOf(Byte.MIN_VALUE), 
                Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)9999999)}, 
                Byte.MIN_VALUE))
        );
        
        assertTrue(Arrays.equals(
            new byte[] {Byte.MIN_VALUE, Byte.MAX_VALUE, (byte)9999999},
            ArrayUtils.toPrimitive(new Byte[] {Byte.valueOf(Byte.MIN_VALUE), null, 
                Byte.valueOf((byte)9999999)}, Byte.MAX_VALUE))
        );
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToObject_byte
    public void testToObject_byte() {
        final byte[] b = null;
        assertEquals(null, ArrayUtils.toObject(b));
        
        assertSame(ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY, 
            ArrayUtils.toObject(new byte[0]));
        
        assertTrue(Arrays.equals(
            new Byte[] {Byte.valueOf(Byte.MIN_VALUE), 
                Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)9999999)},
                ArrayUtils.toObject(new byte[] {Byte.MIN_VALUE, Byte.MAX_VALUE, 
                (byte)9999999}))
        );
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_short
    public void testToPrimitive_short() {
        final Short[] b = null;
        assertEquals(null, ArrayUtils.toPrimitive(b));
        
        assertSame(ArrayUtils.EMPTY_SHORT_ARRAY, ArrayUtils.toPrimitive(new Short[0]));
        
        assertTrue(Arrays.equals(
            new short[] {Short.MIN_VALUE, Short.MAX_VALUE, (short)9999999},
            ArrayUtils.toPrimitive(new Short[] {new Short(Short.MIN_VALUE), 
                new Short(Short.MAX_VALUE), new Short((short)9999999)}))
        );

        try {
            ArrayUtils.toPrimitive(new Short[] {new Short(Short.MIN_VALUE), null});
            fail();
        } catch (NullPointerException ex) {}
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_short_short
    public void testToPrimitive_short_short() {
        final Short[] s = null;
        assertEquals(null, ArrayUtils.toPrimitive(s, Short.MIN_VALUE));
        
        assertSame(ArrayUtils.EMPTY_SHORT_ARRAY, ArrayUtils.toPrimitive(new Short[0], 
        Short.MIN_VALUE));
        
        assertTrue(Arrays.equals(
            new short[] {Short.MIN_VALUE, Short.MAX_VALUE, (short)9999999},
            ArrayUtils.toPrimitive(new Short[] {new Short(Short.MIN_VALUE), 
                new Short(Short.MAX_VALUE), new Short((short)9999999)}, Short.MIN_VALUE))
        );
        
        assertTrue(Arrays.equals(
            new short[] {Short.MIN_VALUE, Short.MAX_VALUE, (short)9999999},
            ArrayUtils.toPrimitive(new Short[] {new Short(Short.MIN_VALUE), null, 
                new Short((short)9999999)}, Short.MAX_VALUE))
        );
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToObject_short
    public void testToObject_short() {
        final short[] b = null;
        assertEquals(null, ArrayUtils.toObject(b));
        
        assertSame(ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY, 
        ArrayUtils.toObject(new short[0]));
        
        assertTrue(Arrays.equals(
            new Short[] {new Short(Short.MIN_VALUE), new Short(Short.MAX_VALUE), 
                new Short((short)9999999)},
            ArrayUtils.toObject(new short[] {Short.MIN_VALUE, Short.MAX_VALUE, 
                (short)9999999}))
        );
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_int
     public void testToPrimitive_int() {
         final Integer[] b = null;
         assertEquals(null, ArrayUtils.toPrimitive(b));
         assertSame(ArrayUtils.EMPTY_INT_ARRAY, ArrayUtils.toPrimitive(new Integer[0]));
         assertTrue(Arrays.equals(
             new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Integer[] {new Integer(Integer.MIN_VALUE), 
                 new Integer(Integer.MAX_VALUE), new Integer(9999999)}))
         );

         try {
             ArrayUtils.toPrimitive(new Integer[] {new Integer(Integer.MIN_VALUE), null});
             fail();
         } catch (NullPointerException ex) {}
     }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_int_int
     public void testToPrimitive_int_int() {
         final Long[] l = null;
         assertEquals(null, ArrayUtils.toPrimitive(l, Integer.MIN_VALUE));
         assertSame(ArrayUtils.EMPTY_INT_ARRAY, 
         ArrayUtils.toPrimitive(new Integer[0], 1));
         assertTrue(Arrays.equals(
             new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Integer[] {new Integer(Integer.MIN_VALUE), 
                 new Integer(Integer.MAX_VALUE), new Integer(9999999)},1)));
         assertTrue(Arrays.equals(
             new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Integer[] {new Integer(Integer.MIN_VALUE), 
                 null, new Integer(9999999)}, Integer.MAX_VALUE))
         );
     }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_intNull
    public void testToPrimitive_intNull() {
        Integer[] iArray = null;
        assertEquals(null, ArrayUtils.toPrimitive(iArray, Integer.MIN_VALUE));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToObject_int
    public void testToObject_int() {
        final int[] b = null;
        assertEquals(null, ArrayUtils.toObject(b));
    
        assertSame(
            ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY,
            ArrayUtils.toObject(new int[0]));
    
        assertTrue(
            Arrays.equals(
                new Integer[] {
                    new Integer(Integer.MIN_VALUE),
                    new Integer(Integer.MAX_VALUE),
                    new Integer(9999999)},
            ArrayUtils.toObject(
                new int[] { Integer.MIN_VALUE, Integer.MAX_VALUE, 9999999 })));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_long
     public void testToPrimitive_long() {
         final Long[] b = null;
         assertEquals(null, ArrayUtils.toPrimitive(b));
         
         assertSame(ArrayUtils.EMPTY_LONG_ARRAY, 
            ArrayUtils.toPrimitive(new Long[0]));
         
         assertTrue(Arrays.equals(
             new long[] {Long.MIN_VALUE, Long.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Long[] {new Long(Long.MIN_VALUE), 
                 new Long(Long.MAX_VALUE), new Long(9999999)}))
         );

         try {
             ArrayUtils.toPrimitive(new Long[] {new Long(Long.MIN_VALUE), null});
             fail();
         } catch (NullPointerException ex) {}
     }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_long_long
     public void testToPrimitive_long_long() {
         final Long[] l = null;
         assertEquals(null, ArrayUtils.toPrimitive(l, Long.MIN_VALUE));
         
         assertSame(ArrayUtils.EMPTY_LONG_ARRAY, 
         ArrayUtils.toPrimitive(new Long[0], 1));
         
         assertTrue(Arrays.equals(
             new long[] {Long.MIN_VALUE, Long.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Long[] {new Long(Long.MIN_VALUE), 
                 new Long(Long.MAX_VALUE), new Long(9999999)},1)));
         
         assertTrue(Arrays.equals(
             new long[] {Long.MIN_VALUE, Long.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Long[] {new Long(Long.MIN_VALUE), 
                 null, new Long(9999999)}, Long.MAX_VALUE))
         );
     }

// org.apache.commons.lang3.ArrayUtilsTest::testToObject_long
    public void testToObject_long() {
        final long[] b = null;
        assertEquals(null, ArrayUtils.toObject(b));
    
        assertSame(
            ArrayUtils.EMPTY_LONG_OBJECT_ARRAY,
            ArrayUtils.toObject(new long[0]));
    
        assertTrue(
            Arrays.equals(
                new Long[] {
                    new Long(Long.MIN_VALUE),
                    new Long(Long.MAX_VALUE),
                    new Long(9999999)},
            ArrayUtils.toObject(
                new long[] { Long.MIN_VALUE, Long.MAX_VALUE, 9999999 })));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_float
     public void testToPrimitive_float() {
         final Float[] b = null;
         assertEquals(null, ArrayUtils.toPrimitive(b));
         
         assertSame(ArrayUtils.EMPTY_FLOAT_ARRAY, 
            ArrayUtils.toPrimitive(new Float[0]));
         
         assertTrue(Arrays.equals(
             new float[] {Float.MIN_VALUE, Float.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Float[] {new Float(Float.MIN_VALUE), 
                 new Float(Float.MAX_VALUE), new Float(9999999)}))
         );

         try {
             ArrayUtils.toPrimitive(new Float[] {new Float(Float.MIN_VALUE), null});
             fail();
         } catch (NullPointerException ex) {}
     }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_float_float
     public void testToPrimitive_float_float() {
         final Float[] l = null;
         assertEquals(null, ArrayUtils.toPrimitive(l, Float.MIN_VALUE));
         
         assertSame(ArrayUtils.EMPTY_FLOAT_ARRAY, 
         ArrayUtils.toPrimitive(new Float[0], 1));
         
         assertTrue(Arrays.equals(
             new float[] {Float.MIN_VALUE, Float.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Float[] {new Float(Float.MIN_VALUE), 
                 new Float(Float.MAX_VALUE), new Float(9999999)},1)));
         
         assertTrue(Arrays.equals(
             new float[] {Float.MIN_VALUE, Float.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Float[] {new Float(Float.MIN_VALUE), 
                 null, new Float(9999999)}, Float.MAX_VALUE))
         );
     }

// org.apache.commons.lang3.ArrayUtilsTest::testToObject_float
    public void testToObject_float() {
        final float[] b = null;
        assertEquals(null, ArrayUtils.toObject(b));
    
        assertSame(
            ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY,
            ArrayUtils.toObject(new float[0]));
    
        assertTrue(
            Arrays.equals(
                new Float[] {
                    new Float(Float.MIN_VALUE),
                    new Float(Float.MAX_VALUE),
                    new Float(9999999)},
            ArrayUtils.toObject(
                new float[] { Float.MIN_VALUE, Float.MAX_VALUE, 9999999 })));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_double
     public void testToPrimitive_double() {
         final Double[] b = null;
         assertEquals(null, ArrayUtils.toPrimitive(b));
         
         assertSame(ArrayUtils.EMPTY_DOUBLE_ARRAY, 
            ArrayUtils.toPrimitive(new Double[0]));
         
         assertTrue(Arrays.equals(
             new double[] {Double.MIN_VALUE, Double.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Double[] {new Double(Double.MIN_VALUE), 
                 new Double(Double.MAX_VALUE), new Double(9999999)}))
         );

         try {
             ArrayUtils.toPrimitive(new Float[] {new Float(Float.MIN_VALUE), null});
             fail();
         } catch (NullPointerException ex) {}
     }

// org.apache.commons.lang3.ArrayUtilsTest::testToPrimitive_double_double
     public void testToPrimitive_double_double() {
         final Double[] l = null;
         assertEquals(null, ArrayUtils.toPrimitive(l, Double.MIN_VALUE));
         
         assertSame(ArrayUtils.EMPTY_DOUBLE_ARRAY, 
         ArrayUtils.toPrimitive(new Double[0], 1));
         
         assertTrue(Arrays.equals(
             new double[] {Double.MIN_VALUE, Double.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Double[] {new Double(Double.MIN_VALUE), 
                 new Double(Double.MAX_VALUE), new Double(9999999)},1)));
         
         assertTrue(Arrays.equals(
             new double[] {Double.MIN_VALUE, Double.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Double[] {new Double(Double.MIN_VALUE), 
                 null, new Double(9999999)}, Double.MAX_VALUE))
         );
     }

// org.apache.commons.lang3.ArrayUtilsTest::testToObject_double
    public void testToObject_double() {
        final double[] b = null;
        assertEquals(null, ArrayUtils.toObject(b));
    
        assertSame(
            ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY,
            ArrayUtils.toObject(new double[0]));
    
        assertTrue(
            Arrays.equals(
                new Double[] {
                    new Double(Double.MIN_VALUE),
                    new Double(Double.MAX_VALUE),
                    new Double(9999999)},
            ArrayUtils.toObject(
                new double[] { Double.MIN_VALUE, Double.MAX_VALUE, 9999999 })));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIsEmptyObject
    public void testIsEmptyObject() {
        Object[] emptyArray = new Object[] {};
        Object[] notEmptyArray = new Object[] { new String("Value") };
        assertEquals(true, ArrayUtils.isEmpty((Object[])null));
        assertEquals(true, ArrayUtils.isEmpty(emptyArray));
        assertEquals(false, ArrayUtils.isEmpty(notEmptyArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIsEmptyPrimitives
    public void testIsEmptyPrimitives() {
        long[] emptyLongArray = new long[] {};
        long[] notEmptyLongArray = new long[] { 1L };
        assertEquals(true, ArrayUtils.isEmpty((long[])null));
        assertEquals(true, ArrayUtils.isEmpty(emptyLongArray));
        assertEquals(false, ArrayUtils.isEmpty(notEmptyLongArray));

        int[] emptyIntArray = new int[] {};
        int[] notEmptyIntArray = new int[] { 1 };
        assertEquals(true, ArrayUtils.isEmpty((int[])null));
        assertEquals(true, ArrayUtils.isEmpty(emptyIntArray));
        assertEquals(false, ArrayUtils.isEmpty(notEmptyIntArray));

        short[] emptyShortArray = new short[] {};
        short[] notEmptyShortArray = new short[] { 1 };
        assertEquals(true, ArrayUtils.isEmpty((short[])null));
        assertEquals(true, ArrayUtils.isEmpty(emptyShortArray));
        assertEquals(false, ArrayUtils.isEmpty(notEmptyShortArray));

        char[] emptyCharArray = new char[] {};
        char[] notEmptyCharArray = new char[] { 1 };
        assertEquals(true, ArrayUtils.isEmpty((char[])null));
        assertEquals(true, ArrayUtils.isEmpty(emptyCharArray));
        assertEquals(false, ArrayUtils.isEmpty(notEmptyCharArray));

        byte[] emptyByteArray = new byte[] {};
        byte[] notEmptyByteArray = new byte[] { 1 };
        assertEquals(true, ArrayUtils.isEmpty((byte[])null));
        assertEquals(true, ArrayUtils.isEmpty(emptyByteArray));
        assertEquals(false, ArrayUtils.isEmpty(notEmptyByteArray));

        double[] emptyDoubleArray = new double[] {};
        double[] notEmptyDoubleArray = new double[] { 1.0 };
        assertEquals(true, ArrayUtils.isEmpty((double[])null));
        assertEquals(true, ArrayUtils.isEmpty(emptyDoubleArray));
        assertEquals(false, ArrayUtils.isEmpty(notEmptyDoubleArray));

        float[] emptyFloatArray = new float[] {};
        float[] notEmptyFloatArray = new float[] { 1.0F };
        assertEquals(true, ArrayUtils.isEmpty((float[])null));
        assertEquals(true, ArrayUtils.isEmpty(emptyFloatArray));
        assertEquals(false, ArrayUtils.isEmpty(notEmptyFloatArray));

        boolean[] emptyBooleanArray = new boolean[] {};
        boolean[] notEmptyBooleanArray = new boolean[] { true };
        assertEquals(true, ArrayUtils.isEmpty((boolean[])null));
        assertEquals(true, ArrayUtils.isEmpty(emptyBooleanArray));
        assertEquals(false, ArrayUtils.isEmpty(notEmptyBooleanArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testGetLength
    public void testGetLength() {
        assertEquals(0, ArrayUtils.getLength(null));
        
        Object[] emptyObjectArray = new Object[0];
        Object[] notEmptyObjectArray = new Object[] {"aValue"};
        assertEquals(0, ArrayUtils.getLength((Object[]) null));
        assertEquals(0, ArrayUtils.getLength(emptyObjectArray));
        assertEquals(1, ArrayUtils.getLength(notEmptyObjectArray));
 
        int[] emptyIntArray = new int[] {};
        int[] notEmptyIntArray = new int[] { 1 };
        assertEquals(0, ArrayUtils.getLength((int[]) null));
        assertEquals(0, ArrayUtils.getLength(emptyIntArray));
        assertEquals(1, ArrayUtils.getLength(notEmptyIntArray));

        short[] emptyShortArray = new short[] {};
        short[] notEmptyShortArray = new short[] { 1 };
        assertEquals(0, ArrayUtils.getLength((short[]) null));
        assertEquals(0, ArrayUtils.getLength(emptyShortArray));
        assertEquals(1, ArrayUtils.getLength(notEmptyShortArray));

        char[] emptyCharArray = new char[] {};
        char[] notEmptyCharArray = new char[] { 1 };
        assertEquals(0, ArrayUtils.getLength((char[]) null));
        assertEquals(0, ArrayUtils.getLength(emptyCharArray));
        assertEquals(1, ArrayUtils.getLength(notEmptyCharArray));

        byte[] emptyByteArray = new byte[] {};
        byte[] notEmptyByteArray = new byte[] { 1 };
        assertEquals(0, ArrayUtils.getLength((byte[]) null));
        assertEquals(0, ArrayUtils.getLength(emptyByteArray));
        assertEquals(1, ArrayUtils.getLength(notEmptyByteArray));

        double[] emptyDoubleArray = new double[] {};
        double[] notEmptyDoubleArray = new double[] { 1.0 };
        assertEquals(0, ArrayUtils.getLength((double[]) null));
        assertEquals(0, ArrayUtils.getLength(emptyDoubleArray));
        assertEquals(1, ArrayUtils.getLength(notEmptyDoubleArray));

        float[] emptyFloatArray = new float[] {};
        float[] notEmptyFloatArray = new float[] { 1.0F };
        assertEquals(0, ArrayUtils.getLength((float[]) null));
        assertEquals(0, ArrayUtils.getLength(emptyFloatArray));
        assertEquals(1, ArrayUtils.getLength(notEmptyFloatArray));

        boolean[] emptyBooleanArray = new boolean[] {};
        boolean[] notEmptyBooleanArray = new boolean[] { true };
        assertEquals(0, ArrayUtils.getLength((boolean[]) null));
        assertEquals(0, ArrayUtils.getLength(emptyBooleanArray));
        assertEquals(1, ArrayUtils.getLength(notEmptyBooleanArray));
        
        try {
            ArrayUtils.getLength("notAnArray");
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new BooleanUtils());
        Constructor<?>[] cons = BooleanUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(BooleanUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(BooleanUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_negate_Boolean
    public void test_negate_Boolean() {
        assertSame(null, BooleanUtils.negate(null));
        assertSame(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
        assertSame(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_isTrue_Boolean
    public void test_isTrue_Boolean() {
        assertEquals(true, BooleanUtils.isTrue(Boolean.TRUE));
        assertEquals(false, BooleanUtils.isTrue(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isTrue((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_isNotTrue_Boolean
    public void test_isNotTrue_Boolean() {
        assertEquals(false, BooleanUtils.isNotTrue(Boolean.TRUE));
        assertEquals(true, BooleanUtils.isNotTrue(Boolean.FALSE));
        assertEquals(true, BooleanUtils.isNotTrue((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_isFalse_Boolean
    public void test_isFalse_Boolean() {
        assertEquals(false, BooleanUtils.isFalse(Boolean.TRUE));
        assertEquals(true, BooleanUtils.isFalse(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isFalse((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_isNotFalse_Boolean
    public void test_isNotFalse_Boolean() {
        assertEquals(true, BooleanUtils.isNotFalse(Boolean.TRUE));
        assertEquals(false, BooleanUtils.isNotFalse(Boolean.FALSE));
        assertEquals(true, BooleanUtils.isNotFalse((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_Boolean
    public void test_toBoolean_Boolean() {
        assertEquals(true, BooleanUtils.toBoolean(Boolean.TRUE));
        assertEquals(false, BooleanUtils.toBoolean(Boolean.FALSE));
        assertEquals(false, BooleanUtils.toBoolean((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanDefaultIfNull_Boolean_boolean
    public void test_toBooleanDefaultIfNull_Boolean_boolean() {
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, true));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, false));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_int
    public void test_toBoolean_int() {
        assertEquals(true, BooleanUtils.toBoolean(1));
        assertEquals(true, BooleanUtils.toBoolean(-1));
        assertEquals(false, BooleanUtils.toBoolean(0));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_int
    public void test_toBooleanObject_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(-1));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_Integer
    public void test_toBooleanObject_Integer() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(1)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(-1)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(0)));
        assertEquals(null, BooleanUtils.toBooleanObject((Integer) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_int_int_int
    public void test_toBoolean_int_int_int() {
        assertEquals(true, BooleanUtils.toBoolean(6, 6, 7));
        assertEquals(false, BooleanUtils.toBoolean(7, 6, 7));
        try {
            BooleanUtils.toBoolean(8, 6, 7);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_Integer_Integer_Integer
    public void test_toBoolean_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);

        assertEquals(true, BooleanUtils.toBoolean((Integer) null, null, seven));
        assertEquals(false, BooleanUtils.toBoolean((Integer) null, six, null));
        try {
            BooleanUtils.toBoolean(null, six, seven);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(true, BooleanUtils.toBoolean(new Integer(6), six, seven));
        assertEquals(false, BooleanUtils.toBoolean(new Integer(7), six, seven));
        try {
            BooleanUtils.toBoolean(new Integer(8), six, seven);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_int_int_int
    public void test_toBooleanObject_int_int_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(6, 6, 7, 8));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(7, 6, 7, 8));
        assertEquals(null, BooleanUtils.toBooleanObject(8, 6, 7, 8));
        try {
            BooleanUtils.toBooleanObject(9, 6, 7, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_Integer_Integer_Integer_Integer
    public void test_toBooleanObject_Integer_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        Integer eight = new Integer(8);

        assertSame(Boolean.TRUE, BooleanUtils.toBooleanObject((Integer) null, null, seven, eight));
        assertSame(Boolean.FALSE, BooleanUtils.toBooleanObject((Integer) null, six, null, eight));
        assertSame(null, BooleanUtils.toBooleanObject((Integer) null, six, seven, null));
        try {
            BooleanUtils.toBooleanObject(null, six, seven, eight);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(6), six, seven, eight));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(7), six, seven, eight));
        assertEquals(null, BooleanUtils.toBooleanObject(new Integer(8), six, seven, eight));
        try {
            BooleanUtils.toBooleanObject(new Integer(9), six, seven, eight);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toInteger_boolean
    public void test_toInteger_boolean() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toIntegerObject_boolean
    public void test_toIntegerObject_boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(true));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toIntegerObject_Boolean
    public void test_toIntegerObject_Boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(Boolean.FALSE));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toInteger_boolean_int_int
    public void test_toInteger_boolean_int_int() {
        assertEquals(6, BooleanUtils.toInteger(true, 6, 7));
        assertEquals(7, BooleanUtils.toInteger(false, 6, 7));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toInteger_Boolean_int_int_int
    public void test_toInteger_Boolean_int_int_int() {
        assertEquals(6, BooleanUtils.toInteger(Boolean.TRUE, 6, 7, 8));
        assertEquals(7, BooleanUtils.toInteger(Boolean.FALSE, 6, 7, 8));
        assertEquals(8, BooleanUtils.toInteger(null, 6, 7, 8));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toIntegerObject_boolean_Integer_Integer
    public void test_toIntegerObject_boolean_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        assertEquals(six, BooleanUtils.toIntegerObject(true, six, seven));
        assertEquals(seven, BooleanUtils.toIntegerObject(false, six, seven));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toIntegerObject_Boolean_Integer_Integer_Integer
    public void test_toIntegerObject_Boolean_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        Integer eight = new Integer(8);
        assertEquals(six, BooleanUtils.toIntegerObject(Boolean.TRUE, six, seven, eight));
        assertEquals(seven, BooleanUtils.toIntegerObject(Boolean.FALSE, six, seven, eight));
        assertEquals(eight, BooleanUtils.toIntegerObject((Boolean) null, six, seven, eight));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null, six, seven, null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_String
    public void test_toBooleanObject_String() {
        assertEquals(null, BooleanUtils.toBooleanObject((String) null));
        assertEquals(null, BooleanUtils.toBooleanObject(""));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("FALSE"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("NO"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("OFF"));
        assertEquals(null, BooleanUtils.toBooleanObject("oof"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TRUE"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("ON"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("YES"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TruE"));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_String_String_String_String
    public void test_toBooleanObject_String_String_String_String() {
        assertSame(Boolean.TRUE, BooleanUtils.toBooleanObject((String) null, null, "N", "U"));
        assertSame(Boolean.FALSE, BooleanUtils.toBooleanObject((String) null, "Y", null, "U"));
        assertSame(null, BooleanUtils.toBooleanObject((String) null, "Y", "N", null));
        try {
            BooleanUtils.toBooleanObject((String) null, "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("Y", "Y", "N", "U"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("N", "Y", "N", "U"));
        assertEquals(null, BooleanUtils.toBooleanObject("U", "Y", "N", "U"));
        try {
            BooleanUtils.toBooleanObject(null, "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            BooleanUtils.toBooleanObject("X", "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_String
    public void test_toBoolean_String() {
        assertEquals(false, BooleanUtils.toBoolean((String) null));
        assertEquals(false, BooleanUtils.toBoolean(""));
        assertEquals(false, BooleanUtils.toBoolean("off"));
        assertEquals(false, BooleanUtils.toBoolean("oof"));
        assertEquals(false, BooleanUtils.toBoolean("yep"));
        assertEquals(false, BooleanUtils.toBoolean("trux"));
        assertEquals(false, BooleanUtils.toBoolean("false"));
        assertEquals(false, BooleanUtils.toBoolean("a"));
        assertEquals(true, BooleanUtils.toBoolean("true")); 
        assertEquals(true, BooleanUtils.toBoolean(new StringBuffer("tr").append("ue").toString()));
        assertEquals(true, BooleanUtils.toBoolean("truE"));
        assertEquals(true, BooleanUtils.toBoolean("trUe"));
        assertEquals(true, BooleanUtils.toBoolean("trUE"));
        assertEquals(true, BooleanUtils.toBoolean("tRue"));
        assertEquals(true, BooleanUtils.toBoolean("tRuE"));
        assertEquals(true, BooleanUtils.toBoolean("tRUe"));
        assertEquals(true, BooleanUtils.toBoolean("tRUE"));
        assertEquals(true, BooleanUtils.toBoolean("TRUE"));
        assertEquals(true, BooleanUtils.toBoolean("TRUe"));
        assertEquals(true, BooleanUtils.toBoolean("TRuE"));
        assertEquals(true, BooleanUtils.toBoolean("TRue"));
        assertEquals(true, BooleanUtils.toBoolean("TrUE"));
        assertEquals(true, BooleanUtils.toBoolean("TrUe"));
        assertEquals(true, BooleanUtils.toBoolean("TruE"));
        assertEquals(true, BooleanUtils.toBoolean("True"));
        assertEquals(true, BooleanUtils.toBoolean("on"));
        assertEquals(true, BooleanUtils.toBoolean("oN"));
        assertEquals(true, BooleanUtils.toBoolean("On"));
        assertEquals(true, BooleanUtils.toBoolean("ON"));
        assertEquals(true, BooleanUtils.toBoolean("yes"));
        assertEquals(true, BooleanUtils.toBoolean("yeS"));
        assertEquals(true, BooleanUtils.toBoolean("yEs"));
        assertEquals(true, BooleanUtils.toBoolean("yES"));
        assertEquals(true, BooleanUtils.toBoolean("Yes"));
        assertEquals(true, BooleanUtils.toBoolean("YeS"));
        assertEquals(true, BooleanUtils.toBoolean("YEs"));
        assertEquals(true, BooleanUtils.toBoolean("YES"));
        assertEquals(false, BooleanUtils.toBoolean("yes?"));
        assertEquals(false, BooleanUtils.toBoolean("tru"));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_String_String_String
    public void test_toBoolean_String_String_String() {
        assertEquals(true, BooleanUtils.toBoolean((String) null, null, "N"));
        assertEquals(false, BooleanUtils.toBoolean((String) null, "Y", null));
        try {
            BooleanUtils.toBooleanObject((String) null, "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(true, BooleanUtils.toBoolean("Y", "Y", "N"));
        assertEquals(false, BooleanUtils.toBoolean("N", "Y", "N"));
        try {
            BooleanUtils.toBoolean(null, "Y", "N");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            BooleanUtils.toBoolean("X", "Y", "N");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringTrueFalse_Boolean
    public void test_toStringTrueFalse_Boolean() {
        assertEquals(null, BooleanUtils.toStringTrueFalse((Boolean) null));
        assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringOnOff_Boolean
    public void test_toStringOnOff_Boolean() {
        assertEquals(null, BooleanUtils.toStringOnOff((Boolean) null));
        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringYesNo_Boolean
    public void test_toStringYesNo_Boolean() {
        assertEquals(null, BooleanUtils.toStringYesNo((Boolean) null));
        assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toString_Boolean_String_String_String
    public void test_toString_Boolean_String_String_String() {
        assertEquals("U", BooleanUtils.toString((Boolean) null, "Y", "N", "U"));
        assertEquals("Y", BooleanUtils.toString(Boolean.TRUE, "Y", "N", "U"));
        assertEquals("N", BooleanUtils.toString(Boolean.FALSE, "Y", "N", "U"));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringTrueFalse_boolean
    public void test_toStringTrueFalse_boolean() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringOnOff_boolean
    public void test_toStringOnOff_boolean() {
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringYesNo_boolean
    public void test_toStringYesNo_boolean() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toString_boolean_String_String_String
    public void test_toString_boolean_String_String_String() {
        assertEquals("Y", BooleanUtils.toString(true, "Y", "N"));
        assertEquals("N", BooleanUtils.toString(false, "Y", "N"));
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_primitive_nullInput
    public void testXor_primitive_nullInput() {
        final boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_primitive_emptyInput
    public void testXor_primitive_emptyInput() {
        try {
            BooleanUtils.xor(new boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_primitive_validInput_2items
    public void testXor_primitive_validInput_2items() {
        assertTrue(
            "True result for (true, true)",
            ! BooleanUtils.xor(new boolean[] { true, true }));

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils.xor(new boolean[] { false, false }));

        assertTrue(
            "False result for (true, false)",
            BooleanUtils.xor(new boolean[] { true, false }));

        assertTrue(
            "False result for (false, true)",
            BooleanUtils.xor(new boolean[] { false, true }));
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_primitive_validInput_3items
    public void testXor_primitive_validInput_3items() {
        assertTrue(
            "False result for (false, false, true)",
            BooleanUtils.xor(new boolean[] { false, false, true }));

        assertTrue(
            "False result for (false, true, false)",
            BooleanUtils.xor(new boolean[] { false, true, false }));

        assertTrue(
            "False result for (true, false, false)",
            BooleanUtils.xor(new boolean[] { true, false, false }));

        assertTrue(
            "True result for (true, true, true)",
            ! BooleanUtils.xor(new boolean[] { true, true, true }));

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils.xor(new boolean[] { false, false, false }));

        assertTrue(
            "True result for (true, true, false)",
            ! BooleanUtils.xor(new boolean[] { true, true, false }));

        assertTrue(
            "True result for (true, false, true)",
            ! BooleanUtils.xor(new boolean[] { true, false, true }));

        assertTrue(
            "False result for (false, true, true)",
            ! BooleanUtils.xor(new boolean[] { false, true, true }));
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_nullInput
    public void testXor_object_nullInput() {
        final Boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_emptyInput
    public void testXor_object_emptyInput() {
        try {
            BooleanUtils.xor(new Boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_nullElementInput
    public void testXor_object_nullElementInput() {
        try {
            BooleanUtils.xor(new Boolean[] {null});
            fail("Exception was not thrown for null element input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_validInput_2items
    public void testXor_object_validInput_2items() {
        assertTrue(
            "True result for (true, true)",
            ! BooleanUtils
                .xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils
                .xor(new Boolean[] { Boolean.FALSE, Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "False result for (true, false)",
            BooleanUtils
                .xor(new Boolean[] { Boolean.TRUE, Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "False result for (false, true)",
            BooleanUtils
                .xor(new Boolean[] { Boolean.FALSE, Boolean.TRUE })
                .booleanValue());
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_validInput_3items
    public void testXor_object_validInput_3items() {
        assertTrue(
            "False result for (false, false, true)",
            BooleanUtils
                .xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.FALSE,
                        Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "False result for (false, true, false)",
            BooleanUtils
                .xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.TRUE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "False result for (true, false, false)",
            BooleanUtils
                .xor(
                    new Boolean[] {
                        Boolean.TRUE,
                        Boolean.FALSE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "True result for (true, true, true)",
            ! BooleanUtils
                .xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE, Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.FALSE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "True result for (true, true, false)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.TRUE,
                        Boolean.TRUE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "True result for (true, false, true)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.TRUE,
                        Boolean.FALSE,
                        Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "False result for (false, true, true)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.TRUE,
                        Boolean.TRUE })
                .booleanValue());
                
    }

// org.apache.commons.lang3.CharEncodingTest::testConstructor
    public void testConstructor() {
        new CharEncoding();
    }

// org.apache.commons.lang3.CharEncodingTest::testMustBeSupportedJava1_3_1
    public void testMustBeSupportedJava1_3_1() {
        if (SystemUtils.isJavaVersionAtLeast(1.3f)) {
            this.assertSupportedEncoding(CharEncoding.ISO_8859_1);
            this.assertSupportedEncoding(CharEncoding.US_ASCII);
            this.assertSupportedEncoding(CharEncoding.UTF_16);
            this.assertSupportedEncoding(CharEncoding.UTF_16BE);
            this.assertSupportedEncoding(CharEncoding.UTF_16LE);
            this.assertSupportedEncoding(CharEncoding.UTF_8);
        } else {
            this.warn("Java 1.3 tests not run since the current version is " + SystemUtils.JAVA_VERSION);
        }
    }

// org.apache.commons.lang3.CharEncodingTest::testNotSupported
    public void testNotSupported() {
        assertFalse(CharEncoding.isSupported(null));
        assertFalse(CharEncoding.isSupported(""));
        assertFalse(CharEncoding.isSupported(" "));
        assertFalse(CharEncoding.isSupported("\t\r\n"));
        assertFalse(CharEncoding.isSupported("DOESNOTEXIST"));
        assertFalse(CharEncoding.isSupported("this is not a valid encoding name"));
    }

// org.apache.commons.lang3.CharEncodingTest::testWorksOnJava1_1_8
    public void testWorksOnJava1_1_8() {
        
        
        
        
        if (SystemUtils.isJavaVersionAtLeast(1.1f)) {
            this.assertSupportedEncoding(CharEncoding.ISO_8859_1);
            this.assertSupportedEncoding(CharEncoding.US_ASCII);
            this.assertSupportedEncoding(CharEncoding.UTF_8);
        } else {
            this.warn("Java 1.1 tests not run since the current version is " + SystemUtils.JAVA_VERSION);
        }
    }

// org.apache.commons.lang3.CharEncodingTest::testWorksOnJava1_2_2
    public void testWorksOnJava1_2_2() {
        
        
        
        
        if (SystemUtils.isJavaVersionAtLeast(1.2f)) {
            this.assertSupportedEncoding(CharEncoding.ISO_8859_1);
            this.assertSupportedEncoding(CharEncoding.US_ASCII);
            this.assertSupportedEncoding(CharEncoding.UTF_8);
        } else {
            this.warn("Java 1.2 tests not run since the current version is " + SystemUtils.JAVA_VERSION);
        }
    }

// org.apache.commons.lang3.CharSetTest::testClass
    public void testClass() {
        assertEquals(true, Modifier.isPublic(CharSet.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(CharSet.class.getModifiers()));
    }

// org.apache.commons.lang3.CharSetTest::testGetInstance
    public void testGetInstance() {
        assertSame(CharSet.EMPTY, CharSet.getInstance( (String) null));
        assertSame(CharSet.EMPTY, CharSet.getInstance(""));
        assertSame(CharSet.ASCII_ALPHA, CharSet.getInstance("a-zA-Z"));
        assertSame(CharSet.ASCII_ALPHA, CharSet.getInstance("A-Za-z"));
        assertSame(CharSet.ASCII_ALPHA_LOWER, CharSet.getInstance("a-z"));
        assertSame(CharSet.ASCII_ALPHA_UPPER, CharSet.getInstance("A-Z"));
        assertSame(CharSet.ASCII_NUMERIC, CharSet.getInstance("0-9"));
    }

// org.apache.commons.lang3.CharSetTest::testGetInstance_Stringarray
    public void testGetInstance_Stringarray() {
        assertEquals(null, CharSet.getInstance((String[]) null));
        assertEquals("[]", CharSet.getInstance(new String[0]).toString());
        assertEquals("[]", CharSet.getInstance(new String[] {null}).toString());
        assertEquals("[a-e]", CharSet.getInstance(new String[] {"a-e"}).toString());
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_simple
    public void testConstructor_String_simple() {
        CharSet set;
        CharRange[] array;
        
        set = CharSet.getInstance((String) null);
        array = set.getCharRanges();
        assertEquals("[]", set.toString());
        assertEquals(0, array.length);
        
        set = CharSet.getInstance("");
        array = set.getCharRanges();
        assertEquals("[]", set.toString());
        assertEquals(0, array.length);
        
        set = CharSet.getInstance("a");
        array = set.getCharRanges();
        assertEquals("[a]", set.toString());
        assertEquals(1, array.length);
        assertEquals("a", array[0].toString());
        
        set = CharSet.getInstance("^a");
        array = set.getCharRanges();
        assertEquals("[^a]", set.toString());
        assertEquals(1, array.length);
        assertEquals("^a", array[0].toString());
        
        set = CharSet.getInstance("a-e");
        array = set.getCharRanges();
        assertEquals("[a-e]", set.toString());
        assertEquals(1, array.length);
        assertEquals("a-e", array[0].toString());
        
        set = CharSet.getInstance("^a-e");
        array = set.getCharRanges();
        assertEquals("[^a-e]", set.toString());
        assertEquals(1, array.length);
        assertEquals("^a-e", array[0].toString());
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_combo
    public void testConstructor_String_combo() {
        CharSet set;
        CharRange[] array;
        
        set = CharSet.getInstance("abc");
        array = set.getCharRanges();
        assertEquals(3, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c')));
        
        set = CharSet.getInstance("a-ce-f");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', 'c')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('e', 'f')));
        
        set = CharSet.getInstance("ae-f");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('e', 'f')));
        
        set = CharSet.getInstance("e-fa");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('e', 'f')));
        
        set = CharSet.getInstance("ae-fm-pz");
        array = set.getCharRanges();
        assertEquals(4, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('e', 'f')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('m', 'p')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('z')));
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_comboNegated
    public void testConstructor_String_comboNegated() {
        CharSet set;
        CharRange[] array;
        
        set = CharSet.getInstance("^abc");
        array = set.getCharRanges();
        assertEquals(3, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c')));
        
        set = CharSet.getInstance("b^ac");
        array = set.getCharRanges();
        assertEquals(3, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c')));
        
        set = CharSet.getInstance("db^ac");
        array = set.getCharRanges();
        assertEquals(4, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('d')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c')));
        
        set = CharSet.getInstance("^b^a");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a')));
        
        set = CharSet.getInstance("b^a-c^z");
        array = set.getCharRanges();
        assertEquals(3, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('a', 'c')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('z')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_oddDash
    public void testConstructor_String_oddDash() {
        CharSet set;
        CharRange[] array;
        
        set = CharSet.getInstance("-");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("--");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("---");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("----");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("-a");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        
        set = CharSet.getInstance("a-");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("a--");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', '-')));
        
        set = CharSet.getInstance("--a");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('-', 'a')));
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_oddNegate
    public void testConstructor_String_oddNegate() {
        CharSet set;
        CharRange[] array;
        set = CharSet.getInstance("^");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('^'))); 
        
        set = CharSet.getInstance("^^");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('^'))); 
        
        set = CharSet.getInstance("^^^");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('^'))); 
        
        set = CharSet.getInstance("^^^^");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('^'))); 
        
        set = CharSet.getInstance("a^");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('^'))); 
        
        set = CharSet.getInstance("^a-");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-'))); 
        
        set = CharSet.getInstance("^^-c");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('^', 'c'))); 
        
        set = CharSet.getInstance("^c-^");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('c', '^'))); 
        
        set = CharSet.getInstance("^c-^d");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('c', '^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('d'))); 
        
        set = CharSet.getInstance("^^-");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-'))); 
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_oddCombinations
    public void testConstructor_String_oddCombinations() {
        CharSet set;
        CharRange[] array = null;
        
        set = CharSet.getInstance("a-^c");
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', '^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c'))); 
        assertEquals(false, set.contains('b'));
        assertEquals(true, set.contains('^'));  
        assertEquals(true, set.contains('_')); 
        assertEquals(true, set.contains('c'));  
        
        set = CharSet.getInstance("^a-^c");
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('a', '^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c'))); 
        assertEquals(true, set.contains('b'));
        assertEquals(false, set.contains('^'));  
        assertEquals(false, set.contains('_')); 
        
        set = CharSet.getInstance("a- ^-- "); 
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', ' '))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('-', ' '))); 
        assertEquals(true, set.contains('#'));
        assertEquals(true, set.contains('^'));
        assertEquals(true, set.contains('a'));
        assertEquals(true, set.contains('*'));
        assertEquals(true, set.contains('A'));
        
        set = CharSet.getInstance("^-b");
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('^','b'))); 
        assertEquals(true, set.contains('b'));
        assertEquals(true, set.contains('_')); 
        assertEquals(false, set.contains('A'));
        assertEquals(true, set.contains('^')); 
        
        set = CharSet.getInstance("b-^");
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('^','b'))); 
        assertEquals(true, set.contains('b'));
        assertEquals(true, set.contains('^'));
        assertEquals(true, set.contains('a')); 
        assertEquals(false, set.contains('c')); 
    }

// org.apache.commons.lang3.CharSetTest::testEquals_Object
    public void testEquals_Object() {
        CharSet abc = CharSet.getInstance("abc");
        CharSet abc2 = CharSet.getInstance("abc");
        CharSet atoc = CharSet.getInstance("a-c");
        CharSet atoc2 = CharSet.getInstance("a-c");
        CharSet notatoc = CharSet.getInstance("^a-c");
        CharSet notatoc2 = CharSet.getInstance("^a-c");
        
        assertEquals(false, abc.equals(null));
        
        assertEquals(true, abc.equals(abc));
        assertEquals(true, abc.equals(abc2));
        assertEquals(false, abc.equals(atoc));
        assertEquals(false, abc.equals(notatoc));
        
        assertEquals(false, atoc.equals(abc));
        assertEquals(true, atoc.equals(atoc));
        assertEquals(true, atoc.equals(atoc2));
        assertEquals(false, atoc.equals(notatoc));
        
        assertEquals(false, notatoc.equals(abc));
        assertEquals(false, notatoc.equals(atoc));
        assertEquals(true, notatoc.equals(notatoc));
        assertEquals(true, notatoc.equals(notatoc2));
    }

// org.apache.commons.lang3.CharSetTest::testHashCode
    public void testHashCode() {
        CharSet abc = CharSet.getInstance("abc");
        CharSet abc2 = CharSet.getInstance("abc");
        CharSet atoc = CharSet.getInstance("a-c");
        CharSet atoc2 = CharSet.getInstance("a-c");
        CharSet notatoc = CharSet.getInstance("^a-c");
        CharSet notatoc2 = CharSet.getInstance("^a-c");
        
        assertEquals(abc.hashCode(), abc.hashCode());
        assertEquals(abc.hashCode(), abc2.hashCode());
        assertEquals(atoc.hashCode(), atoc.hashCode());
        assertEquals(atoc.hashCode(), atoc2.hashCode());
        assertEquals(notatoc.hashCode(), notatoc.hashCode());
        assertEquals(notatoc.hashCode(), notatoc2.hashCode());
    }

// org.apache.commons.lang3.CharSetTest::testContains_Char
    public void testContains_Char() {
        CharSet btod = CharSet.getInstance("b-d");
        CharSet dtob = CharSet.getInstance("d-b");
        CharSet bcd = CharSet.getInstance("bcd");
        CharSet bd = CharSet.getInstance("bd");
        CharSet notbtod = CharSet.getInstance("^b-d");
        
        assertEquals(false, btod.contains('a'));
        assertEquals(true, btod.contains('b'));
        assertEquals(true, btod.contains('c'));
        assertEquals(true, btod.contains('d'));
        assertEquals(false, btod.contains('e'));
        
        assertEquals(false, bcd.contains('a'));
        assertEquals(true, bcd.contains('b'));
        assertEquals(true, bcd.contains('c'));
        assertEquals(true, bcd.contains('d'));
        assertEquals(false, bcd.contains('e'));
        
        assertEquals(false, bd.contains('a'));
        assertEquals(true, bd.contains('b'));
        assertEquals(false, bd.contains('c'));
        assertEquals(true, bd.contains('d'));
        assertEquals(false, bd.contains('e'));
        
        assertEquals(true, notbtod.contains('a'));
        assertEquals(false, notbtod.contains('b'));
        assertEquals(false, notbtod.contains('c'));
        assertEquals(false, notbtod.contains('d'));
        assertEquals(true, notbtod.contains('e'));
        
        assertEquals(false, dtob.contains('a'));
        assertEquals(true, dtob.contains('b'));
        assertEquals(true, dtob.contains('c'));
        assertEquals(true, dtob.contains('d'));
        assertEquals(false, dtob.contains('e'));
      
        CharRange[] array = dtob.getCharRanges();
        assertEquals("[b-d]", dtob.toString());
        assertEquals(1, array.length);
    }

// org.apache.commons.lang3.CharSetTest::testSerialization
    public void testSerialization() {
        CharSet set = CharSet.getInstance("a");
        assertEquals(set, SerializationUtils.clone(set)); 
        set = CharSet.getInstance("a-e");
        assertEquals(set, SerializationUtils.clone(set)); 
        set = CharSet.getInstance("be-f^a-z");
        assertEquals(set, SerializationUtils.clone(set)); 
    }

// org.apache.commons.lang3.CharSetTest::testStatics
    public void testStatics() {
        CharRange[] array;
        
        array = CharSet.EMPTY.getCharRanges();
        assertEquals(0, array.length);
        
        array = CharSet.ASCII_ALPHA.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', 'z')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('A', 'Z')));
        
        array = CharSet.ASCII_ALPHA_LOWER.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', 'z')));
        
        array = CharSet.ASCII_ALPHA_UPPER.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('A', 'Z')));
        
        array = CharSet.ASCII_NUMERIC.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('0', '9')));
    }

// org.apache.commons.lang3.CharSetUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new CharSetUtils());
        Constructor<?>[] cons = CharSetUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(CharSetUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(CharSetUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.CharSetUtilsTest::testSqueeze_StringString
    public void testSqueeze_StringString() {
        assertEquals(null, CharSetUtils.squeeze(null, (String) null));
        assertEquals(null, CharSetUtils.squeeze(null, ""));
        
        assertEquals("", CharSetUtils.squeeze("", (String) null));
        assertEquals("", CharSetUtils.squeeze("", ""));
        assertEquals("", CharSetUtils.squeeze("", "a-e"));
        
        assertEquals("hello", CharSetUtils.squeeze("hello", (String) null));
        assertEquals("hello", CharSetUtils.squeeze("hello", ""));
        assertEquals("hello", CharSetUtils.squeeze("hello", "a-e"));
        assertEquals("helo", CharSetUtils.squeeze("hello", "l-p"));
        assertEquals("heloo", CharSetUtils.squeeze("helloo", "l"));
        assertEquals("hello", CharSetUtils.squeeze("helloo", "^l"));
    }

// org.apache.commons.lang3.CharSetUtilsTest::testSqueeze_StringStringarray
    public void testSqueeze_StringStringarray() {
        assertEquals(null, CharSetUtils.squeeze(null, (String[]) null));
        assertEquals(null, CharSetUtils.squeeze(null, new String[0]));
        assertEquals(null, CharSetUtils.squeeze(null, new String[] {null}));
        assertEquals(null, CharSetUtils.squeeze(null, new String[] {"el"}));
        
        assertEquals("", CharSetUtils.squeeze("", (String[]) null));
        assertEquals("", CharSetUtils.squeeze("", new String[0]));
        assertEquals("", CharSetUtils.squeeze("", new String[] {null}));
        assertEquals("", CharSetUtils.squeeze("", new String[] {"a-e"}));
        
        assertEquals("hello", CharSetUtils.squeeze("hello", (String[]) null));
        assertEquals("hello", CharSetUtils.squeeze("hello", new String[0]));
        assertEquals("hello", CharSetUtils.squeeze("hello", new String[] {null}));
        assertEquals("hello", CharSetUtils.squeeze("hello", new String[] {"a-e"}));
        
        assertEquals("helo", CharSetUtils.squeeze("hello", new String[] { "el" }));
        assertEquals("hello", CharSetUtils.squeeze("hello", new String[] { "e" }));
        assertEquals("fofof", CharSetUtils.squeeze("fooffooff", new String[] { "of" }));
        assertEquals("fof", CharSetUtils.squeeze("fooooff", new String[] { "fo" }));
    }

// org.apache.commons.lang3.CharSetUtilsTest::testCount_StringString
    public void testCount_StringString() {
        assertEquals(0, CharSetUtils.count(null, (String) null));
        assertEquals(0, CharSetUtils.count(null, ""));
        
        assertEquals(0, CharSetUtils.count("", (String) null));
        assertEquals(0, CharSetUtils.count("", ""));
        assertEquals(0, CharSetUtils.count("", "a-e"));
        
        assertEquals(0, CharSetUtils.count("hello", (String) null));
        assertEquals(0, CharSetUtils.count("hello", ""));
        assertEquals(1, CharSetUtils.count("hello", "a-e"));
        assertEquals(3, CharSetUtils.count("hello", "l-p"));
    }

// org.apache.commons.lang3.CharSetUtilsTest::testCount_StringStringarray
    public void testCount_StringStringarray() {
        assertEquals(0, CharSetUtils.count(null, (String[]) null));
        assertEquals(0, CharSetUtils.count(null, new String[0]));
        assertEquals(0, CharSetUtils.count(null, new String[] {null}));
        assertEquals(0, CharSetUtils.count(null, new String[] {"a-e"}));
        
        assertEquals(0, CharSetUtils.count("", (String[]) null));
        assertEquals(0, CharSetUtils.count("", new String[0]));
        assertEquals(0, CharSetUtils.count("", new String[] {null}));
        assertEquals(0, CharSetUtils.count("", new String[] {"a-e"}));
        
        assertEquals(0, CharSetUtils.count("hello", (String[]) null));
        assertEquals(0, CharSetUtils.count("hello", new String[0]));
        assertEquals(0, CharSetUtils.count("hello", new String[] {null}));
        assertEquals(1, CharSetUtils.count("hello", new String[] {"a-e"}));
        
        assertEquals(3, CharSetUtils.count("hello", new String[] { "el" }));
        assertEquals(0, CharSetUtils.count("hello", new String[] { "x" }));
        assertEquals(2, CharSetUtils.count("hello", new String[] { "e-i" }));
        assertEquals(5, CharSetUtils.count("hello", new String[] { "a-z" }));
        assertEquals(0, CharSetUtils.count("hello", new String[] { "" }));
    }

// org.apache.commons.lang3.CharSetUtilsTest::testKeep_StringString
    public void testKeep_StringString() {
        assertEquals(null, CharSetUtils.keep(null, (String) null));
        assertEquals(null, CharSetUtils.keep(null, ""));
        
        assertEquals("", CharSetUtils.keep("", (String) null));
        assertEquals("", CharSetUtils.keep("", ""));
        assertEquals("", CharSetUtils.keep("", "a-e"));
        
        assertEquals("", CharSetUtils.keep("hello", (String) null));
        assertEquals("", CharSetUtils.keep("hello", ""));
        assertEquals("", CharSetUtils.keep("hello", "xyz"));
        assertEquals("hello", CharSetUtils.keep("hello", "a-z"));
        assertEquals("hello", CharSetUtils.keep("hello", "oleh"));
        assertEquals("ell", CharSetUtils.keep("hello", "el"));
    }

// org.apache.commons.lang3.CharSetUtilsTest::testKeep_StringStringarray
    public void testKeep_StringStringarray() {
        assertEquals(null, CharSetUtils.keep(null, (String[]) null));
        assertEquals(null, CharSetUtils.keep(null, new String[0]));
        assertEquals(null, CharSetUtils.keep(null, new String[] {null}));
        assertEquals(null, CharSetUtils.keep(null, new String[] {"a-e"}));
        
        assertEquals("", CharSetUtils.keep("", (String[]) null));
        assertEquals("", CharSetUtils.keep("", new String[0]));
        assertEquals("", CharSetUtils.keep("", new String[] {null}));
        assertEquals("", CharSetUtils.keep("", new String[] {"a-e"}));
        
        assertEquals("", CharSetUtils.keep("hello", (String[]) null));
        assertEquals("", CharSetUtils.keep("hello", new String[0]));
        assertEquals("", CharSetUtils.keep("hello", new String[] {null}));
        assertEquals("e", CharSetUtils.keep("hello", new String[] {"a-e"}));
        
        assertEquals("e", CharSetUtils.keep("hello", new String[] { "a-e" }));
        assertEquals("ell", CharSetUtils.keep("hello", new String[] { "el" }));
        assertEquals("hello", CharSetUtils.keep("hello", new String[] { "elho" }));
        assertEquals("hello", CharSetUtils.keep("hello", new String[] { "a-z" }));
        assertEquals("----", CharSetUtils.keep("----", new String[] { "-" }));
        assertEquals("ll", CharSetUtils.keep("hello", new String[] { "l" }));
    }

// org.apache.commons.lang3.CharSetUtilsTest::testDelete_StringString
    public void testDelete_StringString() {
        assertEquals(null, CharSetUtils.delete(null, (String) null));
        assertEquals(null, CharSetUtils.delete(null, ""));
        
        assertEquals("", CharSetUtils.delete("", (String) null));
        assertEquals("", CharSetUtils.delete("", ""));
        assertEquals("", CharSetUtils.delete("", "a-e"));
        
        assertEquals("hello", CharSetUtils.delete("hello", (String) null));
        assertEquals("hello", CharSetUtils.delete("hello", ""));
        assertEquals("hllo", CharSetUtils.delete("hello", "a-e"));
        assertEquals("he", CharSetUtils.delete("hello", "l-p"));
        assertEquals("hello", CharSetUtils.delete("hello", "z"));
    }

// org.apache.commons.lang3.CharSetUtilsTest::testDelete_StringStringarray
    public void testDelete_StringStringarray() {
        assertEquals(null, CharSetUtils.delete(null, (String[]) null));
        assertEquals(null, CharSetUtils.delete(null, new String[0]));
        assertEquals(null, CharSetUtils.delete(null, new String[] {null}));
        assertEquals(null, CharSetUtils.delete(null, new String[] {"el"}));
        
        assertEquals("", CharSetUtils.delete("", (String[]) null));
        assertEquals("", CharSetUtils.delete("", new String[0]));
        assertEquals("", CharSetUtils.delete("", new String[] {null}));
        assertEquals("", CharSetUtils.delete("", new String[] {"a-e"}));
        
        assertEquals("hello", CharSetUtils.delete("hello", (String[]) null));
        assertEquals("hello", CharSetUtils.delete("hello", new String[0]));
        assertEquals("hello", CharSetUtils.delete("hello", new String[] {null}));
        assertEquals("hello", CharSetUtils.delete("hello", new String[] {"xyz"}));

        assertEquals("ho", CharSetUtils.delete("hello", new String[] { "el" }));
        assertEquals("", CharSetUtils.delete("hello", new String[] { "elho" }));
        assertEquals("hello", CharSetUtils.delete("hello", new String[] { "" }));
        assertEquals("hello", CharSetUtils.delete("hello", ""));
        assertEquals("", CharSetUtils.delete("hello", new String[] { "a-z" }));
        assertEquals("", CharSetUtils.delete("----", new String[] { "-" }));
        assertEquals("heo", CharSetUtils.delete("hello", new String[] { "l" }));
    }

// org.apache.commons.lang3.ClassUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new ClassUtils());
        Constructor<?>[] cons = ClassUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(ClassUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ClassUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getShortClassName_Object
    public void test_getShortClassName_Object() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(new ClassUtils(), "<null>"));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortClassName(new Inner(), "<null>"));
        assertEquals("String", ClassUtils.getShortClassName("hello", "<null>"));
        assertEquals("<null>", ClassUtils.getShortClassName(null, "<null>"));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getShortClassName_Class
    public void test_getShortClassName_Class() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class));
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));

        
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        assertEquals("Map.Entry[]", ClassUtils.getShortClassName(Map.Entry[].class));

        
        assertEquals("boolean", ClassUtils.getShortClassName(boolean.class));
        assertEquals("byte", ClassUtils.getShortClassName(byte.class));
        assertEquals("char", ClassUtils.getShortClassName(char.class));
        assertEquals("short", ClassUtils.getShortClassName(short.class));
        assertEquals("int", ClassUtils.getShortClassName(int.class));
        assertEquals("long", ClassUtils.getShortClassName(long.class));
        assertEquals("float", ClassUtils.getShortClassName(float.class));
        assertEquals("double", ClassUtils.getShortClassName(double.class));

        
        assertEquals("boolean[]", ClassUtils.getShortClassName(boolean[].class));
        assertEquals("byte[]", ClassUtils.getShortClassName(byte[].class));
        assertEquals("char[]", ClassUtils.getShortClassName(char[].class));
        assertEquals("short[]", ClassUtils.getShortClassName(short[].class));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        assertEquals("long[]", ClassUtils.getShortClassName(long[].class));
        assertEquals("float[]", ClassUtils.getShortClassName(float[].class));
        assertEquals("double[]", ClassUtils.getShortClassName(double[].class));

        
        assertEquals("String[][]", ClassUtils.getShortClassName(String[][].class));
        assertEquals("String[][][]", ClassUtils.getShortClassName(String[][][].class));
        assertEquals("String[][][][]", ClassUtils.getShortClassName(String[][][][].class));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getShortClassName_String
    public void test_getShortClassName_String() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class.getName()));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getPackageName_Object
    public void test_getPackageName_Object() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(new ClassUtils(), "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(new Inner(), "<null>"));
        assertEquals("<null>", ClassUtils.getPackageName(null, "<null>"));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getPackageName_Class
    public void test_getPackageName_Class() {
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class));
        assertEquals("", ClassUtils.getPackageName((Class<?>)null));

        
        assertEquals("java.lang", ClassUtils.getPackageName(String[].class));

        
        assertEquals("", ClassUtils.getPackageName(boolean[].class));
        assertEquals("", ClassUtils.getPackageName(byte[].class));
        assertEquals("", ClassUtils.getPackageName(char[].class));
        assertEquals("", ClassUtils.getPackageName(short[].class));
        assertEquals("", ClassUtils.getPackageName(int[].class));
        assertEquals("", ClassUtils.getPackageName(long[].class));
        assertEquals("", ClassUtils.getPackageName(float[].class));
        assertEquals("", ClassUtils.getPackageName(double[].class));

        
        assertEquals("java.lang", ClassUtils.getPackageName(String[][].class));
        assertEquals("java.lang", ClassUtils.getPackageName(String[][][].class));
        assertEquals("java.lang", ClassUtils.getPackageName(String[][][][].class));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getPackageName_String
    public void test_getPackageName_String() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(ClassUtils.class.getName()));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getPackageName((String)null));
        assertEquals("", ClassUtils.getPackageName(""));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getAllSuperclasses_Class
    public void test_getAllSuperclasses_Class() {
        List<?> list = ClassUtils.getAllSuperclasses(CY.class);
        assertEquals(2, list.size());
        assertEquals(CX.class, list.get(0));
        assertEquals(Object.class, list.get(1));
        
        assertEquals(null, ClassUtils.getAllSuperclasses(null));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getAllInterfaces_Class
    public void test_getAllInterfaces_Class() {
        List<?> list = ClassUtils.getAllInterfaces(CY.class);
        assertEquals(6, list.size());
        assertEquals(IB.class, list.get(0));
        assertEquals(IC.class, list.get(1));
        assertEquals(ID.class, list.get(2));
        assertEquals(IE.class, list.get(3));
        assertEquals(IF.class, list.get(4));
        assertEquals(IA.class, list.get(5));
        
        assertEquals(null, ClassUtils.getAllInterfaces(null));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_convertClassNamesToClasses_List
    public void test_convertClassNamesToClasses_List() {
        List<String> list = new ArrayList<String>();
        List<Class<?>> result = ClassUtils.convertClassNamesToClasses(list);
        assertEquals(0, result.size());
        
        list.add("java.lang.String");
        list.add("java.lang.xxx");
        list.add("java.lang.Object");
        result = ClassUtils.convertClassNamesToClasses(list);
        assertEquals(3, result.size());
        assertEquals(String.class, result.get(0));
        assertEquals(null, result.get(1));
        assertEquals(Object.class, result.get(2));

        @SuppressWarnings("unchecked") 
        List<Object> olist = (List<Object>)(List<?>)list; 
        olist.add(new Object());
        try {
            ClassUtils.convertClassNamesToClasses(list);
            fail("Should not have been able to convert list");
        } catch (ClassCastException expected) {}
        assertEquals(null, ClassUtils.convertClassNamesToClasses(null));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_convertClassesToClassNames_List
    public void test_convertClassesToClassNames_List() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        List<String> result = ClassUtils.convertClassesToClassNames(list);
        assertEquals(0, result.size());
        
        list.add(String.class);
        list.add(null);
        list.add(Object.class);
        result = ClassUtils.convertClassesToClassNames(list);
        assertEquals(3, result.size());
        assertEquals("java.lang.String", result.get(0));
        assertEquals(null, result.get(1));
        assertEquals("java.lang.Object", result.get(2));

        @SuppressWarnings("unchecked") 
        List<Object> olist = (List<Object>)(List<?>)list; 
        olist.add(new Object());
        try {
            ClassUtils.convertClassesToClassNames(list);
            fail("Should not have been able to convert list");
        } catch (ClassCastException expected) {}
        assertEquals(null, ClassUtils.convertClassesToClassNames(null));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_isInnerClass_Class
    public void test_isInnerClass_Class() {
        assertEquals(true, ClassUtils.isInnerClass(Inner.class));
        assertEquals(true, ClassUtils.isInnerClass(Map.Entry.class));
        assertEquals(true, ClassUtils.isInnerClass(new Cloneable() {
        }.getClass()));
        assertEquals(false, ClassUtils.isInnerClass(this.getClass()));
        assertEquals(false, ClassUtils.isInnerClass(String.class));
        assertEquals(false, ClassUtils.isInnerClass(null));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_isAssignable_ClassArray_ClassArray
    public void test_isAssignable_ClassArray_ClassArray() throws Exception {
        Class<?>[] array2 = new Class[] {Object.class, Object.class};
        Class<?>[] array1 = new Class[] {Object.class};
        Class<?>[] array1s = new Class[] {String.class};
        Class<?>[] array0 = new Class[] {};

        assertFalse(ClassUtils.isAssignable(array1, array2));
        assertFalse(ClassUtils.isAssignable(null, array2));
        assertTrue(ClassUtils.isAssignable(null, array0));
        assertTrue(ClassUtils.isAssignable(array0, array0));
        assertTrue(ClassUtils.isAssignable(array0, null));
        assertTrue(ClassUtils.isAssignable((Class[]) null, (Class[]) null));
        
        assertFalse(ClassUtils.isAssignable(array1, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_isAssignable
    public void test_isAssignable() throws Exception {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null));
        assertFalse(ClassUtils.isAssignable(String.class, null));
        
        assertTrue(ClassUtils.isAssignable(null, Object.class));
        assertTrue(ClassUtils.isAssignable(null, Integer.class));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(String.class, Object.class));
        assertTrue(ClassUtils.isAssignable(String.class, String.class));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class));
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Boolean.class));
        assertFalse(ClassUtils.isAssignable(Boolean.class, Boolean.TYPE));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_isAssignable_Autoboxing
    public void test_isAssignable_Autoboxing() throws Exception {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null, true));
        assertFalse(ClassUtils.isAssignable(String.class, null, true));

        assertTrue(ClassUtils.isAssignable(null, Object.class, true));
        assertTrue(ClassUtils.isAssignable(null, Integer.class, true));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(String.class, Object.class, true));
        assertTrue(ClassUtils.isAssignable(String.class, String.class, true));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class, true));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_isAssignable_Widening
    public void test_isAssignable_Widening() throws Exception {
        
        assertFalse("byte -> char", ClassUtils.isAssignable(Byte.TYPE, Character.TYPE));
        assertTrue("byte -> byte", ClassUtils.isAssignable(Byte.TYPE, Byte.TYPE));
        assertTrue("byte -> short", ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        assertTrue("byte -> int", ClassUtils.isAssignable(Byte.TYPE, Integer.TYPE));
        assertTrue("byte -> long", ClassUtils.isAssignable(Byte.TYPE, Long.TYPE));
        assertTrue("byte -> float", ClassUtils.isAssignable(Byte.TYPE, Float.TYPE));
        assertTrue("byte -> double", ClassUtils.isAssignable(Byte.TYPE, Double.TYPE));
        assertFalse("byte -> boolean", ClassUtils.isAssignable(Byte.TYPE, Boolean.TYPE));
        
        
        assertFalse("short -> char", ClassUtils.isAssignable(Short.TYPE, Character.TYPE));
        assertFalse("short -> byte", ClassUtils.isAssignable(Short.TYPE, Byte.TYPE));
        assertTrue("short -> short", ClassUtils.isAssignable(Short.TYPE, Short.TYPE));
        assertTrue("short -> int", ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        assertTrue("short -> long", ClassUtils.isAssignable(Short.TYPE, Long.TYPE));
        assertTrue("short -> float", ClassUtils.isAssignable(Short.TYPE, Float.TYPE));
        assertTrue("short -> double", ClassUtils.isAssignable(Short.TYPE, Double.TYPE));
        assertFalse("short -> boolean", ClassUtils.isAssignable(Short.TYPE, Boolean.TYPE));
        
        
        assertTrue("char -> char", ClassUtils.isAssignable(Character.TYPE, Character.TYPE));
        assertFalse("char -> byte", ClassUtils.isAssignable(Character.TYPE, Byte.TYPE));
        assertFalse("char -> short", ClassUtils.isAssignable(Character.TYPE, Short.TYPE));
        assertTrue("char -> int", ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        assertTrue("char -> long", ClassUtils.isAssignable(Character.TYPE, Long.TYPE));
        assertTrue("char -> float", ClassUtils.isAssignable(Character.TYPE, Float.TYPE));
        assertTrue("char -> double", ClassUtils.isAssignable(Character.TYPE, Double.TYPE));
        assertFalse("char -> boolean", ClassUtils.isAssignable(Character.TYPE, Boolean.TYPE));
        
        
        assertFalse("int -> char", ClassUtils.isAssignable(Integer.TYPE, Character.TYPE));
        assertFalse("int -> byte", ClassUtils.isAssignable(Integer.TYPE, Byte.TYPE));
        assertFalse("int -> short", ClassUtils.isAssignable(Integer.TYPE, Short.TYPE));
        assertTrue("int -> int", ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertTrue("int -> long", ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        assertTrue("int -> float", ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        assertTrue("int -> double", ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));
        assertFalse("int -> boolean", ClassUtils.isAssignable(Integer.TYPE, Boolean.TYPE));
 
        
        assertFalse("long -> char", ClassUtils.isAssignable(Long.TYPE, Character.TYPE));
        assertFalse("long -> byte", ClassUtils.isAssignable(Long.TYPE, Byte.TYPE));
        assertFalse("long -> short", ClassUtils.isAssignable(Long.TYPE, Short.TYPE));
        assertFalse("long -> int", ClassUtils.isAssignable(Long.TYPE, Integer.TYPE));
        assertTrue("long -> long", ClassUtils.isAssignable(Long.TYPE, Long.TYPE));
        assertTrue("long -> float", ClassUtils.isAssignable(Long.TYPE, Float.TYPE));
        assertTrue("long -> double", ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        assertFalse("long -> boolean", ClassUtils.isAssignable(Long.TYPE, Boolean.TYPE));
 
        
        assertFalse("float -> char", ClassUtils.isAssignable(Float.TYPE, Character.TYPE));
        assertFalse("float -> byte", ClassUtils.isAssignable(Float.TYPE, Byte.TYPE));
        assertFalse("float -> short", ClassUtils.isAssignable(Float.TYPE, Short.TYPE));
        assertFalse("float -> int", ClassUtils.isAssignable(Float.TYPE, Integer.TYPE));
        assertFalse("float -> long", ClassUtils.isAssignable(Float.TYPE, Long.TYPE));
        assertTrue("float -> float", ClassUtils.isAssignable(Float.TYPE, Float.TYPE));
        assertTrue("float -> double", ClassUtils.isAssignable(Float.TYPE, Double.TYPE));
        assertFalse("float -> boolean", ClassUtils.isAssignable(Float.TYPE, Boolean.TYPE));
        
        
        assertFalse("double -> char", ClassUtils.isAssignable(Double.TYPE, Character.TYPE));
        assertFalse("double -> byte", ClassUtils.isAssignable(Double.TYPE, Byte.TYPE));
        assertFalse("double -> short", ClassUtils.isAssignable(Double.TYPE, Short.TYPE));
        assertFalse("double -> int", ClassUtils.isAssignable(Double.TYPE, Integer.TYPE));
        assertFalse("double -> long", ClassUtils.isAssignable(Double.TYPE, Long.TYPE));
        assertFalse("double -> float", ClassUtils.isAssignable(Double.TYPE, Float.TYPE));
        assertTrue("double -> double", ClassUtils.isAssignable(Double.TYPE, Double.TYPE));
        assertFalse("double -> boolean", ClassUtils.isAssignable(Double.TYPE, Boolean.TYPE));
        
        
        assertFalse("boolean -> char", ClassUtils.isAssignable(Boolean.TYPE, Character.TYPE));
        assertFalse("boolean -> byte", ClassUtils.isAssignable(Boolean.TYPE, Byte.TYPE));
        assertFalse("boolean -> short", ClassUtils.isAssignable(Boolean.TYPE, Short.TYPE));
        assertFalse("boolean -> int", ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
        assertFalse("boolean -> long", ClassUtils.isAssignable(Boolean.TYPE, Long.TYPE));
        assertFalse("boolean -> float", ClassUtils.isAssignable(Boolean.TYPE, Float.TYPE));
        assertFalse("boolean -> double", ClassUtils.isAssignable(Boolean.TYPE, Double.TYPE));
        assertTrue("boolean -> boolean", ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_isAssignable_Unboxing_Widening
    public void test_isAssignable_Unboxing_Widening() throws Exception {
        
        assertFalse("byte -> char", ClassUtils.isAssignable(Byte.class, Character.TYPE, true));
        assertTrue("byte -> byte", ClassUtils.isAssignable(Byte.class, Byte.TYPE, true));
        assertTrue("byte -> short", ClassUtils.isAssignable(Byte.class, Short.TYPE, true));
        assertTrue("byte -> int", ClassUtils.isAssignable(Byte.class, Integer.TYPE, true));
        assertTrue("byte -> long", ClassUtils.isAssignable(Byte.class, Long.TYPE, true));
        assertTrue("byte -> float", ClassUtils.isAssignable(Byte.class, Float.TYPE, true));
        assertTrue("byte -> double", ClassUtils.isAssignable(Byte.class, Double.TYPE, true));
        assertFalse("byte -> boolean", ClassUtils.isAssignable(Byte.class, Boolean.TYPE, true));
        
        
        assertFalse("short -> char", ClassUtils.isAssignable(Short.class, Character.TYPE, true));
        assertFalse("short -> byte", ClassUtils.isAssignable(Short.class, Byte.TYPE, true));
        assertTrue("short -> short", ClassUtils.isAssignable(Short.class, Short.TYPE, true));
        assertTrue("short -> int", ClassUtils.isAssignable(Short.class, Integer.TYPE, true));
        assertTrue("short -> long", ClassUtils.isAssignable(Short.class, Long.TYPE, true));
        assertTrue("short -> float", ClassUtils.isAssignable(Short.class, Float.TYPE, true));
        assertTrue("short -> double", ClassUtils.isAssignable(Short.class, Double.TYPE, true));
        assertFalse("short -> boolean", ClassUtils.isAssignable(Short.class, Boolean.TYPE, true));
        
        
        assertTrue("char -> char", ClassUtils.isAssignable(Character.class, Character.TYPE, true));
        assertFalse("char -> byte", ClassUtils.isAssignable(Character.class, Byte.TYPE, true));
        assertFalse("char -> short", ClassUtils.isAssignable(Character.class, Short.TYPE, true));
        assertTrue("char -> int", ClassUtils.isAssignable(Character.class, Integer.TYPE, true));
        assertTrue("char -> long", ClassUtils.isAssignable(Character.class, Long.TYPE, true));
        assertTrue("char -> float", ClassUtils.isAssignable(Character.class, Float.TYPE, true));
        assertTrue("char -> double", ClassUtils.isAssignable(Character.class, Double.TYPE, true));
        assertFalse("char -> boolean", ClassUtils.isAssignable(Character.class, Boolean.TYPE, true));
        
        
        assertFalse("int -> char", ClassUtils.isAssignable(Integer.class, Character.TYPE, true));
        assertFalse("int -> byte", ClassUtils.isAssignable(Integer.class, Byte.TYPE, true));
        assertFalse("int -> short", ClassUtils.isAssignable(Integer.class, Short.TYPE, true));
        assertTrue("int -> int", ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertTrue("int -> long", ClassUtils.isAssignable(Integer.class, Long.TYPE, true));
        assertTrue("int -> float", ClassUtils.isAssignable(Integer.class, Float.TYPE, true));
        assertTrue("int -> double", ClassUtils.isAssignable(Integer.class, Double.TYPE, true));
        assertFalse("int -> boolean", ClassUtils.isAssignable(Integer.class, Boolean.TYPE, true));
        
        
        assertFalse("long -> char", ClassUtils.isAssignable(Long.class, Character.TYPE, true));
        assertFalse("long -> byte", ClassUtils.isAssignable(Long.class, Byte.TYPE, true));
        assertFalse("long -> short", ClassUtils.isAssignable(Long.class, Short.TYPE, true));
        assertFalse("long -> int", ClassUtils.isAssignable(Long.class, Integer.TYPE, true));
        assertTrue("long -> long", ClassUtils.isAssignable(Long.class, Long.TYPE, true));
        assertTrue("long -> float", ClassUtils.isAssignable(Long.class, Float.TYPE, true));
        assertTrue("long -> double", ClassUtils.isAssignable(Long.class, Double.TYPE, true));
        assertFalse("long -> boolean", ClassUtils.isAssignable(Long.class, Boolean.TYPE, true));
        
        
        assertFalse("float -> char", ClassUtils.isAssignable(Float.class, Character.TYPE, true));
        assertFalse("float -> byte", ClassUtils.isAssignable(Float.class, Byte.TYPE, true));
        assertFalse("float -> short", ClassUtils.isAssignable(Float.class, Short.TYPE, true));
        assertFalse("float -> int", ClassUtils.isAssignable(Float.class, Integer.TYPE, true));
        assertFalse("float -> long", ClassUtils.isAssignable(Float.class, Long.TYPE, true));
        assertTrue("float -> float", ClassUtils.isAssignable(Float.class, Float.TYPE, true));
        assertTrue("float -> double", ClassUtils.isAssignable(Float.class, Double.TYPE, true));
        assertFalse("float -> boolean", ClassUtils.isAssignable(Float.class, Boolean.TYPE, true));
        
        
        assertFalse("double -> char", ClassUtils.isAssignable(Double.class, Character.TYPE, true));
        assertFalse("double -> byte", ClassUtils.isAssignable(Double.class, Byte.TYPE, true));
        assertFalse("double -> short", ClassUtils.isAssignable(Double.class, Short.TYPE, true));
        assertFalse("double -> int", ClassUtils.isAssignable(Double.class, Integer.TYPE, true));
        assertFalse("double -> long", ClassUtils.isAssignable(Double.class, Long.TYPE, true));
        assertFalse("double -> float", ClassUtils.isAssignable(Double.class, Float.TYPE, true));
        assertTrue("double -> double", ClassUtils.isAssignable(Double.class, Double.TYPE, true));
        assertFalse("double -> boolean", ClassUtils.isAssignable(Double.class, Boolean.TYPE, true));
        
        
        assertFalse("boolean -> char", ClassUtils.isAssignable(Boolean.class, Character.TYPE, true));
        assertFalse("boolean -> byte", ClassUtils.isAssignable(Boolean.class, Byte.TYPE, true));
        assertFalse("boolean -> short", ClassUtils.isAssignable(Boolean.class, Short.TYPE, true));
        assertFalse("boolean -> int", ClassUtils.isAssignable(Boolean.class, Integer.TYPE, true));
        assertFalse("boolean -> long", ClassUtils.isAssignable(Boolean.class, Long.TYPE, true));
        assertFalse("boolean -> float", ClassUtils.isAssignable(Boolean.class, Float.TYPE, true));
        assertFalse("boolean -> double", ClassUtils.isAssignable(Boolean.class, Double.TYPE, true));
        assertTrue("boolean -> boolean", ClassUtils.isAssignable(Boolean.class, Boolean.TYPE, true));
    }

// org.apache.commons.lang3.ClassUtilsTest::testPrimitiveToWrapper
    public void testPrimitiveToWrapper() {
       
        
        assertEquals("boolean -> Boolean.class", 
            Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));   
        assertEquals("byte -> Byte.class",
            Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        assertEquals("char -> Character.class",
            Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        assertEquals("short -> Short.class",
            Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        assertEquals("int -> Integer.class",
            Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals("long -> Long.class",
            Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        assertEquals("double -> Double.class",
            Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        assertEquals("float -> Float.class",
            Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));
        
        
        assertEquals("String.class -> String.class",
            String.class, ClassUtils.primitiveToWrapper(String.class));
        assertEquals("ClassUtils.class -> ClassUtils.class",
            org.apache.commons.lang3.ClassUtils.class, 
            ClassUtils.primitiveToWrapper(org.apache.commons.lang3.ClassUtils.class));
        assertEquals("Void.TYPE -> Void.TYPE",
            Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
            
        
        assertNull("null -> null",
            ClassUtils.primitiveToWrapper(null));
    }

// org.apache.commons.lang3.ClassUtilsTest::testPrimitivesToWrappers
    public void testPrimitivesToWrappers() {
        
        assertNull("null -> null",
            ClassUtils.primitivesToWrappers(null));
        
        assertEquals("empty -> empty",
                ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.primitivesToWrappers(ArrayUtils.EMPTY_CLASS_ARRAY));

        
        final Class<?>[] primitives = new Class[] {
                Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE, 
                Integer.TYPE, Long.TYPE, Double.TYPE, Float.TYPE,
                String.class, ClassUtils.class
        };
        Class<?>[] wrappers= ClassUtils.primitivesToWrappers(primitives);
        
        for (int i=0; i < primitives.length; i++) {
            
            Class<?> primitive = primitives[i];
            Class<?> expectedWrapper = ClassUtils.primitiveToWrapper(primitive);
            
            assertEquals(primitive + " -> " + expectedWrapper, expectedWrapper, wrappers[i]);
        }

        
        final Class<?>[] noPrimitives = new Class[] {
                String.class, ClassUtils.class, Void.TYPE
        };
        
        assertNotSame("unmodified", noPrimitives, ClassUtils.primitivesToWrappers(noPrimitives));
    }

// org.apache.commons.lang3.ClassUtilsTest::testWrapperToPrimitive
    public void testWrapperToPrimitive() {
        
        final Class<?>[] primitives = {
                Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE,
                Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
        };
        for (int i = 0; i < primitives.length; i++) {
            Class<?> wrapperCls = ClassUtils.primitiveToWrapper(primitives[i]);
            assertFalse("Still primitive", wrapperCls.isPrimitive());
            assertEquals(wrapperCls + " -> " + primitives[i], primitives[i],
                    ClassUtils.wrapperToPrimitive(wrapperCls));
        }
    }

// org.apache.commons.lang3.ClassUtilsTest::testWrapperToPrimitiveNoWrapper
    public void testWrapperToPrimitiveNoWrapper() {
        assertNull("Wrong result for non wrapper class", ClassUtils.wrapperToPrimitive(String.class));
    }

// org.apache.commons.lang3.ClassUtilsTest::testWrapperToPrimitiveNull
    public void testWrapperToPrimitiveNull() {
        assertNull("Wrong result for null class", ClassUtils.wrapperToPrimitive(null));
    }

// org.apache.commons.lang3.ClassUtilsTest::testWrappersToPrimitives
    public void testWrappersToPrimitives() {
        
        final Class<?>[] classes = {
                Boolean.class, Byte.class, Character.class, Short.class,
                Integer.class, Long.class, Float.class, Double.class,
                String.class, ClassUtils.class, null
        };

        Class<?>[] primitives = ClassUtils.wrappersToPrimitives(classes);
        
        assertEquals("Wrong length of result array", classes.length, primitives.length);
        for (int i = 0; i < classes.length; i++) {
            Class<?> expectedPrimitive = ClassUtils.wrapperToPrimitive(classes[i]);
            assertEquals(classes[i] + " -> " + expectedPrimitive, expectedPrimitive,
                    primitives[i]);
        }
    }

// org.apache.commons.lang3.ClassUtilsTest::testWrappersToPrimitivesNull
    public void testWrappersToPrimitivesNull() {
        assertNull("Wrong result for null input", ClassUtils.wrappersToPrimitives(null));
    }

// org.apache.commons.lang3.ClassUtilsTest::testWrappersToPrimitivesEmpty
    public void testWrappersToPrimitivesEmpty() {
        Class<?>[] empty = new Class[0];
        assertEquals("Wrong result for empty input", empty, ClassUtils.wrappersToPrimitives(empty));
    }

// org.apache.commons.lang3.ClassUtilsTest::testGetClassClassNotFound
    public void testGetClassClassNotFound() throws Exception {
        assertGetClassThrowsClassNotFound( "bool" );
        assertGetClassThrowsClassNotFound( "bool[]" );
        assertGetClassThrowsClassNotFound( "integer[]" );
    }

// org.apache.commons.lang3.ClassUtilsTest::testGetClassInvalidArguments
    public void testGetClassInvalidArguments() throws Exception {
        assertGetClassThrowsNullPointerException( null );
        assertGetClassThrowsClassNotFound( "[][][]" );
        assertGetClassThrowsClassNotFound( "[[]" );
        assertGetClassThrowsClassNotFound( "[" );
        assertGetClassThrowsClassNotFound( "java.lang.String][" );
        assertGetClassThrowsClassNotFound( ".hello.world" );
        assertGetClassThrowsClassNotFound( "hello..world" );
    }

// org.apache.commons.lang3.ClassUtilsTest::testWithInterleavingWhitespace
    public void testWithInterleavingWhitespace() throws ClassNotFoundException {
        assertEquals( int[].class, ClassUtils.getClass( " int [ ] " ) );
        assertEquals( long[].class, ClassUtils.getClass( "\rlong\t[\n]\r" ) );
        assertEquals( short[].class, ClassUtils.getClass( "\tshort                \t\t[]" ) );
        assertEquals( byte[].class, ClassUtils.getClass( "byte[\t\t\n\r]   " ) );
    }

// org.apache.commons.lang3.ClassUtilsTest::testGetClassByNormalNameArrays
    public void testGetClassByNormalNameArrays() throws ClassNotFoundException {
        assertEquals( int[].class, ClassUtils.getClass( "int[]" ) );
        assertEquals( long[].class, ClassUtils.getClass( "long[]" ) );
        assertEquals( short[].class, ClassUtils.getClass( "short[]" ) );
        assertEquals( byte[].class, ClassUtils.getClass( "byte[]" ) );
        assertEquals( char[].class, ClassUtils.getClass( "char[]" ) );
        assertEquals( float[].class, ClassUtils.getClass( "float[]" ) );
        assertEquals( double[].class, ClassUtils.getClass( "double[]" ) );
        assertEquals( boolean[].class, ClassUtils.getClass( "boolean[]" ) );
        assertEquals( String[].class, ClassUtils.getClass( "java.lang.String[]" ) );
    }

// org.apache.commons.lang3.ClassUtilsTest::testGetClassByNormalNameArrays2D
    public void testGetClassByNormalNameArrays2D() throws ClassNotFoundException {
        assertEquals( int[][].class, ClassUtils.getClass( "int[][]" ) );
        assertEquals( long[][].class, ClassUtils.getClass( "long[][]" ) );
        assertEquals( short[][].class, ClassUtils.getClass( "short[][]" ) );
        assertEquals( byte[][].class, ClassUtils.getClass( "byte[][]" ) );
        assertEquals( char[][].class, ClassUtils.getClass( "char[][]" ) );
        assertEquals( float[][].class, ClassUtils.getClass( "float[][]" ) );
        assertEquals( double[][].class, ClassUtils.getClass( "double[][]" ) );
        assertEquals( boolean[][].class, ClassUtils.getClass( "boolean[][]" ) );
        assertEquals( String[][].class, ClassUtils.getClass( "java.lang.String[][]" ) );
    }

// org.apache.commons.lang3.ClassUtilsTest::testGetClassWithArrayClasses2D
    public void testGetClassWithArrayClasses2D() throws Exception {
        assertGetClassReturnsClass( String[][].class );
        assertGetClassReturnsClass( int[][].class );
        assertGetClassReturnsClass( long[][].class );
        assertGetClassReturnsClass( short[][].class );
        assertGetClassReturnsClass( byte[][].class );
        assertGetClassReturnsClass( char[][].class );
        assertGetClassReturnsClass( float[][].class );
        assertGetClassReturnsClass( double[][].class );
        assertGetClassReturnsClass( boolean[][].class );
    }

// org.apache.commons.lang3.ClassUtilsTest::testGetClassWithArrayClasses
    public void testGetClassWithArrayClasses() throws Exception {
        assertGetClassReturnsClass( String[].class );
        assertGetClassReturnsClass( int[].class );
        assertGetClassReturnsClass( long[].class );
        assertGetClassReturnsClass( short[].class );
        assertGetClassReturnsClass( byte[].class );
        assertGetClassReturnsClass( char[].class );
        assertGetClassReturnsClass( float[].class );
        assertGetClassReturnsClass( double[].class );
        assertGetClassReturnsClass( boolean[].class );
    }

// org.apache.commons.lang3.ClassUtilsTest::testGetClassRawPrimitives
    public void testGetClassRawPrimitives() throws ClassNotFoundException {
        assertEquals( int.class, ClassUtils.getClass( "int" ) );
        assertEquals( long.class, ClassUtils.getClass( "long" ) );
        assertEquals( short.class, ClassUtils.getClass( "short" ) );
        assertEquals( byte.class, ClassUtils.getClass( "byte" ) );
        assertEquals( char.class, ClassUtils.getClass( "char" ) );
        assertEquals( float.class, ClassUtils.getClass( "float" ) );
        assertEquals( double.class, ClassUtils.getClass( "double" ) );
        assertEquals( boolean.class, ClassUtils.getClass( "boolean" ) );
    }

// org.apache.commons.lang3.ClassUtilsTest::testShowJavaBug
    public void testShowJavaBug() throws Exception {
        
        Set<?> set = Collections.unmodifiableSet(new HashSet<Object>());
        Method isEmptyMethod = set.getClass().getMethod("isEmpty",  new Class[0]);
        try {
            isEmptyMethod.invoke(set, new Object[0]);
            fail("Failed to throw IllegalAccessException as expected");
        } catch(IllegalAccessException iae) {
            
        }
    }

// org.apache.commons.lang3.ClassUtilsTest::testGetPublicMethod
    public void testGetPublicMethod() throws Exception {
        
        Set<?> set = Collections.unmodifiableSet(new HashSet<Object>());
        Method isEmptyMethod = ClassUtils.getPublicMethod(set.getClass(), "isEmpty",  new Class[0]);
            assertTrue(Modifier.isPublic(isEmptyMethod.getDeclaringClass().getModifiers()));
 
        try {
            isEmptyMethod.invoke(set, new Object[0]);
        } catch(java.lang.IllegalAccessException iae) {
            fail("Should not have thrown IllegalAccessException");
        }
               
        
        Method toStringMethod = ClassUtils.getPublicMethod(Object.class, "toString",  new Class[0]);
            assertEquals(Object.class.getMethod("toString", new Class[0]), toStringMethod);
    }

// org.apache.commons.lang3.ClassUtilsTest::testToClass_object
    public void testToClass_object() {
        assertEquals(null, ClassUtils.toClass(null));

        assertSame(
            ArrayUtils.EMPTY_CLASS_ARRAY,
            ClassUtils.toClass(new Class[0]));

        Object[] array = new Object[3];
        array[0] = new String("Test");
        array[1] = new Integer(1);
        array[2] = new Double(99);

        Class<?>[] results = ClassUtils.toClass(array);
        assertEquals("String", ClassUtils.getShortClassName(results[0]));
        assertEquals("Integer", ClassUtils.getShortClassName(results[1]));
        assertEquals("Double", ClassUtils.getShortClassName(results[2]));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getShortCanonicalName_Object
    public void test_getShortCanonicalName_Object() {
        assertEquals("<null>", ClassUtils.getShortCanonicalName(null, "<null>"));
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(new ClassUtils(), "<null>"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName(new ClassUtils[0], "<null>"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName(new ClassUtils[0][0], "<null>"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(new int[0], "<null>"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(new int[0][0], "<null>"));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getShortCanonicalName_Class
    public void test_getShortCanonicalName_Class() {
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(ClassUtils.class));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName(ClassUtils[].class));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName(ClassUtils[][].class));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(int[].class));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(int[][].class));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getShortCanonicalName_String
    public void test_getShortCanonicalName_String() {
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtils"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName("[Lorg.apache.commons.lang3.ClassUtils;"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName("[[Lorg.apache.commons.lang3.ClassUtils;"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtils[]"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtils[][]"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("[[I"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("int[]"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("int[][]"));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getPackageCanonicalName_Object
    public void test_getPackageCanonicalName_Object() {
        assertEquals("<null>", ClassUtils.getPackageCanonicalName(null, "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new ClassUtils(), "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new ClassUtils[0], "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new ClassUtils[0][0], "<null>"));
        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0], "<null>"));
        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0][0], "<null>"));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getPackageCanonicalName_Class
    public void test_getPackageCanonicalName_Class() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils.class));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils[].class));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils[][].class));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[].class));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[][].class));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getPackageCanonicalName_String
    public void test_getPackageCanonicalName_String() {
        assertEquals("org.apache.commons.lang3", 
            ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtils"));
        assertEquals("org.apache.commons.lang3", 
            ClassUtils.getPackageCanonicalName("[Lorg.apache.commons.lang3.ClassUtils;"));
        assertEquals("org.apache.commons.lang3", 
            ClassUtils.getPackageCanonicalName("[[Lorg.apache.commons.lang3.ClassUtils;"));
        assertEquals("org.apache.commons.lang3", 
            ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtils[]"));
        assertEquals("org.apache.commons.lang3", 
            ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtils[][]"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[[I"));
        assertEquals("", ClassUtils.getPackageCanonicalName("int[]"));
        assertEquals("", ClassUtils.getPackageCanonicalName("int[][]"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new StringEscapeUtils());
        Constructor<?>[] cons = StringEscapeUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(StringEscapeUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(StringEscapeUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeJava
    public void testEscapeJava() throws IOException {
        assertEquals(null, StringEscapeUtils.escapeJava(null));
        try {
            StringEscapeUtils.ESCAPE_JAVA.translate(null, null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.ESCAPE_JAVA.translate("", null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        assertEscapeJava("empty string", "", "");
        assertEscapeJava(FOO, FOO);
        assertEscapeJava("tab", "\\t", "\t");
        assertEscapeJava("backslash", "\\\\", "\\");
        assertEscapeJava("single quote should not be escaped", "'", "'");
        assertEscapeJava("\\\\\\b\\t\\r", "\\\b\t\r");
        assertEscapeJava("\\u1234", "\u1234");
        assertEscapeJava("\\u0234", "\u0234");
        assertEscapeJava("\\u00EF", "\u00ef");
        assertEscapeJava("\\u0001", "\u0001");
        assertEscapeJava("Should use capitalized unicode hex", "\\uABCD", "\uabcd");

        assertEscapeJava("He didn't say, \\\"stop!\\\"",
                "He didn't say, \"stop!\"");
        assertEscapeJava("non-breaking space", "This space is non-breaking:" + "\\u00A0",
                "This space is non-breaking:\u00a0");
        assertEscapeJava("\\uABCD\\u1234\\u012C",
                "\uABCD\u1234\u012C");
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeJavaWithSlash
    public void testEscapeJavaWithSlash() {
        final String input = "String with a slash (/) in it";

        final String expected = input;
        final String actual = StringEscapeUtils.escapeJava(input);

        
        assertEquals(expected, actual);
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeJava
    public void testUnescapeJava() throws IOException {
        assertEquals(null, StringEscapeUtils.unescapeJava(null));
        try {
            StringEscapeUtils.UNESCAPE_JAVA.translate(null, null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.UNESCAPE_JAVA.translate("", null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.unescapeJava("\\u02-3");
            fail();
        } catch (RuntimeException ex) {
        }
        
        assertUnescapeJava("", "");
        assertUnescapeJava("test", "test");
        assertUnescapeJava("\ntest\b", "\\ntest\\b");
        assertUnescapeJava("\u123425foo\ntest\b", "\\u123425foo\\ntest\\b");
        assertUnescapeJava("'\foo\teste\r", "\\'\\foo\\teste\\r");
        assertUnescapeJava("", "\\");
        
        assertUnescapeJava("lowercase unicode", "\uABCDx", "\\uabcdx");
        assertUnescapeJava("uppercase unicode", "\uABCDx", "\\uABCDx");
        assertUnescapeJava("unicode as final character", "\uABCD", "\\uabcd");
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeEcmaScript
    public void testEscapeEcmaScript() {
        assertEquals(null, StringEscapeUtils.escapeEcmaScript(null));
        try {
            StringEscapeUtils.ESCAPE_ECMASCRIPT.translate(null, null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            StringEscapeUtils.ESCAPE_ECMASCRIPT.translate("", null);
            fail();
        } catch (IOException ex) {
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        assertEquals("He didn\\'t say, \\\"stop!\\\"", StringEscapeUtils.escapeEcmaScript("He didn't say, \"stop!\""));
        assertEquals("document.getElementById(\\\"test\\\").value = \\'<script>alert(\\'aaa\\');<\\/script>\\';", 
                StringEscapeUtils.escapeEcmaScript("document.getElementById(\"test\").value = '<script>alert('aaa');</script>';"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHtml
    public void testEscapeHtml() {
        for (int i = 0; i < htmlEscapes.length; ++i) {
            String message = htmlEscapes[i][0];
            String expected = htmlEscapes[i][1];
            String original = htmlEscapes[i][2];
            assertEquals(message, expected, StringEscapeUtils.escapeHtml4(original));
            StringWriter sw = new StringWriter();
            try {
                StringEscapeUtils.ESCAPE_HTML4.translate(original, sw);
            } catch (IOException e) {
            }
            String actual = original == null ? null : sw.toString();
            assertEquals(message, expected, actual);
        }
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeHtml4
    public void testUnescapeHtml4() {
        for (int i = 0; i < htmlEscapes.length; ++i) {
            String message = htmlEscapes[i][0];
            String expected = htmlEscapes[i][2];
            String original = htmlEscapes[i][1];
            assertEquals(message, expected, StringEscapeUtils.unescapeHtml4(original));
            
            StringWriter sw = new StringWriter();
            try {
                StringEscapeUtils.UNESCAPE_HTML4.translate(original, sw);
            } catch (IOException e) {
            }
            String actual = original == null ? null : sw.toString();
            assertEquals(message, expected, actual);
        }
        
        
        
        assertEquals("funny chars pass through OK", "Fran\u00E7ais", StringEscapeUtils.unescapeHtml4("Fran\u00E7ais"));
        
        assertEquals("Hello&;World", StringEscapeUtils.unescapeHtml4("Hello&;World"));
        assertEquals("Hello&#;World", StringEscapeUtils.unescapeHtml4("Hello&#;World"));
        assertEquals("Hello&# ;World", StringEscapeUtils.unescapeHtml4("Hello&# ;World"));
        assertEquals("Hello&##;World", StringEscapeUtils.unescapeHtml4("Hello&##;World"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeHexCharsHtml
    public void testUnescapeHexCharsHtml() {
        
        assertEquals("hex number unescape", "\u0080\u009F", StringEscapeUtils.unescapeHtml4("&#x80;&#x9F;"));
        assertEquals("hex number unescape", "\u0080\u009F", StringEscapeUtils.unescapeHtml4("&#X80;&#X9F;"));
        
        for (char i = Character.MIN_VALUE; i < Character.MAX_VALUE; i++) {
            Character c1 = new Character(i);
            Character c2 = new Character((char)(i+1));
            String expected = c1.toString() + c2.toString();
            String escapedC1 = "&#x" + Integer.toHexString((c1.charValue())) + ";";
            String escapedC2 = "&#x" + Integer.toHexString((c2.charValue())) + ";";
            assertEquals("hex number unescape index " + (int)i, expected, StringEscapeUtils.unescapeHtml4(escapedC1 + escapedC2));
        }
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeUnknownEntity
    public void testUnescapeUnknownEntity() throws Exception
    {
        assertEquals("&zzzz;", StringEscapeUtils.unescapeHtml4("&zzzz;"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHtmlVersions
    public void testEscapeHtmlVersions() throws Exception
    {
        assertEquals("&Beta;", StringEscapeUtils.escapeHtml4("\u0392"));
        assertEquals("\u0392", StringEscapeUtils.unescapeHtml4("&Beta;"));

        

    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeXml
    public void testEscapeXml() throws Exception {
        assertEquals("&lt;abc&gt;", StringEscapeUtils.escapeXml("<abc>"));
        assertEquals("<abc>", StringEscapeUtils.unescapeXml("&lt;abc&gt;"));

        assertEquals("XML should not escape >0x7f values",
                "\u00A1", StringEscapeUtils.escapeXml("\u00A1"));
        assertEquals("XML should be able to unescape >0x7f values",
                "\u00A0", StringEscapeUtils.unescapeXml("&#160;"));

        assertEquals("ain't", StringEscapeUtils.unescapeXml("ain&apos;t"));
        assertEquals("ain&apos;t", StringEscapeUtils.escapeXml("ain't"));
        assertEquals("", StringEscapeUtils.escapeXml(""));
        assertEquals(null, StringEscapeUtils.escapeXml(null));
        assertEquals(null, StringEscapeUtils.unescapeXml(null));

        StringWriter sw = new StringWriter();
        try {
            StringEscapeUtils.ESCAPE_XML.translate("<abc>", sw);
        } catch (IOException e) {
        }
        assertEquals("XML was escaped incorrectly", "&lt;abc&gt;", sw.toString() );

        sw = new StringWriter();
        try {
            StringEscapeUtils.UNESCAPE_XML.translate("&lt;abc&gt;", sw);
        } catch (IOException e) {
        }
        assertEquals("XML was unescaped incorrectly", "<abc>", sw.toString() );
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testStandaloneAmphersand
    public void testStandaloneAmphersand() {
        assertEquals("<P&O>", StringEscapeUtils.unescapeHtml4("&lt;P&O&gt;"));
        assertEquals("test & <", StringEscapeUtils.unescapeHtml4("test & &lt;"));
        assertEquals("<P&O>", StringEscapeUtils.unescapeXml("&lt;P&O&gt;"));
        assertEquals("test & <", StringEscapeUtils.unescapeXml("test & &lt;"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testLang313
    public void testLang313() {
        assertEquals("& &", StringEscapeUtils.unescapeHtml4("& &amp;"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeCsvString
    public void testEscapeCsvString() throws Exception
    {
        assertEquals("foo.bar",          StringEscapeUtils.escapeCsv("foo.bar"));
        assertEquals("\"foo,bar\"",      StringEscapeUtils.escapeCsv("foo,bar"));
        assertEquals("\"foo\nbar\"",     StringEscapeUtils.escapeCsv("foo\nbar"));
        assertEquals("\"foo\rbar\"",     StringEscapeUtils.escapeCsv("foo\rbar"));
        assertEquals("\"foo\"\"bar\"",   StringEscapeUtils.escapeCsv("foo\"bar"));
        assertEquals("",   StringEscapeUtils.escapeCsv(""));
        assertEquals(null, StringEscapeUtils.escapeCsv(null));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeCsvWriter
    public void testEscapeCsvWriter() throws Exception
    {
        checkCsvEscapeWriter("foo.bar",        "foo.bar");
        checkCsvEscapeWriter("\"foo,bar\"",    "foo,bar");
        checkCsvEscapeWriter("\"foo\nbar\"",   "foo\nbar");
        checkCsvEscapeWriter("\"foo\rbar\"",   "foo\rbar");
        checkCsvEscapeWriter("\"foo\"\"bar\"", "foo\"bar");
        checkCsvEscapeWriter("", null);
        checkCsvEscapeWriter("", "");
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeCsvString
    public void testUnescapeCsvString() throws Exception
    {
        assertEquals("foo.bar",          StringEscapeUtils.unescapeCsv("foo.bar"));
        assertEquals("foo,bar",      StringEscapeUtils.unescapeCsv("\"foo,bar\""));
        assertEquals("foo\nbar",     StringEscapeUtils.unescapeCsv("\"foo\nbar\""));
        assertEquals("foo\rbar",     StringEscapeUtils.unescapeCsv("\"foo\rbar\""));
        assertEquals("foo\"bar",   StringEscapeUtils.unescapeCsv("\"foo\"\"bar\""));
        assertEquals("",   StringEscapeUtils.unescapeCsv(""));
        assertEquals(null, StringEscapeUtils.unescapeCsv(null));

        assertEquals("\"foo.bar\"",          StringEscapeUtils.unescapeCsv("\"foo.bar\""));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeCsvWriter
    public void testUnescapeCsvWriter() throws Exception
    {
        checkCsvUnescapeWriter("foo.bar",        "foo.bar");
        checkCsvUnescapeWriter("foo,bar",    "\"foo,bar\"");
        checkCsvUnescapeWriter("foo\nbar",   "\"foo\nbar\"");
        checkCsvUnescapeWriter("foo\rbar",   "\"foo\rbar\"");
        checkCsvUnescapeWriter("foo\"bar", "\"foo\"\"bar\"");
        checkCsvUnescapeWriter("", null);
        checkCsvUnescapeWriter("", "");

        checkCsvUnescapeWriter("\"foo.bar\"",        "\"foo.bar\"");
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHtmlHighUnicode
    public void testEscapeHtmlHighUnicode() throws java.io.UnsupportedEncodingException {
        
        
        
        
        byte[] data = new byte[] { (byte)0xF0, (byte)0x9D, (byte)0x8D, (byte)0xA2 };

        String original = new String(data, "UTF8");

        String escaped = StringEscapeUtils.escapeHtml4( original );
        assertEquals( "High unicode should not have been escaped", original, escaped);

        String unescaped = StringEscapeUtils.unescapeHtml4( escaped );
        assertEquals( "High unicode should have been unchanged", original, unescaped);

    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHiragana
    public void testEscapeHiragana() {
        
        String original = "\u304B\u304C\u3068";
        String escaped = StringEscapeUtils.escapeHtml4(original);
        assertEquals( "Hiragana character unicode behaviour should not be being escaped by escapeHtml4",
        original, escaped);

        String unescaped = StringEscapeUtils.unescapeHtml4( escaped );

        assertEquals( "Hiragana character unicode behaviour has changed - expected no unescaping", escaped, unescaped);
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testEquals
    public void testEquals() {
        assertEquals(true, StringUtils.equals(null, null));
        assertEquals(true, StringUtils.equals(FOO, FOO));
        assertEquals(true, StringUtils.equals(FOO, new String(new char[] { 'f', 'o', 'o' })));
        assertEquals(false, StringUtils.equals(FOO, new String(new char[] { 'f', 'O', 'O' })));
        assertEquals(false, StringUtils.equals(FOO, BAR));
        assertEquals(false, StringUtils.equals(FOO, null));
        assertEquals(false, StringUtils.equals(null, FOO));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testEqualsIgnoreCase
    public void testEqualsIgnoreCase() {
        assertEquals(true, StringUtils.equalsIgnoreCase(null, null));
        assertEquals(true, StringUtils.equalsIgnoreCase(FOO, FOO));
        assertEquals(true, StringUtils.equalsIgnoreCase(FOO, new String(new char[] { 'f', 'o', 'o' })));
        assertEquals(true, StringUtils.equalsIgnoreCase(FOO, new String(new char[] { 'f', 'O', 'O' })));
        assertEquals(false, StringUtils.equalsIgnoreCase(FOO, BAR));
        assertEquals(false, StringUtils.equalsIgnoreCase(FOO, null));
        assertEquals(false, StringUtils.equalsIgnoreCase(null, FOO));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOf_char
    public void testIndexOf_char() {
        assertEquals(-1, StringUtils.indexOf(null, ' '));
        assertEquals(-1, StringUtils.indexOf("", ' '));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOf_charInt
    public void testIndexOf_charInt() {
        assertEquals(-1, StringUtils.indexOf(null, ' ', 0));
        assertEquals(-1, StringUtils.indexOf(null, ' ', -1));
        assertEquals(-1, StringUtils.indexOf("", ' ', 0));
        assertEquals(-1, StringUtils.indexOf("", ' ', -1));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a', 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOf_String
    public void testIndexOf_String() {
        assertEquals(-1, StringUtils.indexOf(null, null));
        assertEquals(-1, StringUtils.indexOf("", null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfIgnoreCase_String
    public void testIndexOfIgnoreCase_String() {
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, null));
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, ""));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("", null));
        assertEquals(0, StringUtils.indexOfIgnoreCase("", ""));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "a"));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "b"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "ab"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB"));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", ""));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfIgnoreCase_StringInt
    public void testIndexOfIgnoreCase_StringInt() {
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", -1));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 0));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 1));
        assertEquals(4, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 2));
        assertEquals(4, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 3));
        assertEquals(4, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 4));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 5));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 6));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 7));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 8));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aab", "AB", 1));
        assertEquals(5, StringUtils.indexOfIgnoreCase("aabaabaa", "", 5));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("ab", "AAB", 0));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aab", "AAB", 1));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testOrdinalIndexOf
    public void testOrdinalIndexOf() {
        assertEquals(-1, StringUtils.ordinalIndexOf(null, null, Integer.MIN_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("", null, Integer.MIN_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("", "", Integer.MIN_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", Integer.MIN_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "b", Integer.MIN_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "ab", Integer.MIN_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "", Integer.MIN_VALUE));
        
        assertEquals(-1, StringUtils.ordinalIndexOf(null, null, -1));
        assertEquals(-1, StringUtils.ordinalIndexOf("", null, -1));
        assertEquals(-1, StringUtils.ordinalIndexOf("", "", -1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", -1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "b", -1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "ab", -1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "", -1));

        assertEquals(-1, StringUtils.ordinalIndexOf(null, null, 0));
        assertEquals(-1, StringUtils.ordinalIndexOf("", null, 0));
        assertEquals(-1, StringUtils.ordinalIndexOf("", "", 0));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", 0));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "b", 0));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "ab", 0));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "", 0));

        assertEquals(-1, StringUtils.ordinalIndexOf(null, null, 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("", null, 1));
        assertEquals(0, StringUtils.ordinalIndexOf("", "", 1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        assertEquals(2, StringUtils.ordinalIndexOf("aabaabaa", "b", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "ab", 1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 1));

        assertEquals(-1, StringUtils.ordinalIndexOf(null, null, 2));
        assertEquals(-1, StringUtils.ordinalIndexOf("", null, 2));
        assertEquals(0, StringUtils.ordinalIndexOf("", "", 2));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(5, StringUtils.ordinalIndexOf("aabaabaa", "b", 2));
        assertEquals(4, StringUtils.ordinalIndexOf("aabaabaa", "ab", 2));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 2));
        
        assertEquals(-1, StringUtils.ordinalIndexOf(null, null, Integer.MAX_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("", null, Integer.MAX_VALUE));
        assertEquals(0, StringUtils.ordinalIndexOf("", "", Integer.MAX_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", Integer.MAX_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "b", Integer.MAX_VALUE));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "ab", Integer.MAX_VALUE));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", Integer.MAX_VALUE));
        
        assertEquals(-1, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 0));
        assertEquals(0, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 2));
        assertEquals(2, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 3));
        assertEquals(3, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 4));
        assertEquals(4, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 5));
        assertEquals(5, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 6));
        assertEquals(6, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 7));
        assertEquals(7, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 8));
        assertEquals(8, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 9));
        assertEquals(-1, StringUtils.ordinalIndexOf("aaaaaaaaa", "a", 10));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOf_StringInt
    public void testIndexOf_StringInt() {
        assertEquals(-1, StringUtils.indexOf(null, null, 0));
        assertEquals(-1, StringUtils.indexOf(null, null, -1));
        assertEquals(-1, StringUtils.indexOf(null, "", 0));
        assertEquals(-1, StringUtils.indexOf(null, "", -1));
        assertEquals(-1, StringUtils.indexOf("", null, 0));
        assertEquals(-1, StringUtils.indexOf("", null, -1));
        assertEquals(0, StringUtils.indexOf("", "", 0));
        assertEquals(0, StringUtils.indexOf("", "", -1));
        assertEquals(0, StringUtils.indexOf("", "", 9));
        assertEquals(0, StringUtils.indexOf("abc", "", 0));
        assertEquals(0, StringUtils.indexOf("abc", "", -1));
        assertEquals(3, StringUtils.indexOf("abc", "", 9));
        assertEquals(3, StringUtils.indexOf("abc", "", 3));
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a", 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 0));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab", 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", "b", 9));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        assertEquals(2,StringUtils.indexOf("aabaabaa", "", 2)); 
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOf_char
    public void testLastIndexOf_char() {
        assertEquals(-1, StringUtils.lastIndexOf(null, ' '));
        assertEquals(-1, StringUtils.lastIndexOf("", ' '));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOf_charInt
    public void testLastIndexOf_charInt() {
        assertEquals(-1, StringUtils.lastIndexOf(null, ' ', 0));
        assertEquals(-1, StringUtils.lastIndexOf(null, ' ', -1));
        assertEquals(-1, StringUtils.lastIndexOf("", ' ', 0));
        assertEquals(-1, StringUtils.lastIndexOf("", ' ', -1));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a', 8));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 8));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 3));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 9));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', -1));
        assertEquals(0, StringUtils.lastIndexOf("aabaabaa", 'a', 0));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOf_String
    public void testLastIndexOf_String() {
        assertEquals(-1, StringUtils.lastIndexOf(null, null));
        assertEquals(-1, StringUtils.lastIndexOf("", null));
        assertEquals(-1, StringUtils.lastIndexOf("", "a"));
        assertEquals(0, StringUtils.lastIndexOf("", ""));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a"));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastOrdinalIndexOf
    public void testLastOrdinalIndexOf() {
        assertEquals(-1, StringUtils.lastOrdinalIndexOf(null, "*", 42) );
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("*", null, 42) );
        assertEquals(0, StringUtils.lastOrdinalIndexOf("", "", 42) );
        assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1) );
        assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2) );
        assertEquals(5, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 1) );
        assertEquals(2, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 2) );
        assertEquals(4, StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 1) );
        assertEquals(1, StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 2) );
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 1) );
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 2) );
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOf_StringInt
    public void testLastIndexOf_StringInt() {
        assertEquals(-1, StringUtils.lastIndexOf(null, null, 0));
        assertEquals(-1, StringUtils.lastIndexOf(null, null, -1));
        assertEquals(-1, StringUtils.lastIndexOf(null, "", 0));
        assertEquals(-1, StringUtils.lastIndexOf(null, "", -1));
        assertEquals(-1, StringUtils.lastIndexOf("", null, 0));
        assertEquals(-1, StringUtils.lastIndexOf("", null, -1));
        assertEquals(0, StringUtils.lastIndexOf("", "", 0));
        assertEquals(-1, StringUtils.lastIndexOf("", "", -1));
        assertEquals(0, StringUtils.lastIndexOf("", "", 9));
        assertEquals(0, StringUtils.lastIndexOf("abc", "", 0));
        assertEquals(-1, StringUtils.lastIndexOf("abc", "", -1));
        assertEquals(3, StringUtils.lastIndexOf("abc", "", 9));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a", 8));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 8));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab", 8));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", "b", 3));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 9));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", -1));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", 0));
        assertEquals(0, StringUtils.lastIndexOf("aabaabaa", "a", 0));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOfIgnoreCase_String
    public void testLastIndexOfIgnoreCase_String() {
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, null));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", null));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, ""));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", "a"));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("", ""));
        assertEquals(8, StringUtils.lastIndexOfIgnoreCase("aabaabaa", ""));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "a"));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "b"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "ab"));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB"));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("ab", "AAB"));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("aab", "AAB"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOfIgnoreCase_StringInt
    public void testLastIndexOfIgnoreCase_StringInt() {
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, null, 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, null, -1));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "", -1));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", null, 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", null, -1));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("", "", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("", "", -1));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("", "", 9));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("abc", "", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("abc", "", -1));
        assertEquals(3, StringUtils.lastIndexOfIgnoreCase("abc", "", 9));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 8));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB", 8));
        assertEquals(2, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 3));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 9));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 0));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 0));
        assertEquals(1, StringUtils.lastIndexOfIgnoreCase("aab", "AB", 1));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsChar
    public void testContainsChar() {
        assertEquals(false, StringUtils.contains(null, ' '));
        assertEquals(false, StringUtils.contains("", ' '));
        assertEquals(false, StringUtils.contains("",null));
        assertEquals(false, StringUtils.contains(null,null));
        assertEquals(true, StringUtils.contains("abc", 'a'));
        assertEquals(true, StringUtils.contains("abc", 'b'));
        assertEquals(true, StringUtils.contains("abc", 'c'));
        assertEquals(false, StringUtils.contains("abc", 'z'));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsString
    public void testContainsString() {
        assertEquals(false, StringUtils.contains(null, null));
        assertEquals(false, StringUtils.contains(null, ""));
        assertEquals(false, StringUtils.contains(null, "a"));
        assertEquals(false, StringUtils.contains("", null));
        assertEquals(true, StringUtils.contains("", ""));
        assertEquals(false, StringUtils.contains("", "a"));
        assertEquals(true, StringUtils.contains("abc", "a"));
        assertEquals(true, StringUtils.contains("abc", "b"));
        assertEquals(true, StringUtils.contains("abc", "c"));
        assertEquals(true, StringUtils.contains("abc", "abc"));
        assertEquals(false, StringUtils.contains("abc", "z"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsIgnoreCase_StringString
    public void testContainsIgnoreCase_StringString() {
        assertFalse(StringUtils.containsIgnoreCase(null, null));
        
        
        assertFalse(StringUtils.containsIgnoreCase(null, ""));
        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase(null, "abc"));
        
        assertFalse(StringUtils.containsIgnoreCase("", null));
        assertFalse(StringUtils.containsIgnoreCase("a", null));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
        
        
        assertTrue(StringUtils.containsIgnoreCase("", ""));
        assertTrue(StringUtils.containsIgnoreCase("a", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", ""));

        
        assertFalse(StringUtils.containsIgnoreCase("", "a"));
        assertTrue(StringUtils.containsIgnoreCase("a", "a"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "a"));
        assertFalse(StringUtils.containsIgnoreCase("", "A"));
        assertTrue(StringUtils.containsIgnoreCase("a", "A"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        
        
        assertFalse(StringUtils.containsIgnoreCase("", "abc"));
        assertFalse(StringUtils.containsIgnoreCase("a", "abc"));
        assertTrue(StringUtils.containsIgnoreCase("xabcz", "abc"));
        assertFalse(StringUtils.containsIgnoreCase("", "ABC"));
        assertFalse(StringUtils.containsIgnoreCase("a", "ABC"));
        assertTrue(StringUtils.containsIgnoreCase("xabcz", "ABC"));
    }
