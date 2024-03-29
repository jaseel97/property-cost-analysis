package finalproject;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Main {

	public static void main(String[] args) {
		System.out.println("\nProperty Cost Analysis");
		System.out.println("------------------------------------------------------------------------------------------------");
		Gson gson = new Gson();

		// Read JSON data into a JsonArray
		JsonArray propertyList = null;
		try (FileReader reader = new FileReader("RealtorProperties.json")) {
			propertyList = gson.fromJson(reader, JsonArray.class);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		Scanner inputReader = new Scanner(System.in);
		String searchOption;
		do {
			System.out.println("\n\nSelect search option:");
			System.out.println("\n1. Search by City");
			System.out.println("2. Search by Province");
			System.out.println("3. Search by Pincode");
			System.out.println("4. Search with Price Range");
			System.out.println("5. Search with City, Price Range, Number of Bedrooms & Bathrooms");
			System.out.println("6. Exit");
			searchOption = inputReader.nextLine();

			switch (searchOption) {
			case "1":
				System.out.println("\nEnter city:");
				String searchedCity = inputReader.nextLine();
				boolean isValidInput = validateStringInput(searchedCity);
				if(isValidInput) 
					searchByProperty(propertyList, searchedCity, "city");
				else
					System.out.println("Invalid city name. Please enter a valid city with letters only.");
				break;
			case "2":
				System.out.println("\nEnter province:");
				String searchedProvince = inputReader.nextLine();
				boolean isValidInput2 = validateStringInput(searchedProvince);
				if(isValidInput2) 
					searchByProperty(propertyList, searchedProvince, "province");
				else
					System.out.println("Invalid province name. Please enter a valid province with letters only.");
				break;
			case "3":
				System.out.println("\nEnter pincode:");
				String searchedPincode = inputReader.nextLine();
				boolean isValidInput3 = validatePincodeInput(searchedPincode);
				if(isValidInput3) {
					searchedPincode = searchedPincode.replace(" ", "");
					searchByProperty(propertyList, searchedPincode, "pincode");
				}
				else
					System.out.println("Invalid Pincode. Please enter a valid Canadian postal code!");
				break;
			case "4":
				System.out.println("\nEnter the lower price range:");
				String lowerPriceString = inputReader.nextLine();
				boolean isValidInput4 = validatePriceInput(lowerPriceString);
				if(isValidInput4) {
					System.out.println("\nEnter the higher price range:");
					String higherPriceString = inputReader.nextLine();
					isValidInput4 = validatePriceInput(higherPriceString);
					if(isValidInput4) {
						BigDecimal lowerPropertyPrice = new BigDecimal(lowerPriceString);
						BigDecimal higherPropertyPrice = new BigDecimal(higherPriceString);
						searchByPriceRange(propertyList, lowerPropertyPrice, higherPropertyPrice);
					} else
						System.out.println("Invalid price format. Please enter a valid price in numbers only! (Decimal points can be used)");
				}
				else
					System.out.println("Invalid price format. Please enter a valid price in numbers only! (Decimal points can be used)");
				break;
			case "5":
				System.out.println("\nEnter city:");
				String searchedCity2 = inputReader.nextLine();
				boolean isCityValidInput = validateStringInput(searchedCity2);
				if(isCityValidInput) {
					System.out.println("\nEnter the lower price range:");
					String lowerPriceString2 = inputReader.nextLine();
					boolean isLowerPriceInputValid = validatePriceInput(lowerPriceString2);
					if(isLowerPriceInputValid) {
						System.out.println("\nEnter the higher price range:");
						String higherPriceString = inputReader.nextLine();
						boolean isHigherPriceInputValid = validatePriceInput(higherPriceString);
						if(isHigherPriceInputValid) {
							System.out.println("\nEnter number of bedrooms:");
							String noOfBedrooms = inputReader.nextLine();
							boolean validBedrooms = isValidInteger(noOfBedrooms);
							if(validBedrooms) {
								System.out.println("\nEnter number of bathrooms:");
								String noOfBathrooms = inputReader.nextLine();
								boolean validBathrooms = isValidInteger(noOfBathrooms);
								if(validBathrooms) {
									searchByCityPriceBedroomsAndBathrooms(propertyList, searchedCity2, lowerPriceString2, higherPriceString, noOfBedrooms, noOfBathrooms);
								}
								else
									System.out.println("Invalid input. Please enter a valid positive number!");
							}
							else
								System.out.println("Invalid input. Please enter a valid positive number!");
						} else
							System.out.println("Invalid price format. Please enter a valid price in numbers only! (Decimal points can be used)");
					}
					else
						System.out.println("Invalid price format. Please enter a valid price in numbers only! (Decimal points can be used)");
				}
				else
					System.out.println("Invalid city name. Please enter a valid city with letters only.");
				break;
			case "6":
				System.out.println("Exiting...");
				break;
			default:
				System.out.println("Invalid option. Please enter a valid option!");
			}
		} while (!searchOption.equals("6"));
		inputReader.close();

	}

	public static void searchByProperty(JsonArray propertyList, String searchedInput, String searchFactor) {
		int propertyCount = 0;
		for (JsonElement element : propertyList) {
			JsonObject property = element.getAsJsonObject();
			JsonElement cityElement = property.get(searchFactor);
			if (cityElement != null && cityElement.getAsString().equalsIgnoreCase(searchedInput)) {
				DisplayFormatter.printPropertyDetails(property);
				propertyCount++;
			} 
		}
		if(propertyCount == 0) {
			List<String> autoCompletedWords = wordCompletion2.call(searchFactor, searchedInput);
			if (autoCompletedWords.isEmpty()) {
				String suggestion = SpellChecker.call(searchFactor, searchedInput);
				if(suggestion != null) {
					System.out.println("Did you mean: "+suggestion.substring(0, 1).toUpperCase() + suggestion.substring(1));
				}
				else
					System.out.println("No property found with the provided "+searchFactor+"!");
	        } else {
	        	System.out.print("Did you mean: ");
	        	//To print word alone if single suggestion
	        	if (autoCompletedWords.size() == 1) {
	        	    String completedWord = autoCompletedWords.get(0);
	        	    System.out.println(completedWord.substring(0, 1).toUpperCase() + completedWord.substring(1));
	        	} else {
	        		// to print words separated by a comma if multiple suggestions are present
	        	    for (int i = 0; i < autoCompletedWords.size(); i++) {
	        	        String completedWord = autoCompletedWords.get(i);
	        	        System.out.print(completedWord.substring(0, 1).toUpperCase() + completedWord.substring(1));
	        	        
	        	        // If it's not the last word, print a comma
	        	        if (i < autoCompletedWords.size() - 1) {
	        	            System.out.print(", ");
	        	        }
	        	    }
	        	}
	        }
		}
	}
	
	public static void searchByCityPriceBedroomsAndBathrooms(JsonArray propertyList, String cityInput, String lowerPriceString2, String higherPriceString, String noOfBedrooms, String noOfBathrooms) {
        int propertyCount = 0;
        BigDecimal lowerPriceRangeValue = new BigDecimal(lowerPriceString2);
        BigDecimal higherPriceRangeValue = new BigDecimal(higherPriceString);
        int bedroomsInputValue = Integer.parseInt(noOfBedrooms);
        int bathroomsInputValue = Integer.parseInt(noOfBathrooms);
        for (JsonElement element : propertyList) {
            JsonObject property = element.getAsJsonObject();
            JsonElement cityElement = property.get("city");
            JsonElement priceElement = property.get("price");
            JsonElement bedroomsElement = property.get("bedrooms");
            JsonElement bathroomsElement = property.get("bathrooms");
            
            if (cityElement != null && cityElement.getAsString().equalsIgnoreCase(cityInput)) {
                BigDecimal price = priceElement.getAsBigDecimal();
                int bedrooms = bedroomsElement.getAsInt();
                int bathrooms = bathroomsElement.getAsInt();
                
                // Check if the price is within the specified range
                if (price.compareTo(lowerPriceRangeValue) >= 0 && price.compareTo(higherPriceRangeValue) <= 0) {
                    // Check if the number of bedrooms and bathrooms match the specified criteria
                    if (bedrooms == bedroomsInputValue && bathrooms == bathroomsInputValue) {
                        DisplayFormatter.printPropertyDetails(property);
                        propertyCount++;
                    }
                }
            } 
        }
        if(propertyCount == 0) 
            System.out.println("No properties found with the provided criteria!");
    }

	public static void searchByPriceRange(JsonArray propertyList, BigDecimal lowerPriceInput, BigDecimal higherPriceInput) {
		int propertyCount = 0;
		for (JsonElement element : propertyList) {
			JsonObject property = element.getAsJsonObject();
			JsonElement priceElement = property.get("price");
			if (priceElement != null && priceElement.isJsonPrimitive()) {
				BigDecimal propertyPrice = priceElement.getAsBigDecimal();
				if (propertyPrice.compareTo(lowerPriceInput) >= 0 && propertyPrice.compareTo(higherPriceInput) <= 0) {
					DisplayFormatter.printPropertyDetails(property);
					propertyCount++;
				}
			}
		}
		if (propertyCount == 0) {
			System.out.println("No property found in the provided price range!");
		}
	}

	public static boolean validateStringInput(String userInput) {
		String stringRegex = "^[a-zA-Z]+(?:[\\s-][a-zA-Z]+)*$";
		// Compile the regex pattern
		Pattern pattern = Pattern.compile(stringRegex);
		// Match the user input against the regex pattern
		Matcher matcher = pattern.matcher(userInput);
		return matcher.matches();
	}

	public static boolean validatePincodeInput(String userInput) {
		// Regular expression for validating Canadian postal codes
		String postalCodeRegex = "^[A-Za-z]\\d[A-Za-z]\\s?\\d[A-Za-z]\\d$";
		Pattern pattern = Pattern.compile(postalCodeRegex);
		Matcher matcher = pattern.matcher(userInput);
		return matcher.matches();
	}

	public static boolean validatePriceInput(String userInputPrice) {
		// Regular expression for validating price (allows positive decimal numbers)
		String priceRegex = "^\\d+(\\.\\d+)?$";
		Pattern pattern = Pattern.compile(priceRegex);
		Matcher matcher = pattern.matcher(userInputPrice);
		return matcher.matches();
	}
	
	public static boolean isValidInteger(String input) {
        // Regular expression to match an integer (positive or negative)
        String regex = "[-+]?\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }
}