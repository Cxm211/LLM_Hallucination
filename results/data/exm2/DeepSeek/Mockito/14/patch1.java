    public <T> T verify(T mock, VerificationMode mode) {
        if (mock == null) {
            reporter.nullPassedToVerify();
            throw new NullPointerException();
        } else if (!mockUtil.isMock(mock)) {
            reporter.notAMockPassedToVerify();
            throw new ClassCastException();
        }
        mockingProgress.setMockToVerify(mock);
        mockingProgress.verificationStarted(mode);
        return mock;
    }