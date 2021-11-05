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


import org.cloudbus.cloudsim.scheduling.MetaHeuristicAlgorithms;


public class HyperHeuristicScheduling{

	/** The cloudlet list. */
	public static Cloudlet[] cloudletList;

	/** The vmlist. */
	public static Vm[] vmList;

    public static int populationSize;

    public static int cloudletSize;

    public static int vmSize;

    public static int maxIterations;

    public static int maxIterNotImproved;

    public static double intitalDiversity;

    HyperHeuristicScheduling(Cloudlet[] cloudletList, Vm[] vmList, int size, int maxIterations, int maxIterNotImproved){
        cloudletSize       = cloudletList.length;
        vmSize             = vmList.length;
        cloudletList       = cloudletList.clone();
        vmList             = vmList.clone();
        populationSize     = size;
        maxIterations      = maxIterations;
        maxIterNotImproved = maxIterNotImproved;
    }

	public static void runHyperHeuristic(){

        Log.printLine("HyperHeuristic Algorithm starting to run.....");
        int[][] initialPopulation = new int[populationSize][cloudletSize];
        
        // initializing population
        for(int i = 0; i < populationSize; i++){
            for(int j = 0; j < cloudletSize; j++){
                initialPopulation[i][j] = (int)(4 * Math.random());
            }
        }
        
        intitalDiversity = getDiversity(initialPopulation) - 3 * getStandardDeviation(initialPopulation);
        // Log.printLine(intitalDiversity);
        for(int i = 0; i < maxIterations; i++){

        }

    }

    public static int[][] perturbation(int[][] population){

    }
    public static boolean changeHeuristic(int[][] population, int notImprovedIterations){
        if(improvementDetection(notImprovedIterations) && diversityDetection(population))return false;
        else return true;
    }

    public static boolean diversityDetection(int[][] population){
        if(getDiversity(population) > intitalDiversity) return true;
        else return false;
    }

    public static double getDiversity(int[][] population){
        int diversitySum = 0;
        for(int k = 0; k < cloudletSize; k++){
            for(int i = 0; i < populationSize; i++){
                for(int j = i+1; j < populationSize; j++){
                        if(population[i][k] != population[j][k]){
                            diversitySum++;
                        }
                }   
            }
        }

        return diversitySum/cloudletSize;
    }

    public static double getStandardDeviation(int[][] population){
        ArrayList<Integer> diversity = new ArrayList<Integer>();

        int sum = 0;
        for(int k = 0; k < cloudletSize; k++){
            int diversitySum = 0;
            for(int i = 0; i < populationSize; i++){
                for(int j = i+1; j < populationSize; j++){
                        if(population[i][k] != population[j][k]){
                            diversitySum++;
                        }
                }   
            }
            sum += diversitySum;
            diversity.add(diversitySum);
        }

        double mean = (double)sum/cloudletSize;
        double sd   = 0;
        for(int i = 0; i < diversity.size(); i++){
            sd += Math.pow(diversity.get(i) - mean, 2);
        }
        return Math.sqrt(sd/diversity.size());
    }
    public static boolean improvementDetection(int notImprovedIterations){
        if(notImprovedIterations > maxIterNotImproved){
            return false;
        }
        return true;
    }

}
