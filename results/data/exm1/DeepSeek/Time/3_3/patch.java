public void add(DurationFieldType type, int amount) {
    if (type == null) {
        throw new IllegalArgumentException("Field must not be null");
    }
    DateTimeField field = type.getField(getChronology());
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, amount);
    setMillis(field.set(getMillis(), newValue));
}