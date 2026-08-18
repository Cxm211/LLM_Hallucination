public void setInclude(String inc) {
        // Empty or null disables include processing explicitly
        include = (inc == null || inc.length() == 0) ? "" : inc;
    }