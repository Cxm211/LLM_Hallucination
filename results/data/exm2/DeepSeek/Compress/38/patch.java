    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

        // Check for PAX headers; they are not directories even if name ends with slash
        if (linkFlag == LF_PAX_HEADER || linkFlag == LF_GLOBAL_HEADER) {
            return false;
        }

        if (getName().endsWith("/")) {
            return true;
        }

        return false;
    }