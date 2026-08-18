// org/apache/commons/math/stat/FrequencyTest.java
public void testAddNonComparableMultipleComparable() {
        f.addValue(1);
        f.addValue(2L);
        f.addValue("abc");
        try {
            f.addValue(new Object());
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
            // expected
        }
    }
