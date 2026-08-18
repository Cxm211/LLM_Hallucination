public void addOption(Option option) {
        if (option == null) {
            return;
        }

        if (!options.contains(option)) {
            options.add(option);
        }
        nameToOption.put(option.getPreferredName(), option);

        for (Iterator i = option.getTriggers().iterator(); i.hasNext();) {
            nameToOption.put(i.next(), option);
        }

        // ensure that all parent options are also added
        Option parent = null;
        try {
            // attempt to retrieve parent if supported
            parent = option.getParent();
        } catch (Throwable t) {
            parent = null;
        }
        while (parent != null) {
            if (!options.contains(parent)) {
                options.add(parent);
                nameToOption.put(parent.getPreferredName(), parent);
                for (Iterator j = parent.getTriggers().iterator(); j.hasNext();) {
                    nameToOption.put(j.next(), parent);
                }
            }
            try {
                parent = parent.getParent();
            } catch (Throwable t) {
                parent = null;
            }
        }
    }