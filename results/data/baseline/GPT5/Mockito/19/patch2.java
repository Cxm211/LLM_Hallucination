public OngoingInjecter filterCandidateByName(Collection<Object> mocks,
            Field field, Object fieldInstance) {
        List<Object> mockNameMatches = new ArrayList<Object>();
        if (mocks.size() > 1) {
            for (Object mock : mocks) {
                if (field.getName().equals(mockUtil.getMockName(mock).toString())) {
                    mockNameMatches.add(mock);
                }
            }
            return filterCandidate(mockNameMatches, field,
                    fieldInstance);
        }
        return filterCandidate(mocks, field, fieldInstance);
    }