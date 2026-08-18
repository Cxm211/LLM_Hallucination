// com/fasterxml/jackson/databind/ser/TestEnumSerialization.java
public void testRawEnumClassAsKey() throws Exception
{
    Map<Class<?>, String> input = new HashMap<Class<?>, String>();
    input.put(Enum.class, "enumValue");
    String result = MAPPER.writeValueAsString(input);
    assertTrue(result.contains("enumValue"));
}