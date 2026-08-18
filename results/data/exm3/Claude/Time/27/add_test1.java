// org/joda/time/format/TestPeriodFormatterBuilder.java
public void testSeparatorWithParserOnly() {
    PeriodFormatter pfmt = new PeriodFormatterBuilder()
        .appendYears()
        .appendSuffix("Y")
        .appendSeparatorIfFieldsAfter("T")
        .appendHours()
        .appendSuffix("H")
        .toParser();
    Period period = pfmt.parsePeriod("1YT2H");
    assertEquals(1, period.getYears());
    assertEquals(2, period.getHours());
}