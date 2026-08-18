public List getValues(final Option option,
                          List defaultValues) {
        // initialize the return list
        List valueList = (List) values.get(option);

        List result = null;
        if (valueList != null && !valueList.isEmpty()) {
            result = new ArrayList(valueList);
        } else if (defaultValues != null && !defaultValues.isEmpty()) {
            result = new ArrayList(defaultValues);
        } else {
            List dv = (List) this.defaultValues.get(option);
            if (dv != null && !dv.isEmpty()) {
                result = new ArrayList(dv);
            }
        }
        return result == null ? Collections.EMPTY_LIST : result;
    }