protected void setOptions(final Options options) {
        this.options = options;
        // Copy required options to avoid sharing the mutable internal list from Options
        this.requiredOptions = new java.util.ArrayList(options.getRequiredOptions());
    }