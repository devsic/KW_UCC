import socket
import Adafruit_DHT #Adafruit library

sensor=Adafruit_DHT.DHT11
pin=2 #GPIO pin num

sock=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
HOST='192.168.43.10' #ip address
PORT=12345

sock.connect((HOST,PORT))
humidity,temperature=Adafruit_DHT.read_retry(sensor,pin)
if humidity is not None and temperature is not None:
    msg1='Humidity={0:0.1f}%'.format(humidity)
    msg2='temperature={0:0.1f}C'.format(temperature)
    msg=str(msg1+msg2)
    sock.send(msg) #send temp,humidity value
sock.close()