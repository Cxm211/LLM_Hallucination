// com/google/gson/internal/UnsafeAllocatorInstantiationTest.java
public void testAbstractClassInstantiationMessage() {
    UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
    try {
      unsafeAllocator.newInstance(AbstractClass.class);
      fail();
    } catch (UnsupportedOperationException e) {
      assertEquals("Cannot allocate " + AbstractClass.class.toString(), e.getMessage());
    }
  }
