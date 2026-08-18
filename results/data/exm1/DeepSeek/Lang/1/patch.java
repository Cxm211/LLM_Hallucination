public static Number createNumber(final String str) throws NumberFormatException {
        if (str == null) {
            return null;
        }
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        final String[] hex_prefixes = {"0x", "0X", "-0x", "-0X", "#", "-#"};
        int pfxLen = 0;
        for(final String pfx : hex_prefixes) {
            if (str.startsWith(pfx)) {
                pfxLen += pfx.length();
                break;
            }
        }
        if (pfxLen > 0) { // we have a hex number
            final String hexDigits = str.substring(pfxLen);
            if (hexDigits.isEmpty()) {
                throw new NumberFormatException(str + " is not a valid number.");
            }
            // parse as BigInteger to determine the smallest type
            BigInteger bigInt;
            try {
                bigInt = new BigInteger(hexDigits, 16);
            } catch (final NumberFormatException e) {
                throw new NumberFormatException(str + " is not a valid number.");
            }
            if (str.startsWith("-")) {
                bigInt = bigInt.negate();
            }
            if (bigInt.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0 &&
                bigInt.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0) {
                return bigInt.intValue();
            }
            if (bigInt.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0 &&
                bigInt.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0) {
                return bigInt.longValue();
            }
            return bigInt;
        }
        final char lastChar = str.charAt(str.length() - 1);
        String mant;
        String dec;
        String exp;
        final int decPos = str.indexOf('.');
        int expPos = str.indexOf('e');
        if (expPos < 0) {
            expPos = str.indexOf('E');
        }
        int numDecimals = 0;
        if (decPos > -1) {
            if (expPos > -1) {
                if (expPos < decPos) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                dec = str.substring(decPos + 1, expPos);
            } else {
                dec = str.substring(decPos + 1);
            }
            mant = str.substring(0, decPos);
            numDecimals = dec.length();
        } else {
            if (expPos > -1) {
                mant = str.substring(0, expPos);
            } else {
                mant = str;
            }
            dec = null;
        }
        if (!Character.isDigit(lastChar) && lastChar != '.') {
            if (expPos > -1 && expPos < str.length() - 1) {
                exp = str.substring(expPos + 1, str.length() - 1);
            } else {
                exp = null;
            }
            final String numeric = str.substring(0, str.length() - 1);
            final boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
            switch (lastChar) {
                case 'l' :
                case 'L' :
                    if (dec == null
                        && exp == null
                        && (numeric.charAt(0) == '-' && isDigits(numeric.substring(1)) || isDigits(numeric))) {
                        try {
                            return createLong(numeric);
                        } catch (final NumberFormatException nfe) {
                        }
                        return createBigInteger(numeric);
                    }
                    throw new NumberFormatException(str + " is not a valid number.");
                case 'f' :
                case 'F' :
                    try {
                        final Float f = NumberUtils.createFloat(numeric);
                        if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                            return f;
                        }
                    } catch (final NumberFormatException nfe) {
                    }
                case 'd' :
                case 'D' :
                    try {
                        final Double d = NumberUtils.createDouble(numeric);
                        if (!(d.isInfinite() || (d.doubleValue() == 0.0D && !allZeros))) {
                            return d;
                        }
                    } catch (final NumberFormatException nfe) {
                    }
                    try {
                        return createBigDecimal(numeric);
                    } catch (final NumberFormatException e) {
                    }
                default :
                    throw new NumberFormatException(str + " is not a valid number.");
            }
        }
        if (expPos > -1 && expPos < str.length() - 1) {
            exp = str.substring(expPos + 1, str.length());
        } else {
            exp = null;
        }
        if (dec == null && exp == null) {
            try {
                return createInteger(str);
            } catch (final NumberFormatException nfe) {
            }
            try {
                return createLong(str);
            } catch (final NumberFormatException nfe) {
            }
            return createBigInteger(str);
        }
        final boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
        try {
            if(numDecimals <= 7){
                final Float f = createFloat(str);
                if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                    return f;
                }
            }
        } catch (final NumberFormatException nfe) {
        }
        try {
            if(numDecimals <= 16){
                final Double d = createDouble(str);
                if (!(d.isInfinite() || (d.doubleValue() == 0.0D && !allZeros))) {
                    return d;
                }
            }
        } catch (final NumberFormatException nfe) {
        }
        return createBigDecimal(str);
    }