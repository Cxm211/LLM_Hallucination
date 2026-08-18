// org/joda/time/TestPartial_Basics.java
public void testWith_baseMonthAndArgHaveNoRange() {
        Partial test = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial result = test.with(DateTimeFieldType.era(), 1);
        assertEquals(2, result.size());
        assertEquals(0, result.indexOf(DateTimeFieldType.era()));
        assertEquals(1, result.indexOf(DateTimeFieldType.monthOfYear()));
    }