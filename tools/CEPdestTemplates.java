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
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class CEPdestTemplates {
    
    //KML template of a single itinary of a particular user.
    public static final String KML_ITINERARY_TEMPLATE = "\t<Placemark>\n" +
                                                        "\t\t<name>ITINERARY_NAME</name>\n" +
                                                        "\t\t<description><![CDATA[Start: START_TIMESTAMP ; End: END_TIMESTAMP ]]></description>\n" +
                                                        "\t\t<styleUrl>STYLE_URL</styleUrl>\n"+
                                                        "\t\t<gx:Track>\n"+
                                                        "ITINERARY_COORDINATES\n"+
                                                        "\t\t</gx:Track>\n"+
                                                        "\t</Placemark>\n";
}
