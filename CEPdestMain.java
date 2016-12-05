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
package cepdest;

import cepdest.CEP.EPA.MicroClusterHolder;
import cepdest.CEP.EPA.TrajectoryMicroClusterEPA;
import cepdest.CEP.EPA.adaptor.GeoLifeDirectoryAdaptor;
import cepdest.CEP.consumer.EventConsumerWithPrediction;
import cepdest.config.ConfigProviderFactory;
import cepdest.CEP.consumer.LDAEventConsumer;
import cepdest.CEP.consumer.GrillEventConsumer;
import cepdest.pattern.holder.PatternsHolder;
import cepdest.CEP.producer.BasicItineraryConstructor;
import cepdest.area.ItAreaOfInterestType;
import cepdest.config.ConfigProvider;
import cepdest.config.ExecutionMode;
import cepdest.config.RepresentationMode;
import cepdest.pattern.holder.PatternsHolderFactory;
import cepdest.prediction.Predictor;
import cepdest.prediction.PredictorConfig.PredictorType;
import cepdest.prediction.PredictorFactory;
import cepdest.stats.BasicStatsGenerator;
import cepdest.stats.StatsGenerator;
import cepdest.tools.Constants;
import ceptraj.EPA.adaptor.LocationSourceFormat;
import ceptraj.main.CEPtraj;
import ceptraj.output.EventConsumer;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import lda.config.LDAConfigProvider;
import lda.config.LDAConfigProviderFactory;
import lda.landmark.LandmarkType;
import lda.landmark.provider.LandmarkProvider.LandmarkProviderStats;
import lda.landmark.provider.LandmarkProviderFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class CEPdestMain {
    
    static Logger LOG = Logger.getLogger(CEPdestMain.class);
    
    public static void main(String[] args) {
        try{
            
            LOG.info("----------------");
            LOG.info("CEP-dest started.");
            // We disable esper log as it is too verbose.
            Logger.getLogger("com.espertech.esper").setLevel(Level.ERROR);
            
            String hostOperatingSystem = System.getProperty("os.name");     
            String configPath = ConfigProviderFactory.getTestPath(hostOperatingSystem)+ args[0];
            ConfigProviderFactory.createConfigProvider(configPath);
            
            ConfigProvider config = ConfigProviderFactory.getConfig();
            
            if(args.length > 0){
                LOG.info("Run CEP-dest with params "+Arrays.toString(args));
                for(int i= 0; i< args.length; i++){
                    switch(i){
                        case 1:
                            config.setRepresentationMode(RepresentationMode.valueOf(args[i]));
                            break;
                        case 2:
                            config.getPredictorConfig().setMinIndividualProbability(Double.valueOf(args[i]));
                            break;
                        case 3:
                            switch(config.getRepresentationMode()){
                                case LDA:
                                    LDAConfigProvider ldaConfig = LDAConfigProviderFactory.getCurrentConfigProvider();
                                    List<LandmarkType> ldaTypes = ldaConfig.getClusterTypes();
                                    String[] parts = args[i].split("x");
                                    for(LandmarkType ldaType : ldaTypes){
                                        if(ldaType.getLevel() == 1){
                                            ldaType.setRadius(Float.valueOf(parts[0]));
                                            ldaType.setMinPoints(Integer.valueOf(parts[1]));
                                        }
                                    }
                                    break;
                                case GRILL:
                                    Constants.CELL_SIZE = Double.valueOf(args[i]);  
                                    config.getPredictorConfig().setType(PredictorType.BASIC);
                                    break;                                            
                            }
                            break;
                        case 4:
                            config.setExecutionMode(ExecutionMode.valueOf(args[i]));                            
                            break;
                    }                    
                }
            }
            
            switch(config.getExecutionMode()){
                case ITINERARY_GENERATION:                    
                    runItineraryGenerator();
                    break;
                case DESTINATION_PREDICTION:
                    runDestinationPrediction();
                    break;
            }            
            
            LOG.info("CEP-dest has finished.");

        }catch(Exception e){
            LOG.error("Error in CEP-dest main method ", e);
        }
    }  
    
    private static void runItineraryGenerator(){
        LOG.info("Initiating 'itinerary generation' mode...");
        for(int i = 0; i<= 181; i++){
            ConfigProviderFactory.getConfig().setNumIt(i);
            GeoLifeDirectoryAdaptor geoLifeAdaptor = new GeoLifeDirectoryAdaptor(ConfigProviderFactory.getConfig().getPositionFilePaths().get(0));
            geoLifeAdaptor.start(new BasicItineraryConstructor(),false);
        }        
        
        LOG.info("END OF EXECUTION: All itineraries have been created.");
    }
    
    private static void runDestinationPrediction() throws Exception{
        LOG.info("Initiating 'destination prediction' mode...");
        
        ConfigProvider config = ConfigProviderFactory.getConfig();
        
        String userID = config.getUserID();
        
        boolean dynamicUser = (userID == null);
        
        int nFilteredLocs = 0;
        int nTPs = 0;
        int nSPs = 0;
        int nAggTPs = 0;
        
        
        for(String path : config.getPositionFilePaths()){
                        
            if(dynamicUser){
                String [] parts = path.split("_");
                String aux = parts[parts.length-1];
                String[] parts2 = aux.split("\\.");
                userID = parts2[0];
                config.setUserID(userID);
            }
                        
            for(int i= 0; i< config.getPredictorConfig().getIterationModes().size(); i++){
                
                config.getPredictorConfig().setIterationModeIndex(i);
                
                LOG.info("Starting processing user "+userID + " with iteration "+config.getPredictorConfig().getCurrentIterationMode());
            
                PatternsHolder patternsHolder = PatternsHolderFactory.getPatternsHolder();
                Predictor predictor = PredictorFactory.getPredictor();
                StatsGenerator statsGen = new BasicStatsGenerator();

                EventConsumer eventConsumer = null;
                switch(config.getRepresentationMode()){
                    case GRILL:
                        eventConsumer = new GrillEventConsumer(patternsHolder, predictor, statsGen);
                        break;
                    case LDA:
                        LDAEventConsumer ldaEventConsumer = new LDAEventConsumer(patternsHolder, predictor, statsGen);        
                        MicroClusterHolder.setMicroClusterConsumer(ldaEventConsumer);
                        eventConsumer = ldaEventConsumer;
                        break;
                }
                
                
                CEPtraj.start(
                        config.getLocationSourceFormat(),
                        eventConsumer, 
                        path, 
                        config.getCommonOuputPath()+"cep_traj"+File.separator,
                        new TrajectoryMicroClusterEPA(),
                        config.getLocationSpaceType(),
                        new CEPDestEngineConfigurator());        
                
                LOG.info("AVG TIME DURATION: "+((LDAEventConsumer)eventConsumer).avgItTimeLength);
                                
                //Eventually, the landmarks are exported to KML format
                LandmarkProviderFactory.getCurrentLandmarkProvider(userID).printLandmarks();
//                LandmarkProviderStats lps = LandmarkProviderFactory.getCurrentLandmarkProvider(userID).getStats();
//                LOG.info("t:num: "+lps.getNumLandmarksForLevel(1) + " dist:"+ lps.getAvgDistForLevel(1));
//                LandmarkProviderFactory.getCurrentLandmarkProvider(userID).serializeLandmarks();

                LandmarkProviderFactory.getCurrentLandmarkProvider(userID+"_"+ItAreaOfInterestType.START_END.getAbr()).printLandmarks();
//                lps = LandmarkProviderFactory.getCurrentLandmarkProvider(userID+"_"+ItAreaOfInterestType.START_END.getAbr()).getStats();
//                LOG.info("s_e:num: "+lps.getNumLandmarksForLevel(1) + " dist:"+ lps.getAvgDistForLevel(1));
//                LandmarkProviderFactory.getCurrentLandmarkProvider(userID+"_"+ItAreaOfInterestType.START_END.getAbr()).serializeLandmarks();

//                PatternsHolderFactory.serializePatterns();            

                eventConsumer.postProcessAllEvents();
                
                // And the stats are also generated.
                statsGen.generateUserStats();
                
                nFilteredLocs +=((EventConsumerWithPrediction) eventConsumer).getNumFilteredLocations();                
                nTPs += MicroClusterHolder.numReceivedTPs;                
                nAggTPs += MicroClusterHolder.numOfDeliveredMicroClusters;
                nSPs += ((EventConsumerWithPrediction) eventConsumer).getNumReceivedSPs();
                
                if(PredictorType.BASIC.equals(config.getPredictorConfig().getType()))
                    break;                
            }        
        }

        StringBuilder sb = new StringBuilder();
        sb.append("filtered:");
        sb.append(nFilteredLocs);
        sb.append(" SP-TPs:");
        sb.append((nTPs+nSPs));
        sb.append(" SP-Agg. TPs:");
        sb.append((nAggTPs + nSPs));
        
        LOG.debug(sb.toString());
        
        LOG.info("END OF EXECUTION: All itineraries have been processed.");

    }
    
}
