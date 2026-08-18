// org/apache/commons/lang3/builder/HashCodeBuilderTest.java
public void testReflectionObjectDeepCycle() {
    ReflectionTestCycleA a = new ReflectionTestCycleA();
    ReflectionTestCycleB b = new ReflectionTestCycleB();
    ReflectionTestCycleA a2 = new ReflectionTestCycleA();
    a.b = b;
    b.a = a2;
    a2.b = b;
    
    a.hashCode();
    assertNull(HashCodeBuilder.getRegistry());
    b.hashCode();
    assertNull(HashCodeBuilder.getRegistry());
    a2.hashCode();
    assertNull(HashCodeBuilder.getRegistry());
}