    public String format(Date date) {
        Calendar c = new GregorianCalendar(mTimeZone);
        c.setTime(date);
        if (mLocale != null) {
            Calendar localeCal = Calendar.getInstance(mTimeZone, mLocale);
            c.setFirstDayOfWeek(localeCal.getFirstDayOfWeek());
            c.setMinimalDaysInFirstWeek(localeCal.getMinimalDaysInFirstWeek());
        }
        return applyRules(c, new StringBuffer(mMaxLengthEstimate)).toString();
    }