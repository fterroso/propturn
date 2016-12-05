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
package cepdest.CEP.EPA;

import cepdest.CEP.consumer.LDAEventConsumer;
import cepdest.CEP.event.BearingChangeClusterEvent;
import ceptraj.event.trajectory.change.BearingTrajectoryChangeEvent;
import ceptraj.tool.Point;
import org.apache.log4j.Logger;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class MicroClusterHolder {
    
    static Logger LOG = Logger.getLogger(TrajectoryMicroClusterEPA.class);
    
    private static BearingChangeClusterEvent microCluster;// = new BearingChangeClusterEvent();
    private static LDAEventConsumer microClusterConsumer;
    
    //for stats
    public static int numReceivedTPs = 0;
    public static int numOfDeliveredMicroClusters = 0;


    public static void setMicroClusterConsumer(LDAEventConsumer microClusterConsumer) {
        MicroClusterHolder.microClusterConsumer = microClusterConsumer;
    }
    
    public static void updateMicroCluster(BearingTrajectoryChangeEvent btce){
        microCluster.update(btce);
        numReceivedTPs++;
    }
    
    public static Point getMicroClusterCentroid(){
        return microCluster.getCentroid();
    }
    
    public static long getMicroClusterTimestamp(){
        return microCluster.getTimestamp();
    }
    
    public static Point getMicroClusterLastPoint(){
        return microCluster.getLastPoint();
    }
    
    public static void setFirstEvent(BearingTrajectoryChangeEvent btce){
        try{
            microCluster = new BearingChangeClusterEvent(btce);
            numReceivedTPs++;
        }catch(Exception e){
            LOG.error("Error in setFirstEvent ",e);
        }
    }
    
    public static void deliverCluster(){
        if(!microCluster.isDelivered()){
            microClusterConsumer.processBearingChangeClusterEvent(microCluster);
            microCluster.setDelivered(true);
            numOfDeliveredMicroClusters++;
        }
    }
    
}
