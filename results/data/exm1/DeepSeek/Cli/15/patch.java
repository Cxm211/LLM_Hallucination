public List getValues(final Option option,
                          List defaultValues) {
        // initialize the return list
        List valueList = (List) values.get(option);

        // grab the correct default values
        if ((valueList == null) || valueList.isEmpty()) {
            valueList = defaultValues;
        }

        // copy the list first to prevent external modification
        return valueList == null ? Collections.EMPTY_LIST : new ArrayList(valueList);
    }