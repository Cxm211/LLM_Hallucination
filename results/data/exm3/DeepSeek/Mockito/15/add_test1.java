// org/mockitousage/bugs/InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest.java
@Test
    public void shouldInjectWhenMultipleMocksAndOneNameMatches() throws Exception {
        class Target {
            private String foo;
            public void setFoo(String foo) {
                this.foo = foo;
            }
        }
        Field field = Target.class.getDeclaredField("foo");
        Target target = new Target();
        
        Object matchingMock = org.mockito.Mockito.mock(String.class, "foo");
        Object nonMatchingMock = org.mockito.Mockito.mock(String.class, "bar");
        
        java.util.Collection<Object> mocks = java.util.Arrays.asList(matchingMock, nonMatchingMock);
        
        org.mockito.internal.configuration.injection.filter.NameBasedCandidateFilter filter = 
            new org.mockito.internal.configuration.injection.filter.NameBasedCandidateFilter();
        org.mockito.internal.configuration.injection.filter.OngoingInjecter injecter = 
            filter.filterCandidate(mocks, field, target);
        
        assertTrue(injecter.thenInject());
        field.setAccessible(true);
        assertSame(matchingMock, field.get(target));
    }
