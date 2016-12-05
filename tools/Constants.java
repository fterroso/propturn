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
package cepdest.tools;

/**
 * Class with the common constant values for all the system.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class Constants {
    
    public static final double TIME_BETWEEN_ITINERARIES = 900000; //ms
    
    public static final double MIN_ITINERARY_LIFESPAN = 300000; //ms
    
    public static final double MIN_ITINERARY_POINTS = 5; //ms
    
    public static final String ITINERARIES_FOLDER_NAME = "generated_itineraries";
    
    public static final String GEO_LIFE_FILE_EXTENSION = ".plt";
    
    //Constants related to the stats generation process
    public static final String NEXT_AREA_DR_FILE_NAME = "next_area_dr";
    public static final String NEXT_AREA_DIST_FILE_NAME = "next_area_dist";
    public static final String NEXT_AREA_RATIO_FILE_NAME = "next_area_ratio";
    public static final String NEXT_AREA_DR_DATASET_FILE_NAME = "next_area_dr_dataset";
    public static final String NEXT_AREA_DIST_DATASET_FILE_NAME = "next_area_dist_dataset";
    public static final String NEXT_AREA_RATIO_DATASET_FILE_NAME = "next_area_ratio_dataset";
    
    public static final String DEST_DR_FILE_NAME = "dest_dr"; 
    public static final String DEST_DIST_FILE_NAME = "dest_dist";
    public static final String DEST_RATIO_FILE_NAME = "dest_ratio";
    public static final String DEST_DR_DATASET_FILE_NAME = "dest_dr_dataset";
    public static final String DEST_DIST_DATASET_FILE_NAME = "dest_dist_dataset";    
    public static final String DEST_RATIO_DATASET_FILE_NAME = "dest_ratio_dataset";
    
    public static final String CHAIN_LENGHT_DATASET_FILE_NAME = "chain_length_dataset";
    public static final String PREDICTION_DATASET_FILE_NAME = "prediction_dataset";
    
    public static final String PREDICTION_LENGTH_DATASET_FILE_NAME = "prediction_lengths_dataset";

    
    public static final String ENTROPY_DATASET_FILE_NAME = "entropy_dataset";
    public static final String CONVERGENCE_DATASET_FILE_NAME = "convergence_dataset";
    
    public static final String LDA_STATS_FILE_NAME = "lda_stats";
    
    public static double CELL_SIZE = 100; //in meters.

}
