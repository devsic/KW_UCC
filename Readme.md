# How to get temperature&humidity value
1.Adafruit library package install


  sudo apt-get install -y python3 python3-pip python-dev


  sudo pip3 install rpi.gpio 




2.DHT library package install


  sudo apt-get update


  sudo apt-get install build-essential python-dev


3.Port and Sensor set and run



  git clone https://github.com/adafruit/Adafruit_Python_DHT.git

  cd Adafruit_Python_DHT
  
  
  sudo python setup.py install


  cd examples


  sudo ./AdafruitDHT.py 11 2 (Temperature sensor:11, gpoi port num:2)


# How to get acceleration value
1. git clone https://github.com/adafruit/Adafruit_Python_ADXL345

2. Port - raspberry pi 1:3.3v 3. SDA 5. SCL 6. GND 
