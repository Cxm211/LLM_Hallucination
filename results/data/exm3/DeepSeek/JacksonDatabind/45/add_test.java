// com/fasterxml/jackson/databind/ser/DateSerializationTest.java
public void testDateShapeAnyWithPatternAndTimezone() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // @JsonFormat with Shape.ANY, pattern, and timezone => STRING shape, regardless of user config
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = mapper.writeValueAsString(new DateAsDefaultBeanWithPatternAndTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01 01:00'}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithPatternAndTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01 01:00'}"), json);
    }

    public static class DateAsDefaultBeanWithPatternAndTimezone {
        @JsonFormat(shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+1")
        public Date date;
        public DateAsDefaultBeanWithPatternAndTimezone(long l) { date = new Date(l); }
    }
