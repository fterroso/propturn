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

import cepdest.itinerary.Itinerary;

/**
 * Class that processes and generates all the statistical data of the system.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public interface StatsGenerator {
    
    /**
     * Registers that a new itinerary has come to an end.
     *
     * @param itinierary the ended itinerary.
     */
    public void itineraryHasFinished(Itinerary itinierary);
    
    /**
     * This method generates the stats of a user.
     */
    public void generateUserStats();
    
}
