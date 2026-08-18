// buggy code
    public static boolean toBoolean(String str) {
        // Previously used equalsIgnoreCase, which was fast for interned 'true'.
        // Non interned 'true' matched 15 times slower.
        // 
        // Optimisation provides same performance as before for interned 'true'.
        // Similar performance for null, 'false', and other strings not length 2/3/4.
        // 'true'/'TRUE' match 4 times slower, 'tRUE'/'True' 7 times slower.
        if (str == "true") {
            return true;
        }
        if (str == null) {
            return false;
        }
        switch (str.length()) {
            case 2: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                return 
                    (ch0 == 'o' || ch0 == 'O') &&
                    (ch1 == 'n' || ch1 == 'N');
            }
            case 3: {
                char ch = str.charAt(0);
                if (ch == 'y') {
                    return 
                        (str.charAt(1) == 'e' || str.charAt(1) == 'E') &&
                        (str.charAt(2) == 's' || str.charAt(2) == 'S');
                }
                if (ch == 'Y') {
                    return 
                        (str.charAt(1) == 'E' || str.charAt(1) == 'e') &&
                        (str.charAt(2) == 'S' || str.charAt(2) == 's');
                }
            }
            case 4: {
                char ch = str.charAt(0);
                if (ch == 't') {
                    return 
                        (str.charAt(1) == 'r' || str.charAt(1) == 'R') &&
                        (str.charAt(2) == 'u' || str.charAt(2) == 'U') &&
                        (str.charAt(3) == 'e' || str.charAt(3) == 'E');
                }
                if (ch == 'T') {
                    return 
                        (str.charAt(1) == 'R' || str.charAt(1) == 'r') &&
                        (str.charAt(2) == 'U' || str.charAt(2) == 'u') &&
                        (str.charAt(3) == 'E' || str.charAt(3) == 'e');
                }
            }
        }
        return false;
    }

// relevant test
// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayBoolean
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

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayByte
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

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayChar
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

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayDouble
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

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayFloat
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

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayInt
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

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayLong
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

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayShort
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

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayObject
    public void testAddObjectArrayObject() {
        Object[] newArray;
        newArray = ArrayUtils.add((Object[])null, null);
        assertTrue(Arrays.equals((new Object[]{null}), newArray));
        assertEquals(Object.class, newArray.getClass().getComponentType());
        
        newArray = ArrayUtils.add((Object[])null, "a");
        assertTrue(Arrays.equals((new String[]{"a"}), newArray));
        assertTrue(Arrays.equals((new Object[]{"a"}), newArray));
        assertEquals(String.class, newArray.getClass().getComponentType());
        
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
        
        numberArray1 = null;
        newArray = ArrayUtils.add(numberArray1, null);
        assertTrue(Arrays.equals((new Object[]{null}), newArray));
        assertEquals(Object.class, newArray.getClass().getComponentType());
    }

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectArrayToObjectArray
    public void testAddObjectArrayToObjectArray() {
        assertNull(ArrayUtils.addAll((Object[]) null, (Object[]) null));
        Object[] newArray;
        String[] stringArray1 = new String[]{"a", "b", "c"};
        String[] stringArray2 = new String[]{"1", "2", "3"};
        newArray = ArrayUtils.addAll(stringArray1, null);
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
        newArray = ArrayUtils.addAll(ArrayUtils.EMPTY_STRING_ARRAY, null);
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

// org.apache.commons.lang.ArrayUtilsAddTest::testAddObjectAtIndex
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

// org.apache.commons.lang.BooleanUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new BooleanUtils());
        Constructor[] cons = BooleanUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(BooleanUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(BooleanUtils.class.getModifiers()));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_negate_Boolean
    public void test_negate_Boolean() {
        assertSame(null, BooleanUtils.negate(null));
        assertSame(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
        assertSame(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_isTrue_Boolean
    public void test_isTrue_Boolean() {
        assertEquals(true, BooleanUtils.isTrue(Boolean.TRUE));
        assertEquals(false, BooleanUtils.isTrue(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isTrue((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_isNotTrue_Boolean
    public void test_isNotTrue_Boolean() {
        assertEquals(false, BooleanUtils.isNotTrue(Boolean.TRUE));
        assertEquals(true, BooleanUtils.isNotTrue(Boolean.FALSE));
        assertEquals(true, BooleanUtils.isNotTrue((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_isFalse_Boolean
    public void test_isFalse_Boolean() {
        assertEquals(false, BooleanUtils.isFalse(Boolean.TRUE));
        assertEquals(true, BooleanUtils.isFalse(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isFalse((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_isNotFalse_Boolean
    public void test_isNotFalse_Boolean() {
        assertEquals(true, BooleanUtils.isNotFalse(Boolean.TRUE));
        assertEquals(false, BooleanUtils.isNotFalse(Boolean.FALSE));
        assertEquals(true, BooleanUtils.isNotFalse((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_boolean
    public void test_toBooleanObject_boolean() {
        assertSame(Boolean.TRUE, BooleanUtils.toBooleanObject(true));
        assertSame(Boolean.FALSE, BooleanUtils.toBooleanObject(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_Boolean
    public void test_toBoolean_Boolean() {
        assertEquals(true, BooleanUtils.toBoolean(Boolean.TRUE));
        assertEquals(false, BooleanUtils.toBoolean(Boolean.FALSE));
        assertEquals(false, BooleanUtils.toBoolean((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanDefaultIfNull_Boolean_boolean
    public void test_toBooleanDefaultIfNull_Boolean_boolean() {
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, true));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, false));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_int
    public void test_toBoolean_int() {
        assertEquals(true, BooleanUtils.toBoolean(1));
        assertEquals(true, BooleanUtils.toBoolean(-1));
        assertEquals(false, BooleanUtils.toBoolean(0));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_int
    public void test_toBooleanObject_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(-1));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_Integer
    public void test_toBooleanObject_Integer() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(1)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(-1)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(0)));
        assertEquals(null, BooleanUtils.toBooleanObject((Integer) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_int_int_int
    public void test_toBoolean_int_int_int() {
        assertEquals(true, BooleanUtils.toBoolean(6, 6, 7));
        assertEquals(false, BooleanUtils.toBoolean(7, 6, 7));
        try {
            BooleanUtils.toBoolean(8, 6, 7);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_Integer_Integer_Integer
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

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_int_int_int
    public void test_toBooleanObject_int_int_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(6, 6, 7, 8));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(7, 6, 7, 8));
        assertEquals(null, BooleanUtils.toBooleanObject(8, 6, 7, 8));
        try {
            BooleanUtils.toBooleanObject(9, 6, 7, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_Integer_Integer_Integer_Integer
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

// org.apache.commons.lang.BooleanUtilsTest::test_toInteger_boolean
    public void test_toInteger_boolean() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toIntegerObject_boolean
    public void test_toIntegerObject_boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(true));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toIntegerObject_Boolean
    public void test_toIntegerObject_Boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(Boolean.FALSE));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toInteger_boolean_int_int
    public void test_toInteger_boolean_int_int() {
        assertEquals(6, BooleanUtils.toInteger(true, 6, 7));
        assertEquals(7, BooleanUtils.toInteger(false, 6, 7));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toInteger_Boolean_int_int_int
    public void test_toInteger_Boolean_int_int_int() {
        assertEquals(6, BooleanUtils.toInteger(Boolean.TRUE, 6, 7, 8));
        assertEquals(7, BooleanUtils.toInteger(Boolean.FALSE, 6, 7, 8));
        assertEquals(8, BooleanUtils.toInteger(null, 6, 7, 8));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toIntegerObject_boolean_Integer_Integer
    public void test_toIntegerObject_boolean_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        assertEquals(six, BooleanUtils.toIntegerObject(true, six, seven));
        assertEquals(seven, BooleanUtils.toIntegerObject(false, six, seven));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toIntegerObject_Boolean_Integer_Integer_Integer
    public void test_toIntegerObject_Boolean_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        Integer eight = new Integer(8);
        assertEquals(six, BooleanUtils.toIntegerObject(Boolean.TRUE, six, seven, eight));
        assertEquals(seven, BooleanUtils.toIntegerObject(Boolean.FALSE, six, seven, eight));
        assertEquals(eight, BooleanUtils.toIntegerObject((Boolean) null, six, seven, eight));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null, six, seven, null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_String
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

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_String_String_String_String
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

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_String
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

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_String_String_String
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

// org.apache.commons.lang.BooleanUtilsTest::test_toStringTrueFalse_Boolean
    public void test_toStringTrueFalse_Boolean() {
        assertEquals(null, BooleanUtils.toStringTrueFalse((Boolean) null));
        assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringOnOff_Boolean
    public void test_toStringOnOff_Boolean() {
        assertEquals(null, BooleanUtils.toStringOnOff((Boolean) null));
        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringYesNo_Boolean
    public void test_toStringYesNo_Boolean() {
        assertEquals(null, BooleanUtils.toStringYesNo((Boolean) null));
        assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toString_Boolean_String_String_String
    public void test_toString_Boolean_String_String_String() {
        assertEquals("U", BooleanUtils.toString((Boolean) null, "Y", "N", "U"));
        assertEquals("Y", BooleanUtils.toString(Boolean.TRUE, "Y", "N", "U"));
        assertEquals("N", BooleanUtils.toString(Boolean.FALSE, "Y", "N", "U"));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringTrueFalse_boolean
    public void test_toStringTrueFalse_boolean() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringOnOff_boolean
    public void test_toStringOnOff_boolean() {
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringYesNo_boolean
    public void test_toStringYesNo_boolean() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toString_boolean_String_String_String
    public void test_toString_boolean_String_String_String() {
        assertEquals("Y", BooleanUtils.toString(true, "Y", "N"));
        assertEquals("N", BooleanUtils.toString(false, "Y", "N"));
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_primitive_nullInput
    public void testXor_primitive_nullInput() {
        final boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_primitive_emptyInput
    public void testXor_primitive_emptyInput() {
        try {
            BooleanUtils.xor(new boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_primitive_validInput_2items
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

// org.apache.commons.lang.BooleanUtilsTest::testXor_primitive_validInput_3items
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

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_nullInput
    public void testXor_object_nullInput() {
        final Boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_emptyInput
    public void testXor_object_emptyInput() {
        try {
            BooleanUtils.xor(new Boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_nullElementInput
    public void testXor_object_nullElementInput() {
        try {
            BooleanUtils.xor(new Boolean[] {null});
            fail("Exception was not thrown for null element input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_validInput_2items
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

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_validInput_3items
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

// org.apache.commons.lang.CharUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new CharUtils());
        Constructor[] cons = CharUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(BooleanUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(BooleanUtils.class.getModifiers()));
    }

// org.apache.commons.lang.CharUtilsTest::testToCharacterObject_char
    public void testToCharacterObject_char() {
        assertEquals(new Character('a'), CharUtils.toCharacterObject('a'));
        assertSame(CharUtils.toCharacterObject('a'), CharUtils.toCharacterObject('a'));
       
        for (int i = 0; i < 128; i++) {
            Character ch = CharUtils.toCharacterObject((char) i);
            Character ch2 = CharUtils.toCharacterObject((char) i);
            assertSame(ch, ch2);
            assertEquals(i, ch.charValue());
        }
        for (int i = 128; i < 196; i++) {
            Character ch = CharUtils.toCharacterObject((char) i);
            Character ch2 = CharUtils.toCharacterObject((char) i);
            assertEquals(ch, ch2);
            assertTrue(ch != ch2);
            assertEquals(i, ch.charValue());
            assertEquals(i, ch2.charValue());
        }
    }

// org.apache.commons.lang.CharUtilsTest::testToCharacterObject_String
    public void testToCharacterObject_String() {
        assertEquals(null, CharUtils.toCharacterObject(null));
        assertEquals(null, CharUtils.toCharacterObject(""));
        assertEquals(new Character('a'), CharUtils.toCharacterObject("a"));
        assertEquals(new Character('a'), CharUtils.toCharacterObject("abc"));
        assertSame(CharUtils.toCharacterObject("a"), CharUtils.toCharacterObject("a"));
        assertSame(CharUtils.toCharacterObject("a"), CharUtils.toCharacterObject('a'));
    }

// org.apache.commons.lang.CharUtilsTest::testToChar_Character
    public void testToChar_Character() {
        assertEquals('A', CharUtils.toChar(CHARACTER_A));
        assertEquals('B', CharUtils.toChar(CHARACTER_B));
        try {
            CharUtils.toChar((Character) null);
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.CharUtilsTest::testToChar_Character_char
    public void testToChar_Character_char() {
        assertEquals('A', CharUtils.toChar(CHARACTER_A, 'X'));
        assertEquals('B', CharUtils.toChar(CHARACTER_B, 'X'));
        assertEquals('X', CharUtils.toChar((Character) null, 'X'));
    }

// org.apache.commons.lang.CharUtilsTest::testToChar_String
    public void testToChar_String() {
        assertEquals('A', CharUtils.toChar("A"));
        assertEquals('B', CharUtils.toChar("BA"));
        try {
            CharUtils.toChar((String) null);
        } catch (IllegalArgumentException ex) {}
        try {
            CharUtils.toChar("");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.CharUtilsTest::testToChar_String_char
    public void testToChar_String_char() {
        assertEquals('A', CharUtils.toChar("A", 'X'));
        assertEquals('B', CharUtils.toChar("BA", 'X'));
        assertEquals('X', CharUtils.toChar("", 'X'));
        assertEquals('X', CharUtils.toChar((String) null, 'X'));
    }

// org.apache.commons.lang.CharUtilsTest::testToIntValue_char
    public void testToIntValue_char() {
        assertEquals(0, CharUtils.toIntValue('0'));
        assertEquals(1, CharUtils.toIntValue('1'));
        assertEquals(2, CharUtils.toIntValue('2'));
        assertEquals(3, CharUtils.toIntValue('3'));
        assertEquals(4, CharUtils.toIntValue('4'));
        assertEquals(5, CharUtils.toIntValue('5'));
        assertEquals(6, CharUtils.toIntValue('6'));
        assertEquals(7, CharUtils.toIntValue('7'));
        assertEquals(8, CharUtils.toIntValue('8'));
        assertEquals(9, CharUtils.toIntValue('9'));
        try {
            CharUtils.toIntValue('a');
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.CharUtilsTest::testToIntValue_char_int
    public void testToIntValue_char_int() {
        assertEquals(0, CharUtils.toIntValue('0', -1));
        assertEquals(3, CharUtils.toIntValue('3', -1));
        assertEquals(-1, CharUtils.toIntValue('a', -1));
    }

// org.apache.commons.lang.CharUtilsTest::testToIntValue_Character
    public void testToIntValue_Character() {
        assertEquals(0, CharUtils.toIntValue(new Character('0')));
        assertEquals(3, CharUtils.toIntValue(new Character('3')));
        try {
            CharUtils.toIntValue(null);
        } catch (IllegalArgumentException ex) {}
        try {
            CharUtils.toIntValue(CHARACTER_A);
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.CharUtilsTest::testToIntValue_Character_int
    public void testToIntValue_Character_int() {
        assertEquals(0, CharUtils.toIntValue(new Character('0'), -1));
        assertEquals(3, CharUtils.toIntValue(new Character('3'), -1));
        assertEquals(-1, CharUtils.toIntValue(new Character('A'), -1));
        assertEquals(-1, CharUtils.toIntValue(null, -1));
    }

// org.apache.commons.lang.CharUtilsTest::testToString_char
    public void testToString_char() {
        assertEquals("a", CharUtils.toString('a'));
        assertSame(CharUtils.toString('a'), CharUtils.toString('a'));
       
        for (int i = 0; i < 128; i++) {
            String str = CharUtils.toString((char) i);
            String str2 = CharUtils.toString((char) i);
            assertSame(str, str2);
            assertEquals(1, str.length());
            assertEquals(i, str.charAt(0));
        }
        for (int i = 128; i < 196; i++) {
            String str = CharUtils.toString((char) i);
            String str2 = CharUtils.toString((char) i);
            assertEquals(str, str2);
            assertTrue(str != str2);
            assertEquals(1, str.length());
            assertEquals(i, str.charAt(0));
            assertEquals(1, str2.length());
            assertEquals(i, str2.charAt(0));
        }
    }

// org.apache.commons.lang.CharUtilsTest::testToString_Character
    public void testToString_Character() {
        assertEquals(null, CharUtils.toString(null));
        assertEquals("A", CharUtils.toString(CHARACTER_A));
        assertSame(CharUtils.toString(CHARACTER_A), CharUtils.toString(CHARACTER_A));
    }

// org.apache.commons.lang.CharUtilsTest::testToUnicodeEscaped_char
    public void testToUnicodeEscaped_char() {
        assertEquals("\\u0041", CharUtils.unicodeEscaped('A'));
       
        for (int i = 0; i < 196; i++) {
            String str = CharUtils.unicodeEscaped((char) i);
            assertEquals(6, str.length());
            int val = Integer.parseInt(str.substring(2), 16);
            assertEquals(i, val);
        }
        assertEquals("\\u0999", CharUtils.unicodeEscaped((char) 0x999));
        assertEquals("\\u1001", CharUtils.unicodeEscaped((char) 0x1001));
    }

// org.apache.commons.lang.CharUtilsTest::testToUnicodeEscaped_Character
    public void testToUnicodeEscaped_Character() {
        assertEquals(null, CharUtils.unicodeEscaped(null));
        assertEquals("\\u0041", CharUtils.unicodeEscaped(CHARACTER_A));
    }

// org.apache.commons.lang.CharUtilsTest::testIsAscii_char
    public void testIsAscii_char() {
        assertEquals(true, CharUtils.isAscii('a'));
        assertEquals(true, CharUtils.isAscii('A'));
        assertEquals(true, CharUtils.isAscii('3'));
        assertEquals(true, CharUtils.isAscii('-'));
        assertEquals(true, CharUtils.isAscii('\n'));
        assertEquals(false, CharUtils.isAscii(CHAR_COPY));
       
        for (int i = 0; i < 128; i++) {
            if (i < 128) {
                assertEquals(true, CharUtils.isAscii((char) i));
            } else {
                assertEquals(false, CharUtils.isAscii((char) i));
            }
        }
    }

// org.apache.commons.lang.CharUtilsTest::testIsAsciiPrintable_char
    public void testIsAsciiPrintable_char() {
        assertEquals(true, CharUtils.isAsciiPrintable('a'));
        assertEquals(true, CharUtils.isAsciiPrintable('A'));
        assertEquals(true, CharUtils.isAsciiPrintable('3'));
        assertEquals(true, CharUtils.isAsciiPrintable('-'));
        assertEquals(false, CharUtils.isAsciiPrintable('\n'));
        assertEquals(false, CharUtils.isAscii(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i >= 32 && i <= 126) {
                assertEquals(true, CharUtils.isAsciiPrintable((char) i));
            } else {
                assertEquals(false, CharUtils.isAsciiPrintable((char) i));
            }
        }
    }

// org.apache.commons.lang.CharUtilsTest::testIsAsciiControl_char
    public void testIsAsciiControl_char() {
        assertEquals(false, CharUtils.isAsciiControl('a'));
        assertEquals(false, CharUtils.isAsciiControl('A'));
        assertEquals(false, CharUtils.isAsciiControl('3'));
        assertEquals(false, CharUtils.isAsciiControl('-'));
        assertEquals(true, CharUtils.isAsciiControl('\n'));
        assertEquals(false, CharUtils.isAsciiControl(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i < 32 || i == 127) {
                assertEquals(true, CharUtils.isAsciiControl((char) i));
            } else {
                assertEquals(false, CharUtils.isAsciiControl((char) i));
            }
        }
    }

// org.apache.commons.lang.CharUtilsTest::testIsAsciiAlpha_char
    public void testIsAsciiAlpha_char() {
        assertEquals(true, CharUtils.isAsciiAlpha('a'));
        assertEquals(true, CharUtils.isAsciiAlpha('A'));
        assertEquals(false, CharUtils.isAsciiAlpha('3'));
        assertEquals(false, CharUtils.isAsciiAlpha('-'));
        assertEquals(false, CharUtils.isAsciiAlpha('\n'));
        assertEquals(false, CharUtils.isAsciiAlpha(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if ((i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z')) {
                assertEquals(true, CharUtils.isAsciiAlpha((char) i));
            } else {
                assertEquals(false, CharUtils.isAsciiAlpha((char) i));
            }
        }
    }

// org.apache.commons.lang.CharUtilsTest::testIsAsciiAlphaUpper_char
    public void testIsAsciiAlphaUpper_char() {
        assertEquals(false, CharUtils.isAsciiAlphaUpper('a'));
        assertEquals(true, CharUtils.isAsciiAlphaUpper('A'));
        assertEquals(false, CharUtils.isAsciiAlphaUpper('3'));
        assertEquals(false, CharUtils.isAsciiAlphaUpper('-'));
        assertEquals(false, CharUtils.isAsciiAlphaUpper('\n'));
        assertEquals(false, CharUtils.isAsciiAlphaUpper(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i >= 'A' && i <= 'Z') {
                assertEquals(true, CharUtils.isAsciiAlphaUpper((char) i));
            } else {
                assertEquals(false, CharUtils.isAsciiAlphaUpper((char) i));
            }
        }
    }

// org.apache.commons.lang.CharUtilsTest::testIsAsciiAlphaLower_char
    public void testIsAsciiAlphaLower_char() {
        assertEquals(true, CharUtils.isAsciiAlphaLower('a'));
        assertEquals(false, CharUtils.isAsciiAlphaLower('A'));
        assertEquals(false, CharUtils.isAsciiAlphaLower('3'));
        assertEquals(false, CharUtils.isAsciiAlphaLower('-'));
        assertEquals(false, CharUtils.isAsciiAlphaLower('\n'));
        assertEquals(false, CharUtils.isAsciiAlphaLower(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i >= 'a' && i <= 'z') {
                assertEquals(true, CharUtils.isAsciiAlphaLower((char) i));
            } else {
                assertEquals(false, CharUtils.isAsciiAlphaLower((char) i));
            }
        }
    }

// org.apache.commons.lang.CharUtilsTest::testIsAsciiNumeric_char
    public void testIsAsciiNumeric_char() {
        assertEquals(false, CharUtils.isAsciiNumeric('a'));
        assertEquals(false, CharUtils.isAsciiNumeric('A'));
        assertEquals(true, CharUtils.isAsciiNumeric('3'));
        assertEquals(false, CharUtils.isAsciiNumeric('-'));
        assertEquals(false, CharUtils.isAsciiNumeric('\n'));
        assertEquals(false, CharUtils.isAsciiNumeric(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i >= '0' && i <= '9') {
                assertEquals(true, CharUtils.isAsciiNumeric((char) i));
            } else {
                assertEquals(false, CharUtils.isAsciiNumeric((char) i));
            }
        }
    }

// org.apache.commons.lang.CharUtilsTest::testIsAsciiAlphanumeric_char
    public void testIsAsciiAlphanumeric_char() {
        assertEquals(true, CharUtils.isAsciiAlphanumeric('a'));
        assertEquals(true, CharUtils.isAsciiAlphanumeric('A'));
        assertEquals(true, CharUtils.isAsciiAlphanumeric('3'));
        assertEquals(false, CharUtils.isAsciiAlphanumeric('-'));
        assertEquals(false, CharUtils.isAsciiAlphanumeric('\n'));
        assertEquals(false, CharUtils.isAsciiAlphanumeric(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if ((i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z') || (i >= '0' && i <= '9')) {
                assertEquals(true, CharUtils.isAsciiAlphanumeric((char) i));
            } else {
                assertEquals(false, CharUtils.isAsciiAlphanumeric((char) i));
            }
        }
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "]").toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]").toString());
        
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=hello" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "]").append("a", "hello").toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "  a=hello" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]").append("a", "hello").toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=hello" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "  b=4" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<Integer>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<size=0>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=[]" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<size=0>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a={}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<size=0>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a={}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "  b=4" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {<null>,5,{3,6}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {<null>,5,{3,6}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {1,2,-3,4}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {1,2,-3,4}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {{1,2},<null>,{5}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {{1,2},<null>,{5}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[3,4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[<size=0>]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[<size=0>]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[{}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[{}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[3,4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals("", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals("", new ToStringBuilder(base).appendSuper("").toString());
        assertEquals("<null>", new ToStringBuilder(base).appendSuper("<null>").toString());
        
        assertEquals("hello", new ToStringBuilder(base).appendSuper("").append("a", "hello").toString());
        assertEquals("<null>,hello", new ToStringBuilder(base).appendSuper("<null>").append("a", "hello").toString());
        assertEquals("hello", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals("<null>", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals("3", new ToStringBuilder(base).append(i3).toString());
        assertEquals("<null>", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals("3", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals("3,4", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals("<Integer>", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals("<size=0>", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals("[]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals("<size=0>", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals("{}", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals("<size=0>", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals("{}", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testLong
    public void testLong() {
        assertEquals("3", new ToStringBuilder(base).append(3L).toString());
        assertEquals("3", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals("3,4", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals("{<null>,5,{3,6}}", new ToStringBuilder(base).append(array).toString());
        assertEquals("{<null>,5,{3,6}}", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals("<null>", new ToStringBuilder(base).append(array).toString());
        assertEquals("<null>", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals("{1,2,-3,4}", new ToStringBuilder(base).append(array).toString());
        assertEquals("{1,2,-3,4}", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals("<null>", new ToStringBuilder(base).append(array).toString());
        assertEquals("<null>", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals("{{1,2},<null>,{5}}", new ToStringBuilder(base).append(array).toString());
        assertEquals("{{1,2},<null>,{5}}", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals("<null>", new ToStringBuilder(base).append(array).toString());
        assertEquals("<null>", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).appendSuper("Integer@8888[%NULL%]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[%NULL%,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[%NULL%]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=%NULL%]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=%Integer%]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[[%NULL%, 5, [3, 6]]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[%NULL%, 5, [3, 6]]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[[1, 2, -3, 4]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[1, 2, -3, 4]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[[[1, 2], %NULL%, [5]]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[[1, 2], %NULL%, [5]]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testConstructorEx1
    public void testConstructorEx1() {
        assertEquals("<null>", new ToStringBuilder(null).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testConstructorEx2
    public void testConstructorEx2() {
        assertEquals("<null>", new ToStringBuilder(null, null).toString());
        new ToStringBuilder(this.base, null).toString();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testConstructorEx3
    public void testConstructorEx3() {
        assertEquals("<null>", new ToStringBuilder(null, null, null).toString());
        new ToStringBuilder(this.base, null, null);
        new ToStringBuilder(this.base, ToStringStyle.DEFAULT_STYLE, null);
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testGetSetDefault
    public void testGetSetDefault() {
        try {
            ToStringBuilder.setDefaultStyle(ToStringStyle.NO_FIELD_NAMES_STYLE);
            assertSame(ToStringStyle.NO_FIELD_NAMES_STYLE, ToStringBuilder.getDefaultStyle());
        } finally {
            
            ToStringBuilder.setDefaultStyle(ToStringStyle.DEFAULT_STYLE);
        }
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSetDefaultEx
    public void testSetDefaultEx() {
        try {
            ToStringBuilder.setDefaultStyle(null);
            
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionInteger
    public void testReflectionInteger() {
        assertEquals(baseStr + "[value=5]", ToStringBuilder.reflectionToString(base));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionCharacter
    public void testReflectionCharacter() {
        Character c = new Character('A');
        assertEquals(this.toBaseString(c) + "[value=A]", ToStringBuilder.reflectionToString(c));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionBoolean
    public void testReflectionBoolean() {
        Boolean b;
        b = Boolean.TRUE;
        assertEquals(this.toBaseString(b) + "[value=true]", ToStringBuilder.reflectionToString(b));
        b = Boolean.FALSE;
        assertEquals(this.toBaseString(b) + "[value=false]", ToStringBuilder.reflectionToString(b));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionObjectArray
    public void testReflectionObjectArray() {
        Object[] array = new Object[] { null, base, new int[] { 3, 6 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionLongArray
    public void testReflectionLongArray() {
        long[] array = new long[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionIntArray
    public void testReflectionIntArray() {
        int[] array = new int[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionShortArray
    public void testReflectionShortArray() {
        short[] array = new short[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionyteArray
    public void testReflectionyteArray() {
        byte[] array = new byte[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionCharArray
    public void testReflectionCharArray() {
        char[] array = new char[] { 'A', '2', '_', 'D' };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{A,2,_,D}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionDoubleArray
    public void testReflectionDoubleArray() {
        double[] array = new double[] { 1.0, 2.9876, -3.00001, 4.3 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionFloatArray
    public void testReflectionFloatArray() {
        float[] array = new float[] { 1.0f, 2.9876f, -3.00001f, 4.3f };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionBooleanArray
    public void testReflectionBooleanArray() {
        boolean[] array = new boolean[] { true, false, false };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{true,false,false}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionFloatArrayArray
    public void testReflectionFloatArrayArray() {
        float[][] array = new float[][] { { 1.0f, 2.29686f }, null, { Float.NaN } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionLongArrayArray
    public void testReflectionLongArrayArray() {
        long[][] array = new long[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionIntArrayArray
    public void testReflectionIntArrayArray() {
        int[][] array = new int[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionhortArrayArray
    public void testReflectionhortArrayArray() {
        short[][] array = new short[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionByteArrayArray
    public void testReflectionByteArrayArray() {
        byte[][] array = new byte[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionCharArrayArray
    public void testReflectionCharArrayArray() {
        char[][] array = new char[][] { { 'A', 'B' }, null, { 'p' } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{A,B},<null>,{p}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionDoubleArrayArray
    public void testReflectionDoubleArrayArray() {
        double[][] array = new double[][] { { 1.0, 2.29686 }, null, { Double.NaN } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionBooleanArrayArray
    public void testReflectionBooleanArrayArray() {
        boolean[][] array = new boolean[][] { { true, false }, null, { false } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", ToStringBuilder.reflectionToString(array));
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionHierarchyArrayList
    public void testReflectionHierarchyArrayList() {}

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionHierarchy
    public void testReflectionHierarchy() {
        ReflectionTestFixtureA baseA = new ReflectionTestFixtureA();
        String baseStr = this.toBaseString(baseA);
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false));
        assertEquals(baseStr + "[a=a,transientA=t]", ToStringBuilder.reflectionToString(baseA, null, true));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false, null));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false, Object.class));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false, List.class));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false, ReflectionTestFixtureA.class));
        
        ReflectionTestFixtureB baseB = new ReflectionTestFixtureB();
        baseStr = this.toBaseString(baseB);
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false));
        assertEquals(baseStr + "[b=b,transientB=t,a=a,transientA=t]", ToStringBuilder.reflectionToString(baseB, null, true));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false, null));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false, Object.class));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false, List.class));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false, ReflectionTestFixtureA.class));
        assertEquals(baseStr + "[b=b]", ToStringBuilder.reflectionToString(baseB, null, false, ReflectionTestFixtureB.class));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testInnerClassReflection
    public void testInnerClassReflection() {
        Outer outer = new Outer();
        assertEquals(toBaseString(outer) + "[inner=" + toBaseString(outer.inner) + "[]]", outer.toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionArrayCycle
    public void testReflectionArrayCycle() throws Exception {
        Object[] objects = new Object[1];
        objects[0] = objects;
        assertEquals(
            this.toBaseString(objects) + "[{" + this.toBaseString(objects) + "}]",
            ToStringBuilder.reflectionToString(objects));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionArrayCycleLevel2
    public void testReflectionArrayCycleLevel2() throws Exception {
        Object[] objects = new Object[1];
        Object[] objectsLevel2 = new Object[1];
        objects[0] = objectsLevel2;
        objectsLevel2[0] = (Object) objects;
        assertEquals(
            this.toBaseString(objects) + "[{{" + this.toBaseString(objects) + "}}]",
            ToStringBuilder.reflectionToString(objects));
        assertEquals(
            this.toBaseString(objectsLevel2) + "[{{" + this.toBaseString(objectsLevel2) + "}}]",
            ToStringBuilder.reflectionToString(objectsLevel2));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionArrayArrayCycle
    public void testReflectionArrayArrayCycle() throws Exception {
        Object[][] objects = new Object[2][2];
        objects[0][0] = objects;
        objects[0][1] = objects;
        objects[1][0] = objects;
        objects[1][1] = objects;
        String basicToString = this.toBaseString(objects);
        assertEquals(
            basicToString
                + "[{{"
                + basicToString
                + ","
                + basicToString
                + "},{"
                + basicToString
                + ","
                + basicToString
                + "}}]",
            ToStringBuilder.reflectionToString(objects));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSimpleReflectionObjectCycle
    public void testSimpleReflectionObjectCycle() throws Exception {
        SimpleReflectionTestFixture simple = new SimpleReflectionTestFixture();
        simple.o = simple;
        assertTrue(ToStringStyle.getRegistry().isEmpty());
        assertEquals(this.toBaseString(simple) + "[o=" + this.toBaseString(simple) + "]", simple.toString());
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSelfInstanceVarReflectionObjectCycle
    public void testSelfInstanceVarReflectionObjectCycle() throws Exception {
        SelfInstanceVarReflectionTestFixture test = new SelfInstanceVarReflectionTestFixture();
        assertTrue(ToStringStyle.getRegistry().isEmpty());
        assertEquals(this.toBaseString(test) + "[typeIsSelf=" + this.toBaseString(test) + "]", test.toString());
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSelfInstanceTwoVarsReflectionObjectCycle
    public void testSelfInstanceTwoVarsReflectionObjectCycle() throws Exception {
        SelfInstanceTwoVarsReflectionTestFixture test = new SelfInstanceTwoVarsReflectionTestFixture();
        assertTrue(ToStringStyle.getRegistry().isEmpty());
        assertEquals(this.toBaseString(test) + "[typeIsSelf=" + this.toBaseString(test) + ",otherType=" + test.getOtherType().toString() + "]", test.toString());
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionObjectCycle
    public void testReflectionObjectCycle() throws Exception {
        ReflectionTestCycleA a = new ReflectionTestCycleA();
        ReflectionTestCycleB b = new ReflectionTestCycleB();
        a.b = b;
        b.a = a;
        assertEquals(
            this.toBaseString(a) + "[b=" + this.toBaseString(b) + "[a=" + this.toBaseString(a) + "]]",
            a.toString());
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionArrayAndObjectCycle
    public void testReflectionArrayAndObjectCycle() throws Exception {
        Object[] objects = new Object[1];
        SimpleReflectionTestFixture simple = new SimpleReflectionTestFixture(objects);
        objects[0] = (Object) simple;
        assertEquals(
            this.toBaseString(objects)
                + "[{"
                + this.toBaseString(simple)
                + "[o="
                + this.toBaseString(objects)
                + "]"
                + "}]",
            ToStringBuilder.reflectionToString(objects));
        assertEquals(
            this.toBaseString(simple)
                + "[o={"
                + this.toBaseString(simple)
                + "}]",
            ToStringBuilder.reflectionToString(simple));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testAppendToString
    public void testAppendToString() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendToString("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendToString("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendToString("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendToString("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendToString(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testInt
    public void testInt() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append((int) 3).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", (int) 3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", (int) 3).append("b", (int) 4).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testShort
    public void testShort() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append((short) 3).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", (short) 3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", (short) 3).append("b", (short) 4).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testChar
    public void testChar() {
        assertEquals(baseStr + "[A]", new ToStringBuilder(base).append((char) 65).toString());
        assertEquals(baseStr + "[a=A]", new ToStringBuilder(base).append("a", (char) 65).toString());
        assertEquals(baseStr + "[a=A,b=B]", new ToStringBuilder(base).append("a", (char) 65).append("b", (char) 66).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testByte
    public void testByte() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append((byte) 3).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", (byte) 3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", (byte) 3).append("b", (byte) 4).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testDouble
    public void testDouble() {
        assertEquals(baseStr + "[3.2]", new ToStringBuilder(base).append((double) 3.2).toString());
        assertEquals(baseStr + "[a=3.2]", new ToStringBuilder(base).append("a", (double) 3.2).toString());
        assertEquals(baseStr + "[a=3.2,b=4.3]", new ToStringBuilder(base).append("a", (double) 3.2).append("b", (double) 4.3).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testFloat
    public void testFloat() {
        assertEquals(baseStr + "[3.2]", new ToStringBuilder(base).append((float) 3.2).toString());
        assertEquals(baseStr + "[a=3.2]", new ToStringBuilder(base).append("a", (float) 3.2).toString());
        assertEquals(baseStr + "[a=3.2,b=4.3]", new ToStringBuilder(base).append("a", (float) 3.2).append("b", (float) 4.3).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testBoolean
    public void testBoolean() {
        assertEquals(baseStr + "[true]", new ToStringBuilder(base).append(true).toString());
        assertEquals(baseStr + "[a=true]", new ToStringBuilder(base).append("a", true).toString());
        assertEquals(baseStr + "[a=true,b=false]", new ToStringBuilder(base).append("a", true).append("b", false).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testIntArray
    public void testIntArray() {
        int[] array = new int[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testShortArray
    public void testShortArray() {
        short[] array = new short[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testByteArray
    public void testByteArray() {
        byte[] array = new byte[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testCharArray
    public void testCharArray() {
        char[] array = new char[] {'A', '2', '_', 'D'};
        assertEquals(baseStr + "[{A,2,_,D}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{A,2,_,D}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testDoubleArray
    public void testDoubleArray() {
        double[] array = new double[] {1.0, 2.9876, -3.00001, 4.3};
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testFloatArray
    public void testFloatArray() {
        float[] array = new float[] {1.0f, 2.9876f, -3.00001f, 4.3f};
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testBooleanArray
    public void testBooleanArray() {
        boolean[] array = new boolean[] {true, false, false};
        assertEquals(baseStr + "[{true,false,false}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{true,false,false}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testIntArrayArray
    public void testIntArrayArray() {
        int[][] array = new int[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testShortArrayArray
    public void testShortArrayArray() {
        short[][] array = new short[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testByteArrayArray
    public void testByteArrayArray() {
        byte[][] array = new byte[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testCharArrayArray
    public void testCharArrayArray() {
        char[][] array = new char[][] {{'A', 'B'}, null, {'p'}};
        assertEquals(baseStr + "[{{A,B},<null>,{p}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{A,B},<null>,{p}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testDoubleArrayArray
    public void testDoubleArrayArray() {
        double[][] array = new double[][] {{1.0, 2.29686}, null, {Double.NaN}};
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testFloatArrayArray
    public void testFloatArrayArray() {
        float[][] array = new float[][] {{1.0f, 2.29686f}, null, {Float.NaN}};
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testBooleanArrayArray
    public void testBooleanArrayArray() {
        boolean[][] array = new boolean[][] {{true, false}, null, {false}};
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testObjectCycle
    public void testObjectCycle() {
        ObjectCycle a = new ObjectCycle();
        ObjectCycle b = new ObjectCycle();
        a.obj = b;
        b.obj = a;
       
        String expected = toBaseString(a) + "[" + toBaseString(b) + "[" + toBaseString(a) + "]]";
        assertEquals(expected, a.toString());
        validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSimpleReflectionStatics
    public void testSimpleReflectionStatics() {
        SimpleReflectionStaticFieldsFixture instance1 = new SimpleReflectionStaticFieldsFixture();
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345]",
            ReflectionToStringBuilder.toString(instance1, null, false, true, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345]",
            ReflectionToStringBuilder.toString(instance1, null, true, true, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345]",
            this.toStringWithStatics(instance1, null, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345]",
            this.toStringWithStatics(instance1, null, SimpleReflectionStaticFieldsFixture.class));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionStatics
    public void testReflectionStatics() {
        ReflectionStaticFieldsFixture instance1 = new ReflectionStaticFieldsFixture();
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345,instanceString=instanceString,instanceInt=67890]",
            ReflectionToStringBuilder.toString(instance1, null, false, true, ReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345,staticTransientString=staticTransientString,staticTransientInt=54321,instanceString=instanceString,instanceInt=67890,transientString=transientString,transientInt=98765]",
            ReflectionToStringBuilder.toString(instance1, null, true, true, ReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345,instanceString=instanceString,instanceInt=67890]",
            this.toStringWithStatics(instance1, null, ReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345,instanceString=instanceString,instanceInt=67890]",
            this.toStringWithStatics(instance1, null, ReflectionStaticFieldsFixture.class));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testInheritedReflectionStatics
    public void testInheritedReflectionStatics() {
        InheritedReflectionStaticFieldsFixture instance1 = new InheritedReflectionStaticFieldsFixture();
        assertEquals(
            this.toBaseString(instance1) + "[staticString2=staticString2,staticInt2=67890]",
            ReflectionToStringBuilder.toString(instance1, null, false, true, InheritedReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString2=staticString2,staticInt2=67890,staticString=staticString,staticInt=12345]",
            ReflectionToStringBuilder.toString(instance1, null, false, true, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString2=staticString2,staticInt2=67890,staticString=staticString,staticInt=12345]",
            this.toStringWithStatics(instance1, null, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString2=staticString2,staticInt2=67890,staticString=staticString,staticInt=12345]",
            this.toStringWithStatics(instance1, null, SimpleReflectionStaticFieldsFixture.class));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionNull
    public void testReflectionNull() {
        assertEquals("<null>", ReflectionToStringBuilder.toString(null));
    }
