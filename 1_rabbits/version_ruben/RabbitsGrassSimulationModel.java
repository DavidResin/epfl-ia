import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.event.SliderListener;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.engine.SimInit;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		

		// Default Values
	  	private static final int GRIDSIZE = 20;
	  	private static final int AGENT_MIN_LIFESPAN = 30;
	    private static final int AGENT_MAX_LIFESPAN = 50;
	    private static final int NUM_INIT_RABBITS = 10;
	    private static final int NUM_INIT_GRASS = 10;
	    private static final int GRASS_GROWTH_RATE = 5;
	  
		private Schedule schedule;
		private int gridSize = GRIDSIZE;
		private int numInitRabbits = NUM_INIT_RABBITS;
		private int numInitGrass = NUM_INIT_GRASS;
		private int grassGrowthRate = GRASS_GROWTH_RATE;
		private int birthThreshold;
		private int agentMinLifespan = AGENT_MIN_LIFESPAN;
		private int agentMaxLifespan = AGENT_MAX_LIFESPAN;
		
		private RabbitsGrassSimulationSpace rgsSpace;
		private DisplaySurface displaySurf;
		
		private ArrayList agentList;
		
		private OpenSequenceGraph amountOfGrassInSpace;
		
		class grassInSpace implements DataSource, Sequence {
			public Object execute(){
				return new Double(getSValue());
			}
			
			public double getSValue(){
				return (double) rgsSpace.getTotalGrass();
			}
		}
		
		class GridSizeSliderListener extends SliderListener{
			public void execute(){
				gridSize = value;
			}
		}
	
		public static void main(String[] args) {
			
			System.out.println("Rabbit skeleton");

			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
			// Do "not" modify the following lines of parsing arguments
			if (args.length == 0) // by default, you don't use parameter file nor batch mode 
				init.loadModel(model, "", false);
			else
				init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));
			
		}
		
		public void setup() {
			System.out.println("Running setup");
			rgsSpace = null;
			agentList = new ArrayList();
			schedule = new Schedule(1);
			
			// Tear down Displays
			if (displaySurf != null){
		      displaySurf.dispose();
		    }
		    displaySurf = null;
		    
		    if(amountOfGrassInSpace != null){
		    	amountOfGrassInSpace.dispose();
		    }
		    amountOfGrassInSpace = null;
		    
		    // Create Displays
		    displaySurf = new DisplaySurface(this, "Rabbits Grass Simulation Model Window 1");
		    amountOfGrassInSpace = new OpenSequenceGraph("Amount of Grass in Space", this);
		    
		    // Register Displays
		    registerDisplaySurface("Rabbits Grass Simulation Model Window 1", displaySurf);
		    this.registerMediaProducer("Plot", amountOfGrassInSpace);
		}

		public void begin() {
			buildModel();
		    buildSchedule();
		    buildDisplay();
		    
		    displaySurf.display();
		    amountOfGrassInSpace.display();
		}
		
		public void buildModel(){
			System.out.println("Running BuildModel");
			rgsSpace = new RabbitsGrassSimulationSpace(gridSize, gridSize);
			rgsSpace.spreadGrass(numInitGrass);
			
			for(int i = 0; i < numInitRabbits; i++){
			      addNewAgent();
			}
			for(int i = 0; i < agentList.size(); i++){
			      RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)agentList.get(i);
			      rgsa.report();
			}
		}
	
		public void buildSchedule(){
			System.out.println("Running BuildSchedule");
			
			class RabbitsGrassSimulationStep extends BasicAction {
			      public void execute() {
			        SimUtilities.shuffle(agentList);
			        for(int i =0; i < agentList.size(); i++){
			          RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)agentList.get(i);
			          rgsa.step();
			        }
			        
			        reapDeadAgents();
			        
			        displaySurf.updateDisplay();
			      }
			 }

			 schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());
			 
			 class RabbitsGrassCountLiving extends BasicAction {
				 public void execute() {
					 countLivingAgents();
				 }
			 }
			 
			 schedule.scheduleActionAtInterval(10, new RabbitsGrassCountLiving());
			 
			 class RabbitsGrassUpdateGrassInSpace extends BasicAction {
				 public void execute(){
					 amountOfGrassInSpace.step();
				 }
			 }
			 
			 schedule.scheduleActionAtInterval(10, new RabbitsGrassUpdateGrassInSpace());
		}
	
		public void buildDisplay(){
			modelManipulator.init();
		    modelManipulator.addSlider("Grid size", 1, 100, 1, new GridSizeSliderListener());
		    
			System.out.println("Running BuildDisplay");
			
			ColorMap map = new ColorMap();

		    for(int i = 1; i<16; i++){
		      map.mapColor(i, new Color(0, (int)(i * 8 + 127), 0));
		    }
		    map.mapColor(0, Color.black);

		    Value2DDisplay displayGrass =
		        new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);
		    
		    Object2DDisplay displayAgents = new Object2DDisplay(rgsSpace.getCurrentAgentSpace());
		    displayAgents.setObjectList(agentList);

		    displaySurf.addDisplayableProbeable(displayGrass, "Grass");
		    displaySurf.addDisplayableProbeable(displayAgents, "Agents");
		    
		    amountOfGrassInSpace.addSequence("Grass in Space", new grassInSpace());
		    
		}
		
		private void addNewAgent(){
		    RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(agentMinLifespan, agentMaxLifespan);
		    agentList.add(a);
		    rgsSpace.addAgent(a);
		}
		
		private int reapDeadAgents(){
			int count = 0;
			for(int i = (agentList.size() - 1); i >=0; i--){
				RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentList.get(i);
				if(agent.getEnergy() < 1){
					rgsSpace.removeAgentAt(agent.getX(), agent.getY());
					agentList.remove(i);
					count++;
				}
			}
			return count;
		}
		
		private int countLivingAgents(){
			int livingAgents = 0;
			for(int i = 0; i < agentList.size(); i++){
				RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent) agentList.get(i);
				if(agent.getEnergy() > 0) livingAgents++;
			}
			System.out.println("Number of living agents is: " + livingAgents);
			
			return livingAgents;
		}

		public String[] getInitParam() {
			// Parameters to be set by users via the Repast UI slider bar
			// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
			String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold", "AgentMinLifespan", "AgentMaxLifespan"};
			return params;
		}

		public String getName() {
			return "RabbitsGrass Simulation Model";
		}

		public Schedule getSchedule() {
			return schedule;
		}

		public int getGridSize() {
			return gridSize;
		}

		public void setGridSize(int gridSize) {
			this.gridSize = gridSize;
		}

		public int getNumInitRabbits() {
			return numInitRabbits;
		}

		public void setNumInitRabbits(int numInitRabbits) {
			this.numInitRabbits = numInitRabbits;
		}

		public int getNumInitGrass() {
			return numInitGrass;
		}

		public void setNumInitGrass(int numInitGrass) {
			this.numInitGrass = numInitGrass;
		}

		public int getGrassGrowthRate() {
			return grassGrowthRate;
		}

		public void setGrassGrowthRate(int grassGrowthRate) {
			this.grassGrowthRate = grassGrowthRate;
		}

		public int getBirthThreshold() {
			return birthThreshold;
		}

		public void setBirthThreshold(int birthThreshold) {
			this.birthThreshold = birthThreshold;
		}

		public int getAgentMinLifespan() {
			return agentMinLifespan;
		}

		public void setAgentMinLifespan(int agentMinLifespan) {
			this.agentMinLifespan = agentMinLifespan;
		}

		public int getAgentMaxLifespan() {
			return agentMaxLifespan;
		}

		public void setAgentMaxLifespan(int agentMaxLifespan) {
			this.agentMaxLifespan = agentMaxLifespan;
		}
}
