// org/apache/commons/lang3/builder/HashCodeBuilderTest.java
public void testUnregisterWhenRegistryNull() throws Exception {
    // Ensure registry is null
    try {
        java.lang.reflect.Field field = HashCodeBuilder.class.getDeclaredField("REGISTRY");
        field.setAccessible(true);
        java.lang.ThreadLocal<?> registry = (java.lang.ThreadLocal<?>) field.get(null);
        if (registry != null) {
            registry.remove();
        }
    } catch (NoSuchFieldException e1) {
        try {
            java.lang.reflect.Field field = HashCodeBuilder.class.getDeclaredField("registry");
            field.setAccessible(true);
            field.set(null, null);
        } catch (NoSuchFieldException e2) {
            // ignore
        }
    }
    // should not throw exception
    HashCodeBuilder.unregister(new Object());
    assertNull(HashCodeBuilder.getRegistry());
}
