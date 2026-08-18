// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testLocaleMixedSeparators() throws IOException
{
    assertEquals(new Locale("zh", "CN", "variant"),
            MAPPER.readValue(quote("zh-CN_variant"), Locale.class));
}