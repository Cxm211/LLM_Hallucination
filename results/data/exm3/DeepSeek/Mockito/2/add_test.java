// org/mockito/internal/util/TimerTest.java
@Test
    public void should_throw_exception_with_message_when_duration_is_negative() {
        try {
            new Timer(-1000);
            Assert.fail("Should have thrown FriendlyReminderException");
        } catch (FriendlyReminderException e) {
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(e.getMessage().contains("negative"));
        }
    }
