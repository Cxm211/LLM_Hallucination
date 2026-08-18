// org/mockito/internal/creation/DelegatingMethodTest.java
@Test
    public void equals_should_return_false_when_comparing_with_wrapped_method() throws Exception {
        assertFalse(delegatingMethod.equals(someMethod));
    }
