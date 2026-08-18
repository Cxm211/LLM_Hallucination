// org/apache/commons/math/stat/FrequencyTest.java
public void testAddNonComparableNull() {
    try {
        f.addValue((Object) null);
        fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
}