public void addDays(final int days) {
    DateTimeField field = getChronology().days();
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, days);
    setMillis(field.set(getMillis(), newValue));
}