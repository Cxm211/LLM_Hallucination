// buggy code
    static float toJavaVersionInt(String version) {
        return toVersionInt(toJavaVersionIntArray(version, JAVA_VERSION_TRIM_SIZE));
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
        
        Object[] original = new Object[] {true, false};
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
        Class<?>[] arrayPrimitives = { Integer.TYPE, Boolean.TYPE };
        Class<?>[] arrayWrappers = { Integer.class, Boolean.class };

        assertFalse(ClassUtils.isAssignable(array1, array2));
        assertFalse(ClassUtils.isAssignable(null, array2));
        assertTrue(ClassUtils.isAssignable(null, array0));
        assertTrue(ClassUtils.isAssignable(array0, array0));
        assertTrue(ClassUtils.isAssignable(array0, null));
        assertTrue(ClassUtils.isAssignable((Class[]) null, (Class[]) null));

        assertFalse(ClassUtils.isAssignable(array1, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1));

        boolean autoboxing = SystemUtils.isJavaVersionAtLeast(1.5f);

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

        boolean autoboxing = SystemUtils.isJavaVersionAtLeast(1.5f);

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
        boolean autoboxing = SystemUtils.isJavaVersionAtLeast(1.5f);

        
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
        assertNull(ClassUtils.toClass(null));

        assertSame(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass(ArrayUtils.EMPTY_OBJECT_ARRAY));

        assertTrue(Arrays.equals(new Class[] { String.class, Integer.class, Double.class },
                ClassUtils.toClass(new Object[] { "Test", 1, 99d })));

        assertTrue(Arrays.equals(new Class[] { String.class, null, Double.class },
                ClassUtils.toClass(new Object[] { "Test", null, 99d })));
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

// org.apache.commons.lang3.LocaleUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new LocaleUtils());
        Constructor<?>[] cons = LocaleUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(LocaleUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(LocaleUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.LocaleUtilsTest::testToLocale_1Part
    public void testToLocale_1Part() {
        assertEquals(null, LocaleUtils.toLocale((String) null));
        
        assertValidToLocale("us");
        assertValidToLocale("fr");
        assertValidToLocale("de");
        assertValidToLocale("zh");
        
        assertValidToLocale("qq");
        
        try {
            LocaleUtils.toLocale("Us");
            fail("Should fail if not lowercase");
        } catch (IllegalArgumentException iae) {}
        try {
            LocaleUtils.toLocale("US");
            fail("Should fail if not lowercase");
        } catch (IllegalArgumentException iae) {}
        try {
            LocaleUtils.toLocale("uS");
            fail("Should fail if not lowercase");
        } catch (IllegalArgumentException iae) {}
        try {
            LocaleUtils.toLocale("u#");
            fail("Should fail if not lowercase");
        } catch (IllegalArgumentException iae) {}
        
        try {
            LocaleUtils.toLocale("u");
            fail("Must be 2 chars if less than 5");
        } catch (IllegalArgumentException iae) {}
       
        try {
            LocaleUtils.toLocale("uuu");
            fail("Must be 2 chars if less than 5");
        } catch (IllegalArgumentException iae) {}

        try {
            LocaleUtils.toLocale("uu_U");
            fail("Must be 2 chars if less than 5");
        } catch (IllegalArgumentException iae) {}
    }

// org.apache.commons.lang3.LocaleUtilsTest::testToLocale_2Part
    public void testToLocale_2Part() {
        assertValidToLocale("us_EN", "us", "EN");
        
        assertValidToLocale("us_ZH", "us", "ZH");
        
        try {
            LocaleUtils.toLocale("us-EN");
            fail("Should fail as not underscore");
        } catch (IllegalArgumentException iae) {}
        try {
            LocaleUtils.toLocale("us_En");
            fail("Should fail second part not uppercase");
        } catch (IllegalArgumentException iae) {}
        try {
            LocaleUtils.toLocale("us_en");
            fail("Should fail second part not uppercase");
        } catch (IllegalArgumentException iae) {}
        try {
            LocaleUtils.toLocale("us_eN");
            fail("Should fail second part not uppercase");
        } catch (IllegalArgumentException iae) {}
        try {
            LocaleUtils.toLocale("uS_EN");
            fail("Should fail first part not lowercase");
        } catch (IllegalArgumentException iae) {}
        try {
            LocaleUtils.toLocale("us_E3");
            fail("Should fail second part not uppercase");
        } catch (IllegalArgumentException iae) {}
    }

// org.apache.commons.lang3.LocaleUtilsTest::testToLocale_3Part
    public void testToLocale_3Part() {
        assertValidToLocale("us_EN_A", "us", "EN", "A");
        
        
        if (SystemUtils.isJavaVersionAtLeast(1.4f)) {
            assertValidToLocale("us_EN_a", "us", "EN", "a");
            assertValidToLocale("us_EN_SFsafdFDsdfF", "us", "EN", "SFsafdFDsdfF");
        } else {
            assertValidToLocale("us_EN_a", "us", "EN", "A");
            assertValidToLocale("us_EN_SFsafdFDsdfF", "us", "EN", "SFSAFDFDSDFF");
        }
        
        try {
            LocaleUtils.toLocale("us_EN-a");
            fail("Should fail as not underscore");
        } catch (IllegalArgumentException iae) {}
        try {
            LocaleUtils.toLocale("uu_UU_");
            fail("Must be 3, 5 or 7+ in length");
        } catch (IllegalArgumentException iae) {}
    }

// org.apache.commons.lang3.LocaleUtilsTest::testLocaleLookupList_Locale
    public void testLocaleLookupList_Locale() {
        assertLocaleLookupList(null, null, new Locale[0]);
        assertLocaleLookupList(LOCALE_QQ, null, new Locale[]{LOCALE_QQ});
        assertLocaleLookupList(LOCALE_EN, null, new Locale[]{LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN, null, new Locale[]{LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN_US, null,
            new Locale[] {
                LOCALE_EN_US,
                LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN_US_ZZZZ, null,
            new Locale[] {
                LOCALE_EN_US_ZZZZ,
                LOCALE_EN_US,
                LOCALE_EN});
    }

// org.apache.commons.lang3.LocaleUtilsTest::testLocaleLookupList_LocaleLocale
    public void testLocaleLookupList_LocaleLocale() {
        assertLocaleLookupList(LOCALE_QQ, LOCALE_QQ, 
                new Locale[]{LOCALE_QQ});
        assertLocaleLookupList(LOCALE_EN, LOCALE_EN, 
                new Locale[]{LOCALE_EN});
        
        assertLocaleLookupList(LOCALE_EN_US, LOCALE_EN_US, 
            new Locale[]{
                LOCALE_EN_US,
                LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN_US, LOCALE_QQ,
            new Locale[] {
                LOCALE_EN_US,
                LOCALE_EN,
                LOCALE_QQ});
        assertLocaleLookupList(LOCALE_EN_US, LOCALE_QQ_ZZ,
            new Locale[] {
                LOCALE_EN_US,
                LOCALE_EN,
                LOCALE_QQ_ZZ});
        
        assertLocaleLookupList(LOCALE_EN_US_ZZZZ, null,
            new Locale[] {
                LOCALE_EN_US_ZZZZ,
                LOCALE_EN_US,
                LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN_US_ZZZZ, LOCALE_EN_US_ZZZZ,
            new Locale[] {
                LOCALE_EN_US_ZZZZ,
                LOCALE_EN_US,
                LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN_US_ZZZZ, LOCALE_QQ,
            new Locale[] {
                LOCALE_EN_US_ZZZZ,
                LOCALE_EN_US,
                LOCALE_EN,
                LOCALE_QQ});
        assertLocaleLookupList(LOCALE_EN_US_ZZZZ, LOCALE_QQ_ZZ,
            new Locale[] {
                LOCALE_EN_US_ZZZZ,
                LOCALE_EN_US,
                LOCALE_EN,
                LOCALE_QQ_ZZ});
        assertLocaleLookupList(LOCALE_FR_CA, LOCALE_EN,
            new Locale[] {
                LOCALE_FR_CA,
                LOCALE_FR,
                LOCALE_EN});
    }

// org.apache.commons.lang3.LocaleUtilsTest::testAvailableLocaleList
    public void testAvailableLocaleList() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        List<Locale> list2 = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertSame(list, list2);
        assertUnmodifiableCollection(list);
        
        Locale[] jdkLocaleArray = Locale.getAvailableLocales();
        List<Locale> jdkLocaleList = Arrays.asList(jdkLocaleArray);
        assertEquals(jdkLocaleList, list);
    }

// org.apache.commons.lang3.LocaleUtilsTest::testAvailableLocaleSet
    public void testAvailableLocaleSet() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        Set<Locale> set2 = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertSame(set, set2);
        assertUnmodifiableCollection(set);
        
        Locale[] jdkLocaleArray = Locale.getAvailableLocales();
        List<Locale> jdkLocaleList = Arrays.asList(jdkLocaleArray);
        Set<Locale> jdkLocaleSet = new HashSet<Locale>(jdkLocaleList);
        assertEquals(jdkLocaleSet, set);
    }

// org.apache.commons.lang3.LocaleUtilsTest::testIsAvailableLocale
    public void testIsAvailableLocale() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        assertEquals(set.contains(LOCALE_EN), LocaleUtils.isAvailableLocale(LOCALE_EN));
        assertEquals(set.contains(LOCALE_EN_US), LocaleUtils.isAvailableLocale(LOCALE_EN_US));
        assertEquals(set.contains(LOCALE_EN_US_ZZZZ), LocaleUtils.isAvailableLocale(LOCALE_EN_US_ZZZZ));
        assertEquals(set.contains(LOCALE_FR), LocaleUtils.isAvailableLocale(LOCALE_FR));
        assertEquals(set.contains(LOCALE_FR_CA), LocaleUtils.isAvailableLocale(LOCALE_FR_CA));
        assertEquals(set.contains(LOCALE_QQ), LocaleUtils.isAvailableLocale(LOCALE_QQ));
        assertEquals(set.contains(LOCALE_QQ_ZZ), LocaleUtils.isAvailableLocale(LOCALE_QQ_ZZ));
    }

// org.apache.commons.lang3.LocaleUtilsTest::testLanguagesByCountry
    public void testLanguagesByCountry() {
        assertLanguageByCountry(null, new String[0]);
        assertLanguageByCountry("GB", new String[]{"en"});
        assertLanguageByCountry("ZZ", new String[0]);
        assertLanguageByCountry("CH", new String[]{"fr", "de", "it"});
    }

// org.apache.commons.lang3.LocaleUtilsTest::testCountriesByLanguage
    public void testCountriesByLanguage() {
        assertCountriesByLanguage(null, new String[0]);
        assertCountriesByLanguage("de", new String[]{"DE", "CH", "AT", "LU"});
        assertCountriesByLanguage("zz", new String[0]);
        assertCountriesByLanguage("it", new String[]{"IT", "CH"});
    }

// org.apache.commons.lang3.LocaleUtilsTest::testLang328
    public void testLang328() {
        assertValidToLocale("fr__POSIX", "fr", "", "POSIX");
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testIsEmpty
    public void testIsEmpty() {
        assertEquals(true, StringUtils.isEmpty(null));
        assertEquals(true, StringUtils.isEmpty(""));
        assertEquals(false, StringUtils.isEmpty(" "));
        assertEquals(false, StringUtils.isEmpty("foo"));
        assertEquals(false, StringUtils.isEmpty("  foo  "));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testIsNotEmpty
    public void testIsNotEmpty() {
        assertEquals(false, StringUtils.isNotEmpty(null));
        assertEquals(false, StringUtils.isNotEmpty(""));
        assertEquals(true, StringUtils.isNotEmpty(" "));
        assertEquals(true, StringUtils.isNotEmpty("foo"));
        assertEquals(true, StringUtils.isNotEmpty("  foo  "));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testIsBlank
    public void testIsBlank() {
        assertEquals(true, StringUtils.isBlank(null));
        assertEquals(true, StringUtils.isBlank(""));
        assertEquals(true, StringUtils.isBlank(StringUtilsTest.WHITESPACE));
        assertEquals(false, StringUtils.isBlank("foo"));
        assertEquals(false, StringUtils.isBlank("  foo  "));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testIsNotBlank
    public void testIsNotBlank() {
        assertEquals(false, StringUtils.isNotBlank(null));
        assertEquals(false, StringUtils.isNotBlank(""));
        assertEquals(false, StringUtils.isNotBlank(StringUtilsTest.WHITESPACE));
        assertEquals(true, StringUtils.isNotBlank("foo"));
        assertEquals(true, StringUtils.isNotBlank("  foo  "));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testTrim
    public void testTrim() {
        assertEquals(FOO, StringUtils.trim(FOO + "  "));
        assertEquals(FOO, StringUtils.trim(" " + FOO + "  "));
        assertEquals(FOO, StringUtils.trim(" " + FOO));
        assertEquals(FOO, StringUtils.trim(FOO + ""));
        assertEquals("", StringUtils.trim(" \t\r\n\b "));
        assertEquals("", StringUtils.trim(StringUtilsTest.TRIMMABLE));
        assertEquals(StringUtilsTest.NON_TRIMMABLE, StringUtils.trim(StringUtilsTest.NON_TRIMMABLE));
        assertEquals("", StringUtils.trim(""));
        assertEquals(null, StringUtils.trim(null));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testTrimToNull
    public void testTrimToNull() {
        assertEquals(FOO, StringUtils.trimToNull(FOO + "  "));
        assertEquals(FOO, StringUtils.trimToNull(" " + FOO + "  "));
        assertEquals(FOO, StringUtils.trimToNull(" " + FOO));
        assertEquals(FOO, StringUtils.trimToNull(FOO + ""));
        assertEquals(null, StringUtils.trimToNull(" \t\r\n\b "));
        assertEquals(null, StringUtils.trimToNull(StringUtilsTest.TRIMMABLE));
        assertEquals(StringUtilsTest.NON_TRIMMABLE, StringUtils.trimToNull(StringUtilsTest.NON_TRIMMABLE));
        assertEquals(null, StringUtils.trimToNull(""));
        assertEquals(null, StringUtils.trimToNull(null));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testTrimToEmpty
    public void testTrimToEmpty() {
        assertEquals(FOO, StringUtils.trimToEmpty(FOO + "  "));
        assertEquals(FOO, StringUtils.trimToEmpty(" " + FOO + "  "));
        assertEquals(FOO, StringUtils.trimToEmpty(" " + FOO));
        assertEquals(FOO, StringUtils.trimToEmpty(FOO + ""));
        assertEquals("", StringUtils.trimToEmpty(" \t\r\n\b "));
        assertEquals("", StringUtils.trimToEmpty(StringUtilsTest.TRIMMABLE));
        assertEquals(StringUtilsTest.NON_TRIMMABLE, StringUtils.trimToEmpty(StringUtilsTest.NON_TRIMMABLE));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty(null));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStrip_String
    public void testStrip_String() {
        assertEquals(null, StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("", StringUtils.strip("        "));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals(StringUtilsTest.NON_WHITESPACE, 
            StringUtils.strip(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripToNull_String
    public void testStripToNull_String() {
        assertEquals(null, StringUtils.stripToNull(null));
        assertEquals(null, StringUtils.stripToNull(""));
        assertEquals(null, StringUtils.stripToNull("        "));
        assertEquals(null, StringUtils.stripToNull(StringUtilsTest.WHITESPACE));
        assertEquals("ab c", StringUtils.stripToNull("  ab c  "));
        assertEquals(StringUtilsTest.NON_WHITESPACE, 
            StringUtils.stripToNull(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripToEmpty_String
    public void testStripToEmpty_String() {
        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty(""));
        assertEquals("", StringUtils.stripToEmpty("        "));
        assertEquals("", StringUtils.stripToEmpty(StringUtilsTest.WHITESPACE));
        assertEquals("ab c", StringUtils.stripToEmpty("  ab c  "));
        assertEquals(StringUtilsTest.NON_WHITESPACE, 
            StringUtils.stripToEmpty(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStrip_StringString
    public void testStrip_StringString() {
        
        assertEquals(null, StringUtils.strip(null, null));
        assertEquals("", StringUtils.strip("", null));
        assertEquals("", StringUtils.strip("        ", null));
        assertEquals("abc", StringUtils.strip("  abc  ", null));
        assertEquals(StringUtilsTest.NON_WHITESPACE, 
            StringUtils.strip(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE, null));

        
        assertEquals(null, StringUtils.strip(null, ""));
        assertEquals("", StringUtils.strip("", ""));
        assertEquals("        ", StringUtils.strip("        ", ""));
        assertEquals("  abc  ", StringUtils.strip("  abc  ", ""));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.strip(StringUtilsTest.WHITESPACE, ""));
        
        
        assertEquals(null, StringUtils.strip(null, " "));
        assertEquals("", StringUtils.strip("", " "));
        assertEquals("", StringUtils.strip("        ", " "));
        assertEquals("abc", StringUtils.strip("  abc  ", " "));
        
        
        assertEquals(null, StringUtils.strip(null, "ab"));
        assertEquals("", StringUtils.strip("", "ab"));
        assertEquals("        ", StringUtils.strip("        ", "ab"));
        assertEquals("  abc  ", StringUtils.strip("  abc  ", "ab"));
        assertEquals("c", StringUtils.strip("abcabab", "ab"));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.strip(StringUtilsTest.WHITESPACE, ""));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripStart_StringString
    public void testStripStart_StringString() {
        
        assertEquals(null, StringUtils.stripStart(null, null));
        assertEquals("", StringUtils.stripStart("", null));
        assertEquals("", StringUtils.stripStart("        ", null));
        assertEquals("abc  ", StringUtils.stripStart("  abc  ", null));
        assertEquals(StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE, 
            StringUtils.stripStart(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE, null));

        
        assertEquals(null, StringUtils.stripStart(null, ""));
        assertEquals("", StringUtils.stripStart("", ""));
        assertEquals("        ", StringUtils.stripStart("        ", ""));
        assertEquals("  abc  ", StringUtils.stripStart("  abc  ", ""));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.stripStart(StringUtilsTest.WHITESPACE, ""));
        
        
        assertEquals(null, StringUtils.stripStart(null, " "));
        assertEquals("", StringUtils.stripStart("", " "));
        assertEquals("", StringUtils.stripStart("        ", " "));
        assertEquals("abc  ", StringUtils.stripStart("  abc  ", " "));
        
        
        assertEquals(null, StringUtils.stripStart(null, "ab"));
        assertEquals("", StringUtils.stripStart("", "ab"));
        assertEquals("        ", StringUtils.stripStart("        ", "ab"));
        assertEquals("  abc  ", StringUtils.stripStart("  abc  ", "ab"));
        assertEquals("cabab", StringUtils.stripStart("abcabab", "ab"));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.stripStart(StringUtilsTest.WHITESPACE, ""));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripEnd_StringString
    public void testStripEnd_StringString() {
        
        assertEquals(null, StringUtils.stripEnd(null, null));
        assertEquals("", StringUtils.stripEnd("", null));
        assertEquals("", StringUtils.stripEnd("        ", null));
        assertEquals("  abc", StringUtils.stripEnd("  abc  ", null));
        assertEquals(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE, 
            StringUtils.stripEnd(StringUtilsTest.WHITESPACE + StringUtilsTest.NON_WHITESPACE + StringUtilsTest.WHITESPACE, null));

        
        assertEquals(null, StringUtils.stripEnd(null, ""));
        assertEquals("", StringUtils.stripEnd("", ""));
        assertEquals("        ", StringUtils.stripEnd("        ", ""));
        assertEquals("  abc  ", StringUtils.stripEnd("  abc  ", ""));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.stripEnd(StringUtilsTest.WHITESPACE, ""));
        
        
        assertEquals(null, StringUtils.stripEnd(null, " "));
        assertEquals("", StringUtils.stripEnd("", " "));
        assertEquals("", StringUtils.stripEnd("        ", " "));
        assertEquals("  abc", StringUtils.stripEnd("  abc  ", " "));
        
        
        assertEquals(null, StringUtils.stripEnd(null, "ab"));
        assertEquals("", StringUtils.stripEnd("", "ab"));
        assertEquals("        ", StringUtils.stripEnd("        ", "ab"));
        assertEquals("  abc  ", StringUtils.stripEnd("  abc  ", "ab"));
        assertEquals("abc", StringUtils.stripEnd("abcabab", "ab"));
        assertEquals(StringUtilsTest.WHITESPACE, StringUtils.stripEnd(StringUtilsTest.WHITESPACE, ""));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripAll
    public void testStripAll() {
        
        String[] empty = new String[0];
        String[] fooSpace = new String[] { "  "+FOO+"  ", "  "+FOO, FOO+"  " };
        String[] fooDots = new String[] { ".."+FOO+"..", ".."+FOO, FOO+".." };
        String[] foo = new String[] { FOO, FOO, FOO };

        assertEquals(null, StringUtils.stripAll(null));
        assertArrayEquals(empty, StringUtils.stripAll(empty));
        assertArrayEquals(foo, StringUtils.stripAll(fooSpace));
        
        assertEquals(null, StringUtils.stripAll(null, null));
        assertArrayEquals(foo, StringUtils.stripAll(fooSpace, null));
        assertArrayEquals(foo, StringUtils.stripAll(fooDots, "."));
    }

// org.apache.commons.lang3.StringUtilsTrimEmptyTest::testStripAccents
    public void testStripAccents() {
        if(SystemUtils.isJavaVersionAtLeast(1.6f)) {
            String cue = "\u00C7\u00FA\u00EA";
            assertEquals( "Failed to strip accents from " + cue, "Cue", StringUtils.stripAccents(cue));

            String lots = "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C7\u00C8\u00C9" + 
                          "\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D1\u00D2\u00D3" + 
                          "\u00D4\u00D5\u00D6\u00D9\u00DA\u00DB\u00DC\u00DD";
            assertEquals( "Failed to strip accents from " + lots, 
                          "AAAAAACEEEEIIIINOOOOOUUUUY", 
                          StringUtils.stripAccents(lots));

            assertNull( "Failed null safety", StringUtils.stripAccents(null) );
            assertEquals( "Failed empty String", "", StringUtils.stripAccents("") );
            assertEquals( "Failed to handle non-accented text", "control", StringUtils.stripAccents("control") );
            assertEquals( "Failed to handle easy example", "eclair", StringUtils.stripAccents("\u00E9clair") );
        } else {
            try {
                StringUtils.stripAccents("string");
                fail("Before JDK 1.6, stripAccents is not expected to work");
            } catch(UnsupportedOperationException uoe) {
                assertEquals("The stripAccents(String) method is not supported until Java 1.6", uoe.getMessage());
            }
        }
    }

// org.apache.commons.lang3.SystemUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new SystemUtils());
        Constructor<?>[] cons = SystemUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(SystemUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(SystemUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.SystemUtilsTest::testGetJavaHome
    public void testGetJavaHome() {
        File dir = SystemUtils.getJavaHome();
        Assert.assertNotNull(dir);
        Assert.assertTrue(dir.exists());
    }

// org.apache.commons.lang3.SystemUtilsTest::testGetJavaIoTmpDir
    public void testGetJavaIoTmpDir() {
        File dir = SystemUtils.getJavaIoTmpDir();
        Assert.assertNotNull(dir);
        Assert.assertTrue(dir.exists());
    }

// org.apache.commons.lang3.SystemUtilsTest::testGetUserDir
    public void testGetUserDir() {
        File dir = SystemUtils.getUserDir();
        Assert.assertNotNull(dir);
        Assert.assertTrue(dir.exists());
    }

// org.apache.commons.lang3.SystemUtilsTest::testGetUserHome
    public void testGetUserHome() {
        File dir = SystemUtils.getUserHome();
        Assert.assertNotNull(dir);
        Assert.assertTrue(dir.exists());
    }

// org.apache.commons.lang3.SystemUtilsTest::testIS_JAVA
    public void testIS_JAVA() {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion == null) {
            assertEquals(false, SystemUtils.IS_JAVA_1_1);
            assertEquals(false, SystemUtils.IS_JAVA_1_2);
            assertEquals(false, SystemUtils.IS_JAVA_1_3);
            assertEquals(false, SystemUtils.IS_JAVA_1_4);
            assertEquals(false, SystemUtils.IS_JAVA_1_5);
            assertEquals(false, SystemUtils.IS_JAVA_1_6);
            assertEquals(false, SystemUtils.IS_JAVA_1_7);
        } else if (javaVersion.startsWith("1.1")) {
            assertEquals(true, SystemUtils.IS_JAVA_1_1);
            assertEquals(false, SystemUtils.IS_JAVA_1_2);
            assertEquals(false, SystemUtils.IS_JAVA_1_3);
            assertEquals(false, SystemUtils.IS_JAVA_1_4);
            assertEquals(false, SystemUtils.IS_JAVA_1_5);
            assertEquals(false, SystemUtils.IS_JAVA_1_6);
            assertEquals(false, SystemUtils.IS_JAVA_1_7);
        } else if (javaVersion.startsWith("1.2")) {
            assertEquals(false, SystemUtils.IS_JAVA_1_1);
            assertEquals(true, SystemUtils.IS_JAVA_1_2);
            assertEquals(false, SystemUtils.IS_JAVA_1_3);
            assertEquals(false, SystemUtils.IS_JAVA_1_4);
            assertEquals(false, SystemUtils.IS_JAVA_1_5);
            assertEquals(false, SystemUtils.IS_JAVA_1_6);
            assertEquals(false, SystemUtils.IS_JAVA_1_7);
        } else if (javaVersion.startsWith("1.3")) {
            assertEquals(false, SystemUtils.IS_JAVA_1_1);
            assertEquals(false, SystemUtils.IS_JAVA_1_2);
            assertEquals(true, SystemUtils.IS_JAVA_1_3);
            assertEquals(false, SystemUtils.IS_JAVA_1_4);
            assertEquals(false, SystemUtils.IS_JAVA_1_5);
            assertEquals(false, SystemUtils.IS_JAVA_1_6);
            assertEquals(false, SystemUtils.IS_JAVA_1_7);
        } else if (javaVersion.startsWith("1.4")) {
            assertEquals(false, SystemUtils.IS_JAVA_1_1);
            assertEquals(false, SystemUtils.IS_JAVA_1_2);
            assertEquals(false, SystemUtils.IS_JAVA_1_3);
            assertEquals(true, SystemUtils.IS_JAVA_1_4);
            assertEquals(false, SystemUtils.IS_JAVA_1_5);
            assertEquals(false, SystemUtils.IS_JAVA_1_6);
            assertEquals(false, SystemUtils.IS_JAVA_1_7);
        } else if (javaVersion.startsWith("1.5")) {
            assertEquals(false, SystemUtils.IS_JAVA_1_1);
            assertEquals(false, SystemUtils.IS_JAVA_1_2);
            assertEquals(false, SystemUtils.IS_JAVA_1_3);
            assertEquals(false, SystemUtils.IS_JAVA_1_4);
            assertEquals(true, SystemUtils.IS_JAVA_1_5);
            assertEquals(false, SystemUtils.IS_JAVA_1_6);
            assertEquals(false, SystemUtils.IS_JAVA_1_7);
        } else if (javaVersion.startsWith("1.6")) {
            assertEquals(false, SystemUtils.IS_JAVA_1_1);
            assertEquals(false, SystemUtils.IS_JAVA_1_2);
            assertEquals(false, SystemUtils.IS_JAVA_1_3);
            assertEquals(false, SystemUtils.IS_JAVA_1_4);
            assertEquals(false, SystemUtils.IS_JAVA_1_5);
            assertEquals(true, SystemUtils.IS_JAVA_1_6);
            assertEquals(false, SystemUtils.IS_JAVA_1_7);
        } else {
            System.out.println("Can't test IS_JAVA value");
        }
    }

// org.apache.commons.lang3.SystemUtilsTest::testIS_OS
    public void testIS_OS() {
        String osName = System.getProperty("os.name");
        if (osName == null) {
            assertEquals(false, SystemUtils.IS_OS_WINDOWS);
            assertEquals(false, SystemUtils.IS_OS_UNIX);
            assertEquals(false, SystemUtils.IS_OS_SOLARIS);
            assertEquals(false, SystemUtils.IS_OS_LINUX);
            assertEquals(false, SystemUtils.IS_OS_MAC_OSX);
        } else if (osName.startsWith("Windows")) {
            assertEquals(false, SystemUtils.IS_OS_UNIX);
            assertEquals(true, SystemUtils.IS_OS_WINDOWS);
        } else if (osName.startsWith("Solaris")) {
            assertEquals(true, SystemUtils.IS_OS_SOLARIS);
            assertEquals(true, SystemUtils.IS_OS_UNIX);
            assertEquals(false, SystemUtils.IS_OS_WINDOWS);
        } else if (osName.toLowerCase(Locale.ENGLISH).startsWith("linux")) {
            assertEquals(true, SystemUtils.IS_OS_LINUX);
            assertEquals(true, SystemUtils.IS_OS_UNIX);
            assertEquals(false, SystemUtils.IS_OS_WINDOWS);
        } else if (osName.startsWith("Mac OS X")) {
            assertEquals(true, SystemUtils.IS_OS_MAC_OSX);
            assertEquals(true, SystemUtils.IS_OS_UNIX);
            assertEquals(false, SystemUtils.IS_OS_WINDOWS);
        } else if (osName.startsWith("OS/2")) {
            assertEquals(true, SystemUtils.IS_OS_OS2);
            assertEquals(false, SystemUtils.IS_OS_UNIX);
            assertEquals(false, SystemUtils.IS_OS_WINDOWS);
        } else if (osName.startsWith("SunOS")) {
            assertEquals(true, SystemUtils.IS_OS_SUN_OS);
            assertEquals(true, SystemUtils.IS_OS_UNIX);
            assertEquals(false, SystemUtils.IS_OS_WINDOWS);
        } else {
            System.out.println("Can't test IS_OS value");
        }
    }

// org.apache.commons.lang3.SystemUtilsTest::testJavaVersionAsFloat
    public void testJavaVersionAsFloat() {
        assertEquals(0f, SystemUtils.toJavaVersionFloat(null), 0.000001f);
        assertEquals(0f, SystemUtils.toJavaVersionFloat(""), 0.000001f);
        assertEquals(0f, SystemUtils.toJavaVersionFloat("0"), 0.000001f);
        assertEquals(1.1f, SystemUtils.toJavaVersionFloat("1.1"), 0.000001f);
        assertEquals(1.2f, SystemUtils.toJavaVersionFloat("1.2"), 0.000001f);
        assertEquals(1.3f, SystemUtils.toJavaVersionFloat("1.3.0"), 0.000001f);
        assertEquals(1.31f, SystemUtils.toJavaVersionFloat("1.3.1"), 0.000001f);
        assertEquals(1.4f, SystemUtils.toJavaVersionFloat("1.4.0"), 0.000001f);
        assertEquals(1.41f, SystemUtils.toJavaVersionFloat("1.4.1"), 0.000001f);
        assertEquals(1.42f, SystemUtils.toJavaVersionFloat("1.4.2"), 0.000001f);
        assertEquals(1.5f, SystemUtils.toJavaVersionFloat("1.5.0"), 0.000001f);
        assertEquals(1.6f, SystemUtils.toJavaVersionFloat("1.6.0"), 0.000001f);
        assertEquals(1.31f, SystemUtils.toJavaVersionFloat("JavaVM-1.3.1"), 0.000001f);
        assertEquals(1.3f, SystemUtils.toJavaVersionFloat("1.3.0 subset"), 0.000001f);
        
        assertEquals(1.3f, SystemUtils.toJavaVersionFloat("XXX-1.3.x"), 0.000001f);
    }

// org.apache.commons.lang3.SystemUtilsTest::testJavaVersionAsInt
    public void testJavaVersionAsInt() {
        assertEquals(0, SystemUtils.toJavaVersionInt(null));
        assertEquals(0, SystemUtils.toJavaVersionInt(""));
        assertEquals(0, SystemUtils.toJavaVersionInt("0"));
        assertEquals(110, SystemUtils.toJavaVersionInt("1.1"));
        assertEquals(120, SystemUtils.toJavaVersionInt("1.2"));
        assertEquals(130, SystemUtils.toJavaVersionInt("1.3.0"));
        assertEquals(131, SystemUtils.toJavaVersionInt("1.3.1"));
        assertEquals(140, SystemUtils.toJavaVersionInt("1.4.0"));
        assertEquals(141, SystemUtils.toJavaVersionInt("1.4.1"));
        assertEquals(142, SystemUtils.toJavaVersionInt("1.4.2"));
        assertEquals(150, SystemUtils.toJavaVersionInt("1.5.0"));
        assertEquals(160, SystemUtils.toJavaVersionInt("1.6.0"));
        assertEquals(131, SystemUtils.toJavaVersionInt("JavaVM-1.3.1"));
        assertEquals(131, SystemUtils.toJavaVersionInt("1.3.1 subset"));
        
        assertEquals(130, SystemUtils.toJavaVersionInt("XXX-1.3.x"));
    }

// org.apache.commons.lang3.SystemUtilsTest::testJavaVersionAtLeastFloat
    public void testJavaVersionAtLeastFloat() {
        float version = SystemUtils.JAVA_VERSION_FLOAT;
        assertEquals(true, SystemUtils.isJavaVersionAtLeast(version));
        version -= 0.1f;
        assertEquals(true, SystemUtils.isJavaVersionAtLeast(version));
        version += 0.2f;
        assertEquals(false, SystemUtils.isJavaVersionAtLeast(version));
    }

// org.apache.commons.lang3.SystemUtilsTest::testJavaVersionAtLeastInt
    public void testJavaVersionAtLeastInt() {
        int version = SystemUtils.JAVA_VERSION_INT;
        assertEquals(true, SystemUtils.isJavaVersionAtLeast(version));
        version -= 10;
        assertEquals(true, SystemUtils.isJavaVersionAtLeast(version));
        version += 20;
        assertEquals(false, SystemUtils.isJavaVersionAtLeast(version));
    }

// org.apache.commons.lang3.SystemUtilsTest::testJavaVersionMatches
    public void testJavaVersionMatches() {
        String javaVersion = null;
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.0";
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.1";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.2";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.3.0";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.3.1";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.4.0";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.4.1";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.4.2";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.5.0";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.6.0";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
        javaVersion = "1.7.0";
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.0"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.1"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.2"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.3"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.4"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.5"));
        assertEquals(false, SystemUtils.isJavaVersionMatch(javaVersion, "1.6"));
        assertEquals(true, SystemUtils.isJavaVersionMatch(javaVersion, "1.7"));
    }

// org.apache.commons.lang3.SystemUtilsTest::testOSMatchesName
    public void testOSMatchesName() {
        String osName = null;
        assertEquals(false, SystemUtils.isOSNameMatch(osName, "Windows"));
        osName = "";
        assertEquals(false, SystemUtils.isOSNameMatch(osName, "Windows"));
        osName = "Windows 95";
        assertEquals(true, SystemUtils.isOSNameMatch(osName, "Windows"));
        osName = "Windows NT";
        assertEquals(true, SystemUtils.isOSNameMatch(osName, "Windows"));
        osName = "OS/2";
        assertEquals(false, SystemUtils.isOSNameMatch(osName, "Windows"));
    }

// org.apache.commons.lang3.SystemUtilsTest::testOSMatchesNameAndVersion
    public void testOSMatchesNameAndVersion() {
        String osName = null;
        String osVersion = null;
        assertEquals(false, SystemUtils.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
        osName = "";
        osVersion = "";
        assertEquals(false, SystemUtils.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
        osName = "Windows 95";
        osVersion = "4.0";
        assertEquals(false, SystemUtils.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
        osName = "Windows 95";
        osVersion = "4.1";
        assertEquals(true, SystemUtils.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
        osName = "Windows 98";
        osVersion = "4.1";
        assertEquals(true, SystemUtils.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
        osName = "Windows NT";
        osVersion = "4.0";
        assertEquals(false, SystemUtils.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
        osName = "OS/2";
        osVersion = "4.0";
        assertEquals(false, SystemUtils.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
    }

// org.apache.commons.lang3.SystemUtilsTest::testJavaAwtHeadless
    public void testJavaAwtHeadless() {
        boolean atLeastJava14 = SystemUtils.isJavaVersionAtLeast(140);
        String expectedStringValue = System.getProperty("java.awt.headless");
        String expectedStringValueWithDefault = System.getProperty("java.awt.headless", "false");
        assertNotNull(expectedStringValueWithDefault);
        if (atLeastJava14) {
            boolean expectedValue = Boolean.valueOf(expectedStringValue).booleanValue();
            if (expectedStringValue != null) {
                assertEquals(expectedStringValue, SystemUtils.JAVA_AWT_HEADLESS);
            }
            assertEquals(expectedValue, SystemUtils.isJavaAwtHeadless());
        } else {
            assertNull(expectedStringValue);
            assertNull(SystemUtils.JAVA_AWT_HEADLESS);
            assertEquals(expectedStringValueWithDefault, "" + SystemUtils.isJavaAwtHeadless());
        }
        assertEquals(expectedStringValueWithDefault, "" + SystemUtils.isJavaAwtHeadless());
    }

// org.apache.commons.lang3.builder.DefaultToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang3.builder.DefaultToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang3.builder.DefaultToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang3.builder.DefaultToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "John Doe";
        p.age = 33;
        p.smoker = false;
        String pBaseStr = p.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(p));
        assertEquals(pBaseStr + "[name=John Doe,age=33,smoker=false]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang3.builder.DefaultToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang3.builder.DefaultToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.DefaultToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.DefaultToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.MultiLineToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang3.builder.MultiLineToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "]").toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]").toString());
        
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=hello" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "]").append("a", "hello").toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "  a=hello" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]").append("a", "hello").toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=hello" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang3.builder.MultiLineToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "  b=4" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<Integer>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<size=0>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=[]" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), true).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<size=0>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a={}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), true).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<size=0>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a={}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang3.builder.MultiLineToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "Jane Doe";
        p.age = 25;
        p.smoker = true;
        String pBaseStr = p.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(p));
        assertEquals(pBaseStr + "[" + SystemUtils.LINE_SEPARATOR + "  name=Jane Doe" + SystemUtils.LINE_SEPARATOR + "  age=25" + SystemUtils.LINE_SEPARATOR + "  smoker=true" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang3.builder.MultiLineToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "  b=4" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang3.builder.MultiLineToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {<null>,5,{3,6}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {<null>,5,{3,6}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.MultiLineToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {1,2,-3,4}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {1,2,-3,4}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.MultiLineToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {{1,2},<null>,{5}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {{1,2},<null>,{5}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.NoFieldNamesToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang3.builder.NoFieldNamesToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang3.builder.NoFieldNamesToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[3,4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[<size=0>]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), false).toString());
        assertEquals(baseStr + "[[]]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), true).toString());
        assertEquals(baseStr + "[<size=0>]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), false).toString());
        assertEquals(baseStr + "[{}]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), true).toString());
        assertEquals(baseStr + "[<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[{}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang3.builder.NoFieldNamesToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "Ron Paul";
        p.age = 72;
        p.smoker = false;
        String pBaseStr = p.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(p));
        assertEquals(pBaseStr + "[Ron Paul,72,false]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang3.builder.NoFieldNamesToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[3,4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang3.builder.NoFieldNamesToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.NoFieldNamesToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.NoFieldNamesToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExclude
    public void test_toStringExclude() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), SECRET_FIELD);
        this.validateSecretFieldAbsent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeArray
    public void test_toStringExcludeArray() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), new String[]{SECRET_FIELD});
        this.validateSecretFieldAbsent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeArrayWithNull
    public void test_toStringExcludeArrayWithNull() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), new String[]{null});
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeArrayWithNulls
    public void test_toStringExcludeArrayWithNulls() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), new String[]{null, null});
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeCollection
    public void test_toStringExcludeCollection() {
        List<String> excludeList = new ArrayList<String>();
        excludeList.add(SECRET_FIELD);
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), excludeList);
        this.validateSecretFieldAbsent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeCollectionWithNull
    public void test_toStringExcludeCollectionWithNull() {
        List<String> excludeList = new ArrayList<String>();
        excludeList.add(null);
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), excludeList);
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeCollectionWithNulls
    public void test_toStringExcludeCollectionWithNulls() {
        List<String> excludeList = new ArrayList<String>();
        excludeList.add(null);
        excludeList.add(null);
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), excludeList);
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeEmptyArray
    public void test_toStringExcludeEmptyArray() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), ArrayUtils.EMPTY_STRING_ARRAY);
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeEmptyCollection
    public void test_toStringExcludeEmptyCollection() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), new ArrayList<String>());
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeNullArray
    public void test_toStringExcludeNullArray() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), (String[]) null);
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ReflectionToStringBuilderExcludeTest::test_toStringExcludeNullCollection
    public void test_toStringExcludeNullCollection() {
        String toString = ReflectionToStringBuilder.toStringExclude(new TestFixture(), (Collection<String>) null);
        this.validateSecretFieldPresent(toString);
    }

// org.apache.commons.lang3.builder.ShortPrefixToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang3.builder.ShortPrefixToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang3.builder.ShortPrefixToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang3.builder.ShortPrefixToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "John Q. Public";
        p.age = 45;
        p.smoker = true;
        String pBaseStr = "ToStringStyleTest.Person";
        assertEquals(pBaseStr + "[name=John Q. Public,age=45,smoker=true]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang3.builder.ShortPrefixToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang3.builder.ShortPrefixToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.ShortPrefixToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.ShortPrefixToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.SimpleToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals("", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang3.builder.SimpleToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals("", new ToStringBuilder(base).appendSuper("").toString());
        assertEquals("<null>", new ToStringBuilder(base).appendSuper("<null>").toString());
        
        assertEquals("hello", new ToStringBuilder(base).appendSuper("").append("a", "hello").toString());
        assertEquals("<null>,hello", new ToStringBuilder(base).appendSuper("<null>").append("a", "hello").toString());
        assertEquals("hello", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang3.builder.SimpleToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals("<null>", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals("3", new ToStringBuilder(base).append(i3).toString());
        assertEquals("<null>", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals("3", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals("3,4", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals("<Integer>", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals("<size=0>", new ToStringBuilder(base).append("a", new ArrayList<Object>(), false).toString());
        assertEquals("[]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), true).toString());
        assertEquals("<size=0>", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), false).toString());
        assertEquals("{}", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), true).toString());
        assertEquals("<size=0>", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals("{}", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang3.builder.SimpleToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "Jane Q. Public";
        p.age = 47;
        p.smoker = false;
        assertEquals("Jane Q. Public,47,false", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang3.builder.SimpleToStringStyleTest::testLong
    public void testLong() {
        assertEquals("3", new ToStringBuilder(base).append(3L).toString());
        assertEquals("3", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals("3,4", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang3.builder.SimpleToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals("{<null>,5,{3,6}}", new ToStringBuilder(base).append(array).toString());
        assertEquals("{<null>,5,{3,6}}", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals("<null>", new ToStringBuilder(base).append(array).toString());
        assertEquals("<null>", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.SimpleToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals("{1,2,-3,4}", new ToStringBuilder(base).append(array).toString());
        assertEquals("{1,2,-3,4}", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals("<null>", new ToStringBuilder(base).append(array).toString());
        assertEquals("<null>", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.SimpleToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals("{{1,2},<null>,{5}}", new ToStringBuilder(base).append(array).toString());
        assertEquals("{{1,2},<null>,{5}}", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals("<null>", new ToStringBuilder(base).append(array).toString());
        assertEquals("<null>", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.StandardToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang3.builder.StandardToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).appendSuper("Integer@8888[%NULL%]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[%NULL%,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[%NULL%]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang3.builder.StandardToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=%NULL%]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=%Integer%]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), true).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), true).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang3.builder.StandardToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "Suzy Queue";
        p.age = 19;
        p.smoker = false;
        String pBaseStr = "ToStringStyleTest.Person";
        assertEquals(pBaseStr + "[name=Suzy Queue,age=19,smoker=false]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang3.builder.StandardToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang3.builder.StandardToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[[%NULL%, 5, [3, 6]]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[%NULL%, 5, [3, 6]]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.StandardToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[[1, 2, -3, 4]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[1, 2, -3, 4]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.StandardToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[[[1, 2], %NULL%, [5]]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[[1, 2], %NULL%, [5]]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang3.builder.ToStringBuilderTest::testConstructorEx1
    public void testConstructorEx1() {
        assertEquals("<null>", new ToStringBuilder(null).toString());
    }

// org.apache.commons.lang3.builder.ToStringBuilderTest::testConstructorEx2
    public void testConstructorEx2() {
        assertEquals("<null>", new ToStringBuilder(null, null).toString());
        new ToStringBuilder(this.base, null).toString();
    }
