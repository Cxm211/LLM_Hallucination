// com/fasterxml/jackson/databind/ser/TestEnumSerialization.java
public void testEnumsWithoutJsonPropertyAsKey() throws Exception
{
    EnumMap<TestEnum,String> input = new EnumMap<TestEnum,String>(TestEnum.class);
    input.put(TestEnum.A, "value");
    assertEquals("{\"A\":\"value\"}", MAPPER.writeValueAsString(input));
}