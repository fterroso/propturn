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
package cepdest.stats;

/**
 *
 * @author fterroso
 */
public class PredictionStats{
        
        long initialTimestamp = 0; //only for ProPTurn predictions
        long timestamp;
        double weight;
        double distError;
        int distLength;

    public long getInitialTimestamp() {
        return initialTimestamp;
    }

    public void setInitialTimestamp(long initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }    
        
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getDistError() {
        return distError;
    }

    public void setDistError(double distError) {
        this.distError = distError;
    }

    public int getDistLength() {
        return distLength;
    }

    public void setDistLength(int distLength) {
        this.distLength = distLength;
    }

    @Override
    public String toString() {
        return "{" + "timestamp=" + timestamp + ", weight=" + weight + ", distError=" + distError + ", distLength=" + distLength + '}';
    }                     
        
}