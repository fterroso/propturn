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
package cepdest.CEP.EPA.adaptor;

import cepdest.tools.Constants;
import ceptraj.EPA.adaptor.AdaptorEPA;
import ceptraj.EPA.adaptor.PlainTextAdaptorEPA;
import ceptraj.event.MapElement;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class GeoLifeDirectoryAdaptor extends AdaptorEPA{

    
    private String GPSTraceFile;
    
    public GeoLifeDirectoryAdaptor(String GPStraceFile) {
        
        this.GPSTraceFile = GPStraceFile;
    }
        
    @Override
    public List<MapElement> generateTargetEvents() {
        
        List<MapElement> events = new LinkedList<MapElement>();
        
        try{
            LOG.info("Processing geolife GPS files...");
            File rootFile = new File(GPSTraceFile);
            if(rootFile.isDirectory()){
                String[] geolifeFiles = rootFile.list();
                Arrays.sort(geolifeFiles);
                
                for(String geolifeFile : geolifeFiles){
                    if(geolifeFile.endsWith(Constants.GEO_LIFE_FILE_EXTENSION)){
                        PlainTextAdaptorEPA.FieldOrder order = new PlainTextAdaptorEPA.FieldOrder();
                        order.setId(-1);
                        order.setDate(new int[]{5,6});
                        order.setLat(0);
                        order.setLon(1);
                        PlainTextAdaptorEPA glaEPA = new PlainTextAdaptorEPA(
                                GPSTraceFile+geolifeFile, 
                                ceptraj.tool.Constants.GEOLIFE_DATE_FORMAT, 
                                ceptraj.tool.Constants.GEOLIFE_FILE_HEAD_NUM_LINES,
                                order,
                                1150);
                        
                        events.addAll(glaEPA.generateTargetEvents());
                    }
                }
            }else{
                 PlainTextAdaptorEPA.FieldOrder order = new PlainTextAdaptorEPA.FieldOrder();
                order.setId(-1);
                order.setDate(new int[]{5,6});
                order.setLat(0);
                order.setLon(1);
                PlainTextAdaptorEPA glaEPA = new PlainTextAdaptorEPA(
                        GPSTraceFile, 
                        ceptraj.tool.Constants.GEOLIFE_DATE_FORMAT, 
                        ceptraj.tool.Constants.GEOLIFE_FILE_HEAD_NUM_LINES,
                        order,
                        1150);
                events.addAll(glaEPA.generateTargetEvents());
            }
            LOG.info("All geolife GPS files have been processed.");
            
        }catch(Exception e){
            LOG.error("Error accessing GEO-life file ", e);  
        }finally{
            return events;
        }        
    }    

    @Override
    public MapElement getNextEvent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
