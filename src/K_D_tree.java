import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import static java.lang.Math.*;

public class K_D_tree{
    private class Node{
        double[] point;

        double currentBest;

        int dimension;
        Node left;
        Node right;

        public Node(double[] coordinates, Node left, Node right) {
            this.point = coordinates;
            this.left = left;
            this.right = right;
        }

        public Node(double[] point, int dimension) {
            this.point = point;
            this.dimension = dimension;
        }

        public Node(double[] coordinates) {
            this.point = coordinates;
            this.left=this.right=null;

        }
    }

    int dimensions;
    Node root;

    public K_D_tree(int dimensions)
    {
        this.dimensions=dimensions;
    }
    public K_D_tree(int dimensions,double[][] points)
    {
        //this is the approach that might lead to an imbalanced tree depending on the points and
        //the order those points are given to us
        this.dimensions=dimensions;
        for(double[] point: points)
        {
            insert(point);
        }
    }
    public K_D_tree(int dimensions,double[][] points2,int type,boolean threaded) //the type here is to discern the different ways of making a balanced k-d tree
    {
        //the boolean is there so you can choose whether you want to create threads for the creation process
        //type 1 is the O(n (log(n))^2) approach
        //type 2 is the O(n log(n)) approach
        //type 3 is the O(k n log(n)) approach
        this.dimensions=dimensions;
        if(type>3 || type <1)
            throw new RuntimeException("There is no type above 3 or below 1");
        ForkJoinPool fjp = new ForkJoinPool();
        double[][] points = new double[points2.length][dimensions];
        for(int i=0;i<points2.length;i++)
        {
            System.arraycopy(points2[i],0,points[i],0,dimensions);
        }
        switch (type)
        {
            case 1:
                if(threaded)
                    fjp.invoke(new FirstApproach(this,points,0,points.length-1,0));
                else {
                    FirstApproach(points, 0, points.length-1, 0);
                }
                break;
            case 2:
                if(threaded)
                    fjp.invoke(new SecondApproach(this,points,points.length,0));
                else
                SecondApproach(points,points.length,0);
                break;
            case 3:
                ThirdApproach(points,threaded,fjp);
                break;
        }

    }

    private void FirstApproach(double[][] points,int start ,int end,int dimension) // this is the O(n (n log(n))^2) approach
    {
        if(start>end)
            return;
        int k = dimension%dimensions;

        Arrays.sort(points,start,end+1, new Comparator<double[]>() {
            @Override
            public int compare(double[] o1, double[] o2) {
                return Double.compare(o1[k],o2[k]);
            }
        });
        int median = start + (end - start) / 2;
        insert(points[median]);
        FirstApproach(points,median+1,end,dimension+1);
        FirstApproach(points,start,median-1,dimension+1);
    }

    private void SecondApproach(double[][] points,int end,int dimension) // this would be the O(n log(n)) approach
            //this method can be inconsistent. Trees from this may be a bit imbalanced but not so much so they affect performance
    {
        if(end<1)
            return;
        int d = dimension%dimensions;
        int k=0,j=0;
        int median = MedianOfMedians.getMedian(points,end,d);
        double[] newmedian = new double[dimensions];
        System.arraycopy(points[median],0,newmedian,0,dimensions);
        insert(newmedian);
        double[][] left = new double[end][dimensions];
        double[][] right = new double[end][dimensions];
        for(int i=0;i<end;i++)
        {
            if(points[i][d]>newmedian[d])
            {
                System.arraycopy(points[i],0,right[k],0,dimensions);
                k++;
            }else if(!isarrayequal(points[i],newmedian))
            {
                System.arraycopy(points[i],0,left[j],0,dimensions);
                j++;
            }
        }
        SecondApproach(right,k,d+1);
        SecondApproach(left,j,d+1);

    }

    public void ThirdApproach(double[][] points,boolean threaded,ForkJoinPool fjp)// this is the O(k n log(n)) approach
    {
        double[][][] array = new double[dimensions][points.length][dimensions];
        for(int i=0;i<dimensions;i++)
        {
            final int ii = i;
            for(int j=0;j<points.length;j++) {
                System.arraycopy(points[j], 0, array[i][j], 0, dimensions);
            }
            Arrays.sort(array[ii],0,points.length, new Comparator<double[]>() {
                @Override
                public int compare(double[] o1, double[] o2) {
                    if(o1[ii]==o2[ii])
                    {
                        if(o1[(ii+1)%dimensions]==o2[(ii+1)%dimensions])
                        {
                            if(o1[(ii+2)%dimensions]==o2[(ii+2)%dimensions])
                            {
                                return 0;
                            }
                            return Double.compare(o1[(ii+2)%dimensions],o2[(ii+2)%dimensions]);
                        }
                        return Double.compare(o1[(ii+1)%dimensions],o2[(ii+1)%dimensions]);
                    }
                    return Double.compare(o1[(ii)],o2[(ii)]);
                }
            });
        }

        if(threaded)
            fjp.invoke(new ThirdApproach(this,array,points.length,0));
        else
            ThirdApproachRec(array,points.length,0);

    }


    private void ThirdApproachRec(double[][][] array, int end, int dimension) {
        int d = dimension%dimensions;
        if(end<1)
            return;
        double[][] points = array[d];
        int middle = (end)/2;
        int k=0,j=0;
        double[] median = new double[dimensions];
        System.arraycopy(points[middle],0,median,0,dimensions);
        insert(median);
        double[][][] left = new double[dimensions][end][dimensions];
        double[][][] right = new double[dimensions][end][dimensions];
        for(int i=0;i<dimensions;i++)
        {
            k=j=0;
            for(int l=0;l<end;l++)
            {
                if(array[i][l][d]>median[d])
                {
                    System.arraycopy(array[i][l],0,right[i][k],0,dimensions);
                    k++;
                }else if(!isarrayequal(array[i][l],median))
                {
                    System.arraycopy(array[i][l],0,left[i][j],0,dimensions);
                    j++;
                }
            }
        }
        ThirdApproachRec(right,k, d + 1);
        ThirdApproachRec(left,j, d + 1);
    }
    public synchronized void insert(double[] point)
    {

        if(point.length != dimensions)
            throw new RuntimeException("Wrong Dimensions");

        root = insertrec(point,root,0);
    }
    private Node insertrec(double[] point,Node root,int depth)
    {
        if(root == null)
        {
          /*  for(int i=0;i<dimensions;i++)
            {
                System.out.print(point[i] + ", ");
            }
            System.out.println();
            */
            return new Node(point,depth%dimensions);
        }
        if (point[depth%dimensions] < root.point[depth%dimensions]){
            root.left = insertrec(point,root.left,depth+1);
        }
        else if(!isarrayequal(root.point,point))
        {
            root.right = insertrec(point,root.right,depth+1);
        }
        return root;
    }
    public double[] nearest_neighbor(double[] point) {
        if (point.length != dimensions)
            throw new RuntimeException("Wrong Dimensions");
        Node nearest = nearest_neighborrec(point,root);
        return nearest.point;
    }

    private Node nearest_neighborrec(double[] point, Node root) {
        if (root == null)
            return null;

        if(root.left==null && root.right == null)
        {
            root.currentBest = findDistance(point,root.point);
            return root;
        }

        Node closest, explore;
        if (point[root.dimension] < root.point[root.dimension]) {
            closest = nearest_neighborrec(point, root.left);
            explore = root.right;
        } else {
            closest = nearest_neighborrec(point, root.right);
            explore = root.left;
        }

        // Update closest if necessary
        double roottopoint = findDistance(point, root.point);
        if (closest == null || closest.currentBest > roottopoint) {
            root.currentBest=roottopoint;
            closest = root;
        }

        if (explore != null) {
            double distSplit = Math.abs(point[root.dimension] - root.point[root.dimension]);
            double bestDist = closest.currentBest;
            if (distSplit< sqrt(bestDist)) {
                Node otherClosest = nearest_neighborrec(point, explore);
                if (otherClosest.currentBest < bestDist){
                    closest = otherClosest;
                }
            }
        }
        return closest;
    }

    private double findDistance(double[] point1, double[] point2)
    {
        double dist=0;
        for(int i= 0; i< point1.length;i++)
        {
            double number = (abs(point2[i] - point1[i]));
            dist += pow(number,2);
        }
        return dist;
    }

    public double[] findMinimum(int dimension)
    {
        return findminrec(dimension,root,0).point;
    }
    private Node findmin(int dimension,Node roott,int depth)
    {
        return findminrec(dimension,roott,depth);
    } // this function is for the deletion process
    private Node findminrec(int dimension, Node roott, int depth)
    {
        if(roott==null)
        {
            double[] nums = {Double.MAX_VALUE,Double.MAX_VALUE};
            return new Node(nums);
        }
        if(dimension==depth%dimensions && roott.left!=null)
            return findminrec(dimension,roott.left,depth+1);
        else
        {
            return min(roott,min(findminrec(dimension,roott.left,depth+1),findminrec(dimension,roott.right,depth+1),dimension),dimension);

        }
    }

    private Node min(Node one, Node two, int i)
    {
        if(one.point[i]<two.point[i])
            return one;
        else
            return two;
    }


    public void delete(double[] point)
    {
        deleterec(point,root,0);
    }
    private Node deleterec(double[] point , Node root , int depth)
    {
        if (root == null)
            return null;


        if (point[depth % dimensions] < root.point[depth % dimensions]) {
             root.left = deleterec(point, root.left, depth + 1);

        } else if(point[depth % dimensions] >= root.point[depth % dimensions] && !isarrayequal(root.point,point)){
            root.right = deleterec(point, root.right, depth + 1);
        }
        else
        {
            Node min;
            double[] temp;
            if(root.right!=null) {
                min = findmin(depth % dimensions, root.right,(depth+1)%dimensions);
                temp = min.point;
                min.point=root.point;
                root.point=temp;

                root.right = deleterec(min.point,root.right,depth+1);
            }
            else if(root.left!=null)
            {
                min = findmin(depth % dimensions, root.left,(depth+1)%dimensions);
                temp = min.point;
                min.point=root.point;
                root.point=temp;

                root.right = deleterec(min.point,root.left,depth+1);
            }else
                return null;


        }
        return root;
    }

    static boolean isarrayequal(double[] one, double[] two)
    {
        for(int i = 0;i<one.length;i++)
        {
            if(one[i]!=two[i])
                return false;
        }
        return true;
    }

    public int depth()
    {
        return depthrec(root);
    }
    private int depthrec(Node root)
    {
        if(root==null)
            return 0;

        int one = depthrec(root.left);
        int two = depthrec(root.right);
        one++;
        two++;
        return max(one,two);
    }

    public int getDimensions() {
        return dimensions;
    }

}

class FirstApproach extends RecursiveAction{

    K_D_tree tree;
    double[][] points;
    int start;
    int end;
    int dimension;

    static int THRESHOLD=10;

    public FirstApproach(K_D_tree tree, double[][] points, int start, int end, int dimension) {
        this.tree = tree;
        this.points = points;
        this.start = start;
        this.end = end;
        this.dimension = dimension;
    }

    @Override
    protected void compute() {
        if(start>end)
            return;
        int k = dimension%tree.getDimensions();

        Arrays.sort(points,start,end+1, new Comparator<double[]>() {
            @Override
            public int compare(double[] o1, double[] o2) {
                return Double.compare(o1[k],o2[k]);
            }
        });
        int median = start + (end - start) / 2;
        tree.insert(points[median]);
        invokeAll(new FirstApproach(tree,points,median+1,end,dimension+1),
                new FirstApproach(tree,points,start,median-1,dimension+1));
    }

}
class SecondApproach extends RecursiveAction {

    K_D_tree tree;
    double[][] points;
    int end;
    int dimension;


    public SecondApproach(K_D_tree tree, double[][] points, int end, int dimension) {
        this.tree = tree;
        this.points = points;
        this.end = end;
        this.dimension = dimension;
    }

    @Override
    protected void compute() {
        if(end<1)
            return;
        int dimensions = tree.getDimensions();
        int d = dimension%dimensions;
        int k=0,j=0;
        int median = MedianOfMedians.getMedian(points,end,d);
        double[] newmedian = new double[dimensions];
        System.arraycopy(points[median],0,newmedian,0,dimensions);
        tree.insert(newmedian);
        double[][] left = new double[end][dimensions];
        double[][] right = new double[end][dimensions];
        for(int i=0;i<end;i++)
        {
            if(points[i][d]>newmedian[d])
            {
                System.arraycopy(points[i],0,right[k],0,dimensions);
                k++;
            }else if(!K_D_tree.isarrayequal(points[i],newmedian))
            {
                System.arraycopy(points[i],0,left[j],0,dimensions);
                j++;
            }
        }
        invokeAll(new SecondApproach(tree,right,k,d+1),new SecondApproach(tree,left,j,d+1));
    }
}
class ThirdApproach extends RecursiveAction {
    K_D_tree tree;
    double[][][] array;
    int end;
    int dimension;


    public ThirdApproach(K_D_tree tree, double[][][] array, int end, int dimension) {
        this.tree = tree;
        this.array = array;
        this.end = end;
        this.dimension = dimension;
    }

    @Override
    protected void compute() {
        int dimensions = tree.getDimensions();
        int d = dimension%dimensions;
        if(end<1)
            return;
        double[][] points = array[d];
        int middle = (end)/2;
        int k=0,j=0;
        double[] median = new double[dimensions];
        System.arraycopy(points[middle],0,median,0,dimensions);
        tree.insert(median);
        double[][][] left = new double[dimensions][end][dimensions];
        double[][][] right = new double[dimensions][end][dimensions];
        for(int i=0;i<dimensions;i++)
        {
            k=j=0;
            for(int l=0;l<end;l++)
            {
                if(array[i][l][d]>median[d])
                {
                    System.arraycopy(array[i][l],0,right[i][k],0,dimensions);
                    k++;
                }else if(!K_D_tree.isarrayequal(array[i][l],median))
                {
                    System.arraycopy(array[i][l],0,left[i][j],0,dimensions);
                    j++;
                }
            }
        }
        invokeAll(new ThirdApproach(tree,right,k, d + 1),new ThirdApproach(tree,left,j, d + 1));
    }
}

class Tuple<T,E>{
    T obj1;
    E obj2;

    public Tuple(T obj1, E obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }
    public Tuple(Tuple<T,E> t)
    {
        this.obj1=t.obj1;
        this.obj2= t.obj2;
    }
    public Tuple(){}
}
