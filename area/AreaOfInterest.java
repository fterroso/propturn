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

import java.io.Serializable;
import lda.config.LDAConfigProviderFactory;
import lda.landmark.LandmarkProxy;
import lda.location.Location;
import lda.place.Place;
import lda.place.PlaceType;
import static lda.place.PlaceType.LANDMARK;
import static lda.place.PlaceType.POINT;
import lda.tools.SupportFunctions;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class AreaOfInterest implements Comparable, Serializable{
    
    ItAreaOfInterestType type;
    Place place = null;
    long timestamp;
    
    double lengthFromOrigin;
    
    String ID = null;
    
    boolean isNew = false;

    public AreaOfInterest(Place place, ItAreaOfInterestType type) {
        this.place = place;
        this.type = type;
    }
    
    public AreaOfInterest(){}
    
    public ItAreaOfInterestType getType() {
        return type;
    }
    
    public PlaceType getPlaceType(){
        return place.getType();
    }

    public void setType(ItAreaOfInterestType type) {
        this.type = type;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isNamed(){
        return ItAreaOfInterestType.CELL.equals(getType()) || 
                    PlaceType.LANDMARK.equals(getPlace().getType());
    }
    
    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
    
    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID(){
        
        if(ID==null){
            StringBuilder sb = new StringBuilder();
            sb.append(type.getAbr());
            sb.append("_");
            if(PlaceType.LANDMARK.equals(place.getType())){
                sb.append(place);
            }else{
                sb.append("\u2205"); // Empty symbol.
            }
             ID = sb.toString();
        }
        
        return ID;
    }

    public double getLengthFromOrigin() {
        return lengthFromOrigin;
    }

    public void setLengthFromOrigin(double lengthFromOrigin) {
        this.lengthFromOrigin = lengthFromOrigin;
    }
    
    public Location getMeaningfulLocation(){
            
        Location l1 = null;
        switch(getType()){
            case CELL:
                l1 = (Location) place;
            default:
                if(place != null){

                    if(PlaceType.LANDMARK.equals(place.getType())){
                        l1 = ((LandmarkProxy)place).getMiddlePoint();
                    }else{
                        l1 = (Location)place;
                    }
                }
                break;
        }
        return l1;
            
    }
    
    public double getDistanceWithArea(AreaOfInterest area){
        if(place != null && area != null){
            Location l1;
            Location l2;
            if(PlaceType.LANDMARK.equals(place.getType())){
                l1 = ((LandmarkProxy)place).getMiddlePoint();
            }else{
                l1 = (Location)place;
            }
            
            if(PlaceType.LANDMARK.equals(area.getPlace().getType())){
                l2 = ((LandmarkProxy)area.getPlace()).getMiddlePoint();
            }else{
                l2 = (Location)area.getPlace();
            }
            
            return Math.sqrt(Math.pow(l1.getX()-l2.getX(),2)+Math.pow(l1.getY()-l2.getY(), 2));
        }
        
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public int hashCode(){
        int hash = type.hashCode();
        if(place!= null){
            hash += place.hashCode();
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        AreaOfInterest area = (AreaOfInterest)obj; 
        
        if(ItAreaOfInterestType.CELL.equals(getType())){
            if(ItAreaOfInterestType.CELL.equals(area.getType())){
                if(getID().equals(area.getID())){
                    return true;
                }                
            }            
            return false;
        }        
           
        switch(place.getType()){
            case LANDMARK:   
                LandmarkProxy l = (LandmarkProxy)place;
                switch(area.getPlace().getType()){
                    case LANDMARK:
                        LandmarkProxy l2 = (LandmarkProxy)area.getPlace();
                        if(!l.getID().equals(l2.getID())){
                            return false;
                        }
                        break;
                    case POINT:
                        if(!l.contains((Location)area.getPlace())){
                            return false;
                        }
                        break;                        
                }
                break;
            case POINT:
                switch(area.getPlace().getType()){
                    case LANDMARK:
                        l = (LandmarkProxy)area.getPlace();
                        if(!l.contains((Location)place)){
                            return false;
                        }
                        break;
                    case POINT:
                       Location l1 = (Location) place;
                       Location l2 = (Location)area.getPlace();
                       float maxDist = LDAConfigProviderFactory.getCurrentConfigProvider().getClusterTypes().get(0).getRadius();
                       
                       float dist = SupportFunctions.dist(l1.getX(), l1.getY(), l2.getX(), l2.getY());
                       if(dist <= maxDist){
                           return false;
                       }
                        break;                        
                }
                break;
        }
        
        return true;        
    }
    
    @Override
    public String toString(){       
                
        return getID();
    }

    @Override
    public int compareTo(Object t) {
        AreaOfInterest a = (AreaOfInterest) t;
        switch(getType()){
            case CELL:
                return getID().compareTo(a.getID());
            default:
                return place.getID().compareTo(a.getPlace().getID());
                                        
        }
        
    }

}
