package calculate;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CyclicBarrier;

/**
 * @author Thomas Bijl
 */
public class KochTask extends Task<ArrayList> implements Runnable, Observer{

    private KochFractal kochFractal;
    private KochManager kochManager;
    private EdgeEnum position;
    private CyclicBarrier cyclicBarrier;
	private ProgressBar progressBar;
	private Label label;
	
	private int edgesCalculated;
	private int currentLevel;


    public KochTask(KochManager kochManager, CyclicBarrier cyclicBarrier, EdgeEnum position, int currentLevel, ProgressBar progressBar, Label label)
    {
        this.kochFractal = new KochFractal(position,kochManager);
        this.kochFractal.setLevel(currentLevel);
		this.kochManager = kochManager;
		this.kochFractal.addObserver(this);
		this.cyclicBarrier = cyclicBarrier;
		this.progressBar = progressBar;
		this.label = label;
		this.position = position;
		if(progressBar!=null) {
			progressBar.progressProperty().bind(this.progressProperty());
			label.textProperty().bind(this.messageProperty());
		}
    }

	@Override
    public ArrayList<Edge> call() throws Exception
    {
		switch (position) {
			case LeftEdge:
				kochFractal.generateLeftEdge();
				break;
			case RightEdge:
				kochFractal.generateRightEdge();
				break;
			case BottomEdge:
				kochFractal.generateBottomEdge();
				break;
		}

		if (cyclicBarrier.await() == 0){

			kochManager.cancelThreadPool();
		}
		return new ArrayList<Edge>();
    }


    @Override
    public void update(Observable o, Object object)
    {
        Edge edge = (Edge) object;
		kochManager.addEdge(edge);

		edgesCalculated++;
			Platform.runLater(() -> {
				if(progressBar!=null) {
					updateProgress(edgesCalculated, kochFractal.getNrOfEdges() / 3);
					updateMessage(edgesCalculated + "");
				}
			});

		kochManager.updateTimestamp();
		
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

    }

	@Override
	protected void cancelled() {
		super.cancelled();
		kochFractal.cancelCalculation();
	}


}
