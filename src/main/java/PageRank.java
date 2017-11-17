import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class PageRank {
    private ArrayList<Boolean> runMap = new ArrayList<Boolean>();
    private ArrayList<Boolean> stopMap = new ArrayList<Boolean>();
    private HashMap<Integer, LinkedBlockingQueue<Double>> messageMap = new HashMap<>();
    private HashMap<Integer, ArrayList<Integer>> adjList = new HashMap<>();
    private Double initialWeight;
    private Double[] vertexWeights;

    public PageRank(int size, int[] fromVertices, int[] toVertices) {
        this.vertexWeights = new Double[size];
        this.initialWeight = 1.0/size;
        for(int i = 0; i < size; i++){
            this.runMap.add(true);
            this.stopMap.add(false);
            this.messageMap.put(i,new LinkedBlockingQueue<Double>());
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
            if(this.adjList.get(key).size()==0){
                defaultList.remove(key);
                this.adjList.put(key,defaultList);
            }
        }

        for(int i =  0; i < size; i++){
            ArrayList<Integer> edges = this.adjList.get(i);
            for(Integer edge:edges){
                try {
                    LinkedBlockingQueue<Double> queue = this.messageMap.get(edge);
                    queue.put(this.initialWeight/edges.size());
                    this.messageMap.put(edge,queue);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
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
        int size = 4;
        int[] from = {1,2,3};
        int[] to = {0,0,0};

        PageRank pr = new PageRank(size, from, to);

        try {
            pr.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
