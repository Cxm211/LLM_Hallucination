// com/google/gson/internal/UnsafeAllocatorInstantiationTest.java
public void testInterfaceInstantiationMessage() {
    UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
    try {
      unsafeAllocator.newInstance(Interface.class);
      fail();
    } catch (UnsupportedOperationException e) {
      assertEquals("Cannot allocate " + Interface.class.toString(), e.getMessage());
    }
  }
