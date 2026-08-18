// org/mockito/verification/NegativeDurationTest.java::should_throw_exception_when_duration_is_negative_for_timeout_method
@Test
    public void should_throw_exception_when_duration_is_extremely_negative_for_timeout_method() {
        try {
            Mockito.timeout(Integer.MIN_VALUE);
            Assert.fail("It is forbidden to invoke Mockito.timeout() with negative value.");
        } catch (FriendlyReminderException e) {
            Assert.assertTrue(true);
        }
    }