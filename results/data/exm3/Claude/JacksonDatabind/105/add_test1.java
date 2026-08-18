// com/fasterxml/jackson/databind/deser/jdk/JDKScalarsTest.java
public void testVoidDeserString() throws Exception
{
    VoidBean bean = MAPPER.readValue(aposToQuotes("{'value' : 'test' }"),
            VoidBean.class);
    assertNull(bean.value);
}