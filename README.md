# encrypted-chat

encrypted chat program--RSA to share keys and AES128 to encrypt messages


project for Network Security. My modules are client & net.

In client module I developed the chat window GUI and client's
message receiver i.e. the class that listens on the socket for incoming messages and hands them off to GUI controller.

In net module I developed a P2P secure networking architecture resembling master/slave. One user becomes the P2P coordinator
and all other users connect to the coordinator. The coordinator will decrypt messages, update user lists and other state, then 
re-encrypt and broadcast message to all users. Encryption is done with AES128--developed by teammates Chad and Udit and
AES keys are shared via RSA developed by Sriram.  Prasant developed SHA-512 module and also contributed 2 bug fixes 
to my secureConnection class.


some pictures of my chat GUI

![alt text](https://github.com/jcavalieri8619/encrypted-chat/blob/master/chatpic_1.png)

![alt text](https://github.com/jcavalieri8619/encrypted-chat/blob/master/chatpic_2.png)
