public void addSeconds(final int seconds) {
        if (seconds == 0) {
            return;
        }
        setMillis(getChronology().seconds().add(getMillis(), seconds));
    }