// org/joda/time/format/TestISOPeriodFormat.java
public void testFormatStandard_negativeMilliseconds() {
    Period p = Period.millis(-1);
    assertEquals("PT-0.001S", ISOPeriodFormat.standard().print(p));
    p = Period.millis(-999);
    assertEquals("PT-0.999S", ISOPeriodFormat.standard().print(p));
    p = Period.millis(-500);
    assertEquals("PT-0.500S", ISOPeriodFormat.standard().print(p));
    p = Period.seconds(0).withMillis(-123);
    assertEquals("PT-0.123S", ISOPeriodFormat.standard().print(p));
}
