# Stock app
Stock web application - first project to my CV - still a lot to add or improve - Technologies: Spring, JPA, Hibernate, Thymeleaf

This app was created to simulate transactions you can do on Warsaw Stock Exchange with real time listings.

If you want to try your best on Stock Markets, but don't want to sink in all your money at the very beginning. Then you have come to the right place!
Here you are able to buy, sell stocks and multiply your money!

But to launch this app you need a little bit of programming skills.
First, you have to set up MySQL database and in application.properties file you need to give a propper link, username and password.

After doing so just open terminal in the folder where you have the whole project and enter command: mvn clean install.
This will create in the target file a jar file which you have to run.
App is by default running on port 8080.

# Endpoints:

1. /user/register - register new account
2. /stock/myWallet - wallet details, account statistics
3. /stock/stockListings/{index} - Warsaw Stock Exchange Index stock listings
4. /stock/stockListings/{index}/{ticker} - Warsaw Stock Exchange stock statistics
5. /order/perform - buy or sell stock

Available indexes:

* WIG20
* WIG40
* WIG80
