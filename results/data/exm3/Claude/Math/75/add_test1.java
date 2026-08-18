// org/apache/commons/math/stat/FrequencyTest.java
public void testPctWithSingleValue() {
    f.addValue(oneL);
    assertEquals("single value pct", 1.0, f.getPct((Object) oneL), tolerance);
}