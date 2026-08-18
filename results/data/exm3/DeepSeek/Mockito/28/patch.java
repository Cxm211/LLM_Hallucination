    private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
        List<Field> fields = new ArrayList<>(orderedInstanceFieldsFrom(awaitingInjectionClazz));
        // Sort fields so that more specific types (deeper inheritance) come first
        fields.sort((f1, f2) -> {
            Class<?> c1 = f1.getType();
            int depth1 = 0;
            while (c1 != null) {
                depth1++;
                c1 = c1.getSuperclass();
            }
            Class<?> c2 = f2.getType();
            int depth2 = 0;
            while (c2 != null) {
                depth2++;
                c2 = c2.getSuperclass();
            }
            return Integer.compare(depth2, depth1);
        });
        Set<Object> remainingMocks = new HashSet<>(mocks);
        for (Field field : fields) {
            Object candidate = mockCandidateFilter.filterCandidate(remainingMocks, field, fieldInstance);
            // candidate is an instance of org.mockito.internal.configuration.injection.MockCandidate
            try {
                Field injectableMockField = candidate.getClass().getDeclaredField("injectableMock");
                injectableMockField.setAccessible(true);
                Object injectedMock = injectableMockField.get(candidate);
                candidate.thenInject();
                remainingMocks.remove(injectedMock);
            } catch (Exception e) {
                // fallback to original behavior if reflection fails
                candidate.thenInject();
            }
        }
    }