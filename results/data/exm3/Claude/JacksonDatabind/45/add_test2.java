// com/fasterxml/jackson/databind/ser/DateSerializationTest.java
public void testDateWithLocaleAndTimezone() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // @JsonFormat with both locale and timezone => STRING shape
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        class DateWithLocaleAndTimezone {
            @JsonFormat(locale="en_GB", timezone="Europe/London")
            public Date date;
            public DateWithLocaleAndTimezone(long timestamp) {
                this.date = new Date(timestamp);
            }
        }
        
        String json = mapper.writeValueAsString(new DateWithLocaleAndTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T01:00:00.000+0100'}"), json);
    }