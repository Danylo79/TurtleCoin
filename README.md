# TurtleCoin
## How to Build  
### Frontend:  
* ``npm run-scipt build``
* Make Sure to have the docker daemon running
* ``docker build -t dankom/turtleportal .``
* ``docker push dankom/turtleportal``  
### Backend:  
* ``mvn install``
* Make Sure to have the docker daemon running
* ``docker build -t dankom/turtle .``
* ``docker push dankom/turtle``
