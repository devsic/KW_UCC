# KW_UCC
using raspberry pi and detecting environment sensor

# How to get temperature&humidity value
1.Adafruit library package install


  sudo apt-get install -y python3 python3-pip python-dev
  
  
  sudo pip3 install rpi.gpio 
  
  
  
  
2.DHT library package install


  sudo apt-get update
  
  
  sudo apt-get install build-essential python-dev
  
  
3.Port and Sensor set and run


  cd Adafruit_DHT
  
  
  cd examples
  
  
  sudo ./AdafruitDHT.py 11 2 (Temperature sensor:11, gpoi port num:2)
  
  
