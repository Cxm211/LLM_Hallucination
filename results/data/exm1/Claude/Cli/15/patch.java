public List getValues(final Option option, List defaultValues) {
    List valueList = (List) values.get(option);

    if ((valueList == null) || valueList.isEmpty()) {
        valueList = defaultValues;
    }

    if ((valueList == null) || valueList.isEmpty()) {
        valueList = (List) this.defaultValues.get(option);
    }

    if (valueList == null) {
        return Collections.EMPTY_LIST;
    }

    List augmentedList = new ArrayList(valueList);
    List optionDefaults = (List) this.defaultValues.get(option);
    
    if (optionDefaults != null && !optionDefaults.isEmpty()) {
        for (int i = augmentedList.size(); i < optionDefaults.size(); i++) {
            augmentedList.add(optionDefaults.get(i));
        }
    }
    
    return augmentedList;
}