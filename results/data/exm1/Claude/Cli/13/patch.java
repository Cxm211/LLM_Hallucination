public List getValues(final Option option,
                          final List defaultValues) {
        List valueList = (List) values.get(option);

        if ((valueList == null) || valueList.isEmpty()) {
            valueList = defaultValues;
        }

        if ((valueList == null) || valueList.isEmpty()) {
            valueList = (List) this.defaultValues.get(option);
        }

        if (valueList == null || valueList.isEmpty()) {
            valueList = Collections.EMPTY_LIST;
        }

        return valueList;
    }