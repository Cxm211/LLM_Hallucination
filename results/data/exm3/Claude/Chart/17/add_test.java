// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCloneWithSingleItem() throws CloneNotSupportedException {
    TimeSeries s1 = new TimeSeries("Series");
    s1.add(new Day(1, 1, 2007), 100.0);
    TimeSeries s2 = (TimeSeries) s1.clone();
    assertTrue(s1 != s2);
    assertTrue(s1.getClass() == s2.getClass());
    assertTrue(s1.equals(s2));
    
    // test independence
    s1.add(new Day(2, 1, 2007), 200.0);
    assertFalse(s1.equals(s2));
    assertEquals(2, s1.getItemCount());
    assertEquals(1, s2.getItemCount());
}