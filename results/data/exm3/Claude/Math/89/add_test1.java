// org/apache/commons/math/stat/FrequencyTest.java
public void testAddNonComparableAfterMultipleAdds() {
    f.addValue(1);
    f.addValue(2);
    f.addValue(3);
    try {
        f.addValue(new Object());
        fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
}