public void addYears(final int years) {
    DateTimeField field = getChronology().years();
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, years);
    setMillis(field.set(getMillis(), newValue));
}