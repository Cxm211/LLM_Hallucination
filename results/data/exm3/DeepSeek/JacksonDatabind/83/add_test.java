// com/fasterxml/jackson/databind/filter/ProblemHandlerTest.java
public void testWeirdStringHandlingWithNullForEnum() throws Exception
{
    ObjectMapper mapper = new ObjectMapper()
        .addHandler(new WeirdStringHandler(null));
    SingleValuedEnum result = mapper.readValue("\"C\"", SingleValuedEnum.class);
    assertNull(result);
}
