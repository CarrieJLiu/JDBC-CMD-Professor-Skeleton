/*****************************************************
 * File:  Professor.java Course materials (23W) CST8277
 * 
 * @date December 2022
 * @author Teddy Yap
 */
package jdbccmd;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Simple POJO for professor.
 * 
 * @author Teddy Yap
 * @version December 2022
 */
public class Professor implements Serializable {
	private static final long serialVersionUID = 1L;

	protected int id;
	protected String lastName;
	protected String firstName;
	protected String email;
	protected String phoneNumber;
	//TODO Add the degree and major fields
	protected LocalDateTime created;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	//TODO Add getter and setter methods for the degree and major fields

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Professor)) {
			return false;
		}
		Professor other = (Professor) obj;
		return id != other.id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Professor[id = ").append(id);
		if (lastName != null) {
			builder.append(", lastName = ").append(lastName);
		}
		if (firstName != null) {
			builder.append(", firstName = ").append(firstName);
		}
		if (email != null) {
			builder.append(", email = ").append(email);
		}
		if (phoneNumber != null) {
			builder.append(", phoneNumber = ").append(phoneNumber);
		}
		//TODO Append the degree and major fields here
		if (created != null) {
			builder.append(", created = ").append(created);
		}
		return builder.append("]").toString();
	}
	
}
