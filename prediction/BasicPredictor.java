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
package cepdest.prediction;

import cepdest.area.AreaOfInterest;
import cepdest.itinerary.Itinerary;
import cepdest.pattern.PatternNode;
import cepdest.pattern.holder.PatternsHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class BasicPredictor implements Predictor{
    
    static Logger LOG = Logger.getLogger(BasicPredictor.class);

    PredictorConfig config;

    public BasicPredictor(PredictorConfig config) {
        this.config = config;
    }
    
    @Override
    public Prediction getPrediction(
            PatternsHolder patterns, 
            Itinerary it,
            double... bearing){
        PatternNode node = patterns.getCurrentNode(it);
        if(node != null)
            return getPrediction(node);
        
        return null;
     }
    
    private Prediction getPrediction(PatternNode node) {
        
        Prediction holder = new Prediction();
        
        //Next areas
         List<PredictedArea> candidates = getPredictedNextAreas(node);      
        
        for(int i =0; i< candidates.size(); i++){
            holder.getPredictedNextAreas().add(candidates.get(i));
        }
        
        //Next destinations
        candidates = getPredictedDestination(node);
               
        for(int i =0; i< candidates.size(); i++){
            holder.getPredictedDestination().add(candidates.get(i));
        }
        
        return holder;
    }
    
    @Override
    public Prediction getPredictionWithBearing(PatternsHolder patterns, Itinerary it) {
        return null;
    }

    private List<PredictedArea> getPredictedNextAreas(PatternNode node){
        
        List<PredictedArea> candidates = new ArrayList<PredictedArea>();
      
        double totalRepetitions = node.getNumRepetitions();
        
        for(PatternNode son : node.getSons()){
            double prob = son.getNumRepetitions()/totalRepetitions;
            if(prob >= config.getMinIndividualProbability()){
                PredictedArea pArea = new PredictedArea(son.getElement(), prob);
                candidates.add(pArea);
            }
        }
        
        Collections.sort(candidates);
        
        return candidates;
    }
    
    private List<PredictedArea> getPredictedDestination(PatternNode node){
        
        List<PredictedArea> candidates = new ArrayList<PredictedArea>();
        
        double totalRepetitions = node.getNumRepetitions();
        for(AreaOfInterest dest : node.getDestinations().keySet()){
            double prob = node.getDestinations().get(dest) / totalRepetitions;
            if(prob >= config.getMinIndividualProbability()){
                PredictedArea pArea = new PredictedArea(dest, prob);
                candidates.add(pArea);
            }            
        }
        Collections.sort(candidates);
        
        return candidates;
    }
    
}
