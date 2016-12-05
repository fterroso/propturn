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
package cepdest.pattern;

import cepdest.area.AreaOfInterest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class EnhancedPatternNode extends PatternNode{
    
    static Logger LOG = Logger.getLogger(EnhancedPatternNode.class);

    Map<String, List<String>> patternIdsForSon;
    Map<String, List<String>> sonsForPatternId;
    
    String startingPatternId; 
    
    //Añadir posicion en cada patron.
    Map<String,List<Integer>> positionsForPatternId;
        
    Map<String, Integer> numRepetitionsForPattern;
    Map<String,Map<AreaOfInterest,Integer>> destinations;
    
    Set<String> ownPatternIds;
    
    int index;
    
    public EnhancedPatternNode(AreaOfInterest area){
        super(area);
        patternIdsForSon = new HashMap<String, List<String>>();
        sonsForPatternId = new HashMap<String, List<String>>();
        startingPatternId = element.toString() + "_";
        numRepetitionsForPattern = new HashMap<String, Integer>();
        destinations = new HashMap<String,Map<AreaOfInterest,Integer>>(); 
        ownPatternIds = new HashSet<String>();
        positionsForPatternId = new HashMap<String,List<Integer>>();
        index = 0;
    }
    
    public int getNumRepetitionsForPatternIds(List<String> ids){
        int n = 0;
        
        for(String id : ids){
            n+= getNumRepetitionForPatternId(id);
        }
        
        return n;
    }
    
    public void addPatternId(String patternId, int pos, String nextNodeId){
        incNumRepetitions(patternId);
        setPositionForPattern(patternId, pos);
        setSonWithPatternId(nextNodeId, patternId);
    }

    public int getNumRepetitionForPatternId(String patternId) {
        if(numRepetitionsForPattern.containsKey(patternId)){
            List<String> sonsForPattern = getSonsForPatternId(patternId);           
            int n = sonsForPattern != null ? sonsForPattern.size() : 1;
            return numRepetitionsForPattern.get(patternId)/n;          
        }else{
            return 0;
        }        
    }

    public void setNumRepetitions(String patternId, int numRepetitions) {
        this.numRepetitionsForPattern.put(patternId, numRepetitions);
        this.numRepetitions += numRepetitions;
    }
    
    public void incNumRepetitions(String patternId){
        int n = getNumRepetitionForPatternId(patternId);
        setNumRepetitions(patternId, n+1);
    }
    
    public void setPositionForPattern(String patternId, int position){
        List<Integer> positions = (positionsForPatternId.containsKey(patternId)) ? positionsForPatternId.get(patternId) : new ArrayList<Integer>();
        positions.add(position);
        positionsForPatternId.put(patternId, positions);
    }
    
    public List<Integer> getPositionsForPatternId(String patternId){
        return (positionsForPatternId.containsKey(patternId)) ? positionsForPatternId.get(patternId) : null;
    }
    
     public Map<AreaOfInterest, Integer> getAllDestinations() {
         List<String> allPatternIds = new LinkedList(getPatternIds());
         return getDestinations(allPatternIds);         
     }
    
    public Map<AreaOfInterest, Integer> getDestinations(List<String> patternIds) {
        Map<AreaOfInterest, Integer> destCandidates = new HashMap<AreaOfInterest, Integer>();
        for(String patternId : patternIds){
            Map<AreaOfInterest, Integer> destForPatterns = getDestinations(patternId);
            if(destForPatterns != null){
                for(AreaOfInterest a : destForPatterns.keySet()){
                    int n = (destCandidates.containsKey(a)) ? destCandidates.get(a) : 0;
                    n += destForPatterns.get(a);
                    destCandidates.put(a, n);
                }
            }
        }
        return destCandidates;
    }

    public Map<AreaOfInterest, Integer> getDestinations(String patternId) {
        return destinations.get(patternId);
    }

    public void setDestination(String patternId, AreaOfInterest destination) {
        Map<AreaOfInterest,Integer> dests = (destinations.containsKey(patternId)) ? destinations.get(patternId) : new HashMap<AreaOfInterest,Integer>();
        int i = (dests.containsKey(destination)) ? dests.get(destination) : 0;
        dests.put(destination, i+1);
        destinations.put(patternId, dests);
    }
    
    public List<String> getSonsForPatternId(String patternId){
        return (sonsForPatternId.containsKey(patternId) ? new ArrayList(sonsForPatternId.get(patternId)) : null);
    }
    
    public List<String> getSonsForPatternIds(List<String> patternIds){
        Set<String> sons = new HashSet<String>();
        
        for(String patternId : patternIds){
            List<String> sonsForPattern = getSonsForPatternId(patternId);
            if(sonsForPattern != null)
                sons.addAll(sonsForPattern);
        }
        return new ArrayList(sons);
    }
    
    public List<String> getPatternIdsForSon(String sonId) {        
        return (patternIdsForSon.containsKey(sonId)) ? new ArrayList(patternIdsForSon.get(sonId)) : null;
    }

    public void setSonWithPatternId(String sonId, String patternId) {
        List<String> patternIds = (patternIdsForSon.containsKey(sonId)) ? patternIdsForSon.get(sonId) : new ArrayList<String>();
        if(!patternIds.contains(patternId)){
            patternIds.add(patternId);
            patternIdsForSon.put(sonId, patternIds);
        }     
        
        List<String> sonIds = (sonsForPatternId.containsKey(patternId)) ? sonsForPatternId.get(patternId) : new ArrayList<String>();
        if(!sonIds.contains(sonId)){
            sonIds.add(sonId);
            sonsForPatternId.put(patternId, sonIds);
        }                
    }
    
    public int getNumOfSons(){
        return patternIdsForSon.keySet().size();
    }
    
    public Set<String> getPatternIds(){
        return numRepetitionsForPattern.keySet();
    }  

    public Set<String> getOwnPatternIds() {
        return new HashSet(ownPatternIds);
    }
    
    public Set<String> getOutboundPatternIds(){
        return sonsForPatternId.keySet();
    }

    public String createNewOwnPatternId() {
        String newPatternId = startingPatternId + String.valueOf(++index);
        ownPatternIds.add(newPatternId);
        return newPatternId;
    }
    
    public Set<String> getSonIds(){
        return patternIdsForSon.keySet();      
    }

    
    
    public String toString(String patternId){
        StringBuilder sb= new StringBuilder();
        sb.append("[");
        sb.append(element);
        sb.append(",");
        sb.append(getNumRepetitionForPatternId(patternId));
        sb.append(",");
        sb.append(positionsForPatternId.get(patternId));
        
        if(destinations.containsKey(patternId)){
            sb.append(", dest:{");
            Map<AreaOfInterest,Integer> destinationForPattern = destinations.get(patternId);
            for(AreaOfInterest destination : destinationForPattern.keySet()){
                sb.append(destination);
                sb.append(":");
                sb.append(destinationForPattern.get(destination));
                sb.append(",");
            }
            sb.append("}");            
        }

        sb.append("]->");
        
        return sb.toString();
    }
    
    @Override
    public String toString(){
        StringBuilder sb= new StringBuilder();
        sb.append("[");
        sb.append(element);
        sb.append(", rep{");
        sb.append(numRepetitionsForPattern);
        sb.append("}");        
        sb.append(", sons:{");
        sb.append(patternIdsForSon);
        sb.append("}");
        sb.append("]\n");
        
        return sb.toString();
    }
}
