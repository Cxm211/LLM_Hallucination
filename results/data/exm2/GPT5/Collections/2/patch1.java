public void setInclude(String inc) {
        if (inc == null || inc.length() == 0) {
            include = null;
        } else {
            include = inc;
        }
    }