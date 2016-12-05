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
package cepdest.area;

import ceptraj.tool.Bearing;
import lda.place.PlaceType;
import org.apache.log4j.Logger;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class TurningArea extends AreaOfInterest {

    static Logger LOG = Logger.getLogger(TurningArea.class);
    
    double initialBearingVal;
    double finalBearingVal;
    
    
    public TurningArea() {
        type = ItAreaOfInterestType.TURNING;
    }

    public Bearing getInitialBearing() {
        return Bearing.getCardinalPointFromValue(initialBearingVal);
    }

    public void setInitialBearingVal(Double initialBearing) {
        this.initialBearingVal = initialBearing;
    }

    public Bearing getFinalBearing() {
        return Bearing.getCardinalPointFromValue(finalBearingVal);
    }

    public void setFinalBearingVal(double finalBearingVal) {
        this.finalBearingVal = finalBearingVal;
    }

    public double getInitialBearingVal() {
        return initialBearingVal;
    }

    public double getFinalBearingVal() {
        return finalBearingVal;
    } 
    
    @Override
    public String getID(){
        
        StringBuilder sb = new StringBuilder();
        
        if(PlaceType.LANDMARK.equals(place.getType())){
            sb.append(place);
        }else{
            sb.append("\u2205"); // Empty symbol.
        }
        
        return sb.toString();
    }
    
    @Override
    public int hashCode(){
                
        int hash = super.hashCode();
        
        return hash;      
        
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if(!super.equals(obj)){
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;

    }      
}
