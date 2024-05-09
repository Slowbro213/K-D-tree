import java.util.*;

public class MedianOfMedians {
    private MedianOfMedians() {

    }

    /**
     * Returns median of list in linear time.
     *
     * @param list list to search, which may be reordered on return
     * @return median of array in linear time.
     */
    public static int getMedian(double[][] list,int end,int d) {
        int s = end;
        int pos = select(list,0, s, s/2,d);
        return pos;
    }

    /**
     * Returns position of k'th largest element of sub-list.
     *
     * @param list list to search, whose sub-list may be shuffled before
     *            returning
     * @param lo first element of sub-list in list
     * @param hi just after last element of sub-list in list
     * @param k
     * @return position of k'th largest element of (possibly shuffled) sub-list.
     */
    public static int select(double[][]  list, int lo, int hi, int k,int d) {
        if (lo >= hi || k < 0 || lo + k >= hi)
            throw new IllegalArgumentException();
        if (hi - lo < 10) {
            Arrays.sort(list, lo, hi, new Comparator<double[]>() {
                @Override
                public int compare(double[] o1, double[] o2) {
                    return Double.compare(o1[d],o2[d]);
                }
            });
            return lo + k;
        }
        int s = hi - lo;
        int np = s / 5; // Number of partitions
        for (int i = 0; i < np; i++) {
            // For each partition, move its median to front of our sublist
            int lo2 = lo + i * 5;
            int hi2 = (i + 1 == np) ? hi : (lo2 + 5);
            int pos = select(list, lo2, hi2, 2,d);
            swap(list, pos, lo + i);
        }

        // Partition medians were moved to front, so we can recurse without making another list.
        int pos = select(list, lo, lo + np, np / 2,d);

        // Re-partition list to [<pivot][pivot][>pivot]
        int m = triage(list, lo, hi, pos,d);
        int cmp = lo + k - m;
        if (cmp > 0)
            return select(list, m + 1, hi, k - (m - lo) - 1,d);
        else if (cmp < 0)
            return select(list, lo, m, k,d);
        return lo + k;
    }

    /**
     * Partition sub-list into 3 parts [<pivot][pivot][>pivot].
     *
     * @param list
     * @param lo
     * @param hi
     * @param pos input position of pivot value
     * @return output position of pivot value
     */
    private static int triage(double[][] list, int lo, int hi,
                              int pos,int d) {
        double[] pivot = list[pos];
        int lo3 = lo;
        int hi3 = hi;
        while (lo3 < hi3) {
            double[] e = list[lo3];
            int cmp = Double.compare(e[d],pivot[d]);
            if (cmp < 0)
                lo3++;
            else if (cmp > 0)
                swap(list, lo3, --hi3);
            else {
                while (hi3 > lo3 + 1) {
                    assert (Double.compare(list[lo3][d],pivot[d]) == 0);
                    e = list[--hi3];
                    cmp = Double.compare(e[d],pivot[d]);
                    if (cmp <= 0) {
                        if (lo3 + 1 == hi3) {
                            swap(list, lo3, lo3 + 1);
                            lo3++;
                            break;
                        }
                        swap(list, lo3, lo3 + 1);
                        assert (Double.compare(list[lo3 + 1][d],pivot[d]) == 0);
                        swap(list, lo3, hi3);
                        lo3++;
                        hi3++;
                    }
                }
                break;
            }
        }
        assert (Double.compare(list[lo3][d],pivot[d]) == 0);
        return lo3;
    }

    private static void swap(double[][] list ,int pos1, int pos2)
    {
        double[] lis = list[pos1];
        list[pos1] = list[pos2];
        list[pos2] = lis;
    }
}