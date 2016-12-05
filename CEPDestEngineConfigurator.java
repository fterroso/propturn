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
package cepdest;

import cepdest.CEP.EPA.MicroClusterHolder;
import cepdest.CEP.event.BearingChangeClusterEvent;
import ceptraj.CEP.BasicCEPConfigurator;
import com.espertech.esper.client.Configuration;
import java.util.List;
import lda.config.LDAConfigProvider;
import lda.config.LDAConfigProviderFactory;
import lda.landmark.LandmarkType;
import cepdest.tools.Constants;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class CEPDestEngineConfigurator extends BasicCEPConfigurator{
            
    @Override
    protected void registerEvents(Configuration configuration){
        super.registerEvents(configuration);
        configuration.addEventType(BearingChangeClusterEvent.class); 
    }
    
    @Override
    protected void registerSingleRowFunctions(Configuration configuration){
        
        super.registerSingleRowFunctions(configuration);
                
        configuration.addPlugInSingleRowFunction("updateMicroCluster", MicroClusterHolder.class.getName(), "updateMicroCluster");   
        configuration.addPlugInSingleRowFunction("getMicroClusterCentroid", MicroClusterHolder.class.getName(), "getMicroClusterCentroid"); 
        configuration.addPlugInSingleRowFunction("getMicroClusterLastPoint", MicroClusterHolder.class.getName(), "getMicroClusterLastPoint");   
        configuration.addPlugInSingleRowFunction("getMicroClusterTimestamp", MicroClusterHolder.class.getName(), "getMicroClusterTimestamp"); 

        configuration.addPlugInSingleRowFunction("setFirstEvent", MicroClusterHolder.class.getName(), "setFirstEvent");   
        configuration.addPlugInSingleRowFunction("deliverCluster", MicroClusterHolder.class.getName(), "deliverCluster");           
    }
    
    @Override
    protected void registerVariables(Configuration configuration){
        super.registerVariables(configuration);
        
        LDAConfigProvider ldaConfig = LDAConfigProviderFactory.getCurrentConfigProvider();
        List<LandmarkType> ldaTypes = ldaConfig.getClusterTypes();        
        double r = 100;
        for(LandmarkType ldaType : ldaTypes){
            if(ldaType.getLevel() == 1){
                r= ldaType.getRadius();
                break;
            }
        }
        configuration.addVariable("maxClusterDist", Double.class, r);
        configuration.addVariable("maxClusterTime", Double.class, Constants.TIME_BETWEEN_ITINERARIES);

    }
}
