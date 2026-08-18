// org/mockito/internal/creation/instance/ConstructorInstantiatorTest.java::creates_instances_of_private_inner_classes
@Test public void creates_instances_of_private_inner_classes() {
        assertEquals(new ConstructorInstantiator(this).newInstance(PrivateInner.class).getClass(), PrivateInner.class);
    }

    private class PrivateInner { }