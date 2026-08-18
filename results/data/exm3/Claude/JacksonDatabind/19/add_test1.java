// com/fasterxml/jackson/databind/deser/TestMapDeserialization.java
public void testPropertiesWithNonStringKeys() throws Exception
{
    // Properties should handle non-string keys by converting them to strings
    Properties props = MAPPER.readValue("[['key1','value1'],['key2','value2']]",
            Properties.class);
    assertEquals(2, props.size());
    assertEquals("value1", props.getProperty("key1"));
    assertEquals("value2", props.getProperty("key2"));
}