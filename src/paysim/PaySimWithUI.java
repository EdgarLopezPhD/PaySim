package paysim;

import java.awt.Color;

import javax.swing.JFrame;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

public class PaySimWithUI extends GUIState{

	public Display2D display;
	public JFrame displayFrame;
	public ContinuousPortrayal2D yardPortrayal = new ContinuousPortrayal2D();
	PaySim paysim = new PaySim(2);
	
	public PaySimWithUI() {
		super(new PaySim("C://Users//ahmad//Desktop//EclipseProjs//git//projects//ahmad//paysim//paysim1.properties"));
	}
	
	public PaySimWithUI(SimState state) {
		super(state);
	}
	
	public static void main(String[] args) {
		PaySimWithUI vid = new PaySimWithUI();
		//The console is the GUI control which allows us to start/stop/pause etc
		Console c = new Console(vid);
		
		//Make the console visibile
		c.setVisible(true);
	}
	
	public static String getName() {
		return "PaySim simulator";
	}

	
	public void start(){
		super.start();
		setupPortrayals();
	}
	
	public void setupPortrayals(){
		PaySim paysim = (PaySim) state;
		
		//To customize the students, one can just create own sim.portrayal.SimplePortrayal2D subclass;
		//or have the Students themselves subclass from SimplePortrayal2D. But you could also take an existing
		//SimplePortrayal2D and modify it: typically change its size or its color. Thats whats done here
		yardPortrayal.setPortrayalForAll(new OvalPortrayal2D());

		// reschedule the displayer
		display.reset();
		display.setBackdrop(Color.white);
		// redraw the display
		display.repaint();
	}
	
	public void init(Controller c){
		super.init(c);
		display = new Display2D(600,600,this);
		display.setClipping(false);
		displayFrame = display.createFrame();
		displayFrame.setTitle("Schoolyard Display");
		c.registerFrame(displayFrame); // so the frame appears in the "Display" list
		displayFrame.setVisible(true);
		display.attach( yardPortrayal, "Yard" );
	}
	
	public void quit(){
		super.quit();
		if(this.displayFrame == null){
			displayFrame.dispose();
			displayFrame = null;
			display = null;
		}
	}
	
	
	
	
	
	
	
	
	
	public Display2D getDisplay() {
		return display;
	}

	public void setDisplay(Display2D display) {
		this.display = display;
	}

	public JFrame getDisplayFrame() {
		return displayFrame;
	}

	public void setDisplayFrame(JFrame displayFrame) {
		this.displayFrame = displayFrame;
	}

	public ContinuousPortrayal2D getYardPortrayal() {
		return yardPortrayal;
	}

	public void setYardPortrayal(ContinuousPortrayal2D yardPortrayal) {
		this.yardPortrayal = yardPortrayal;
	}


}




























