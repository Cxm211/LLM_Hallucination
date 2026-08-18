    public Timer(long durationMillis) {
        if (durationMillis < 0) {
            throw new FriendlyReminderException("It is forbidden to create timer with negative value of timer's duration.");
        }
        this.durationMillis = durationMillis;
    }