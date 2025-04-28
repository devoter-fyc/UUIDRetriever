import json
import os
import time
import sys
import socket
import threading as thread
__author__ = "devoter-fyc"
####################
# Define JSON reading and writing functions
def read_from_file(file_path: str):
    if os.path.exists(file_path):
        with open(file_path, 'r+') as file:
            data = json.load(file)
        return data
    else:
        raise FileNotFoundError(f"File {file_path} not found.")
        
# Main code
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
if len(sys.argv) < 3:
    s.bind(('localhost', 8192))
    s.listen(8)
    print("Server started on port 8192")
else:
    s.bind(('localhost', sys.argv[2]))
    s.listen(8)
    print(f"Server started on port {sys.argv[2]}")

# Open the JSON file
if len(sys.argv) < 2:
    print("Specify JSON file as first argument.")
    raise FileNotFoundError
else:
    path = sys.argv[1]

def handle_client(sock: socket.socket, addr: socket._RetAddress):
    print(f"Connection from {addr} has been established.")
    sock.send(b"Welcome.")
    err = 0
    while True:
        try:
            data = sock.recv(2048).decode()
            if not data or data == 'exit':
                print("Exit request received, exiting.")
                break
            x = read_from_file(path)
            if isinstance(x, dict):
                t = x[data]
                sending = json.dumps(t).encode("utf-8")
                sock.send(sending)
            else:
                print("Data format is unexpected, halting.")
                break
        except FileNotFoundError:
            print("File is not found.")
            sock.send(b"File is not found.")
            err = 1
        except json.JSONDecodeError:
            print("JSON format is illegal.")
            sock.send(b"JSON format is illegal.")
            err = 10
        except IOError as e:
            print(f"Caught IOError: {e}")
            err = 2
        except Exception as e:
            print(f"Unknown error occurred: {e}")
            err = -1
        finally:
            sock.close()
            print(f"Connection from {addr} closed.")
        return err

while True:
    sock, addr = s.accept()
    t = thread.Thread(target=handle_client, args=(sock, addr))
    t.start()