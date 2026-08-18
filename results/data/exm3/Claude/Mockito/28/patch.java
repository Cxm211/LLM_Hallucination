private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
    for(Field field : orderedInstanceFieldsFrom(awaitingInjectionClazz)) {
        OngoingMockInjection injection = mockCandidateFilter.filterCandidate(mocks, field, fieldInstance);
        Object injectedMock = injection.thenInject();
        if(injectedMock != null) {
            mocks.remove(injectedMock);
        }
    }
}