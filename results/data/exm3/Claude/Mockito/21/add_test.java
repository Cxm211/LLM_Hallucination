// org/mockito/internal/creation/instance/ConstructorInstantiatorTest.java
@Test public void creates_instances_of_inner_classes_with_grandchild_outer_instance() {
    class GrandchildOfThis extends ChildOfThis {}
    assertEquals(new ConstructorInstantiator(new GrandchildOfThis()).newInstance(SomeInnerClass.class).getClass(), SomeInnerClass.class);
}