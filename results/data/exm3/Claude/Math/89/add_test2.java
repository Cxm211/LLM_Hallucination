// org/apache/commons/math/stat/FrequencyTest.java
public void testAddComparableObjectAfterClear() {
    f.addValue(1);
    f.clear();
    try {
        f.addValue("valid");
    } catch (Exception e) {
        fail("Should accept Comparable object after clear");
    }
}