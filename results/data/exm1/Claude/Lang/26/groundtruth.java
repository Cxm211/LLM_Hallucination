// ===== FIXED org.apache.commons.lang3.time.FastDateFormat :: format(Date) [lines 819-823] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-26-fixed/src/main/java/org/apache/commons/lang3/time/FastDateFormat.java =====
    public String format(Date date) {
        Calendar c = new GregorianCalendar(mTimeZone, mLocale);
        c.setTime(date);
        return applyRules(c, new StringBuffer(mMaxLengthEstimate)).toString();
    }
