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
 * 
 */
package cepdest.pattern;

import cepdest.area.AreaOfInterest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class GroupOfEnhancedPatternNode {

    Set<EnhancedPatternNode> nodes;
    
    public GroupOfEnhancedPatternNode(Set<EnhancedPatternNode> nodes) {
        this.nodes = nodes;
    }
    
    public AreaOfInterest getElement() {
        AreaOfInterest a=null;
        int nameLength = Integer.MIN_VALUE;
        for(EnhancedPatternNode node : nodes){
            if(node.getElement().getID().length()> nameLength){
                nameLength = node.getElement().getID().length();
                a = node.getElement();
            }
        }
        return a;
    }

    
    public List<String> getPatternIdsForSon(String sonId) {        
        Set<String> patternIds = new HashSet<String>();
        for(EnhancedPatternNode node : nodes){
            if(node.getPatternIdsForSon(sonId)!=null){
                patternIds.addAll(node.getPatternIdsForSon(sonId));
            }
        }
        
        List<String> result = new ArrayList();
        result.addAll(patternIds);        
        return result;
    }

    public Set<String> getOutboundPatternIds() {        
        Set<String> patternIds = new HashSet<String>();
        for(EnhancedPatternNode node : nodes){
            if(node.getOutboundPatternIds()!=null){
                patternIds.addAll(node.getOutboundPatternIds());
            }
        }
        
        return patternIds;
    }
    
    public List<Integer> getPositionsForPatternId(String patternId){
        for(EnhancedPatternNode node : nodes){
            if(node.getPositionsForPatternId(patternId)!= null){
                return node.getPositionsForPatternId(patternId);
            }
        }
        return null;
    }

    public int getNumRepetitionsForPatternIds(List<String> ids) {
        int nRep = 0;
        for(EnhancedPatternNode node : nodes){
            nRep += node.getNumRepetitionsForPatternIds(ids);
        }
        
        return nRep;
    }

    public List<String> getSonsForPatternIds(List<String> patternIds){
        Set<String> sons = new HashSet<String>();
        for(EnhancedPatternNode node : nodes){
            if(node.getSonsForPatternIds(patternIds)!=null){
                sons.addAll(node.getSonsForPatternIds(patternIds));
            }
        }        
        List<String> result = new ArrayList();
        result.addAll(sons);        
        return result;
    }

    public Set<String> getSonIds(){
        Set<String> sons = new HashSet<String>();
        for(EnhancedPatternNode node : nodes){
            if(node.getSonIds()!=null){
                sons.addAll(node.getSonIds());
            }
        }   
        return sons;
    }

    public Map<AreaOfInterest, Integer> getDestinations(List<String> patternIds) {
        Map<AreaOfInterest, Integer> destCandidates = new HashMap<AreaOfInterest, Integer>();
        for(EnhancedPatternNode node : nodes){
            Map<AreaOfInterest, Integer> destCandidatesForNode = node.getDestinations(patternIds);        
            if(destCandidatesForNode!=null){
                for(AreaOfInterest a : destCandidatesForNode.keySet()){
                    int n = (destCandidates.containsKey(a)) ? destCandidates.get(a) : 0;
                    n += destCandidatesForNode.get(a);
                    destCandidates.put(a, n);
                }
            }
        }
        return destCandidates;
    }

    public Map<AreaOfInterest, Integer> getAllDestinations() {
        Map<AreaOfInterest, Integer> destCandidates = new HashMap<AreaOfInterest, Integer>();
        for(EnhancedPatternNode node : nodes){
            Map<AreaOfInterest, Integer> destCandidatesForNode = node.getAllDestinations();        
            if(destCandidatesForNode!=null){
                for(AreaOfInterest a : destCandidatesForNode.keySet()){
                    int n = (destCandidates.containsKey(a)) ? destCandidates.get(a) : 0;
                    n += destCandidatesForNode.get(a);
                    destCandidates.put(a, n);
                }
            }
        }
        return destCandidates;
    }
}
