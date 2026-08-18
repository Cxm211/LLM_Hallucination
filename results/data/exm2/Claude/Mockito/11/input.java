    public boolean equals(Object o) {
            return method.equals(o);
    }

    public int hashCode() {
        return 1;
    }

// trigger testcase
@Test
    public void equals_should_return_true_when_equal() throws Exception {
        DelegatingMethod equal = new DelegatingMethod(someMethod);
        assertTrue(delegatingMethod.equals(equal));
    }

@Test
    public void equals_should_return_true_when_self() throws Exception {
        assertTrue(delegatingMethod.equals(delegatingMethod));
    }
