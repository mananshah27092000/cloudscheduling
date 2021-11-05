package org.cloudbus.cloudsim.hyperhueristicscheduling.metaheuristics.antcolony;

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

public class AntColonyTaskScheduler {
    List<Vm> vmList;
    List<Cloudlet> cloudletList;

    ArrayList<Boolean> tabu;
    AntColonyParameters params;
    ArrayList<ArrayList<Double>> pheromoneTable;
    ArrayList<Integer> bestSolution;
    double bestMakespan;
    ArrayList<Integer> currentBestSolution;
    double currentBestMakespan;
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

    public AntColonyTaskScheduler(List<Vm> vmList, 
                                  List<Cloudlet> cloudletList, 
                                  List<List<Integer>> population,
                                  List<Integer> bestIndividual,
                                  AntColonyParameters params) {
        this.vmList = vmList;
        this.cloudletList = cloudletList;
        this.params = params;
        this.tabu = new ArrayList<>();

        for(int i = 0; i < vmList.size(); i++) {
            this.tabu.add(false);
        }

        this.pheromoneTable = new ArrayList<>();

        for(int i = 0; i < cloudletList.size(); i++) {
            this.pheromoneTable.add(new ArrayList<Double>());
            for(int j = 0; j < vmList.size(); j++) {
                this.pheromoneTable.get(i).add(this.params.initPheromone);
                // System.out.println(i + " " + j + " " + this.pheromoneTable.get(i).get(j));
            }
        }

        this.bestSolution = null;
        this.bestMakespan = Double.POSITIVE_INFINITY;
        this.currentGeneration = 0;
        // System.out.println(tabu.get(3));
    }

    public AntColonyTaskScheduler(List<Vm> vmList, 
                                  List<Cloudlet> cloudletList, 
                                  AntColonyParameters params) {
        this(vmList, cloudletList, null, null, params);
        

    }
}
