    private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
        for(Field field : orderedInstanceFieldsFrom(awaitingInjectionClazz)) {
            Object ongoingInjector = mockCandidateFilter.filterCandidate(mocks, field, fieldInstance);
            if (ongoingInjector != null) {
                ongoingInjector.thenInject();
            }
        }
    }