package calculate;

import javafx.application.Platform;

import timeutil.TimeStamp;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fun3kochfractalfx.FUN3KochFractalFX;


public class KochManager {

   
    
    private ArrayList<Edge> edges = new ArrayList<Edge>();
    private ExecutorService threadPool;
    private FUN3KochFractalFX application;
    private CyclicBarrier cyclicBarrier;
	private KochTask leftTask;
	private KochTask bottomTask;
	private KochTask rightTask;
	private TimeStamp tsCalc;
	private TimeStamp tsDraw;
	private int count = 1;

	

    public KochManager(FUN3KochFractalFX application) {
        this.application = application;
        this.tsCalc = new TimeStamp();
        this.tsDraw = new TimeStamp();
    }

	public void changeLevel(int nxt){
		
		this.edges.clear();
		 

		tsCalc.init();
        tsCalc.setBegin("Begin calculating");
        this.count = 0;

        
        this.threadPool = Executors.newFixedThreadPool(3);
        this.cyclicBarrier = new CyclicBarrier(3);

		this.leftTask = new KochTask(this,cyclicBarrier, EdgeEnum.LeftEdge, nxt, application.getProgressLeft(),application.getLabelProgressLeft());
		this.bottomTask = new KochTask(this,cyclicBarrier, EdgeEnum.BottomEdge,nxt,application.getProgressBottom(),application.getLabelProgressBottom());
		this.rightTask = new KochTask(this, cyclicBarrier, EdgeEnum.RightEdge,nxt, application.getProgressRight(),application.getLabelProgressRight());

		this.threadPool.submit(leftTask);
		this.threadPool.submit(bottomTask);
		this.threadPool.submit(rightTask);
tsCalc.setEnd();
		
        this.application.setTextCalc(tsCalc.toString());
    }


    public void drawEdges() {
    	 tsDraw.init();
         tsDraw.setBegin();
        application.clearKochPanel();
		synchronized (edges) {
			for (Edge e : edges) {
					application.drawEdge(e);	
			}
			tsDraw.setEnd();
	        application.setTextDraw(tsDraw.toString());
		}
    }

    public void cancelThreadPool() {
    	threadPool.shutdown();
        application.requestDrawEdges();
    }

	public void cancel() {
		leftTask.cancel();
		bottomTask.cancel();
		rightTask.cancel();
	}

	public synchronized void addEdge(Edge e )    {
        edges.add(e);
    }
	
	public synchronized void drawEdge(Edge edge){
		application.drawEdge(edge);
	}
	
	public void updateTimestamp() {
			Platform.runLater(() -> {
				application.setTextDraw(tsDraw.toString());
				application.setTextNrEdges(String.valueOf(edges.size()));
			});
	}

	public void setEdges(ArrayList<Edge> edges) {
		this.edges = edges;
	}
}



