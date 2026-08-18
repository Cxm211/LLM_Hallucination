public boolean isDirectory() {
    if (file != null && file.isDirectory()) {
        return true;
    }

    if (linkFlag == LF_DIR) {
        return true;
    }

    if (getName().endsWith("/")) {
        return true;
    }

    return false;
}