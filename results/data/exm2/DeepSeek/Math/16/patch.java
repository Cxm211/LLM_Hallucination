    public static double cosh(double x) {
      if (x != x) {
          return x;
      }

      // cosh[z] = (exp(z) + exp(-z))/2

      // for numbers with magnitude 20 or so,
      // exp(-z) can be ignored in comparison with exp(z)

      final double LOG_MAX_VALUE = Math.log(Double.MAX_VALUE);
      if (x > 20) {
          if (x > LOG_MAX_VALUE) {
              return Math.cosh(x);
          } else {
              return 0.5 * exp(x);
          }
      }
      if (x < -20) {
          if (x < -LOG_MAX_VALUE) {
              return Math.cosh(x);
          } else {
              return 0.5 * exp(-x);
          }
      }

      final double hiPrec[] = new double[2];
      if (x < 0.0) {
          x = -x;
      }
      exp(x, 0.0, hiPrec);

      double ya = hiPrec[0] + hiPrec[1];
      double yb = -(ya - hiPrec[0] - hiPrec[1]);

      double temp = ya * HEX_40000000;
      double yaa = ya + temp - temp;
      double yab = ya - yaa;

      // recip = 1/y
      double recip = 1.0/ya;
      temp = recip * HEX_40000000;
      double recipa = recip + temp - temp;
      double recipb = recip - recipa;

      // Correct for rounding in division
      recipb += (1.0 - yaa*recipa - yaa*recipb - yab*recipa - yab*recipb) * recip;
      // Account for yb
      recipb += -yb * recip * recip;

      // y = y + 1/y
      temp = ya + recipa;
      yb += -(temp - ya - recipa);
      ya = temp;
      temp = ya + recipb;
      yb += -(temp - ya - recipb);
      ya = temp;

      double result = ya + yb;
      result *= 0.5;
      return result;
    }