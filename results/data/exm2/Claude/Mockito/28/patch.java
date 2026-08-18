private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
    for(Field field : orderedInstanceFieldsFrom(awaitingInjectionClazz)) {
        OngoingMockInjection injection = mockCandidateFilter.filterCandidate(mocks, field, fieldInstance);
        if (injection != null) {
            injection.thenInject();
        }
    }
}