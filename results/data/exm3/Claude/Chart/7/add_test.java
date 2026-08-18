// org/jfree/data/time/junit/TimePeriodValuesTests.java
public void testGetMinMiddleIndex() {
    TimePeriodValues s = new TimePeriodValues("Test");
    assertEquals(-1, s.getMinMiddleIndex());
    s.add(new SimpleTimePeriod(100L, 200L), 1.0);
    assertEquals(0, s.getMinMiddleIndex());
    s.add(new SimpleTimePeriod(300L, 400L), 2.0);
    assertEquals(0, s.getMinMiddleIndex());
    s.add(new SimpleTimePeriod(0L, 50L), 3.0);
    assertEquals(2, s.getMinMiddleIndex());
    s.add(new SimpleTimePeriod(150L, 200L), 4.0);
    assertEquals(2, s.getMinMiddleIndex());
}