    public Timer(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

// trigger testcase
@Test
    public void should_throw_friendly_reminder_exception_when_duration_is_negative() {
        try {
            new Timer(-1);
            Assert.fail("It is forbidden to create timer with negative value of timer's duration.");
        } catch (FriendlyReminderException e) {
            Assert.assertTrue(true);
        }
    }

@Test
    public void should_throw_exception_when_duration_is_negative_for_after_method() {
        try {
            Mockito.after(-1);
            Assert.fail("It is forbidden to invoke Mockito.after() with negative value.");
        } catch (FriendlyReminderException e) {
            Assert.assertTrue(true);
        }
    }

@Test
    public void should_throw_exception_when_duration_is_negative_for_timeout_method() {
        try {
            Mockito.timeout(-1);
            Assert.fail("It is forbidden to invoke Mockito.timeout() with negative value.");
        } catch (FriendlyReminderException e) {
            Assert.assertTrue(true);
        }
    }
