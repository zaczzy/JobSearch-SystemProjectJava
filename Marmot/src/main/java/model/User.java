package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


public class User implements Serializable {
	Integer userId;
	String userName;
	String password;
	String firstName;
	String lastName;
	Set<String> subscribedChannel;


	public User(Integer userId, String userName, String password, String firstName, String lastName) {
		this.userId = userId;
		this.userName = userName;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.subscribedChannel = new HashSet<>();
	}

	public Integer getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}


	public Set<String> getSubscribedChannel() {
		return subscribedChannel;
	}

	public void setSubscribedChannel(Set<String> subscribedChannel) {
		this.subscribedChannel = subscribedChannel;
	}

	public void subscribe(String channelName) {
		this.subscribedChannel.add(channelName);
	}
}
