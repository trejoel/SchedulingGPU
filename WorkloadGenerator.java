package Architecture;

import java.util.Random;

/**
 * Modified by Joel Trejo
 * */
public class WorkloadGenerator{

	private JobA[] virtual_machine=new JobA[300];

    
	private static final int AVG_INTERARRIVAL_TIME=500;
	private static final int AVG_INTERDEPARTURE_TIME=20000;
	
	
    public WorkloadGenerator (){

    }    
	
	public int generateRandomCPU(){
		Random randomGenerator=new Random();
		int randCPU=1;
		//1,2,4,6,8
		int valGen= randomGenerator.nextInt(5);
		switch  (valGen){
		  case 0:  randCPU=1;
		           break;
		  case 1: randCPU=2;
          		  break;
		  case 2: randCPU=4;
  		  		  break;
		  case 3: randCPU=6;
	  		  	   break;
	  	  default: randCPU=8;
	  		  	   break;
		}
		
		return randCPU;
	}
	
	public int generateRandomMEM(){
		Random randomGenerator=new Random();
		//int randMEM= randomGenerator.nextInt(21);
		int randMEM= randomGenerator.nextInt(2);
		randMEM++;
		return randMEM;
	}
        
        public TaskDemand generateRandomDemand(){
		Random randomGenerator=new Random();
		//int randMEM= randomGenerator.nextInt(21);
		int randMEM= randomGenerator.nextInt(3);
                if(randMEM == 0)
                    return TaskDemand.LOW;
                else if(randMEM == 1)
                    return TaskDemand.MEDIUM;
		return TaskDemand.HIGH;
	}
	
	public long generateRandomStartTime(long averageDelayForAllocation){
		// Based on WorkLoadGenerator
		long delayForAllocation = (long) (AVG_INTERARRIVAL_TIME*(-Math.log(Math.random())));		
		averageDelayForAllocation=averageDelayForAllocation+delayForAllocation;		
		return averageDelayForAllocation;
	}
		
	
	public long generateRandomExecutionTime(long averageDelayForDeallocation){
		// Based on WorkLoadGenerator
		// Rate of inter-departure time is 35000 ms (it is 35 seconds)
		//We construct a function that determines a value from from 30000 to 40000 starting from the arrival time of the previous machine.
            
                 // Low: 10 - 2,000 ms
                 //      10 - 500 secuencial
                 //      501 - 2,000 para 2 cores o más
                 // Medium: 2,001 - 20,000 ms
                 // High: 20,001 - 40,000
        long delayForDeallocation = (long) (AVG_INTERDEPARTURE_TIME*(-Math.log(Math.random()))); //  Arrival process is Poisson Distributed
        averageDelayForDeallocation=averageDelayForDeallocation+delayForDeallocation;	
        //averageDelayForDeallocation=delayForDeallocation;			
		return averageDelayForDeallocation;		
	}

	
    public long generateDeadline(){
		Random randomGenerator=new Random();
    	long x=0;
    	int y=randomGenerator.nextInt(3);
    	switch (y){
    	  case 0: x=500;
    	  		  break;
    	  case 1: x=1000;
  		  		  break;    	  
    	  case 2: x=3000;
  		  		  break;    	  
    	  default: x=5000;
  		            break;
    	}
    	return x;
    }
}
