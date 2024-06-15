package io.bankbridge.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.ArrayList;

public class BankModel {
	
	private String bic;
	private String name;
	private String countryCode;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonView(Views.RemoteCall.class)
	private String auth;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonView(Views.Cache.class)
	private ArrayList products;

	public String getBic() {
		return bic;
	}

	public void setBic(String bic) {
		this.bic = bic;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public ArrayList getProducts() {
		return products;
	}

	public void setProducts(ArrayList products) {
		this.products = products;
	}
}
