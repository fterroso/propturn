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
package cepdest.pattern.holder;

import cepdest.area.AreaOfInterest;
import cepdest.area.ItAreaOfInterestType;
import cepdest.itinerary.Itinerary;
import cepdest.pattern.PatternNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lda.place.PlaceType;
import org.apache.log4j.Logger;

/**
 * Pattern holder for the simple markov process approach.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class BasicPatternsHolder implements PatternsHolder, Serializable{
    
    static Logger LOG = Logger.getLogger(BasicPatternsHolder.class);
    
    Set<PatternNode> patterns  = new HashSet<PatternNode>();
    
    @Override
    public void insertNewItinerary(Itinerary newItinerary){
                      
        if(newItinerary.getNumOfAllNamedAreas()>1){
          for(PatternNode node : patterns){
              if(node.getElement().equals(newItinerary.getAllNamedAreas().get(0))){
                  insertItineraryInNode(newItinerary,node);
//                  LOG.info(newItinerary +"<e->\n" +toPlainText("",node));
                  return;
              }
          }
          PatternNode n = extendNodeWithItinerary(null,newItinerary,0);
          patterns.add(n);
//          LOG.info(newItinerary +"<n->\n" +toPlainText("",n)); 
        }
    }
        
    @Override
    public PatternNode getCurrentNode(Itinerary currentItinerary){
                            
        AreaOfInterest currentArea = currentItinerary.getAllNamedAreas().get(0);

        for(PatternNode node : patterns){
          if(node.equals(currentArea)){
              if(1 < currentItinerary.getNumOfAllNamedAreas()){
                return getCurrentNode(currentItinerary,1,node);
              }else{
                  return node;
              }
          }
        }
        
        return null;
    }
    
    private PatternNode getCurrentNode(
            Itinerary itinerary, 
            int index,
            PatternNode node){
           
        
        if(node.getSons() != null){

            AreaOfInterest itArea = itinerary.getAllNamedAreas().get(index);

            for(PatternNode son : node.getSons()){
                if(son.getElement().equals(itArea)){
                    index++;
                    if(index < itinerary.getNumOfAllNamedAreas()){
                        return getCurrentNode(itinerary,index,son);
                    }else{
                        return son;
                    }
                }
            }
        }
                
        return null;
                
    }

    
    private void insertItineraryInNode(Itinerary newItinerary, PatternNode node){
        
        PatternNode first = node;

        AreaOfInterest destination = newItinerary.getFinalDestination();
        
        int i =1;
        for(; i< newItinerary.getNumOfAllNamedAreas(); i++){
            AreaOfInterest itArea = newItinerary.getAllNamedAreas().get(i);
            
            PatternNode nextNode = null;
            for(PatternNode son : node.getSons()){
                if(son.getElement().equals(itArea)){
                    
                    if(ItAreaOfInterestType.START_END.equals(destination.getType()) && !son.getElement().equals(destination)){
                        int n = 1;
                        if(son.getDestinations().containsKey(destination)){
                            n = n+ son.getDestinations().get(destination);                            
                        }
                        son.getDestinations().put(destination, n);
                    }
                    nextNode = son;
                    break;
                }
            }
            
            if(nextNode != null){
                node.setNumRepetitions(node.getNumRepetitions()+1);
                node = nextNode;
            }else{
               extendNodeWithItinerary(node,newItinerary,i);
               break;
            }            
        }
        
        if(i >= newItinerary.getNumOfAllNamedAreas()){
            node.setNumRepetitions(node.getNumRepetitions()+1);
        }
        
        patterns.add(first);
        
    }
    
    private PatternNode extendNodeWithItinerary(
            PatternNode node, 
            Itinerary itinerary, 
            int index){
        
        PatternNode currentNode = node;
        if(currentNode == null){
            currentNode = new PatternNode(itinerary.getAllNamedAreas().get(index++));           
        }
        AreaOfInterest destination = itinerary.getFinalDestination();
        
        PatternNode firstNode = currentNode;
        firstNode.setNumRepetitions(firstNode.getNumRepetitions()+1);
        
        for(;index < itinerary.getNumOfAllNamedAreas();index++){
            AreaOfInterest currentArea = itinerary.getAllNamedAreas().get(index);
            PatternNode newSon = new PatternNode(currentArea);
            newSon.setNumRepetitions(1);
            if((ItAreaOfInterestType.CELL.equals(destination.getType()) || 
                    PlaceType.LANDMARK.equals(destination.getPlace().getType())) && 
                    !currentArea.equals(destination)){
                newSon.getDestinations().put(destination, 1);
            }
            currentNode.getSons().add(newSon);
            currentNode = newSon;
        }       

        return firstNode;
    }
    
    private String toPlainText(String prefix, PatternNode node){
        
        StringBuilder sb = new StringBuilder();        
        String newPrefix = prefix + node;
        
        if(node.getSons().isEmpty()){
            sb.append(newPrefix);
        }else{
            List<PatternNode> sonsList = new ArrayList(node.getSons());
            Collections.sort(sonsList);
            
            for(PatternNode son : sonsList){
              sb.append(toPlainText(newPrefix,son));
            }     
        }
        
        return sb.toString();
    }
    
    
    @Override
    public String toString(){
        StringBuilder sb= new StringBuilder();
        
        List<PatternNode> patternsList = new ArrayList(patterns);
        Collections.sort(patternsList);
        
        for(PatternNode node : patternsList){
            sb.append(toPlainText("",node));
        }
        
        return sb.toString();
    }
    
}
