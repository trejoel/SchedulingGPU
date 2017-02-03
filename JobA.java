package Architecture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;


//This class simulates a VMA
public class JobA  implements Serializable{

	
	//TO add a deadline to finish the current task
	private int Id;		
	
	
	/*
	 * We create by default 300 VM with random CPU and MEM avaible
	 * **/	
	private int CPU_avaible; //This can be any value in 1,2,4,6,8 pick random one of these
	private int MEM_avaible; // From 1 to 20	
	private int CPUusage; //this can be any interger from 0...CPU_avaible
	private int MEMusage; //this can be any interger from 0...MEM_Avaible
	private TaskDemand percentage_MEM;
	private TaskDemand percentage_CPU;
	private float starting_time;
	private float execution_time; //ending_time is just computing ending_time=starting_time+execution_time
	private float ending_time;
	private float deadline;
	private int synchro; //Defines how synchronizable is the task
	private int currentSMA; //Set the identifier of the current SMA
	
	//This structure give me a trace of the VMA. The SMAS that hosted it.  06-January-2016 
	ArrayList<Integer> hostSMA;
	//This structure give me an historic trace of the VMA. The SMAS that have been hosted it.		
	
	//According to CLUS 2015, average every 500 ms arrives a new VM and it remains in the system 35000 ms. 
	//The number of cores corresponds to the use of CPU
	//Consider the best execution time as xCPU_Avaible, xMemAvaible consider as the priority
	
	
	//Added by Joel 9th-Dec-2016
	//We add a deadline, the time in MS  waits for the resource
	public JobA(int xId, int xCPU_Avaible, int xMEM_Avaible, TaskDemand xCPU_Demand, float xstart_time, float xexecution_time, float xdeadline){
		this.Id=xId;
		//this.CPU_avaible=xCPU_Avaible;
		//if (xMEM_Avaible>20 || xMEM_Avaible<1){
		//	xMEM_Avaible=20;
		//}
		this.MEM_avaible=xMEM_Avaible;
		this.starting_time=xstart_time;
		this.execution_time=xexecution_time;
		//this.percentage_CPU=percentageResource.HIGH;
                //this.percentage_MEM=percentageResource.HIGH;
                this.percentage_CPU=xCPU_Demand;
		this.percentage_MEM=xCPU_Demand;
                
		this.ending_time=this.get_ending_time();
		this.deadline=xdeadline;
		//Added by Joel 06-January-2016 
                hostSMA=new ArrayList<Integer>();
		//Added by Joel 06-January-2016 		
                this.currentSMA=0; //By default it is 0, it means it does not have SMA October 2016
                //Generates how synchronizable is a task
                //Determine if the task is or not parallelizable
                //Non paralelizable --0, semi-paralelizable 50% paralelizable, paralelizable 100%
                this.synchro =  (int)(Math.random() * 3); 
	}
	
	//getters
	
	
	public int getSynchro(){
		return this.synchro;
	}
	
	public int getId()
	{
		return this.Id;
	}
	
	public int get_CPU_avaible()
	{
		return this.CPU_avaible;
	}
	
	public int get_MEM_avaible(){
		return this.MEM_avaible;
	}
	
	public float get_starting_time(){
		return this.starting_time;
	}
	
	public float get_execution_time(){
		return this.execution_time;
	}
	
	public float getDeadline(){
		return this.deadline;
	}
	
	public TaskDemand get_demand(int memCPU){
		//1 Mem, 0 CPU
		if (memCPU==1){
			return this.percentage_MEM;
		}
		else
		{
			return this.percentage_CPU;
		}		
	}
	

	public int getCPU_usage() {
		return this.CPUusage;
	}
	
	public int getMEM_usage() {
		return this.MEMusage;
	}

	public float getEndingTime(){
		return this.ending_time;
	}
	
	//setters
	
	
	public void set_CPU_avaible(int xVal)
	{
		this.CPU_avaible=xVal;
	}
	
	public void set_MEM_avaible(int xVal){
		this.MEM_avaible=xVal;
	}
	
	public void set_starting_time(float xVal){
		this.starting_time=xVal;
	}
	
	public void set_execution_time(float xVal){
		this.execution_time=xVal;
	}
			
	public float get_ending_time(){
		   float result=this.get_starting_time()+this.get_execution_time();
		   return result;
	}
	
	public void set_demand(int memCPU,TaskDemand xPercentage){
		//1 Mem, 0 CPU
		if (memCPU==1){
			this.percentage_MEM=xPercentage;
		}
		else
		{
			this.percentage_CPU=xPercentage;
		}		
	}
	
	public void setCPUusage() {  //this behaves according to the percentage of the resource
		this.CPUusage = this.computeCPUUse(this.get_demand(0));
	}



	public void setMEMusage() { //this behaves according to the percentage of the resource
		this.MEMusage = this.computeMemUse(this.get_demand(1));
	}
		
	// I should program this function.  It returns the relative value of the resource according to the percentage
	//2015-11-24
	
	
	protected int computeMemUse(TaskDemand X){
		double xMem=(double)this.get_MEM_avaible();
		int relMem=0;
		switch (X){
			case LOW:{//50
				          relMem= (int)Math.floor(xMem*(0.50));	 				          				          
					 }
			case MEDIUM:{//75
		          relMem= (int)Math.floor(xMem*(0.75));	 				          				          
			 }			
			default: relMem= this.get_MEM_avaible();
			
		}
		return relMem; 		
	}
	
	protected int computeCPUUse(TaskDemand X){
		double xCPU=(double)this.get_CPU_avaible();
		int relMem=0;
		switch (X){
			case LOW:{//50
                                    relMem= (int)Math.floor(xCPU*(0.50));	 				          				          
				}
			case MEDIUM:{//75
		          relMem= (int)Math.floor(xCPU*(0.75));	 				          				          
			 }			
			default: relMem= this.get_MEM_avaible();
			
		}
		return relMem; 		
	}	
	
	public void printVMA(){
		System.out.println("Virtual Machine: "+this.getId()+" Num CORES: "+this.get_CPU_avaible()+" MEM: "+this.get_MEM_avaible()+" Arrival: "+this.get_starting_time()+" End time: "+this.getEndingTime());		
	}
	
	public void addSMA(int idSMA)
	{
		hostSMA.add(idSMA);
	}
	
	public void removeSMA(int idSMA){
		hostSMA.remove(idSMA);
	}
	
	public void printHistoricTrace(){
		Iterator<Integer> itSMAS=hostSMA.iterator();
		while (itSMAS.hasNext()){
			int x=itSMAS.next();
			System.out.println("SMA:"+x+" -- ");
			
		}
	}
	
	public int getCurrentSMA(){
		return this.currentSMA;
	}
	
	public void setCurrentSMA(int xSMA){
		this.currentSMA=xSMA;
	}
}
