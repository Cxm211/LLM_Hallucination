	public OngoingInjecter filterCandidate(Collection<Object> mocks,
			Field field, Object fieldInstance) {
		List<Object> mockNameMatches = new ArrayList<Object>();
		for (Object mock : mocks) {
			if (field.getName().equals(mockUtil.getMockName(mock).toString())) {
				mockNameMatches.add(mock);
			}
		}
		return next.filterCandidate(mockNameMatches, field,
				fieldInstance);
	}