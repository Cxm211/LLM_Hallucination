// org/apache/commons/math/stat/FrequencyTest.java
public void testAddNonComparableAfterString(){
    f.addValue("test");
    try {
        f.addValue(new Object());
        fail("Expected ClassCastException");
    } catch (ClassCastException expected) {
    }
}