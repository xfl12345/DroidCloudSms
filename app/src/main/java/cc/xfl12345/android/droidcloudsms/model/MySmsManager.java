package cc.xfl12345.android.droidcloudsms.model;

import static android.telephony.SmsManager.RESULT_NO_DEFAULT_SMS_APP;

import android.app.PendingIntent;
import android.os.RemoteException;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.IIntegerConsumer;

import java.util.ArrayList;
import java.util.List;

public class MySmsManager {
    public static final String TAG = "MySmsManager";

    private static final int RESULT_REMOTE_EXCEPTION = 32;

    private int mSubId = Integer.MAX_VALUE;

    private static MySms mySms = null;

    public MySmsManager() throws ReflectiveOperationException, RemoteException {
        mySms = new MySms();
    }

    private interface SubscriptionResolverResult {
        void onSuccess(int subId);
        void onFailure();
    }

    private static String formatCrossStackMessageId(long id) {
        return "{x-message-id:" + id + "}";
    }

    private static void notifySmsError(PendingIntent pendingIntent, int error) {
        if (pendingIntent != null) {
            try {
                pendingIntent.send(error);
            } catch (PendingIntent.CanceledException e) {
                // Don't worry about it, we do not need to notify the caller if this is the case.
            }
        }
    }

    private static void notifySmsError(List<PendingIntent> pendingIntents, int error) {
        if (pendingIntents != null) {
            for (PendingIntent pendingIntent : pendingIntents) {
                notifySmsError(pendingIntent, error);
            }
        }
    }


    /**
     * Returns the ISms service, or throws an UnsupportedOperationException if
     * the service does not exist.
     */
    private static MySms getISmsServiceOrThrow() {
        if (mySms == null) {
            throw new UnsupportedOperationException("Sms is not supported");
        }
        return mySms;
    }

    private static MySms getISmsService() {
        return mySms;
    }

    /**
     * <p class="note"><strong>Note:</strong> This method used to display a disambiguation dialog to
     * the user asking them to choose a default subscription to send SMS messages over if they
     * haven't chosen yet. Starting in API level 29, we allow the user to not have a default set as
     * a valid option for the default SMS subscription on multi-SIM devices. We no longer show the
     * disambiguation dialog and return {@link SubscriptionManager#INVALID_SUBSCRIPTION_ID} if the
     * device has multiple active subscriptions and no default is set.
     * </p>
     *
     * @return associated subscription ID or {@link SubscriptionManager#INVALID_SUBSCRIPTION_ID} if
     * the default subscription id cannot be determined or the device has multiple active
     * subscriptions and and no default is set ("ask every time") by the user.
     */
    public int getSubscriptionId() {
        return (mSubId == SubscriptionManager.DEFAULT_SUBSCRIPTION_ID)
                ? getISmsServiceOrThrow().getPreferredSmsSubscription() : mSubId;
    }

    private void sendResolverResult(SubscriptionResolverResult resolverResult, int subId,
                                    boolean pickActivityShown) {
        if (subId > SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            resolverResult.onSuccess(subId);
            return;
        }

        if (!pickActivityShown) {
            // Do not fail, return a success with an INVALID subid for apps targeting P or below
            // that tried to perform an operation and the SMS disambiguation dialog was never shown,
            // as these applications may not have been written to handle the failure case properly.
            // This will resolve to performing the operation on phone 0 in telephony.
            resolverResult.onSuccess(subId);
        } else {
            // Fail if the app targets Q or above or it targets P and below and the disambiguation
            // dialog was shown and the user clicked out of it.
            resolverResult.onFailure();
        }
    }


    /**
     * Send a text based SMS.
     *
     * <p class="note"><strong>Note:</strong> Using this method requires that your app has the
     * {@link android.Manifest.permission#SEND_SMS} permission.</p>
     *
     * <p class="note"><strong>Note:</strong> Beginning with Android 4.4 (API level 19), if
     * <em>and only if</em> an app is not selected as the default SMS app, the system automatically
     * writes messages sent using this method to the SMS Provider (the default SMS app is always
     * responsible for writing its sent messages to the SMS Provider). For information about
     * how to behave as the default SMS app, see {@link android.provider.Telephony}.</p>
     *
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *  the current default SMSC
     * @param text the body of the message to send
     * @param sentIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is successfully sent, or failed.
     *  The result code will be <code>Activity.RESULT_OK</code> for success,
     *  or one of these errors:<br>
     *  <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *  <code>RESULT_ERROR_RADIO_OFF</code><br>
     *  <code>RESULT_ERROR_NULL_PDU</code><br>
     *  <code>RESULT_ERROR_NO_SERVICE</code><br>
     *  <code>RESULT_ERROR_LIMIT_EXCEEDED</code><br>
     *  <code>RESULT_ERROR_FDN_CHECK_FAILURE</code><br>
     *  <code>RESULT_ERROR_SHORT_CODE_NOT_ALLOWED</code><br>
     *  <code>RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED</code><br>
     *  <code>RESULT_RADIO_NOT_AVAILABLE</code><br>
     *  <code>RESULT_NETWORK_REJECT</code><br>
     *  <code>RESULT_INVALID_ARGUMENTS</code><br>
     *  <code>RESULT_INVALID_STATE</code><br>
     *  <code>RESULT_NO_MEMORY</code><br>
     *  <code>RESULT_INVALID_SMS_FORMAT</code><br>
     *  <code>RESULT_SYSTEM_ERROR</code><br>
     *  <code>RESULT_MODEM_ERROR</code><br>
     *  <code>RESULT_NETWORK_ERROR</code><br>
     *  <code>RESULT_ENCODING_ERROR</code><br>
     *  <code>RESULT_INVALID_SMSC_ADDRESS</code><br>
     *  <code>RESULT_OPERATION_NOT_ALLOWED</code><br>
     *  <code>RESULT_INTERNAL_ERROR</code><br>
     *  <code>RESULT_NO_RESOURCES</code><br>
     *  <code>RESULT_CANCELLED</code><br>
     *  <code>RESULT_REQUEST_NOT_SUPPORTED</code><br>
     *  <code>RESULT_NO_BLUETOOTH_SERVICE</code><br>
     *  <code>RESULT_INVALID_BLUETOOTH_ADDRESS</code><br>
     *  <code>RESULT_BLUETOOTH_DISCONNECTED</code><br>
     *  <code>RESULT_UNEXPECTED_EVENT_STOP_SENDING</code><br>
     *  <code>RESULT_SMS_BLOCKED_DURING_EMERGENCY</code><br>
     *  <code>RESULT_SMS_SEND_RETRY_FAILED</code><br>
     *  <code>RESULT_REMOTE_EXCEPTION</code><br>
     *  <code>RESULT_NO_DEFAULT_SMS_APP</code><br>
     *  <code>RESULT_RIL_RADIO_NOT_AVAILABLE</code><br>
     *  <code>RESULT_RIL_SMS_SEND_FAIL_RETRY</code><br>
     *  <code>RESULT_RIL_NETWORK_REJECT</code><br>
     *  <code>RESULT_RIL_INVALID_STATE</code><br>
     *  <code>RESULT_RIL_INVALID_ARGUMENTS</code><br>
     *  <code>RESULT_RIL_NO_MEMORY</code><br>
     *  <code>RESULT_RIL_REQUEST_RATE_LIMITED</code><br>
     *  <code>RESULT_RIL_INVALID_SMS_FORMAT</code><br>
     *  <code>RESULT_RIL_SYSTEM_ERR</code><br>
     *  <code>RESULT_RIL_ENCODING_ERR</code><br>
     *  <code>RESULT_RIL_INVALID_SMSC_ADDRESS</code><br>
     *  <code>RESULT_RIL_MODEM_ERR</code><br>
     *  <code>RESULT_RIL_NETWORK_ERR</code><br>
     *  <code>RESULT_RIL_INTERNAL_ERR</code><br>
     *  <code>RESULT_RIL_REQUEST_NOT_SUPPORTED</code><br>
     *  <code>RESULT_RIL_INVALID_MODEM_STATE</code><br>
     *  <code>RESULT_RIL_NETWORK_NOT_READY</code><br>
     *  <code>RESULT_RIL_OPERATION_NOT_ALLOWED</code><br>
     *  <code>RESULT_RIL_NO_RESOURCES</code><br>
     *  <code>RESULT_RIL_CANCELLED</code><br>
     *  <code>RESULT_RIL_SIM_ABSENT</code><br>
     *  <code>RESULT_RIL_SIMULTANEOUS_SMS_AND_CALL_NOT_ALLOWED</code><br>
     *  <code>RESULT_RIL_ACCESS_BARRED</code><br>
     *  <code>RESULT_RIL_BLOCKED_DUE_TO_CALL</code><br>
     *  For <code>RESULT_ERROR_GENERIC_FAILURE</code> or any of the RESULT_RIL errors,
     *  the sentIntent may include the extra "errorCode" containing a radio technology specific
     *  value, generally only useful for troubleshooting.<br>
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").
     *
     * @throws IllegalArgumentException if destinationAddress or text are empty
     */
    public void sendTextMessage(
            String destinationAddress, String scAddress, String text,
            PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextMessageInternal(
                destinationAddress,
                scAddress,
                text,
                sentIntent,
                deliveryIntent,
                true /* persistMessage*/,
                "com.android.mms",
                null,
                0L /* messageId */);
    }




    private void sendTextMessageInternal(String destinationAddress, String scAddress,
                                         String text, PendingIntent sentIntent, PendingIntent deliveryIntent,
                                         boolean persistMessage, String packageName, String attributionTag, long messageId) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        }

        if (TextUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Invalid message body");
        }

        // We will only show the SMS disambiguation dialog in the case that the message is being
        // persisted. This is for two reasons:
        // 1) Messages that are not persisted are sent by carrier/OEM apps for a specific
        //    subscription and require special permissions. These messages are usually not sent by
        //    the device user and should not have an SMS disambiguation dialog associated with them
        //    because the device user did not trigger them.
        // 2) The SMS disambiguation dialog ONLY checks to make sure that the user has the SEND_SMS
        //    permission. If we call resolveSubscriptionForOperation from a carrier/OEM app that has
        //    the correct MODIFY_PHONE_STATE or carrier permissions, but no SEND_SMS, it will throw
        //    an incorrect SecurityException.
        if (persistMessage) {
            resolveSubscriptionForOperation(new SubscriptionResolverResult() {
                @Override
                public void onSuccess(int subId) {
                    MySms mySms = getISmsServiceOrThrow();
                    try {
                        mySms.sendTextForSubscriber(subId, packageName, attributionTag,
                                destinationAddress, scAddress, text, sentIntent, deliveryIntent,
                                persistMessage, messageId);
                    } catch (RemoteException e) {
                        Log.e(TAG, "sendTextMessageInternal: Couldn't send SMS, exception - "
                                + e.getMessage() + " " + formatCrossStackMessageId(messageId));
                        notifySmsError(sentIntent, RESULT_REMOTE_EXCEPTION);
                    }
                }

                @Override
                public void onFailure() {
                    notifySmsError(sentIntent, RESULT_NO_DEFAULT_SMS_APP);
                }
            });
        } else {
            // Not persisting the message, used by sendTextMessageWithoutPersisting() and is not
            // visible to the user.
            MySms mySms = getISmsServiceOrThrow();
            try {
                mySms.sendTextForSubscriber(getSubscriptionId(), packageName, attributionTag,
                        destinationAddress, scAddress, text, sentIntent, deliveryIntent,
                        persistMessage, messageId);
            } catch (RemoteException e) {
                Log.e(TAG, "sendTextMessageInternal (no persist): Couldn't send SMS, exception - "
                        + e.getMessage() + " " + formatCrossStackMessageId(messageId));
                notifySmsError(sentIntent, RESULT_REMOTE_EXCEPTION);
            }
        }
    }

    private void resolveSubscriptionForOperation(SubscriptionResolverResult resolverResult) {
        int subId = getSubscriptionId();
        boolean isSmsSimPickActivityNeeded = false;
        MySms mySms = getISmsService();
        if (mySms != null) {
            // Determines if the SMS SIM pick activity should be shown. This is only shown if:
            // 1) The device has multiple active subscriptions and an SMS default subscription
            //    hasn't been set, and
            // 2) SmsManager is being called from the foreground app.
            // Android does not allow background activity starts, so we need to block this.
            // if Q+, do not perform requested operation if these two operations are not set. If
            // <P, perform these operations on phone 0 (for compatibility purposes, since we
            // used to not wait for the result of this activity).
            isSmsSimPickActivityNeeded = mySms.isSmsSimPickActivityNeeded(subId);
        }
        if (!isSmsSimPickActivityNeeded) {
            sendResolverResult(resolverResult, subId, false /*pickActivityShown*/);
            return;
        }
        // We need to ask the user pick an appropriate subid for the operation.
        Log.d(TAG, "resolveSubscriptionForOperation isSmsSimPickActivityNeeded is true for calling"
                + " package. ");
        try {
            // Create the SMS pick activity and call back once the activity is complete. Can't do
            // it here because we do not have access to the activity context that is performing this
            // operation.
            // Requires that the calling process has the SEND_SMS permission.
            SystemServiceBinderHelper telephonyServiceHelper = new SystemServiceBinderHelper("phone");
            telephonyServiceHelper.executeServiceDeclaredMethod(
                    "enqueueSmsPickResult",
                    null,
                    null,
                    new IIntegerConsumer.Stub() {
                        @Override
                        public void accept(int subId) {
                            // Runs on binder thread attached to this app's process.
                            sendResolverResult(resolverResult, subId, true /*pickActivityShown*/);
                        }
                    }
            );

            // Method telephonyGetter = HiddenApiBypass.getDeclaredMethod(SmsManager.class, "getITelephony");
            // telephonyGetter.setAccessible(true);
            // Object telephonyInstance = Objects.requireNonNull(telephonyGetter.invoke(null));
            // Method enqueueSmsPickResultMethod = HiddenApiBypass.getDeclaredMethod(
            //         telephonyInstance.getClass(),
            //         "enqueueSmsPickResult",
            //         String.class, String.class, IIntegerConsumer.class
            // );
            // enqueueSmsPickResultMethod.invoke(
            //         telephonyInstance,
            //         null, null,
            //         new IIntegerConsumer.Stub() {
            //             @Override
            //             public void accept(int subId) {
            //                 // Runs on binder thread attached to this app's process.
            //                 sendResolverResult(resolverResult, subId, true /*pickActivityShown*/);
            //             }
            //         });

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to launch activity", ex);
            // pickActivityShown is true here because we want to call sendResolverResult and always
            // have this operation fail. This is because we received a RemoteException here, which
            // means that telephony is not available and the next operation to Telephony will fail
            // as well anyways, so we might as well shortcut fail here first.
            sendResolverResult(resolverResult, subId, true /*pickActivityShown*/);
        }
    }


    public void sendMultipartTextMessage(
            String destinationAddress, String scAddress, ArrayList<String> parts,
            ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents,
                deliveryIntents, true /* persistMessage*/, null,
                null, 0L /* messageId */);
    }

    private void sendMultipartTextMessageInternal(
            String destinationAddress, String scAddress, List<String> parts,
            List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents,
            boolean persistMessage, String packageName, String attributionTag,
            long messageId) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        }
        if (parts == null || parts.size() < 1) {
            throw new IllegalArgumentException("Invalid message body");
        }

        if (parts.size() > 1) {
            // We will only show the SMS disambiguation dialog in the case that the message is being
            // persisted. This is for two reasons:
            // 1) Messages that are not persisted are sent by carrier/OEM apps for a specific
            //    subscription and require special permissions. These messages are usually not sent
            //    by the device user and should not have an SMS disambiguation dialog associated
            //    with them because the device user did not trigger them.
            // 2) The SMS disambiguation dialog ONLY checks to make sure that the user has the
            //    SEND_SMS permission. If we call resolveSubscriptionForOperation from a carrier/OEM
            //    app that has the correct MODIFY_PHONE_STATE or carrier permissions, but no
            //    SEND_SMS, it will throw an incorrect SecurityException.
            if (persistMessage) {
                resolveSubscriptionForOperation(new SubscriptionResolverResult() {
                    @Override
                    public void onSuccess(int subId) {
                        try {
                            MySms mySms = getISmsServiceOrThrow();
                            mySms.sendMultipartTextForSubscriber(subId, packageName, attributionTag,
                                    destinationAddress, scAddress, parts, sentIntents,
                                    deliveryIntents, persistMessage, messageId);
                        } catch (RemoteException e) {
                            Log.e(TAG, "sendMultipartTextMessageInternal: Couldn't send SMS - "
                                    + e.getMessage() + " " + formatCrossStackMessageId(messageId));
                            notifySmsError(sentIntents, RESULT_REMOTE_EXCEPTION);
                        }
                    }

                    @Override
                    public void onFailure() {
                        notifySmsError(sentIntents, RESULT_NO_DEFAULT_SMS_APP);
                    }
                });
            } else {
                // Called by apps that are not user facing, don't show disambiguation dialog.
                try {
                    MySms mySms = getISmsServiceOrThrow();
                    if (mySms != null) {
                        mySms.sendMultipartTextForSubscriber(getSubscriptionId(), packageName,
                                attributionTag, destinationAddress, scAddress, parts, sentIntents,
                                deliveryIntents, persistMessage, messageId);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "sendMultipartTextMessageInternal: Couldn't send SMS - "
                            + e.getMessage() + " " + formatCrossStackMessageId(messageId));
                    notifySmsError(sentIntents, RESULT_REMOTE_EXCEPTION);
                }
            }
        } else {
            PendingIntent sentIntent = null;
            PendingIntent deliveryIntent = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                sentIntent = sentIntents.get(0);
            }
            if (deliveryIntents != null && deliveryIntents.size() > 0) {
                deliveryIntent = deliveryIntents.get(0);
            }
            sendTextMessageInternal(destinationAddress, scAddress, parts.get(0),
                    sentIntent, deliveryIntent, true, packageName, attributionTag, messageId);
        }
    }
}
