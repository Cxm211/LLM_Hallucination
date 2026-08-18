// org/apache/commons/lang/time/FastDateFormatTest.java
public void testToStringWithNullPattern() {
    FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");
    format = (FastDateFormat) SerializationUtils.deserialize(SerializationUtils.serialize(format));
    assertNotNull("toString should not return null", format.toString());
    assertTrue("toString should contain FastDateFormat", format.toString().startsWith("FastDateFormat["));
}