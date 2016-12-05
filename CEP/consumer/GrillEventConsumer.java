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

import cepdest.area.AreaOfInterest;
import cepdest.area.ItAreaOfInterestType;
import cepdest.itinerary.Itinerary;
import cepdest.pattern.holder.PatternsHolder;
import cepdest.prediction.Predictor;
import cepdest.stats.StatsGenerator;
import cepdest.tools.CEPdestSupportFunctions;
import cepdest.tools.Constants;
import ceptraj.event.itinerary.ItineraryFinishesStartsEvent;
import ceptraj.event.trajectory.TrajectoryEvent;
import ceptraj.tool.Bearing;
import ceptraj.tool.Point;
import ceptraj.tool.supportFunction.LocationFunction;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class GrillEventConsumer extends EventConsumerWithPrediction{    

    Point referencePoint = new Point(39.973056,116.400639);
        
    public GrillEventConsumer(
            PatternsHolder patternsHolder, 
            Predictor predictor, 
            StatsGenerator statsGen) {
        super(patternsHolder, predictor, statsGen);
    }
    
    
    @Override
    protected void processTrajectoryEvent(TrajectoryEvent te){
        
        watch.reset();
        watch.start();
        
        super.processTrajectoryEvent(te);       
        
        double bearing = Bearing.bearing(referencePoint, te.getHead());
         
        int x;
        if(bearing >= 0 && bearing < 180){
            x = hDist(referencePoint,te.getHead());
        }else{
            x = 0 - hDist(referencePoint,te.getHead());
        }
        
        int y;        
        if(bearing >= 90 && bearing < 270){
            y = 0 - vDist(referencePoint,te.getHead());
        }else{
            y = vDist(referencePoint,te.getHead());
        }
        
        String id = String.valueOf(x)+"_"+String.valueOf(y);
        
        AreaOfInterest area = new AreaOfInterest(
                CEPdestSupportFunctions.toLDALocation(te.getHead()),
                ItAreaOfInterestType.CELL);
        area.setID(id);
        area.setLengthFromOrigin(lengthOfCurrentItinerary);
                
        if(currentItinerary.addArea(area)){
            makePrediction();
        }
        
        if(watch.isStarted())
            watch.stop();
    }
    
    
       @Override
    protected void processItineraryEvent(ItineraryFinishesStartsEvent event) {
                                       
        currentItinerary.setLength(lengthOfCurrentItinerary);
        patternsHolder.insertNewItinerary(currentItinerary);
                
        statsGen.itineraryHasFinished(currentItinerary);
        
        currentItinerary = new Itinerary(event.getStartedItineraryId());
        lengthOfCurrentItinerary = 0;
               
    }
    

    public int hDist(Point refPoint, Point p){
        
        return ((int)LocationFunction.haversineDistance(refPoint, new Point(refPoint.getLat(), p.getLon()))/(int)Constants.CELL_SIZE)+1;
                   
    }
    
    public int vDist(Point refPoint, Point p){
        
        return ((int)LocationFunction.haversineDistance(refPoint, new Point(p.getLat(), refPoint.getLon()))/(int)Constants.CELL_SIZE)+1;
        
    }
    
}
