// org/jfree/data/time/junit/TimePeriodValuesTests.java
public void testMaxMiddleIndexNotUpdatedAfterLowerMiddle() {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.add(new SimpleTimePeriod(0L, 10L), 1.0);     // middle=5 -> max=0
        s.add(new SimpleTimePeriod(100L, 200L), 2.0);  // middle=150 -> max=1
        s.add(new SimpleTimePeriod(-100L, -50L), 3.0); // middle=-75 -> min changes
        s.add(new SimpleTimePeriod(50L, 60L), 4.0);    // middle=55 (<150)
        assertEquals(1, s.getMaxMiddleIndex());
    }