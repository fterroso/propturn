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
import cepdest.area.TurningArea;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Basic node for the patterns used by the simple markovian process.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class PatternNode implements Comparable, Serializable {
    
    static Logger LOG = Logger.getLogger(PatternNode.class);
    
    AreaOfInterest element;
    Set<PatternNode> sons = new HashSet<PatternNode>();
        
    int numRepetitions;
    Map<AreaOfInterest,Integer> destinations = new HashMap<AreaOfInterest,Integer>();

    public PatternNode(AreaOfInterest element) {
        this.element = element;
    }

    public int getNumRepetitions() {
        return numRepetitions;
    }

    public void setNumRepetitions(int numRepetitions) {
        this.numRepetitions = numRepetitions;
    }

    public Map<AreaOfInterest, Integer> getDestinations() {
        return destinations;
    }

    public void setDestinations(Map<AreaOfInterest, Integer> destinations) {
        this.destinations = destinations;
    }
    
    public AreaOfInterest getElement() {
        return element;
    }

    public void setElement(AreaOfInterest element) {
        this.element = element;
    }

    public Set<PatternNode> getSons() {
        return sons;
    }

    public void setSons(Set<PatternNode> sons) {
        this.sons = sons;
    }
    
    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
           if(obj.getClass() == AreaOfInterest.class || obj.getClass() == TurningArea.class){
                 AreaOfInterest  a = (AreaOfInterest) obj;
                 return a.getID().equals(element.getID());
             } 
           return false;
        }
        final PatternNode other = (PatternNode) obj;
        if (this.element != other.element && (this.element == null || !this.element.equals(other.element))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString(){
        StringBuilder sb= new StringBuilder();
        sb.append("[");
        sb.append(element);
        sb.append(",");
        sb.append(numRepetitions);
        
        if(!destinations.isEmpty()){
            sb.append(",");        
            sb.append("{");
            for(AreaOfInterest destination : destinations.keySet()){
                sb.append(destination);
                sb.append(":");
                sb.append(destinations.get(destination));
                sb.append(",");
            }
            sb.append("}");
        }
        
        sb.append("] ");

        if(sons.isEmpty()){
            sb.append("\n");
        }
        
        return sb.toString();
    }

    @Override
    public int compareTo(Object t) {
        PatternNode n = (PatternNode)t;
        int c = element.compareTo(n.getElement());
        if(c!=0){
            return c;
        }else{
            return new Integer(numRepetitions).compareTo(new Integer(n.getNumRepetitions()));
        }
    }
    
}
