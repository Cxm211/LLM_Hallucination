    public GroupImpl(final List options,
                     final String name,
                     final String description,
                     final int minimum,
                     final int maximum) {
        super(0, false);

        this.name = name;
        this.description = description;
        this.minimum = minimum;
        this.maximum = maximum;

        // store a copy of the options to be used by the
        // help methods
        List optionsCopy = new ArrayList(options);
        this.options = Collections.unmodifiableList(optionsCopy);

        // anonymous Argument temporary storage
        final List newAnonymous = new ArrayList();

        // map (key=trigger & value=Option) temporary storage
        final SortedMap newOptionMap = new TreeMap(ReverseStringComparator.getInstance());

        // prefixes temporary storage
        final Set newPrefixes = new HashSet();

        // process the options
        for (final Iterator i = optionsCopy.iterator(); i.hasNext();) {
            final Option option = (Option) i.next();

            if (option instanceof Argument) {
                // do not remove from the list, just add to anonymous
                newAnonymous.add(option);
            } else {
                // add preferred name as a trigger
                newOptionMap.put(option.getPreferredName(), option);

                final Set triggers = option.getTriggers();
                for (Iterator j = triggers.iterator(); j.hasNext();) {
                    newOptionMap.put(j.next(), option);
                }

                // store the prefixes
                newPrefixes.addAll(option.getPrefixes());
            }
        }

        this.anonymous = Collections.unmodifiableList(newAnonymous);
        this.optionMap = Collections.unmodifiableSortedMap(newOptionMap);
        this.prefixes = Collections.unmodifiableSet(newPrefixes);
    }