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

// Implements FIFO
public class FIFO{

	/** The cloudlet list. */
	public static Cloudlet[] cloudletList;

	/** The vmlist. */
	public static Vm[] vmList;

    public static int populationSize;

    public static int cloudletSize;

    public static int vmSize;

    public static double[][] cloudletExecTime;

    public static boolean initialized = false;
    
    public FIFO(Cloudlet[] cloudletList, Vm[] vmList){
        FIFO.cloudletSize       = cloudletList.length;
        FIFO.vmSize             = vmList.length;
        FIFO.cloudletList       = cloudletList.clone();
        FIFO.vmList             = vmList.clone();
    }

    // implements FIFO
	public int[] runFIFO(){

        Log.printLine("FIFO running .....");

        int[] bestIndividual = new int[cloudletSize];
        
        double[] vmOccupiedTime = new double[4];
        for(int i = 0; i < cloudletSize; i++){
            double mini = 100000;
            int vmId = -1;
            for(int j = 0; j < vmOccupiedTime.length; j++){
                if(vmOccupiedTime[j] < mini){
                    vmId = j;
                    mini = vmOccupiedTime[j];
                }
            }

            bestIndividual[i] = vmId;
            vmOccupiedTime[vmId] = getTime(i, vmId);
        }

        return bestIndividual;

    }

    // Get the makespan time
    public double getTime(int cloudletId, int vmId){
        
        if(!initialized){    
            cloudletExecTime = new double [cloudletSize][vmSize];
            for(int i=0; i < cloudletSize; i++){
                for(int j=0; j < vmSize; j++){
                    cloudletExecTime[i][j] = (double)cloudletList[i].getCloudletLength()/(double)(vmList[j].getNumberOfPes() + vmList[j].getMips()) + (double)cloudletList[i].getCloudletFileSize()/(double)vmList[j].getBw();
                }
            }
            initialized = true;
        }
        
        return cloudletExecTime[cloudletId][vmId];
    } 
}
