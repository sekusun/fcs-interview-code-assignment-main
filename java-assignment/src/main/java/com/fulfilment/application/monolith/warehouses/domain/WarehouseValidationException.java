package com.fulfilment.application.monolith.warehouses.domain;

public class WarehouseValidationException extends RuntimeException {

  private final int statusCode;

  public WarehouseValidationException(String message) {
    this(message, 400);
  }

  public WarehouseValidationException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
