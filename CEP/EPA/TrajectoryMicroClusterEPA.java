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

import ceptraj.EPA.EPA;
import ceptraj.output.EventConsumer;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class TrajectoryMicroClusterEPA extends EPA{
    
    private static final String CREATE_FIRST_MICRO_CLUSTER_EPL =
            "@Priority(11) "+
            "SELECT "+
            "   setFirstEvent(A) "+
            "FROM pattern[every-distinct(A.movingObjId) A=BearingTrajectoryChangeEvent]";
    
    private static final String CREATE_NEW_MICRO_CLUSTER_BY_TIME = 
            "SELECT"+
            "   setFirstEvent(btce) "+
            "FROM BearingTrajectoryChangeEvent btce "+
            "WHERE btce.initialTimestamp - getMicroClusterTimestamp() > maxClusterTime";
    
    private static final String CREATE_NEW_MICRO_CLUSTER_BY_DISTANCE_EPL = 
            "SELECT "+
            "   setFirstEvent(btce) "+
            "FROM BearingTrajectoryChangeEvent btce "+
            "WHERE btce.initialTimestamp - getMicroClusterTimestamp() <= maxClusterTime and "+
            "euclideanDist(getMicroClusterCentroid(),btce.middlePoint) > maxClusterDist and "+
            "euclideanDist(getMicroClusterLastPoint(),btce.middlePoint) > maxClusterDist ";
    
    private static final String UPDATE_MICRO_CLUSTER_EPL = 
            "SELECT "+
            "   updateMicroCluster(btce) "+
            "FROM BearingTrajectoryChangeEvent btce "+
            "WHERE btce.initialTimestamp - getMicroClusterTimestamp() <= maxClusterTime and "+
            "(euclideanDist(getMicroClusterCentroid(),btce.middlePoint) <= maxClusterDist or "+
            "euclideanDist(getMicroClusterLastPoint(),btce.middlePoint) <= maxClusterDist) ";
    
    private static final String DELIVER_MICRO_CLUSTER_EPL =
            " SELECT "+
            "   deliverCluster() "+
            "FROM pattern [every btce=BearingTrajectoryChangeEvent -> (timer:interval(10 sec) and not BearingTrajectoryChangeEvent(id=btce.id))]";
            
    
    
    EPStatement firstMicroClusterSt;
    EPStatement createNewMicroClusterByTime;
    EPStatement createNewMicroClusterByDist;
    EPStatement updateMicroCluster;
    EPStatement deliverMicroClusterSt;
    
    @Override
    public void start(EPServiceProvider epsp, EventConsumer ec) {
        CEPEngine = epsp;
        
        MicroClusterHolder.numOfDeliveredMicroClusters = 0;
        MicroClusterHolder.numReceivedTPs = 0;
        
        firstMicroClusterSt = CEPEngine.getEPAdministrator().createEPL(CREATE_FIRST_MICRO_CLUSTER_EPL);
        firstMicroClusterSt.addListener(new TrajectoryMicroClusterListener());
        
        createNewMicroClusterByTime = CEPEngine.getEPAdministrator().createEPL(CREATE_NEW_MICRO_CLUSTER_BY_TIME);
        createNewMicroClusterByTime.addListener(new TrajectoryMicroClusterListener());

        createNewMicroClusterByDist = CEPEngine.getEPAdministrator().createEPL(CREATE_NEW_MICRO_CLUSTER_BY_DISTANCE_EPL);
        createNewMicroClusterByDist.addListener(new TrajectoryMicroClusterListener());

        updateMicroCluster = CEPEngine.getEPAdministrator().createEPL(UPDATE_MICRO_CLUSTER_EPL);
        updateMicroCluster.addListener(new TrajectoryMicroClusterListener());
        
        deliverMicroClusterSt = CEPEngine.getEPAdministrator().createEPL(DELIVER_MICRO_CLUSTER_EPL);
        deliverMicroClusterSt.addListener(new TrajectoryMicroClusterListener());

    }
      
}
