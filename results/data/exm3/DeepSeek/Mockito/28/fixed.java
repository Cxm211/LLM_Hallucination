// ===== FIXED org.mockito.internal.configuration.DefaultInjectionEngine :: injectMockCandidate(Class, Set, Object) [lines 91-96] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-28-fixed/src/org/mockito/internal/configuration/DefaultInjectionEngine.java =====
    private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
        for(Field field : orderedInstanceFieldsFrom(awaitingInjectionClazz)) {
            Object injected = mockCandidateFilter.filterCandidate(mocks, field, fieldInstance).thenInject();
            mocks.remove(injected);
        }
    }
