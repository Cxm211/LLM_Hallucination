// com/fasterxml/jackson/databind/convert/TestConvertingSerializer.java
public void testIssue731WithNullDelegateValue() throws Exception
{
    String json = objectWriter().writeValueAsString(new ConvertingBeanWithUntypedConverter(-1, -1));
    assertNotNull(json);
}