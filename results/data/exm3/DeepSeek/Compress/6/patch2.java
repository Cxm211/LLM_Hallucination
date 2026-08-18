    public int hashCode() {
        int result = (getName() == null) ? 0 : getName().hashCode();
        result = 31 * result + (int) (getSize() ^ (getSize() >>> 32));
        result = 31 * result + (int) (getCompressedSize() ^ (getCompressedSize() >>> 32));
        result = 31 * result + (int) (getCrc() ^ (getCrc() >>> 32));
        result = 31 * result + (int) (getTime() ^ (getTime() >>> 32));
        result = 31 * result + getMethod();
        result = 31 * result + java.util.Arrays.hashCode(getExtra());
        return result;
    }