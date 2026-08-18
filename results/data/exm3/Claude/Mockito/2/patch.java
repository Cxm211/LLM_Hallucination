public Timer(long durationMillis) {
    if (durationMillis < 0) {
        throw new FriendlyReminderException(
            "Don't panic! I'm just a friendly reminder!",
            "It looks like you are trying to use Mockito in a wrong way.\n" +
            "You cannot create a Timer with negative duration."
        );
    }
    this.durationMillis = durationMillis;
}
