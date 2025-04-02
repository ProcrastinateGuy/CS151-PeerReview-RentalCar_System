package project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

// this is a scheduler to track the status of cars in rental or the history price of a car on sell
// it's useful when a user want to check what cars are available in a store
// or what specific model are available in which store branch
public class carRentalSchedule {

    //this array stores all the available cars in every branch using an arrayList
    private ArrayList<HashMap <String, RentalCar>> availabilityArray = new ArrayList <HashMap <String, RentalCar>> ();


    private String carVIN;
    private LocalDateTime estimatedAvailableDate;

    //this method load the inventory from a store to the corresponding HashMap
    //a hashmap to track all the cars in this store branch
    //key: carID, value: car object reference
    private HashMap<String, RentalCar> loadInventory (Store targetStore){
        HashMap<String, RentalCar> productionMap = new HashMap<>();
        ListIterator <Vehicle> iterator = targetStore.getInventory().getAllVehicles().listIterator();
        //put all the vehicles into the hashMap
        while (iterator.hasNext()){
            Vehicle vehicle = iterator.next();
            try{
                productionMap.put(vehicle.getVIN(), (RentalCar) vehicle);} //down casting
            catch( ClassCastException e){
                System.out.println(vehicle.getVIN() + " is not a RentalCar");
                System.out.println("Skipping this vehicle...");
            }
        }
        return productionMap;
    }

    //no argument constructor
    //initialize current car distribution in every store branch
    public carRentalSchedule() {
        Store sampleStore = null;
        try {
            sampleStore = new Store("Sample City", "Sample State");
        }catch( ObjectOverLimitException e){
            System.out.println("ObjectOverLimitException Occurred");
            System.out.println(e.getMessage());
        }

        if( sampleStore != null){
            availabilityArray.add(loadInventory(sampleStore));
        }else{
            System.out.println("No sample available");
        }
    }

    //constructor
    // instantiate for only one Store
    // this will append the inventory of that store to our arrayList
    public carRentalSchedule(Store targetStore) {
        availabilityArray.add(loadInventory(targetStore));
    }

    //constructor
    // for loading an array of Stores
    public carRentalSchedule(Store [] storeArray) {
        for (Store store : storeArray) {
            availabilityArray.add(loadInventory(store));
        }
    }

    //helper method
    //checks if a car is available at a specific store branch
    private boolean checkAvailabilityModel(String  modelName, HashMap<String, RentalCar> targetMap){
        for(RentalCar car: targetMap.values()) {
            if (car.getModel().equals(modelName)) {
                return true;
            }
        }
        return false; // if not found
    }

    //helper method
    //checks if a car is available at a specific store branch
    private boolean checkAvailabilityVIN(String  carVIN, HashMap<String, RentalCar> targetMap){
        return targetMap.containsKey(carVIN);
    }

    //this method checks if any branch has this specific model
    public boolean isAvailableInAnyBranch(String modelName) {
        boolean available = false;
        HashMap<String, RentalCar> tmpMap;
        ListIterator <HashMap<String, RentalCar>> iterator = availabilityArray.listIterator();
        while (iterator.hasNext()) {
            tmpMap = iterator.next();
            available = checkAvailabilityModel(modelName, tmpMap);
        }
        return available;
    }

    //overloaded, check if a specific car object is available in any branch
    //useful when a car is transferred to another branch but lost track of it
    public boolean isAvailableInAnyBranch( Car carOfInterest ) {
        boolean available = false;
        String carVIN = carOfInterest.getVIN();
        HashMap<String, RentalCar> tmpMap;
        ListIterator <HashMap<String, RentalCar>> iterator = availabilityArray.listIterator();
        while (iterator.hasNext()) {
            tmpMap = iterator.next();
            available = checkAvailabilityModel(carVIN, tmpMap);
            //if the specified model is available in any branch then break the loop
            if (available){ break; }
        }

        return available;
    }

    public LocalDateTime getEstimatedAvailableDate( String carVIN) {
        LocalDateTime estimatedAvailableDate = LocalDateTime.now(); //default the value to time of now
        HashMap<String, RentalCar> tmpMap;
        boolean availableAtThisStore = false;
        ListIterator <HashMap<String, RentalCar>> iterator = availabilityArray.listIterator();
        while (iterator.hasNext()) {
            tmpMap = iterator.next();
            availableAtThisStore = checkAvailabilityVIN(carVIN, tmpMap);
            if (availableAtThisStore){
                //if found, added the days of rent to current time to give an estimated return date
                estimatedAvailableDate = estimatedAvailableDate.plusDays(tmpMap.get(carVIN).getDaysRented());
                break; //break the loop if found
            }
        }


        return estimatedAvailableDate;
    }


    }
