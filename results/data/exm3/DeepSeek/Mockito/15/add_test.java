// org/mockitousage/bugs/InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest.java
@Test
    public void shouldNotInjectWhenOnlyMockNameDoesNotMatch() throws Exception {
        class Target {
            private String foo;
            public void setFoo(String foo) {
                this.foo = foo;
            }
        }
        Field field = Target.class.getDeclaredField("foo");
        Target target = new Target();
        
        Object mock = org.mockito.Mockito.mock(String.class, "bar");
        
        java.util.Collection<Object> mocks = java.util.Collections.singleton(mock);
        
        org.mockito.internal.configuration.injection.filter.NameBasedCandidateFilter filter = 
            new org.mockito.internal.configuration.injection.filter.NameBasedCandidateFilter();
        org.mockito.internal.configuration.injection.filter.OngoingInjecter injecter = 
            filter.filterCandidate(mocks, field, target);
        
        assertFalse(injecter.thenInject());
        field.setAccessible(true);
        assertNull(field.get(target));
    }
