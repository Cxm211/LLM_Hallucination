public List getValues(final Option option,
                          final List defaultValues) {
        // First grab the command line values
        List valueList = (List) values.get(option);

        // Secondly try the defaults supplied to the method (only if not provided already)
        if (valueList == null) {
            valueList = defaultValues;
        }

        // Thirdly try the option's default values (only if still not provided)
        if (valueList == null) {
            valueList = (List) this.defaultValues.get(option);
        }

        // Finally use an empty list
        if (valueList == null) {
            valueList = Collections.EMPTY_LIST;
        }

        return valueList;
    }