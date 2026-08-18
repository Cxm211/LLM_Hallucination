// org/mockito/internal/creation/DelegatingMethodTest.java
@Test
    public void hashCode_should_be_consistent_with_equals() throws Exception {
        DelegatingMethod equal = new DelegatingMethod(someMethod);
        assertTrue(delegatingMethod.equals(equal));
        assertEquals(delegatingMethod.hashCode(), equal.hashCode());
    }
