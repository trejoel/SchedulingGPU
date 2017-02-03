package Architecture;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;


public class NodeA {

	private int idSMA;
	private int idGroup;
	private boolean is_leader;	
	private int PUERTOSERVICIO; //starting the service at port 4999 + idSMA	
	private float timeToFinish;
	private float timeToStart;
 
        /* According to paper Agent Based Load Balancing in Cloud Data Center, there exists the following configurations**/
	
	/* ***** 
	 * In total there exists 20 SMAS
	 * 
	 * 6 Hosts with 20 cores and 48 GB
	 * 8 Hosts with 40 cores and 96 GB
	 * 6 Hosts with 60 cores and 144 GB
	 * 
	 * */
	private int CPU_assigned;  //It is first assigned a (Cores enum) number of cores
	private int MEM_assigned;		
	private int CPU_used;	   //It could use a number among 0 ...CPU_assigned number of cores.
	private int MEM_used;
	ArrayList<JobA> vMachine; //Just one vMachine
	// In case this SMA is a group leader, it should access to it
	//ArrayList<Integer> listSMA; //Store the identifier of the SMAS
	Vector<Integer> listSMA;
  
  
	//This values should be computed randomized
    public NodeA(int xID, int xCPU_A, int xMEM_A){
     	this.idSMA=xID;
     	this.idGroup=0;
     	this.is_leader=false;
     	this.CPU_assigned=xCPU_A;
     	this.MEM_assigned=xMEM_A;
        this.CPU_used=0;        
        this.MEM_used=0;
        vMachine=new ArrayList<JobA>();
         listSMA = new Vector<Integer>();
        PUERTOSERVICIO=5000+xID;
        this.timeToStart=0;
        this.timeToFinish=0;
        //vMachine = new Vector();
    }
  
  //Estimates the time to be available Added by Joel
  public float isAvailable(float curTime){
	  float available=0;
	  available=computeHoldTime(curTime);
	  return available;
  }
  
  protected float computeHoldTime(float curTime){	  
	  float holdTime=this.timeToFinish; //This is the time in the simulation
	  if (vMachine.size()>0) {
		  //holdTime=vMachine.get(0).get_starting_time()+vMachine.get(0).get_execution_time();
		  if (holdTime>curTime) //The task is still in the node
		  {			  
			  holdTime=holdTime-curTime; // The time Jobi has to wait
		  }
		  else
		  {
			  //vMachine.get(0).removeSMA(this.getID());
			  holdTime=0;
		  }
	  }
    return holdTime;
  }
  
  //Estimates the time to be available Added by Joel
  
  public int getAvaibleCPU(){
  	return this.CPU_assigned;
  }
  
  public int getAvaibleMEM(){
  	return this.MEM_assigned;
  }
  
  //ADDED 25-FEB-2016 by JOEL
  public int getPUERTOSERVICIO(){
  	return this.PUERTOSERVICIO;
  }
  
  
  //Update the number of resources used (cores or memory)by increasing the percentage of use

  protected void updateResource(int typeResource,int xValue) {  //xValue could be positive or negative
  	//float newValue=this.getAvaibleResourse(typeResource)+xValue;    	
  	if (xValue<0) //This sceneario never should occurs. 
  		xValue=0;
  	if (typeResource==0) //CPU
  		this.CPU_used=xValue;
  	else                 //MEMORY   
  		this.MEM_used=xValue;    
  }
      
  
  public int getID(){
  	return this.idSMA;
  }
  
  public int getidGroup(){
  	return this.idGroup;
  }
  
  public boolean isLeader(){
  	return this.is_leader;
  }
  
  public void setGroup(int newGroup){
  	this.idGroup=newGroup;
  }
  
  public void setLeadership(boolean ldr){ //This only sets as true, such SMAs in the leader role
  	this.is_leader=ldr;
  }
  
  public void setFinishTime(float x){
	  this.timeToFinish=x;
  }
  
  public boolean acceptVMA(){
  	// This function determines if the SMA can accept a new Virtual Machine
  	
  	return true;
  }
  
  public void receiveVMA(JobA x){	  
  	 //This function communicates via Socket with a SMA if the current SMA accept a VMA    
	 //Here to review
	 float exTime=0;	 
	  this.timeToStart=x.get_starting_time()+this.computeHoldTime(x.get_starting_time());
	   exTime=computeExecutionTime(x);
	 this.timeToFinish=this.timeToStart+exTime;
	 //Here to review
  	vMachine.add(x);    
  	//Add the trace to the VMA added by Joel 06-January-2016
  	x.addSMA(this.getID());
  	//Add the trace to the VMA added by Joel 06-January-2016
  }
  
  protected float computeExecutionTime(JobA x){
	  //Here we make a map, if number of CPU is 20 then it is a multi-core
	  //if number of CPU is 40 then it is 256 GPU
	  //if number of CPU is 60 then it is 2496 GPU
	  float xExTime=x.get_execution_time();
	  switch (this.getAvaibleCPU()){
	  case 20: 		 if (x.getSynchro()==2){
		  				xExTime=x.get_execution_time()/2;
	                 }
	  				else if (x.getSynchro()==1){
	  					xExTime=(x.get_execution_time()/2 + (x.get_execution_time()/4));		
	  				}
	  				else //x.getSynchor()==0
	  				{
	  					xExTime=x.get_execution_time();
	  				}
	  		   		break;
	  case 40: 		 if (x.getSynchro()==2){
						xExTime=(x.get_execution_time()/10+(x.get_execution_time()/100));
       				  }
					else if (x.getSynchro()==1){
						xExTime=(x.get_execution_time()/10 + (x.get_execution_time()/20));		
					}
					else //x.getSynchor()==0
					{
						xExTime=x.get_execution_time();
					}
   					break;
	  case 60: 		 if (x.getSynchro()==2){
						xExTime=(x.get_execution_time()/100+(x.get_execution_time()/500));
			  		}
					else if (x.getSynchro()==1){
						xExTime=(x.get_execution_time()/100 + (x.get_execution_time()/200));		
					}
					else //x.getSynchor()==0
					{
						xExTime=x.get_execution_time();
					}
					break;
      default: xExTime=x.get_execution_time();	  		   	  		   
	  }
	  //System.out.println("ESTE ES LA CAPACIDAD DEL NODE: "+this.getAvaibleCPU()+ " ExTime:"+xExTime);
	  return xExTime;
  }
  
  public float getStartTime(){
	  return this.timeToStart;
  }
  
  public float getFinishTime(){
	  return this.timeToFinish;
  }
  
  public void removeVMA(int x){
	  vMachine.remove(0);
  }
  
  public void rejectVMA(){
  	//This function communicates via Socket with a SMA if the current SMA rejects a VMA 
  	
  	
  }
  
  /*Added by Joel on June 30th 2016*/
  
  public void printSMAFeatures(){
  	System.out.println("SMA "+this.getID()+" with "+this.getAvaibleCPU()+" cores and "+this.getAvaibleMEM()+" memory");
  }
  
	public void printVMAs(){
		Iterator<JobA> itVMAS=vMachine.iterator();
		while (itVMAS.hasNext()){
			//Added by Joel 07-January-2016  Review if it does not form an infinity loop
			System.out.println("SMA:"+itVMAS.next().getId()+" -- ");
			//Added by Joel 07-January-2016
		}
	}
  
  // compute the percentage of capacity of the SMA considering the resources used by the vMachines hosted by SMA
  // 1 MEM, 0 CPU

  
  // This code should be assigned to a thread in order to surveillance the performance of the SMA
  public void computeResouceUsed(int typeResource){
  	int len=vMachine.size();
  	int i;
  	int sum=0;
  	for (i=1;i<=len;i++){
  		if(typeResource==1)
  		  sum=sum+vMachine.get(i).getMEM_usage();
  		else
  		  sum=sum+vMachine.get(i).getCPU_usage();
  	}
  	this.updateResource(typeResource, sum);
  }
  
  
  /*
   * Added by Joel 2016-01-13
   * 
   * */
  
  //Used when a SMA subscribe to that group
	public void subscribeSMA(int idSMA){
		//This SMA should be removed from 
		listSMA.add(new Integer(idSMA));		
	}
	
	//Used when a SMA unsubscribe to such a group
	public void unsubscribeSMA(int idSMA){
		listSMA.remove((Integer)idSMA);		
	}
		
	//Sends message to the idSMAN
	public void sendMessage(int idSMA){
		
	}
	
	//Receive message, we need to identify who is sending the message
	public void receiveMessage(){
		
	}
      
}


