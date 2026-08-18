	public OngoingInjecter filterCandidate(Collection<Object> mocks,
			Field field, Object fieldInstance) {
		List<Object> mockNameMatches = new ArrayList<Object>();
		for (Object mock : mocks) {
			Object mockNameObj = mockUtil.getMockName(mock);
			if (mockNameObj != null && field.getName().equals(mockNameObj.toString())) {
				mockNameMatches.add(mock);
			}
		}
		return next.filterCandidate(mockNameMatches, field,
				fieldInstance);
	}