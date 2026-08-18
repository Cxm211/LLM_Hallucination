// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCloneWithMultipleItems() throws CloneNotSupportedException {
    TimeSeries s1 = new TimeSeries("Series");
    s1.add(new Day(1, 1, 2007), 100.0);
    s1.add(new Day(2, 1, 2007), 200.0);
    s1.add(new Day(3, 1, 2007), 300.0);
    TimeSeries s2 = (TimeSeries) s1.clone();
    assertTrue(s1 != s2);
    assertTrue(s1.getClass() == s2.getClass());
    assertTrue(s1.equals(s2));
    
    // test independence - modify original
    s1.update(new Day(2, 1, 2007), 250.0);
    assertFalse(s1.equals(s2));
    assertEquals(200.0, s2.getValue(1));
}