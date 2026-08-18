// org/joda/time/format/TestPeriodFormatterBuilder.java
public void testParserOnlyWithSeparator() {
    PeriodParser parser = new PeriodFormatterBuilder()
        .appendSeparatorIfFieldsAfter("T")
        .appendHours()
        .appendSuffix("H")
        .appendMinutes()
        .appendSuffix("M")
        .toParser();
    PeriodFormatter fmt = new PeriodFormatter(null, parser);
    Period p = fmt.parsePeriod("T5H30M");
    assertEquals(new Period(0, 0, 0, 0, 5, 30, 0, 0), p);
}
