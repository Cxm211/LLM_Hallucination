// org/apache/commons/lang3/builder/HashCodeBuilderTest.java
public void testRegistryClearedAfterSingleObject() throws Exception {
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
    Object obj = new Object();
    HashCodeBuilder.register(obj);
    assertNotNull(HashCodeBuilder.getRegistry());
    assertTrue(HashCodeBuilder.isRegistered(obj));
    HashCodeBuilder.unregister(obj);
    assertNull(HashCodeBuilder.getRegistry());
}
