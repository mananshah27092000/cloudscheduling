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


import org.cloudbus.cloudsim.scheduling.MetaHeuristicAlgorithms;
import org.cloudbus.cloudsim.scheduling.antcolony.AntColonyTaskScheduler;
import org.cloudbus.cloudsim.scheduling.antcolony.AntColonyParameters;
import org.cloudbus.cloudsim.scheduling.genetic.GeneticScheduling;
import org.cloudbus.cloudsim.scheduling.genetic.GeneticParameters;

public class HyperHeuristicScheduling{

    // Which cloudlet dataset is being used from j30, j60, j90 and j120
	private static String filepath = "j30.sm\\j30";
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

    public static double Tmax;

    public static boolean rr = false;

    public static int current = 0;

    public HyperHeuristicScheduling(Cloudlet[] cloudletList, Vm[] vmList, int size, int maxIterations, int maxIterNotImproved, double Tmax){
        Log.printLine(vmList.length);
        HyperHeuristicScheduling.cloudletSize       = cloudletList.length;
        HyperHeuristicScheduling.vmSize             = vmList.length;
        HyperHeuristicScheduling.cloudletList       = cloudletList.clone();
        HyperHeuristicScheduling.vmList             = vmList.clone();
        HyperHeuristicScheduling.populationSize     = size;
        HyperHeuristicScheduling.maxIterations      = maxIterations;
        HyperHeuristicScheduling.maxIterNotImproved = maxIterNotImproved;
        HyperHeuristicScheduling.Tmax               = Tmax;
        Log.printLine(maxIterations);
    }

	public static int[] runHyperHeuristic(){

        ArrayList<Double> quality = new ArrayList<Double> ();

        Log.printLine("HyperHeuristic Algorithm starting to run.....");
        int[][] initialPopulation = new int[populationSize][cloudletSize];
        
        // initializing population
        for(int i = 0; i < populationSize; i++){
            for(int j = 0; j < cloudletSize; j++){
                initialPopulation[i][j] = (int)(4 * Math.random());
            }
        }
        
        intitalDiversity = getDiversity(initialPopulation) - 3 * getStandardDeviation(initialPopulation);
        Log.printLine(getDiversity(initialPopulation)+ " " + getStandardDeviation(initialPopulation));
        Log.printLine(intitalDiversity);

        MetaHeuristicAlgorithms LLH = getHeuristicInit();
        int[] bestIndividual = LLH.bestIndividual.clone();
        double bestQ = LLH.bestQuality;
        int[][] updatedPopulation;
        int notImprovedIterations = 0, iterationByLLH = 0;
        int LLHQualityValue = -1;

        // Iterations of Hueristic / Hyper hueristic algorithms
        for(int i = 0; i < maxIterations ; i++){
            Log.printLine("Runnig metahueristic iteration number:"+ i +"\n  Quality uptill now "+bestQ);
            LLH.runNextGeneration();
            updatedPopulation = LLH.population.clone();
            iterationByLLH++;
            int[] currentBest = updatedPopulation[LLH.getBestIndividual()].clone();
            double currentBestQ = LLH.getQuality(currentBest);
            quality.add(bestQ);


            if(bestQ - currentBestQ < 1e-4){
                notImprovedIterations++;
            }
            if(currentBestQ < bestQ){
                bestQ          = currentBestQ;
                bestIndividual = currentBest;
            }

            // Comment the if condition to not change the heuristic function. (So if GA is selected at first the we will get results for GA as heuristic don't change)
            // This the main part of Hyper Heuristic paper
            if(changeHeuristic(updatedPopulation, notImprovedIterations)){
                Log.printLine("Changing heuristic");
                updatedPopulation = perturbation(updatedPopulation, LLH, iterationByLLH);
                LLH = getHeuristic(updatedPopulation, bestIndividual);
                iterationByLLH = 0;
                notImprovedIterations = 0;
            }

        }

        // Uncomment to save the change in makespan in iterations
        // saveQuality(quality);
        return bestIndividual;

    }


    private static void saveQuality(ArrayList<Double> quality){
        try{
            FileWriter  file = new FileWriter (filepath.concat("resultsBSFMP_HHSA.txt"));
            for(int i = 0; i < quality.size(); i++){
                file.write( quality.get(i) + "\n");
            }
            file.close();
        }catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // Function to call for heuristic in round robin fashion
    public static int RoundRobin(){
        int temp = current;

        if(current == 1)current = 0;
        else current = 1;

        return temp;
    }

    // Function is called to change heuristic
    public static  MetaHeuristicAlgorithms getHeuristic(int[][] population, int[] bestIndividual){
        int hueristicNumber = (int)(2*Math.random());

        if(rr)hueristicNumber = RoundRobin();
        
        Log.printLine(hueristicNumber);
        AntColonyParameters ACOparameters = new AntColonyParameters() {
            {
                evaporationRate = 0.5;
                pheromoneWeight = 2;
                heuristicWeight = 1;
                pheromoneUpdationRate = 100;
                antsPerGeneration = 8;
                initPheromone = 1;
                tabuStrategy = false;
            }
        };

        GeneticParameters GNparameters = new GeneticParameters(){
            {
                mutationRate = 0.95;
                crossOverRate = 0.01;
                rouletteThreshold = 150;
            }
        };

        if(hueristicNumber == 0){
            return new AntColonyTaskScheduler(cloudletList, vmList, population, bestIndividual, ACOparameters);
        }else if(hueristicNumber == 1){
            return new GeneticScheduling(cloudletList, vmList, population, bestIndividual, GNparameters);
        }
        return new AntColonyTaskScheduler(cloudletList, vmList, population, bestIndividual, ACOparameters);
    }


    // Function is called to choose heuristic for the first time
    public static  MetaHeuristicAlgorithms getHeuristicInit(){
        int hueristicNumber = (int)(2*Math.random());
        
        if(rr)hueristicNumber = RoundRobin();

        // To take ACO
        // hueristicNumber = 0;
        // To take GA
        // hueristicNumber = 1;
        Log.printLine(hueristicNumber);
        AntColonyParameters ACOparameters = new AntColonyParameters() {
            {
                evaporationRate = 0.5;
                pheromoneWeight = 2;
                heuristicWeight = 1;
                pheromoneUpdationRate = 100;
                antsPerGeneration = populationSize;
                initPheromone = 1;
                tabuStrategy = false;
            }
        };

        GeneticParameters GNparameters = new GeneticParameters(){
            {
                mutationRate = 0.95;
                crossOverRate = 0.01;
                rouletteThreshold = 150;
                populationSize = HyperHeuristicScheduling.populationSize;
            }
        };

        if(hueristicNumber == 0){
            return new AntColonyTaskScheduler(cloudletList, vmList, ACOparameters);
        }else if(hueristicNumber == 1){
            return new GeneticScheduling(cloudletList, vmList, GNparameters);
        }
        return new AntColonyTaskScheduler(cloudletList, vmList, ACOparameters);
    }

    // Perturbation as per the paper with the use of simmulate annealing
    public static int[][] perturbation(int[][] population, MetaHeuristicAlgorithms LLH, int iterationByLLH){
        ArrayList<Double> fitness = new ArrayList<Double>();
        double maxFitness = -1;
        double minFitness = 100000000;

        for(int i = 0; i < populationSize; i++){
            double tempFitness =LLH.getQuality(population[i]);
            fitness.add(tempFitness);
            maxFitness = Math.max(maxFitness, tempFitness);
            minFitness = Math.max(minFitness, tempFitness);
        }
        double maxLowLevelIterations = 50;
        ArrayList<Double> temperature = new ArrayList<Double>();
        for(int i = 0; i < populationSize; i++){
            double temp = Tmax * (double)((maxLowLevelIterations - iterationByLLH) * (maxFitness - fitness.get(i))) / maxLowLevelIterations * (maxFitness - minFitness);
            temperature.add(temp);
        }

        for(int i = 0; i < populationSize; i++){
            int[] individual = population[i];
            int[] newIndividual = individual.clone();
            int subsolution = (int)(Math.random()*cloudletSize);
            int subsolutionValue = (int)(Math.random()*vmSize);
            newIndividual[subsolution] = subsolutionValue;

            double fitnessOld = LLH.getQuality(individual);
            double fitnessNew = LLH.getQuality(newIndividual);

            if(fitnessOld >= fitnessNew){
               population[i] = newIndividual;
            }else{
                double prob = Math.exp((double)(fitnessOld-fitnessNew)/temperature.get(i));
                if(Math.random() > (1.0 - prob)){
                    population[i] = newIndividual;
                }
            }
        }
        return population;
        
    }

    // Change heuristic operator as mentioned in paper
    public static boolean changeHeuristic(int[][] population, int notImprovedIterations){
        if(improvementDetection(notImprovedIterations) && diversityDetection(population))return false;
        else return true;
    }

    // Diversity Detection operator as mentioned in paper
    public static boolean diversityDetection(int[][] population){
        double diversity = getDiversity(population);

        if(diversity > intitalDiversity) return true;
        else return false;
    }

    // Computing diversity
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

    // Computing standard deviation of diversity
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

    // Improvement Detection operator as mentioned in paper
    public static boolean improvementDetection(int notImprovedIterations){
        if(notImprovedIterations > maxIterNotImproved){
            return false;
        }
        return true;
    }
    
}
