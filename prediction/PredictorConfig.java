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
package cepdest.prediction;

import java.util.List;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class PredictorConfig {
    
    int maxPredictedAreas;
    double minTotalProbability;
    double minIndividualProbability;
    
    PredictorType type;
    List<HolderIterationMode> iterationModes;
    
    int iterationModeIndex;
    
    double maxBearingDiff;

    public int getMaxPredictedAreas() {
        return maxPredictedAreas;
    }

    public void setMaxPredictedAreas(int maxPredictedAreas) {
        this.maxPredictedAreas = maxPredictedAreas;
    }

    public double getMinTotalProbability() {
        return minTotalProbability;
    }

    public void setMinTotalProbability(double minTotalProbability) {
        this.minTotalProbability = minTotalProbability;
    }

    public double getMinIndividualProbability() {
        return minIndividualProbability;
    }

    public void setMinIndividualProbability(double minIndividualProbability) {
        this.minIndividualProbability = minIndividualProbability;
    }

    public PredictorType getType() {
        return type;
    }

    public void setType(PredictorType type) {
        this.type = type;
    }

    public List<HolderIterationMode> getIterationModes() {
        return iterationModes;
    }

    public void setIterationModes(List<HolderIterationMode> iterationMode) {
        this.iterationModes = iterationMode;
    }
    
    public HolderIterationMode getCurrentIterationMode(){
        return iterationModes.get(iterationModeIndex);
    }

    public int getIterationModeIndex() {
        return iterationModeIndex;
    }

    public void setIterationModeIndex(int iterationModeIndex) {
        this.iterationModeIndex = iterationModeIndex;
    }

    public double getMaxBearingDiff() {
        return maxBearingDiff;
    }

    public void setMaxBearingDiff(double maxBearingDiff) {
        this.maxBearingDiff = maxBearingDiff;
    }

    @Override
    public String toString() {
        return type.toString().toLowerCase()+"_" + maxPredictedAreas + "_ " + minIndividualProbability+"_"+getCurrentIterationMode().getAbrv();
    }
    
    public enum PredictorType{
        BASIC,
        ENHANCED;
    }
    
        
    public enum HolderIterationMode{
        BACKWARDS ("b"),
        FULL_FORWARD ("ff"),
        FORWARD ("f");
        
        private String abrv;

        private HolderIterationMode(String abrv) {
            this.abrv = abrv;
        }

        public String getAbrv() {
            return abrv;
        }   
    }
    
}
