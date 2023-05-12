package cc.xfl12345.android.droidcloudsms.model;

import android.app.PendingIntent;
import android.os.RemoteException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class ISms {
    protected SystemServiceBinderHelper serviceBinderHelper;

    private Method methodIsSmsSimPickActivityNeeded;

    private Method methodGetPreferredSmsSubscription;

    private Method methodSendTextForSubscriber;


    public ISms() throws ReflectiveOperationException, RemoteException {
        serviceBinderHelper = new SystemServiceBinderHelper("isms");

        methodIsSmsSimPickActivityNeeded = Objects.requireNonNull(
            serviceBinderHelper.getServiceDeclaredMethod("isSmsSimPickActivityNeeded", int.class)
        );

        methodGetPreferredSmsSubscription = Objects.requireNonNull(
            serviceBinderHelper.getServiceDeclaredMethod("getPreferredSmsSubscription")
        );

    }


    public boolean isSmsSimPickActivityNeeded(int subId) {
        try {
            return (boolean) methodIsSmsSimPickActivityNeeded.invoke(serviceBinderHelper.getServiceInstance(), subId);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public int getPreferredSmsSubscription() {
        try {
            return (int) methodGetPreferredSmsSubscription.invoke(serviceBinderHelper.getServiceInstance());
        } catch (ReflectiveOperationException e) {
            // SubscriptionManager.INVALID_SUBSCRIPTION_ID
            return -1;
        }
    }

    public void sendTextForSubscriber(
            int subId,
            String callingPackage,
            String callingAttributionTag,
            String destAddr,
            String scAddr,
            String text,
            PendingIntent sentIntent,
            PendingIntent deliveryIntent,
            boolean persistMessageForNonDefaultSmsApp,
            long messageId) throws RemoteException {
        try {
            serviceBinderHelper.executeServiceDeclaredMethod(
                    "sendTextForSubscriber",
                    subId,
                    callingPackage,
                    callingAttributionTag,
                    destAddr,
                    scAddr,
                    text,
                    sentIntent,
                    deliveryIntent,
                    persistMessageForNonDefaultSmsApp,
                    messageId
            );
        } catch (ReflectiveOperationException e) {
            throw new RemoteException(e.getMessage());
        }
    }


    public void sendMultipartTextForSubscriber(
            int subId,
            String callingPkg,
            String callingAttributionTag,
            String destinationAddress,
            String scAddress,
            List<String> parts,
            List<PendingIntent> sentIntents,
            List<PendingIntent> deliveryIntents,
            boolean persistMessageForNonDefaultSmsApp,
            long messageId) throws RemoteException {
        try {
            serviceBinderHelper.executeServiceDeclaredMethod(
                    "sendMultipartTextForSubscriber",
                    subId,
                    callingPkg,
                    callingAttributionTag,
                    destinationAddress,
                    scAddress,
                    parts,
                    sentIntents,
                    deliveryIntents,
                    persistMessageForNonDefaultSmsApp,
                    messageId
            );
        } catch (ReflectiveOperationException e) {
            throw new RemoteException(e.getMessage());
        }
    }


}
