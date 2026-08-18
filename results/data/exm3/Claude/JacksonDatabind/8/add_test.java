// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testStringBuilderWithWhitespace() throws Exception
{
    StringBuilder sb = MAPPER.readValue(quote(" test "), StringBuilder.class);
    assertEquals(" test ", sb.toString());
}