    public List getValues(final Option option,
                          List defaultValues) {
        // initialize the return list
        List valueList = (List) values.get(option);
        if (valueList == null) {
            valueList = Collections.EMPTY_LIST;
        }
        List defaults = defaultValues;
        if (defaults == null) {
            defaults = (List) this.defaultValues.get(option);
        }
        List result = new ArrayList(valueList);
        if (defaults != null && result.size() < defaults.size()) {
            result.addAll(defaults.subList(result.size(), defaults.size()));
        }
        return result.isEmpty() ? Collections.EMPTY_LIST : result;
    }