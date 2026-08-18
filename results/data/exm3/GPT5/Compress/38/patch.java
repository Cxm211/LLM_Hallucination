public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

        // Only treat trailing slash as directory for regular entries; not for PAX or other special headers
        if (getName() != null && getName().endsWith("/")) {
            if (linkFlag != LF_PAX_EXTENDED_HEADER && linkFlag != LF_PAX_GLOBAL_EXTENDED_HEADER) {
                return true;
            }
        }

        return false;
    }