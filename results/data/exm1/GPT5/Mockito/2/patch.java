public Timer(long durationMillis) {
        if (durationMillis < 0) {
            throw new FriendlyReminderException("Duration must not be negative.");
        }
        this.durationMillis = durationMillis;
    }