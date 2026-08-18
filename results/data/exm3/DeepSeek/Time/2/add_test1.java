// org/joda/time/TestPartial_Basics.java
public void testWith_largerUnit() {
        Partial test = new Partial(DateTimeFieldType.monthOfYear(), 5);
        Partial result = test.with(DateTimeFieldType.year(), 2020);
        assertEquals(2, result.size());
        assertEquals(0, result.indexOf(DateTimeFieldType.year()));
        assertEquals(1, result.indexOf(DateTimeFieldType.monthOfYear()));
    }
