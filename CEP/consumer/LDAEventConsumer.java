/*
 * Copyright 2015 University of Murcia (Fernando Terroso-Saenz (fterroso@um.es), Mercedes Valdes-Vela, Antonio F. Skarmeta)
 * 
 * This file is part of ProPTurn.
 * 
 * ProPTurn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ProPTurn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see http://www.gnu.org/licenses/.
 * 
 */
package cepdest.CEP.consumer;

import cepdest.CEP.event.BearingChangeClusterEvent;
import cepdest.itinerary.Itinerary;
import cepdest.pattern.holder.PatternsHolder;
import cepdest.area.AreaOfInterest;
import cepdest.area.ItAreaOfInterestType;
import cepdest.area.TurningArea;
import cepdest.prediction.Predictor;
import cepdest.stats.StatsGenerator;
import cepdest.tools.CEPdestSupportFunctions;
import ceptraj.event.itinerary.ItineraryFinishesStartsEvent;
import ceptraj.event.trajectory.change.TrajectoryChangeEvent;
import ceptraj.tool.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lda.landmark.LandmarkProxy;
import lda.landmark.provider.LandmarkProviderFactory;
import lda.location.Location;
import lda.place.Place;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class LDAEventConsumer extends EventConsumerWithPrediction{
    
    List<TrajectoryChangeEvent> changeEventsBuffer = new ArrayList<TrajectoryChangeEvent>();
    
    int numItinerariesWithPrediction = 0;
    int numItinerariesWithNoPrediction = 0;
    boolean counted = false;
    
    String mostRecentLandmark = "0";
    String mostRecentSELandmark = "0";
    
    public double avgItTimeLength = 0;
    double itCounter = 1;
    long currentItStartTime = 0;
            
    public LDAEventConsumer(
            PatternsHolder patternsHolder,
            Predictor predictor,
            StatsGenerator statsGen) {
        
        super(patternsHolder, predictor, statsGen);

    }

    public void processBearingChangeClusterEvent(BearingChangeClusterEvent bcce){
        watch.reset();
        watch.start();

//        if(bcce.getFirstEvent().getId().contains("818"))
//            LOG.debug(bcce);
        
        // Here we test wheather any of the points in the event is included in a landmark
        Collection<Point> involvedPath = bcce.getFirstEvent().getInvolvedPath();        
        Iterator<Point> iterator = involvedPath.iterator();
        LandmarkProxy landmark = null;
        while(iterator.hasNext()){
            landmark = landmarkForPoint(iterator.next(), config.getUserID());
            if(landmark!=null) break;
        }
        
        TurningArea area;
        boolean isNew = false;
        if(landmark!=null){
            
            if(landmark.getID().compareTo(mostRecentLandmark) > 0){
                isNew = true;
                mostRecentLandmark = landmark.getID();
            }            
        }
        
        area = createTurningArea(landmark,bcce,isNew);
                
        if(currentItinerary.addArea(area)){
                makePrediction();
        }else{
            AreaOfInterest lastNamedArea = currentItinerary.getLastNamedArea();  
            if(lastNamedArea != null && !lastNamedArea.equals(area)){
                double b = CEPdestSupportFunctions.bearingBetweenAreas(lastNamedArea, area);
                makePrediction(b);
            }
        }

        if(watch.isStarted()){
            watch.stop();
        }
    }
       
    @Override
    protected void processItineraryEvent(ItineraryFinishesStartsEvent event) {
            
        watch.reset();
        watch.start();
        
        numReceivedSPs++;
        
        changeEventsBuffer = new LinkedList<TrajectoryChangeEvent>();
        
        Point lastLocation = event.getLastLocation();
        Point startLocation = event.getStartLocation();
                
        if(currentItStartTime != 0){
            long timeLength = lastLocation.getTimestamp() - currentItStartTime;
            avgItTimeLength = avgItTimeLength + ((timeLength-avgItTimeLength)/itCounter);
            itCounter++;
        }
        
        currentItStartTime = startLocation.getTimestamp();
        
        LandmarkProxy landmark = landmarkForPoint(
                lastLocation, 
                config.getUserID()+"_"+ItAreaOfInterestType.START_END.getAbr());
            
        AreaOfInterest area;
        if(landmark != null){
            
            boolean isNew = false;
            if(landmark.getID().compareTo(mostRecentSELandmark) > 0){
                isNew = true;
                mostRecentSELandmark = landmark.getID();
            }
            
            area = createAreaOfInterest(landmark, lastLocation.getTimestamp(), isNew);       
            if(currentItinerary.addArea(area)){
                makePrediction();
            }
        }else{
            area = createAreaOfInterest(CEPdestSupportFunctions.toLDALocation(lastLocation),lastLocation.getTimestamp(), false);
            currentItinerary.addArea(area);
        }
        
        if(watch.isStarted()){
            watch.stop();
        }
              
        currentItinerary.setLength(lengthOfCurrentItinerary);
        patternsHolder.insertNewItinerary(currentItinerary);
         
//        LOG.debug(currentItinerary);
//        LOG.debug(patternsHolder);
        statsGen.itineraryHasFinished(currentItinerary);
//        LOG.debug("----------");
        
         currentItinerary = new Itinerary(event.getStartedItineraryId());
         counted = false;
         lengthOfCurrentItinerary = 0;
         
         watch.reset();
         watch.start();
               
        landmark = landmarkForPoint(
                startLocation, 
                config.getUserID()+"_"+ItAreaOfInterestType.START_END.getAbr());
                
        if(landmark != null){
            
            boolean isNew = false;
            if(landmark.getID().compareTo(mostRecentSELandmark) > 0){
                isNew = true;
                mostRecentSELandmark = landmark.getID();
            }
            
            area = createAreaOfInterest(landmark, startLocation.getTimestamp(), isNew);       
            if(currentItinerary.addArea(area)){
                makePrediction();
            }
        }else{
            area = createAreaOfInterest(CEPdestSupportFunctions.toLDALocation(startLocation),startLocation.getTimestamp(),false);
            currentItinerary.addArea(area);
        }
        
        if(watch.isStarted()){
            watch.stop();
        }
    }

    
    private AreaOfInterest createAreaOfInterest(Place place, long timestamp, boolean isNew){
        
        AreaOfInterest area = new AreaOfInterest(place,ItAreaOfInterestType.START_END);
        area.setLengthFromOrigin(lengthOfCurrentItinerary);
        area.setIsNew(isNew);
        area.setTimestamp(timestamp);
        return area;
    }
        
    private TurningArea createTurningArea(
            Place place, 
            BearingChangeClusterEvent bcce,
            boolean isNew){
                
        TurningArea area = new TurningArea();
        if(place != null){
            area.setPlace(place);
        }else{
            area.setPlace(CEPdestSupportFunctions.toLDALocation(bcce.getCentroid()));
        }
        area.setLengthFromOrigin(lengthOfCurrentItinerary);
        area.setInitialBearingVal(bcce.getFirstEvent().getInitialBearing());
        area.setFinalBearingVal(bcce.getLastEvent().getFinalBearing());
        area.setIsNew(isNew);
        area.setTimestamp(bcce.getTimestamp());
        return area;
    }
    
    private LandmarkProxy landmarkForPoint(Point p, String userID){
        Location l = CEPdestSupportFunctions.toLDALocation(p); 
        
        return LandmarkProviderFactory.getCurrentLandmarkProvider(userID).inferLandmarkForPoint(l, true);                
    }
 
}
