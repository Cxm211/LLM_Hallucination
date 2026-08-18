// buggy code
    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
            return cs1.equals(cs2);
    }

// relevant test
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

// org.apache.commons.lang3.ArrayUtilsTest::testHashCode
    public void testHashCode() {
        long[][] array1 = new long[][] {{2,5}, {4,5}};
        long[][] array2 = new long[][] {{2,5}, {4,6}};
        assertEquals(true, ArrayUtils.hashCode(array1) == ArrayUtils.hashCode(array1));
        assertEquals(false, ArrayUtils.hashCode(array1) == ArrayUtils.hashCode(array2));
        
        Object[] array3 = new Object[] {new String(new char[] {'A', 'B'})};
        Object[] array4 = new Object[] {"AB"};
        assertEquals(true, ArrayUtils.hashCode(array3) == ArrayUtils.hashCode(array3));
        assertEquals(true, ArrayUtils.hashCode(array3) == ArrayUtils.hashCode(array4));
        
        Object[] arrayA = new Object[] {new boolean[] {true, false}, new int[] {6, 7}};
        Object[] arrayB = new Object[] {new boolean[] {true, false}, new int[] {6, 7}};
        assertEquals(true, ArrayUtils.hashCode(arrayB) == ArrayUtils.hashCode(arrayA));
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

// org.apache.commons.lang3.ArrayUtilsTest::testArrayCreation
    public void testArrayCreation()
    {
        final String[] array = ArrayUtils.toArray("foo", "bar");
        assertEquals(2, array.length);
        assertEquals("foo", array[0]);
        assertEquals("bar", array[1]);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testArrayCreationWithGeneralReturnType
    public void testArrayCreationWithGeneralReturnType()
    {
        final Object obj = ArrayUtils.toArray("foo", "bar");
        assertTrue(obj instanceof String[]);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testArrayCreationWithDifferentTypes
    public void testArrayCreationWithDifferentTypes()
    {
        final Number[] array = ArrayUtils.<Number>toArray(Integer.valueOf(42), Double.valueOf(Math.PI));
        assertEquals(2, array.length);
        assertEquals(Integer.valueOf(42), array[0]);
        assertEquals(Double.valueOf(Math.PI), array[1]);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndirectArrayCreation
    public void testIndirectArrayCreation()
    {
        final String[] array = toArrayPropagatingType("foo", "bar");
        assertEquals(2, array.length);
        assertEquals("foo", array[0]);
        assertEquals("bar", array[1]);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testEmptyArrayCreation
    public void testEmptyArrayCreation()
    {
        final String[] array = ArrayUtils.<String>toArray();
        assertEquals(0, array.length);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIndirectEmptyArrayCreation
    public void testIndirectEmptyArrayCreation()
    {
        final String[] array = ArrayUtilsTest.<String>toArrayPropagatingType();
        assertEquals(0, array.length);
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

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyBoolean
    public void testNullToEmptyBoolean() {
        
        assertEquals(ArrayUtils.EMPTY_BOOLEAN_ARRAY, ArrayUtils.nullToEmpty((boolean[]) null));
        
        boolean[] original = new boolean[] {true, false};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        boolean[] empty = new boolean[]{};
        boolean[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_BOOLEAN_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyLong
    public void testNullToEmptyLong() {
        
        assertEquals(ArrayUtils.EMPTY_LONG_ARRAY, ArrayUtils.nullToEmpty((long[]) null));
        
        long[] original = new long[] {1L, 2L};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        long[] empty = new long[]{};
        long[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_LONG_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyInt
    public void testNullToEmptyInt() {
        
        assertEquals(ArrayUtils.EMPTY_INT_ARRAY, ArrayUtils.nullToEmpty((int[]) null));
        
        int[] original = new int[] {1, 2};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        int[] empty = new int[]{};
        int[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_INT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyShort
    public void testNullToEmptyShort() {
        
        assertEquals(ArrayUtils.EMPTY_SHORT_ARRAY, ArrayUtils.nullToEmpty((short[]) null));
        
        short[] original = new short[] {1, 2};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        short[] empty = new short[]{};
        short[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_SHORT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyChar
    public void testNullToEmptyChar() {
        
        assertEquals(ArrayUtils.EMPTY_CHAR_ARRAY, ArrayUtils.nullToEmpty((char[]) null));
        
        char[] original = new char[] {'a', 'b'};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        char[] empty = new char[]{};
        char[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_CHAR_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyByte
    public void testNullToEmptyByte() {
        
        assertEquals(ArrayUtils.EMPTY_BYTE_ARRAY, ArrayUtils.nullToEmpty((byte[]) null));
        
        byte[] original = new byte[] {0x0F, 0x0E};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        byte[] empty = new byte[]{};
        byte[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_BYTE_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyDouble
    public void testNullToEmptyDouble() {
        
        assertEquals(ArrayUtils.EMPTY_DOUBLE_ARRAY, ArrayUtils.nullToEmpty((double[]) null));
        
        double[] original = new double[] {1L, 2L};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        double[] empty = new double[]{};
        double[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_DOUBLE_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyFloat
    public void testNullToEmptyFloat() {
        
        assertEquals(ArrayUtils.EMPTY_FLOAT_ARRAY, ArrayUtils.nullToEmpty((float[]) null));
        
        float[] original = new float[] {2.6f, 3.8f};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        float[] empty = new float[]{};
        float[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_FLOAT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyObject
    public void testNullToEmptyObject() {
        
        assertEquals(ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.nullToEmpty((Object[]) null));
        
        Object[] original = new Object[] {Boolean.TRUE, Boolean.FALSE};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        Object[] empty = new Object[]{};
        Object[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_OBJECT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyString
    public void testNullToEmptyString() {
        
        assertEquals(ArrayUtils.EMPTY_STRING_ARRAY, ArrayUtils.nullToEmpty((String[]) null));
        
        String[] original = new String[] {"abc", "def"};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        String[] empty = new String[]{};
        String[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_STRING_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyBooleanObject
    public void testNullToEmptyBooleanObject() {
        
        assertEquals(ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY, ArrayUtils.nullToEmpty((Boolean[]) null));
        
        Boolean[] original = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        Boolean[] empty = new Boolean[]{};
        Boolean[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyLongObject
    public void testNullToEmptyLongObject() {
        
        assertEquals(ArrayUtils.EMPTY_LONG_OBJECT_ARRAY, ArrayUtils.nullToEmpty((Long[]) null));
        
        @SuppressWarnings("boxing")
        Long[] original = new Long[] {1L, 2L};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        Long[] empty = new Long[]{};
        Long[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_LONG_OBJECT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyIntObject
    public void testNullToEmptyIntObject() {
        
        assertEquals(ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY, ArrayUtils.nullToEmpty((Integer[]) null));
        
        Integer[] original = new Integer[] {1, 2};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        Integer[] empty = new Integer[]{};
        Integer[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyShortObject
    public void testNullToEmptyShortObject() {
        
        assertEquals(ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY, ArrayUtils.nullToEmpty((Short[]) null));
        
        @SuppressWarnings("boxing")
        Short[] original = new Short[] {1, 2};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        Short[] empty = new Short[]{};
        Short[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyCharObject
    public void testNullToEmptyCharObject() {
        
        assertEquals(ArrayUtils.EMPTY_CHARACTER_OBJECT_ARRAY, ArrayUtils.nullToEmpty((Character[]) null));
        
        Character[] original = new Character[] {'a', 'b'};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        Character[] empty = new Character[]{};
        Character[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_CHARACTER_OBJECT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyByteObject
    public void testNullToEmptyByteObject() {
        
        assertEquals(ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY, ArrayUtils.nullToEmpty((Byte[]) null));
        
        Byte[] original = new Byte[] {0x0F, 0x0E};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        Byte[] empty = new Byte[]{};
        Byte[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyDoubleObject
    public void testNullToEmptyDoubleObject() {
        
        assertEquals(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY, ArrayUtils.nullToEmpty((Double[]) null));
        
        Double[] original = new Double[] {1D, 2D};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        Double[] empty = new Double[]{};
        Double[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY, result);
        assertTrue(empty != result);
    }

// org.apache.commons.lang3.ArrayUtilsTest::testNullToEmptyFloatObject
    public void testNullToEmptyFloatObject() {
        
        assertEquals(ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY, ArrayUtils.nullToEmpty((Float[]) null));
        
        Float[] original = new Float[] {2.6f, 3.8f};
        assertEquals(original, ArrayUtils.nullToEmpty(original));
        
        Float[] empty = new Float[]{};
        Float[] result = ArrayUtils.nullToEmpty(empty);
        assertEquals(ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY, result);
        assertTrue(empty != result);
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
            ArrayUtils.toPrimitive(new Short[] {Short.valueOf(Short.MIN_VALUE), 
                Short.valueOf(Short.MAX_VALUE), Short.valueOf((short)9999999)}))
        );

        try {
            ArrayUtils.toPrimitive(new Short[] {Short.valueOf(Short.MIN_VALUE), null});
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
            ArrayUtils.toPrimitive(new Short[] {Short.valueOf(Short.MIN_VALUE), 
                Short.valueOf(Short.MAX_VALUE), Short.valueOf((short)9999999)}, Short.MIN_VALUE))
        );
        
        assertTrue(Arrays.equals(
            new short[] {Short.MIN_VALUE, Short.MAX_VALUE, (short)9999999},
            ArrayUtils.toPrimitive(new Short[] {Short.valueOf(Short.MIN_VALUE), null, 
                Short.valueOf((short)9999999)}, Short.MAX_VALUE))
        );
    }

// org.apache.commons.lang3.ArrayUtilsTest::testToObject_short
    public void testToObject_short() {
        final short[] b = null;
        assertEquals(null, ArrayUtils.toObject(b));
        
        assertSame(ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY, 
        ArrayUtils.toObject(new short[0]));
        
        assertTrue(Arrays.equals(
            new Short[] {Short.valueOf(Short.MIN_VALUE), Short.valueOf(Short.MAX_VALUE), 
                Short.valueOf((short)9999999)},
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
             ArrayUtils.toPrimitive(new Integer[] {Integer.valueOf(Integer.MIN_VALUE), 
                 Integer.valueOf(Integer.MAX_VALUE), Integer.valueOf(9999999)}))
         );

         try {
             ArrayUtils.toPrimitive(new Integer[] {Integer.valueOf(Integer.MIN_VALUE), null});
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
             ArrayUtils.toPrimitive(new Integer[] {Integer.valueOf(Integer.MIN_VALUE), 
                 Integer.valueOf(Integer.MAX_VALUE), Integer.valueOf(9999999)},1)));
         assertTrue(Arrays.equals(
             new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Integer[] {Integer.valueOf(Integer.MIN_VALUE), 
                 null, Integer.valueOf(9999999)}, Integer.MAX_VALUE))
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
                    Integer.valueOf(Integer.MIN_VALUE),
                    Integer.valueOf(Integer.MAX_VALUE),
                    Integer.valueOf(9999999)},
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
             ArrayUtils.toPrimitive(new Long[] {Long.valueOf(Long.MIN_VALUE), 
                 Long.valueOf(Long.MAX_VALUE), Long.valueOf(9999999)}))
         );

         try {
             ArrayUtils.toPrimitive(new Long[] {Long.valueOf(Long.MIN_VALUE), null});
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
             ArrayUtils.toPrimitive(new Long[] {Long.valueOf(Long.MIN_VALUE), 
                 Long.valueOf(Long.MAX_VALUE), Long.valueOf(9999999)},1)));
         
         assertTrue(Arrays.equals(
             new long[] {Long.MIN_VALUE, Long.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Long[] {Long.valueOf(Long.MIN_VALUE), 
                 null, Long.valueOf(9999999)}, Long.MAX_VALUE))
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
                    Long.valueOf(Long.MIN_VALUE),
                    Long.valueOf(Long.MAX_VALUE),
                    Long.valueOf(9999999)},
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
             ArrayUtils.toPrimitive(new Float[] {Float.valueOf(Float.MIN_VALUE), 
                 Float.valueOf(Float.MAX_VALUE), Float.valueOf(9999999)}))
         );

         try {
             ArrayUtils.toPrimitive(new Float[] {Float.valueOf(Float.MIN_VALUE), null});
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
             ArrayUtils.toPrimitive(new Float[] {Float.valueOf(Float.MIN_VALUE), 
                 Float.valueOf(Float.MAX_VALUE), Float.valueOf(9999999)},1)));
         
         assertTrue(Arrays.equals(
             new float[] {Float.MIN_VALUE, Float.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Float[] {Float.valueOf(Float.MIN_VALUE), 
                 null, Float.valueOf(9999999)}, Float.MAX_VALUE))
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
                    Float.valueOf(Float.MIN_VALUE),
                    Float.valueOf(Float.MAX_VALUE),
                    Float.valueOf(9999999)},
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
             ArrayUtils.toPrimitive(new Double[] {Double.valueOf(Double.MIN_VALUE), 
                 Double.valueOf(Double.MAX_VALUE), Double.valueOf(9999999)}))
         );

         try {
             ArrayUtils.toPrimitive(new Float[] {Float.valueOf(Float.MIN_VALUE), null});
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
             ArrayUtils.toPrimitive(new Double[] {Double.valueOf(Double.MIN_VALUE), 
                 Double.valueOf(Double.MAX_VALUE), Double.valueOf(9999999)},1)));
         
         assertTrue(Arrays.equals(
             new double[] {Double.MIN_VALUE, Double.MAX_VALUE, 9999999},
             ArrayUtils.toPrimitive(new Double[] {Double.valueOf(Double.MIN_VALUE), 
                 null, Double.valueOf(9999999)}, Double.MAX_VALUE))
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
                    Double.valueOf(Double.MIN_VALUE),
                    Double.valueOf(Double.MAX_VALUE),
                    Double.valueOf(9999999)},
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

// org.apache.commons.lang3.ArrayUtilsTest::testIsNotEmptyObject
    public void testIsNotEmptyObject() {
        Object[] emptyArray = new Object[] {};
        Object[] notEmptyArray = new Object[] { new String("Value") };
        assertFalse(ArrayUtils.isNotEmpty((Object[])null));
        assertFalse(ArrayUtils.isNotEmpty(emptyArray));
        assertTrue(ArrayUtils.isNotEmpty(notEmptyArray));
    }

// org.apache.commons.lang3.ArrayUtilsTest::testIsNotEmptyPrimitives
    public void testIsNotEmptyPrimitives() {
        long[] emptyLongArray = new long[] {};
        long[] notEmptyLongArray = new long[] { 1L };
        assertFalse(ArrayUtils.isNotEmpty((long[])null));
        assertFalse(ArrayUtils.isNotEmpty(emptyLongArray));
        assertTrue(ArrayUtils.isNotEmpty(notEmptyLongArray));

        int[] emptyIntArray = new int[] {};
        int[] notEmptyIntArray = new int[] { 1 };
        assertFalse(ArrayUtils.isNotEmpty((int[])null));
        assertFalse(ArrayUtils.isNotEmpty(emptyIntArray));
        assertTrue(ArrayUtils.isNotEmpty(notEmptyIntArray));

        short[] emptyShortArray = new short[] {};
        short[] notEmptyShortArray = new short[] { 1 };
        assertFalse(ArrayUtils.isNotEmpty((short[])null));
        assertFalse(ArrayUtils.isNotEmpty(emptyShortArray));
        assertTrue(ArrayUtils.isNotEmpty(notEmptyShortArray));

        char[] emptyCharArray = new char[] {};
        char[] notEmptyCharArray = new char[] { 1 };
        assertFalse(ArrayUtils.isNotEmpty((char[])null));
        assertFalse(ArrayUtils.isNotEmpty(emptyCharArray));
        assertTrue(ArrayUtils.isNotEmpty(notEmptyCharArray));

        byte[] emptyByteArray = new byte[] {};
        byte[] notEmptyByteArray = new byte[] { 1 };
        assertFalse(ArrayUtils.isNotEmpty((byte[])null));
        assertFalse(ArrayUtils.isNotEmpty(emptyByteArray));
        assertTrue(ArrayUtils.isNotEmpty(notEmptyByteArray));

        double[] emptyDoubleArray = new double[] {};
        double[] notEmptyDoubleArray = new double[] { 1.0 };
        assertFalse(ArrayUtils.isNotEmpty((double[])null));
        assertFalse(ArrayUtils.isNotEmpty(emptyDoubleArray));
        assertTrue(ArrayUtils.isNotEmpty(notEmptyDoubleArray));

        float[] emptyFloatArray = new float[] {};
        float[] notEmptyFloatArray = new float[] { 1.0F };
        assertFalse(ArrayUtils.isNotEmpty((float[])null));
        assertFalse(ArrayUtils.isNotEmpty(emptyFloatArray));
        assertTrue(ArrayUtils.isNotEmpty(notEmptyFloatArray));

        boolean[] emptyBooleanArray = new boolean[] {};
        boolean[] notEmptyBooleanArray = new boolean[] { true };
        assertFalse(ArrayUtils.isNotEmpty((boolean[])null));
        assertFalse(ArrayUtils.isNotEmpty(emptyBooleanArray));
        assertTrue(ArrayUtils.isNotEmpty(notEmptyBooleanArray));
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

// org.apache.commons.lang3.CharUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new CharUtils());
        Constructor<?>[] cons = CharUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(BooleanUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(BooleanUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.CharUtilsTest::testToCharacterObject_char
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

// org.apache.commons.lang3.CharUtilsTest::testToCharacterObject_String
    public void testToCharacterObject_String() {
        assertEquals(null, CharUtils.toCharacterObject(null));
        assertEquals(null, CharUtils.toCharacterObject(""));
        assertEquals(new Character('a'), CharUtils.toCharacterObject("a"));
        assertEquals(new Character('a'), CharUtils.toCharacterObject("abc"));
        assertSame(CharUtils.toCharacterObject("a"), CharUtils.toCharacterObject("a"));
        assertSame(CharUtils.toCharacterObject("a"), CharUtils.toCharacterObject('a'));
    }

// org.apache.commons.lang3.CharUtilsTest::testToChar_Character
    public void testToChar_Character() {
        assertEquals('A', CharUtils.toChar(CHARACTER_A));
        assertEquals('B', CharUtils.toChar(CHARACTER_B));
        try {
            CharUtils.toChar((Character) null);
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.CharUtilsTest::testToChar_Character_char
    public void testToChar_Character_char() {
        assertEquals('A', CharUtils.toChar(CHARACTER_A, 'X'));
        assertEquals('B', CharUtils.toChar(CHARACTER_B, 'X'));
        assertEquals('X', CharUtils.toChar((Character) null, 'X'));
    }

// org.apache.commons.lang3.CharUtilsTest::testToChar_String
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

// org.apache.commons.lang3.CharUtilsTest::testToChar_String_char
    public void testToChar_String_char() {
        assertEquals('A', CharUtils.toChar("A", 'X'));
        assertEquals('B', CharUtils.toChar("BA", 'X'));
        assertEquals('X', CharUtils.toChar("", 'X'));
        assertEquals('X', CharUtils.toChar((String) null, 'X'));
    }

// org.apache.commons.lang3.CharUtilsTest::testToIntValue_char
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

// org.apache.commons.lang3.CharUtilsTest::testToIntValue_char_int
    public void testToIntValue_char_int() {
        assertEquals(0, CharUtils.toIntValue('0', -1));
        assertEquals(3, CharUtils.toIntValue('3', -1));
        assertEquals(-1, CharUtils.toIntValue('a', -1));
    }

// org.apache.commons.lang3.CharUtilsTest::testToIntValue_Character
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

// org.apache.commons.lang3.CharUtilsTest::testToIntValue_Character_int
    public void testToIntValue_Character_int() {
        assertEquals(0, CharUtils.toIntValue(new Character('0'), -1));
        assertEquals(3, CharUtils.toIntValue(new Character('3'), -1));
        assertEquals(-1, CharUtils.toIntValue(new Character('A'), -1));
        assertEquals(-1, CharUtils.toIntValue(null, -1));
    }

// org.apache.commons.lang3.CharUtilsTest::testToString_char
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

// org.apache.commons.lang3.CharUtilsTest::testToString_Character
    public void testToString_Character() {
        assertEquals(null, CharUtils.toString(null));
        assertEquals("A", CharUtils.toString(CHARACTER_A));
        assertSame(CharUtils.toString(CHARACTER_A), CharUtils.toString(CHARACTER_A));
    }

// org.apache.commons.lang3.CharUtilsTest::testToUnicodeEscaped_char
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

// org.apache.commons.lang3.CharUtilsTest::testToUnicodeEscaped_Character
    public void testToUnicodeEscaped_Character() {
        assertEquals(null, CharUtils.unicodeEscaped(null));
        assertEquals("\\u0041", CharUtils.unicodeEscaped(CHARACTER_A));
    }

// org.apache.commons.lang3.CharUtilsTest::testIsAscii_char
    public void testIsAscii_char() {
        assertTrue(CharUtils.isAscii('a'));
        assertTrue(CharUtils.isAscii('A'));
        assertTrue(CharUtils.isAscii('3'));
        assertTrue(CharUtils.isAscii('-'));
        assertTrue(CharUtils.isAscii('\n'));
        assertFalse(CharUtils.isAscii(CHAR_COPY));
       
        for (int i = 0; i < 128; i++) {
            if (i < 128) {
                assertTrue(CharUtils.isAscii((char) i));
            } else {
                assertFalse(CharUtils.isAscii((char) i));
            }
        }
    }

// org.apache.commons.lang3.CharUtilsTest::testIsAsciiPrintable_char
    public void testIsAsciiPrintable_char() {
        assertTrue(CharUtils.isAsciiPrintable('a'));
        assertTrue(CharUtils.isAsciiPrintable('A'));
        assertTrue(CharUtils.isAsciiPrintable('3'));
        assertTrue(CharUtils.isAsciiPrintable('-'));
        assertFalse(CharUtils.isAsciiPrintable('\n'));
        assertFalse(CharUtils.isAscii(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i >= 32 && i <= 126) {
                assertTrue(CharUtils.isAsciiPrintable((char) i));
            } else {
                assertFalse(CharUtils.isAsciiPrintable((char) i));
            }
        }
    }

// org.apache.commons.lang3.CharUtilsTest::testIsAsciiControl_char
    public void testIsAsciiControl_char() {
        assertFalse(CharUtils.isAsciiControl('a'));
        assertFalse(CharUtils.isAsciiControl('A'));
        assertFalse(CharUtils.isAsciiControl('3'));
        assertFalse(CharUtils.isAsciiControl('-'));
        assertTrue(CharUtils.isAsciiControl('\n'));
        assertFalse(CharUtils.isAsciiControl(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i < 32 || i == 127) {
                assertTrue(CharUtils.isAsciiControl((char) i));
            } else {
                assertFalse(CharUtils.isAsciiControl((char) i));
            }
        }
    }

// org.apache.commons.lang3.CharUtilsTest::testIsAsciiAlpha_char
    public void testIsAsciiAlpha_char() {
        assertTrue(CharUtils.isAsciiAlpha('a'));
        assertTrue(CharUtils.isAsciiAlpha('A'));
        assertFalse(CharUtils.isAsciiAlpha('3'));
        assertFalse(CharUtils.isAsciiAlpha('-'));
        assertFalse(CharUtils.isAsciiAlpha('\n'));
        assertFalse(CharUtils.isAsciiAlpha(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if ((i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z')) {
                assertTrue(CharUtils.isAsciiAlpha((char) i));
            } else {
                assertFalse(CharUtils.isAsciiAlpha((char) i));
            }
        }
    }

// org.apache.commons.lang3.CharUtilsTest::testIsAsciiAlphaUpper_char
    public void testIsAsciiAlphaUpper_char() {
        assertFalse(CharUtils.isAsciiAlphaUpper('a'));
        assertTrue(CharUtils.isAsciiAlphaUpper('A'));
        assertFalse(CharUtils.isAsciiAlphaUpper('3'));
        assertFalse(CharUtils.isAsciiAlphaUpper('-'));
        assertFalse(CharUtils.isAsciiAlphaUpper('\n'));
        assertFalse(CharUtils.isAsciiAlphaUpper(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i >= 'A' && i <= 'Z') {
                assertTrue(CharUtils.isAsciiAlphaUpper((char) i));
            } else {
                assertFalse(CharUtils.isAsciiAlphaUpper((char) i));
            }
        }
    }

// org.apache.commons.lang3.CharUtilsTest::testIsAsciiAlphaLower_char
    public void testIsAsciiAlphaLower_char() {
        assertTrue(CharUtils.isAsciiAlphaLower('a'));
        assertFalse(CharUtils.isAsciiAlphaLower('A'));
        assertFalse(CharUtils.isAsciiAlphaLower('3'));
        assertFalse(CharUtils.isAsciiAlphaLower('-'));
        assertFalse(CharUtils.isAsciiAlphaLower('\n'));
        assertFalse(CharUtils.isAsciiAlphaLower(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i >= 'a' && i <= 'z') {
                assertTrue(CharUtils.isAsciiAlphaLower((char) i));
            } else {
                assertFalse(CharUtils.isAsciiAlphaLower((char) i));
            }
        }
    }

// org.apache.commons.lang3.CharUtilsTest::testIsAsciiNumeric_char
    public void testIsAsciiNumeric_char() {
        assertFalse(CharUtils.isAsciiNumeric('a'));
        assertFalse(CharUtils.isAsciiNumeric('A'));
        assertTrue(CharUtils.isAsciiNumeric('3'));
        assertFalse(CharUtils.isAsciiNumeric('-'));
        assertFalse(CharUtils.isAsciiNumeric('\n'));
        assertFalse(CharUtils.isAsciiNumeric(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if (i >= '0' && i <= '9') {
                assertTrue(CharUtils.isAsciiNumeric((char) i));
            } else {
                assertFalse(CharUtils.isAsciiNumeric((char) i));
            }
        }
    }

// org.apache.commons.lang3.CharUtilsTest::testIsAsciiAlphanumeric_char
    public void testIsAsciiAlphanumeric_char() {
        assertTrue(CharUtils.isAsciiAlphanumeric('a'));
        assertTrue(CharUtils.isAsciiAlphanumeric('A'));
        assertTrue(CharUtils.isAsciiAlphanumeric('3'));
        assertFalse(CharUtils.isAsciiAlphanumeric('-'));
        assertFalse(CharUtils.isAsciiAlphanumeric('\n'));
        assertFalse(CharUtils.isAsciiAlphanumeric(CHAR_COPY));
       
        for (int i = 0; i < 196; i++) {
            if ((i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z') || (i >= '0' && i <= '9')) {
                assertTrue(CharUtils.isAsciiAlphanumeric((char) i));
            } else {
                assertFalse(CharUtils.isAsciiAlphanumeric((char) i));
            }
        }
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

        
        class Named extends Object {}
        assertEquals("ClassUtilsTest.1", ClassUtils.getShortClassName(new Object(){}, "<null>"));
        assertEquals("ClassUtilsTest.1Named", ClassUtils.getShortClassName(new Named(), "<null>"));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortClassName(new Inner(), "<null>"));
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
        
        
        class Named extends Object {}
        assertEquals("ClassUtilsTest.2", ClassUtils.getShortClassName(new Object(){}.getClass()));
        assertEquals("ClassUtilsTest.2Named", ClassUtils.getShortClassName(Named.class));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortClassName(Inner.class));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getShortClassName_String
    public void test_getShortClassName_String() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class.getName()));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getSimpleName_Class
    public void test_getSimpleName_Class() {
        assertEquals("ClassUtils", ClassUtils.getSimpleName(ClassUtils.class));
        assertEquals("Entry", ClassUtils.getSimpleName(Map.Entry.class));
        assertEquals("", ClassUtils.getSimpleName((Class<?>) null));

        
        assertEquals("String[]", ClassUtils.getSimpleName(String[].class));
        assertEquals("Entry[]", ClassUtils.getSimpleName(Map.Entry[].class));

        
        assertEquals("boolean", ClassUtils.getSimpleName(boolean.class));
        assertEquals("byte", ClassUtils.getSimpleName(byte.class));
        assertEquals("char", ClassUtils.getSimpleName(char.class));
        assertEquals("short", ClassUtils.getSimpleName(short.class));
        assertEquals("int", ClassUtils.getSimpleName(int.class));
        assertEquals("long", ClassUtils.getSimpleName(long.class));
        assertEquals("float", ClassUtils.getSimpleName(float.class));
        assertEquals("double", ClassUtils.getSimpleName(double.class));

        
        assertEquals("boolean[]", ClassUtils.getSimpleName(boolean[].class));
        assertEquals("byte[]", ClassUtils.getSimpleName(byte[].class));
        assertEquals("char[]", ClassUtils.getSimpleName(char[].class));
        assertEquals("short[]", ClassUtils.getSimpleName(short[].class));
        assertEquals("int[]", ClassUtils.getSimpleName(int[].class));
        assertEquals("long[]", ClassUtils.getSimpleName(long[].class));
        assertEquals("float[]", ClassUtils.getSimpleName(float[].class));
        assertEquals("double[]", ClassUtils.getSimpleName(double[].class));

        
        assertEquals("String[][]", ClassUtils.getSimpleName(String[][].class));
        assertEquals("String[][][]", ClassUtils.getSimpleName(String[][][].class));
        assertEquals("String[][][][]", ClassUtils.getSimpleName(String[][][][].class));
        
        
        class Named extends Object {}
        assertEquals("", ClassUtils.getSimpleName(new Object(){}.getClass()));
        assertEquals("Named", ClassUtils.getSimpleName(Named.class));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getSimpleName_Object
    public void test_getSimpleName_Object() {
        assertEquals("ClassUtils", ClassUtils.getSimpleName(new ClassUtils(), "<null>"));
        assertEquals("Inner", ClassUtils.getSimpleName(new Inner(), "<null>"));
        assertEquals("String", ClassUtils.getSimpleName("hello", "<null>"));
        assertEquals("<null>", ClassUtils.getSimpleName(null, "<null>"));
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
        
        
        class Named extends Object {}
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(new Object(){}.getClass()));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(Named.class));
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
        Class<?>[] arrayPrimitives = { Integer.TYPE, Boolean.TYPE };
        Class<?>[] arrayWrappers = { Integer.class, Boolean.class };

        assertFalse(ClassUtils.isAssignable(array1, array2));
        assertFalse(ClassUtils.isAssignable(null, array2));
        assertTrue(ClassUtils.isAssignable(null, array0));
        assertTrue(ClassUtils.isAssignable(array0, array0));

        assertTrue(ClassUtils.isAssignable(array0, (Class<?>[]) null)); 
        assertTrue(ClassUtils.isAssignable((Class[]) null, (Class[]) null));

        assertFalse(ClassUtils.isAssignable(array1, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1));

        boolean autoboxing = SystemUtils.isJavaVersionAtLeast(JAVA_1_5);

        assertEquals(autoboxing, ClassUtils.isAssignable(arrayPrimitives, arrayWrappers));
        assertEquals(autoboxing, ClassUtils.isAssignable(arrayWrappers, arrayPrimitives));
        assertFalse(ClassUtils.isAssignable(arrayPrimitives, array1));
        assertFalse(ClassUtils.isAssignable(arrayWrappers, array1));
        assertEquals(autoboxing, ClassUtils.isAssignable(arrayPrimitives, array2));
        assertTrue(ClassUtils.isAssignable(arrayWrappers, array2));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_isAssignable_ClassArray_ClassArray_Autoboxing
    public void test_isAssignable_ClassArray_ClassArray_Autoboxing() throws Exception {
        Class<?>[] array2 = new Class[] {Object.class, Object.class};
        Class<?>[] array1 = new Class[] {Object.class};
        Class<?>[] array1s = new Class[] {String.class};
        Class<?>[] array0 = new Class[] {};
        Class<?>[] arrayPrimitives = { Integer.TYPE, Boolean.TYPE };
        Class<?>[] arrayWrappers = { Integer.class, Boolean.class };

        assertFalse(ClassUtils.isAssignable(array1, array2, true));
        assertFalse(ClassUtils.isAssignable(null, array2, true));
        assertTrue(ClassUtils.isAssignable(null, array0, true));
        assertTrue(ClassUtils.isAssignable(array0, array0, true));
        assertTrue(ClassUtils.isAssignable(array0, null, true));
        assertTrue(ClassUtils.isAssignable((Class[]) null, (Class[]) null, true));

        assertFalse(ClassUtils.isAssignable(array1, array1s, true));
        assertTrue(ClassUtils.isAssignable(array1s, array1s, true));
        assertTrue(ClassUtils.isAssignable(array1s, array1, true));

        assertTrue(ClassUtils.isAssignable(arrayPrimitives, arrayWrappers, true));
        assertTrue(ClassUtils.isAssignable(arrayWrappers, arrayPrimitives, true));
        assertFalse(ClassUtils.isAssignable(arrayPrimitives, array1, true));
        assertFalse(ClassUtils.isAssignable(arrayWrappers, array1, true));
        assertTrue(ClassUtils.isAssignable(arrayPrimitives, array2, true));
        assertTrue(ClassUtils.isAssignable(arrayWrappers, array2, true));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_isAssignable_ClassArray_ClassArray_NoAutoboxing
    public void test_isAssignable_ClassArray_ClassArray_NoAutoboxing() throws Exception {
        Class<?>[] array2 = new Class[] {Object.class, Object.class};
        Class<?>[] array1 = new Class[] {Object.class};
        Class<?>[] array1s = new Class[] {String.class};
        Class<?>[] array0 = new Class[] {};
        Class<?>[] arrayPrimitives = { Integer.TYPE, Boolean.TYPE };
        Class<?>[] arrayWrappers = { Integer.class, Boolean.class };

        assertFalse(ClassUtils.isAssignable(array1, array2, false));
        assertFalse(ClassUtils.isAssignable(null, array2, false));
        assertTrue(ClassUtils.isAssignable(null, array0, false));
        assertTrue(ClassUtils.isAssignable(array0, array0, false));
        assertTrue(ClassUtils.isAssignable(array0, null, false));
        assertTrue(ClassUtils.isAssignable((Class[]) null, (Class[]) null, false));

        assertFalse(ClassUtils.isAssignable(array1, array1s, false));
        assertTrue(ClassUtils.isAssignable(array1s, array1s, false));
        assertTrue(ClassUtils.isAssignable(array1s, array1, false));

        assertFalse(ClassUtils.isAssignable(arrayPrimitives, arrayWrappers, false));
        assertFalse(ClassUtils.isAssignable(arrayWrappers, arrayPrimitives, false));
        assertFalse(ClassUtils.isAssignable(arrayPrimitives, array1, false));
        assertFalse(ClassUtils.isAssignable(arrayWrappers, array1, false));
        assertTrue(ClassUtils.isAssignable(arrayWrappers, array2, false));
        assertFalse(ClassUtils.isAssignable(arrayPrimitives, array2, false));
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

        boolean autoboxing = SystemUtils.isJavaVersionAtLeast(JAVA_1_5);

        assertEquals(autoboxing, ClassUtils.isAssignable(Integer.TYPE, Integer.class));
        assertEquals(autoboxing, ClassUtils.isAssignable(Integer.TYPE, Object.class));
        assertEquals(autoboxing, ClassUtils.isAssignable(Integer.class, Integer.TYPE));
        assertEquals(autoboxing, ClassUtils.isAssignable(Integer.class, Object.class));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class));
        assertEquals(autoboxing, ClassUtils.isAssignable(Boolean.TYPE, Boolean.class));
        assertEquals(autoboxing, ClassUtils.isAssignable(Boolean.TYPE, Object.class));
        assertEquals(autoboxing, ClassUtils.isAssignable(Boolean.class, Boolean.TYPE));
        assertEquals(autoboxing, ClassUtils.isAssignable(Boolean.class, Object.class));
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
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Object.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Object.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Object.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class, true));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_isAssignable_NoAutoboxing
    public void test_isAssignable_NoAutoboxing() throws Exception {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null, false));
        assertFalse(ClassUtils.isAssignable(String.class, null, false));

        assertTrue(ClassUtils.isAssignable(null, Object.class, false));
        assertTrue(ClassUtils.isAssignable(null, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE, false));
        assertTrue(ClassUtils.isAssignable(String.class, Object.class, false));
        assertTrue(ClassUtils.isAssignable(String.class, String.class, false));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Object.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE, false));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Boolean.class, false));
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Object.class, false));
        assertFalse(ClassUtils.isAssignable(Boolean.class, Boolean.TYPE, false));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Object.class, false));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE, false));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class, false));
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

// org.apache.commons.lang3.ClassUtilsTest::test_isAssignable_DefaultUnboxing_Widening
    public void test_isAssignable_DefaultUnboxing_Widening() throws Exception {
        boolean autoboxing = SystemUtils.isJavaVersionAtLeast(JAVA_1_5);

        
        assertFalse("byte -> char", ClassUtils.isAssignable(Byte.class, Character.TYPE));
        assertEquals("byte -> byte", autoboxing, ClassUtils.isAssignable(Byte.class, Byte.TYPE));
        assertEquals("byte -> short", autoboxing, ClassUtils.isAssignable(Byte.class, Short.TYPE));
        assertEquals("byte -> int", autoboxing, ClassUtils.isAssignable(Byte.class, Integer.TYPE));
        assertEquals("byte -> long", autoboxing, ClassUtils.isAssignable(Byte.class, Long.TYPE));
        assertEquals("byte -> float", autoboxing, ClassUtils.isAssignable(Byte.class, Float.TYPE));
        assertEquals("byte -> double", autoboxing, ClassUtils.isAssignable(Byte.class, Double.TYPE));
        assertFalse("byte -> boolean", ClassUtils.isAssignable(Byte.class, Boolean.TYPE));

        
        assertFalse("short -> char", ClassUtils.isAssignable(Short.class, Character.TYPE));
        assertFalse("short -> byte", ClassUtils.isAssignable(Short.class, Byte.TYPE));
        assertEquals("short -> short", autoboxing, ClassUtils.isAssignable(Short.class, Short.TYPE));
        assertEquals("short -> int", autoboxing, ClassUtils.isAssignable(Short.class, Integer.TYPE));
        assertEquals("short -> long", autoboxing, ClassUtils.isAssignable(Short.class, Long.TYPE));
        assertEquals("short -> float", autoboxing, ClassUtils.isAssignable(Short.class, Float.TYPE));
        assertEquals("short -> double", autoboxing, ClassUtils.isAssignable(Short.class, Double.TYPE));
        assertFalse("short -> boolean", ClassUtils.isAssignable(Short.class, Boolean.TYPE));

        
        assertEquals("char -> char", autoboxing, ClassUtils.isAssignable(Character.class, Character.TYPE));
        assertFalse("char -> byte", ClassUtils.isAssignable(Character.class, Byte.TYPE));
        assertFalse("char -> short", ClassUtils.isAssignable(Character.class, Short.TYPE));
        assertEquals("char -> int", autoboxing, ClassUtils.isAssignable(Character.class, Integer.TYPE));
        assertEquals("char -> long", autoboxing, ClassUtils.isAssignable(Character.class, Long.TYPE));
        assertEquals("char -> float", autoboxing, ClassUtils.isAssignable(Character.class, Float.TYPE));
        assertEquals("char -> double", autoboxing, ClassUtils.isAssignable(Character.class, Double.TYPE));
        assertFalse("char -> boolean", ClassUtils.isAssignable(Character.class, Boolean.TYPE));

        
        assertFalse("int -> char", ClassUtils.isAssignable(Integer.class, Character.TYPE));
        assertFalse("int -> byte", ClassUtils.isAssignable(Integer.class, Byte.TYPE));
        assertFalse("int -> short", ClassUtils.isAssignable(Integer.class, Short.TYPE));
        assertEquals("int -> int", autoboxing, ClassUtils.isAssignable(Integer.class, Integer.TYPE));
        assertEquals("int -> long", autoboxing, ClassUtils.isAssignable(Integer.class, Long.TYPE));
        assertEquals("int -> float", autoboxing, ClassUtils.isAssignable(Integer.class, Float.TYPE));
        assertEquals("int -> double", autoboxing, ClassUtils.isAssignable(Integer.class, Double.TYPE));
        assertFalse("int -> boolean", ClassUtils.isAssignable(Integer.class, Boolean.TYPE));

        
        assertFalse("long -> char", ClassUtils.isAssignable(Long.class, Character.TYPE));
        assertFalse("long -> byte", ClassUtils.isAssignable(Long.class, Byte.TYPE));
        assertFalse("long -> short", ClassUtils.isAssignable(Long.class, Short.TYPE));
        assertFalse("long -> int", ClassUtils.isAssignable(Long.class, Integer.TYPE));
        assertEquals("long -> long", autoboxing, ClassUtils.isAssignable(Long.class, Long.TYPE));
        assertEquals("long -> float", autoboxing, ClassUtils.isAssignable(Long.class, Float.TYPE));
        assertEquals("long -> double", autoboxing, ClassUtils.isAssignable(Long.class, Double.TYPE));
        assertFalse("long -> boolean", ClassUtils.isAssignable(Long.class, Boolean.TYPE));

        
        assertFalse("float -> char", ClassUtils.isAssignable(Float.class, Character.TYPE));
        assertFalse("float -> byte", ClassUtils.isAssignable(Float.class, Byte.TYPE));
        assertFalse("float -> short", ClassUtils.isAssignable(Float.class, Short.TYPE));
        assertFalse("float -> int", ClassUtils.isAssignable(Float.class, Integer.TYPE));
        assertFalse("float -> long", ClassUtils.isAssignable(Float.class, Long.TYPE));
        assertEquals("float -> float", autoboxing, ClassUtils.isAssignable(Float.class, Float.TYPE));
        assertEquals("float -> double", autoboxing, ClassUtils.isAssignable(Float.class, Double.TYPE));
        assertFalse("float -> boolean", ClassUtils.isAssignable(Float.class, Boolean.TYPE));

        
        assertFalse("double -> char", ClassUtils.isAssignable(Double.class, Character.TYPE));
        assertFalse("double -> byte", ClassUtils.isAssignable(Double.class, Byte.TYPE));
        assertFalse("double -> short", ClassUtils.isAssignable(Double.class, Short.TYPE));
        assertFalse("double -> int", ClassUtils.isAssignable(Double.class, Integer.TYPE));
        assertFalse("double -> long", ClassUtils.isAssignable(Double.class, Long.TYPE));
        assertFalse("double -> float", ClassUtils.isAssignable(Double.class, Float.TYPE));
        assertEquals("double -> double", autoboxing, ClassUtils.isAssignable(Double.class, Double.TYPE));
        assertFalse("double -> boolean", ClassUtils.isAssignable(Double.class, Boolean.TYPE));

        
        assertFalse("boolean -> char", ClassUtils.isAssignable(Boolean.class, Character.TYPE));
        assertFalse("boolean -> byte", ClassUtils.isAssignable(Boolean.class, Byte.TYPE));
        assertFalse("boolean -> short", ClassUtils.isAssignable(Boolean.class, Short.TYPE));
        assertFalse("boolean -> int", ClassUtils.isAssignable(Boolean.class, Integer.TYPE));
        assertFalse("boolean -> long", ClassUtils.isAssignable(Boolean.class, Long.TYPE));
        assertFalse("boolean -> float", ClassUtils.isAssignable(Boolean.class, Float.TYPE));
        assertFalse("boolean -> double", ClassUtils.isAssignable(Boolean.class, Double.TYPE));
        assertEquals("boolean -> boolean", autoboxing, ClassUtils.isAssignable(Boolean.class, Boolean.TYPE));
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

// org.apache.commons.lang3.ClassUtilsTest::testIsPrimitiveOrWrapper
    public void testIsPrimitiveOrWrapper() {

        
        assertTrue("Boolean.class", ClassUtils.isPrimitiveOrWrapper(Boolean.class));
        assertTrue("Byte.class", ClassUtils.isPrimitiveOrWrapper(Byte.class));
        assertTrue("Character.class", ClassUtils.isPrimitiveOrWrapper(Character.class));
        assertTrue("Short.class", ClassUtils.isPrimitiveOrWrapper(Short.class));
        assertTrue("Integer.class", ClassUtils.isPrimitiveOrWrapper(Integer.class));
        assertTrue("Long.class", ClassUtils.isPrimitiveOrWrapper(Long.class));
        assertTrue("Double.class", ClassUtils.isPrimitiveOrWrapper(Double.class));
        assertTrue("Float.class", ClassUtils.isPrimitiveOrWrapper(Float.class));
        
        
        assertTrue("boolean", ClassUtils.isPrimitiveOrWrapper(Boolean.TYPE));
        assertTrue("byte", ClassUtils.isPrimitiveOrWrapper(Byte.TYPE));
        assertTrue("char", ClassUtils.isPrimitiveOrWrapper(Character.TYPE));
        assertTrue("short", ClassUtils.isPrimitiveOrWrapper(Short.TYPE));
        assertTrue("int", ClassUtils.isPrimitiveOrWrapper(Integer.TYPE));
        assertTrue("long", ClassUtils.isPrimitiveOrWrapper(Long.TYPE));
        assertTrue("double", ClassUtils.isPrimitiveOrWrapper(Double.TYPE));
        assertTrue("float", ClassUtils.isPrimitiveOrWrapper(Float.TYPE));
        assertTrue("Void.TYPE", ClassUtils.isPrimitiveOrWrapper(Void.TYPE));
        
        
        assertFalse("null", ClassUtils.isPrimitiveOrWrapper(null));
        assertFalse("Void.class", ClassUtils.isPrimitiveOrWrapper(Void.class));
        assertFalse("String.class", ClassUtils.isPrimitiveOrWrapper(String.class));
        assertFalse("this.getClass()", ClassUtils.isPrimitiveOrWrapper(this.getClass()));
    }

// org.apache.commons.lang3.ClassUtilsTest::testIsPrimitiveWrapper
    public void testIsPrimitiveWrapper() {

        
        assertTrue("Boolean.class", ClassUtils.isPrimitiveWrapper(Boolean.class));
        assertTrue("Byte.class", ClassUtils.isPrimitiveWrapper(Byte.class));
        assertTrue("Character.class", ClassUtils.isPrimitiveWrapper(Character.class));
        assertTrue("Short.class", ClassUtils.isPrimitiveWrapper(Short.class));
        assertTrue("Integer.class", ClassUtils.isPrimitiveWrapper(Integer.class));
        assertTrue("Long.class", ClassUtils.isPrimitiveWrapper(Long.class));
        assertTrue("Double.class", ClassUtils.isPrimitiveWrapper(Double.class));
        assertTrue("Float.class", ClassUtils.isPrimitiveWrapper(Float.class));
        
        
        assertFalse("boolean", ClassUtils.isPrimitiveWrapper(Boolean.TYPE));
        assertFalse("byte", ClassUtils.isPrimitiveWrapper(Byte.TYPE));
        assertFalse("char", ClassUtils.isPrimitiveWrapper(Character.TYPE));
        assertFalse("short", ClassUtils.isPrimitiveWrapper(Short.TYPE));
        assertFalse("int", ClassUtils.isPrimitiveWrapper(Integer.TYPE));
        assertFalse("long", ClassUtils.isPrimitiveWrapper(Long.TYPE));
        assertFalse("double", ClassUtils.isPrimitiveWrapper(Double.TYPE));
        assertFalse("float", ClassUtils.isPrimitiveWrapper(Float.TYPE));
        
        
        assertFalse("null", ClassUtils.isPrimitiveWrapper(null));
        assertFalse("Void.class", ClassUtils.isPrimitiveWrapper(Void.class));
        assertFalse("Void.TYPE", ClassUtils.isPrimitiveWrapper(Void.TYPE));
        assertFalse("String.class", ClassUtils.isPrimitiveWrapper(String.class));
        assertFalse("this.getClass()", ClassUtils.isPrimitiveWrapper(this.getClass()));
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
        

        assertNull("null -> null", ClassUtils.primitivesToWrappers((Class<?>[]) null)); 
        
        assertTrue("empty -> empty", Arrays.equals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.primitivesToWrappers()));
        Class<?>[] castNull = ClassUtils.primitivesToWrappers((Class<?>)null); 
        assertTrue("(Class<?>)null -> [null]", Arrays.equals(new Class<?>[]{null}, castNull));
        
        
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
        for (Class<?> primitive : primitives) {
            Class<?> wrapperCls = ClassUtils.primitiveToWrapper(primitive);
            assertFalse("Still primitive", wrapperCls.isPrimitive());
            assertEquals(wrapperCls + " -> " + primitive, primitive,
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

        assertNull("Wrong result for null input", ClassUtils.wrappersToPrimitives((Class<?>[]) null)); 
        
        assertTrue("empty -> empty", Arrays.equals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.wrappersToPrimitives()));
        Class<?>[] castNull = ClassUtils.wrappersToPrimitives((Class<?>)null); 
        assertTrue("(Class<?>)null -> [null]", Arrays.equals(new Class<?>[]{null}, castNull));
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

// org.apache.commons.lang3.ClassUtilsTest::testGetInnerClass
    public void testGetInnerClass() throws ClassNotFoundException {
        assertEquals( Inner.DeeplyNested.class, ClassUtils.getClass( "org.apache.commons.lang3.ClassUtilsTest.Inner.DeeplyNested" ) );
        assertEquals( Inner.DeeplyNested.class, ClassUtils.getClass( "org.apache.commons.lang3.ClassUtilsTest.Inner$DeeplyNested" ) );
        assertEquals( Inner.DeeplyNested.class, ClassUtils.getClass( "org.apache.commons.lang3.ClassUtilsTest$Inner$DeeplyNested" ) );
        assertEquals( Inner.DeeplyNested.class, ClassUtils.getClass( "org.apache.commons.lang3.ClassUtilsTest$Inner.DeeplyNested" ) );
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
        assertEquals( java.util.Map.Entry[].class, ClassUtils.getClass( "java.util.Map.Entry[]" ) );
        assertEquals( java.util.Map.Entry[].class, ClassUtils.getClass( "java.util.Map$Entry[]" ) );
        assertEquals( java.util.Map.Entry[].class, ClassUtils.getClass( "[Ljava.util.Map.Entry;" ) );
        assertEquals( java.util.Map.Entry[].class, ClassUtils.getClass( "[Ljava.util.Map$Entry;" ) );
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

        assertNull(ClassUtils.toClass((Object[]) null)); 
        
        
        assertTrue("empty -> empty", Arrays.equals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass()));
        Class<?>[] castNull = ClassUtils.toClass((Object) null); 
        assertTrue("(Object)null -> [null]", Arrays.equals(new Object[]{null}, castNull));

        assertSame(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass(ArrayUtils.EMPTY_OBJECT_ARRAY));

        assertTrue(Arrays.equals(new Class[] { String.class, Integer.class, Double.class },
                ClassUtils.toClass(new Object[] { "Test", Integer.valueOf(1), Double.valueOf(99d) })));

        assertTrue(Arrays.equals(new Class[] { String.class, null, Double.class },
                ClassUtils.toClass(new Object[] { "Test", null, Double.valueOf(99d) })));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getShortCanonicalName_Object
    public void test_getShortCanonicalName_Object() {
        assertEquals("<null>", ClassUtils.getShortCanonicalName(null, "<null>"));
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(new ClassUtils(), "<null>"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName(new ClassUtils[0], "<null>"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName(new ClassUtils[0][0], "<null>"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(new int[0], "<null>"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(new int[0][0], "<null>"));

        
        class Named extends Object {}
        assertEquals("ClassUtilsTest.6", ClassUtils.getShortCanonicalName(new Object(){}, "<null>"));
        assertEquals("ClassUtilsTest.5Named", ClassUtils.getShortCanonicalName(new Named(), "<null>"));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortCanonicalName(new Inner(), "<null>"));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getShortCanonicalName_Class
    public void test_getShortCanonicalName_Class() {
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(ClassUtils.class));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName(ClassUtils[].class));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName(ClassUtils[][].class));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(int[].class));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(int[][].class));
        
        
        class Named extends Object {}
        assertEquals("ClassUtilsTest.7", ClassUtils.getShortCanonicalName(new Object(){}.getClass()));
        assertEquals("ClassUtilsTest.6Named", ClassUtils.getShortCanonicalName(Named.class));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortCanonicalName(Inner.class));
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
        
        
        assertEquals("ClassUtilsTest.6", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtilsTest$6"));
        assertEquals("ClassUtilsTest.5Named", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtilsTest$5Named"));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtilsTest$Inner"));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getPackageCanonicalName_Object
    public void test_getPackageCanonicalName_Object() {
        assertEquals("<null>", ClassUtils.getPackageCanonicalName(null, "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new ClassUtils(), "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new ClassUtils[0], "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new ClassUtils[0][0], "<null>"));
        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0], "<null>"));
        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0][0], "<null>"));
        
        
        class Named extends Object {}
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new Object(){}, "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new Named(), "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new Inner(), "<null>"));
    }

// org.apache.commons.lang3.ClassUtilsTest::test_getPackageCanonicalName_Class
    public void test_getPackageCanonicalName_Class() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils.class));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils[].class));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils[][].class));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[].class));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[][].class));
        
        
        class Named extends Object {}
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new Object(){}.getClass()));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(Named.class));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(Inner.class));
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
        
        
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtilsTest$6"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtilsTest$5Named"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtilsTest$Inner"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new StringEscapeUtils());
        Constructor<?>[] cons = StringEscapeUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(StringEscapeUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(StringEscapeUtils.class.getModifiers()));
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
        assertEscapeJava("Should use capitalized Unicode hex", "\\uABCD", "\uabcd");

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
        
        assertUnescapeJava("lowercase Unicode", "\uABCDx", "\\uabcdx");
        assertUnescapeJava("uppercase Unicode", "\uABCDx", "\\uABCDx");
        assertUnescapeJava("Unicode as final character", "\uABCD", "\\uabcd");
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
        for (int i = 0; i < HTML_ESCAPES.length; ++i) {
            String message = HTML_ESCAPES[i][0];
            String expected = HTML_ESCAPES[i][1];
            String original = HTML_ESCAPES[i][2];
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
        for (int i = 0; i < HTML_ESCAPES.length; ++i) {
            String message = HTML_ESCAPES[i][0];
            String expected = HTML_ESCAPES[i][2];
            String original = HTML_ESCAPES[i][1];
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
    public void testUnescapeUnknownEntity() throws Exception {
        assertEquals("&zzzz;", StringEscapeUtils.unescapeHtml4("&zzzz;"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHtmlVersions
    public void testEscapeHtmlVersions() throws Exception {
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
        assertEquals("XML should be able to unescape >0x7f values with one leading 0",
                "\u00A0", StringEscapeUtils.unescapeXml("&#0160;"));
        assertEquals("XML should be able to unescape >0x7f values with two leading 0s",
                "\u00A0", StringEscapeUtils.unescapeXml("&#00160;"));
        assertEquals("XML should be able to unescape >0x7f values with three leading 0s",
                "\u00A0", StringEscapeUtils.unescapeXml("&#000160;"));

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

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeXmlSupplementaryCharacters
    public void testEscapeXmlSupplementaryCharacters() {
        CharSequenceTranslator escapeXml = 
            StringEscapeUtils.ESCAPE_XML.with( NumericEntityEscaper.between(0x7f, Integer.MAX_VALUE) );

        assertEquals("Supplementary character must be represented using a single escape", "&#144308;",
                escapeXml.translate("\uD84C\uDFB4"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeXmlSupplementaryCharacters
    public void testUnescapeXmlSupplementaryCharacters() {
        assertEquals("Supplementary character must be represented using a single escape", "\uD84C\uDFB4",
                StringEscapeUtils.unescapeXml("&#144308;") );
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
    public void testEscapeCsvString() throws Exception {
        assertEquals("foo.bar",          StringEscapeUtils.escapeCsv("foo.bar"));
        assertEquals("\"foo,bar\"",      StringEscapeUtils.escapeCsv("foo,bar"));
        assertEquals("\"foo\nbar\"",     StringEscapeUtils.escapeCsv("foo\nbar"));
        assertEquals("\"foo\rbar\"",     StringEscapeUtils.escapeCsv("foo\rbar"));
        assertEquals("\"foo\"\"bar\"",   StringEscapeUtils.escapeCsv("foo\"bar"));
        assertEquals("",   StringEscapeUtils.escapeCsv(""));
        assertEquals(null, StringEscapeUtils.escapeCsv(null));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeCsvWriter
    public void testEscapeCsvWriter() throws Exception {
        checkCsvEscapeWriter("foo.bar",        "foo.bar");
        checkCsvEscapeWriter("\"foo,bar\"",    "foo,bar");
        checkCsvEscapeWriter("\"foo\nbar\"",   "foo\nbar");
        checkCsvEscapeWriter("\"foo\rbar\"",   "foo\rbar");
        checkCsvEscapeWriter("\"foo\"\"bar\"", "foo\"bar");
        checkCsvEscapeWriter("", null);
        checkCsvEscapeWriter("", "");
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testUnescapeCsvString
    public void testUnescapeCsvString() throws Exception {
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
    public void testUnescapeCsvWriter() throws Exception {
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
        assertEquals( "High Unicode should not have been escaped", original, escaped);

        String unescaped = StringEscapeUtils.unescapeHtml4( escaped );
        assertEquals( "High Unicode should have been unchanged", original, unescaped);

    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testEscapeHiragana
    public void testEscapeHiragana() {
        
        String original = "\u304B\u304C\u3068";
        String escaped = StringEscapeUtils.escapeHtml4(original);
        assertEquals( "Hiragana character Unicode behaviour should not be being escaped by escapeHtml4",
        original, escaped);

        String unescaped = StringEscapeUtils.unescapeHtml4( escaped );

        assertEquals( "Hiragana character Unicode behaviour has changed - expected no unescaping", escaped, unescaped);
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testLang708
    public void testLang708() throws IOException {
        String input = IOUtils.toString(new FileInputStream("src/test/resources/lang-708-input.txt"), "UTF-8");
        String escaped = StringEscapeUtils.escapeEcmaScript(input);
        
        assertTrue(escaped, escaped.endsWith("}]"));
        
        assertTrue(escaped, escaped.endsWith("\"valueCode\\\":\\\"\\\"}]"));
    }

// org.apache.commons.lang3.StringEscapeUtilsTest::testLang720
    public void testLang720() {
        String input = new StringBuilder("\ud842\udfb7").append("A").toString();
        String escaped = StringEscapeUtils.escapeXml(input);
        assertEquals(input, escaped);
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContains_Char
    public void testContains_Char() {
        assertEquals(false, StringUtils.contains(null, ' '));
        assertEquals(false, StringUtils.contains("", ' '));
        assertEquals(false, StringUtils.contains("", null));
        assertEquals(false, StringUtils.contains(null, null));
        assertEquals(true, StringUtils.contains("abc", 'a'));
        assertEquals(true, StringUtils.contains("abc", 'b'));
        assertEquals(true, StringUtils.contains("abc", 'c'));
        assertEquals(false, StringUtils.contains("abc", 'z'));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContains_String
    public void testContains_String() {
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

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContains_StringWithBadSupplementaryChars
    public void testContains_StringWithBadSupplementaryChars() {
        
        assertEquals(false, StringUtils.contains(CharUSuppCharHigh, CharU20001));
        assertEquals(false, StringUtils.contains(CharUSuppCharLow, CharU20001));
        assertEquals(false, StringUtils.contains(CharU20001, CharUSuppCharHigh));
        assertEquals(0, CharU20001.indexOf(CharUSuppCharLow));
        assertEquals(true, StringUtils.contains(CharU20001, CharUSuppCharLow));
        assertEquals(true, StringUtils.contains(CharU20001 + CharUSuppCharLow + "a", "a"));
        assertEquals(true, StringUtils.contains(CharU20001 + CharUSuppCharHigh + "a", "a"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContains_StringWithSupplementaryChars
    public void testContains_StringWithSupplementaryChars() {
        assertEquals(true, StringUtils.contains(CharU20000 + CharU20001, CharU20000));
        assertEquals(true, StringUtils.contains(CharU20000 + CharU20001, CharU20001));
        assertEquals(true, StringUtils.contains(CharU20000, CharU20000));
        assertEquals(false, StringUtils.contains(CharU20000, CharU20001));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsAny_StringCharArray
    public void testContainsAny_StringCharArray() {
        assertFalse(StringUtils.containsAny(null, (char[]) null));
        assertFalse(StringUtils.containsAny(null, new char[0]));
        assertFalse(StringUtils.containsAny(null, new char[] { 'a', 'b' }));

        assertFalse(StringUtils.containsAny("", (char[]) null));
        assertFalse(StringUtils.containsAny("", new char[0]));
        assertFalse(StringUtils.containsAny("", new char[] { 'a', 'b' }));

        assertFalse(StringUtils.containsAny("zzabyycdxx", (char[]) null));
        assertFalse(StringUtils.containsAny("zzabyycdxx", new char[0]));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[] { 'z', 'a' }));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[] { 'b', 'y' }));
        assertFalse(StringUtils.containsAny("ab", new char[] { 'z' }));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsAny_StringCharArrayWithBadSupplementaryChars
    public void testContainsAny_StringCharArrayWithBadSupplementaryChars() {
        
        assertEquals(false, StringUtils.containsAny(CharUSuppCharHigh, CharU20001.toCharArray()));
        assertEquals(false, StringUtils.containsAny("abc" + CharUSuppCharHigh + "xyz", CharU20001.toCharArray()));
        assertEquals(-1, CharUSuppCharLow.indexOf(CharU20001));
        assertEquals(false, StringUtils.containsAny(CharUSuppCharLow, CharU20001.toCharArray()));
        assertEquals(false, StringUtils.containsAny(CharU20001, CharUSuppCharHigh.toCharArray()));
        assertEquals(0, CharU20001.indexOf(CharUSuppCharLow));
        assertEquals(true, StringUtils.containsAny(CharU20001, CharUSuppCharLow.toCharArray()));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsAny_StringCharArrayWithSupplementaryChars
    public void testContainsAny_StringCharArrayWithSupplementaryChars() {
        assertEquals(true, StringUtils.containsAny(CharU20000 + CharU20001, CharU20000.toCharArray()));
        assertEquals(true, StringUtils.containsAny("a" + CharU20000 + CharU20001, "a".toCharArray()));
        assertEquals(true, StringUtils.containsAny(CharU20000 + "a" + CharU20001, "a".toCharArray()));
        assertEquals(true, StringUtils.containsAny(CharU20000 + CharU20001 + "a", "a".toCharArray()));
        assertEquals(true, StringUtils.containsAny(CharU20000 + CharU20001, CharU20001.toCharArray()));
        assertEquals(true, StringUtils.containsAny(CharU20000, CharU20000.toCharArray()));
        
        assertEquals(-1, CharU20000.indexOf(CharU20001));
        assertEquals(0, CharU20000.indexOf(CharU20001.charAt(0)));
        assertEquals(-1, CharU20000.indexOf(CharU20001.charAt(1)));
        
        assertEquals(false, StringUtils.containsAny(CharU20000, CharU20001.toCharArray()));
        assertEquals(false, StringUtils.containsAny(CharU20001, CharU20000.toCharArray()));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsAny_StringString
    public void testContainsAny_StringString() {
        assertFalse(StringUtils.containsAny(null, (String) null));
        assertFalse(StringUtils.containsAny(null, ""));
        assertFalse(StringUtils.containsAny(null, "ab"));

        assertFalse(StringUtils.containsAny("", (String) null));
        assertFalse(StringUtils.containsAny("", ""));
        assertFalse(StringUtils.containsAny("", "ab"));

        assertFalse(StringUtils.containsAny("zzabyycdxx", (String) null));
        assertFalse(StringUtils.containsAny("zzabyycdxx", ""));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "by"));
        assertFalse(StringUtils.containsAny("ab", "z"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsAny_StringWithBadSupplementaryChars
    public void testContainsAny_StringWithBadSupplementaryChars() {
        
        assertEquals(false, StringUtils.containsAny(CharUSuppCharHigh, CharU20001));
        assertEquals(-1, CharUSuppCharLow.indexOf(CharU20001));
        assertEquals(false, StringUtils.containsAny(CharUSuppCharLow, CharU20001));
        assertEquals(false, StringUtils.containsAny(CharU20001, CharUSuppCharHigh));
        assertEquals(0, CharU20001.indexOf(CharUSuppCharLow));
        assertEquals(true, StringUtils.containsAny(CharU20001, CharUSuppCharLow));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsAny_StringWithSupplementaryChars
    public void testContainsAny_StringWithSupplementaryChars() {
        assertEquals(true, StringUtils.containsAny(CharU20000 + CharU20001, CharU20000));
        assertEquals(true, StringUtils.containsAny(CharU20000 + CharU20001, CharU20001));
        assertEquals(true, StringUtils.containsAny(CharU20000, CharU20000));
        
        assertEquals(-1, CharU20000.indexOf(CharU20001));
        assertEquals(0, CharU20000.indexOf(CharU20001.charAt(0)));
        assertEquals(-1, CharU20000.indexOf(CharU20001.charAt(1)));
        
        assertEquals(false, StringUtils.containsAny(CharU20000, CharU20001));
        assertEquals(false, StringUtils.containsAny(CharU20001, CharU20000));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsIgnoreCase_LocaleIndependence
    public void testContainsIgnoreCase_LocaleIndependence() {
        Locale orig = Locale.getDefault();

        Locale[] locales = { Locale.ENGLISH, new Locale("tr"), Locale.getDefault() };

        String[][] tdata = {
            { "i", "I" },
            { "I", "i" },
            { "\u03C2", "\u03C3" },
            { "\u03A3", "\u03C2" },
            { "\u03A3", "\u03C3" },
        };

        String[][] fdata = {
            { "\u00DF", "SS" },
        };

        try {
            for (Locale locale : locales) {
                Locale.setDefault(locale);
                for (int j = 0; j < tdata.length; j++) {
                    assertTrue(Locale.getDefault() + ": " + j + " " + tdata[j][0] + " " + tdata[j][1], StringUtils
                            .containsIgnoreCase(tdata[j][0], tdata[j][1]));
                }
                for (int j = 0; j < fdata.length; j++) {
                    assertFalse(Locale.getDefault() + ": " + j + " " + fdata[j][0] + " " + fdata[j][1], StringUtils
                            .containsIgnoreCase(fdata[j][0], fdata[j][1]));
                }
            }
        } finally {
            Locale.setDefault(orig);
        }
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

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsNone_CharArray
    public void testContainsNone_CharArray() {
        String str1 = "a";
        String str2 = "b";
        String str3 = "ab.";
        char[] chars1= {'b'};
        char[] chars2= {'.'};
        char[] chars3= {'c', 'd'};
        char[] emptyChars = new char[0];
        assertEquals(true, StringUtils.containsNone(null, (char[]) null));
        assertEquals(true, StringUtils.containsNone("", (char[]) null));
        assertEquals(true, StringUtils.containsNone(null, emptyChars));
        assertEquals(true, StringUtils.containsNone(str1, emptyChars));
        assertEquals(true, StringUtils.containsNone("", emptyChars));
        assertEquals(true, StringUtils.containsNone("", chars1));
        assertEquals(true, StringUtils.containsNone(str1, chars1));
        assertEquals(true, StringUtils.containsNone(str1, chars2));
        assertEquals(true, StringUtils.containsNone(str1, chars3));
        assertEquals(false, StringUtils.containsNone(str2, chars1));
        assertEquals(true, StringUtils.containsNone(str2, chars2));
        assertEquals(true, StringUtils.containsNone(str2, chars3));
        assertEquals(false, StringUtils.containsNone(str3, chars1));
        assertEquals(false, StringUtils.containsNone(str3, chars2));
        assertEquals(true, StringUtils.containsNone(str3, chars3));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsNone_CharArrayWithBadSupplementaryChars
    public void testContainsNone_CharArrayWithBadSupplementaryChars() {
        
        assertEquals(true, StringUtils.containsNone(CharUSuppCharHigh, CharU20001.toCharArray()));
        assertEquals(-1, CharUSuppCharLow.indexOf(CharU20001));
        assertEquals(true, StringUtils.containsNone(CharUSuppCharLow, CharU20001.toCharArray()));
        assertEquals(-1, CharU20001.indexOf(CharUSuppCharHigh));
        assertEquals(true, StringUtils.containsNone(CharU20001, CharUSuppCharHigh.toCharArray()));
        assertEquals(0, CharU20001.indexOf(CharUSuppCharLow));
        assertEquals(false, StringUtils.containsNone(CharU20001, CharUSuppCharLow.toCharArray()));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsNone_CharArrayWithSupplementaryChars
    public void testContainsNone_CharArrayWithSupplementaryChars() {
        assertEquals(false, StringUtils.containsNone(CharU20000 + CharU20001, CharU20000.toCharArray()));
        assertEquals(false, StringUtils.containsNone(CharU20000 + CharU20001, CharU20001.toCharArray()));
        assertEquals(false, StringUtils.containsNone(CharU20000, CharU20000.toCharArray()));
        
        assertEquals(-1, CharU20000.indexOf(CharU20001));
        assertEquals(0, CharU20000.indexOf(CharU20001.charAt(0)));
        assertEquals(-1, CharU20000.indexOf(CharU20001.charAt(1)));
        
        assertEquals(true, StringUtils.containsNone(CharU20000, CharU20001.toCharArray()));
        assertEquals(true, StringUtils.containsNone(CharU20001, CharU20000.toCharArray()));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsNone_String
    public void testContainsNone_String() {
        String str1 = "a";
        String str2 = "b";
        String str3 = "ab.";
        String chars1= "b";
        String chars2= ".";
        String chars3= "cd";
        assertEquals(true, StringUtils.containsNone(null, (String) null));
        assertEquals(true, StringUtils.containsNone("", (String) null));
        assertEquals(true, StringUtils.containsNone(null, ""));
        assertEquals(true, StringUtils.containsNone(str1, ""));
        assertEquals(true, StringUtils.containsNone("", ""));
        assertEquals(true, StringUtils.containsNone("", chars1));
        assertEquals(true, StringUtils.containsNone(str1, chars1));
        assertEquals(true, StringUtils.containsNone(str1, chars2));
        assertEquals(true, StringUtils.containsNone(str1, chars3));
        assertEquals(false, StringUtils.containsNone(str2, chars1));
        assertEquals(true, StringUtils.containsNone(str2, chars2));
        assertEquals(true, StringUtils.containsNone(str2, chars3));
        assertEquals(false, StringUtils.containsNone(str3, chars1));
        assertEquals(false, StringUtils.containsNone(str3, chars2));
        assertEquals(true, StringUtils.containsNone(str3, chars3));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsNone_StringWithBadSupplementaryChars
    public void testContainsNone_StringWithBadSupplementaryChars() {
        
        assertEquals(true, StringUtils.containsNone(CharUSuppCharHigh, CharU20001));
        assertEquals(-1, CharUSuppCharLow.indexOf(CharU20001));
        assertEquals(true, StringUtils.containsNone(CharUSuppCharLow, CharU20001));
        assertEquals(-1, CharU20001.indexOf(CharUSuppCharHigh));
        assertEquals(true, StringUtils.containsNone(CharU20001, CharUSuppCharHigh));
        assertEquals(0, CharU20001.indexOf(CharUSuppCharLow));
        assertEquals(false, StringUtils.containsNone(CharU20001, CharUSuppCharLow));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsNone_StringWithSupplementaryChars
    public void testContainsNone_StringWithSupplementaryChars() {
        assertEquals(false, StringUtils.containsNone(CharU20000 + CharU20001, CharU20000));
        assertEquals(false, StringUtils.containsNone(CharU20000 + CharU20001, CharU20001));
        assertEquals(false, StringUtils.containsNone(CharU20000, CharU20000));
        
        assertEquals(-1, CharU20000.indexOf(CharU20001));
        assertEquals(0, CharU20000.indexOf(CharU20001.charAt(0)));
        assertEquals(-1, CharU20000.indexOf(CharU20001.charAt(1)));
        
        assertEquals(true, StringUtils.containsNone(CharU20000, CharU20001));
        assertEquals(true, StringUtils.containsNone(CharU20001, CharU20000));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsOnly_CharArray
    public void testContainsOnly_CharArray() {
        String str1 = "a";
        String str2 = "b";
        String str3 = "ab";
        char[] chars1= {'b'};
        char[] chars2= {'a'};
        char[] chars3= {'a', 'b'};
        char[] emptyChars = new char[0];
        assertEquals(false, StringUtils.containsOnly(null, (char[]) null));
        assertEquals(false, StringUtils.containsOnly("", (char[]) null));
        assertEquals(false, StringUtils.containsOnly(null, emptyChars));
        assertEquals(false, StringUtils.containsOnly(str1, emptyChars));
        assertEquals(true, StringUtils.containsOnly("", emptyChars));
        assertEquals(true, StringUtils.containsOnly("", chars1));
        assertEquals(false, StringUtils.containsOnly(str1, chars1));
        assertEquals(true, StringUtils.containsOnly(str1, chars2));
        assertEquals(true, StringUtils.containsOnly(str1, chars3));
        assertEquals(true, StringUtils.containsOnly(str2, chars1));
        assertEquals(false, StringUtils.containsOnly(str2, chars2));
        assertEquals(true, StringUtils.containsOnly(str2, chars3));
        assertEquals(false, StringUtils.containsOnly(str3, chars1));
        assertEquals(false, StringUtils.containsOnly(str3, chars2));
        assertEquals(true, StringUtils.containsOnly(str3, chars3));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsOnly_String
    public void testContainsOnly_String() {
        String str1 = "a";
        String str2 = "b";
        String str3 = "ab";
        String chars1= "b";
        String chars2= "a";
        String chars3= "ab";
        assertEquals(false, StringUtils.containsOnly(null, (String) null));
        assertEquals(false, StringUtils.containsOnly("", (String) null));
        assertEquals(false, StringUtils.containsOnly(null, ""));
        assertEquals(false, StringUtils.containsOnly(str1, ""));
        assertEquals(true, StringUtils.containsOnly("", ""));
        assertEquals(true, StringUtils.containsOnly("", chars1));
        assertEquals(false, StringUtils.containsOnly(str1, chars1));
        assertEquals(true, StringUtils.containsOnly(str1, chars2));
        assertEquals(true, StringUtils.containsOnly(str1, chars3));
        assertEquals(true, StringUtils.containsOnly(str2, chars1));
        assertEquals(false, StringUtils.containsOnly(str2, chars2));
        assertEquals(true, StringUtils.containsOnly(str2, chars3));
        assertEquals(false, StringUtils.containsOnly(str3, chars1));
        assertEquals(false, StringUtils.containsOnly(str3, chars2));
        assertEquals(true, StringUtils.containsOnly(str3, chars3));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testContainsWhitespace
    public void testContainsWhitespace() {
        assertFalse( StringUtils.containsWhitespace("") );
        assertTrue( StringUtils.containsWhitespace(" ") );
        assertFalse( StringUtils.containsWhitespace("a") );
        assertTrue( StringUtils.containsWhitespace("a ") );
        assertTrue( StringUtils.containsWhitespace(" a") );
        assertTrue( StringUtils.containsWhitespace("a\t") );
        assertTrue( StringUtils.containsWhitespace("\n") );
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testCustomCharSequence
    public void testCustomCharSequence() {
        assertThat((CharSequence) new CustomCharSequence(FOO), IsNot.<CharSequence>not(FOO));
        assertThat((CharSequence) FOO, IsNot.<CharSequence>not(new CustomCharSequence(FOO)));
        assertEquals(new CustomCharSequence(FOO), new CustomCharSequence(FOO));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testEquals
    public void testEquals() {
        final CharSequence fooCs = FOO, barCs = BAR, foobarCs = FOOBAR;
        assertTrue(StringUtils.equals(null, null));
        assertTrue(StringUtils.equals(fooCs, fooCs));
        assertTrue(StringUtils.equals(fooCs, (CharSequence) new StringBuilder(FOO)));
        assertTrue(StringUtils.equals(fooCs, (CharSequence) new String(new char[] { 'f', 'o', 'o' })));
        assertTrue(StringUtils.equals(fooCs, (CharSequence) new CustomCharSequence(FOO)));
        assertTrue(StringUtils.equals((CharSequence) new CustomCharSequence(FOO), fooCs));
        assertFalse(StringUtils.equals(fooCs, (CharSequence) new String(new char[] { 'f', 'O', 'O' })));
        assertFalse(StringUtils.equals(fooCs, barCs));
        assertFalse(StringUtils.equals(fooCs, null));
        assertFalse(StringUtils.equals(null, fooCs));
        assertFalse(StringUtils.equals(fooCs, foobarCs));
        assertFalse(StringUtils.equals(foobarCs, fooCs));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testEqualsOnStrings
    public void testEqualsOnStrings() {
        assertTrue(StringUtils.equals(null, null));
        assertTrue(StringUtils.equals(FOO, FOO));
        assertTrue(StringUtils.equals(FOO, new String(new char[] { 'f', 'o', 'o' })));
        assertFalse(StringUtils.equals(FOO, new String(new char[] { 'f', 'O', 'O' })));
        assertFalse(StringUtils.equals(FOO, BAR));
        assertFalse(StringUtils.equals(FOO, null));
        assertFalse(StringUtils.equals(null, FOO));
        assertFalse(StringUtils.equals(FOO, FOOBAR));
        assertFalse(StringUtils.equals(FOOBAR, FOO));
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

        assertEquals(2, StringUtils.indexOf(new StringBuilder("aabaabaa"), 'b'));
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

        assertEquals(5, StringUtils.indexOf(new StringBuilder("aabaabaa"), 'b', 3));
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

        assertEquals(2, StringUtils.indexOf(new StringBuilder("aabaabaa"), "b"));
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

        assertEquals(5, StringUtils.indexOf(new StringBuilder("aabaabaa"), "b", 3));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAny_StringCharArray
    public void testIndexOfAny_StringCharArray() {
        assertEquals(-1, StringUtils.indexOfAny(null, (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny(null, new char[0]));
        assertEquals(-1, StringUtils.indexOfAny(null, new char[] {'a','b'}));

        assertEquals(-1, StringUtils.indexOfAny("", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("", new char[0]));
        assertEquals(-1, StringUtils.indexOfAny("", new char[] {'a','b'}));

        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new char[0]));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new char[] {'z','a'}));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", new char[] {'b','y'}));
        assertEquals(-1, StringUtils.indexOfAny("ab", new char[] {'z'}));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAny_StringCharArrayWithSupplementaryChars
    public void testIndexOfAny_StringCharArrayWithSupplementaryChars() {
        assertEquals(0, StringUtils.indexOfAny(CharU20000 + CharU20001, CharU20000.toCharArray()));
        assertEquals(2, StringUtils.indexOfAny(CharU20000 + CharU20001, CharU20001.toCharArray()));
        assertEquals(0, StringUtils.indexOfAny(CharU20000, CharU20000.toCharArray()));
        assertEquals(-1, StringUtils.indexOfAny(CharU20000, CharU20001.toCharArray()));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAny_StringString
    public void testIndexOfAny_StringString() {
        assertEquals(-1, StringUtils.indexOfAny(null, (String) null));
        assertEquals(-1, StringUtils.indexOfAny(null, ""));
        assertEquals(-1, StringUtils.indexOfAny(null, "ab"));

        assertEquals(-1, StringUtils.indexOfAny("", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("", ""));
        assertEquals(-1, StringUtils.indexOfAny("", "ab"));

        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", ""));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", "by"));
        assertEquals(-1, StringUtils.indexOfAny("ab", "z"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAny_StringStringArray
    public void testIndexOfAny_StringStringArray() {
        assertEquals(-1, StringUtils.indexOfAny(null, (String[]) null));
        assertEquals(-1, StringUtils.indexOfAny(null, FOOBAR_SUB_ARRAY));
        assertEquals(-1, StringUtils.indexOfAny(FOOBAR, (String[]) null));
        assertEquals(2, StringUtils.indexOfAny(FOOBAR, FOOBAR_SUB_ARRAY));
        assertEquals(-1, StringUtils.indexOfAny(FOOBAR, new String[0]));
        assertEquals(-1, StringUtils.indexOfAny(null, new String[0]));
        assertEquals(-1, StringUtils.indexOfAny("", new String[0]));
        assertEquals(-1, StringUtils.indexOfAny(FOOBAR, new String[] {"llll"}));
        assertEquals(0, StringUtils.indexOfAny(FOOBAR, new String[] {""}));
        assertEquals(0, StringUtils.indexOfAny("", new String[] {""}));
        assertEquals(-1, StringUtils.indexOfAny("", new String[] {"a"}));
        assertEquals(-1, StringUtils.indexOfAny("", new String[] {null}));
        assertEquals(-1, StringUtils.indexOfAny(FOOBAR, new String[] {null}));
        assertEquals(-1, StringUtils.indexOfAny(null, new String[] {null}));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAny_StringStringWithSupplementaryChars
    public void testIndexOfAny_StringStringWithSupplementaryChars() {
        assertEquals(0, StringUtils.indexOfAny(CharU20000 + CharU20001, CharU20000));
        assertEquals(2, StringUtils.indexOfAny(CharU20000 + CharU20001, CharU20001));
        assertEquals(0, StringUtils.indexOfAny(CharU20000, CharU20000));
        assertEquals(-1, StringUtils.indexOfAny(CharU20000, CharU20001));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAnyBut_StringCharArray
    public void testIndexOfAnyBut_StringCharArray() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, (char[]) null));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[0]));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[] {'a','b'}));

        assertEquals(-1, StringUtils.indexOfAnyBut("", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("", new char[0]));
        assertEquals(-1, StringUtils.indexOfAnyBut("", new char[] {'a','b'}));

        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", new char[0]));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", new char[] {'z','a'}));
        assertEquals(0, StringUtils.indexOfAnyBut("zzabyycdxx", new char[] {'b','y'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", new char[] {'a', 'b'}));
        assertEquals(0, StringUtils.indexOfAnyBut("aba", new char[] {'z'}));

    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAnyBut_StringCharArrayWithSupplementaryChars
    public void testIndexOfAnyBut_StringCharArrayWithSupplementaryChars() {
        assertEquals(2, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20000.toCharArray()));
        assertEquals(0, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20001.toCharArray()));
        assertEquals(-1, StringUtils.indexOfAnyBut(CharU20000, CharU20000.toCharArray()));
        assertEquals(0, StringUtils.indexOfAnyBut(CharU20000, CharU20001.toCharArray()));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAnyBut_StringString
    public void testIndexOfAnyBut_StringString() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, (String) null));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, ""));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, "ab"));

        assertEquals(-1, StringUtils.indexOfAnyBut("", (String) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("", ""));
        assertEquals(-1, StringUtils.indexOfAnyBut("", "ab"));

        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", (String) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", ""));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(0, StringUtils.indexOfAnyBut("zzabyycdxx", "by"));
        assertEquals(0, StringUtils.indexOfAnyBut("ab", "z"));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testIndexOfAnyBut_StringStringWithSupplementaryChars
    public void testIndexOfAnyBut_StringStringWithSupplementaryChars() {
        assertEquals(2, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20000));
        assertEquals(0, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20001));
        assertEquals(-1, StringUtils.indexOfAnyBut(CharU20000, CharU20000));
        assertEquals(0, StringUtils.indexOfAnyBut(CharU20000, CharU20001));
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

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOf_char
    public void testLastIndexOf_char() {
        assertEquals(-1, StringUtils.lastIndexOf(null, ' '));
        assertEquals(-1, StringUtils.lastIndexOf("", ' '));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));

        assertEquals(5, StringUtils.lastIndexOf(new StringBuilder("aabaabaa"), 'b'));
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

        assertEquals(2, StringUtils.lastIndexOf(new StringBuilder("aabaabaa"), 'b', 2));
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

        assertEquals(4, StringUtils.lastIndexOf(new StringBuilder("aabaabaa"), "ab"));
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

        assertEquals(2, StringUtils.lastIndexOf(new StringBuilder("aabaabaa"), "b", 3));
    }

// org.apache.commons.lang3.StringUtilsEqualsIndexOfTest::testLastIndexOfAny_StringStringArray
    public void testLastIndexOfAny_StringStringArray() {
        assertEquals(-1, StringUtils.lastIndexOfAny(null, (CharSequence) null));   
        assertEquals(-1, StringUtils.lastIndexOfAny(null, (CharSequence[]) null)); 
        assertEquals(-1, StringUtils.lastIndexOfAny(null)); 
        assertEquals(-1, StringUtils.lastIndexOfAny(null, FOOBAR_SUB_ARRAY));
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR, (CharSequence) null));   
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR, (CharSequence[]) null)); 
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR)); 
        assertEquals(3, StringUtils.lastIndexOfAny(FOOBAR, FOOBAR_SUB_ARRAY));
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR, new String[0]));
        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[0]));
        assertEquals(-1, StringUtils.lastIndexOfAny("", new String[0]));
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR, new String[] {"llll"}));
        assertEquals(6, StringUtils.lastIndexOfAny(FOOBAR, new String[] {""}));
        assertEquals(0, StringUtils.lastIndexOfAny("", new String[] {""}));
        assertEquals(-1, StringUtils.lastIndexOfAny("", new String[] {"a"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("", new String[] {null}));
        assertEquals(-1, StringUtils.lastIndexOfAny(FOOBAR, new String[] {null}));
        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[] {null}));
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
