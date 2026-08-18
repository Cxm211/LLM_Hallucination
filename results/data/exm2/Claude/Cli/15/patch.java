public List getValues(final Option option,
                          List defaultValues) {
        // initialize the return list
        List valueList = (List) values.get(option);

        // grab the correct default values
        if ((valueList == null) || valueList.isEmpty()) {
            valueList = defaultValues;
        }

        // augment the list with the default values
        if ((valueList == null) || valueList.isEmpty()) {
            valueList = (List) this.defaultValues.get(option);
        }
        else {
            // if there are default values and the valueList is not empty,
            // we need to add missing defaults to reach the expected size
            List defaults = (List) this.defaultValues.get(option);
            if (defaults != null && defaults.size() > valueList.size()) {
                // copy the list first
                valueList = new ArrayList(valueList);
                for (int i = valueList.size(); i < defaults.size(); i++) {
                    valueList.add(defaults.get(i));
                }
            }
        }
        
        return valueList == null ? Collections.EMPTY_LIST : valueList;
    }