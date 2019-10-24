package centralized;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import logist.LogistSettings;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class CentralizedAgent implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
        List<Plan> plans = CSPPlan(vehicles, tasks);
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        
        return plans;
    }
    
    private List<Plan> CSPPlan(List<Vehicle> vehicles, TaskSet tasks) {
    	// TODO
    	// need stopping criteria
    	// uses selectInitialSolution() and localChoice()
    	System.out.println(vehicles);
    	for(Task task : tasks){
    		System.out.println(task);
    	}
    	// Get the initial solution
    	Assignment A = selectInitialSolution(vehicles, tasks);
    	
    	return A.getPlans();
    }
    
    private Assignment selectInitialSolution(List<Vehicle> vehicles, TaskSet tasks) {
    	// Create an empty assignment
    	Assignment A = new Assignment(tasks, vehicles);
    	
    	// Get the largest vehicle
    	int largestVehicleIndex = 0;
    	int largestCapacity = 0;
    	for(int i = 0; i < A.getVehicles().size(); i++){
    		if(A.getVehicles().get(i).capacity() > largestCapacity){
    			largestCapacity = A.getVehicles().get(i).capacity();
    			largestVehicleIndex = i;
    		}
    	}
    	
    	// Assign all tasks to this vehicle
    	for(int i = 0; i < A.getTasks().size(); i++){
    		A.addTask(largestVehicleIndex, i);
    	}
    	return A;
    }
    
    private Assignment localChoice(List<Assignment> N, Double proba) {
    	// TODO
    	return null;
    }
}
