public void validate(final WriteableCommandLine commandLine)
        throws OptionException {
        // number of options found
        int present = 0;

        // reference to first unexpected option
        Option unexpected = null;

        for (final Iterator i = options.iterator(); i.hasNext();) {
            final Option option = (Option) i.next();

            boolean isPresent = commandLine.hasOption(option);

            // validate required options that are not present
            if (option.isRequired() && !isPresent) {
                option.validate(commandLine);
            }

            // if the child option is present then validate it
            if (isPresent) {
                if (++present > maximum) {
                    unexpected = option;

                    break;
                }

                option.validate(commandLine);
            }
        }

        // too many options
        if (unexpected != null) {
            throw new OptionException(this, ResourceConstants.UNEXPECTED_TOKEN,
                                      unexpected.getPreferredName());
        }

        // too few option
        if (present < minimum) {
            throw new OptionException(this, ResourceConstants.MISSING_OPTION);
        }

        // validate each anonymous argument
        for (final Iterator i = anonymous.iterator(); i.hasNext();) {
            final Option option = (Option) i.next();
            option.validate(commandLine);
        }
    }