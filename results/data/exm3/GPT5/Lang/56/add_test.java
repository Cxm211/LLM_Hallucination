// org/apache/commons/lang/time/FastDateFormatTest.java::testLang303
public void testToStringAfterDeserialization() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");
        FastDateFormat deser = (FastDateFormat) SerializationUtils.deserialize(SerializationUtils.serialize(format));
        String s = deser.toString();
        assertTrue(s.contains("yyyy/MM/dd"));
    }