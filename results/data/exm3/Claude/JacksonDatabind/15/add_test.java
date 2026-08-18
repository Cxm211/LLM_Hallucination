// com/fasterxml/jackson/databind/convert/TestConvertingSerializer.java
public void testIssue731WithNestedConverter() throws Exception
{
    String json = objectWriter().writeValueAsString(new ConvertingBeanWithUntypedConverter(0, 0));
    assertEquals("{\"a\":0,\"b\":0}", json);
}