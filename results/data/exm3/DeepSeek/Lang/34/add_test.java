// org/apache/commons/lang3/builder/ToStringBuilderTest.java
public void testGetRegistryInitialization() {
        // Ensure registry is null at start
        this.validateNullToStringStyleRegistry();
        // Get registry and attempt to modify
        Map<Object, Object> registry = org.apache.commons.lang3.builder.ToStringStyle.getRegistry();
        // This will throw UnsupportedOperationException in buggy version
        registry.put("testKey", "testValue");
        assertTrue(registry.containsKey("testKey"));
        // Clean up
        registry.remove("testKey");
        this.validateNullToStringStyleRegistry();
    }
