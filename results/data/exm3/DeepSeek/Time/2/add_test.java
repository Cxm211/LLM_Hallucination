// org/joda/time/TestPartial_Basics.java
public void testConstructor_InvalidOrder() {
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.monthOfYear(), DateTimeFieldType.year() };
        int[] values = new int[] { 1, 2020 };
        try {
            new Partial(types, values, ISOChronology.getInstanceUTC());
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }
