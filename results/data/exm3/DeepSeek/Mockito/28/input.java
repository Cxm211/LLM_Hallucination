// buggy function
    private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
        for(Field field : orderedInstanceFieldsFrom(awaitingInjectionClazz)) {
            mockCandidateFilter.filterCandidate(mocks, field, fieldInstance).thenInject();
        }
    }

// trigger testcase
// org/mockitousage/bugs/InjectionByTypeShouldFirstLookForExactTypeThenAncestorTest.java::mock_should_be_injected_once_and_in_the_best_matching_type
@Test
    public void mock_should_be_injected_once_and_in_the_best_matching_type() {
        assertSame(REFERENCE, illegalInjectionExample.mockShouldNotGoInHere);
        assertSame(mockedBean, illegalInjectionExample.mockShouldGoInHere);
    }
