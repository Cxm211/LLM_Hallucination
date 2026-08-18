private void injectMockCandidate(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object fieldInstance) {
        java.util.List<java.lang.reflect.Field> fields = new java.util.ArrayList<java.lang.reflect.Field>(orderedInstanceFieldsFrom(awaitingInjectionClazz));
        // Sort fields so that more specific types come before their supertypes
        fields.sort(new java.util.Comparator<java.lang.reflect.Field>() {
            @Override
            public int compare(java.lang.reflect.Field f1, java.lang.reflect.Field f2) {
                Class<?> t1 = f1.getType();
                Class<?> t2 = f2.getType();
                if (t1 == t2) {
                    return 0;
                }
                // If t1 is a supertype of t2, then t2 is more specific -> t2 should come first
                if (t1.isAssignableFrom(t2)) {
                    return 1;
                }
                // If t2 is a supertype of t1, then t1 is more specific -> t1 should come first
                if (t2.isAssignableFrom(t1)) {
                    return -1;
                }
                // Unrelated types - keep original relative order (stable sort)
                return 0;
            }
        });
        for (java.lang.reflect.Field field : fields) {
            mockCandidateFilter.filterCandidate(mocks, field, fieldInstance).thenInject();
        }
    }