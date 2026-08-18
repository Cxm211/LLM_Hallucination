OngoingInjecter filterCandidateAlias(
            Collection<Object> mocks,
            Field fieldToBeInjected,
            Object fieldInstance
    ) {
        return filterCandidate(mocks, fieldToBeInjected, fieldInstance);
    }