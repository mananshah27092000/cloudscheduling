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
import java.util.Random;

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


// Abstract class for the underlying LLH
public abstract class MetaHeuristicAlgorithms{

	/** The cloudlet list. */
	public static Cloudlet[] cloudletList;

	/** The vmlist. */
	public static Vm[] vmList;

    public int[][] population;

    public int[] bestIndividual;
    
    public double bestQuality;

    public static int vmCount;
    
    public static int cloudletCount;
    
    public int populationSize;

    public static double[][] cloudletExecTime;
    
    public static boolean initialized = false;

    // constructor when population and bestIndividual are passed
    public MetaHeuristicAlgorithms(Cloudlet[] cloudletList, Vm[] vmList, int[][] population, int[] bestIndividual){

        if(!initialized){
            vmCount = vmList.length;
            cloudletCount = cloudletList.length;
            MetaHeuristicAlgorithms.vmList = vmList.clone();
            MetaHeuristicAlgorithms.cloudletList = cloudletList.clone();
            
            cloudletExecTime = new double [cloudletCount][vmCount];
            for(int i=0; i < cloudletCount; i++){
                for(int j=0; j < vmCount; j++){
                    cloudletExecTime[i][j] = (double)cloudletList[i].getCloudletLength()/(double)(vmList[j].getNumberOfPes() + vmList[j].getMips()) + (double)cloudletList[i].getCloudletFileSize()/(double)vmList[j].getBw();
                }
            }
            initialized = true;
        }

        this.population = population;
        this.bestIndividual = bestIndividual;
        if(bestIndividual != null) bestQuality = getQuality(bestIndividual);
        if(population != null) this.populationSize = population.length;
        
    }
    
    // constructor for the case when best indiviudal should be calculated automatically from population
    public MetaHeuristicAlgorithms(Cloudlet[] cloudletList, Vm[] vmList, int[][] population){ 
        this.population = population;
        if(population != null) this.populationSize = population.length;

        if(!initialized){
            MetaHeuristicAlgorithms.vmList = vmList;
            MetaHeuristicAlgorithms.cloudletList = cloudletList;
            vmCount = vmList.length;
            cloudletCount = cloudletList.length;
            
            cloudletExecTime = new double [cloudletCount][vmCount];

            for(int i=0; i < cloudletCount; i++){
                for(int j=0; j < vmCount; j++){
                    cloudletExecTime[i][j] = (double)cloudletList[i].getCloudletLength()/(double)(vmList[j].getNumberOfPes() + vmList[j].getMips()) + (double)cloudletList[i].getCloudletFileSize()/(double)vmList[j].getBw();
            
                }
            }
            initialized = true;
        }

        bestQuality = Double.POSITIVE_INFINITY;

        int bestIndividualIndex = -1;
        for(int i = 0; i < populationSize; i++) {
            double quality = getQuality(population[i]);
            if(quality < bestQuality) {
                bestQuality = quality;
                bestIndividualIndex = i;
            }
        }

        bestIndividual = population[bestIndividualIndex].clone();

    }

    // constructor for the case when population should be randomly initialized
    public MetaHeuristicAlgorithms(Cloudlet[] cloudletList, Vm[] vmList, int populationSize){ 
        if(!initialized){
            MetaHeuristicAlgorithms.vmList = vmList;
            MetaHeuristicAlgorithms.cloudletList = cloudletList;
            vmCount = vmList.length;
            cloudletCount = cloudletList.length;
            
            cloudletExecTime = new double [cloudletCount][vmCount];

            for(int i=0; i < cloudletCount; i++){
                for(int j=0; j < vmCount; j++){
                    cloudletExecTime[i][j] = (double)cloudletList[i].getCloudletLength()/(double)(vmList[j].getNumberOfPes() + vmList[j].getMips()) + (double)cloudletList[i].getCloudletFileSize()/(double)vmList[j].getBw();
                    
                
                }
            }
            initialized = true;
        }
        
        this.population = new int[populationSize][cloudletCount];
        this.populationSize = populationSize;

        Random rand = new Random();

        for(int i = 0; i < populationSize; i++) {
            for(int j = 0; j < cloudletCount; j++) {
                population[i][j] = rand.nextInt(vmCount);
            }
        }


        bestQuality = Double.POSITIVE_INFINITY;

        int bestIndividualIndex = -1;
        for(int i = 0; i < populationSize; i++) {
            double quality = getQuality(population[i]);
            if(quality < bestQuality) {
                bestQuality = quality;
                bestIndividualIndex = i;
            }
        }

        bestIndividual = population[bestIndividualIndex].clone();

    }

    // Generating the next generation for GA and ACO which is implemented in the child classes
    public abstract void runNextGeneration();

    // Gives the quality of individual
    public double getQuality(int[] individual){
        int l = individual.length;
        double[] sum = new double[vmList.length];

        for(int i = 0; i < l; i++){
            sum[individual[i]] += cloudletExecTime[i][individual[i]];
        }
        double maxVM = -1;
        for(int i=0; i < vmList.length; i++){
            maxVM = Math.max(maxVM, sum[i]);
        }
        return maxVM;
    }   
    
    // gives the best individual for the current population
    public int getBestIndividual() {
        if(population == null) return -1;

        double bestQuality = Double.POSITIVE_INFINITY;
        int bestIndividualInPopulation = -1;
        for(int i = 0; i < populationSize; i++) {
            double quality = getQuality(population[i]);
            if (quality < bestQuality) {
                bestQuality = quality;
                bestIndividualInPopulation = i;
            }
        }
        return bestIndividualInPopulation;
    }
}