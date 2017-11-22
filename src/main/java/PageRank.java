import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class PageRank {
    private HashMap<Integer,Boolean> runMap = new HashMap<>();
    private ArrayList<Boolean> stopMap = new ArrayList<Boolean>();
    private HashMap<Integer, HashMap<Integer, Double>> messageMap = new HashMap<>();
    private HashMap<Integer, ArrayList<Integer>> adjList = new HashMap<>();
    private Double initialWeight;
    private Double[] vertexWeights;

    public PageRank(int size, int[] fromVertices, int[] toVertices) {
        this.vertexWeights = new Double[size];
        this.initialWeight = 1.0/size;
        for(int i = 0; i < size; i++){
            this.runMap.put(i,true);
            this.stopMap.add(false);
            this.messageMap.put(i,new HashMap<Integer,Double>());
            this.adjList.put(i, new ArrayList<Integer>());
            this.vertexWeights[i] = this.initialWeight;
        }

        for(int i = 0; i < fromVertices.length; i++){
            Integer fromVertex = fromVertices[i];
            Integer toVertex = toVertices[i];
            this.adjList.get(fromVertex).add(toVertex);
        }

        ArrayList<Integer> defaultList = new ArrayList<>();
        for(int i = 0; i < size; i++){
            defaultList.add(i);
        }

        for(int key:this.adjList.keySet()){
            ArrayList<Integer> copyList = new ArrayList<>(defaultList);
            if(this.adjList.get(key).size()==0){
                copyList.remove(key);
                this.adjList.put(key,copyList);
            }
        }

        for(int i =  0; i < size; i++){
            ArrayList<Integer> edges = this.adjList.get(i);
            for(Integer edge:edges){
                HashMap<Integer,Double> queue = this.messageMap.get(edge);
                queue.put(i,this.initialWeight/edges.size());
                this.messageMap.put(edge,queue);
            }
        }
    }

    public void run() throws InterruptedException {
        Queue<Thread> threadList = new LinkedList<>();
        for(int i=0; i<this.vertexWeights.length; i++) {
            Thread startVertex = new Thread(new PageRankVertex(i, this.vertexWeights, this.adjList.get(i), this.runMap,
                    this.stopMap, this.messageMap, false));
            startVertex.start();
            threadList.add(startVertex);
        }

        while(!threadList.isEmpty()) {
            try {
                Thread currThread = threadList.remove();
                currThread.join();
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        for(int i = 0; i < this.vertexWeights.length; i++){
            System.out.println(vertexWeights[i]);
        }

    }

    public static void main(String[] args) {
        // Graph has vertices from 0 to `size-1`
        // and edges 1->0, 2->0, 3->0
        int size = 5;
        int[] from = {1,2,3};
        int[] to = {2,1,0};

        PageRank pr = new PageRank(size, from, to);

        try {
            pr.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
