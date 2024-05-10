import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class Main {
    public static void main(String[] args) {
        Tester.test1(6,5,10,1000,false);
        //the first parameter deteremines up to what dimension the test will go to
        //the second parameter tells us how many times will the size multiply itself with the third parameter where size starts off as 10
        //the fourth parameter determines how many searches per size of tree will be conducted.
        //regarding the fourth parameter, there will be some randomly generated points, then we use bruteforce to find the closest point to the randomly
        //generated points by the tester. we then test each tree with those same points and check if the result is the same as the bruteforce result
        //the number of points is equal to the number of searches determined by the one who calls the function
        //the sole purpose of the fourth parameter is to show that the tree gives back the correct point
        //if during the testing it takes a while for the searches to appear, its because there is a high number of searches being done
        //and probably the finding of the nearest neighbor through bruteforce part.
        // a high number of searches can help get rid of the variance that comes with time of code exceution
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
        return dist;
    }
}

class Tester{
    static void test1(int dimensions,int n,int increment,int searches,boolean threaded){

        warmuprounds();
        Random rand = new Random();

        File file = new File("K_D_tree_performance.csv");
        FileWriter writer=null;
        try {
            writer = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StringBuilder stringBuilder = new StringBuilder("Dimension,n,first_approach_tree_buildtime,second_approach_tree_buildtime,third_approach_tree_buildtime,linearsearchtime,searchtime1,searchtime2,searchtime3\n");
        System.out.println("Testing has Started....");
        System.out.println("tree creation time is in miliseconds, search time is in nanoseconds");
        for(int i = 2;i<=dimensions;i++)
        {
            int size = 30;
            for(int j = 0; j < n;j++)
            {
                System.gc();
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
                stringBuilder.append(i + "," + size + ",");
                long time = System.currentTimeMillis();
                K_D_tree tree1 = new K_D_tree(i,points,1,threaded);
                stringBuilder.append(System.currentTimeMillis()-time + ",");
                time = System.currentTimeMillis();
                K_D_tree tree2 = new K_D_tree(i,points,2,threaded);
                stringBuilder.append(System.currentTimeMillis()-time + ",");
                time = System.currentTimeMillis();
                K_D_tree tree3 = new K_D_tree(i,points,3,threaded);
                stringBuilder.append(System.currentTimeMillis()-time + ",");
                double[] returned;
                double[][] brute = new double[searches][i];

                long t = System.nanoTime();

                for(int m=0;m<searches;m++)
                {
                    brute[m] = Main.nearestbruteforce(points, point[m]);
                }

                stringBuilder.append((System.nanoTime()-t)/searches + ",") ;
                long totalTime = 0;

                for (int m = 0; m < searches; m++) {
                    long startTime = System.nanoTime(); // Start timing before the search operation
                    returned = tree1.nearest_neighbor(point[m]);
                    long endTime = System.nanoTime(); // End timing after the search operation
                    totalTime += (endTime - startTime); // Add the time taken for this search operation

                    for (int g = 0; g < i; g++) {
                        if (returned[g] != brute[m][g])
                            throw new RuntimeException("Tree gave back wrong point");
                    }
                }

                long averageTime = totalTime / searches;
                stringBuilder.append(averageTime + ",");
                totalTime = 0;

                for (int m = 0; m < searches; m++) {
                    long startTime = System.nanoTime(); // Start timing before the search operation
                    returned = tree2.nearest_neighbor(point[m]);
                    long endTime = System.nanoTime(); // End timing after the search operation
                    totalTime += (endTime - startTime); // Add the time taken for this search operation

                    for (int g = 0; g < i; g++) {
                        if (returned[g] != brute[m][g])
                            throw new RuntimeException("Tree gave back wrong point");
                    }
                }

                averageTime = totalTime / searches;
                stringBuilder.append(averageTime + ",");
                totalTime = 0;

                for (int m = 0; m < searches; m++) {
                    long startTime = System.nanoTime(); // Start timing before the search operation
                    returned = tree3.nearest_neighbor(point[m]);
                    long endTime = System.nanoTime(); // End timing after the search operation
                    totalTime += (endTime - startTime); // Add the time taken for this search operation

                    for (int g = 0; g < i; g++) {
                        if (returned[g] != brute[m][g])
                            throw new RuntimeException("Tree gave back wrong point");
                    }
                }

                averageTime = totalTime / searches;
                stringBuilder.append(averageTime + "\n");
                size*=increment;
            }
        }
        try {
            writer.write(stringBuilder.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finished!");
    }

    private static void warmuprounds()
    {
        System.out.println("Warming up...");
        Random rand = new Random();
        int size = 100000,dim = 10;
        double[][] points = new double[size][dim];
        for(int k=0;k<size;k++)
        {
            for(int l=0;l<dim;l++)
            {
                points[k][l]= rand.nextInt();
            }
        }
        K_D_tree tree1 = new K_D_tree(dim,points,1,false);
        K_D_tree tree2 = new K_D_tree(dim,points,2,false);
        K_D_tree tree3 = new K_D_tree(dim,points,3,false);

        for(int i= 0; i<10000;i++)
        {
            double[] point= new double[dim];
            for(int l=0;l<dim;l++)
            {
                point[l]= rand.nextInt();
            }
            tree1.nearest_neighbor(point);
            tree2.nearest_neighbor(point);
            tree3.nearest_neighbor(point);
            Main.nearestbruteforce(points,point);
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
