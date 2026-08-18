// com/fasterxml/jackson/databind/ser/DateSerializationTest.java::testShapeStringUsesMapperFormat
public void testShapeStringUsesMapperFormat() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy/MM/dd"));
        class Bean {
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            public Date date = new Date(0L);
        }
        String json = mapper.writeValueAsString(new Bean());
        assertEquals(aposToQuotes("{'date':'1970/01/01'}"), json);
    }