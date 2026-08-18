// com/fasterxml/jackson/core/util/TestDefaultPrettyPrinter.java
public void testDirectDefaultPrettyPrinterCreateInstance() throws Exception
{
    // Test that DefaultPrettyPrinter itself can call createInstance successfully
    DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
    DefaultPrettyPrinter instance = pp.createInstance();
    assertNotNull(instance);
    assertEquals(DefaultPrettyPrinter.class, instance.getClass());
}