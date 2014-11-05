package it.cnr.isti.hpc.dexter.annotate.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User {
	@DatabaseField(generatedId = true)
	private int uid;
	/**
	 * Email
	 */
	@DatabaseField
	private String email;

	private boolean isAdmin = false;

	/**
	 * First Name
	 */
	@DatabaseField(canBeNull = false, format = "UTF-8")
	private String firstName;

	/**
	 * Last Name
	 */
	@DatabaseField(canBeNull = false, format = "UTF-8")
	private String lastName;

	/**
	 * Country
	 */
	private String country;
	@DatabaseField
	private String password;

	/**
	 * Language
	 */
	private String language;

	/**
	 * Full Name
	 */
	@DatabaseField
	private String name;

	/**
	 * Display Name
	 */
	@DatabaseField
	private String displayName;

	/**
	 * Date of Birth
	 */
	private String dob;

	/**
	 * Gender
	 */
	private String gender;

	/**
	 * Location
	 */
	private String location;

	/**
	 * profile image URL
	 */
	@DatabaseField
	private String profileImageURL;

	/**
	 * provider id with this profile associates
	 */
	private String providerId;

	/**
	 * Retrieves the first name
	 * 
	 * @return String the first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Updates the first name
	 * 
	 * @param firstName
	 *            the first name of user
	 */
	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Retrieves the last name
	 * 
	 * @return String the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Updates the last name
	 * 
	 * @param lastName
	 *            the last name of user
	 */
	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Returns the email address.
	 * 
	 * @return email address of the user
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Updates the email
	 * 
	 * @param email
	 *            the email of user
	 */
	public void setEmail(final String email) {
		this.email = email;
	}

	/**
	 * Retrieves the display name
	 * 
	 * @return String the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Updates the display name
	 * 
	 * @param displayName
	 *            the display name of user
	 */
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Retrieves the country
	 * 
	 * @return String the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Updates the country
	 * 
	 * @param country
	 *            the country of user
	 */
	public void setCountry(final String country) {
		this.country = country;
	}

	/**
	 * Retrieves the language
	 * 
	 * @return String the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Updates the language
	 * 
	 * @param language
	 *            the language of user
	 */
	public void setLanguage(final String language) {
		this.language = language;
	}

	/**
	 * Retrieves the full name
	 * 
	 * @return String the full name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Updates the name
	 * 
	 * @param name
	 *            the full name of user
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Retrieves the date of birth
	 * 
	 * @return the date of birth different providers may use different formats
	 */
	public String getDob() {
		return dob;
	}

	/**
	 * Updates the date of birth
	 * 
	 * @param dob
	 *            the date of birth of user
	 */
	public void setDob(final String dob) {
		this.dob = dob;
	}

	/**
	 * Retrieves the gender
	 * 
	 * @return String the gender - could be "Male", "M" or "male"
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * Updates the gender
	 * 
	 * @param gender
	 *            the gender of user
	 */
	public void setGender(final String gender) {
		this.gender = gender;
	}

	/**
	 * Retrieves the location
	 * 
	 * @return String the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Updates the location
	 * 
	 * @param location
	 *            the location of user
	 */
	public void setLocation(final String location) {
		this.location = location;
	}

	/**
	 * Retrieves the profile image URL
	 * 
	 * @return String the profileImageURL
	 */
	public String getProfileImageURL() {
		return profileImageURL;
	}

	/**
	 * Updates the profile image URL
	 * 
	 * @param profileImageURL
	 *            profile image URL of user
	 */
	public void setProfileImageURL(final String profileImageURL) {
		this.profileImageURL = profileImageURL;
	}

	/**
	 * Retrieves the provider id with this profile associates
	 * 
	 * @return the provider id
	 */
	public String getProviderId() {
		return providerId;
	}

	/**
	 * Updates the provider id
	 * 
	 * @param providerId
	 *            the provider id
	 */
	public void setProviderId(final String providerId) {
		this.providerId = providerId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Retrieves the profile info as a string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" id: " + uid + NEW_LINE);
		result.append(" email: " + email + NEW_LINE);
		result.append(" firstName: " + firstName + NEW_LINE);
		result.append(" lastName: " + lastName + NEW_LINE);
		result.append(" country: " + country + NEW_LINE);
		result.append(" language: " + language + NEW_LINE);
		result.append(" name: " + name + NEW_LINE);
		result.append(" displayName: " + displayName + NEW_LINE);
		result.append(" dob: " + dob + NEW_LINE);
		result.append(" gender: " + gender + NEW_LINE);
		result.append(" location: " + location + NEW_LINE);
		result.append(" isAdmin: " + isAdmin + NEW_LINE);

		result.append(" profileImageURL: " + profileImageURL + NEW_LINE);
		result.append(" providerId: " + providerId + NEW_LINE);
		result.append("}");

		return result.toString();

	}

	public int getId() {
		return uid;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public void setId(final int id) {
		this.uid = id;
	}
}
