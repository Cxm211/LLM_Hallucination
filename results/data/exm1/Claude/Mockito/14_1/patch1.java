public <T> T verify(T mock, VerificationMode mode) {
        if (mock == null) {
            reporter.nullPassedToVerify();
        }
        if (!mockUtil.isMock(mock)) {
            reporter.notAMockPassedToVerify();
        }
        mockingProgress.verificationStarted(mode);
        return mock;
    }