// com/fasterxml/jackson/databind/ser/DateSerializationTest.java
public void testFormatWithStringShapeNoPattern() throws Exception {
        @JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
        class LocalBean {
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            public Date date = new Date(0L);
        }
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(sdf);
        String json = mapper.writeValueAsString(new LocalBean());
        assertEquals(aposToQuotes("{\"date\":\"1970-01-01X00:00:00\"}"), json);
    }
