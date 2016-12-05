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

import cepdest.config.ConfigProvider;
import cepdest.config.ConfigProviderFactory;
import cepdest.itinerary.Itinerary;
import cepdest.pattern.holder.PatternsHolder;
import cepdest.prediction.Prediction;
import cepdest.prediction.Predictor;
import cepdest.stats.StatsGenerator;
import ceptraj.event.trajectory.TrajectoryEvent;
import ceptraj.output.visualizer.OutputType;
import ceptraj.output.visualizer.Visualizer;
import ceptraj.tool.Point;
import ceptraj.tool.supportFunction.LocationFunction;
import org.apache.commons.lang3.time.StopWatch;

/**
 * Basic class with the common functionalities of the event consumers for
 * prediction purposes.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class EventConsumerWithPrediction extends Visualizer{
    
    PatternsHolder patternsHolder;
    Predictor predictor;
    Itinerary currentItinerary = new Itinerary("1");
    StatsGenerator statsGen;
    
    ConfigProvider config;
    
    double lengthOfCurrentItinerary;
    
    StopWatch watch;
    
    int numReceivedSPs = 0;
    
    public int getNumReceivedSPs(){
        return numReceivedSPs;
    }

    public EventConsumerWithPrediction(
            PatternsHolder patternsHolder, 
            Predictor predictor, 
            StatsGenerator statsGen) {
        super();
        this.patternsHolder = patternsHolder;
        this.predictor = predictor;
        this.statsGen = statsGen;        
        
        this.config = ConfigProviderFactory.getConfig();
        
        watch = new StopWatch();
        
    }
   
    @Override
    public void serializeTraceInFilePath(String path, OutputType outputType) {
        
        printMapElements(path, "trajectoryChangeEvents_bearing", outputType, bearingTrajectoryChangeEvents);    
        printMapElements(path, "locationEvents", outputType, bearingLocationEvents);    

        LOG.info("CEP-traj info serialized.");
    }
    
    protected boolean makePrediction(double...bearing){
        
//        Prediction prediction = predictor.getPrediction(patternsHolder, currentItinerary,bearing);
        Prediction prediction = predictor.getPredictionWithBearing(patternsHolder, currentItinerary);

        if(watch.isStarted())
            watch.stop();
        
        if(prediction != null){
            prediction.setTime(watch.getTime());
            currentItinerary.addPrediction(prediction);
            return true;
        }
                
        return false;
    }       

    @Override
    protected void processTrajectoryEvent(TrajectoryEvent te){
        
        super.processTrajectoryEvent(te);
        
        if(te.getLevel() == 1){
            Point p1 = te.getHead();
            Point p2 = te.getTail();

            double length = LocationFunction.haversineDistance(p1, p2);
            lengthOfCurrentItinerary += length;
        }
    }
    
}
