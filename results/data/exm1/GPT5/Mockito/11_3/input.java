// buggy code
    public boolean equals(Object o) {
            return method.equals(o);
    }

    public int hashCode() {
        return 1;
    }

// relevant test
// org.mockitousage.verification.VerificationUsingMatchersTest::shouldVerifyUsingMixedMatchers
    public void shouldVerifyUsingMixedMatchers() {
        mock.threeArgumentMethod(11, "", "01234");

        try {
            verify(mock).threeArgumentMethod(and(geq(7), leq(10)), isA(String.class), Matchers.contains("123"));
            fail();
        } catch (ArgumentsAreDifferent e) {}

        mock.threeArgumentMethod(8, new Object(), "01234");
        
        try {
            verify(mock).threeArgumentMethod(and(geq(7), leq(10)), isA(String.class), Matchers.contains("123"));
            fail();
        } catch (ArgumentsAreDifferent e) {}
        
        mock.threeArgumentMethod(8, "", "no match");

        try {
            verify(mock).threeArgumentMethod(and(geq(7), leq(10)), isA(String.class), Matchers.contains("123"));
            fail();
        } catch (ArgumentsAreDifferent e) {}
        
        mock.threeArgumentMethod(8, "", "123");
        
        verify(mock).threeArgumentMethod(and(geq(7), leq(10)), isA(String.class), Matchers.contains("123"));
    }

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldVerifyWithTimeout
    public void shouldVerifyWithTimeout() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldFailVerificationWithTimeout
    public void shouldFailVerificationWithTimeout() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowMixingOtherModesWithTimeout
    public void shouldAllowMixingOtherModesWithTimeout() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowMixingOtherModesWithTimeoutAndFail
    public void shouldAllowMixingOtherModesWithTimeoutAndFail() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowMixingOnlyWithTimeout
    public void shouldAllowMixingOnlyWithTimeout() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowMixingOnlyWithTimeoutAndFail
    public void shouldAllowMixingOnlyWithTimeoutAndFail() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::canIgnoreInvocationsWithJunit
    public void canIgnoreInvocationsWithJunit() {
        
        Thread t1 = new Thread() {
            @Override
            public void run() {
                mock.add("0");
                mock.add("1");
                VerificationWithTimeoutTest.this.sleep(100);
                mock.add("2");
            }
        };

        
        t1.start();

        
        verify(mock, timeout(200)).add("1");
        verify(mock, timeout(200)).add("2");
    }

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowTimeoutVerificationInOrder
    public void shouldAllowTimeoutVerificationInOrder() throws Exception {
        
        Thread t1 = waitAndExerciseMock(20);

        
        t1.start();
        mock.add("foo");

        
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).add(anyString());
        inOrder.verify(mock, never()).clear();
        inOrder.verify(mock, timeout(40)).clear();
    }
