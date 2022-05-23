import javaSimulation.*;
import javaSimulation.Process;

public class BusLineSimulation extends Process{
    /**
     * class representing bus line
     * by calling constructor of this class you will receive data related to your parameters
     */
    int noOfBusStops;//number of bus stops in the busline
    Head busLine = new Head();//queue contains bus stops
    Random random = new Random(5);
    //variables for simulation analysis
    int generated_passengers;//total number of generated passengers on busstops
    int travelled_passengers;//number of transported passengers
    int generated_buses;//number of generated buses in simulation time
    double[] avg_used_bus_capacity;//average used bus transport capacity between bus stops
    double realTravelTime;
    double boardingTime;
    double exitTime;
    //variables for changing simulation environment
    int bus_capacity;//capacity of generated buses
    double simPeriod = 12*60;//simulation time
    double idealTravelTime=10;//ideal travel time between two bus stops

    BusLineSimulation(int n, int capacity) {noOfBusStops = n;avg_used_bus_capacity = new double[n];bus_capacity=capacity;}

    public void actions() {
        //running simulation
        for (int i = 1; i <= noOfBusStops; i++)
            activate(new BusStop());//generating bus stops based on given parameter
        activate(new BusGenerator());//activating bus generator
        //hold for simulation time plus some added time to let simulation finish it work
        hold(simPeriod+1000000);
        report();//generate report about simulation
    }

    void report() {
        System.out.println("\nSimulation output...");
        System.out.println("\nGENERAL SIMULATION INFO\n-----------------------");
        System.out.println("Generated buses: "+generated_buses);
        System.out.println("Number of bus Stops: " + busLine.cardinal());
        System.out.println("Number of generated passengers: "+generated_passengers);
        System.out.println("Number of travelled passengers: "+travelled_passengers);
        //printing all individual passengers and printing their entry and exit stop
        Link stoplink = busLine.first();
        BusStop stop;
        for (int i = 1; i <= noOfBusStops;i++){
            stop = (BusStop)stoplink;
            System.out.println(("Number of passenger on "+ i +". bus Stops: " + stop.waitingPassengers.cardinal()));
            stoplink = stoplink.suc();
        }
        //statistics based on fullness of busses
        System.out.println("\nSIMULATION STATISTICS\n---------------------");
        System.out.println("Bus capacity:" + bus_capacity);
        for(int i=0;i<noOfBusStops-1;i++){
            System.out.println("Average used bus capacity between "+ (i+1) + ". and " + (i+2)+ ". stop is: "+ 100*avg_used_bus_capacity[i]/(bus_capacity*generated_buses) + " %");
        }
        System.out.println("Ideal travel time thru busline with 1 min for stopping and boarding passengers: "+ ((idealTravelTime+1)*(noOfBusStops-1))+" min");
        System.out.println("Average bus travel time: " + (realTravelTime/generated_buses)+" min");
        System.out.println("Boarding takes: " + (100*boardingTime/realTravelTime) +" %");
        System.out.println("Exitting takes: " + (100*exitTime/realTravelTime)+" %");
        System.out.println("Traffic jam takes: " + (100*(realTravelTime-((idealTravelTime+1)*generated_buses*noOfBusStops))/realTravelTime)+"%");
        System.out.println("Average time of boarding 1 passenger takes "+ (boardingTime/travelled_passengers)+" min");
        System.out.println("Average time of exiting 1 passenger takes "+ (exitTime/travelled_passengers)+" min");



    }



    class Bus extends Process{
        /**
         * class representing bus in bus line simulation
         */
        public void actions(){
            //internal parameters specifying bus
            int capacity = bus_capacity;//
            int busstopnumber = 1;//actual number of busstop
            Head passengers = new Head();//passengers in bus capacity
            Head getting_off = new Head();//passengers exiting next stop (waiting near doors)
            Link busstop_link = busLine.first();//link onto first bus stop
            //variables for helping with processing
            BusStop busstop;
            Link passengerlink;
            Passenger passenger;
            //statistics parameters
            double inTime;
            double totalInTime=0;
            double outTime;
            double totalOutTime=0;
            double start = time();//bus starting time
            //cycle what bus do from time its generated till final stop
            while(busstopnumber<=noOfBusStops) {
                //exiting
                while(!getting_off.empty()){
                    getting_off.first().out();//unboarding / exiting
                    outTime = random.uniform(0.0,15.0)/60;//generating random exit time
                    totalOutTime+=outTime;
                    hold(outTime);//time that passenger needs to exit bus
                }
                //boarding
                busstop = (BusStop) busstop_link;
                while(passengers.cardinal()<capacity) {
                    if(!busstop.waitingPassengers.empty()){
                        passengerlink = busstop.waitingPassengers.first();
                        passenger = (Passenger)passengerlink;
                        passengerlink.out();
                        passenger.into(passengers);
                        travelled_passengers+=1;
                        inTime = random.uniform(0.0,15.0)/60;//generating random exit time
                        totalInTime+=inTime;
                        hold(inTime);//one passenger boarding time
                        continue;
                    }else {
                        break;
                    }
                }
                //counting passengers in bus for statistics
                if(busstopnumber<noOfBusStops){
                    avg_used_bus_capacity[busstopnumber-1] += passengers.cardinal();
                }

                //nalezeni pasazeru, co vystupuji nasledujici zastavku
                passengerlink = passengers.first();
                passenger = (Passenger)passengers.first();
                for(int i=0 ;i<passengers.cardinal();i++){
                    if(busstopnumber==noOfBusStops-1){
                        passenger.out();
                        passenger.into(getting_off);
                        passengerlink = passengers.first();
                        passenger = (Passenger)passengerlink;
                    }
                    if(passenger.exitStop==busstopnumber+1){
                        passenger.out();
                        passenger.into(getting_off);
                        passengerlink = passengers.first();
                        passenger = (Passenger)passengerlink;
                        continue;
                    }
                    passengerlink = passengerlink.suc();
                    passenger = (Passenger)passengerlink;
                }
                busstop_link = busstop_link.suc();
                busstopnumber += 1;
                hold(idealTravelTime+random.uniform(0.0,2.0));//travel time plus delay
            }
            //counting complete statistics
            boardingTime+=totalInTime;
            exitTime+=totalOutTime;
            realTravelTime+=time()-start;
        }
    }

    class BusGenerator extends Process{
        /**
         * class representing some depo from which drive buses in periodic time given by bus line schedule
         */
        public void actions(){
            //generating busses in fixed interval
            while(time()<=simPeriod){
                activate(new Bus());
                generated_buses+=1;
                hold(90);//waiting time between bus departures
            }
        }
    }

    class Passenger extends Link{
        /**
         * class representing passenger with his own exit stop
         */
        int exitStop;
        Passenger(int e){exitStop=e;}
    }

    class BusStop extends Process{
        /**
         * class representing bus stop
         * passengers appears here so it works like passenger generator
         */
        Head waitingPassengers = new Head();//waiting passengers on bus stop
        int no_of_stop;//process variable holding number of this bus stop
        public void actions(){
            into(busLine);//taken into bus line
            no_of_stop = busLine.cardinal();//given number based on position in bus line
            if(no_of_stop<noOfBusStops){
                //generating passengers on busstop
                while (time() < simPeriod) {
                    new Passenger(random.randInt(no_of_stop+1,noOfBusStops)).into(waitingPassengers);//new waiting passenger
                    generated_passengers +=1;//statistics of total generated passengers
                    //statistics about generated passengers and their desired exit stop
                    System.out.println("pasazer cekajici na zastavce: "+no_of_stop+" vystupuje na zastavce: " + ((Passenger)waitingPassengers.first()).exitStop);
                    hold(random.negexp(1/11.0));//hold time defining frequency of generating passengers
                }
            }
        }
    }
    public static void main(String args[]){
        activate(new BusLineSimulation(5,20));
    }
}
