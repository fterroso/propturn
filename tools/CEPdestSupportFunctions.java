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
package cepdest.tools;

import cepdest.area.AreaOfInterest;
import ceptraj.config.ConfigProvider;
import ceptraj.config.ConfigProvider.SpaceType;
import ceptraj.tool.Bearing;
import ceptraj.tool.Point;
import lda.location.Location;

/**
 * Some common methods widely used by different CEP-dest classes.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class CEPdestSupportFunctions {
    
    
    /**
     * Bridge function that maps a CEP-traj point to a LDA location.
     *  
     * @param p CEP-traj point to be mapped     
     * @return LDA location
     */
    public static Location toLDALocation(Point p){
//        System.out.println(p.getLat()+" "+ p.getLon() + " cart:"+p.getX()+ " "+p.getY());
        Location l = null;
        
        switch(ConfigProvider.getSpaceType()){
            case cartesian:
                l = new Location(p.getY(), p.getX());
                break;
            default: //lat_lon
                l = new Location(p.getLat(), p.getLon());
                break;
        }
                
        l.setTimestamp(p.getTimestamp());        
        
        return l;
    }
    
    /**
     * Bridge function that maps a LDA locaton to a CEP-traj point
     * @param l LDA location
     * @return CEPtraj point
     */
    public static Point toCEPtrajPoint(Location l){
        Point p = new Point(l.getY(), l.getX());
        if(SpaceType.cartesian.equals(ConfigProvider.getSpaceType())){
            p.setX(l.getX());
            p.setY(l.getY());
        }
        
        p.setTimestamp(l.getTimestamp());
        
        return p;
    }
    
    public static double bearingBetweenAreas(
            AreaOfInterest a1, 
            AreaOfInterest a2){
        Location l1 = a1.getMeaningfulLocation();
        Location l2 = a2.getMeaningfulLocation();
        
        return  Bearing.bearing(CEPdestSupportFunctions.toCEPtrajPoint(l1), CEPdestSupportFunctions.toCEPtrajPoint(l2));
    }
    
}
