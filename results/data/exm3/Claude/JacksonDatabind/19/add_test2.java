// com/fasterxml/jackson/databind/convert/TestMapConversions.java
public void testPropertiesToMap() throws Exception
{
    // Test converting Properties back to a regular Map
    Properties props = new Properties();
    props.setProperty("key1", "value1");
    props.setProperty("key2", "value2");
    
    @SuppressWarnings("unchecked")
    java.util.Map<String,String> map = MAPPER.convertValue(props, 
            MAPPER.getTypeFactory().constructMapType(java.util.HashMap.class, String.class, String.class));
    
    assertEquals(2, map.size());
    assertEquals("value1", map.get("key1"));
    assertEquals("value2", map.get("key2"));
}