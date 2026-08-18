// ===== FIXED org.joda.time.field.LenientDateTimeField :: set(long, int) [lines 69-76] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-26-fixed/src/main/java/org/joda/time/field/LenientDateTimeField.java =====
    public long set(long instant, int value) {
        // lenient needs to handle time zone chronologies
        // so we do the calculation using local milliseconds
        long localInstant = iBase.getZone().convertUTCToLocal(instant);
        long difference = FieldUtils.safeSubtract(value, get(instant));
        localInstant = getType().getField(iBase.withUTC()).add(localInstant, difference);
        return iBase.getZone().convertLocalToUTC(localInstant, false, instant);
    }
