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

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores a particular prediction made by the system.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class Prediction {
    
    List<PredictedArea> predictedNextAreas = new ArrayList<PredictedArea>();    
    List<PredictedArea> predictedDestination = new ArrayList<PredictedArea>();
    
    double lengthOfUsedChain;
    
    long time; // in milliseconds

    public List<PredictedArea> getPredictedNextAreas() {
        return predictedNextAreas;
    }

    public void setPredictedNextAreas(List<PredictedArea> predictedNextAreas) {
        this.predictedNextAreas = predictedNextAreas;
    }

    public List<PredictedArea> getPredictedDestination() {
        return predictedDestination;
    }

    public void setPredictedDestination(List<PredictedArea> predictedDestination) {
        this.predictedDestination = predictedDestination;
    }

    public double getLengthOfUsedChain() {
        return lengthOfUsedChain;
    }

    public void setLengthOfUsedChain(double lengthOfUsedChain) {
        this.lengthOfUsedChain = lengthOfUsedChain;
    }
    
    public double getNextAreaWeight(){
        double w = 0;
        for(PredictedArea pa : predictedNextAreas){
            if(pa.getWeight() > w){
                w = pa.getWeight();
            }
        }
        
        return w;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
    
    public boolean isEmpty(){
        return predictedNextAreas.isEmpty() && predictedDestination.isEmpty();
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("[areas:{");
        for(PredictedArea area : predictedNextAreas){
            sb.append(area);
            sb.append(",");
        }
       sb.append("}");
       
        if(predictedDestination.size()>0){
            sb.append(", dest:{"); 
            
            for(PredictedArea area : predictedDestination){
                sb.append(area);
                sb.append(", ");
            }
            sb.append("}");
        }
        
         sb.append("]");
                
        return sb.toString();
    }
}
