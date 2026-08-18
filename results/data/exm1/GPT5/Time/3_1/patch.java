public void add(DurationFieldType type, int amount) {
        if (type == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
        if (amount == 0) {
            return;
        }
        DateTimeField field = type.getField(getChronology());
        if (field == null || !field.isSupported()) {
            throw new IllegalArgumentException("Field is not supported");
        }
        setMillis(field.add(getMillis(), amount));
    }