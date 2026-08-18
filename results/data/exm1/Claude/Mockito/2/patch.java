public Timer(long durationMillis) {
        if (durationMillis < 0) {
            throw new FriendlyReminderException(
                "Don't panic! It is ok. This exception is part of the design, timer duration cannot be negative. ");
        }
        this.durationMillis = durationMillis;
    }