// org/joda/time/TestPartial_Basics.java
public void testWith5() {
        Partial test = createHourMinPartial();
        try {
            test.with(DateTimeFieldType.halfdayOfDay(), 1);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }
