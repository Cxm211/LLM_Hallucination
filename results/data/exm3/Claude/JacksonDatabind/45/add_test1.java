// com/fasterxml/jackson/databind/ser/DateSerializationTest.java
public void testDateWithCombinedPatternAndTimezone() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // @JsonFormat with both pattern and timezone => STRING shape
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        class DateWithPatternAndTimezone {
            @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="America/New_York")
            public Date date;
            public DateWithPatternAndTimezone(long timestamp) {
                this.date = new Date(timestamp);
            }
        }
        
        String json = mapper.writeValueAsString(new DateWithPatternAndTimezone(0L));
        assertTrue(json.contains("1969-12-31") || json.contains("1970-01-01"));
    }