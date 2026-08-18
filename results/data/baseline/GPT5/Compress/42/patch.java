public boolean isUnixSymlink() {
        return (getUnixMode() & UnixStat.FILE_TYPE_MASK) == UnixStat.LINK_FLAG;
    }