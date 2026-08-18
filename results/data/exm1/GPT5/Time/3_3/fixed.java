// ===== FIXED org.joda.time.MutableDateTime :: add(DurationFieldType, int) [lines 635-642] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void add(DurationFieldType type, int amount) {
        if (type == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
        if (amount != 0) {
            setMillis(type.getField(getChronology()).add(getMillis(), amount));
        }
    }

// ===== FIXED org.joda.time.MutableDateTime :: addDays(int) [lines 773-777] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void addDays(final int days) {
        if (days != 0) {
            setMillis(getChronology().days().add(getMillis(), days));
        }
    }

// ===== FIXED org.joda.time.MutableDateTime :: addHours(int) [lines 796-800] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void addHours(final int hours) {
        if (hours != 0) {
            setMillis(getChronology().hours().add(getMillis(), hours));
        }
    }

// ===== FIXED org.joda.time.MutableDateTime :: addMillis(int) [lines 897-901] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void addMillis(final int millis) {
        if (millis != 0) {
            setMillis(getChronology().millis().add(getMillis(), millis));
        }
    }

// ===== FIXED org.joda.time.MutableDateTime :: addMinutes(int) [lines 829-833] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void addMinutes(final int minutes) {
        if (minutes != 0) {
            setMillis(getChronology().minutes().add(getMillis(), minutes));
        }
    }

// ===== FIXED org.joda.time.MutableDateTime :: addMonths(int) [lines 707-711] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void addMonths(final int months) {
        if (months != 0) {
            setMillis(getChronology().months().add(getMillis(), months));
        }
    }

// ===== FIXED org.joda.time.MutableDateTime :: addSeconds(int) [lines 862-866] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void addSeconds(final int seconds) {
        if (seconds != 0) {
            setMillis(getChronology().seconds().add(getMillis(), seconds));
        }
    }

// ===== FIXED org.joda.time.MutableDateTime :: addWeeks(int) [lines 730-734] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void addWeeks(final int weeks) {
        if (weeks != 0) {
            setMillis(getChronology().weeks().add(getMillis(), weeks));
        }
    }

// ===== FIXED org.joda.time.MutableDateTime :: addWeekyears(int) [lines 684-688] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void addWeekyears(final int weekyears) {
        if (weekyears != 0) {
            setMillis(getChronology().weekyears().add(getMillis(), weekyears));
        }
    }

// ===== FIXED org.joda.time.MutableDateTime :: addYears(int) [lines 661-665] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-3-fixed/src/main/java/org/joda/time/MutableDateTime.java =====
    public void addYears(final int years) {
        if (years != 0) {
            setMillis(getChronology().years().add(getMillis(), years));
        }
    }
