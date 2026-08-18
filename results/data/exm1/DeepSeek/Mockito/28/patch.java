private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
    for(Field field : orderedInstanceFieldsFrom(awaitingInjectionClazz)) {
        Object candidate = mockCandidateFilter.filterCandidate(mocks, field, fieldInstance);
        if (candidate != null) {
            candidate.thenInject();
        }
    }
}