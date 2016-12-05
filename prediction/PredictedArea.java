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
import java.text.DecimalFormat;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class PredictedArea implements Comparable{
    
    AreaOfInterest area;
    double weight;

    public PredictedArea(AreaOfInterest area, double weight) {
        this.area = area;
        this.weight = weight;
    }
    
    public AreaOfInterest getArea() {
        return area;
    }

    public void setArea(AreaOfInterest area) {
        this.area = area;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(Object o) {
        PredictedArea a = (PredictedArea) o;
        
        Double d = new Double(weight);
        return d.compareTo(new Double(a.weight));
        
    }
    
    @Override
    public String toString(){
        
        DecimalFormat twoDForm = new DecimalFormat("#.##");

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(area);
        sb.append(", ");
        sb.append((twoDForm.format(weight).replace(",", ".")));       
        sb.append("}");
        
        return sb.toString();
    }
    
    
}
