Justin,

To use this code, start MouseCatcher and DataReceiver on your computer. Make sure 
MouseTracker says "Inactive" and not "Connecting To Server..." in its title bar.
Then click on the MouseCatcher window so it says "Active" and start moving your mouse 
around. After a short delay (I think due to my use of a BufferedReader) DataReceiver
will begin displaying coordinates it has received from MouseCatcher.

If everything goes right, you should be able to plug in your Arduino to your computer 
via USB, set the appropriate port name (i.e. "COM*" where * is the number corresponding 
to the USB COM port) in DataReceiver as the "portName" class variable, and DataReceiver 
will transmit the coordinates to the Arduino as it receives them. You'll know it's 
working if you stop getting errors in DataReceiver's console.

DataReceiver will send coordinates to the Arduino one 8-bit byte at a time. Data will 
come across in the following format:

[0] [1-253] [1-253] [0] [1-253] [1-253] ...

The byte '0' is the separator character which arrives before every coordinate pair. The 
next two bytes are the x and y values of the coordinate pair, respectively, and range 
from 1-253 inclusive.

DataReceiver will also send a byte with the value of 254 or 255 whenever the user 
toggles the laser on or off by clicking on the MouseTracker window or moves the cursor 
out of the window (which is registered as a 'laser off' command for safety reasons). A 
value of 254 indicates that the laser should be turned on, and a value of 255 indicates 
that it should be turned off.

To use this program with two computers, just change the hostname in MouseCatcher to the 
IP address of the computer where DataReceiver is running and it should work. 
 
I think that's everything - let me know if you have any questions. Of course, any 
changes you'd like to make to this code are fine with me.

Graham