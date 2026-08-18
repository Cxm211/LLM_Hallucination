private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
        for(Field field : orderedInstanceFieldsFrom(awaitingInjectionClazz)) {
            boolean injected = mockCandidateFilter.filterCandidate(mocks, field, fieldInstance).thenInject();
            if (injected) {
                break;
            }
        }
    }