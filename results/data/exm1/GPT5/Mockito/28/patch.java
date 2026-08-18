private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
        OngoingInjecter ongoing = null;
        for (Field field : orderedInstanceFieldsFrom(awaitingInjectionClazz)) {
            ongoing = mockCandidateFilter.filterCandidate(mocks, field, fieldInstance);
        }
        if (ongoing != null) {
            ongoing.thenInject();
        }
    }