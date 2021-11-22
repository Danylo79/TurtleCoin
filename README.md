# TurtleCoin
## How to Build  
Make sure have the docker daemon running
### Frontend:  
* ``npm run-scipt build``
* ``docker build -t dankom/turtleportal .``
* ``docker push dankom/turtleportal``  
### Backend:  
* ``mvn install``
* ``docker build -t dankom/turtle .``
* ``docker push dankom/turtle``
