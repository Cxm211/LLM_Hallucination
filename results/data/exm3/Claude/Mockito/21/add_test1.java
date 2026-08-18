// org/mockito/internal/creation/instance/ConstructorInstantiatorTest.java
@Test public void creates_instances_of_inner_classes_with_direct_outer_instance() {
    ConstructorInstantiatorTest outer = new ConstructorInstantiatorTest();
    assertEquals(new ConstructorInstantiator(outer).newInstance(SomeInnerClass.class).getClass(), SomeInnerClass.class);
}