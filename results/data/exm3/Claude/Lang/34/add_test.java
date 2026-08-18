// org/apache/commons/lang3/builder/ToStringBuilderTest.java
public void testRegistryInitialization() {
        // Test that getRegistry properly initializes when REGISTRY.get() is null
        // This test verifies the fix handles the case where the registry hasn't been set yet
        
        // Create a simple object to register
        Object testObj = new Object();
        
        // The first call to getRegistry should initialize the registry
        Map<Object, Object> registry = getRegistry();
        assertNotNull("Registry should not be null", registry);
        
        // Verify it's not an empty immutable map by attempting to put
        registry.put(testObj, testObj);
        assertTrue("Registry should contain the test object", registry.containsKey(testObj));
        
        // Verify isRegistered works after initialization
        assertTrue("isRegistered should return true for registered object", isRegistered(testObj));
        
        // Clean up
        registry.remove(testObj);
        validateNullToStringStyleRegistry();
    }