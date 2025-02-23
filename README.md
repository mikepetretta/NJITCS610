# NJITCS610
## Module 5 Assignment
### Problem Description
The purpose of this assignment is to run a simulation of different queuing policies, and observe their impact on average waiting time.  Imagine that we have five interchangeable service stations (airline check-in counters; store check-out counters; DMV service windows; immigration posts in an international airport; etc).  Arriving passengers (or clients or citizens or visitors) are lined up in FIFO queues, and you have to decide how to organize these queues and associated queuing policies:

Option 1:  all arriving passengers are placed in a single queue, and service stations take passengers from the front of that queue.

Option 2:  each service station has its own queue, and arriving passengers are dispatched to a queue according to one of many policies:

2.A:  round robin (1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, ...).

2.B:  arriving passenger is placed in a shortest queue.

2.C:  arriving passenger is placed in a random queue.

Inputs to the simulation:
The duration of the simulation measured in minutes (D:  make it arbitrarily long, do not worry about it being or not being realistic).
The average arrival rate measured in minutes (A:  arrivals are random, but on average there is one new passenger every A minutes),
The average service rate measured in minutes (S:  service rates are random, but on average they need about S minutes of service).
For the sake of this study, make sure to crowd the system, by choosing S >> 5*A, without causing an overflow of your queues.  Also choose D to be long enough to get rid of any transitory effects.

Outputs of the Simulation for each queuing policy:
The duration of the simulation (which may be longer than the input parameter, as when check-in closes, there may be passengers in the waiting queues and service stations).
The maximum length of the queue for each queue.
The average and maximum waiting time for each queue.
The rate of occupancy of each service station (percentage of time each station was busy).
If you want: show the real-time evolution of the queues during the run-time simulation.
Students will be given directives to develop and test their programs on specific development environments. These programs will be tested and graded in the same environment in which they are developed.

### Simulation Results 
Simulation results for input parameters - 
* Simulation Duration = 1800
* Average Arrival Rate = 2
* Average Service Rate = 10
```
SingleQueue Simulation Summary
Total Processing Duration (Seconds) = 2241
Station Summary -
stationName=SingleQueue Station 0, occupancyRate=99.11%
stationName=SingleQueue Station 1, occupancyRate=99.64%
stationName=SingleQueue Station 2, occupancyRate=99.73%
stationName=SingleQueue Station 3, occupancyRate=99.29%
stationName=SingleQueue Station 4, occupancyRate=99.33%
Queue Summary -
numberOfProcessedElements=1193, maximumLength=234, waitingTimeMax=432.092, waitingTimeAvg=228.239
-------------------------
RoundRobinQueue Simulation Summary
Total Processing Duration (Seconds) = 2390
Station Summary -
stationName=RoundRobinQueue Station 0, occupancyRate=96.32%
stationName=RoundRobinQueue Station 1, occupancyRate=94.39%
stationName=RoundRobinQueue Station 2, occupancyRate=99.66%
stationName=RoundRobinQueue Station 3, occupancyRate=97.28%
stationName=RoundRobinQueue Station 4, occupancyRate=95.4%
Queue Summary -
numberOfProcessedElements=241, maximumLength=55, waitingTimeMax=505.07, waitingTimeAvg=256.819
numberOfProcessedElements=241, maximumLength=47, waitingTimeMax=466.091, waitingTimeAvg=233.994
numberOfProcessedElements=241, maximumLength=60, waitingTimeMax=582.007, waitingTimeAvg=304.154
numberOfProcessedElements=240, maximumLength=60, waitingTimeMax=543.099, waitingTimeAvg=284.813
numberOfProcessedElements=240, maximumLength=51, waitingTimeMax=487.025, waitingTimeAvg=223.918
-------------------------
ShortestQueue Simulation Summary
Total Processing Duration (Seconds) = 2290
Station Summary -
stationName=ShortestQueue Station 0, occupancyRate=98.91%
stationName=ShortestQueue Station 1, occupancyRate=97.73%
stationName=ShortestQueue Station 2, occupancyRate=99.21%
stationName=ShortestQueue Station 3, occupancyRate=97.47%
stationName=ShortestQueue Station 4, occupancyRate=99.69%
Queue Summary -
numberOfProcessedElements=240, maximumLength=51, waitingTimeMax=472.025, waitingTimeAvg=234.871
numberOfProcessedElements=250, maximumLength=51, waitingTimeMax=435.087, waitingTimeAvg=221.963
numberOfProcessedElements=240, maximumLength=51, waitingTimeMax=473.045, waitingTimeAvg=234.093
numberOfProcessedElements=238, maximumLength=51, waitingTimeMax=446.031, waitingTimeAvg=232.342
numberOfProcessedElements=235, maximumLength=50, waitingTimeMax=492.075, waitingTimeAvg=237.727
-------------------------
RandomQueue Simulation Summary
Total Processing Duration (Seconds) = 2474
Station Summary -
stationName=RandomQueue Station 0, occupancyRate=89.45%
stationName=RandomQueue Station 1, occupancyRate=78.5%
stationName=RandomQueue Station 2, occupancyRate=99.43%
stationName=RandomQueue Station 3, occupancyRate=89.61%
stationName=RandomQueue Station 4, occupancyRate=91.11%
Queue Summary -
numberOfProcessedElements=236, maximumLength=46, waitingTimeMax=439.071, waitingTimeAvg=217.235
numberOfProcessedElements=216, maximumLength=22, waitingTimeMax=208.007, waitingTimeAvg=95.625
numberOfProcessedElements=264, maximumLength=74, waitingTimeMax=667.006, waitingTimeAvg=287.93
numberOfProcessedElements=224, maximumLength=44, waitingTimeMax=448.016, waitingTimeAvg=249.91
numberOfProcessedElements=236, maximumLength=49, waitingTimeMax=463.053, waitingTimeAvg=215.194
```

### How to run the program 
From terminal or command line (with java installed)
<br>
javac Assignment.java
<br>
java Assignment {SimulationDuration} {AverageArrivalRate} {AverageServiceRate}
<br> 
```
# Example 
# Simulation Duration = 10
# Average Arrival Rate = 2
# Average Service Rate = 4
java Assignment 10 2 4
```
