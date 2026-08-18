// org/apache/commons/math/stat/FrequencyTest.java
public void testPctWithNullValue() {
    f.addValue(oneL);
    f.addValue(twoL);
    f.addValue(oneI);
    f.addValue(twoI);
    assertEquals("null pct", 0.0, f.getPct((Object) null), tolerance);
}