public void addOption(Option option) {
        if (!options.contains(option)) {
            options.add(option);
        }
        nameToOption.put(option.getPreferredName(), option);

        for (Iterator i = option.getTriggers().iterator(); i.hasNext();) {
            nameToOption.put(i.next(), option);
        }

        // ensure that all child options are also added
        Group children = option.getChildren();
        if (children != null) {
            for (Iterator it = children.getOptions().iterator(); it.hasNext();) {
                Option child = (Option) it.next();
                // recursively add child options
                addOption(child);
            }
        }
    }