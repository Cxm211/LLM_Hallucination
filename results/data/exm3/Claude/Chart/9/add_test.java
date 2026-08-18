// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCreateCopyStartAfterEnd() {
    TimeSeries s = new TimeSeries("S");
    s.add(new Day(1, 1, 2005), 1);
    s.add(new Day(1, 6, 2005), 2);
    TimeSeries copy = null;
    try {
        copy = s.createCopy(new Day(1, 7, 2005), new Day(1, 12, 2005));
    }
    catch (CloneNotSupportedException e) {
        fail("CloneNotSupportedException should not be thrown");
    }
    assertNotNull(copy);
    assertEquals(0, copy.getItemCount());
}