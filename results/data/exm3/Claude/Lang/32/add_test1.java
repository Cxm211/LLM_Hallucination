// org/apache/commons/lang3/builder/HashCodeBuilderTest.java
public void testReflectionObjectNoCycle() {
    ReflectionTestCycleA a = new ReflectionTestCycleA();
    ReflectionTestCycleB b = new ReflectionTestCycleB();
    a.b = null;
    b.a = null;
    
    a.hashCode();
    assertNull(HashCodeBuilder.getRegistry());
    b.hashCode();
    assertNull(HashCodeBuilder.getRegistry());
}