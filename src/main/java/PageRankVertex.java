import java.util.*;
import java.util.concurrent.*;
import static java.lang.Math.*;

public class PageRankVertex implements Vertex<Double, Double> {
    private static final Double damping = 0.85; // the damping ratio, as in the PageRank paper
    private static final Double tolerance = 1e-4; // the tolerance for converge checking

    private Integer vertexId;
    private ArrayList<Integer> toEdges;
    private volatile ArrayList<Boolean> runMap;
    private volatile ArrayList<Boolean> stopMap;
    private volatile HashMap<Integer,LinkedBlockingQueue<Double>> messageMap;
    private Double weight;
    private volatile static Boolean runStop;
    private volatile Double[] vertexWeights;
    private volatile static Object monitor = new Object();

    public PageRankVertex(Integer vertexId, Double[] vertexWeights, ArrayList<Integer> toEdges, ArrayList<Boolean> runMap,
                          ArrayList<Boolean> stopMap, HashMap<Integer, LinkedBlockingQueue<Double>> messageMap, Boolean runStop) {
        this.vertexId = vertexId;
        this.toEdges = toEdges;
        this.runMap = runMap;
        this.stopMap = stopMap;
        this.messageMap = messageMap;
        this.weight = vertexWeights[this.vertexId];
        this.runStop = runStop;
        this.vertexWeights = vertexWeights;
    }

    @Override
    public int getVertexID() {
        return this.vertexId;
    }

    @Override
    public Double getValue() {
        return this.weight;
    }

    @Override
    public void compute(Collection<Double> messages) {
        Double sumMessages = 0.0;
        for(Double i:messages){
            sumMessages += i;
        }
        this.weight = (1 - this.damping) + this.damping * sumMessages;
    }

    @Override
    public void sendMessageTo(int neighborID, Double message) {
        System.out.println(this.vertexId + message.toString());
        try {
            if(this.messageMap.containsKey(neighborID)) {
                synchronized (this.messageMap) {
                    LinkedBlockingQueue<Double> messages = this.messageMap.get(neighborID);
                    messages.put(message);
                    this.messageMap.put(neighborID, messages);
                }
            }
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void voteToHalt() {
        synchronized (this.stopMap){
            this.stopMap.set(this.vertexId,true);
        }
    }

    public void setRunStop(Boolean flag){
        synchronized (this.runStop) {
            this.runStop = flag;
        }
        synchronized (this.monitor) {
            this.monitor.notifyAll();
        }
    }

    public void waitOnRun(Boolean flag){
        while(this.runStop != flag){
            try {
                synchronized (this.monitor) {
                    this.monitor.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Double oldWeight = 0.0;
        Double delta = abs(this.weight - oldWeight);
        ArrayList<Double> messageList;
        Integer counter = 1;
        while(counter > 0){
            oldWeight = this.getValue();
            messageList = new ArrayList<Double>();
            LinkedBlockingQueue<Double> messages = this.messageMap.get(this.vertexId);
            while(!messages.isEmpty()){
                try {
                    messageList.add(messages.take());
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            this.compute(messageList);

            synchronized (this.runMap){
                this.runMap.set(this.vertexId, false);
            }
            Boolean flag = true;
            for(Boolean i:this.runMap){
                flag = flag && !i;
            }
            if (flag) {
                this.setRunStop(flag);
            }
            else {
                this.waitOnRun(true);
            }

            System.out.println(this.vertexId.toString() + this.weight/toEdges.size());
            for(Integer e:toEdges) {
                sendMessageTo(e, this.weight/toEdges.size());
            }
            delta = abs(this.weight - oldWeight);
            synchronized (this.runMap){
                this.runMap.set(this.vertexId,true);
            }
            flag = true;
            for(Boolean i:this.runMap){
                flag = flag && i;
            }
            if (flag) {
                this.setRunStop(!flag);
            }
            else {
                this.waitOnRun(false);
            }
            System.out.println(this.vertexId.toString() + messageList.toString());
            counter--;
        }
        this.voteToHalt();
        synchronized (this.runMap){
            this.runMap.remove(this.vertexId);
        }
        synchronized (this.messageMap){
            this.messageMap.remove(this.vertexId);
        }
        synchronized (this.vertexWeights){
            this.vertexWeights[vertexId] = this.weight;
        }
        return;
    }
}
