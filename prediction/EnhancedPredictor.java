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
import cepdest.area.ItAreaOfInterestType;
import cepdest.area.TurningArea;
import cepdest.itinerary.Itinerary;
import cepdest.pattern.EnhancedPatternNode;
import cepdest.pattern.GroupOfEnhancedPatternNode;
import cepdest.pattern.holder.EnhancedPatternsHolder;
import cepdest.pattern.holder.PatternsHolder;
import cepdest.tools.CEPdestSupportFunctions;
import ceptraj.tool.Bearing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lda.location.Location;
import org.apache.log4j.Logger;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class EnhancedPredictor implements Predictor{
    
    static Logger LOG = Logger.getLogger(EnhancedPredictor.class);
    
    PredictorConfig config;

    public EnhancedPredictor(PredictorConfig config) {
        this.config = config;
    }
    
    @Override
    public Prediction getPrediction(
            PatternsHolder patterns, 
            Itinerary it,
            double...bearing) {
        
        Prediction prediction = new Prediction();
        
        EnhancedPatternsHolder ePatterns = (EnhancedPatternsHolder)patterns;
        GroupOfEnhancedPatternNode fNode = ePatterns.getCurrentNodes(it);
        
        if(fNode != null){
            
            List<String> patternIds = ePatterns.getPatternIdForItinerary(it);
                        
            double chainLenght = 0;           
            
            List<PredictedArea> nextAreaCandidates = new ArrayList<PredictedArea>();
            List<PredictedArea> destCandidates = new ArrayList<PredictedArea>();

            double norm;
             Map<AreaOfInterest, Integer> destsForNode;
            if(patternIds != null && !patternIds.isEmpty()){
                chainLenght = Double.valueOf(patternIds.get(patternIds.size()-1));
                patternIds.remove(patternIds.size()-1);
                norm = fNode.getNumRepetitionsForPatternIds(patternIds);
                for(String sonId : fNode.getSonsForPatternIds(patternIds)){
                    List<String>patternsOfSon = fNode.getPatternIdsForSon(sonId);
                    EnhancedPatternNode node = ePatterns.getNodeWithId(sonId);
                    patternsOfSon.retainAll(patternIds);
                    double sonRep = node.getNumRepetitionsForPatternIds(patternsOfSon);
                    double prob = sonRep/norm;
                    
                    if(prob >= config.minIndividualProbability){
                        if(bearing != null && bearing.length > 0){
                            double b = CEPdestSupportFunctions.bearingBetweenAreas(fNode.getElement(), node.getElement());
                            
                            double bearDiff = Bearing.bearingDifference(b, bearing[0]);
                            if(bearDiff <= config.getMaxBearingDiff()){
                                PredictedArea p = new PredictedArea(node.getElement(),prob);
                                nextAreaCandidates.add(p);
                                
                            }
                        }else{
                            PredictedArea p = new PredictedArea(node.getElement(),prob);
                            nextAreaCandidates.add(p);
                        }
                        
                    }                
                }

                destsForNode = fNode.getDestinations(patternIds);                       

            }else{
                norm = fNode.getNumRepetitionsForPatternIds(new LinkedList(fNode.getOutboundPatternIds()));//  fNode.getNumRepetitions();
                for(String sonId : fNode.getSonIds()){
                    
                    List<String> patternIdsForSon = fNode.getPatternIdsForSon(sonId);
                    EnhancedPatternNode node = ePatterns.getNodeWithId(sonId);
                    double sonRep = node.getNumRepetitionsForPatternIds(patternIdsForSon);
                    double prob = sonRep/norm;
                    if(prob >= config.minIndividualProbability){
                        if(bearing != null && bearing.length > 0){
                            double b = CEPdestSupportFunctions.bearingBetweenAreas(fNode.getElement(), node.getElement());
                            double bearDiff = Bearing.bearingDifference(b, bearing[0]);
                            if(bearDiff <= config.getMaxBearingDiff()){
                                PredictedArea p = new PredictedArea(node.getElement(),prob);
                                nextAreaCandidates.add(p);
                            }
                        }else{
                            PredictedArea p = new PredictedArea(node.getElement(),prob);
                            nextAreaCandidates.add(p);
                        }
                    }                
                }      

                destsForNode = fNode.getAllDestinations();

            }

            Collections.sort(nextAreaCandidates);        
            prediction.setPredictedNextAreas(nextAreaCandidates);

            for(AreaOfInterest a : destsForNode.keySet()){
                double rep = destsForNode.get(a);
                double prob = rep / norm;
                if(prob >= config.minIndividualProbability){
                    PredictedArea pArea = new PredictedArea(a, prob);
                    destCandidates.add(pArea);
                }
            }

            Collections.sort(destCandidates);
            prediction.setPredictedDestination(destCandidates); 
            prediction.setLengthOfUsedChain(chainLenght);   
           
        }
        
        return prediction;
        
    }
    
    @Override
    public Prediction getPredictionWithBearing(PatternsHolder patterns, Itinerary it) {
        
        Prediction prediction = new Prediction();
        
        EnhancedPatternsHolder ePatterns = (EnhancedPatternsHolder)patterns;
        GroupOfEnhancedPatternNode fNode = ePatterns.getCurrentNodes(it);
        
        if(fNode != null){
            
            List<String> patternIds = ePatterns.getPatternIdForItinerary(it);
                        
            double chainLenght = 0;           
            
            List<PredictedArea> nextAreaCandidates = new ArrayList<PredictedArea>();
            List<PredictedArea> destCandidates = new ArrayList<PredictedArea>();

            double norm;
             Map<AreaOfInterest, Integer> destsForNode;
            if(patternIds != null && !patternIds.isEmpty()){
                chainLenght = Double.valueOf(patternIds.get(patternIds.size()-1));
                patternIds.remove(patternIds.size()-1);
                norm = fNode.getNumRepetitionsForPatternIds(patternIds);
                for(String sonId : fNode.getSonsForPatternIds(patternIds)){
                    List<String>patternsOfSon = fNode.getPatternIdsForSon(sonId);
                    EnhancedPatternNode candidate = ePatterns.getNodeWithId(sonId);
                    patternsOfSon.retainAll(patternIds);
                    double sonRep = candidate.getNumRepetitionsForPatternIds(patternsOfSon);
                    double prob = sonRep/norm;
                    
                    if(prob >= config.minIndividualProbability){
                        
                        double b = CEPdestSupportFunctions.bearingBetweenAreas(fNode.getElement(), candidate.getElement());
                        
                        if(ItAreaOfInterestType.TURNING.equals(it.getLastNamedArea().getType())){
                            TurningArea ta = (TurningArea) it.getLastNamedArea();
                            double lastBearing = ta.getFinalBearingVal();
                            
                            double bearDiff = Bearing.bearingDifference(b,lastBearing);
                            
                            if(bearDiff <= config.getMaxBearingDiff()){
                                PredictedArea p = new PredictedArea(candidate.getElement(),prob);
                                nextAreaCandidates.add(p);
                                
                            }
                            
                        }else{
                            PredictedArea p = new PredictedArea(candidate.getElement(),prob);
                            nextAreaCandidates.add(p);
                        }                        
                    }                
                }

                destsForNode = fNode.getDestinations(patternIds);                       

            }else{
                // Here we only use the last node
                norm = fNode.getNumRepetitionsForPatternIds(new LinkedList(fNode.getOutboundPatternIds()));
                for(String sonId : fNode.getSonIds()){
                    
                    List<String> patternIdsForSon = fNode.getPatternIdsForSon(sonId);
                    EnhancedPatternNode candidate = ePatterns.getNodeWithId(sonId);
                    double sonRep = candidate.getNumRepetitionsForPatternIds(patternIdsForSon);
                    double prob = sonRep/norm;
                    if(prob >= config.minIndividualProbability){
                        
                        double b = CEPdestSupportFunctions.bearingBetweenAreas(fNode.getElement(), candidate.getElement());
                        
                        if(ItAreaOfInterestType.TURNING.equals(it.getLastNamedArea().getType())){
                            TurningArea ta = (TurningArea) it.getLastNamedArea();
                            double lastBearing = ta.getFinalBearingVal();
                            
                            double bearDiff = Bearing.bearingDifference(b,lastBearing);
                            
                            if(bearDiff <= config.getMaxBearingDiff()){
                                PredictedArea p = new PredictedArea(candidate.getElement(),prob);
                                nextAreaCandidates.add(p);
                                
                            }                        
                        }else{
                            PredictedArea p = new PredictedArea(candidate.getElement(),prob);
                            nextAreaCandidates.add(p);
                        }                                 
                    }
                }

                destsForNode = fNode.getAllDestinations();

            }

            Collections.sort(nextAreaCandidates);        
            prediction.setPredictedNextAreas(nextAreaCandidates);

            for(AreaOfInterest a : destsForNode.keySet()){
                double rep = destsForNode.get(a);
                double prob = rep / norm;
                if(prob >= config.minIndividualProbability){
                    PredictedArea pArea = new PredictedArea(a, prob);
                    destCandidates.add(pArea);
                }
            }

            Collections.sort(destCandidates);
            prediction.setPredictedDestination(destCandidates); 
            prediction.setLengthOfUsedChain(chainLenght);   
           
        }
        return prediction;
        
    }
    
}
