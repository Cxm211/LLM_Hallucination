    public <T> T verify(T mock, VerificationMode mode) {
        if (mock == null) {
            reporter.nullPassedToVerify();
            throw new NullPointerException("Mock cannot be null");
        } else if (!mockUtil.isMock(mock)) {
            reporter.notAMockPassedToVerify();
        }
        mockingProgress.verificationStarted(mode);
        return mock;
    }