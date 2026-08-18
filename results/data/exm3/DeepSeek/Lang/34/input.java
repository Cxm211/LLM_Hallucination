// buggy function
    static Map<Object, Object> getRegistry() {
        return REGISTRY.get() != null ? REGISTRY.get() : Collections.<Object, Object>emptyMap();
    }

    static boolean isRegistered(Object value) {
        Map<Object, Object> m = getRegistry();
        return m.containsKey(value);
    }

// trigger testcase
// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testObjectCycle
public void testObjectCycle() {
        ObjectCycle a = new ObjectCycle();
        ObjectCycle b = new ObjectCycle();
        a.obj = b;
        b.obj = a;

        String expected = toBaseString(a) + "[" + toBaseString(b) + "[" + toBaseString(a) + "]]";
        assertEquals(expected, a.toString());
        validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionArrayAndObjectCycle
public void testReflectionArrayAndObjectCycle() throws Exception {
        Object[] objects = new Object[1];
        SimpleReflectionTestFixture simple = new SimpleReflectionTestFixture(objects);
        objects[0] = simple;
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
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionArrayArrayCycle
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
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionArrayCycle
public void testReflectionArrayCycle() throws Exception {
        Object[] objects = new Object[1];
        objects[0] = objects;
        assertEquals(
            this.toBaseString(objects) + "[{" + this.toBaseString(objects) + "}]",
            ToStringBuilder.reflectionToString(objects));
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionArrayCycleLevel2
public void testReflectionArrayCycleLevel2() throws Exception {
        Object[] objects = new Object[1];
        Object[] objectsLevel2 = new Object[1];
        objects[0] = objectsLevel2;
        objectsLevel2[0] = objects;
        assertEquals(
            this.toBaseString(objects) + "[{{" + this.toBaseString(objects) + "}}]",
            ToStringBuilder.reflectionToString(objects));
        assertEquals(
            this.toBaseString(objectsLevel2) + "[{{" + this.toBaseString(objectsLevel2) + "}}]",
            ToStringBuilder.reflectionToString(objectsLevel2));
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionBooleanArray
public void testReflectionBooleanArray() {
        boolean[] array = new boolean[] { true, false, false };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{true,false,false}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionBooleanArrayArray
public void testReflectionBooleanArrayArray() {
        boolean[][] array = new boolean[][] { { true, false }, null, { false } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", ToStringBuilder.reflectionToString(array));
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionByteArrayArray
public void testReflectionByteArrayArray() {
        byte[][] array = new byte[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionCharArray
public void testReflectionCharArray() {
        char[] array = new char[] { 'A', '2', '_', 'D' };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{A,2,_,D}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionCharArrayArray
public void testReflectionCharArrayArray() {
        char[][] array = new char[][] { { 'A', 'B' }, null, { 'p' } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{A,B},<null>,{p}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionDoubleArray
public void testReflectionDoubleArray() {
        double[] array = new double[] { 1.0, 2.9876, -3.00001, 4.3 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionDoubleArrayArray
public void testReflectionDoubleArrayArray() {
        double[][] array = new double[][] { { 1.0, 2.29686 }, null, { Double.NaN } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionFloatArray
public void testReflectionFloatArray() {
        float[] array = new float[] { 1.0f, 2.9876f, -3.00001f, 4.3f };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionFloatArrayArray
public void testReflectionFloatArrayArray() {
        float[][] array = new float[][] { { 1.0f, 2.29686f }, null, { Float.NaN } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionHierarchy
public void testReflectionHierarchy() {
        ReflectionTestFixtureA baseA = new ReflectionTestFixtureA();
        String baseStr = this.toBaseString(baseA);
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false));
        assertEquals(baseStr + "[a=a,transientA=t]", ToStringBuilder.reflectionToString(baseA, null, true));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false, null));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false, Object.class));
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
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false, ReflectionTestFixtureA.class));
        assertEquals(baseStr + "[b=b]", ToStringBuilder.reflectionToString(baseB, null, false, ReflectionTestFixtureB.class));
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionIntArray
public void testReflectionIntArray() {
        int[] array = new int[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionIntArrayArray
public void testReflectionIntArrayArray() {
        int[][] array = new int[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionLongArray
public void testReflectionLongArray() {
        long[] array = new long[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionLongArrayArray
public void testReflectionLongArrayArray() {
        long[][] array = new long[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionObjectArray
public void testReflectionObjectArray() {
        Object[] array = new Object[] { null, base, new int[] { 3, 6 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionObjectCycle
public void testReflectionObjectCycle() throws Exception {
        ReflectionTestCycleA a = new ReflectionTestCycleA();
        ReflectionTestCycleB b = new ReflectionTestCycleB();
        a.b = b;
        b.a = a;
        assertEquals(
            this.toBaseString(a) + "[b=" + this.toBaseString(b) + "[a=" + this.toBaseString(a) + "]]",
            a.toString());
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionShortArray
public void testReflectionShortArray() {
        short[] array = new short[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionhortArrayArray
public void testReflectionhortArrayArray() {
        short[][] array = new short[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testReflectionyteArray
public void testReflectionyteArray() {
        byte[] array = new byte[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testSelfInstanceTwoVarsReflectionObjectCycle
public void testSelfInstanceTwoVarsReflectionObjectCycle() throws Exception {
        SelfInstanceTwoVarsReflectionTestFixture test = new SelfInstanceTwoVarsReflectionTestFixture();
        assertEquals(this.toBaseString(test) + "[typeIsSelf=" + this.toBaseString(test) + ",otherType=" + test.getOtherType().toString() + "]", test.toString());
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testSelfInstanceVarReflectionObjectCycle
public void testSelfInstanceVarReflectionObjectCycle() throws Exception {
        SelfInstanceVarReflectionTestFixture test = new SelfInstanceVarReflectionTestFixture();
        assertEquals(this.toBaseString(test) + "[typeIsSelf=" + this.toBaseString(test) + "]", test.toString());
        this.validateNullToStringStyleRegistry();
    }

// org/apache/commons/lang3/builder/ToStringBuilderTest.java::testSimpleReflectionObjectCycle
public void testSimpleReflectionObjectCycle() throws Exception {
        SimpleReflectionTestFixture simple = new SimpleReflectionTestFixture();
        simple.o = simple;
        assertEquals(this.toBaseString(simple) + "[o=" + this.toBaseString(simple) + "]", simple.toString());
        this.validateNullToStringStyleRegistry();
    }
