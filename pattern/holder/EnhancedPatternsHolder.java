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
import cepdest.config.ConfigProvider;
import cepdest.config.ConfigProviderFactory;
import cepdest.itinerary.Itinerary;
import cepdest.pattern.EnhancedPatternNode;
import cepdest.pattern.GroupOfEnhancedPatternNode;
import cepdest.pattern.PatternNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lda.place.PlaceType;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Class the implements the new approach to store the itinerary patterns.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class EnhancedPatternsHolder implements PatternsHolder, Serializable{

    static Logger LOG = Logger.getLogger(EnhancedPatternsHolder.class);
    Set<EnhancedPatternNode> nodes = new HashSet<EnhancedPatternNode>();
        
    @Override
    public void insertNewItinerary(Itinerary it) {
        
        if(it.getNumOfAllNamedAreas() > 1){
        
            String patternId = null;
            
            AreaOfInterest a1 = it.getAllNamedAreas().get(it.getNumOfAllNamedAreas()-2);
            AreaOfInterest a2 = it.getAllNamedAreas().get(it.getNumOfAllNamedAreas()-1);            
            
            EnhancedPatternNode node1 = getNodeWithArea(a1);
            EnhancedPatternNode lastNode = getNodeWithArea(a2);
            
            Set<String> outPatternIds = lastNode.getPatternIds();
            
            List<String> prevPatternIds = node1.getPatternIdsForSon(a2.getID()); 
            
            if(prevPatternIds != null){
                
                if(outPatternIds!=null){
                    prevPatternIds.removeAll(outPatternIds);
                }
                int init = it.getNumOfAllNamedAreas()-2;
                a2 = a1;
                for(int i=init; i> 0; i--){

                    a1 = it.getAllNamedAreas().get(i-1);
                    EnhancedPatternNode node = getNodeWithArea(a1);

                    List<String>patternIds = node.getPatternIdsForSon(a2.getID());

                    if(patternIds == null){
                        break;
                    }

                    prevPatternIds.retainAll(patternIds);
                    if(prevPatternIds.size() == 1){
                        patternId = prevPatternIds.get(0);
                        break;
                    }
                    a2 = a1;
                }
            }
            
            if(patternId == null){
                a1 = it.getAllNamedAreas().get(0);
                node1 = getNodeWithArea(a1);
                patternId = node1.createNewOwnPatternId();
            }
            
            insertNewItineraryWithPatternId(it, patternId);              
            
        }
    } 
    
    public List<String> getPatternIdForItinerary(Itinerary it){
        
        List<String> finalPatternIds = null; 
        double chainLength = 1;

        ConfigProvider config = ConfigProviderFactory.getConfig();
        
        switch(config.getPredictorConfig().getCurrentIterationMode()){
            case BACKWARDS:
                AreaOfInterest a = it.getAllNamedAreas().get(it.getNumOfAllNamedAreas()-1);
                GroupOfEnhancedPatternNode node = getNodesWithArea(a);

                finalPatternIds = new ArrayList(node.getOutboundPatternIds());          

                int lastIndex = it.getNumOfAllNamedAreas()-1;

                for(int i= lastIndex; i>= 1; i--){
                    AreaOfInterest a1 = it.getAllNamedAreas().get(i);
                    AreaOfInterest a2 = it.getAllNamedAreas().get(i-1);

                    GroupOfEnhancedPatternNode node2 = getNodesWithArea(a2);


                    List<String> cPatternIds =  node2.getPatternIdsForSon(a1.getID());

                    if(cPatternIds != null){
                        List<String> aux = new ArrayList(finalPatternIds);
                        finalPatternIds.retainAll(cPatternIds);
                        if(finalPatternIds.isEmpty()){
                            finalPatternIds = aux;
                            break;
                        }else{
                            for(String patternId : finalPatternIds){
                                List<Integer> pos1 = node.getPositionsForPatternId(patternId);
                                List<Integer> pos2 = node2.getPositionsForPatternId(patternId);
                                if(!compatiblePositions(pos1,pos2)){
                                    finalPatternIds = aux;
                                    break;
                                }
                            }
                        }
                    }else{
                        break;
                    }

                    chainLength++;
                    node = node2;

                }
                break;
            case FULL_FORWARD:
                int i = 0;
                while(i< it.getNumOfAllNamedAreas()-1){
                    AreaOfInterest a1 = it.getAllNamedAreas().get(i);
                    AreaOfInterest a2 = it.getAllNamedAreas().get(i+1);

                    node = getNodesWithArea(a1);
                    List<String> cPatternIds =  node.getPatternIdsForSon(a2.getID());
                    if(finalPatternIds != null){
                        if(cPatternIds != null){
                            finalPatternIds.retainAll(cPatternIds);
                            chainLength++;
                        }else{
                            finalPatternIds = null;
                        }
                    }else if(cPatternIds != null && !cPatternIds.isEmpty()){
                        finalPatternIds = new ArrayList<String>(cPatternIds);
                        chainLength = 2;
                    } 
                    i++;
                }                
                break;
            case FORWARD:  
                for(i= 0; i< it.getNumOfOldNamedAreas()-1; i++){
                    AreaOfInterest a1 = it.getOldNamedAreas().get(i);
                    AreaOfInterest a2 = it.getOldNamedAreas().get(i+1);
                   
                    node = getNodesWithArea(a1);
                    List<String> cPatternIds =  node.getPatternIdsForSon(a2.getID());
                    
                    if(finalPatternIds != null){
                        if(cPatternIds != null){
                            finalPatternIds.retainAll(cPatternIds);
                            chainLength++;
                        }else{
                            AreaOfInterest iArea = it.getOldNamedAreas().get(0);
                            AreaOfInterest fArea = it.getOldNamedAreas().get(it.getNumOfOldNamedAreas()-1);

                             EnhancedPatternNode iNode = getNodeWithArea(iArea);
                             EnhancedPatternNode fNode = getNodeWithArea(fArea);

                             finalPatternIds= new ArrayList(iNode.getPatternIds());
                             finalPatternIds.retainAll(fNode.getPatternIds());
                             chainLength = 1;
                            break;
                        }
                    }else if(cPatternIds != null && !cPatternIds.isEmpty()){
                        finalPatternIds = new ArrayList<String>(cPatternIds);
                        chainLength = 2;
                    } 
                }
                
            break;                
        }
        
        if(finalPatternIds != null){
            finalPatternIds.add(String.valueOf(chainLength/it.getNumOfAllNamedAreas()));
        }
        return finalPatternIds;
    }
    
    private boolean compatiblePositions(List<Integer> pos1, List<Integer>pos2){
        for(int i : pos1){
            for(int j : pos2){
                if(Math.abs(i-j)==1) return true;
            }
        }
        
        return false;
    }
    
    public GroupOfEnhancedPatternNode getCurrentNodes(Itinerary currentItinerary){
        Set<EnhancedPatternNode> rNodes = new HashSet<EnhancedPatternNode>();
        AreaOfInterest a = currentItinerary.getLastNamedArea();
        String[] ids = a.getID().split("-");
        
        for(EnhancedPatternNode n : nodes){   
            for(String id : ids){
                String extendedId = "-"+id+"-";
                if(id.equals(n.getElement().getID()) || n.getElement().getID().contains(extendedId)){
                    rNodes.add(n);
                }
            }
        }
        return new GroupOfEnhancedPatternNode(rNodes);
    }

    @Override
    public PatternNode getCurrentNode(Itinerary currentItinerary) {
        AreaOfInterest a = currentItinerary.getLastNamedArea();//.getAllNamedAreas().get(currentItinerary.getNumOfAllNamedAreas()-1);
        for(EnhancedPatternNode n : nodes){           
            if(n.equals(a)){
                return n;
            }
        }
        return null;
        
    }
    
    public EnhancedPatternNode getNodeWithId(String id){
        EnhancedPatternNode resultingNode = null;
        for(EnhancedPatternNode node : nodes){
            if(node.getElement().getID().equals(id)){
                resultingNode = node;
                break;
            }
        }
        
        return resultingNode;
    }
    
     @Override
    public String toString(){        
         
        List<EnhancedPatternNode> nodeList = new LinkedList(nodes);        
        Collections.sort(nodeList);
        
        StringBuilder sb = new StringBuilder();
        
        for(EnhancedPatternNode node : nodeList){
            for(String patternId : node.getOwnPatternIds()){
                sb.append("<");
                sb.append(patternId);
                sb.append(">");
                sb.append(toPlainText("",node,patternId));
            }
        }
        
        return sb.toString();
    }
     
    private String toPlainText(String prefix, EnhancedPatternNode node, String patternId){
        
        StringBuilder sb = new StringBuilder();        
        String newPrefix = prefix + node.toString(patternId);
        
        List<String> sons = node.getSonsForPatternId(patternId);
        if(sons == null || sons.isEmpty()){
            sb.append(newPrefix);
            sb.append("\n");
        }else{
            int index = StringUtils.countMatches(newPrefix, node.toString(patternId));
            if(index <= sons.size()){
                sb.append(toPlainText(newPrefix,getNodeFromId(sons.get(index-1)),patternId));
            }else{
                sb.append(newPrefix);
                sb.append("\n");
            }                
        }
        return sb.toString();
    }     
    
    private GroupOfEnhancedPatternNode getNodesWithArea(AreaOfInterest area){
        Set<EnhancedPatternNode> resultingNodes = new HashSet<EnhancedPatternNode>();
        
        String[] ids = area.getID().split("-");
        for(EnhancedPatternNode node : nodes){
            for(String id : ids){
                if(id.equals(node.getElement().getID()) || node.getElement().getID().contains(id)){
                    resultingNodes.add(node);
                    break;
                }
            }
        }
        
        if(resultingNodes.isEmpty()){
            EnhancedPatternNode resultingNode = createNodeFromArea(area);
            resultingNodes.add(resultingNode);
        }
        
        return new GroupOfEnhancedPatternNode(resultingNodes);
    }
    
    private EnhancedPatternNode getNodeWithArea(AreaOfInterest area){
       EnhancedPatternNode resultingNode = null;
        for(EnhancedPatternNode node : nodes){
            if(node.equals(area)){
                resultingNode = node;
                break;
            }
        }
        if(resultingNode == null){
            resultingNode = createNodeFromArea(area);
        }
        
        return resultingNode;
    }
    
    private void insertNewItineraryWithPatternId(
            Itinerary it, 
            String patternId){
                
        AreaOfInterest destination = it.getFinalDestination();
        boolean namedDestination = ItAreaOfInterestType.START_END.equals(destination.getType()) && PlaceType.LANDMARK.equals(destination.getPlace().getType());
        
        for(int i =0; i< it.getNumOfAllNamedAreas()-1; i++){
            AreaOfInterest area = it.getAllNamedAreas().get(i);
            EnhancedPatternNode node = getNodeWithArea(area);
            node.addPatternId(patternId, i, it.getAllNamedAreas().get(i+1).getID());
            if(namedDestination){
                node.setDestination(patternId, destination);
            }
        } 
        
        EnhancedPatternNode node = getNodeWithArea(destination);
        node.incNumRepetitions(patternId);
        node.setPositionForPattern(patternId, it.getNumOfAllNamedAreas()-1);
    }
    
    private EnhancedPatternNode createNodeFromArea(AreaOfInterest area){
        EnhancedPatternNode node = new EnhancedPatternNode(area);
        nodes.add(node);
        return node;
    }
    
    private EnhancedPatternNode getNodeFromId(String nodeID){
        for(EnhancedPatternNode node : nodes){
            if(node.getElement().getID().equals(nodeID)){
                return node;
            }
        }
        return null;
    }
    
}
