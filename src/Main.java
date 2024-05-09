import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class Main {
    public static void main(String[] args) {
        Tester.test1(5,5,10,10000,false);
        //the first parameter deteremines up to what dimension the test will go to
        //the second parameter tells us how many times will the size multiply itself with the third parameter where size starts off as 10
        //the fourth parameter determines how many searches per size of tree will be conducted.
        //regarding the fourth parameter, there will be some randomly generated points, then we use bruteforce to find the closest point to the randomly
        //generated points by the tester. we then test each tree with those same points and check if the result is the same as the bruteforce result
        //the number of points is equal to the number of searches determined by the one who calls the function
        //the sole purpose of the fourth parameter is to show that the tree gives back the correct point
        //the fifth parameter decides whether the tree creation process should be done with threading or not.
    }

    static double[] nearestbruteforce(double[][] points,double[] point)
    {
        double[] min = points[0];
        double distance = Double.POSITIVE_INFINITY;
        for(double[] x : points)
        {
            if(findDistance(x,point)<distance)
            {
                min=x;
                distance = findDistance(min,point);
            }
        }
        return min;
    }
    private static double findDistance(double[] point1, double[] point2)
    {
        double dist = 0;
        for(int i = 0; i < point1.length; i++)
        {
            double diff = point2[i] - point1[i];
            dist += diff * diff;
        }
        return sqrt(dist);
    }
}

class Tester{

    static int dim = 2;
    static void test1(int dimensions,int n,int increment,int searches,boolean threaded){
        Random rand = new Random();

        System.out.println("tree creation time is in miliseconds, search time is in nanoseconds");
        for(int i = 2;i<=dimensions;i++)
        {
            System.out.println("Dimensions: "+i);
            int size = 10;
            System.out.println("n      first     second      third      search1      search2      search3");
            for(int j = 0; j < n;j++)
            {
                double[][] points = new double[size][i];
                for(int k=0;k<size;k++)
                {
                    for(int l=0;l<i;l++)
                    {
                        points[k][l]= rand.nextInt();
                    }
                }
                double[][] point = new double[searches][i];
                for(int m=0;m<searches;m++) {
                    for (int g = 0; g < i; g++) {
                        point[m][g] = rand.nextInt();
                    }
                }
                System.out.print(size+ "      ");
                long time = System.currentTimeMillis();
                K_D_tree tree1 = new K_D_tree(i,points,1,threaded);
                System.out.print(System.currentTimeMillis()-time + "      ");
                time = System.currentTimeMillis();
                K_D_tree tree2 = new K_D_tree(i,points,2,threaded);
                System.out.print(System.currentTimeMillis()-time + "      ");
                time = System.currentTimeMillis();
                K_D_tree tree3 = new K_D_tree(i,points,3,threaded);
                System.out.print(System.currentTimeMillis()-time + "      ");
                double[] returned;
                double[][] brute = new double[searches][i];
                for(int m=0;m<searches;m++)
                {
                    brute[m] = Main.nearestbruteforce(points, point[m]);
                }
                time = System.nanoTime();
                for(int m=0;m<searches;m++) {
                    returned = tree1.nearest_neighbor(point[m]);
                    for (int g = 0; g < i; g++) {
                        if (returned[g] != brute[m][g])
                            throw new RuntimeException("Tree gave back wrong point");
                    }
                }
                System.out.print((System.nanoTime()-time)/searches + "      ");
                time = System.nanoTime();
                for(int m=0;m<searches;m++) {
                    returned = tree2.nearest_neighbor(point[m]);
                    for (int g = 0; g < i; g++) {
                        if (returned[g] != brute[m][g])
                            throw new RuntimeException("Tree gave back wrong point");
                    }
                }
                System.out.print((System.nanoTime()-time)/searches + "      ");
                time = System.nanoTime();
                for(int m=0;m<searches;m++) {
                    returned = tree3.nearest_neighbor(point[m]);
                    for (int g = 0; g < i; g++) {
                        if (returned[g] != brute[m][g])
                            throw new RuntimeException("Tree gave back wrong point");
                    }
                }
                System.out.println((System.nanoTime()-time)/searches + "      ");
                size*=increment;
                System.gc();
            }
        }
    }
//    public static void Quicksort(int[][][] arr, int l, int r,int k)
//    {
//        if (l > r)
//            return;
//
//        k = k % dim;
//        int mid = (l + r) / 2;
//        for (int i = 1; i < dim; i++) {
//            partition(arr[(k+i)%dim], l, r, arr[k][mid][k] , k);
//        }
//
//        System.out.println(arr[k][mid][0] + " " + arr[k][mid][1]);
//        Quicksort(arr, mid + 1, r, k + 1);
//        Quicksort(arr, l, mid - 1, k + 1);
//    }
//    static void swap(int[][] arr, int i, int j)
//    {
//        int[] temp = new int[arr[j].length];
//        System.arraycopy(arr[i],0,temp,0,arr[i].length);
//        System.arraycopy(arr[j],0,arr[i],0,arr[j].length);
//        System.arraycopy(temp,0,arr[j],0,arr[j].length);
//    }
//
//    static int partition(int[][] arr, int low, int high,int pivotValue,int k)
//    {
//        int i = low;
//
//        for (int j = low; j < high; j++) {
//            if (arr[j][k] < pivotValue) {
//                swap(arr, i, j);
//                i++;
//            }
//            else if(arr[j][k] == pivotValue){
//                swap(arr, j, high);
//            }
//        }
//
//        // Move the pivot element to its correct position
//        swap(arr, i, high);
//
//        return i;
//    }
}
