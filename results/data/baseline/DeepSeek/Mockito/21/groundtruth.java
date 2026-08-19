// ===== FIXED org.mockito.internal.creation.instance.ConstructorInstantiator :: newInstance(Class) [lines 13-18] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-21-fixed/src/org/mockito/internal/creation/instance/ConstructorInstantiator.java =====
    public <T> T newInstance(Class<T> cls) {
        if (outerClassInstance == null) {
            return noArgConstructor(cls);
        }
        return withParams(cls, outerClassInstance);
    }

// ===== FIXED org.mockito.internal.creation.instance.ConstructorInstantiator :: paramsException(Class, Exception) [lines 36-39] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-21-fixed/src/org/mockito/internal/creation/instance/ConstructorInstantiator.java =====
    private static <T> InstantationException paramsException(Class<T> cls, Exception e) {
        return new InstantationException("Unable to create mock instance of '"
                + cls.getSimpleName() + "'.\nPlease ensure that the outer instance has correct type and that the target class has parameter-less constructor.", e);
    }
