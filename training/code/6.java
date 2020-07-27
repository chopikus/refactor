        for (int i = 1; i < p.length; i++) {
            if (i < k)
                p[i] = i % (k);
            else if (i == k)
                p[i] = k;
            else
                p[i] = k - (i % k);
        }
