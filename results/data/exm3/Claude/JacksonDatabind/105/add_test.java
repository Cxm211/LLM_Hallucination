// com/fasterxml/jackson/databind/deser/jdk/JDKScalarsTest.java
public void testVoidDeserNull() throws Exception
{
    VoidBean bean = MAPPER.readValue(aposToQuotes("{'value' : null }"),
            VoidBean.class);
    assertNull(bean.value);
}