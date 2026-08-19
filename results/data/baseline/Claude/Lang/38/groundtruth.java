// ===== FIXED org.apache.commons.lang3.time.FastDateFormat :: format(Calendar, StringBuffer) [lines 870-877] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-38-fixed/src/java/org/apache/commons/lang3/time/FastDateFormat.java =====
    public StringBuffer format(Calendar calendar, StringBuffer buf) {
        if (mTimeZoneForced) {
            calendar.getTime(); /// LANG-538
            calendar = (Calendar) calendar.clone();
            calendar.setTimeZone(mTimeZone);
        }
        return applyRules(calendar, buf);
    }
