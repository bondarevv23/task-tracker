# tasks-tracker

This project is done in connection with my tutoring. I helped student to master the material of the course "Java developer" by Yandex Practicum.

Simple task-tracker. It can store tasks and multitasks (epics).

There are three implementations for 
TaskManager: one stores everything in RAM, second makes file-backup for every change, and the third one use remote server
for storing data (every access to data by any method makes a http-request to server).

Server realization is simple imitation of spring controllers.

Each every manager and for server has corresponded test class.
