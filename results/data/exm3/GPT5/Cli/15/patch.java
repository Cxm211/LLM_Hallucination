public List getValues(final Option option,
                          List defaultValues) {
        // initialize the return list
        List valueList = (List) values.get(option);

        // if there are parsed values, augment with the last default (if any)
        if (valueList != null && !valueList.isEmpty()) {
            Object extra = null;
            if (defaultValues != null && !defaultValues.isEmpty()) {
                extra = defaultValues.get(defaultValues.size() - 1);
            } else {
                List dv = (List) this.defaultValues.get(option);
                if (dv != null && !dv.isEmpty()) {
                    extra = dv.get(dv.size() - 1);
                }
            }
            List result = new ArrayList(valueList);
            if (extra != null) {
                result.add(extra);
            }
            return result;
        }

        // no parsed values; fall back to provided defaultValues
        if (defaultValues != null && !defaultValues.isEmpty()) {
            return new ArrayList(defaultValues);
        }

        // finally, fall back to stored defaults
        List dv = (List) this.defaultValues.get(option);
        return dv == null ? Collections.EMPTY_LIST : new ArrayList(dv);
    }