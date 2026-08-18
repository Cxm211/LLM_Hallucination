// ===== FIXED org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream :: BZip2CompressorInputStream [lines 108-110] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-22-fixed/src/main/java/org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream.java =====
    public BZip2CompressorInputStream(final InputStream in) throws IOException {
        this(in, false);
    }

// ===== FIXED org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream :: read0() [lines 196-225] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-22-fixed/src/main/java/org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream.java =====
    private int read0() throws IOException {
        switch (currentState) {
        case EOF:
            return -1;

        case START_BLOCK_STATE:
            return setupBlock();

        case RAND_PART_A_STATE:
            throw new IllegalStateException();

        case RAND_PART_B_STATE:
            return setupRandPartB();

        case RAND_PART_C_STATE:
            return setupRandPartC();

        case NO_RAND_PART_A_STATE:
            throw new IllegalStateException();

        case NO_RAND_PART_B_STATE:
            return setupNoRandPartB();

        case NO_RAND_PART_C_STATE:
            return setupNoRandPartC();

        default:
            throw new IllegalStateException();
        }
    }

// ===== FIXED org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream :: setupNoRandPartA() [lines 855-871] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-22-fixed/src/main/java/org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream.java =====
    private int setupNoRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {
            this.su_chPrev = this.su_ch2;
            int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;
            this.su_ch2 = su_ch2Shadow;
            this.su_tPos = this.data.tt[this.su_tPos];
            this.su_i2++;
            this.currentState = NO_RAND_PART_B_STATE;
            this.crc.updateCRC(su_ch2Shadow);
            return su_ch2Shadow;
        } else {
            this.currentState = NO_RAND_PART_A_STATE;
            endBlock();
            initBlock();
            return setupBlock();
        }
    }

// ===== FIXED org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream :: setupNoRandPartC() [lines 928-940] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-22-fixed/src/main/java/org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream.java =====
    private int setupNoRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {
            int su_ch2Shadow = this.su_ch2;
            this.crc.updateCRC(su_ch2Shadow);
            this.su_j2++;
            this.currentState = NO_RAND_PART_C_STATE;
            return su_ch2Shadow;
        } else {
            this.su_i2++;
            this.su_count = 0;
            return setupNoRandPartA();
        }
    }

// ===== FIXED org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream :: setupRandPartA() [lines 830-853] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-22-fixed/src/main/java/org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream.java =====
    private int setupRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {
            this.su_chPrev = this.su_ch2;
            int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;
            this.su_tPos = this.data.tt[this.su_tPos];
            if (this.su_rNToGo == 0) {
                this.su_rNToGo = Rand.rNums(this.su_rTPos) - 1;
                if (++this.su_rTPos == 512) {
                    this.su_rTPos = 0;
                }
            } else {
                this.su_rNToGo--;
            }
            this.su_ch2 = su_ch2Shadow ^= (this.su_rNToGo == 1) ? 1 : 0;
            this.su_i2++;
            this.currentState = RAND_PART_B_STATE;
            this.crc.updateCRC(su_ch2Shadow);
            return su_ch2Shadow;
        } else {
            endBlock();
            initBlock();
            return setupBlock();
        }
    }

// ===== FIXED org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream :: setupRandPartC() [lines 901-912] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-22-fixed/src/main/java/org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream.java =====
    private int setupRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {
            this.crc.updateCRC(this.su_ch2);
            this.su_j2++;
            return this.su_ch2;
        } else {
            this.currentState = RAND_PART_A_STATE;
            this.su_i2++;
            this.su_count = 0;
            return setupRandPartA();
        }
    }
