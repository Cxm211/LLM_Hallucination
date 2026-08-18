public StringBuffer format(Calendar calendar, StringBuffer buf) {
        if (mTimeZoneForced) {
            long millis = calendar.getTimeInMillis();
            Calendar newCal = Calendar.getInstance(mTimeZone);
            newCal.setTimeInMillis(millis);
            calendar = newCal;
        }
        return applyRules(calendar, buf);
    }