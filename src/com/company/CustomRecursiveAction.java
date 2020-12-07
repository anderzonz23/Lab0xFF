package com.company;
import java.util.concurrent.RecursiveAction;

public class CustomRecursiveAction extends
        RecursiveAction {
    int[][] CostMatrix;
    static int LowestNewCost;
    int Vertices;
    int[] LowestNewPath;
    int[] Nodes;
    int l;
    int r;
    CustomRecursiveAction(int[][] CostMatrix, int LowestNewCost, int Vertices, int[] LowestNewPath, int[] Nodes, int l, int r){

        this.CostMatrix = CostMatrix;
        this.LowestNewCost = LowestNewCost;
        this.Vertices = Vertices;
        this.LowestNewPath = LowestNewPath;
        this.Nodes = Nodes;
        this.l = l;
        this.r = r;



    }

    public int CalculateCost(int[][] CostMatrix, int[] Nodes){
        int cost = 0;

        for(int i = 0; i<Vertices; i++){

            if(i == Vertices-1)
                cost += CostMatrix[Nodes[i]][Nodes[0]];                  //cost to get back home from last node
            else
                cost += CostMatrix[Nodes[i]][Nodes[i+1]];
        }

        return cost;
    }

    public int[] swap(int[] Nodes, int i, int j){
        int temp = Nodes[i];
        int[] NodeArray = Nodes;
        NodeArray[i] = Nodes[j];
        NodeArray[j] = temp;
        return NodeArray;

    }

    @Override
    protected void compute() {
        if (l == r){
            int cost = CalculateCost(CostMatrix, Nodes);
            if (cost < LowestNewCost) {
                LowestNewCost = cost;
                for (int i=0; i<LowestNewPath.length; i++)
                    LowestNewPath[i] = Nodes[i];

            }
        }
        else{
            for (int i = l; i <= r; i++){
                if (i != 0) {
                    Nodes = swap(Nodes, l, i);
                    invokeAll(new CustomRecursiveAction(CostMatrix, LowestNewCost, Vertices, LowestNewPath, Nodes, l + 1, r));
                    Nodes = swap(Nodes, l, i);
                }
            }

        }
    }
}
