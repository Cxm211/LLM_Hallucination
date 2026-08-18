// org/apache/commons/math/stat/FrequencyTest.java
public void testPctWithNonExistentValue() {
    f.addValue(oneL);
    f.addValue(twoL);
    assertEquals("non-existent value pct", 0.0, f.getPct((Object) Integer.valueOf(99)), tolerance);
}