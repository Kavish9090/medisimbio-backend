package com.medisimbio.backend.dispensing.entity;

/**
 * State lifecycle of a medicine dispensing order.
 *
 * PENDING             -> Order placed referencing a finalized prescription; awaiting pharmacy processing.
 * ACCEPTED            -> Order accepted by dispensing pharmacy/department.
 * PARTIALLY_DISPENSED -> Portion of ordered medications supplied.
 * DISPENSED           -> All ordered medications completely supplied.
 * CANCELLED           -> Dispensing order cancelled prior to fulfillment.
 * REJECTED            -> Dispensing order rejected by pharmacy (e.g. out of stock, invalid request).
 */
public enum DispensingOrderStatus {
    PENDING,
    ACCEPTED,
    PARTIALLY_DISPENSED,
    DISPENSED,
    CANCELLED,
    REJECTED;

    public boolean canCancel() {
        return this == PENDING;
    }

    public boolean isTerminal() {
        return this == DISPENSED || this == CANCELLED || this == REJECTED;
    }
}
