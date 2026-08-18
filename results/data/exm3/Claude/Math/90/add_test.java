// org/apache/commons/math/stat/FrequencyTest.java
public void testAddNonComparableEmpty(){
    try {
        f.addValue(new Object());
        f.addValue(new Object());
        fail("Expected ClassCastException");
    } catch (ClassCastException expected) {
    }
}