# Stock app
Stock web application - first project to my CV - still a lot to add or improve - Technologies: Spring, JPA, Hibernate

This app was created to simulate transactions you can do on Warsaw Stock Exchange with real time listings.

#Prerequisites:

* Java (this project is using version 11)
* Docker
* Maven

#Running the app:

After pulling repository from GitHub just enter the directory where the code is and enter the command:

``
docker compose up
``

and everything will be done for you automatically.

# Endpoints:

Public endpoints:
* /user/register - register new account
* /stock/{index} - Warsaw Stock Exchange Index stock listings
* /stock/{index}/{ticker} - Warsaw Stock Exchange stock statistics

Secured endpoints:
* /user/myWallet - wallet details, account statistics
* /transaction/perform - buy or sell stock

***Available indexes:***

* WIG20
* WIG40
* WIG80
