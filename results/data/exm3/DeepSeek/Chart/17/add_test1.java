// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCloneEmptySeriesModifyClone() {
    TimeSeries s1 = new TimeSeries("Series");
    TimeSeries s2 = null;
    try {
        s2 = (TimeSeries) s1.clone();
    } catch (CloneNotSupportedException e) {
        e.printStackTrace();
    }
    assertTrue(s1 != s2);
    assertTrue(s1.equals(s2));
    s2.add(new Day(1, 1, 2007), 100.0);
    assertEquals(0, s1.getItemCount());
    assertFalse(s1.equals(s2));
}
