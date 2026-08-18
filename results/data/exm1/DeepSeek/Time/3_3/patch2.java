public void addWeekyears(final int weekyears) {
    DateTimeField field = getChronology().weekyears();
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, weekyears);
    setMillis(field.set(getMillis(), newValue));
}