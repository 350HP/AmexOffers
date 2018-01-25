# AmexOffers
Display your Amex Offers in an easy to read format. Sample output will look like this:

<img src="https://i.imgur.com/BTxn6G6.png" />

ELIGIBLE = This offer is available on this card. You should click Add to Card before using this offer.<br />
ENROLLED = This offer has been enrolled on this card already. You can go ahead and use the offer now.<br />
NA       = This offer is not available on this card.<br />

### Steps to use:
1. Install <a href="https://www.google.com/chrome/">Chrome</a> and <a href="https://java.com/en/download/">Java</a> on your machine
2. Download the JAR file from <a href="https://github.com/350HP/AmexOffers/raw/master/AmexOffers.jar">here</a> and run it
3. This will open a new instance of Chrome and prompt you to login to Amex
4. Sit back and watch while the program runs through your different cards
5. Once Chrome closes down, you will find a CSV file with a list of all your offers in the same location as the JAR file

### Other notes:
In case you are worried about what is in the JAR file, use the source code in this repository to build your own JAR file and run it. You will need to add Selenium Libraries and ChromeDriver executiables to your project to get it to work.
