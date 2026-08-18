// com/fasterxml/jackson/databind/ser/TestConfig.java::testDateFormatConfig
public void testDateFormatRepeatedConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TimeZone tz1 = TimeZone.getTimeZone("GMT+1");
        TimeZone tz2 = TimeZone.getTimeZone("GMT+3");
        TimeZone tz3 = TimeZone.getTimeZone("GMT-5");

        mapper.setTimeZone(tz1);
        assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());

        SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd");
        f2.setTimeZone(tz2);
        mapper.setDateFormat(f2);
        assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
        assertEquals(tz1, mapper.getDeserializationConfig().getTimeZone());

        SimpleDateFormat f3 = new SimpleDateFormat("HH:mm");
        f3.setTimeZone(tz3);
        mapper.setDateFormat(f3);
        assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
        assertEquals(tz1, mapper.getDeserializationConfig().getTimeZone());
        assertEquals(tz1, mapper.writer().getConfig().getTimeZone());
        assertEquals(tz1, mapper.reader().getConfig().getTimeZone());
    }