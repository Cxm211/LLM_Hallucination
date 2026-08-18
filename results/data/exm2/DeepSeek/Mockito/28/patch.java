private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
    for (Field field : orderedInstanceFieldsFrom(awaitingInjectionClazz)) {
        Candidate candidate = mockCandidateFilter.filterCandidate(mocks, field, fieldInstance);
        if (candidate != null) {
            candidate.thenInject();
            mocks.remove(candidate.getMock());
        }
    }
}