// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testEmptyStringBuilder() throws Exception
{
    StringBuilder sb = MAPPER.readValue(quote(""), StringBuilder.class);
    assertEquals("", sb.toString());
}