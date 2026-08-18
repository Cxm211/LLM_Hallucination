    public ZipShort getCentralDirectoryLength() {
        int uidLen = 0;
        long temp = uid;
        while (temp != 0) {
            uidLen++;
            temp >>>= 8;
        }
        int gidLen = 0;
        temp = gid;
        while (temp != 0) {
            gidLen++;
            temp >>>= 8;
        }
        return new ZipShort(3 + uidLen + gidLen);
    }