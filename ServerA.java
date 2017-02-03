package Architecture;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;
import java.util.LinkedList;
import java.util.Queue;

public class ServerA {
	
	/**
	 * When a VM arrives to the FA. 
	 * 
	 * **/
	
	private int grupo;
	
	/***
	 * The FA does not contain the SMAs nor the VMAs, it just considers how to trace back them. 
	 * */
	private int curSMA;
	private Vector<NodeA> listSMA; //Store the identifier of the SMAS
	private  Vector<Integer> listVMA; //Store the identifier of the VMAS and 	
	private NodeA[] SMA=new NodeA[20];  // from 0 to 5 is a low SMA, from 6 to 13 is a medium SMA, from 14 to 19 is high

    //We will be able to trace a VMA historic by its object hostSMA
	
	/***
	 * The FA does not contain the SMAs nor the VMAs, it just considers how to trace back them. 
	 * */
	
	public ServerA()
	{
		curSMA=0;
		listSMA=new Vector<NodeA>();
		listVMA=new Vector<Integer>();
		this.setGPU();
	}
	
	public void setGPU(){
	
		//CPUs with 8 cores
		for (int i=0;i<6;i++){
			SMA[i]=new NodeA(i, 20, 48);
			//front_agent.subscribeSMA(i);
			subscribeSMA(SMA[i]);
			//SMAS.put(i, SMA_low[i]);
		}
		
		//There exists 8 hosts with 40 CORES and 96 GB.
		
		//GPUs with 256 cores (Quadro)
		for (int i=6;i<14;i++){  
			SMA[i]=new NodeA(i, 40, 96);
			//front_agent.subscribeSMA(i+6);
			subscribeSMA(SMA[i]);
			//SMAS.put(i+6, SMA_low[i]);
		}
		
		//There exists 6 hosts with 60 CORES and 144 GB.
				
		//GPUs K20 with 2496 cores 
		for (int i=14;i<20;i++){
			SMA[i]=new NodeA(i, 60, 144);
			//front_agent.subscribeSMA(i+14);
			subscribeSMA(SMA[i]);
			//SMAS.put(i+14, SMA_low[i]);
		}							
	}

	
	
	public void restartNodes(){
		for (int i=0;i<20;i++){
			SMA[i].setFinishTime(0);
		}
	}
	
	protected void reOrder(int indexA, int indexB){
		NodeA temp;		
		temp=SMA[indexA];
		SMA[indexA]=SMA[indexB];
		SMA[indexB]=temp;
	}
	
	public void initiallizeBestFit(){
		this.reOrder(0,14);
		this.reOrder(1,15);
		this.reOrder(2,16);
		this.reOrder(3,17);
		this.reOrder(4,18);
		this.reOrder(5,19);
		/*for (int i=0;i<20;i++){
			listSMA.remove(SMA[i]);
        }        
		for (int i=0;i<20;i++){
        	subscribeSMA(SMA[i]);
        }*/
        
	}
	
	public void orderByFinishTime(){		
		for (int i=1;i<20;i++){
			for (int j=0;j<20-i;j++){
				if (SMA[j].getFinishTime()>SMA[i].getFinishTime()){
					reOrder(i,j);
				}
			}
		}
	}
	
	public String receiveJob(JobA job, long timeArrival, int policy){
		//String text="Received Job:"+job.getId()+" at time:"+timeArrival+" estimation time="+job.get_execution_time();
		String text="";
	    switch (policy){  //1 Round robin; 2 Best fit; 3 First come first serve; 4 Round Robin Priority
	       case 1:text=text+roundRobin(job, this.curSMA,timeArrival);
	       		  break;
	       case 2:text=text+bestFit(job, this.curSMA,timeArrival);
	       		  break;
	       case 3: text=text+firstComefirstServe(job, this.curSMA,timeArrival);
	       		  break;
               case 4: text=text+priority(job, this.curSMA,timeArrival);
	       		  break;
                   
	    }		
	    curSMA++;
	    return text;
	}
	
	protected void assignVMA(JobA xVMA, NodeA yHost){
		yHost.receiveVMA(xVMA);
	}
	
	public void subscribeSMA(NodeA newSMA){
		listSMA.add(newSMA);
	}
	
	//Used when a SMA unsubscribe to such a group
	public void unsubscribeSMA(int idSMA){
		listSMA.remove((Integer)idSMA);
	}
	
	
	//This function need to trace the location of the VMA. Please modify the 
	//structure to include the SMA location in the listVMA function
	//Deadline 04-10-2016
	public void enterNewVMA(int idVMA, int idCurrentSMA){
		listVMA.add(new Integer(idVMA));
		//Here we stabilish the distribution policy
		//roundRobin();
	}

	/*
	 * Here we define the scheduling policies
	 * */	
	
	//public void printFile(PrintWriter printWriter, String text){
	public void printFile(String text){
		File file=new File("file.txt");
		try(  PrintWriter out = new PrintWriter( file)  ){
		    out.println(text);
		    out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}  
	}	
	public String roundRobin(JobA job, int i,float xTimeArrival){
                String text="";
		NodeA xNode;
		float xAvailable=0;
		int index=i%20;
		xNode=listSMA.get(index);
                xAvailable=xNode.isAvailable(xTimeArrival);	    
                text=text+job.getId()+","+job.get_demand(0)+","+job.get_starting_time()+","+xAvailable;
                xAvailable=xAvailable+xNode.computeExecutionTime(job); //This is the estimated time to be return
                text=text+","+xAvailable;
                if (xAvailable<job.getDeadline())
                {
                	xNode.receiveVMA(job);
                    text=text+",1,"+xNode.getID();
                    //text="GREAT Waiting time:"+xAvailable;
                    //System.out.println("Se asigna a la SMA:"+xNode.getID()+" hold time: "+xAvailable);
                }
                else
                {
                    //text="SORRY Waiting time:"+xAvailable;
                    text=text+",0,"+xNode.getID();
                    //System.out.println("No se pudo asignar el Job:"+job.getId()+" hold time:"+xAvailable);
                }	    
                return text;
	}
	
        
        public String priority(JobA job, int i, float xTimeArrival){
                String text="";
                float xAvailable=0;
                int bestIndex=0;
                NodeA xNode = null;
                
                // colas de estaciones de trabajo disponibles
                Queue queue_WS_CPU = new LinkedList(); 
                Queue queue_WS_Quadro = new LinkedList(); 
                Queue queue_WS_Tesla = new LinkedList(); 
                
                for (int index=0;index<20;index++){
                    xNode=listSMA.get(index);                         
		    xAvailable=xNode.isAvailable(xTimeArrival);
                    
                    if (xAvailable < job.getDeadline()){ // si hay disponibilidad en estacion de trabajo
                        switch(xNode.getAvaibleCPU()){
                            case 20: 
                                queue_WS_CPU.add(xNode); 
                                break;
                            case 40:
                                queue_WS_Quadro.add(xNode); 
                                break;
                            case 60:
                                queue_WS_Tesla.add(xNode); 
                                break;
                        }
                    }
                }
                
                // Si no existe ninguna estación de trabajo disponible
                if(queue_WS_CPU.isEmpty() && queue_WS_Quadro.isEmpty() && queue_WS_Tesla.isEmpty()){
                    text=text+job.getId()+","+job.get_demand(0)+","+job.get_starting_time()+",-";
                    text=text+",0,-";
                }
                else{    

                    TaskDemand CPUtype = job.get_demand(0); //  tipo de tarea?? (low, medium, high)
                    switch(CPUtype){
                        case LOW: // Prioridad: CPU > Quadro > Tesla
                            if(!queue_WS_CPU.isEmpty())
                                xNode=(NodeA)queue_WS_CPU.remove();
                            else if(!queue_WS_Quadro.isEmpty())  
                                xNode=(NodeA)queue_WS_Quadro.remove();
                            else xNode=(NodeA)queue_WS_Tesla.remove();
                            break;
                        case HIGH: // Prioridad: Tesla > Quadro > CPU
                            if(!queue_WS_Tesla.isEmpty())
                                xNode=(NodeA)queue_WS_Tesla.remove();
                            else if(!queue_WS_Quadro.isEmpty())  
                                xNode=(NodeA)queue_WS_Quadro.remove();
                            else xNode=(NodeA)queue_WS_CPU.remove();
                            break;
                        case MEDIUM: // Prioridad: Quadro > CPU > Tesla
                            if(!queue_WS_Quadro.isEmpty())
                                xNode=(NodeA)queue_WS_Quadro.remove();
                            else if(!queue_WS_CPU.isEmpty())
                                xNode=(NodeA)queue_WS_CPU.remove();
                            else xNode=(NodeA)queue_WS_Tesla.remove();
                            break;
                    }
                    
                    text=text+job.getId()+","+job.get_demand(0)+","+job.get_starting_time()+","+xAvailable;
                    xAvailable=xAvailable+xNode.computeExecutionTime(job); //This is the estimated time to be return
                    text=text+","+xAvailable;
                    xNode.receiveVMA(job);
                    text=text+",1,"+xNode.getID();
                }
                return text;  
        }
        
        
	public String bestFit(JobA job, int i, float xTimeArrival){
	    String text="";
            float xAvailable=0;
	    float lessTime=100000;
	    int bestIndex=0;
	    NodeA xNode;
	    for (int index=0;index<20;index++){
                    xNode=listSMA.get(index);
		    xAvailable=xNode.isAvailable(xTimeArrival);
		    //text=text+" Task;"+job.getId()+";Waiting Time;"+xAvailable;
		    xAvailable=xAvailable+xNode.computeExecutionTime(job); //This is the estimated time to be return
		    //text=text+";Execution_Time;"+xAvailable;
		    if (xAvailable<lessTime){
		    	bestIndex=index;
		    	lessTime=xAvailable;
		    }
	    }	  
	    
	    xNode=listSMA.get(bestIndex); 	 
	    text=text+job.getId()+","+job.get_demand(0)+","+job.get_starting_time()+","+xNode.isAvailable(xTimeArrival);
	    //text=text+"Waiting Time:"+ xNode.computeHoldTime(xTimeArrival)+";Execution_Time;"+xAvailable;
	    text=text+","+lessTime;
	    if (lessTime<job.getDeadline())
	    {
	    	xNode.receiveVMA(job);
	    	//text="Se asigna a la SMA:"+xNode.getID()+" hold time: "+xAvailable;
	    	//text=text+";1;Assigned to WS;"+xNode.getID();
	    	text=text+",1,"+xNode.getID();
	    	//text="GREAT Waiting time:"+xAvailable;
	    	//System.out.println("Se asigna a la SMA:"+xNode.getID()+" hold time: "+xAvailable);
	    }
	    else
	    {
	    	//text="SORRY Waiting time:"+xAvailable;
	    	//text="No se pudo asignar el Job:"+job.getId()+" hold time:"+xAvailable;
	    	//text=text+";0;Rejected by WS;"+xNode.getID();
	    	//System.out.println("No se pudo asignar el Job:"+job.getId()+" hold time:"+xAvailable);
	    	text=text+",0,"+xNode.getID();
	    }	    
	    return text;		
	}
		
	public String firstComefirstServe(JobA job, int i,float xTimeArrival){
	    String text="";
		NodeA xNode;
		float xAvailable=0;
		int index=i%20;
		if (index==0){
			this.orderByFinishTime();
		}
		//xNode=listSMA.get(index);
		xNode=SMA[index];
	    xAvailable=xNode.isAvailable(xTimeArrival);
	    //text=text+" Task;"+job.getId()+";Waiting Time;"+xAvailable;
	    text=text+job.getId()+","+job.get_demand(0)+","+job.get_starting_time()+","+xAvailable;
	    xAvailable=xAvailable+xNode.computeExecutionTime(job); //This is the estimated time to be return
	    //text=text+";Execution_Time;"+xAvailable;
	    text=text+","+xAvailable;
	    if (xAvailable<job.getDeadline())
	    {
	    	xNode.receiveVMA(job);	    	
	    	//text=" Se asigna a la WS:"+xNode.getID()+" hold time: "+xAvailable;
	    	//text=text+";1;Assigned to WS;"+xNode.getID();
	    	//text="GREAT Waiting time:"+xAvailable;
	    	//System.out.println("Se asigna a la SMA:"+xNode.getID()+" hold time: "+xAvailable);
	    	text=text+",1,"+xNode.getID();
	    }
	    else
	    {
	    	//text="SORRY Waiting time:"+xAvailable;
	    	//text="No se pudo asignar el Job:"+job.getId()+"al WS:"+xNode.getID()+" hold time:"+xAvailable;
	    	//text=text+";0;Rejected by WS;"+xNode.getID();
	    	//System.out.println("No se pudo asignar el Job:"+job.getId()+" hold time:"+xAvailable);
	    	text=text+",0,"+xNode.getID();
	    }	    
	    return text;
	}
	
	
	/*
	 * Here we define the scheduling policies
	 * */		
	
	public void printSMAS(){
		   for (int i=0;i<listSMA.size();i++){
			   System.out.println("Elemento:"+listSMA.get(i).getID());
		   }
	}

}
