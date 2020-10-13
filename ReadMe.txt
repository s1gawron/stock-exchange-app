# Stock
Stock web application - first project to my CV - still a lot to add or improve - Technologies: Spring, JPA, Hibernate, Thymeleaf

This app was created to simulate transactions you can do on Warsaw Stock Exchange with real time listings.

If you want to try your best on Stock Markets, but don't want to sink in all your money at the very beginning. Then you have come to right place!
Here you are able to buy, sell stocks and multiply your money!

But to launch this app you need a little bit of programming skills.
First, you have to set up MySQL database and in application.properties file you need to give a propper link, username and password.

After doing so just open terminal in the folder where you have the whole project and enter command: mvn clean install.
This will create in the target file a jar file which you have to run and then in browser enter localhost:8080/{someendpoint}

ENDPOINTS:

localhost:8080/user/login - login to your account !!!
localhost:8080/user/register - register new account !!!

localhost:8080/mainView - view your account statistics, move to your wallet details or stock listings !!!
localhost:8080/stockListings/WIG20 - Warsaw Stock Exchange WIG20 stock listings
localhost:8080/stockListings/WIG40 - Warsaw Stock Exchange WIG40 stock listings
localhost:8080/stockListings/WIG80 - Warsaw Stock Exchange WIG80 stock listings
localhost:8080/myWallet - wallet details

localhost:8080/stockListings/{index}/{ticker}/{action} - buy or sell stock

Endpoints with three exclamation mark are the most important ones. From these endpoints you can do anything without needing to know other endpoints.
