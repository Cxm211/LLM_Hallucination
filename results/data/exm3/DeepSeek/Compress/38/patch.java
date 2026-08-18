    public boolean isDirectory() {
        if (linkFlag == LF_DIR) {
            return true;
        }
        if (linkFlag == LF_SYMLINK) {
            return false;
        }
        if (file != null) {
            return file.isDirectory();
        }
        if (getName().endsWith("/")) {
            return true;
        }
        return false;
    }