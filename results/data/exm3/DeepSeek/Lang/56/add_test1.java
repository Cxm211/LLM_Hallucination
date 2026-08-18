// org/apache/commons/lang/time/FastDateFormatTest.java
public void testToStringAfterSerialization() {
    FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");
    FastDateFormat deserialized = (FastDateFormat) SerializationUtils.deserialize(SerializationUtils.serialize(format));
    assertEquals(format.toString(), deserialized.toString());
}
