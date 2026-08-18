    public Timer(long durationMillis) {
        if (durationMillis < 0) {
            throw new FriendlyReminderException();
        }
        this.durationMillis = durationMillis;
    }