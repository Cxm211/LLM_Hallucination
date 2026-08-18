// com/fasterxml/jackson/core/util/TestDefaultPrettyPrinter.java
public void testValidSubClassOverridesCreateInstance() throws Exception
{
    // Test that a subclass that properly overrides createInstance works
    class ProperSubClass extends DefaultPrettyPrinter {
        @Override
        public DefaultPrettyPrinter createInstance() {
            return new ProperSubClass();
        }
    }
    
    ProperSubClass pp = new ProperSubClass();
    DefaultPrettyPrinter instance = pp.createInstance();
    assertNotNull(instance);
    assertTrue(instance instanceof ProperSubClass);
}