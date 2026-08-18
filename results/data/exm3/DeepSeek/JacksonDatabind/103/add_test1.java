// com/fasterxml/jackson/databind/exc/BasicExceptionTest.java
public void testLocationAdditionForEnumKey2() throws Exception
    {
        try {
            MAPPER.readValue("{\"key\":123}", new TypeReference<Map<ABC, String>>() { });
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            String msg = e.getMessage();
            String[] str = msg.split(" at \\[");
            if (str.length != 2) {
                fail("Should only get one 'at [' marker, got "+(str.length-1)+", source: "+msg);
            }
        }
    }
