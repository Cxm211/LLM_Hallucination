public boolean isUnixSymlink() {
        int mode = getUnixMode();
        if ((mode & UnixStat.LINK_FLAG) != UnixStat.LINK_FLAG) {
            return false;
        }
        // ensure no other file type flags are set
        if ((mode & UnixStat.DIR_FLAG) == UnixStat.DIR_FLAG) {
            return false;
        }
        if ((mode & UnixStat.FILE_FLAG) == UnixStat.FILE_FLAG) {
            return false;
        }
        return true;
    }