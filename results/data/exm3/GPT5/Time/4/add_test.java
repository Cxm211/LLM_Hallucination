// org/joda/time/TestPartial_Basics.java::testWith3
public void testWith_conflictReverseOrder() {
        Partial test = new Partial(DateTimeFieldType.clockhourOfDay(), 12).with(DateTimeFieldType.minuteOfHour(), 34);
        try {
            test.with(DateTimeFieldType.hourOfDay(), 10);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(34, test.get(DateTimeFieldType.minuteOfHour()));
    }