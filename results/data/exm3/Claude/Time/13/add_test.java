// org/joda/time/format/TestISOPeriodFormat.java
public void testFormatStandard_negativeEdgeCases() {
    Period p = Period.seconds(-1).withMillis(0);
    assertEquals("PT-1S", ISOPeriodFormat.standard().print(p));
    
    p = Period.seconds(-1).withMillis(-1);
    assertEquals("PT-1.001S", ISOPeriodFormat.standard().print(p));
    
    p = Period.seconds(-1).withMillis(1);
    assertEquals("PT-0.999S", ISOPeriodFormat.standard().print(p));
    
    p = Period.seconds(-9).withMillis(-999);
    assertEquals("PT-9.999S", ISOPeriodFormat.standard().print(p));
    
    p = Period.seconds(-10);
    assertEquals("PT-10S", ISOPeriodFormat.standard().print(p));
}