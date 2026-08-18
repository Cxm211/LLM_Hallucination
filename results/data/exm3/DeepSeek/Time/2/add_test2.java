// org/joda/time/TestPartial_Basics.java
public void testConstructor_ValidOrderNonDuplicate() {
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.year(), DateTimeFieldType.monthOfYear() };
        int[] values = new int[] { 2020, 5 };
        Partial partial = new Partial(types, values, ISOChronology.getInstanceUTC());
        assertEquals(2, partial.size());
        assertEquals(0, partial.indexOf(DateTimeFieldType.year()));
        assertEquals(1, partial.indexOf(DateTimeFieldType.monthOfYear()));
    }
