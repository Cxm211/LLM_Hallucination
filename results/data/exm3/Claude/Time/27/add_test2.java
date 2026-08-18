// org/joda/time/format/TestPeriodFormatterBuilder.java
public void testMultipleSeparators() {
    PeriodFormatter pfmt = new PeriodFormatterBuilder()
        .appendYears()
        .appendSuffix("Y")
        .appendSeparatorIfFieldsAfter("-")
        .appendMonths()
        .appendSuffix("M")
        .appendSeparatorIfFieldsAfter("T")
        .appendHours()
        .appendSuffix("H")
        .toFormatter();
    Period period = pfmt.parsePeriod("1Y-2MT3H");
    assertEquals(1, period.getYears());
    assertEquals(2, period.getMonths());
    assertEquals(3, period.getHours());
}