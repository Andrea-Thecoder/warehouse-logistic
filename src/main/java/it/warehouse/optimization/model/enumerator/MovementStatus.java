package it.warehouse.optimization.model.enumerator;


import java.util.EnumSet;

public enum MovementStatus {

    SENT,
    RECEIVED,
    PARTIALLY_RECEIVED,
    OVER_RECEIVED,
    IN_TRANSIT,
    CANCELLED,
    TO_SALE;


    public static final EnumSet<MovementStatus> VALID_INSERT_STATUS = EnumSet.of(IN_TRANSIT,TO_SALE);
    public static final EnumSet<MovementStatus> VALID_RECEIVED_STATUS =  EnumSet.of(RECEIVED,PARTIALLY_RECEIVED,OVER_RECEIVED);
}
