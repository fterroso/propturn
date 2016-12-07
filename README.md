# ProPTurn
##Getting Started
ProPTurn is a real-time personal route prediction. It processes the spatio-temporal data coming from the GPS of a smartphone (timestamp + lat-lon coordinates) and, at the same time the phone's holder is moving, it is able to predict the final destination of his ongoing trip along with the next turning point. 

For this goal, it is based on the Complex Event Processing paradigm. Although the current version only processes data stored in a file (.csv, .kml, etc.) it can be easily adapted to process the spatio-temporal data in real time. Furthremore, its deployment in Android devices is also feasible by means of the Asper library.

## Third-party library dependencies
* Esper 4.11
* log4j 1.2.16
* Commons lang3 3.3.2
* jCoord 1.0
* jDom
* CEP-traj (available [here](https://github.com/fterroso/cep-traj))
* landmark discovery algorithm (available [here](https://github.com/fterroso/landmark-discovery-alg))

## Bibliography

For more info about the features of this application and reference purposes please use:

Terroso-Saenz, F., Valdes-Vela, M., & Skarmeta-Gomez, A. F. (2016). Online route prediction based on clustering of meaningful velocity-change areas. Data Mining and Knowledge Discovery, 1-40.
