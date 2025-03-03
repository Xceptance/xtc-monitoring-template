package com.xceptance.ordermonitoring.model.configuration;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.Predicate;

public class PaymentPredicate implements Predicate {
	private List<String> acceptablePaymentMethods;

	public PaymentPredicate(List<String> acceptablePaymentMethods) {
		this.acceptablePaymentMethods = acceptablePaymentMethods;
	}

	@Override
	public boolean apply(PredicateContext ctx) {

		JsonObject paymentJson = JsonParser.parseString((ctx.item(Map.class).get("payment_instruments").toString()))
				.getAsJsonArray().get(0).getAsJsonObject();
		String adyenPaymentMethod = paymentJson.get("c_adyenPaymentMethod") != null
				? paymentJson.get("c_adyenPaymentMethod").getAsString()
				: null;
		String payment_method_id = paymentJson.get("payment_method_id") != null
				? paymentJson.get("payment_method_id").getAsString()
				: null;
		String paymentType = paymentJson.get("c_paymentType") != null ? paymentJson.get("c_paymentType").getAsString()
				: null;
		String resultingPaymentMethod = (StringUtils.isNotBlank(adyenPaymentMethod) ? adyenPaymentMethod
				: payment_method_id) + (StringUtils.isNotBlank(paymentType) ? "_" + paymentType : "");
		return acceptablePaymentMethods.contains(resultingPaymentMethod);
	}

}
