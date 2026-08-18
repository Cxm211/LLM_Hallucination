    public boolean isUnixSymlink() {
        return (getUnixMode() & 0xF000) == UnixStat.LINK_FLAG;
    }