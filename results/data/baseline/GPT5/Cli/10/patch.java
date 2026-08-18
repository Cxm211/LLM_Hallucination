protected void setOptions(final Options options) {
        this.options = options;
        if (options == null) {
            this.requiredOptions = new java.util.ArrayList();
        } else {
            this.requiredOptions = new java.util.ArrayList(options.getRequiredOptions());
        }
    }