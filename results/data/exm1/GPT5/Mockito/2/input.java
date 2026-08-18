// buggy code
    public Timer(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

// relevant test
// org.mockito.internal.util.TimerTest::should_return_true_if_task_is_in_acceptable_time_bounds
    public void should_return_true_if_task_is_in_acceptable_time_bounds() {
        
        long duration = 10000L;
        Timer timer = new Timer(duration);

        
        timer.start();

        
        assertThat(timer.isCounting(), is(true));
    }

// org.mockito.internal.util.TimerTest::should_return_false_when_time_run_out
    public void should_return_false_when_time_run_out() throws Exception {
        
        Timer timer = new Timer(0);
        timer.start();

        
        oneMillisecondPasses();

        
        assertThat(timer.isCounting(), is(false));
    }

// org.mockito.internal.util.TimerTest::should_throw_friendly_reminder_exception_when_duration_is_negative
    public void should_throw_friendly_reminder_exception_when_duration_is_negative() {
        try {
            new Timer(-1);
            Assert.fail("It is forbidden to create timer with negative value of timer's duration.");
        } catch (FriendlyReminderException e) {
            Assert.assertTrue(true);
        }
    }

// org.mockito.internal.verification.VerificationOverTimeImplTest::should_return_on_success
    public void should_return_on_success() {
        impl.verify(null);
        verify(delegate).verify(null);
    }

// org.mockito.internal.verification.VerificationOverTimeImplTest::should_throw_mockito_assertion_error
    public void should_throw_mockito_assertion_error() {
        MockitoAssertionError toBeThrown = new MockitoAssertionError("message");
        exception.expect(is(toBeThrown));

        doThrow(toBeThrown).when(delegate).verify(null);
        impl.verify(null);
    }

// org.mockito.internal.verification.VerificationOverTimeImplTest::should_deal_with_junit_assertion_error
    public void should_deal_with_junit_assertion_error() {
        ArgumentsAreDifferent toBeThrown = new ArgumentsAreDifferent("message", "wanted", "actual");
        exception.expect(is(toBeThrown));
        exception.expectMessage("message");

        doThrow(toBeThrown).when(delegate).verify(null);
        impl.verify(null);
    }

// org.mockito.internal.verification.VerificationOverTimeImplTest::should_not_wrap_other_exceptions
    public void should_not_wrap_other_exceptions() {
        RuntimeException toBeThrown = new RuntimeException();
        exception.expect(is(toBeThrown));

        doThrow(toBeThrown).when(delegate).verify(null);
        impl.verify(null);
    }

// org.mockito.verification.NegativeDurationTest::should_throw_exception_when_duration_is_negative_for_timeout_method
    public void should_throw_exception_when_duration_is_negative_for_timeout_method() {
        try {
            Mockito.timeout(-1);
            Assert.fail("It is forbidden to invoke Mockito.timeout() with negative value.");
        } catch (FriendlyReminderException e) {
            Assert.assertTrue(true);
        }
    }

// org.mockito.verification.NegativeDurationTest::should_throw_exception_when_duration_is_negative_for_after_method
    public void should_throw_exception_when_duration_is_negative_for_after_method() {
        try {
            Mockito.after(-1);
            Assert.fail("It is forbidden to invoke Mockito.after() with negative value.");
        } catch (FriendlyReminderException e) {
            Assert.assertTrue(true);
        }
    }

// org.mockito.verification.TimeoutTest::should_pass_when_verification_passes
    public void should_pass_when_verification_passes() {
        Timeout t = new Timeout(1, 3, mode, timer);

        when(timer.isCounting()).thenReturn(true);
        doNothing().when(mode).verify(data);

        t.verify(data);

        InOrder inOrder = inOrder(timer);
        inOrder.verify(timer).start();
        inOrder.verify(timer).isCounting();
    }

// org.mockito.verification.TimeoutTest::should_fail_because_verification_fails
    public void should_fail_because_verification_fails() {
        Timeout t = new Timeout(1, 2, mode, timer);

        when(timer.isCounting()).thenReturn(true, true, true, false);
        doThrow(error).
        doThrow(error).
        doThrow(error).
        when(mode).verify(data);
        
        try {
            t.verify(data);
            fail();
        } catch (MockitoAssertionError e) {}

        verify(timer, times(4)).isCounting();
    }

// org.mockito.verification.TimeoutTest::should_pass_even_if_first_verification_fails
    public void should_pass_even_if_first_verification_fails() {}

// org.mockito.verification.TimeoutTest::should_try_to_verify_correct_number_of_times
    public void should_try_to_verify_correct_number_of_times() {}

// org.mockito.verification.TimeoutTest::should_create_correctly_configured_timeout
    public void should_create_correctly_configured_timeout() {
        Timeout t = new Timeout(25, 50, mode, timer);
        
        assertTimeoutCorrectlyConfigured(t.atLeastOnce(), Timeout.class, 50, 25, AtLeast.class);
        assertTimeoutCorrectlyConfigured(t.atLeast(5), Timeout.class, 50, 25, AtLeast.class);
        assertTimeoutCorrectlyConfigured(t.times(5), Timeout.class, 50, 25, Times.class);
        assertTimeoutCorrectlyConfigured(t.only(), Timeout.class, 50, 25, Only.class);
    }

// org.mockitointegration.NoJUnitDependenciesTest::pure_mockito_should_not_depend_JUnit
    public void pure_mockito_should_not_depend_JUnit() throws Exception {
        ClassLoader classLoader_without_JUnit = ClassLoaders.excludingClassLoader()
                .withCodeSourceUrlOf(
                        Mockito.class,
                        Matcher.class,
                        Enhancer.class,
                        Objenesis.class
                )
                .without("junit", "org.junit")
                .build();

        Set<String> pureMockitoAPIClasses = ClassLoaders.in(classLoader_without_JUnit).omit("runners", "junit", "JUnit").listOwnedClasses();

        for (String pureMockitoAPIClass : pureMockitoAPIClasses) {
            checkDependency(classLoader_without_JUnit, pureMockitoAPIClass);
        }
    }

// org.mockitousage.bugs.ConcurrentModificationExceptionOnMultiThreadedVerificationTest::shouldSuccessfullyVerifyConcurrentInvocationsWithTimeout
	public void shouldSuccessfullyVerifyConcurrentInvocationsWithTimeout() throws Exception {
        int potentialOverhead = 1000; 
        int expectedMaxTestLength = TIMES * INTERVAL_MILLIS + potentialOverhead;

		reset(target);
		startInvocations();
		
		verify(target, timeout(expectedMaxTestLength).times(TIMES * nThreads)).targetMethod("arg");
		verifyNoMoreInteractions(target);
	}

// org.mockitousage.bugs.TimeoutWithAtMostOrNeverShouldBeDisabledTest::shouldDisableTimeoutForAtMost
    public void shouldDisableTimeoutForAtMost() {
        try {
            verify(mock, timeout(30000).atMost(1)).simpleMethod();
            fail();
        } catch (FriendlyReminderException e) {}
    }

// org.mockitousage.bugs.TimeoutWithAtMostOrNeverShouldBeDisabledTest::shouldDisableTimeoutForNever
    public void shouldDisableTimeoutForNever() {
        try {
            verify(mock, timeout(30000).never()).simpleMethod();
            fail();
        } catch (FriendlyReminderException e) {}
    }

// org.mockitousage.verification.VerificationAfterDelayTest::shouldVerifyNormallyWithSpecificTimes
    public void shouldVerifyNormallyWithSpecificTimes() {}

// org.mockitousage.verification.VerificationAfterDelayTest::shouldVerifyNormallyWithAtLeast
    public void shouldVerifyNormallyWithAtLeast() {}

// org.mockitousage.verification.VerificationAfterDelayTest::shouldFailVerificationWithWrongTimes
    public void shouldFailVerificationWithWrongTimes() throws Exception {
        
        Thread t = waitAndExerciseMock(20);

        
        t.start();

        
        verify(mock, times(0)).clear();
        
        expected.expect(MockitoAssertionError.class);
        verify(mock, after(50).times(2)).clear();
    }

// org.mockitousage.verification.VerificationAfterDelayTest::shouldWaitTheFullTimeIfTheTestCouldPass
    public void shouldWaitTheFullTimeIfTheTestCouldPass() throws Exception {
        
        Thread t = waitAndExerciseMock(50);

        
        t.start();

        
        long startTime = System.currentTimeMillis();
        
        try {
            verify(mock, after(100).atLeast(2)).clear();
            fail();
        } catch (MockitoAssertionError e) {}
        
        assertTrue(System.currentTimeMillis() - startTime >= 100);
    }

// org.mockitousage.verification.VerificationAfterDelayTest::shouldStopEarlyIfTestIsDefinitelyFailed
    public void shouldStopEarlyIfTestIsDefinitelyFailed() throws Exception {
        
        Thread t = waitAndExerciseMock(50);
        
        
        t.start();
        
        
        expected.expect(MockitoAssertionError.class);
        verify(mock, after(10000).never()).clear();
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
