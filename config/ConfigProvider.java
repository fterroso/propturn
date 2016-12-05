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
package cepdest.config;

import cepdest.prediction.PredictorConfig;
import ceptraj.EPA.adaptor.LocationSourceFormat;
import ceptraj.config.ConfigProvider.SpaceType;
import ceptraj.tool.Polygon2D;
import java.util.List;

/**
 * 
 * Interface for the classes that provide the config data of the IvCA module.
 *
 * @author Fernando Terroso-Saenz
 */
public interface ConfigProvider {
    
    public String getUserID();
    
    public void setUserID(String userID);
    
    public void setRepresentationMode(RepresentationMode mode);
                               
    public List<String> getPositionFilePaths();  
    
    public String getCommonOuputPath();
    
    public ExecutionMode getExecutionMode();
    
    public void setExecutionMode(ExecutionMode mode);
    
    public RepresentationMode getRepresentationMode();
    
    public PredictorConfig getPredictorConfig();
    
    public int numItinerariesForTraining();
    
    public Polygon2D getReferenceArea();   
    
    public LocationSourceFormat getLocationSourceFormat();
    
    public SpaceType getLocationSpaceType();
            
    //temporal
    public void setNumIt(int n);

}
