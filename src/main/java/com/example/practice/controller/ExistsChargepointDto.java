package com.example.practice.controller;

/**
 * This DTO is currently only used to signify existence to OCPP Connector.
 * In the future we might include more parameters or even replace with full Chargepoint info.
 *
 * @param exists indicates if the chargepoint exists in the system or not.
 */
public record ExistsChargepointDto(String exists) {
}
