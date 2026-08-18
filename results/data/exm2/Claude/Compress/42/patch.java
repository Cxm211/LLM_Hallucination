public boolean isUnixSymlink() {
    return UnixStat.FILE_TYPE_FLAG == (getUnixMode() & UnixStat.FILE_TYPE_FLAG)
        && (getUnixMode() & UnixStat.LINK_FLAG) == UnixStat.LINK_FLAG;
}