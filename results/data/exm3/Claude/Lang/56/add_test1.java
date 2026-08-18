// org/apache/commons/lang/time/FastDateFormatTest.java
public void testToStringAfterMultipleSerialization() {
    FastDateFormat format = FastDateFormat.getInstance("dd-MM-yyyy HH:mm:ss");
    format = (FastDateFormat) SerializationUtils.deserialize(SerializationUtils.serialize(format));
    format = (FastDateFormat) SerializationUtils.deserialize(SerializationUtils.serialize(format));
    String result = format.toString();
    assertNotNull("toString should not return null after multiple serializations", result);
    assertTrue("toString should be valid after multiple serializations", result.contains("FastDateFormat["));
}