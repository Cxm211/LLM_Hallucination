// com/fasterxml/jackson/databind/exc/BasicExceptionTest.java
public void testLocationAdditionForValue() throws Exception
    {
        try {
            MAPPER.readValue("{\"value\":\"foo\"}", new TypeReference<Map<String, Integer>>() { });
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            String msg = e.getMessage();
            String[] str = msg.split(" at \\[");
            if (str.length != 2) {
                fail("Should only get one 'at [' marker, got "+(str.length-1)+", source: "+msg);
            }
        }
    }
