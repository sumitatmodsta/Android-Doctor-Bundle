package com.modastadoc.doctors.common.interfaces;

import com.modastadoc.doctors.model.Appointment;

/**
 * Created by kunasi on 17/08/17.
 */

public interface IApmtDialogListener {

    /**
     * Callback tells when call option is clicked in alert dialog
     *
     * @param appointment  clicked Appointment in alert dialog.
     */
    void onCallClicked(Appointment appointment);

    /**
     * Callback tells when cancel option is clicked in alert dialog
     *
     * @param appointment clicked Appointment in alert dialog.
     */
    void onCancelClicked(Appointment appointment);

    /**
     * Callback tells when delay option is clicked in alert dialog
     *
     * @param appointment  clicked Appointment in alert dialog.
     */
    void onDelayClicked(Appointment appointment);

    /**
     * Callback tells when summary option is clicked in alert dialog
     *
     * @param appointment  clicked Appointment in alert dialog.
     */
    void onSummaryClicked(Appointment appointment);

    /**
     * Callback tells when delay option is clicked in alert dialog
     *
     * @param appointment  clicked Appointment in alert dialog.
     */
    void onViewDetailsClicked(Appointment appointment);
}
