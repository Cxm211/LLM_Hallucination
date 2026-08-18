// org/apache/commons/lang3/builder/HashCodeBuilderTest.java
public void testReflectionObjectCycleMultiple() {
    ReflectionTestCycleA a1 = new ReflectionTestCycleA();
    ReflectionTestCycleB b1 = new ReflectionTestCycleB();
    a1.b = b1;
    b1.a = a1;
    
    ReflectionTestCycleA a2 = new ReflectionTestCycleA();
    ReflectionTestCycleB b2 = new ReflectionTestCycleB();
    a2.b = b2;
    b2.a = a2;
    
    a1.hashCode();
    assertNull(HashCodeBuilder.getRegistry());
    a2.hashCode();
    assertNull(HashCodeBuilder.getRegistry());
    b1.hashCode();
    assertNull(HashCodeBuilder.getRegistry());
    b2.hashCode();
    assertNull(HashCodeBuilder.getRegistry());
}