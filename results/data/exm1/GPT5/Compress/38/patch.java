public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR || linkFlag == LF_GNUTYPE_DUMPDIR) {
            return true;
        }

        String n = getName();
        if (n != null && n.endsWith("/")) {
            return true;
        }

        return false;
    }