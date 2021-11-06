package org.cloudbus.cloudsim.scheduling.antcolony;

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

public class AntColonyTaskScheduler extends MetaHeuristicAlgorithms {
    AntColonyParameters params;
    double[][] pheromoneTable;
    double[][] pheromoneHeuristicTable;
    double[] pheromoneHeuristicTotal;
    // double bestMakespan;
    int currentGeneration;

    // static AntColonyParameters defaultParams = new AntColonyParameters() {
    //     {
    //         evaporationRate = 0.5;
    //         pheromoneWeight = 1;
    //         heuristicWeight = 1;
    //         pheromoneUpdationRate = 100;
    //         maxIterations = 150;
    //         antsPerGeneration = 8;
    //     }
    // };

    public AntColonyTaskScheduler(Cloudlet[] cloudletList, 
                                  Vm[] vmList, 
                                  int[][] population,
                                  int[] bestIndividual,
                                  AntColonyParameters params) {
        super(cloudletList, vmList, population, bestIndividual);
        Log.printLine(bestIndividual);
        this.params = params;
        this.pheromoneTable = new double[cloudletCount][vmCount];
        this.pheromoneHeuristicTable = new double[cloudletCount][vmCount];
        this.pheromoneHeuristicTotal = new double[cloudletCount];
        
        // this.bestMakespan = Double.POSITIVE_INFINITY;
        this.currentGeneration = 0;
        // System.out.println(tabu.get(3));
    }

    public AntColonyTaskScheduler(Cloudlet[] cloudletList, 
                                  Vm[] vmList, 
                                  AntColonyParameters params) {
        
        super(cloudletList, vmList, params.antsPerGeneration);
        this.params = params;
        this.pheromoneTable = new double[cloudletCount][vmCount];
        this.pheromoneHeuristicTable = new double[cloudletCount][vmCount];
        this.pheromoneHeuristicTotal = new double[cloudletCount];
        
        // this.bestMakespan = Double.POSITIVE_INFINITY;
        this.currentGeneration = 0;

    }

    private double getPheromoneHeuristic(int cloudlet, int vm) {
        return Math.pow(pheromoneTable[cloudlet][vm], params.pheromoneWeight) *
               Math.pow(1 / cloudletExecTime[cloudlet][vm], params.heuristicWeight);
    }

    private void addValueToPheromone(int cloudlet, int vm, double value) {
        pheromoneTable[cloudlet][vm] += value;
        pheromoneHeuristicTotal[cloudlet] -= pheromoneHeuristicTable[cloudlet][vm];
        pheromoneHeuristicTable[cloudlet][vm] = getPheromoneHeuristic(cloudlet, vm);
        pheromoneHeuristicTotal[cloudlet] += pheromoneHeuristicTable[cloudlet][vm];
    }

    private void initPheromoneTable() {

        for(int i = 0; i < cloudletCount; i++) {
            pheromoneHeuristicTotal[i] = 0;
            for(int j = 0; j < vmCount; j++) {
                pheromoneTable[i][j] = params.initPheromone;
                pheromoneHeuristicTable[i][j] = getPheromoneHeuristic(i, j);
                pheromoneHeuristicTotal[i] += pheromoneHeuristicTable[i][j];
                // System.out.println(i + " " + j + " " + this.pheromoneTable.get(i).get(j));
            }
        }
        updatePheromoneTable();
    }

    private void evaporatePheromones() {
        for(int i = 0; i < cloudletCount; i++) {
            for(int j = 0; j < vmCount; j++) {
                addValueToPheromone(i, j, (1 - params.evaporationRate) * pheromoneTable[i][j]);
            }
        }
    }

    private void updatePheromoneTable() {
        if(population != null) {
            for(int i = 0; i < populationSize; i++) {
                updatePheromoneTableUsingIndividual(population[i]);
            }
        }

        if(bestIndividual != null) updatePheromoneTableUsingIndividual(bestIndividual);
    }

    private void updatePheromoneTableUsingIndividual(int[] individual) {
        double quality = getQuality(individual);
        for(int cloudlet = 0; cloudlet < cloudletCount; cloudlet++) {
            int vm = individual[cloudlet];
            addValueToPheromone(cloudlet, vm, params.pheromoneUpdationRate / quality);
        }
    }

    // discrete inverse transform sampling
    private int sampleFromVmList(int cloudletIndex) {
        double uniformRand = Math.random();
        double cumulativeProb = 0.0;

        for(int i = 0; i < vmCount; i++) {
            cumulativeProb += pheromoneHeuristicTable[cloudletIndex][i] / 
                                pheromoneHeuristicTotal[cloudletIndex];
            if(uniformRand <= cumulativeProb) {
                return i;
            }
        }

        return vmCount - 1;
    }

    private void updatePheromoneHeuristicTotals() {

        for(int i = 0; i < cloudletCount; i++) {
            pheromoneHeuristicTotal[i] = 0.0;
            for(int j = 0; j < vmCount; j++) {
                pheromoneHeuristicTotal[i] += Math.pow(pheromoneTable[i][j], params.pheromoneWeight) *
                                              Math.pow(1.0 / cloudletExecTime[i][j], params.heuristicWeight);
            }
        }
    }

    @Override
    public void runNextGeneration() {
        for(int i = 0; i < params.antsPerGeneration; i++) {
            for(int j = 0; j < cloudletCount; j++) {
                population[i][j] = sampleFromVmList(j);
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
