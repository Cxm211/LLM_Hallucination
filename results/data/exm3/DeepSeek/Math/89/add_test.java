// org/apache/commons/math/stat/FrequencyTest.java
public void testAddNonComparableTypes() {
        // test null
        try {
            f.addValue(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        f.clear();
        // test array
        try {
            f.addValue(new int[]{1, 2, 3});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }
