// com/google/gson/internal/UnsafeAllocatorInstantiationTest.java
public void testConcreteClassInstantiation() throws Exception {
    UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
    Object instance = unsafeAllocator.newInstance(Object.class);
    assertNotNull(instance);
    assertTrue(instance instanceof Object);
  }