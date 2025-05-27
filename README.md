# ParkingBuddy
Welcome to ParkingBuddy! The website where you can see up-to-date parkingstation-availability and get predictions for future availability.

## How to get started
Open the class 'ParkingBuddyApplication' and run as 'Springboot App' (you'll need to wait less than a minute for it to be ready). Then open your browser on 'http://localhost:8080/'. Et-voilà: you can use our website!  
In order to get the best predictions, a function will be automatically executed every day at 10:00 am to update the data files used for the prediction. However, since our website is not online, this will not be done, if the website is not running at that time. Therefore, **we advise to update the latest data manually** . You can do this by running the 'HistoricalData' class once, which will need approximately 20 minutes to complete.

## Internal structure
We used java, html, css, javascript, springboot and data from opendatahub.com to realize this project.  
Our classes are divided into the components: 
- data getter: here we retrieve the data from the Open Data Hub and create classes from it
- data storage: to save data inside and read from files
- predicition: everything regarding the prediction
- website: here we setup the website via controller and added a service to save the data models and a scheduler to automatically download data files
  
Our system has the following architectural higlights:  
**Extensibility:**  
Different kinds of data from the open data hub can be retrieved.  
 - add subclass of „getData“ for implementing the logic to get the data
 - add a subclass of “OpenData”, in which the data Objects are stored  
Any subclass of OpenData can be stored with the CSVFile class
 - add a subclass of “OpenData” and call the CSVFile methods to read and save data  
Storage classes to save Objects in different datatypes can be added
 - Add a storage class, which implements the interfaces saveData and ReadData  
Model classes to make different data models can be added
 - Add new models, which implement the interface Model  
**Scalability:**  
New Parking stations don’t affect the architecture
 - The implementation can handle a change in the number of parking slots  
**Efficiency:**  
Minimal request of data from the OpenDataHub
 - Historical Data is requested and stored into files; no request for every website interaction is needed, what saves time and energy  
Minimal generation of data models
 - The models for each parking station are generated during the initialization of the program; no need to generate them every time a user asks for a prediction  
**Division of Tasks:**  
Logic separation of software components into packages
 - The change of one part of the system, does not change the others  

To realize this, we used reflection and division of components.

## SCRUM
- 6th May -> SpringBoot tutorial, DataGetter

- 12th May -> 
  - Map (Elisabeth)
  - Prediction (Johanna)
  - Filter Location, Search Bar Parking

- 15th May ->
  - Graph (done)
  - Prediction (Johanna)
  - Data get Geschwindigkeit (Elisa)
  - Map -> arrays/ArrayList (Elisa)
  - Parkplätze auf Map anzeigen (Elisabeth)
  - Tag auswählen für Prediction/ Website (Johanna)

- 19th May ->
  - New functionality*
  - Test
  - Parking nearby 

- 26th May
  - Project Finished
  - Prepare Poster
  - Print Poster



- 30th May -> Open Data Hub Day Presentation
