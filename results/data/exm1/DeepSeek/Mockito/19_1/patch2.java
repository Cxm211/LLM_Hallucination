    public OngoingInjecter filterCandidate(Collection<Object> mocks,
            Field field, Object fieldInstance) {
        List<Object> mockNameMatches = new ArrayList<Object>();
        for (Object mock : mocks) {
            if (field.getName().equals(mockUtil.getMockName(mock).toString())) {
                mockNameMatches.add(mock);
            }
        }
        if (!mockNameMatches.isEmpty()) {
            return next.filterCandidate(mockNameMatches, field, fieldInstance);
        }
        // Fallback: if no mock matches by name, pass the original mocks to the next filter
        return next.filterCandidate(mocks, field, fieldInstance);
    }