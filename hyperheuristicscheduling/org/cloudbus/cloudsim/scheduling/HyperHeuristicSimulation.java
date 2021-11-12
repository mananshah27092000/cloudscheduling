package org.cloudbus.cloudsim.scheduling;

/*
    Authors: Manan Shah and Manul Goyal
    Roll No.: B18CSE030 and B18CSE031
*/

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.io.File;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;


import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import org.cloudbus.cloudsim.scheduling.FIFO;
import org.cloudbus.cloudsim.scheduling.HyperHeuristicScheduling;

public class HyperHeuristicSimulation{
	/*
	1 : HyperHeuristic
	2 : FIFO 
	To run ACO or GA changes required in HyperHeruristicScheduling file with value 1
	*/
	private static int algoToSimulate = 1;


	// Which cloudlet dataset is being used from j30, j60, j90 and j120
	private static int cloudletscount = 30;

	private static String filepath = "j30.sm\\j30";

	private static String makespanfile =  filepath.concat("results_HHSA.txt");

	// runs of the algorithm, we have reported results of 30 runs
	private static int runs = 1;

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmList. */
	private static List<Vm> vmList;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args)  {

		Log.printLine("HyperHueristic Algorithm starting to run.....");
		ArrayList<Double> makeSpan = new ArrayList<Double>();

	        try {
				while(runs>0){
					runs--;

	        	    //Initializing the CloudSim package.Called before creating any entities.
	            	int num_user = 1;   // number of cloud users
	            	Calendar calendar = Calendar.getInstance();
	            	boolean trace_flag = false;  // mean trace events

	            	// Initialize the CloudSim library
	            	CloudSim.init(num_user, calendar, trace_flag);

	            	//Create Datacenters
	            	@SuppressWarnings("unused")
					Datacenter datacenter0 = createDatacenter("Datacenter_0");

	            	//Create Broker
	            	DatacenterBroker broker = createBroker();
	            	int brokerId = broker.getId();

	            	//Create Virtual machines as mentioned in the paper
	            	vmList = createVMs(brokerId);

	            	//Submit vm list to the broker
	            	broker.submitVmList(vmList);

					// Create cloudlets as per the dataset
					cloudletList = createCloudlets(brokerId, "1_1.sm");

	            	//submit cloudlet list to the broker
	            	broker.submitCloudletList(cloudletList);

					Cloudlet[] cloudletArray = new Cloudlet[cloudletList.size()];
					Vm[] vmArray = new Vm[vmList.size()];
					cloudletList.toArray(cloudletArray);
					vmList.toArray(vmArray);

					Log.setDisabled(true);
					
					if(algoToSimulate == 1){
						// TO run HyperHeuristic/ GN / ACO / HyperHeuristicRoundRobin and passing parameters as per paper guidelines
						HyperHeuristicScheduling hueristic = new HyperHeuristicScheduling(cloudletArray, vmArray, 50, 1000, 5, 100.0);
						int[] computedSchedule = hueristic.runHyperHeuristic();
					
						//bind the cloudlets to the vms. This way, the broker
						// will submit the bound cloudlets only to the specific VM
						for (int i = 0; i < cloudletArray.length; i++){
							int scheduledVmId = computedSchedule[i];
							broker.bindCloudletToVm(cloudletArray[i].getCloudletId(), vmArray[scheduledVmId].getId());
						}
					}else if(algoToSimulate == 2){
						// To run FIFO

						FIFO fifo = new FIFO(cloudletArray, vmArray);
						int[] computedSchedule = fifo.runFIFO();
					
						//bind the cloudlets to the vms. This way, the broker
						// will submit the bound cloudlets only to the specific VM
						for (int i = 0; i < cloudletArray.length; i++){
							int scheduledVmId = computedSchedule[i];
							broker.bindCloudletToVm(cloudletArray[i].getCloudletId(), vmArray[scheduledVmId].getId());
						}
					}

	            	
	            	//Starts the simulation
					CloudSim.startSimulation();

	            	// Print results when simulation is over
	            	List<Cloudlet> newList = broker.getCloudletReceivedList();

					Log.setDisabled(false);
	            	
					CloudSim.stopSimulation();
					Log.printLine("Make Span: " + getMakeSpan(newList));
					
	            	printCloudletList(newList);
					Log.setDisabled(true);
	            	
					
					makeSpan.add(getMakeSpan(newList));
				}
				// Uncommment to save the results of makespan of runs
				saveResults(makeSpan);
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	            Log.printLine("The simulation has been terminated due to an unexpected error");
	        }
	    }

		// Saving make span
		private static void saveResults(ArrayList<Double> makespan){
			try{
				double sum = 0;
				double mini = 1000000;
				double maxi = -1;
				FileWriter  file     = new FileWriter (makespanfile);
				for(int i = 0; i < makespan.size(); i++){
					file.write( makespan.get(i) + "\n");
					sum += makespan.get(i);
					mini = Math.min(mini, makespan.get(i));
					maxi = Math.max(maxi, makespan.get(i));
				}
				double avg = sum/makespan.size();
				file.write("Average Time: "+ avg + "\n");
				file.write("Best Time: "+ mini + "\n");
				file.write("Worst Time: "+maxi + "\n");
				
				double sd   = 0;
				for(int i = 0; i < makespan.size(); i++){
					sd += Math.pow(makespan.get(i) - avg, 2);
				}
				sd =  Math.sqrt(sd/makespan.size());
				file.write("S.D.:" + sd + "\n");
				file.close();
			}catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
    		}
		}

		//Create Cloudlets/jobs
		private static  List<Cloudlet> createCloudlets(int brokerId, String suffix) throws Exception{

			cloudletList = new ArrayList<Cloudlet>();

			// Log.printLine(filepath.concat(suffix));
			File file     = new File(filepath.concat(suffix));	
			Scanner sc 	  = new Scanner(file);
			int skiplines = getJobStart();

			for (int i=0; i < skiplines-1; i++)sc.nextLine();

			//Cloudlet properties
			int pesNumber 					  = 1;
			int mips      					  = 95;
			long bw 	  					  = 300;
			long outputSize 				  = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();
			long length,fileSize;
			
			for(int id=0; id<cloudletscount; id++){
				Scanner line = new Scanner(sc.nextLine());
				line.nextInt();
				line.nextInt();

				// Creating cloudlet for this task
				int duration = line.nextInt();
				length 		 = duration * mips;
				fileSize 	 = duration * bw / 10;

				// Create cloudlet
				Cloudlet tempcloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
				tempcloudlet.setUserId(brokerId);
				
				//add the cloudlets to the list
				cloudletList.add(tempcloudlet);
			}


			return cloudletList;
		}
	
		// To get the line number from which we can job duration from j30, j60 and j90 datasets
		private static int getJobStart(){
			if(cloudletscount == 30 || cloudletscount == 32){
				cloudletscount = 32;
				return 55; 
			}else if(cloudletscount == 120){
				return 145;
			}else if(cloudletscount == 60){
				return 85;
			}else{
				return 115;
			}
		}

		// Creating four VMs
        private static List<Vm> createVMs(int brokerId){
            ArrayList<Vm> fourVms = new ArrayList<Vm> (); 

            //VM description
            int vmid 		= 0;
            int mips 		= 250;
            long size	 	= 10000; //image size (MB)
            int ram 		= 512; //vm memory (MB)
            long bw 		= 500;
            int pesNumber 	= 1; //number of cpus
            String vmm 		= "Xen"; //VMM name

            //create four VMs
            Vm vm1 = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            fourVms.add(vm1);

            vmid++;
            Vm vm2 = new Vm(vmid, brokerId, mips, pesNumber, ram, 2*bw, size, vmm, new CloudletSchedulerSpaceShared());
            fourVms.add(vm2);
            
            vmid++;
            Vm vm3 = new Vm(vmid, brokerId, mips, pesNumber, 2*ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            fourVms.add(vm3);
            
            vmid++;
            Vm vm4 = new Vm(vmid, brokerId, mips, pesNumber, 2*ram, 2*bw, size, vmm, new CloudletSchedulerSpaceShared());
            fourVms.add(vm4);

            return fourVms;
        }

		private static Datacenter createDatacenter(String name){

	        // Here are the steps needed to create a PowerDatacenter:
	        // 1. We need to create a list to store
	    	//    our machine
	    	List<Host> hostList = new ArrayList<Host>();

	        // 2. A Machine contains one or more PEs or CPUs/Cores.
	    	// In this example, it will have only one core.
	    	List<Pe> peList = new ArrayList<Pe>();

	    	int mips = 1000;

	        // 3. Create PEs and add these into a list.
	    	peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

	        //4. Create Host with its id and list of PEs and add them to the list of machines
	        int hostId=0;
	        int ram = 2*2048; //host memory (MB)
	        long storage = 1000000; //host storage
	        int bw = 10000;

	        hostList.add(
	    			new Host(
	    				hostId,
	    				new RamProvisionerSimple(ram),
	    				new BwProvisionerSimple(bw),
	    				storage,
	    				peList,
	    				new VmSchedulerTimeShared(peList)
	    			)
	    		); // This is our machine


	        // 5. Create a DatacenterCharacteristics object that stores the
	        //    properties of a data center: architecture, OS, list of
	        //    Machines, allocation policy: time- or space-shared, time zone
	        //    and its price (G$/Pe time unit).
	        String arch = "x86";      // system architecture
	        String os = "Linux";          // operating system
	        String vmm = "Xen";
	        double time_zone = 10.0;         // time zone this resource located
	        double cost = 3.0;              // the cost of using processing in this resource
	        double costPerMem = 0.05;		// the cost of using memory in this resource
	        double costPerStorage = 0.001;	// the cost of using storage in this resource
	        double costPerBw = 0.0;			// the cost of using bw in this resource
	        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

	        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
	                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


	        // 6. Finally, we need to create a PowerDatacenter object.
	        Datacenter datacenter = null;
	        try {
	            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return datacenter;
	    }

	    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	    //to the specific rules of the simulated scenario
	    private static DatacenterBroker createBroker(){

	    	DatacenterBroker broker = null;
	        try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	    	return broker;
	    }

		// return makespan for the given list of cloudlet schedule
		private static double getMakeSpan(List<Cloudlet> list){
			int size = list.size();
			double makespan = -1;
			for(int i = 0; i < size; i++){
				makespan = Math.max(makespan, list.get(i).getFinishTime());
			}
			if(algoToSimulate == 2)makespan-=15.5;
			return makespan;
		}
	    /**
	     * Prints the Cloudlet objects
	     * @param list  list of Cloudlets
	     */
	    private static void printCloudletList(List<Cloudlet> list) {
	        int size = list.size();
	        Cloudlet cloudlet;

	        String indent = "         ";
	        Log.printLine();
	        Log.printLine("========== OUTPUT ==========");
	        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
	                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

	        DecimalFormat dft = new DecimalFormat("###.##");
	        for (int i = 0; i < size; i++) {
	            cloudlet = list.get(i);
	            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

	            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
	                Log.print("SUCCESS");

	            	Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
	                     indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                             indent + indent + dft.format(cloudlet.getFinishTime()));
	            }
	        }

	    }
}
