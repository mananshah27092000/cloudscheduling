package org.cloudbus.cloudsim.scheduling.antcolony;

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
import java.lang.Math;
import java.util.Arrays;

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


// Implements ACO
public class AntColonyTaskScheduler extends MetaHeuristicAlgorithms {

    AntColonyParameters params;

    double[][] pheromoneTable;

    double[][] pheromoneHeuristicTable;

    double[] pheromoneHeuristicTotal;

    int currentGeneration;

    // constructor
    public AntColonyTaskScheduler(Cloudlet[] cloudletList, 
                                  Vm[] vmList, 
                                  int[][] population,
                                  int[] bestIndividual,
                                  AntColonyParameters params) {
        super(cloudletList, vmList, population, bestIndividual);

        this.params = params;
        this.pheromoneTable = new double[cloudletCount][vmCount];
        this.pheromoneHeuristicTable = new double[cloudletCount][vmCount];
        this.pheromoneHeuristicTotal = new double[cloudletCount];
        
        this.currentGeneration = 0;
        
        initPheromoneTable();
    }

    // constructor
    public AntColonyTaskScheduler(Cloudlet[] cloudletList, 
                                  Vm[] vmList, 
                                  AntColonyParameters params) {
        
        super(cloudletList, vmList, params.antsPerGeneration);
        this.params = params;
        this.pheromoneTable = new double[cloudletCount][vmCount];
        this.pheromoneHeuristicTable = new double[cloudletCount][vmCount];
        this.pheromoneHeuristicTotal = new double[cloudletCount];
        this.currentGeneration = 0;

        initPheromoneTable();

    }

    // return pheromone heuristic value
    private double getPheromoneHeuristic(int cloudlet, int vm) {
        return Math.pow(pheromoneTable[cloudlet][vm], params.pheromoneWeight) *
               Math.pow(1 / cloudletExecTime[cloudlet][vm], params.heuristicWeight);
    }

    // update pheromone 
    private void addValueToPheromone(int cloudlet, int vm, double value) {
        pheromoneTable[cloudlet][vm] += value;
        pheromoneHeuristicTotal[cloudlet] -= pheromoneHeuristicTable[cloudlet][vm];
        pheromoneHeuristicTable[cloudlet][vm] = getPheromoneHeuristic(cloudlet, vm);
        pheromoneHeuristicTotal[cloudlet] += pheromoneHeuristicTable[cloudlet][vm];
    }

    // init pheromone
    private void initPheromoneTable() {

        for(int i = 0; i < cloudletCount; i++) {
            pheromoneHeuristicTotal[i] = 0;
            for(int j = 0; j < vmCount; j++) {
                pheromoneTable[i][j] = params.initPheromone;
                pheromoneHeuristicTable[i][j] = getPheromoneHeuristic(i, j);
                pheromoneHeuristicTotal[i] += pheromoneHeuristicTable[i][j];
            }
        }
        updatePheromoneTable();
    }

    // reduce weights of pheromones as time passes
    private void evaporatePheromones() {
        for(int i = 0; i < cloudletCount; i++) {
            for(int j = 0; j < vmCount; j++) {
                addValueToPheromone(i, j, (1 - params.evaporationRate) * pheromoneTable[i][j]);
            }
        }
    }

    // Update pheromone table
    private void updatePheromoneTable() {
        if(population != null) {
            for(int i = 0; i < populationSize; i++) {
                updatePheromoneTableUsingIndividual(population[i]);
            }
        }

        if(bestIndividual != null) updatePheromoneTableUsingIndividual(bestIndividual);
    }

    // updates the table using individual
    private void updatePheromoneTableUsingIndividual(int[] individual) {
        double quality = getQuality(individual);
        for(int cloudlet = 0; cloudlet < cloudletCount; cloudlet++) {
            int vm = individual[cloudlet];
            addValueToPheromone(cloudlet, vm, params.pheromoneUpdationRate / quality);
        }
    }

    // discrete inverse transform sampling
    private int sampleFromVmList(int cloudletIndex, boolean[] vmUsed) {
        double uniformRand = Math.random();
        double cumulativeProb = 0.0;

        double phTotal = 0;
        for(int i = 0; i < vmCount; i++) {
            if(!vmUsed[i]) phTotal += pheromoneHeuristicTable[cloudletIndex][i];
        }

        int lastUnusedVm = -1;
        for(int i = 0; i < vmCount; i++) {
            if(vmUsed[i]) continue;
            lastUnusedVm = i;
            cumulativeProb += pheromoneHeuristicTable[cloudletIndex][i] / phTotal;

            if(uniformRand <= cumulativeProb) {
                return i;
            }
        }

        return lastUnusedVm;
    }

    // discrete inverse transform sampling
    private int sampleFromVmList(int cloudletIndex) {
        double uniformRand = Math.random();
        double cumulativeProb = 0.0;

        for(int i = 0; i < vmCount; i++) {
            cumulativeProb += pheromoneHeuristicTable[cloudletIndex][i] / pheromoneHeuristicTotal[cloudletIndex];

            if(uniformRand <= cumulativeProb) {
                return i;
            }
        }

        return vmCount - 1;
    }

    // Updates the pheromone heuristic using weights
    private void updatePheromoneHeuristicTotals() {

        for(int i = 0; i < cloudletCount; i++) {
            pheromoneHeuristicTotal[i] = 0.0;
            for(int j = 0; j < vmCount; j++) {
                pheromoneHeuristicTotal[i] += Math.pow(pheromoneTable[i][j], params.pheromoneWeight) *
                                              Math.pow(1.0 / cloudletExecTime[i][j], params.heuristicWeight);
            }
        }
    }

    // get the next generation using ACO
    @Override
    public void runNextGeneration() {

        for(int i = 0; i < params.antsPerGeneration; i++) {
            boolean[] vmUsed = new boolean[vmCount];
            int vmUsedCount = 0;
            for(int j = 0; j < cloudletCount; j++) {
                if(params.tabuStrategy)
                    population[i][j] = sampleFromVmList(j, vmUsed);
                else 
                    population[i][j] = sampleFromVmList(j);
                
                if(params.tabuStrategy) {
                    vmUsed[population[i][j]] = true;
                    vmUsedCount++;
                    if(vmUsedCount == vmCount) {
                        vmUsedCount = 0;
                        Arrays.fill(vmUsed, 0, vmCount, false);
                    }
                }
            }

            double quality = getQuality(population[i]);
            if(quality < bestQuality) {
                bestQuality = quality;
                bestIndividual = population[i].clone();
            }
        }
        evaporatePheromones();
        updatePheromoneTable();
        currentGeneration++;
    }
    
}
