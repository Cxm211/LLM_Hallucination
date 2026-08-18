// org/apache/commons/math/stat/FrequencyTest.java
public void testAddNonComparableAfterString(){
        f.clear();
        f.addValue("a");
        try {
            f.addValue(new Object());
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }