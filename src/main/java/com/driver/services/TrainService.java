package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();
        List<Station> stationList = trainEntryDto.getStationRoute();
        String route = "";
        for(int i = 0;i<stationList.size();i++){
            if(i==stationList.size()-1){
                route+=String.valueOf(stationList.get(i));
                continue;
            }
            route+=String.valueOf(stationList.get(i))+",";
        }
        train.setRoute(route);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D//////
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.iubv
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        return train.getNoOfSeats();
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train = trainRepository.findById(trainId).get();
        String stations[] = train.getRoute().split(",");
        boolean isTrainPassFromStation = false;
        for(int i =0;i<stations.length;i++){
            if(stations[i].equals(station)){
                isTrainPassFromStation = true;
                break;
            }
        }
        if(!isTrainPassFromStation){
            throw new Exception("Train is not passing from this station");
        }
        int peopleBoardingAtAStation = 0;
        List<Ticket> tickets = train.getBookedTickets();
        for(Ticket ticket: tickets){
            if(ticket.getFromStation().equals(station)){
                peopleBoardingAtAStation+= ticket.getPassengersList().size();
            }
        }

        return peopleBoardingAtAStation;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train = trainRepository.findById(trainId).get();
        List<Ticket> tickets = train.getBookedTickets();

        int oldestPersonAge = 0;
        for(Ticket ticket: tickets){
            List<Passenger> passengerList = ticket.getPassengersList();
            for(Passenger passenger: passengerList){
                if(passenger.getAge()>oldestPersonAge){
                    oldestPersonAge = passenger.getAge();
                }
            }
        }

        return oldestPersonAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> trainListPassingFromGivenStation = new ArrayList<>();

        List<Train> trains = trainRepository.findAll();
        for(Train train: trains){
            int time = -1;
            String stations[] = train.getRoute().split(",");
            boolean isTrainPassFromStation = false;
            for(int i =0;i<stations.length;i++){
                if(stations[i].equals(station)){
                    isTrainPassFromStation = true;
                    time = i;
                    break;
                }
            }
            if(!isTrainPassFromStation){
                continue;
            }
            LocalTime trainLocalTime = train.getDepartureTime();
            int totalTimeOfTrainToReachGivenStationInMinutes = time*60 + trainLocalTime.getHour()*60+trainLocalTime.getMinute();
            int givenStartTimeInMinutes = startTime.getHour()*60+startTime.getMinute();
            int givenEndTimeInMinutes = endTime.getHour()*60+endTime.getMinute();

            if(totalTimeOfTrainToReachGivenStationInMinutes>= givenStartTimeInMinutes && totalTimeOfTrainToReachGivenStationInMinutes<=givenEndTimeInMinutes){
                trainListPassingFromGivenStation.add(train.getTrainId());
            }
        }

        return trainListPassingFromGivenStation;
    }

}