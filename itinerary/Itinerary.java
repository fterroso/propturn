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
package cepdest.itinerary;

import cepdest.area.AreaOfInterest;
import cepdest.area.ItAreaOfInterestType;
import cepdest.area.TurningArea;
import cepdest.area.feature.AreaOfInterestFeature;
import cepdest.itinerary.Itinerary.ItineraryStats.PredictionType;
import cepdest.prediction.PredictedArea;
import cepdest.prediction.Prediction;
import cepdest.stats.PredictionStats;
import cepdest.tools.CEPdestSupportFunctions;
import ceptraj.tool.supportFunction.LocationFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lda.location.Location;
import lda.place.Place;
import org.apache.log4j.Logger;

    
/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class Itinerary {
    
    static Logger LOG = Logger.getLogger(Itinerary.class);
    
    List<AreaOfInterest> areas;
    List<AreaOfInterestFeature> features;
    
    Place startPlace;
    Place finalPlace;
    
    Map<Integer,Prediction> predictions;
    
    private List<Integer>namedAreasPositions;    
    
    ItineraryStats stats = null;
    
    String name;
    double length;
    
    int numOfNewAreas = 0;
    int numOfOldAreas = 0;
    
    int unassignedPoints = 0;
    
    public Itinerary(String name) {
        this.areas = new ArrayList<AreaOfInterest>();
        this.features = new ArrayList<AreaOfInterestFeature>();
        this.predictions = new HashMap<Integer,Prediction>();
        this.namedAreasPositions = new ArrayList<Integer>();
                
        this.name = name;
    }

    public List<AreaOfInterestFeature> getFeatures() {
        return features;
    }
    
    public void addFeature(AreaOfInterestFeature feature){
        features.add(feature);
    }
        
    /**
     * Method reponsible for adding a new area to the current itinerary.
     *
     * @param newArea The new area to be inserted in the current itinerary
     * @return a boolean indicating if the area has been inserted or not.
     */
    public boolean addArea(AreaOfInterest newArea){
        if(areas.isEmpty() || 
                !areas.get(areas.size()-1).getType().equals(newArea.getType()) || 
                !areas.get(areas.size()-1).equals(newArea)){
                       
            areas.add(newArea);
            if(newArea.isNamed()){
                namedAreasPositions.add(areas.size()-1);
                if(newArea.isNew()){ 
                    numOfNewAreas++;
                }else{
                    numOfOldAreas++;
                }
                return true;
            }else{
                incUnassignedPoints();
            }                                 
        }
        return false;
    }
        
    /**
     * Inserts a new prediction referring to this itinerary (in particular
     * to the areas of interest covered so far).
     * @param p the prediction to be inserted.
     */
    public void addPrediction(Prediction p){
        predictions.put(areas.size(), p);
    }

    public List<AreaOfInterest> getAreas() {
        return areas;
    } 
    
    public List<AreaOfInterest> getAllNamedAreas(){
        
        List<AreaOfInterest> namedAreas = new ArrayList<AreaOfInterest>();

        Collections.sort(namedAreasPositions);
        for(int i : namedAreasPositions){
            namedAreas.add(areas.get(i));
        }
        
        return namedAreas;
    }
    
    public List<AreaOfInterest> getOldNamedAreas(){
        List<AreaOfInterest> allAreas = getAllNamedAreas();
        List<AreaOfInterest> oldAreas = new ArrayList<AreaOfInterest>();
        for(AreaOfInterest a : allAreas){
            if(!a.isNew()){
                oldAreas.add(a);
            }
        }
        return oldAreas;
    }

    public int getNumOfAllNamedAreas() {
        return namedAreasPositions.size();
    }    
    
    public int getNumOfOldNamedAreas(){
        return numOfOldAreas;
    }

    public String getName() {
        return name;
    }
        
    private int getLastNamedAreaPosition(){
        int n = getNumOfAllNamedAreas();
        if(n>0){
            return namedAreasPositions.get(n-1);
        }
        return -1;
    }
    
    public AreaOfInterest getLastNamedArea(){
        int n = getLastNamedAreaPosition();
        if(n>=0){
            return getAreas().get(n);
        }else{
            return null;
        }
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }
    
    public void incUnassignedPoints(){
        unassignedPoints++;
    }

    public int getUnassignedPoints() {
        return unassignedPoints;
    }

    private Prediction getPredictionForPos(int pos){
        if(predictions.containsKey(pos)){
            return predictions.get(pos);
        }
        return null;
    }

    /**
     * This method makes up the statistical data of the itinerary
     *
     * @return the itinerary statistics.
     */
    public ItineraryStats getStatistics(){
        
        if(stats == null){
            stats = new ItineraryStats(name);

            stats.numOfAreas = getAreas().size();
            
            stats.nextAreaDR = new PredictionType[stats.numOfAreas];
            Arrays.fill(stats.nextAreaDR, PredictionType.INCORRECT);

            stats.destinationDR = new PredictionType[stats.numOfAreas];
            Arrays.fill(stats.destinationDR, PredictionType.INCORRECT);
            
            stats.nextAreaDist = new double[stats.numOfAreas];
            Arrays.fill(stats.nextAreaDist, Double.POSITIVE_INFINITY);

            stats.destinationDist = new double[stats.numOfAreas];
            Arrays.fill(stats.destinationDist, Double.POSITIVE_INFINITY);
            
            stats.predictionChainLength = new double[stats.numOfAreas];
            Arrays.fill(stats.predictionChainLength, 0);
            
            stats.coveredRoute = new int[stats.numOfAreas];
            
            stats.predLengthsDR =  new HashMap<Integer,Double>();
            stats.predLengthsDist =  new HashMap<Integer,Double>();
            
            Location lDest = getFinalDestination().getMeaningfulLocation();
            
            double avgTime = 0;
            double timeCounter = 0;
            
            double avgPredLength = 0;
            double avgPredLenghtCounter = 0;                   
            
            int i =0;
            for(; i< stats.numOfAreas && getPredictionForPos(i)== null; i++){
                AreaOfInterest currentArea = getAreas().get(i);
                int coveredRange = (int) Math.round((currentArea.getLengthFromOrigin()/getLength())*10);
                stats.coveredRoute[i] = coveredRange;
                
                stats.nextAreaDR[i] = PredictionType.NO_PREDICTION;
                stats.destinationDR[i] = PredictionType.NO_PREDICTION;
            }
                           
            Prediction prediction;
            List<PredictedArea> pAreas = null;
            List<PredictedArea> pDestAreas;
            int predIndex;
            predIndex = i;
            double maxDestDist=Double.NEGATIVE_INFINITY;
            AreaOfInterest prevArea = null;
                    
            for(;i<stats.numOfAreas; i++){

                AreaOfInterest currentArea = getAreas().get(i);
                int coveredRange = (int) Math.round((currentArea.getLengthFromOrigin()/getLength())*10);
                stats.coveredRoute[i] = coveredRange;
                PredictionStats pStats = null;
                
                prediction = getPredictionForPos(i);
                if(prediction!=null){

                    pStats = new PredictionStats();
                    if(i>0){
                        pStats.setInitialTimestamp(getAreas().get(i-1).getTimestamp());
                    }
                    pStats.setTimestamp(currentArea.getTimestamp());
                    pStats.setWeight(prediction.getNextAreaWeight());
                            
                    predIndex = i;
                    pAreas = prediction.getPredictedNextAreas();

                    avgTime += prediction.getTime();
                    timeCounter++;
                    stats.predictionChainLength[predIndex] = prediction.getLengthOfUsedChain();

                    // We check if the forecasted eventual destination was correct.
                    pDestAreas = prediction.getPredictedDestination();
                    maxDestDist = Double.NEGATIVE_INFINITY;
                    for(PredictedArea pArea : pDestAreas){
                        if(pArea.getArea().equals(getFinalDestination())){
                            stats.destinationDR[predIndex] = PredictionType.CORRECT;
//                            totalCorrectDestPred++;
                        }

                        Location l2 = pArea.getArea().getMeaningfulLocation();
                        double dist = LocationFunction.dist(
                                CEPdestSupportFunctions.toCEPtrajPoint(lDest),
                                CEPdestSupportFunctions.toCEPtrajPoint(l2));

                        //We choose the minimum distance critera.
                        if(dist > maxDestDist){
                            maxDestDist = dist;
                        }                          
                    }
                    stats.destinationDist[predIndex] = maxDestDist;
                }else{
                    stats.nextAreaDR[i] = PredictionType.NO_PREDICTION;
                    stats.destinationDR[i] = PredictionType.NO_PREDICTION;
                    stats.destinationDist[i] = stats.destinationDist[predIndex];
                }

                double maxNextAreaDist = Double.NEGATIVE_INFINITY;
                double avgPredLengths = 0;
                double avgNextAreaDist = 0;

                Location currentLoc = currentArea.getMeaningfulLocation();
                Location prevLoc =  null;
                if(prevArea!= null) prevLoc = prevArea.getMeaningfulLocation();
                for(PredictedArea pArea : pAreas){
                    if(pArea.getArea().equals(currentArea)){
                        stats.numOfCorrectedPredictions++;                            
                        stats.nextAreaDR[predIndex] = PredictionType.CORRECT;
                    }

                    Location predLoc = pArea.getArea().getMeaningfulLocation();
                    double dist = LocationFunction.dist(
                            CEPdestSupportFunctions.toCEPtrajPoint(currentLoc),
                            CEPdestSupportFunctions.toCEPtrajPoint(predLoc));

//                    LOG.debug(currentArea.getID()+ "<>"+pArea.getArea().getID()+ ": "+dist);
                    //We choose the maximum distance critera.
                    if(dist > maxNextAreaDist){                            
                        maxNextAreaDist = dist;
                    } 
                    
                    avgNextAreaDist += dist;
                    
                    if(prevLoc!=null){
                        dist = LocationFunction.dist(
                            CEPdestSupportFunctions.toCEPtrajPoint(prevLoc),
                            CEPdestSupportFunctions.toCEPtrajPoint(predLoc));
                            avgPredLengths += dist;
                    }                    
                }
                avgPredLengths /= pAreas.size();
                avgNextAreaDist /= pAreas.size();
                
                if(avgPredLengths >0){
                    avgPredLenghtCounter++;
                    avgPredLength += avgPredLengths;
                    
                    // Here we discretize the prediction length stats
                    int p = (int)avgPredLengths/50;
                    if(pStats!=null) pStats.setDistLength(p);
                    
                    double c = (stats.predLengthsDR.containsKey(p))?stats.predLengthsDR.get(p) : 0;
                    c++;
                    stats.predLengthsDR.put(p, c);
                    
                    double d = (stats.predLengthsDist.containsKey(p))?stats.predLengthsDist.get(p) : 0;
                    d += avgNextAreaDist;
                    stats.predLengthsDist.put(p, d);
                }

                stats.nextAreaDist[i] = maxNextAreaDist;
                if(pStats!= null) pStats.setDistError(maxNextAreaDist);
                
                if(predIndex == (i-1)){
                    stats.nextAreaDist[predIndex] = maxNextAreaDist;
                }
                
                prevArea = currentArea;
                if(pStats.getWeight()> 0 && pStats.getDistError() < 1500) stats.predictionStats.add(pStats);
            }
           
            double total = namedAreasPositions.size() + unassignedPoints;
            stats.namedAreasPct = (double) namedAreasPositions.size() / total;
//            LOG.debug(stats.namedAreasPct+": "+namedAreasPositions.size() + " " +total);                       
            
            if(timeCounter > 0){
                stats.avgPredictionTime = avgTime / timeCounter;
            }
            
            if(avgPredLenghtCounter > 0){
                // Total prediction length
                stats.avgPredLenght = avgPredLength / avgPredLenghtCounter;
                
                //Discretized prediction-length DR and dist (intervals)
                for(int p : stats.predLengthsDR.keySet()){
                    double c = stats.predLengthsDR.get(p);
                    c /= avgPredLenghtCounter;
                    stats.predLengthsDR.put(p, c);
                    
                    double d = stats.predLengthsDist.get(p);
                    d /= avgPredLenghtCounter;
                    stats.predLengthsDist.put(p, d);
                }                
            }
            
        }
        
        return stats;
    }
    
    @Override
    public int hashCode(){

        int hash = getNumOfAllNamedAreas();
        
        for(AreaOfInterest area : areas){
            hash += area.hashCode();
        }
                    
        return hash;
    }
    
    public AreaOfInterest getFinalDestination(){
        
        return getAreas().get(getAreas().size()-1);
    }
    
    @Override
    public boolean equals(Object obj) {
    
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        Itinerary pattern = (Itinerary) obj;     
        
        if(pattern.getAreas().size() != areas.size()){
            return false;
        }
         
        for(int i= 0; i< areas.size(); i++){
            if(!pattern.getAreas().get(i).equals(areas.get(i))){
                return false;
            }            
        }
        
        return true;
    }
    
    public double getEntropy(){
        List<AreaOfInterest> namedAreas = getAllNamedAreas();
        
        StringBuilder readSeq = new StringBuilder();
        readSeq.append("");
        double counter = 0;
        for(int i = 2; i< namedAreas.size()-1;i++){
            StringBuilder currentSeq = new StringBuilder();
            currentSeq.append(namedAreas.get(i));
            currentSeq.append("->");
            currentSeq.append(namedAreas.get(i+1));
            int j = 2;
            while(readSeq.toString().contains(currentSeq.toString()) &&
                    (i+j)< namedAreas.size()){
                currentSeq.append("->");
                currentSeq.append(namedAreas.get(i+j));
                j++;
            }
            if(!readSeq.toString().contains(currentSeq.toString())){
//                LOG.debug("Entra: ["+readSeq+"] ["+currentSeq+"]");
                readSeq.append(namedAreas.get(i));
                readSeq.append("->");
                counter += (double)j/Math.log(i);
//                LOG.info(counter);
            }
        }
        double h = Math.pow(counter/namedAreas.size(), -1);
        return h;
    }
   
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(name);
        sb.append(", {");
        for(AreaOfInterest area : getAllNamedAreas()){
            if(ItAreaOfInterestType.TURNING.equals(area.getType())){
                sb.append((TurningArea)area);
            }else{
                sb.append(area);
            }
            sb.append(", ");
            
        }
        sb.append("}");      
        
        sb.append("]");
        
        return sb.toString();
        
    }
    
    public static class ItineraryStats{
        
        public enum PredictionType{
            CORRECT,
            INCORRECT,
            NO_PREDICTION;
        }
        
        protected String itineraryName;
        
        protected int numOfAreas;
        
        protected PredictionType[] nextAreaDR;       
        protected PredictionType[] destinationDR;
        
//        protected double avgDR;
        
        protected double[] nextAreaDist;
        protected double[] destinationDist;
        
//        protected double avgDist;
        
        protected int[] coveredRoute;
        
        protected double[] predictionChainLength;
        
        protected double numOfCorrectedPredictions;
        protected double avgPredictionTime = 0;
        
        protected double avgPredLenght = 0;
        
        protected double namedAreasPct;
        
        protected Map<Integer,Double> predLengthsDR;
        protected Map<Integer,Double> predLengthsDist;
        
        protected List<PredictionStats> predictionStats;

        public ItineraryStats(String itineraryName) {
            this.itineraryName = itineraryName;
            predictionStats = new ArrayList<PredictionStats>();
        }

        public String getItineraryName() {
            return itineraryName;
        }

        public int getNumOfAreas() {
            return numOfAreas;
        }

        public PredictionType[] getNextAreaDR() {
            return nextAreaDR;
        }

        public PredictionType[] getDestinationDR() {
            return destinationDR;
        }

        public double[] getNextAreaDist() {
            return nextAreaDist;
        }

        public double getNumOfCorrectedPredictions() {
            return numOfCorrectedPredictions;
        }

        public double[] getDestinationDist() {
            return destinationDist;
        }

        public int[] getCoveredRoute() {
            return coveredRoute;
        }

        public double[] getPredictionChainLength() {
            return predictionChainLength;
        }

        public double getAvgPredictionTime() {
            return avgPredictionTime;
        }  

        public double getAvgPredLenght() {
            return avgPredLenght;
        }
        
//        public double getAvgDR(){
//            return avgDR;
//        }
//
//        public double getAvgDist() {
//            return avgDist;
//        }

        public double getNamedAreasPct() {
            return namedAreasPct;
        }

        public Map<Integer, Double> getPredLengthsDR() {
            return predLengthsDR;
        }

        public Map<Integer, Double> getPredLengthsDist() {
            return predLengthsDist;
        }

        public List<PredictionStats> getPredictionStats() {
            return predictionStats;
        }

        
        
    }   
    
}
