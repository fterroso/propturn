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
package cepdest.stats;

import java.io.File;
import java.io.FileFilter;


/**
 * Filter to only select only those files related to a particular stats target
 * and a configuration.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class StatsFileFilter implements FileFilter{
    
    String fileType;
    String config;


    public StatsFileFilter(String fileType, String config) {
        this.fileType = fileType;
        this.config = config;
    }

    
    @Override
    public boolean accept(File pathName) {
        
        String fileName = pathName.getName();
        if(fileName.contains(fileType) && fileName.contains(config)){
            return true;
        }
        
        return false;

    }
    
}
