public void add(DurationFieldType type, int amount) {
    if (type == null) {
        throw new IllegalArgumentException("Field must not be null");
    }
    DurationField field = type.getField(getChronology());
    if (field == null) {
        throw new IllegalArgumentException("Field not supported");
    }
    setMillis(field.add(getMillis(), amount));
}