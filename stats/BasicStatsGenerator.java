package cepdest.stats;

import cepdest.area.ItAreaOfInterestType;
import cepdest.config.ConfigProviderFactory;
import cepdest.config.RepresentationMode;
import cepdest.itinerary.Itinerary;
import cepdest.itinerary.Itinerary.ItineraryStats;
import cepdest.itinerary.Itinerary.ItineraryStats.PredictionType;
import static cepdest.itinerary.Itinerary.ItineraryStats.PredictionType.CORRECT;
import static cepdest.itinerary.Itinerary.ItineraryStats.PredictionType.INCORRECT;
import static cepdest.itinerary.Itinerary.ItineraryStats.PredictionType.NO_PREDICTION;
import cepdest.tools.Constants;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lda.landmark.provider.LandmarkProvider.LandmarkProviderStats;
import lda.landmark.provider.LandmarkProviderFactory;
import org.apache.log4j.Logger;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class BasicStatsGenerator implements StatsGenerator{

    static Logger LOG = Logger.getLogger(BasicStatsGenerator.class);
    
    List<Itinerary> finishedItineraries = new ArrayList<Itinerary>();
    int maxAreas = 0;
    
    @Override
    public void itineraryHasFinished(Itinerary itinerary) {
        finishedItineraries.add(itinerary);
    }
    
    
    @Override
    public void generateUserStats(){
        
        try{

            LOG.info("Generating user stats...");
            double[][] nextAreaPredCounter = new double[11][3];
            double[][] destPredCounter = new double[11][3];

            double[]nextAreaPredTotalCounter = new double[3];
            double[]destPredTotalCounter = new double[3];
            
            double[] naDist = new double[11];
            double[] naDistCounter = new double[11];
            double[] dstDist = new double[11];
            double[] dstDistCounter = new double[11];

            double avgNADist = 0;
            double avgDstDist = 0;   
            double avgNADistCounter = 0;
            double avgDstDistCounter = 0;
            
            double[] chainLenghtCounter = new double[11];            
            
            String userId = ConfigProviderFactory.getConfig().getUserID();
            
            double avgPredTime = 0;
            double avgPredTimeCounter = 0;
            
            double avgPredLength = 0;
            double avgPredLengthCounter = 0;
            
            Map<Integer,Double> avgPredLengthsDR = new HashMap<Integer,Double>();
            Map<Integer,Double> avgPredLengthsDist = new HashMap<Integer,Double>();

            double avgEntropy = 0;
            double avgEntropyC = 0;
            
            int numItineraries = finishedItineraries.size();
            int itRange = numItineraries/20;            
            int itCounter = 0;
            int itPercentage = 5;
            double nNamedAreasForRange = 0;
            
            StringBuilder convergenceData = new StringBuilder();
            
            
            //First we process the stats of all the itineraries
            for(Itinerary it : finishedItineraries){
                
                ItineraryStats stats = it.getStatistics();
                
                double entropy = it.getEntropy();
                if(!Double.isInfinite(entropy) && !Double.isInfinite(entropy) && !Double.isNaN(entropy)){
                    avgEntropy = avgEntropy + ((entropy-avgEntropy)/++avgEntropyC);
                }
                
                if(!Double.isNaN(stats.getNamedAreasPct())){
                    nNamedAreasForRange += stats.getNamedAreasPct();
                }
                                
                PredictionType[] nextAreaPreds = stats.getNextAreaDR();
                PredictionType[] destPreds = stats.getDestinationDR();
                double[] itNADists = stats.getNextAreaDist();
                double[] itDstDists = stats.getDestinationDist();
                int[] ranges = stats.getCoveredRoute();
                double[] chainLengths = stats.getPredictionChainLength();
                
                if(stats.getAvgPredictionTime()>0){
                    avgPredTime += stats.getAvgPredictionTime();
                    avgPredTimeCounter++;
                }
                
                if(stats.getAvgPredLenght() > 0){
                    avgPredLength += stats.getAvgPredLenght();
                    avgPredLengthCounter++;
                    
                    for(int c : stats.getPredLengthsDR().keySet()){
                        double p = stats.getPredLengthsDR().get(c);
                        
                        double g = (avgPredLengthsDR.containsKey(c)) ? avgPredLengthsDR.get(c) : 0;
                        g += p;
                        avgPredLengthsDR.put(c, g);
                    }
                    
                    for(int c : stats.getPredLengthsDist().keySet()){
                        double p = stats.getPredLengthsDist().get(c);
                        
                        double g = (avgPredLengthsDist.containsKey(c)) ? avgPredLengthsDist.get(c) : 0;
                        g += p;
                        avgPredLengthsDist.put(c, g);
                    }                    
                }
                
                for(int i= 0; i < it.getAreas().size();i++){

                    int range = ranges[i];
                    
                    switch(nextAreaPreds[i]){
                        case CORRECT:
                            nextAreaPredCounter[range][0]++;
                            nextAreaPredTotalCounter[0]++;
                            break;
                        case INCORRECT:
                            nextAreaPredCounter[range][1]++;
                            nextAreaPredTotalCounter[1]++;
                            if(itNADists[i] != Double.NEGATIVE_INFINITY &&
                                    itNADists[i] != Double.NaN){
                                naDist[range] = naDist[range] + ((itNADists[i]-naDist[range])/(++naDistCounter[range]));
                                avgNADist = avgNADist + ((itNADists[i]-avgNADist)/(++avgNADistCounter));
                            }
                            break;
                        case NO_PREDICTION:
                            nextAreaPredCounter[range][2]++;
                            nextAreaPredTotalCounter[2]++;
                            break;                        
                    }

                    switch(destPreds[i]){
                        case CORRECT:
                            destPredCounter[range][0]++;
                            destPredTotalCounter[0]++;
                            break;
                        case INCORRECT:
                            destPredCounter[range][1]++;
                            destPredTotalCounter[1]++;
                            if(itDstDists[i] != Double.NEGATIVE_INFINITY &&
                                    itDstDists[i] != Double.NaN){
                                dstDist[range] = dstDist[range] + ((itDstDists[i]-dstDist[range])/(++dstDistCounter[range]));
                                avgDstDist = avgDstDist + ((itDstDists[i]-avgDstDist)/(++avgDstDistCounter));
                            }
                            break;
                        case NO_PREDICTION:
                            destPredCounter[range][2]++;
                            destPredTotalCounter[2]++;
                            break;                        
                    } 
                    chainLenghtCounter[range] += chainLengths[i];
                }
                
                if(itCounter >= itRange){
                    double total = nextAreaPredTotalCounter[0] + nextAreaPredTotalCounter[1] +nextAreaPredTotalCounter[2];
                    double naDRForRange = (nextAreaPredTotalCounter[0] + nextAreaPredTotalCounter[1])/total;
                    
                    total = destPredTotalCounter[0]+destPredTotalCounter[1]+destPredTotalCounter[2];
                    double destDRForRange = (destPredTotalCounter[0]+destPredTotalCounter[1])/total;
                    
                    double drForRange = (naDRForRange>destDRForRange)?naDRForRange : destDRForRange;
                                       
                    double distForRange = (avgDstDist<avgNADist)? avgDstDist : avgNADist;
                    nNamedAreasForRange /= itCounter;
                    
                    convergenceData.append(itPercentage);
                    convergenceData.append("\t");
                    convergenceData.append(String.format(Locale.US, "%.2f", nNamedAreasForRange));
                    convergenceData.append("\t");
                    convergenceData.append(String.format(Locale.US, "%.2f", drForRange));
                    convergenceData.append("\t");
                    convergenceData.append(String.format(Locale.US, "%.2f", distForRange));
                    convergenceData.append("\n");
                    
                    Arrays.fill(nextAreaPredTotalCounter, 0.0);
                    Arrays.fill(destPredTotalCounter, 0.0);
                    
                    itCounter = 0;
                    avgDstDist = nNamedAreasForRange = 0;
                    itPercentage += 5;
                }else{
                    itCounter++;
                }
            }
            
            //Secondly, we generate the charts from the gathered stats

            avgPredTime /= avgPredTimeCounter;
            avgPredLength /= avgPredLengthCounter;
            
            Map<Integer,Double> nextAreaDRSerie = new HashMap<Integer,Double>();
            Map<Integer,Double> destDRSerie = new HashMap<Integer,Double>();
            Map<Integer,Double> nextAreaDistSerie = new HashMap<Integer,Double>();
            Map<Integer,Double> destDistSerie = new HashMap<Integer,Double>();
            Map<Integer,Double> chainLengthSerie = new HashMap<Integer,Double>();
            double avgNextAreaDR = 0;
            double avgDestDR = 0;

            for(int i= 0; i< 11; i++){
                double total = nextAreaPredCounter[i][0] + nextAreaPredCounter[i][1] +nextAreaPredCounter[i][2];
                
                nextAreaDRSerie.put(i*10, (nextAreaPredCounter[i][0]+nextAreaPredCounter[i][1]) / total);

                total = destPredCounter[i][0] + destPredCounter[i][1] +destPredCounter[i][2];

                destDRSerie.put(i*10, (destPredCounter[i][0]+destPredCounter[i][1]) / total);
                
                double aux = (nextAreaPredCounter[i][0]/total)+ (nextAreaPredCounter[i][1]/total);
                avgNextAreaDR += aux;
                
                nextAreaDistSerie.put(i*10, naDist[i]);    
                
                aux = (destPredCounter[i][0]/total)+ (destPredCounter[i][1]/total);
                avgDestDR += aux;
                
                destDistSerie.put(i*10, dstDist[i]); 
                
                chainLenghtCounter[i] = chainLenghtCounter[i]/total;
                chainLengthSerie.put(i*10, chainLenghtCounter[i]);
                
            }
            
            String outputPath = ConfigProviderFactory.getConfig().getCommonOuputPath();

            StringBuilder sb = new StringBuilder();           
            sb.append(outputPath);
            sb.append(File.separator);
            sb.append("datasets");
            sb.append(File.separator);                
            sb.append(userId);
            sb.append("_");  
            sb.append("@target");
            sb.append("_");
            sb.append(ConfigProviderFactory.getConfig());           
            sb.append("@fileExtension");
            
            String fileNamePattern = sb.toString();
                        
            try{     
                
                ///// USER'S ROUTES ENTROPY DATASET /////
                String fileName = fileNamePattern;
                fileName = fileName.replace("@target", Constants.ENTROPY_DATASET_FILE_NAME);
                fileName = fileName.replace("@fileExtension", ".txt");
                
                PrintWriter writer = new PrintWriter(new File(fileName));
                writer.println(avgEntropy);
                writer.flush();
                writer.close(); 
                
                ///// USER'S CONVERGENCE PERIOD DATASET /////
                fileName = fileNamePattern;
                fileName = fileName.replace("@target", Constants.CONVERGENCE_DATASET_FILE_NAME);
                fileName = fileName.replace("@fileExtension", ".txt");
                
                writer = new PrintWriter(new File(fileName));
                writer.println(convergenceData);
                writer.flush();
                writer.close();  
                
                                
                ///// AVG. PREDICTION TIME DATASET /////
                fileName = fileNamePattern;
                fileName = fileName.replace("@target", Constants.PREDICTION_DATASET_FILE_NAME);
                fileName = fileName.replace("@fileExtension", ".txt");
                
                writer = new PrintWriter(new File(fileName));
                writer.println(String.format(Locale.US, "%.2f", avgPredTime));
                writer.println(String.format(Locale.US, "%.2f", avgPredLength));
                writer.flush();
                writer.close();
                
                ///// AVG. PREDICTION LENGTH DATASET /////
                fileName = fileNamePattern;
                fileName = fileName.replace("@target", Constants.PREDICTION_LENGTH_DATASET_FILE_NAME);
                fileName = fileName.replace("@fileExtension", ".csv");
                
                writer = new PrintWriter(new File(fileName));
                for(int c : avgPredLengthsDR.keySet()){
                    double p = avgPredLengthsDR.get(c)/avgPredLengthsDR.keySet().size();
                    double d = avgPredLengthsDist.get(c)/avgPredLengthsDR.keySet().size();
                    writer.println(c+":"+String.format(Locale.US, "%.2f", p)+":"+String.format(Locale.US, "%.1f", d));
                }
    
                writer.flush();
                writer.close();
                
                ///// NEXT AREA DR DATASET /////
                generateDatasetFile(fileNamePattern, 
                        Constants.NEXT_AREA_DR_DATASET_FILE_NAME, 
                        nextAreaDRSerie, 
                        avgNextAreaDR);
                                                
                ///// NEXT AREA DIST DATASET /////               
                generateDatasetFile(
                        fileNamePattern, 
                        Constants.NEXT_AREA_DIST_DATASET_FILE_NAME, 
                        nextAreaDistSerie, 
                        avgNADist);              
                        
                ///// DEST DR DATASET /////
                generateDatasetFile(
                        fileNamePattern, 
                        Constants.DEST_DR_DATASET_FILE_NAME, 
                        destDRSerie, 
                        avgDestDR);  
                               
                ///// DIST DEST DATASET /////               
                generateDatasetFile(
                        fileNamePattern, 
                        Constants.DEST_DIST_DATASET_FILE_NAME, 
                        destDistSerie, 
                        avgDstDist);                          
                
                ///// CHAIN LENGTH DATASET /////                               
                generateDatasetFile(
                        fileNamePattern, 
                        Constants.CHAIN_LENGHT_DATASET_FILE_NAME, 
                        chainLengthSerie);                 
                
                if(ConfigProviderFactory.getConfig().getRepresentationMode().equals(RepresentationMode.LDA)){
                    fileName = fileNamePattern;
                    fileName = fileName.replace("@target", Constants.LDA_STATS_FILE_NAME);
                    fileName = fileName.replace("@fileExtension", ".txt");
                    serializeLDAStats(fileName);
                }
                
            }catch(Exception e){
                LOG.error("Error while creating stats datasets",e);
            }
                                                  
        }catch(Exception e){
            LOG.error("Error while generation charts ", e);
        }        
    }
    

    
    protected void serializeLDAStats(String path) throws Exception{
        
        String userID = ConfigProviderFactory.getConfig().getUserID();
        LandmarkProviderStats LDAStats = LandmarkProviderFactory.getCurrentLandmarkProvider(userID).getStats();
        
       PrintWriter writer = new PrintWriter(new File(path));
       writer.println(LDAStats);
                
        LDAStats = LandmarkProviderFactory.getCurrentLandmarkProvider(userID+"_"+ItAreaOfInterestType.START_END.getAbr()).getStats();
        writer.println(LDAStats);
       
        writer.flush();
        writer.close();
    }
    
    protected void generateDatasetFile(
            String pathPattern,
            String target,         
            Map<Integer,Double> serie,
            double... avgValue) throws Exception{
        
        String fileName = pathPattern;
        fileName = fileName.replace("@target", target);
        fileName = fileName.replace("@fileExtension", ".txt");
                
        PrintWriter writer = new PrintWriter(new File(fileName));
        
        for(int k : serie.keySet()){
          writer.println(k + " " + serie.get(k));
        }
                
        if(avgValue.length > 0)
            writer.println(avgValue[0]/11);
        
        writer.flush();
        writer.close();
    }

}
