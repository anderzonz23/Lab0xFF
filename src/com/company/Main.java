package com.company;
import java.lang.Math;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;


public class Main {

    public static int Vertices = 10;
    public static int LowestCost = 999999999;
    public static int[] LowestPath = new int[Vertices];
    public static int AvgCorrect;
    public static int AvgGreedy;

    public static void main(String[] args) {

        int MaxCost = 100;    //only able to control RandomCostMatrix, but not the Euclidean graphs
        int MaxX = 100;
        int MaxY = 100;


        long before,after,total,totalTotal,nothing,nothingTotal = 0;
        totalTotal=0;

        for(int i=1; i<=10000; i++) {
            before = getCpuTime();
            after = getCpuTime();
            nothingTotal += (after - before);
        }
        nothing = nothingTotal/10000;

        for(int i=1; i<=1; i++) {
            //int[][] CostMatrix = GenerateRandomEuclideanCostMatrix(Vertices, MaxX, MaxY);
            int[][] CostMatrix = GenerateRandomCircularGraphCostMatrix(Vertices, MaxX, MaxY);
            PrintCostMatrix(CostMatrix, Vertices);
            before = getCpuTime();
            TspGreedy(CostMatrix, Vertices);
            //TspBrute(CostMatrix, Vertices);
            //TspBruteParallel(CostMatrix, Vertices);
            after = getCpuTime();
            totalTotal += (after - before);

        }
        total = totalTotal/1 - nothing;
        System.out.println();
        System.out.println( "Time: " + total);
        //System.out.println( "Correctness Ratio: " + ((double)AvgGreedy/AvgCorrect));




        //int[][] CostMatrix = GenerateRandomCostMatrix(Vertices, MaxCost);
        //int[][] CostMatrix = GenerateRandomCircularGraphCostMatrix(Vertices, MaxX, MaxY);


    }

    public static int[][] GenerateRandomEuclideanCostMatrix(int Vertices, int MaxX, int MaxY){
        double[][] NodeMatrix = new double[Vertices][2];
        int[][] CostMatrix = new int[Vertices][Vertices];

        for(int i = 0; i < Vertices; i++){
            
            NodeMatrix[i][0] = (int)(Math.random()*1000)%(MaxX);
            NodeMatrix[i][1] = (int)(Math.random()*1000)%(MaxY);
        }

        for(int i = 0; i < Vertices; i++)
            for(int j = i+1; j < Vertices; j++){

                CostMatrix[i][j] = (int)Math.pow( Math.pow(NodeMatrix[i][0] - NodeMatrix[j][0], 2) + Math.pow(NodeMatrix[i][1] - NodeMatrix[j][1], 2), .5);
                CostMatrix[j][i] = CostMatrix[i][j];
            }
        return CostMatrix;
    }

    public static int[][] GenerateRandomCircularGraphCostMatrix(int Vertices, int MaxX, int MaxY){
        double[][] NodeMatrix = new double[Vertices][2];
        int[][] CostMatrix = new int[Vertices][Vertices];
        double StepAngle = 2*Math.PI/Vertices;
        int radius = 100;
        int[][] NodeUsed = new int[Vertices][1];
        Boolean Used = true;
        int RandomNode = 0;
        System.out.println("Path around the generated circle with coordinates: ");
        for(int i = 0; i < Vertices; i++){
            while(Used){
                RandomNode = (int)(Math.random()*1000)%(Vertices);
                if(NodeUsed[RandomNode][0] != 1)
                    Used = false;
            }
            Used = true;
            NodeUsed[RandomNode][0] = 1;

            NodeMatrix[RandomNode][0] = radius * Math.sin(i * StepAngle);
            NodeMatrix[RandomNode][1] = radius * Math.cos(i * StepAngle);
            System.out.print(RandomNode + " (" + (int)NodeMatrix[RandomNode][0] + ", " + (int)NodeMatrix[RandomNode][1] + ")" + " -> ");
        }
        System.out.println("Connects back to first Node to complete the circle");
        int Lowest = 999999999;
        for(int i = 0; i < Vertices; i++)
            for(int j = i+1; j < Vertices; j++){

                CostMatrix[i][j] = (int)Math.pow( Math.pow(NodeMatrix[i][0] - NodeMatrix[j][0], 2) + Math.pow(NodeMatrix[i][1] - NodeMatrix[j][1], 2), .5);
                CostMatrix[j][i] = CostMatrix[i][j];
                if(CostMatrix[i][j] < Lowest)
                    Lowest = CostMatrix[i][j];
            }
        System.out.println("Expected cost of the solution path: " + Lowest*Vertices);
        return CostMatrix;
    }

    public static int[][] GenerateRandomCostMatrix(int Vertices, int MaxCost){
        int[][] CostMatrix = new int[Vertices][Vertices];

        for(int i = 0; i < Vertices; i++)
            for(int j = i+1; j < Vertices; j++){

                CostMatrix[i][j] = (int)(1 + (Math.random()*1000)%(MaxCost-1));      // adding 1 in the beginning to ensure not zero. Then subtracting one from MaxCost
                CostMatrix[j][i] = CostMatrix[i][j];                                 // so that an edge does not exceed MaxCost

            }


        return CostMatrix;
    }

    public static void TspGreedy(int[][] CostMatrix, int Vertices ){
        int[] Visited = new int[Vertices];
        Visited[0] = 1;
        int ShortestPath;
        int NextNode = 0;
        int CurNode = 0;
        int cost = 0;
        System.out.print("0 -> ");
        for(int i = 0; i < Vertices-1; i++) {
            ShortestPath = 999999999;
            for (int j = 0; j < Vertices; j++) {
                if (CostMatrix[CurNode][j] > 0 && CostMatrix[CurNode][j] < ShortestPath && (Visited[j] != 1)) {
                    NextNode = j;
                    ShortestPath = CostMatrix[CurNode][j];

                }
            }
            cost += CostMatrix[CurNode][NextNode];
            Visited[NextNode] = 1;
            System.out.print(NextNode + " -> ");
            CurNode = NextNode;
        }
        cost += CostMatrix[CurNode][0];         //cost to get home from last node
        System.out.println("0");
        System.out.print("Cost: " + cost);
        AvgGreedy += cost;

    }

    public static void TspBruteParallel(int[][] CostMatrix, int Vertices){

        ForkJoinPool pool = new ForkJoinPool();


        int[] Nodes = new int[Vertices];
        for(int i = 0; i<Vertices; i++){
            Nodes[i] = i;
        }
        int l = 0;
        int r = Vertices-1;


        CustomRecursiveAction task = new CustomRecursiveAction(CostMatrix, LowestCost, Vertices, LowestPath, Nodes, l, r);
        pool.invoke(task);

        ArrayFix(LowestPath);

        for(int i = 0; i<Vertices; i++){
            System.out.print(LowestPath[i] + " -> ");
        }
        System.out.println("0");
        System.out.print("Cost: " + task.LowestNewCost);

    }

    public static void ArrayFix(int[] Jumbled){
        int[] temp1 = new int[Jumbled.length];
        int[] temp2 = new int[Jumbled.length];
        int pivot = 0;
        for(int i = 0; i < Jumbled.length; i++){

            if(Jumbled[i] == 0 ){
                temp1 = Arrays.copyOfRange(Jumbled, 0, i);
                temp2 = Arrays.copyOfRange(Jumbled, i, Jumbled.length);
                pivot = i;
                pivot = Jumbled.length - pivot;
            }

        }

        for(int j = 0; j < temp2.length; j++)
            Jumbled[j] = temp2[j];

        for(int k = 0; k < temp1.length; k++) {
            Jumbled[pivot] = temp1[k];
            pivot++;
        }
    }

    public static void TspBrute(int[][] CostMatrix, int Vertices){
        int[] Nodes = new int[Vertices];

        for(int i = 0; i<Vertices; i++){
            Nodes[i] = i;
        }
        int l = 0;
        int r = Vertices-1;
        permute(CostMatrix, Nodes, l, r);

        for(int i = 0; i<Vertices; i++){
            System.out.print(LowestPath[i] + " -> ");
        }
        System.out.println("0");
        System.out.print("Cost: " + LowestCost);

        AvgCorrect += LowestCost;
    }

    public static int CalculateCost(int[][] CostMatrix, int[] Nodes){
        int cost = 0;

        for(int i = 0; i<Vertices; i++){

            if(i == Vertices-1)
                cost += CostMatrix[Nodes[i]][Nodes[0]];                  //cost to get back home from last node
            else
                cost += CostMatrix[Nodes[i]][Nodes[i+1]];
        }

        return cost;
    }

    public static void permute(int[][] CostMatrix, int[] Nodes, int l, int r){
        if (l == r){
           int cost = CalculateCost(CostMatrix, Nodes);
           if (cost < LowestCost) {
               LowestCost = cost;
               LowestPath = Nodes.clone();
           }
        }
        else{
            for (int i = l; i <= r; i++){
                Nodes = swap(Nodes,l,i);
                permute(CostMatrix, Nodes, l+1, r);
                Nodes = swap(Nodes, l, i);
            }

        }


    }

    public static int[] swap(int[] Nodes, int i, int j){
        int temp = Nodes[i];
        int[] NodeArray = Nodes;
        NodeArray[i] = Nodes[j];
        NodeArray[j] = temp;
        return NodeArray;

    }

    public static void PrintCostMatrix(int[][] CostMatrix, int Vertices){
        System.out.println();
        int rows = -1;
        int cols = 0;
        for(int i = -1; i < Vertices; i++) {
            if(rows>=0)
                System.out.print(rows);
            else
                System.out.print(" ");
            rows++;
            for (int j = 0; j < Vertices; j++) {
                if(i<0) {
                    System.out.printf("%4d", cols);
                    cols++;
                }
                else
                    System.out.printf("%4d", CostMatrix[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    // Get CPU time in nanoseconds since the program(thread) started.
    /** from: http://nadeausoftware.com/articles/2008/03/java_tip_how_get_cpu_and_user_time_benchmarking#TimingasinglethreadedtaskusingCPUsystemandusertime **/
    public static long getCpuTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
                bean.getCurrentThreadCpuTime( ) : 0L;
    }

}