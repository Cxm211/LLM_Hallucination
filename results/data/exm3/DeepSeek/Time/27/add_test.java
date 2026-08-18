// org/joda/time/format/TestPeriodFormatterBuilder.java
public void testSeparatorAtStart() {
    PeriodFormatter fmt = new PeriodFormatterBuilder()
        .appendSeparatorIfFieldsAfter("T")
        .appendHours()
        .appendSuffix("H")
        .appendMinutes()
        .appendSuffix("M")
        .toFormatter();
    Period p = new Period(0, 0, 0, 0, 5, 30, 0, 0);
    assertEquals("T5H30M", fmt.print(p));
    Period parsed = fmt.parsePeriod("T5H30M");
    assertEquals(p, parsed);
}
