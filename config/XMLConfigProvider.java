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

import static cepdest.config.ExecutionMode.ITINERARY_GENERATION;
import cepdest.config.input.LocationInputType;
import cepdest.prediction.PredictorConfig;
import cepdest.prediction.PredictorConfig.HolderIterationMode;
import cepdest.prediction.PredictorConfig.PredictorType;
import ceptraj.EPA.adaptor.LocationSourceFormat;
import ceptraj.tool.Polygon2D;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import lda.config.LDAConfigProviderFactory;
import cepdest.tools.Constants;
import ceptraj.config.ConfigProvider.SpaceType;

/**
 * Class that provides the configuration of the IvCA module whereby a XML file.
 *
 * @author Fernando Terroso Saenz
 */
public class XMLConfigProvider implements ConfigProvider{

    static Logger LOG = Logger.getLogger(XMLConfigProvider.class);
    
    //Environment
    String rootPath;
    
    //User
    String userID = null;
    
    ExecutionMode mode;
    RepresentationMode representationMode;
       
    //Input-positions
    LocationInputType locationInputType;
    List<String> locationFilePaths;
    LocationSourceFormat locationInputFormat;
    SpaceType locationSpaceType; //lat-lon, cartesian
    
    //Input-prediction
    PredictorConfig predictorConfig;
    
    //Input learning-rate
    int nItinerariesForTraining;
    
    //Output
    String outputPath;
    
    Polygon2D restrictedArea;
    
    String numIt;        
        
    public XMLConfigProvider(String configXMLFilePath){
        init(configXMLFilePath);
    }
    
    private void init(String xmlPath){

        try{
            rootPath = getRootPath();
            
            SAXBuilder builder=new SAXBuilder(false);
            Document doc=builder.build(xmlPath);
            Element root =doc.getRootElement();                      
            
            parseUserElement(root);
            parseExecutionModeElement(root);
            
            Element input = root.getChild("input");
            parseInputElement(input);
            
            Element output = root.getChild("output");
            parseOutputElement(output);
            
            Element ldaXMLElement = root.getChild("lda");
           
            //Creates and parses the xml part of the LDA
            LDAConfigProviderFactory.createConfigProvider(ldaXMLElement);
            LDAConfigProviderFactory.getCurrentConfigProvider().setUserID(userID);
            LDAConfigProviderFactory.getCurrentConfigProvider().setSpaceType(locationSpaceType.toString());
     
            LOG.info("Config file parsed...OK");
        }catch(Exception e){
            LOG.error("Parse fail ", e);
        }
    }
    
    private String getRootPath() throws Exception{
        
        String oS = System.getProperty("os.name"); 
        oS = oS.toLowerCase();
        oS = oS.replace(" ", "_");
       
       Properties p = new Properties();
       
       p.load(new FileInputStream("operating_system.properties"));
       String path = p.getProperty(oS+".config.path");
       
       if(!path.endsWith(File.separator)){
           path = path.concat(File.separator);
       }
       
       return path;
    }

    protected void parseUserElement(Element root){       
        Element userE = root.getChild("user");
        userID = (userE != null) ? userE.getText() : null;
    }
    
    protected void parseExecutionModeElement(Element root){
        mode = ExecutionMode.valueOf(root.getChild("mode").getText());
    }
    
    protected void parseInputElement(Element input){

        switch(mode){
            case DESTINATION_PREDICTION:
                Element destinationPredictionE = input.getChild("destination_prediction");
                parseDestPredInputElement(destinationPredictionE);
                break;
            case ITINERARY_GENERATION:
                Element itGenerationE = input.getChild("itinerary_generation");
                parseItineraryGenerationInputElement(itGenerationE);
                break;
        }              
    }
    
    protected void parseDestPredInputElement(Element destPredInput){
        Element locations = destPredInput.getChild("locations");
        parseLocationsElement(locations);
        Element prediction = destPredInput.getChild("prediction");
        parsePredictionElement(prediction);
        
        representationMode = RepresentationMode.valueOf(destPredInput.getChild("representation").getText());        
        nItinerariesForTraining = Integer.valueOf(destPredInput.getChild("num_itineraries_for_training").getText());
    }
    
    protected void parseItineraryGenerationInputElement(Element itGenInput){
        Element locations = itGenInput.getChild("locations");
        parseLocationsElement(locations);
        Element restrictedAreaE = itGenInput.getChild("restricted_area");
        parseRestrictedAreaElement(restrictedAreaE);
    }    
    
    protected void parseRestrictedAreaElement(Element restrictedAreaE){
        List<Element> locations = restrictedAreaE.getChildren("location");
        restrictedArea = new Polygon2D();
        for(Element l :locations){
            String coordsString = l.getText();
            String[] coords = coordsString.split(",");
            String lat = coords[0];
            String lon = coords[1];
            restrictedArea.addPoint(Float.valueOf(lat), Float.valueOf(lon));
        }               
    }
    
    protected void parsePredictionElement(Element input){
        predictorConfig = new PredictorConfig();
        predictorConfig.setMinIndividualProbability(Double.valueOf(input.getChild("minProb").getText()));
        predictorConfig.setMaxPredictedAreas(Integer.valueOf(input.getChild("k").getText()));
        predictorConfig.setType(PredictorType.valueOf(input.getChild("type").getText()));
        predictorConfig.setMaxBearingDiff(Double.valueOf(input.getChild("max_bearing_diff").getText()));
        
        String iterationModesSt = input.getChild("iteration_mode").getText();
        String iterationModesArr[] = iterationModesSt.split(",");
        List<HolderIterationMode> iterationModes = new ArrayList<HolderIterationMode>();
        for(String iterationMode : iterationModesArr){
            iterationModes.add(HolderIterationMode.valueOf(iterationMode.toUpperCase()));
        }
        
        predictorConfig.setIterationModes(iterationModes);
        
    }
    
    protected void parseOutputElement(Element input){

        outputPath = input.getChild("path").getText();
        
        if(!outputPath.endsWith(File.separator))
            outputPath += File.separator;
    }
    
    protected void parseLocationsElement(Element locations){
        locationInputType = LocationInputType.valueOf(locations.getChild("type").getText());
        locationSpaceType = SpaceType.valueOf(locations.getChild("space_type").getText());
                                
        switch(locationInputType){
            case FILE:
                Element file = locations.getChild("file");
                switch(mode){
                    case ITINERARY_GENERATION:
                        locationFilePaths = Arrays.asList(file.getChild("path").getText());
                        break;
                    default:
                        Element pathsE = file.getChild("paths");
                        List<Element> paths = pathsE.getChildren("path");
                        locationFilePaths = new ArrayList<String>();
                        for(Element pathE : paths){
                            locationFilePaths.add(ConfigProviderFactory.getTestPath() + pathE.getText());
                        }
                        break;
                }
                
                locationInputFormat = LocationSourceFormat.valueOf(file.getChild("format").getText());
                break;
        }        
    }

        
    @Override
    public String getUserID() {
        
        switch(mode){
            case ITINERARY_GENERATION:
                return userID.replace("NUM", numIt); 
            default:
                return userID;
        }
    }

    @Override
    public void setUserID(String userID) {
        this.userID = userID;
        LDAConfigProviderFactory.getCurrentConfigProvider().setUserID(userID);
    }

    @Override
    public List<String> getPositionFilePaths() {
        switch(mode){
            case ITINERARY_GENERATION:
                String s = locationFilePaths.get(0).replace("NUM", numIt);
                locationFilePaths= Arrays.asList(s);
            default:
                return locationFilePaths;
        }        
    }
    
    @Override
    public PredictorConfig getPredictorConfig(){
        return predictorConfig;
    }


    @Override
    public String getCommonOuputPath() {
        return ConfigProviderFactory.getTestPath()+outputPath;
    }
    
    @Override
    public ExecutionMode getExecutionMode(){
        return mode;
    }
    
    @Override
    public int numItinerariesForTraining(){
        return nItinerariesForTraining;
    }

    @Override
     public LocationSourceFormat getLocationSourceFormat(){
         return locationInputFormat;
     }
     
    @Override
     public SpaceType getLocationSpaceType(){
         return locationSpaceType;
     }

    @Override
    public void setNumIt(int n){
        
        if(n > 99){
            numIt = String.valueOf(n);
        }else if(n >9){
            numIt = "0"+String.valueOf(n);
        }else{
            numIt = "00"+String.valueOf(n);
        }
    }

    @Override
    public Polygon2D getReferenceArea() {
        return restrictedArea;
    }

    @Override
    public RepresentationMode getRepresentationMode() {
        return representationMode;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        RepresentationMode repMode = ConfigProviderFactory.getConfig().getRepresentationMode();
        sb.append(repMode.toString().toLowerCase());
        sb.append("_");
        sb.append(ConfigProviderFactory.getConfig().getPredictorConfig());
        sb.append("_");
        switch(getRepresentationMode()){
            case LDA:
                sb.append(LDAConfigProviderFactory.getCurrentConfigProvider().getClusterTypeWithLevel(1).getRadius());
                break;
            case GRILL:
                sb.append(Constants.CELL_SIZE);
                break;
        }
        
        
        return sb.toString();
    }

    @Override
    public void setRepresentationMode(RepresentationMode mode) {
        representationMode = mode;
    }
    
    @Override
    public void setExecutionMode(ExecutionMode mode){
        this.mode = mode;
    }

}
