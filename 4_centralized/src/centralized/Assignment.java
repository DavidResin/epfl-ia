package centralized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class Assignment {
	private final List<Task> tasks;
	private final List<Vehicle> vehicles;
	
	private List<List<Integer>> orders; // Map from a vehicle id to a list of task ids (pickup and deliveries)
	
	public Assignment(TaskSet tasks, List<Vehicle> vehicles){
		this.tasks = new ArrayList<Task>();
    	for(Task task : tasks){
    		this.tasks.add(task);
    	}
		this.vehicles = vehicles;
		this.orders = new ArrayList<List<Integer>>(vehicles.size());
		for(int i = 0; i < vehicles.size(); i++){
			this.orders.add(new ArrayList<Integer>());
		}
	}

	public Assignment(List<Task> tasks, List<Vehicle> vehicles, List<List<Integer>> orders) {
		this.tasks = tasks;
		this.vehicles = vehicles;
		this.orders = orders;
	}
	
	public Assignment deepCopy() {
		List<List<Integer>> newOrders = new ArrayList<List<Integer>>();
		
		for (List<Integer> sublist : this.orders)
		    newOrders.add(new ArrayList<Integer>(sublist));
		
		return new Assignment(this.tasks, this.vehicles, newOrders);
	}
	
	private double cost() {
		double sum = 0;
		
		for (int i = 0; i < vehicles.size(); i++) {
			City currCity = vehicles.get(i).getCurrentCity();
			City nextCity;
			double cpkm = vehicles.get(i).costPerKm();
			List<Integer> picked = new ArrayList<Integer>();
			
			for (int action : orders.get(i)) {
				if (picked.contains(action)) {
					picked.remove(picked.indexOf(action));
					nextCity = tasks.get(action).deliveryCity;
				} else {
					picked.add(action);
					nextCity = tasks.get(action).pickupCity;
				}
				
				sum += cpkm * currCity.distanceUnitsTo(nextCity);
				currCity = nextCity;
			}
		}
		
		return sum;
	}
	
	private void swapTasks(int v_id, int t1_id, int t2_id) {
		Collections.swap(this.orders.get(v_id), t1_id, t2_id);
	}
    
    public Assignment changingTaskOrder(int v_id, int t1_id, int t2_id) {
    	Assignment newA = this.deepCopy();
    	newA.swapTasks(v_id, t1_id, t2_id);    	
    	return newA;
    }
	
	public void addTask(int v_id, int t_id) {
		List<Integer> moving = Arrays.asList(new Integer[]{t_id, t_id});
		this.orders.get(v_id).addAll(0, moving);
	}
	
	public void remTask(int v_id, int t_id) {
		List<Integer> moving = Arrays.asList(new Integer[]{t_id, t_id});
		this.orders.get(v_id).removeAll(moving);
	}

	// Move the first task of the src vehicle to the dst vehicle
	public Assignment changingVehicle(int v_src_id, int v_dst_id) {
		int t_id = this.orders.get(v_src_id).get(0);

		Assignment newA = this.deepCopy();
		newA.remTask(v_src_id, t_id);
		newA.addTask(v_dst_id, t_id);
		
		return newA;
	}
    
    public List<Assignment> chooseNeighbors() {
    	// TODO
    	return null;
    }
	
	private boolean isValid() {
		List<Boolean> presence = new ArrayList<Boolean>(Collections.nCopies(60, false));
		
		for (int i = 0; i < vehicles.size(); i++) {
			List<Integer> order = this.orders.get(i);
			
			for (int j = 0; j < tasks.size(); j++) {
				if (order.contains(j)) {
					// Test if one task is shared
					if (presence.get(j))
						return false;
						
					presence.set(j, true);
				}
			}
			
			int load = 0;
			List<Integer> picked = new ArrayList<Integer>();
			
			for (int action : order) {
				int weight = tasks.get(action).weight;
				
				if (picked.contains(action)) {
					picked.remove(picked.indexOf(action));
					load -= weight;
				} else {
					picked.add(action);
					load += weight;
					
					// Test of capacity overload
					if (load > vehicles.get(i).capacity())
						return false;
				}
				
				// Test if tasks left in vehicle
				if (!picked.isEmpty())
					return false;
			}
		}
		
		// Test if one task is untouched
		return !presence.contains(false);
	}
	
	public List<Plan> getPlans() {
		List<Plan> plans = new ArrayList<Plan>();
		
		for (Vehicle vehicle : vehicles) {
			Plan plan = new Plan(vehicle.getCurrentCity());
			List<Boolean> pickedUp = new ArrayList<Boolean>();
			
			for (int i = 0; i < tasks.size(); i++)
				pickedUp.add(false);
			
			City currentCity = vehicle.getCurrentCity();
			
			for (int i : orders.get(vehicles.indexOf(vehicle))) {
				if (!pickedUp.get(i)) {
					// Pickup
					City pickupCity = tasks.get(i).pickupCity; 
					// Check if it is necessary to move
					
					if (pickupCity != currentCity) {
						for (City pathCity : currentCity.pathTo(pickupCity)) {
							plan.appendMove(pathCity);
							currentCity = pathCity;
						}
					}
					
					// Pick up the packet
					plan.appendPickup(tasks.get(i));
					
					// Mark that this task was picked up
					pickedUp.set(i, true);
				} else {
					// Delivery
					City deliveryCity = tasks.get(i).deliveryCity;
					
					// Check if it is necessary to move
					if (deliveryCity != currentCity) {
						for (City pathCity : currentCity.pathTo(deliveryCity)) {
							plan.appendMove(pathCity);
							currentCity = pathCity;
						}
					}
					
					// Deliver the packet
					plan.appendDelivery(tasks.get(i));
				}
			}
			
			plans.add(plan);
		}
		
		return plans;
	}

	public List<List<Integer>> getOrders() {
		return orders;
	}

	public void setOrders(List<List<Integer>> orders) {
		this.orders = orders;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public List<Vehicle> getVehicles() {
		return vehicles;
	}
}
