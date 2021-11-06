package org.cloudbus.cloudsim.scheduling.genetic;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.lang.Math;

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

public class GeneticScheduling extends MetaHeuristicAlgorithms{
    public GeneticParameters params;
    
    public GeneticScheduling(Cloudlet[] cloudletList,Vm[] vmList,int[][] population,int[] bestIndividual, GeneticParameters params){
        super(cloudletList, vmList, population, bestIndividual);
        this.params = params;
    }

    public GeneticScheduling(Cloudlet[] cloudletList,Vm[] vmList, GeneticParameters params){
        super(cloudletList, vmList, params.populationSize);
        this.params = params;
    }

    public int selectIndividual(){
        ArrayList<Double> fitness = new ArrayList<Double>();
        double fitnessSum = 0;
        for(int i = 0; i < population.length; i++){
            fitnessSum = getQuality(population[i]);
            fitness.add(fitnessSum);
        }
        ArrayList<Double> prob = new ArrayList<Double>();
        double probSum = 0;
        for(int i = 0; i < fitness.size(); i++){
            probSum += fitness.get(i)/fitnessSum;
            prob.add(fitness.get(i)/fitnessSum);
        }
        double r = Math.random();
        for(int i = 0; i < prob.size(); i++){
            if(r <= prob.get(i)){
                return i;
            }
        }
        return cloudletCount - 1;
    }

    // Two point crossover
    public void crossOver(){
    
        int i1 = selectIndividual();
        int i2 = selectIndividual();
        // while(i1 != i2){
        //     Log.printLine("Same selection of individual for crossover, changing it");
        //     i2 = selectIndividual();
        // }

        int[] individual1 = population[i1].clone();
        int[] individual2 = population[i2].clone();

        int p1 = (int)(cloudletCount * Math.random());
        int p2 = (int)(cloudletCount * Math.random());
        if(p2 > p1){
            p1 = p2;
        }
        int [] newOne = new int[individual1.length];
        int [] newTwo = new int[individual2.length];
        for(int i = 0; i < cloudletCount; i++){
            if(i <= p1 || i > p2){
                newOne[i] = individual1[i];
                newTwo[i] = individual2[i];
            }else{
                newOne[i] = individual2[i];
                newTwo[i] = individual1[i];
            }
        }
        population[i1] = newOne;
        population[i2] = newTwo;
    }

    public void mutation(){
        int i1 = selectIndividual();
        boolean shouldMutate = Math.random() <= params.mutationRate;
        if(shouldMutate){
            population[i1][(int) (cloudletCount * Math.random())] = (int) (vmCount * Math.random());
        }
    }

    public double getQualitySum(){
        double qualitySum = 0;
        for(int i = 0; i < population.length; i++){
            qualitySum += getQuality(population[i]);
        }
        return qualitySum;
    }

    @Override
    public void runNextGeneration(){
        int runs = 0;
        while(getQualitySum() < params.rouletteThreshold && runs<100){
            crossOver();
            runs++;
        }
        mutation();
    }
}