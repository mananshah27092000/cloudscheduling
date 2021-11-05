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

public abstract class  MetaHeuristicAlgorithms{

	/** The cloudlet list. */
	public List<Cloudlet> cloudletList;

	/** The vmlist. */
	public  List<Vm> vmList;

    public static double[][] dij;
    public static boolean dijCreated = false;

    public MetaHeuristicAlgorithms(Cloudlet[] cloudletList, Vm[] vmList){
        if(!dijCreated){
            int n = cloudletList.length;
            int m = vmList.length;
            dij = new double [n][m];

            for(int i=0; i < n; i++){
                for(int j=0; j < m; j++){
                    dij[i][j] = (double)cloudletList[i].cloudletLength/(double)(vmList[j].numberOfPes + vmList[i].mips) + (double)cloudletList[i].cloudletFileSize/(double)vmList[j].bw;
                }
            }
            dijCreated = true;
        }
    }
    
    public abstract void runNextGeneration(int[][] population, int[] bestIndividual);

    public double getQuality(int[] individual){
        int l = individual.length;
        double[] sum = new int[vmList.length];

        for(int i = 0; i < l; i++){
            sum[individual[i]] += dij[i][individual[i]];
        }
        double maxVM = -1;
        for(int i=0; i < vmList.length; i++){
            maxVM = Math.max(maxVM, sum[i]);
        }
        return maxVM;
    }   
    
}