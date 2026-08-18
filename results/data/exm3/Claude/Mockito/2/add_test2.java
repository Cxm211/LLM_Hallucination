// org/mockito/internal/util/TimerTest.java
@Test
public void should_throw_friendly_reminder_exception_when_duration_is_large_negative() {
    try {
        new Timer(-1000);
        Assert.fail("It is forbidden to create timer with negative value of timer's duration.");
    } catch (FriendlyReminderException e) {
        Assert.assertTrue(true);
    }
}