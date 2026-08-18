// org/joda/time/TestPartial_Basics.java
public void testWith4() {
        Partial test = createHourMinPartial();
        try {
            test.with(DateTimeFieldType.clockhourOfHalfday(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }
