// org/jfree/data/time/junit/TimePeriodValuesTests.java
public void testGetMaxMiddleIndexBug2() {
    TimePeriodValues s = new TimePeriodValues("Test");
    s.add(new SimpleTimePeriod(0L, 200L), 1.0);
    assertEquals(0, s.getMaxMiddleIndex());
    s.add(new SimpleTimePeriod(300L, 500L), 2.0);
    assertEquals(1, s.getMaxMiddleIndex());
    s.add(new SimpleTimePeriod(200L, 300L), 3.0);
    assertEquals(1, s.getMaxMiddleIndex());
}
