// org/joda/time/TestPeriod_Basics.java
public void testNormalizedStandard_periodType_months_yearsNegativeMonthsZero() {
        Period test = new Period(-3, 0, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.months());
        assertEquals(new Period(-3, 0, 0, 0, 0, 0, 0, 0), test);
        assertEquals(new Period(0, -36, 0, 0, 0, 0, 0, 0, PeriodType.months()), result);
    }
