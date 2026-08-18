public List getValues(final Option option,
                      List defaultValues) {
    // initialize the return list
    List valueList = (List) values.get(option);

    // determine the default values source
    List optDefaults = defaultValues;
    if ((optDefaults == null) || optDefaults.isEmpty()) {
        optDefaults = (List) this.defaultValues.get(option);
    }

    // if no values provided, fall back to defaults (if any)
    if (valueList == null || valueList.isEmpty()) {
        return optDefaults == null ? Collections.EMPTY_LIST : optDefaults;
    }

    // augment the provided values with defaults by overlaying provided values
    if (optDefaults != null && !optDefaults.isEmpty()) {
        List merged = new ArrayList(optDefaults);
        for (int i = 0; i < valueList.size(); i++) {
            if (i < merged.size()) {
                merged.set(i, valueList.get(i));
            } else {
                merged.add(valueList.get(i));
            }
        }
        return merged;
    }

    return valueList == null ? Collections.EMPTY_LIST : valueList;
}