// buggy code
        protected Set<IDKey> initialValue() {
            return new HashSet<IDKey>();
        }

    static boolean isRegistered(Object value) {
        return getRegistry().contains(new IDKey(value));
    }

    static void register(Object value) {
        getRegistry().add(new IDKey(value));
    }

    static void unregister(Object value) {
        getRegistry().remove(new IDKey(value));
    }

// relevant test
// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testInteger
    public void testInteger(boolean testTransients) {
        Integer i1 = new Integer(12345);
        Integer i2 = new Integer(12345);
        assertEqualsAndHashCodeContract(i1, i2, testTransients);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testInteger
    public void testInteger() {
        testInteger(false);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testIntegerWithTransients
    public void testIntegerWithTransients() {
        testInteger(true);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testFixture
    public void testFixture() {
        testFixture(false);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testFixtureWithTransients
    public void testFixtureWithTransients() {
        testFixture(true);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderAndEqualsBuilderTest::testFixture
    public void testFixture(boolean testTransients) {
        assertEqualsAndHashCodeContract(new TestFixture(2, 'c', "Test", (short) 2), new TestFixture(2, 'c', "Test", (short) 2), testTransients);
        assertEqualsAndHashCodeContract(
            new AllTransientFixture(2, 'c', "Test", (short) 2),
            new AllTransientFixture(2, 'c', "Test", (short) 2),
            testTransients);
        assertEqualsAndHashCodeContract(
            new SubTestFixture(2, 'c', "Test", (short) 2, "Same"),
            new SubTestFixture(2, 'c', "Test", (short) 2, "Same"),
            testTransients);
        assertEqualsAndHashCodeContract(
            new SubAllTransientFixture(2, 'c', "Test", (short) 2, "Same"),
            new SubAllTransientFixture(2, 'c', "Test", (short) 2, "Same"),
            testTransients);
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testConstructorEx1
    public void testConstructorEx1() {
        try {
            new HashCodeBuilder(0, 0);

        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testConstructorEx2
    public void testConstructorEx2() {
        try {
            new HashCodeBuilder(2, 2);

        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCode
    public void testReflectionHashCode() {
        assertEquals(17 * 37, HashCodeBuilder.reflectionHashCode(new TestObject(0)));
        assertEquals(17 * 37 + 123456, HashCodeBuilder.reflectionHashCode(new TestObject(123456)));
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHierarchyHashCode
    public void testReflectionHierarchyHashCode() {
        assertEquals(17 * 37 * 37, HashCodeBuilder.reflectionHashCode(new TestSubObject(0, 0, 0)));
        assertEquals(17 * 37 * 37 * 37, HashCodeBuilder.reflectionHashCode(new TestSubObject(0, 0, 0), true));
        assertEquals((17 * 37 + 7890) * 37 + 123456, HashCodeBuilder.reflectionHashCode(new TestSubObject(123456, 7890,
                0)));
        assertEquals(((17 * 37 + 7890) * 37 + 0) * 37 + 123456, HashCodeBuilder.reflectionHashCode(new TestSubObject(
                123456, 7890, 0), true));
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHierarchyHashCodeEx1
    public void testReflectionHierarchyHashCodeEx1() {
        try {
            HashCodeBuilder.reflectionHashCode(0, 0, new TestSubObject(0, 0, 0), true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHierarchyHashCodeEx2
    public void testReflectionHierarchyHashCodeEx2() {
        try {
            HashCodeBuilder.reflectionHashCode(2, 2, new TestSubObject(0, 0, 0), true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCodeEx1
    public void testReflectionHashCodeEx1() {
        try {
            HashCodeBuilder.reflectionHashCode(0, 0, new TestObject(0), true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCodeEx2
    public void testReflectionHashCodeEx2() {
        try {
            HashCodeBuilder.reflectionHashCode(2, 2, new TestObject(0), true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCodeEx3
    public void testReflectionHashCodeEx3() {
        try {
            HashCodeBuilder.reflectionHashCode(13, 19, null, true);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testSuper
    public void testSuper() {
        Object obj = new Object();
        assertEquals(17 * 37 + (19 * 41 + obj.hashCode()), new HashCodeBuilder(17, 37).appendSuper(
                new HashCodeBuilder(19, 41).append(obj).toHashCode()).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testObject
    public void testObject() {
        Object obj = null;
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj = new Object();
        assertEquals(17 * 37 + obj.hashCode(), new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testLong
    public void testLong() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((long) 0L).toHashCode());
        assertEquals(17 * 37 + (int) (123456789L ^ (123456789L >> 32)), new HashCodeBuilder(17, 37).append(
                (long) 123456789L).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testInt
    public void testInt() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((int) 0).toHashCode());
        assertEquals(17 * 37 + 123456, new HashCodeBuilder(17, 37).append((int) 123456).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testShort
    public void testShort() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((short) 0).toHashCode());
        assertEquals(17 * 37 + 12345, new HashCodeBuilder(17, 37).append((short) 12345).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testChar
    public void testChar() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((char) 0).toHashCode());
        assertEquals(17 * 37 + 1234, new HashCodeBuilder(17, 37).append((char) 1234).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testByte
    public void testByte() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((byte) 0).toHashCode());
        assertEquals(17 * 37 + 123, new HashCodeBuilder(17, 37).append((byte) 123).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testDouble
    public void testDouble() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((double) 0d).toHashCode());
        double d = 1234567.89;
        long l = Double.doubleToLongBits(d);
        assertEquals(17 * 37 + (int) (l ^ (l >> 32)), new HashCodeBuilder(17, 37).append(d).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testFloat
    public void testFloat() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((float) 0f).toHashCode());
        float f = 1234.89f;
        int i = Float.floatToIntBits(f);
        assertEquals(17 * 37 + i, new HashCodeBuilder(17, 37).append(f).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testBoolean
    public void testBoolean() {
        assertEquals(17 * 37 + 0, new HashCodeBuilder(17, 37).append(true).toHashCode());
        assertEquals(17 * 37 + 1, new HashCodeBuilder(17, 37).append(false).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testObjectArray
    public void testObjectArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((Object[]) null).toHashCode());
        Object[] obj = new Object[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = new Object();
        assertEquals((17 * 37 + obj[0].hashCode()) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = new Object();
        assertEquals((17 * 37 + obj[0].hashCode()) * 37 + obj[1].hashCode(), new HashCodeBuilder(17, 37).append(obj)
                .toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testObjectArrayAsObject
    public void testObjectArrayAsObject() {
        Object[] obj = new Object[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = new Object();
        assertEquals((17 * 37 + obj[0].hashCode()) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = new Object();
        assertEquals((17 * 37 + obj[0].hashCode()) * 37 + obj[1].hashCode(), new HashCodeBuilder(17, 37).append(
                (Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testLongArray
    public void testLongArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((long[]) null).toHashCode());
        long[] obj = new long[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = 5L;
        int h1 = (int) (5L ^ (5L >> 32));
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = 6L;
        int h2 = (int) (6L ^ (6L >> 32));
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testLongArrayAsObject
    public void testLongArrayAsObject() {
        long[] obj = new long[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = 5L;
        int h1 = (int) (5L ^ (5L >> 32));
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = 6L;
        int h2 = (int) (6L ^ (6L >> 32));
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testIntArray
    public void testIntArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((int[]) null).toHashCode());
        int[] obj = new int[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testIntArrayAsObject
    public void testIntArrayAsObject() {
        int[] obj = new int[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testShortArray
    public void testShortArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((short[]) null).toHashCode());
        short[] obj = new short[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = (short) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = (short) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testShortArrayAsObject
    public void testShortArrayAsObject() {
        short[] obj = new short[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = (short) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = (short) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testCharArray
    public void testCharArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((char[]) null).toHashCode());
        char[] obj = new char[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = (char) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = (char) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testCharArrayAsObject
    public void testCharArrayAsObject() {
        char[] obj = new char[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = (char) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = (char) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testByteArray
    public void testByteArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((byte[]) null).toHashCode());
        byte[] obj = new byte[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = (byte) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = (byte) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testByteArrayAsObject
    public void testByteArrayAsObject() {
        byte[] obj = new byte[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = (byte) 5;
        assertEquals((17 * 37 + 5) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = (byte) 6;
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testDoubleArray
    public void testDoubleArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((double[]) null).toHashCode());
        double[] obj = new double[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = 5.4d;
        long l1 = Double.doubleToLongBits(5.4d);
        int h1 = (int) (l1 ^ (l1 >> 32));
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = 6.3d;
        long l2 = Double.doubleToLongBits(6.3d);
        int h2 = (int) (l2 ^ (l2 >> 32));
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testDoubleArrayAsObject
    public void testDoubleArrayAsObject() {
        double[] obj = new double[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = 5.4d;
        long l1 = Double.doubleToLongBits(5.4d);
        int h1 = (int) (l1 ^ (l1 >> 32));
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = 6.3d;
        long l2 = Double.doubleToLongBits(6.3d);
        int h2 = (int) (l2 ^ (l2 >> 32));
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testFloatArray
    public void testFloatArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((float[]) null).toHashCode());
        float[] obj = new float[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = 5.4f;
        int h1 = Float.floatToIntBits(5.4f);
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = 6.3f;
        int h2 = Float.floatToIntBits(6.3f);
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testFloatArrayAsObject
    public void testFloatArrayAsObject() {
        float[] obj = new float[2];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = 5.4f;
        int h1 = Float.floatToIntBits(5.4f);
        assertEquals((17 * 37 + h1) * 37, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = 6.3f;
        int h2 = Float.floatToIntBits(6.3f);
        assertEquals((17 * 37 + h1) * 37 + h2, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testBooleanArray
    public void testBooleanArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((boolean[]) null).toHashCode());
        boolean[] obj = new boolean[2];
        assertEquals((17 * 37 + 1) * 37 + 1, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = true;
        assertEquals((17 * 37 + 0) * 37 + 1, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = false;
        assertEquals((17 * 37 + 0) * 37 + 1, new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testBooleanArrayAsObject
    public void testBooleanArrayAsObject() {
        boolean[] obj = new boolean[2];
        assertEquals((17 * 37 + 1) * 37 + 1, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[0] = true;
        assertEquals((17 * 37 + 0) * 37 + 1, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
        obj[1] = false;
        assertEquals((17 * 37 + 0) * 37 + 1, new HashCodeBuilder(17, 37).append((Object) obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testBooleanMultiArray
    public void testBooleanMultiArray() {
        boolean[][] obj = new boolean[2][];
        assertEquals((17 * 37) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = new boolean[0];
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = new boolean[1];
        assertEquals((17 * 37 + 1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0] = new boolean[2];
        assertEquals(((17 * 37 + 1) * 37 + 1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[0][0] = true;
        assertEquals(((17 * 37 + 0) * 37 + 1) * 37, new HashCodeBuilder(17, 37).append(obj).toHashCode());
        obj[1] = new boolean[1];
        assertEquals((((17 * 37 + 0) * 37 + 1) * 37 + 1), new HashCodeBuilder(17, 37).append(obj).toHashCode());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionHashCodeExcludeFields
    public void testReflectionHashCodeExcludeFields() throws Exception {
        TestObjectWithMultipleFields x = new TestObjectWithMultipleFields(1, 2, 3);

        assertEquals((((17 * 37 + 1) * 37 + 2) * 37 + 3), HashCodeBuilder.reflectionHashCode(x));

        assertEquals((((17 * 37 + 1) * 37 + 2) * 37 + 3), HashCodeBuilder.reflectionHashCode(x, (String[]) null));
        assertEquals((((17 * 37 + 1) * 37 + 2) * 37 + 3), HashCodeBuilder.reflectionHashCode(x, new String[]{}));
        assertEquals((((17 * 37 + 1) * 37 + 2) * 37 + 3), HashCodeBuilder.reflectionHashCode(x, new String[]{"xxx"}));

        assertEquals(((17 * 37 + 1) * 37 + 3), HashCodeBuilder.reflectionHashCode(x, new String[]{"two"}));
        assertEquals(((17 * 37 + 1) * 37 + 2), HashCodeBuilder.reflectionHashCode(x, new String[]{"three"}));

        assertEquals((17 * 37 + 1), HashCodeBuilder.reflectionHashCode(x, new String[]{"two", "three"}));

        assertEquals(17, HashCodeBuilder.reflectionHashCode(x, new String[]{"one", "two", "three"}));
        assertEquals(17, HashCodeBuilder.reflectionHashCode(x, new String[]{"one", "two", "three", "xxx"}));
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testReflectionObjectCycle
    public void testReflectionObjectCycle() {
        ReflectionTestCycleA a = new ReflectionTestCycleA();
        ReflectionTestCycleB b = new ReflectionTestCycleB();
        a.b = b;
        b.a = a;
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        a.hashCode();
        assertNull(HashCodeBuilder.getRegistry());
        b.hashCode();
        assertNull(HashCodeBuilder.getRegistry());
    }

// org.apache.commons.lang3.builder.HashCodeBuilderTest::testToHashCodeEqualsHashCode
    public void testToHashCodeEqualsHashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(new Object()).append('a');
        assertEquals("hashCode() is no longer returning the same value as toHashCode() - see LANG-520", 
                     hcb.toHashCode(), hcb.hashCode());
    }
