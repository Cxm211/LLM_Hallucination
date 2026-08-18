    public List getValues(final Option option,
                          List defaultValues) {
        // initialize the return list
        List valueList = (List) values.get(option);

        List argDefaults = defaultValues;
        List optDefaults = (List) this.defaultValues.get(option);

        // Merge user values with argument defaults
        List result = new ArrayList();
        int i = 0;
        while (true) {
            boolean hasUser = valueList != null && i < valueList.size();
            boolean hasArg = argDefaults != null && i < argDefaults.size();
            if (!hasUser && !hasArg) {
                break;
            }
            if (hasUser) {
                result.add(valueList.get(i));
            } else if (hasArg) {
                result.add(argDefaults.get(i));
            }
            i++;
        }

        // If result is empty, fall back to option defaults
        if (result.isEmpty() && optDefaults != null && !optDefaults.isEmpty()) {
            result = optDefaults;
        }

        return result.isEmpty() ? Collections.EMPTY_LIST : result;
    }