public Week(Date time, TimeZone zone) {
    TimeZone effectiveZone = (zone != null) ? zone : RegularTimePeriod.DEFAULT_TIME_ZONE;
    this(time, effectiveZone, Locale.getDefault());
}