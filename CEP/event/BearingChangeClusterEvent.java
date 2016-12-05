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
package cepdest.CEP.event;

import ceptraj.event.trajectory.change.BearingTrajectoryChangeEvent;
import ceptraj.tool.Point;
import ceptraj.tool.supportFunction.LocationFunction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class BearingChangeClusterEvent{
    
    private Point centroid = null;
    String id;
    List<Long> timestamps = new ArrayList<Long>();
    List<Point> points = new ArrayList<Point>();
    
    boolean delivered = false;
    
    BearingTrajectoryChangeEvent firstEvent; 
    BearingTrajectoryChangeEvent lastEvent; 

    public BearingChangeClusterEvent(BearingTrajectoryChangeEvent firstEvent) {
        this.firstEvent = firstEvent;
        update(firstEvent);
    }

    public Point getCentroid() {
        return centroid;
    }

    public BearingTrajectoryChangeEvent getFirstEvent() {
        return firstEvent;
    }

    public BearingTrajectoryChangeEvent getLastEvent() {
        return lastEvent;
    }
    
    
    public int getNumPoints(){
        return points.size();
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
    
    public void addNewPoint(Point p){
        points.add(p);  
        centroid = LocationFunction.getCentroidFromPoints(points);
    }
    
    public void addNewTimestamp(long t){
        timestamps.add(t);
    }
    
    public long getTimestamp(){
        return timestamps.get(timestamps.size()-1);
    }
    
    public Point getLastPoint(){
        return points.get(points.size()-1);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public void update(BearingTrajectoryChangeEvent btce){
        lastEvent= btce;
        addNewPoint(btce.getMiddlePoint());
        addNewTimestamp(btce.getFinalTimestamp());
        setId(btce.getMovingObjId());
    }
    
    @Override
    public String toString(){

        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        
        StringBuilder sb = new StringBuilder();

        sb.append("BCC");
        sb.append("[");
        sb.append("len:");
        sb.append(points.size());
        sb.append(",id:");
        sb.append(getId());
        sb.append(",first:");
        sb.append(getFirstEvent());
//        sb.append(", ");
//        sb.append(format.format(timestamp));
        sb.append(",last:");
        sb.append(getLastEvent());
        sb.append(", {");
        for(long t : timestamps){
           sb.append(format.format(t));
           sb.append(","); 
        }
        sb.append("}]");
                
        return sb.toString();
    } 
    
}
