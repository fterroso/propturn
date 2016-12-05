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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Factory that provides the right ConfigProvider for each simulation
 *
 * @author Feranando Terroso-Saenz
 */
public class ConfigProviderFactory {
    
    static String configPath = "";
    static ConfigProvider configProvider = null;
    
    public static ConfigProvider createConfigProvider(String path){
        
        if(path.endsWith(".xml")){
            configProvider = new XMLConfigProvider(path);
        }
        
        return configProvider;        
    }

    public static ConfigProvider getConfig() {
        return configProvider;
    }  
    
    public static String getTestPath(String oS) throws Exception{
        
       oS = oS.toLowerCase();
       oS = oS.replace(" ", "_");
       
       Properties p = new Properties();
       
       p.load(new FileInputStream("operating_system.properties"));
       configPath= p.getProperty(oS+".config.path");
       
       if(!configPath.endsWith(File.separator)){
           configPath = configPath.concat(File.separator);
       }
       return configPath;        
    }    
    
    public static String getTestPath(){
        return configPath;
    }
}
