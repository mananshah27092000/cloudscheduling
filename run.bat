cd hyperheuristicscheduling\org\cloudbus\cloudsim\scheduling

rm *.class

cd antcolony

rm *.class

cd ..\..\..\..\..\..

javac -classpath jars\cloudsim-new.jar;hyperheuristicscheduling hyperheuristicscheduling\org\cloudbus\cloudsim\scheduling\HyperHeuristicSimulation.java

java -classpath jars\cloudsim-new.jar;hyperheuristicscheduling org.cloudbus.cloudsim.scheduling.HyperHeuristicSimulation