public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ZipArchiveEntry other = (ZipArchiveEntry) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (size != other.size) {
            return false;
        }
        if (compressedSize != other.compressedSize) {
            return false;
        }
        if (crc != other.crc) {
            return false;
        }
        if (method != other.method) {
            return false;
        }
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        } else if (!comment.equals(other.comment)) {
            return false;
        }
        if (extra == null) {
            if (other.extra != null) {
                return false;
            }
        } else if (!java.util.Arrays.equals(extra, other.extra)) {
            return false;
        }
        return true;
    }