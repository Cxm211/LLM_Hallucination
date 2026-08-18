    public void addOption(Option option) {
        // Add parent options first
        Option parent = option.getParent();
        if (parent != null && !options.contains(parent)) {
            addOption(parent);
        }
        options.add(option);
        nameToOption.put(option.getPreferredName(), option);

        for (Iterator i = option.getTriggers().iterator(); i.hasNext();) {
            nameToOption.put(i.next(), option);
        }

        // ensure that all parent options are also added
    }