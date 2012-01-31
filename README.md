IOIO - CurrentCost bridge
=========================

A basic app to receive electricity consumption data from a CurrentCost 128 / ENVI (and possibly ENVIR) device via a IOIO board to an Android device.

The ultimate goal is to create a little app which can run in the background, store the data locally in a database or blob file and periodically upload to online services like Pachube.

NOTE: At the moment it's not working properly, data received over serial is mostly nonsense characters.