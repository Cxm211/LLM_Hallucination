// buggy function
    public boolean equals(Object o) {
            return method.equals(o);
    }

    public int hashCode() {
        return 1;
    }

// trigger testcase
// org/mockito/internal/creation/DelegatingMethodTest.java::equals_should_return_true_when_equal
@Test
    public void equals_should_return_true_when_equal() throws Exception {
        DelegatingMethod equal = new DelegatingMethod(someMethod);
        assertTrue(delegatingMethod.equals(equal));
    }

// org/mockito/internal/creation/DelegatingMethodTest.java::equals_should_return_true_when_self
@Test
    public void equals_should_return_true_when_self() throws Exception {
        assertTrue(delegatingMethod.equals(delegatingMethod));
    }
