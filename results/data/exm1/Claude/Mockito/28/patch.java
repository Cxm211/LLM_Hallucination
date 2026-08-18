private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
    for(Class<?> classContext = awaitingInjectionClazz; classContext != Object.class; classContext = classContext.getSuperclass()) {
        for(Field field : orderedInstanceFieldsFrom(classContext)) {
            mockCandidateFilter.filterCandidate(mocks, field, fieldInstance).thenInject();
        }
    }
}