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
package cepdest.CEP.producer;

import cepdest.config.ConfigProviderFactory;
import ceptraj.event.MapElement;
import ceptraj.event.location.LocationEvent;
import java.util.ArrayList;
import java.util.List;
import cepdest.tools.Constants;
import ceptraj.tool.Color;
import ceptraj.tool.Templates;
import cepdest.tools.CEPdestTemplates;
import ceptraj.EPA.adaptor.AdaptorEPA;
import ceptraj.output.visualizer.OutputType;
import ceptraj.tool.Point;
import ceptraj.tool.Polygon2D;
import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 * Class to detect the different itineraries in a raw trace log.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class BasicItineraryConstructor implements ItineraryConstructor{

    static Logger LOG = Logger.getLogger(BasicItineraryConstructor.class);

    int numOfItinerary = 1;
    
    List<MapElement> locations;
    StringBuilder serializedItinerariesInKML = new StringBuilder();
    
    @Override
    public void generateItineraries() throws Exception{
             
        List<LocationEvent> currentItinerary = new ArrayList<LocationEvent>();
        LocationEvent prevLe = (LocationEvent)locations.get(0);
        Polygon2D referenceArea = ConfigProviderFactory.getConfig().getReferenceArea();
        String user = ConfigProviderFactory.getConfig().getUserID();
        for(MapElement me : locations){
            LocationEvent le = (LocationEvent)me;
                        
            if(le.getTimestamp() - prevLe.getTimestamp() > Constants.TIME_BETWEEN_ITINERARIES){
                if((prevLe.getTimestamp() - currentItinerary.get(0).getTimestamp()) > Constants.MIN_ITINERARY_LIFESPAN &&
                        currentItinerary.size() > Constants.MIN_ITINERARY_POINTS){
                    
                    Point first = currentItinerary.get(0).getLocation();
                    Point last = currentItinerary.get(currentItinerary.size()-1).getLocation();
                    
                    if(referenceArea.contains(first) &&
                            referenceArea.contains(last)){
                        processNewItinerary(currentItinerary);

                        LOG.debug("Created itinerary #"+numOfItinerary+" for user "+user);
                        numOfItinerary++;
                    }else{
                        LOG.debug("Itinerary not included <out of reference area> for user "+user);
                    }
                }
                currentItinerary = new ArrayList<LocationEvent>();
            }
            
            currentItinerary.add(le);
            
            prevLe = le;
        }
        
        if(currentItinerary.size() > Constants.MIN_ITINERARY_POINTS &&
                (currentItinerary.get(currentItinerary.size()-1).getTimestamp() 
                - currentItinerary.get(0).getTimestamp()) > Constants.MIN_ITINERARY_LIFESPAN){
            processNewItinerary(currentItinerary);
        }
           
        System.out.println(ConfigProviderFactory.getConfig().getUserID() +" "+(numOfItinerary-1));
        
        postProcessAllItineraries();
        
        LOG.info("All the itineraries for user "+ConfigProviderFactory.getConfig().getUserID() + " has been created.");

    }
    
    protected void processNewItinerary(List<LocationEvent> currentItinerary){

        Collections.sort(currentItinerary);
        
        String itineraryInKML = CEPdestTemplates.KML_ITINERARY_TEMPLATE;
        itineraryInKML = itineraryInKML.replace("ITINERARY_NAME", ConfigProviderFactory.getConfig().getUserID() + "_"+numOfItinerary);
        itineraryInKML = itineraryInKML.replace("USER_NAME", ConfigProviderFactory.getConfig().getUserID());
        
        DateFormat format = new SimpleDateFormat(Templates.KML_DATE_FORMAT);
       
        // Info details about the current itinerary.
        Date date = new Date(currentItinerary.get(0).getTimestamp());
        itineraryInKML = itineraryInKML.replace("START_TIMESTAMP", format.format(date));
        date = new Date(currentItinerary.get(currentItinerary.size()-1).getTimestamp());
        itineraryInKML = itineraryInKML.replace("END_TIMESTAMP", format.format(date));
        
        itineraryInKML = itineraryInKML.replace("STYLE_URL", "#linestyle"+numOfItinerary % Color.values().length);

        //Here we add the coordinates comprising the itinerary just created.
        StringBuilder coordinates = new StringBuilder();
        
        for(LocationEvent le : currentItinerary){
            coordinates.append(le.serialize(OutputType.KML));
        }
        
        itineraryInKML = itineraryInKML.replace("ITINERARY_COORDINATES", coordinates);        
        serializedItinerariesInKML.append(itineraryInKML);        
    }
    
    protected void postProcessAllItineraries() throws Exception{
        
        StringBuilder KMLFileContent = new StringBuilder();
        
        //KML general head line
        String kmlGeneralHead = Templates.KML_GENERAL_HEAD.replace("ELEMENT_NAME", ConfigProviderFactory.getConfig().getUserID());
        KMLFileContent.append(kmlGeneralHead);

        
        //KML itineraries styles
        for(Color c : Color.values()){
            String itineraryStyle = Templates.KML_TRACK_STYLE.replace("NUM_LEVEL", String.valueOf(c.getLevel()));
            itineraryStyle = itineraryStyle.replace("COLOR_CODE", c.getHexCode());
            
            KMLFileContent.append(itineraryStyle);            
        }
          
        //KML particular head line
        String kmlSpecificHead = Templates.KML_SPECIFIC_HEAD.replace("ELEMENT_NAME", "tracks");
        KMLFileContent.append(kmlSpecificHead);
        
        //Itineraries in KML format
        KMLFileContent.append(serializedItinerariesInKML);
        
        //KML tail lines        
        KMLFileContent.append(Templates.KML_SPECIFIC_TAIL);
        KMLFileContent.append(Templates.KML_GENERAL_TAIL);
        
        //Final generation of the .KML file
        String KMLFilePath = ConfigProviderFactory.getConfig().getCommonOuputPath();
        KMLFilePath += Constants.ITINERARIES_FOLDER_NAME;
        KMLFilePath += File.separator;
        
        StringBuilder itineraryName = new StringBuilder();
        itineraryName.append(ConfigProviderFactory.getConfig().getUserID());
        itineraryName.append(".kml");
        
        File KMLFile = new File(KMLFilePath + itineraryName.toString());
        PrintWriter kmlWriter = new PrintWriter(KMLFile);
        kmlWriter.write(KMLFileContent.toString());
        kmlWriter.close();
    }
    
    @Override
    public void setLocations(List<MapElement> list) {
        locations = list;
    }

    @Override
    public void run() {
        try{
            generateItineraries();
        }catch(Exception e){
            LOG.error("An error has ocurred while generating itineraries for user "+ ConfigProviderFactory.getConfig().getUserID(),e);
        }
    }    

    @Override
    public void setAdaptor(AdaptorEPA aepa) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
