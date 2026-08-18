public Timer(long durationMillis) {
        if (durationMillis < 0) {
            throw new FriendlyReminderException("duration must not be negative");
        }
        this.durationMillis = durationMillis;
    }