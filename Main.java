import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Main {

	// Class to hold individual offer
	static class Offer implements Comparable<Offer>{
		public String MainOffer;
		public String Merchant;
		public Date ExpiryDate;

		// If the Offer Text and the Merchant is the same, we consider them to be the
		// same
		@Override
		public boolean equals(Object o) {
			Offer cmp = (Offer) o;
			if (this.MainOffer.equals(cmp.MainOffer) && this.Merchant.equals(cmp.Merchant)) {
				return true;
			} else
				return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(MainOffer, Merchant);
		}

		// Constructor to build out the offer
		public Offer(String MainOffer, String Merchant, String DateStr) {
			this.MainOffer = MainOffer;
			this.Merchant = Merchant;

			String Temp = DateStr.replaceAll("[^0-9/]", "");

			if (Temp.isEmpty()) {
				// for today
				this.ExpiryDate = new Date();
			} else if (Temp.matches("^\\d+$")) {
				// for expires in X days
				Date dt = new Date();
				Calendar c = Calendar.getInstance();
				c.setTime(dt);
				c.add(Calendar.DATE, Integer.parseInt(Temp));
				this.ExpiryDate = c.getTime();
			} else {
				// for expiry date
				DateFormat df = new SimpleDateFormat("M/d/y");
				try {
					Date parseresult = df.parse(Temp);
					this.ExpiryDate = parseresult;
				} catch (ParseException pe) {
					this.ExpiryDate = new Date();
				}
			}

		}

		@Override
		public int compareTo(Offer o) {
			return this.Merchant.compareTo(o.Merchant);
		}
	}

	public static void main(String[] args) throws IOException {
		// Open a CSV file to output information
		DateFormat dateFormat = new SimpleDateFormat("MMM-dd hh-mm-a");
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(new File("Amex Offers " + dateFormat.format(new Date()) + ".csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Extract the required Chrome Driver and set it up
		String OS = System.getProperty("os.name");
		String chromeDriverFileName;

		if (OS.contains("Windows"))
			chromeDriverFileName = "chromedriver.exe";
		else if (OS.contains("Mac OS"))
			chromeDriverFileName = "chromedriver.mac";
		else
			chromeDriverFileName = "chromedriver.linux";

		InputStream res = Main.class.getResourceAsStream("/" + chromeDriverFileName);
		FileOutputStream output = new FileOutputStream(chromeDriverFileName);
		byte[] buffer = new byte[4096];
		int bytesRead = res.read(buffer);
		while (bytesRead != -1) {
			output.write(buffer, 0, bytesRead);
			bytesRead = res.read(buffer);
		}
		output.close();
		res.close();

		if (OS.contains("Windows"))
			System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "\\" + chromeDriverFileName);
		else {
			File f = new File(chromeDriverFileName);
			f.setExecutable(true, true);
			System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/" + chromeDriverFileName);
		}

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");

		WebDriver driver = new ChromeDriver(options);
		WebDriverWait wait = new WebDriverWait(driver, 20);

		// Start Processing Offers
		StringBuilder builder = new StringBuilder();
		driver.get("https://global.americanexpress.com/accounts");
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Add Another Card']")));

		int noOfCards = driver.findElements(By.xpath("//div[starts-with(@class,'app__card__')]")).size();

		ArrayList<Offer> offersList = new ArrayList<Offer>();
		ArrayList<String> cardsList = new ArrayList<String>();
		HashMap<Offer, String> offersRelation = new HashMap<Offer, String>();

		for (int i = 0; i < noOfCards; i++) {
			List<WebElement> allCards = driver.findElements(By.xpath("//div[starts-with(@class,'app__card__')]"));
			WebElement card = allCards.get(i);

			String cardName = card.findElement(By.xpath(".//span[@class='heading-3']")).getText();
			cardsList.add(cardName);

			WebElement selectCard = card
					.findElement(By.xpath(".//button[starts-with(@class,'btn-block btn-block-center')]"));

			if (selectCard.getText().contains("Account Selected")) {
				WebElement backButton = driver.findElement(
						By.xpath("//a[starts-with(@class,'main-menu dls-accent-white-01 position-absolute')]"));
				backButton.click();
			} else {
				selectCard.click();
			}

			wait.until(ExpectedConditions
					.elementToBeClickable(By.xpath("//span[starts-with(@class,'GlobalHeader__closed___')]")));
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Frequently Asked Questions']")));

			// after page loaded
			if (!driver.getCurrentUrl().contains("offers")) {
				WebElement viewAll = driver.findElement(By.xpath("//a[text()='View All']"));
				wait.until(ExpectedConditions.elementToBeClickable(viewAll));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				viewAll.click();
			}

			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Frequently Asked Questions']")));

			// process eligible offers
			driver.findElement(By.xpath("//a[@data-view-name='ELIGIBLE']")).click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Frequently Asked Questions']")));
			List<WebElement> offers = driver
					.findElements(By.xpath("//section[@class='offers-list']/section/div[starts-with(@id,'offer-')]"));
			for (WebElement currentOfferElement : offers) {
				Offer TempOffer = new Offer(
						currentOfferElement.findElement(By.xpath(".//p[starts-with(@class,'heading-3')]")).getText(),
						currentOfferElement.findElement(By.xpath(".//p[starts-with(@class,'body-1')]")).getText(),
						currentOfferElement.findElement(By.xpath(".//div[starts-with(@class,'offer-expires')]"))
								.getText());
				if (!offersList.contains(TempOffer))
					offersList.add(TempOffer);

				offersRelation.put(TempOffer,
						(offersRelation.get(TempOffer) == null ? "" : offersRelation.get(TempOffer))
								+ cardsList.indexOf(cardName) + ",ELIGIBLE;");
			}

			// process enrolled offers
			driver.findElement(By.xpath("//a[@data-view-name='ENROLLED']")).click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Frequently Asked Questions']")));
			offers = driver
					.findElements(By.xpath("//section[@class='offers-list']/section/div[starts-with(@id,'offer-')]"));
			for (WebElement currentOfferElement : offers) {
				Offer TempOffer = new Offer(
						currentOfferElement.findElement(By.xpath(".//p[starts-with(@class,'heading-3')]")).getText(),
						currentOfferElement.findElement(By.xpath(".//p[starts-with(@class,'body-1')]")).getText(),
						currentOfferElement.findElement(By.xpath(".//div[starts-with(@class,'offer-expires')]"))
								.getText());
				if (!offersList.contains(TempOffer))
					offersList.add(TempOffer);

				offersRelation.put(TempOffer,
						(offersRelation.get(TempOffer) == null ? "" : offersRelation.get(TempOffer))
								+ cardsList.indexOf(cardName) + ",ENROLLED;");
			}

			if (i == noOfCards - 1)
				break;

			driver.get("https://global.americanexpress.com/accounts");
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Add Another Card']")));

		}

		driver.quit();
		File file = new File(chromeDriverFileName);
		file.delete();

		// start building the output file
		// header
		builder.append("Offer,Merchant,Date");
		for (String s : cardsList) {
			builder.append("," + s);
		}
		builder.append("\n");

		// build each offer
		dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Collections.sort(offersList);
		for (Offer o : offersList) {
			builder.append('"' + o.MainOffer + "\",\"" + o.Merchant + "\"," + dateFormat.format(o.ExpiryDate));
			String TempStr = offersRelation.get(o);
			TempStr = TempStr.substring(0, TempStr.length() - 1);
			HashMap<Integer, String> TempMap = new HashMap<Integer, String>();
			for (String s : TempStr.split(";")) {
				String[] tempsplit = s.split(",");
				TempMap.put(Integer.parseInt(tempsplit[0]), tempsplit[1]);
			}

			for (int i = 0; i < noOfCards; i++) {
				if (TempMap.containsKey(i))
					builder.append("," + TempMap.get(i));
				else
					builder.append(",NA");
			}
			builder.append("\n");
		}
		printWriter.write(builder.toString());
		printWriter.close();

	}

}
