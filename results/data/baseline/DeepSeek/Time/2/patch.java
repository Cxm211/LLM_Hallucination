public int compareTo(DurationField durationField) {
    if (iTypes.length == 0) {
        return 0;
    }
    DateTimeFieldType type = iTypes[0];
    DurationField thisField = type.getDurationType().getField(iChronology);
    return thisField.compareTo(durationField);
}