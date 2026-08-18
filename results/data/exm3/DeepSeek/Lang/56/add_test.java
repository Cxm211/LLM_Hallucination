// org/apache/commons/lang/time/FastDateFormatTest.java
public void testToString() {
    FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");
    assertEquals("FastDateFormat[yyyy/MM/dd]", format.toString());
}
