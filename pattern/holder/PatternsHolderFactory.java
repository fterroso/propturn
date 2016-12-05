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
package cepdest.pattern.holder;

import cepdest.config.ConfigProvider;
import cepdest.config.ConfigProviderFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.log4j.Logger;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class PatternsHolderFactory {
    
    static Logger LOG = Logger.getLogger(PatternsHolderFactory.class);
    private static final String BASIC_PATTERNS_HOLDER_PATH = "basic_patterns_holder.data";
    private static final String ENHANCED_PATTERNS_HOLDER_PATH = "enhanced_patterns_holder.data";
    
    private static PatternsHolder patternsHolder = null;
        
    public static PatternsHolder getPatternsHolder(){
        
        ConfigProvider config = ConfigProviderFactory.getConfig();
        switch(config.getPredictorConfig().getType()){
            case BASIC:
                try {
                    FileInputStream fis = new FileInputStream(config.getUserID()+"_"+BASIC_PATTERNS_HOLDER_PATH);
                    ObjectInputStream in = new ObjectInputStream(fis);
                    patternsHolder = (BasicPatternsHolder) in.readObject();
                    in.close();   
                    LOG.info("BASIC patterns holder recovered from file.");
                } catch (Exception ex) {
                    LOG.error("Error while getting BASIC patterns holder. A new one will be generated ", ex);
                    patternsHolder = new BasicPatternsHolder();
                } 
                break;
            case ENHANCED:
                try {
                    FileInputStream fis = new FileInputStream(config.getUserID()+"_"+ENHANCED_PATTERNS_HOLDER_PATH);
                    ObjectInputStream in = new ObjectInputStream(fis);
                    patternsHolder = (EnhancedPatternsHolder) in.readObject();
                    in.close();   
                    LOG.info("ENHANCED patterns holder recovered from file.");
                } catch (Exception ex) {
                    LOG.error("Error while getting ENHANCED patterns holder. A new one will be generated ", ex);
                    patternsHolder = new EnhancedPatternsHolder();
                }
                break;
        }
        
        return patternsHolder;        
       
    }
    
    public static void serializePatterns(){
        ConfigProvider config = ConfigProviderFactory.getConfig();
        switch(config.getPredictorConfig().getType()){
            case BASIC:
                try {
                    FileOutputStream fos = new FileOutputStream(config.getUserID()+"_"+BASIC_PATTERNS_HOLDER_PATH);
                    ObjectOutputStream out = new ObjectOutputStream(fos);
                    out.writeObject(patternsHolder);
                    out.close();          

                    LOG.info("BASIC Patterns persisted.");
                } catch (Exception ex) {
                    LOG.error("Exception ", ex);
                }
                break;
            case ENHANCED:
                try {
                    FileOutputStream fos = new FileOutputStream(config.getUserID()+"_"+ENHANCED_PATTERNS_HOLDER_PATH);
                    ObjectOutputStream out = new ObjectOutputStream(fos);
                    out.writeObject(patternsHolder);
                    out.close();          

                    LOG.info("ENHANCED Patterns persisted.");
                } catch (Exception ex) {
                    LOG.error("Exception ", ex);
                }
                break;
        }
    }
    
}
