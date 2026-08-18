// org/apache/commons/lang3/builder/ToStringBuilderTest.java
public void testMultiLevelObjectCycle() {
        // Test a three-level cycle to explore deeper nesting behavior
        ObjectCycle a = new ObjectCycle();
        ObjectCycle b = new ObjectCycle();
        ObjectCycle c = new ObjectCycle();
        a.obj = b;
        b.obj = c;
        c.obj = a;

        String expected = toBaseString(a) + "[" + toBaseString(b) + "[" + toBaseString(c) + "[" + toBaseString(a) + "]]]";
        assertEquals(expected, a.toString());
        validateNullToStringStyleRegistry();
    }