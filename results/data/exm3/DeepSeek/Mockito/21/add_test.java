// org/mockito/internal/creation/instance/ConstructorInstantiatorTest.java
@Test
    public void creates_instance_of_private_local_inner_class_with_subclass_outer() {
        class PrivateLocalInner {}
        Object instance = new ConstructorInstantiator(new ChildOfThis()).newInstance(PrivateLocalInner.class);
        assertEquals(PrivateLocalInner.class, instance.getClass());
    }
