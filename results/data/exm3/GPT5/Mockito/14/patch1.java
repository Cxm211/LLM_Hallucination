public <T> T verify(T mock, VerificationMode mode) {
        if (mock == null) {
            reporter.nullPassedToVerify();
        } else if (!mockUtil.isMock(mock)) {
            reporter.notAMockPassedToVerify();
        }
        // Wrap the verification mode with target mock information so that calls to other mocks in the same line
        // don't consume the verification mode.
        VerificationMode wrapped = new VerificationMode() {
            private final VerificationMode delegate = mode;
            @SuppressWarnings("unused")
            private final Object targetMock = mock;
            public void verify(VerificationData data) {
                delegate.verify(data);
            }
            public VerificationMode description(String description) {
                try {
                    java.lang.reflect.Method m = delegate.getClass().getMethod("description", String.class);
                    Object res = m.invoke(delegate, description);
                    if (res instanceof VerificationMode) {
                        return (VerificationMode) res;
                    }
                } catch (Throwable ignored) {
                }
                return this;
            }
            public String toString() {
                return delegate.toString();
            }
        };
        mockingProgress.verificationStarted(wrapped);
        return mock;
    }