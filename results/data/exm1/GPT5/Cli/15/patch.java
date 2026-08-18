public List getValues(final Option option,
                          List defaultValues) {
        // initialize the return list
        List valueList = (List) values.get(option);

        // if there are no values, prefer the passed defaults, then the stored defaults
        if ((valueList == null) || valueList.isEmpty()) {
            List result = defaultValues;
            if ((result == null) || result.isEmpty()) {
                result = (List) this.defaultValues.get(option);
            }
            return result == null ? Collections.EMPTY_LIST : result;
        }

        // values exist; augment with defaults (prefer method defaults, then stored defaults)
        List defs = defaultValues;
        if (defs == null || defs.isEmpty()) {
            defs = (List) this.defaultValues.get(option);
        }

        if (defs == null || defs.isEmpty() || valueList.size() >= defs.size()) {
            return valueList;
        }

        List augmented = new ArrayList(valueList);
        int need = defs.size() - valueList.size();
        for (int i = defs.size() - need; i < defs.size(); i++) {
            augmented.add(defs.get(i));
        }

        return augmented;
    }